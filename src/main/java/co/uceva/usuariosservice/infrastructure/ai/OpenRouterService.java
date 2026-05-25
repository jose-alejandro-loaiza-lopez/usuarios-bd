package co.uceva.usuariosservice.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenRouterService {

    private static final String OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";

    @Value("${openrouter.api-key}")
    private String apiKey;

    @Value("${openrouter.model}")
    private String model;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @SuppressWarnings("unchecked")
    public Map<String, Object> preguntar(String mensaje, List<Map<String, Object>> favoritos,
                                          List<Map<String, Object>> resultadosBusqueda,
                                          String toolCallId, String toolArguments) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("OPENROUTER_API_KEY no configurada");
        }

        try {
            String systemPrompt = construirSystemPrompt(favoritos);

            // ---- FASE 2: Viene con resultados de búsqueda ----
            if (toolCallId != null && !toolCallId.isBlank()
                    && resultadosBusqueda != null && !resultadosBusqueda.isEmpty()) {
                return procesarResultadosBusqueda(mensaje, resultadosBusqueda, toolCallId, toolArguments, systemPrompt);
            }

            // ---- FASE 1: Envio inicial con tools ----
            List<Map<String, Object>> tools = construirToolBuscarEnTiendas();
            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", mensaje));

            JsonNode data = llamarOpenRouter(messages, tools);
            JsonNode choice = extraerPrimerChoice(data);

            if (choice == null) {
                log.error("OpenRouter: sin choices en la respuesta");
                return null;
            }

            JsonNode message = choice.get("message");
            if (message == null) {
                log.error("OpenRouter: mensaje sin field 'message'");
                return null;
            }

            JsonNode toolCalls = message.get("tool_calls");

            if (toolCalls != null && toolCalls.isArray() && !toolCalls.isEmpty()) {
                JsonNode tc = toolCalls.get(0);
                JsonNode function = tc.get("function");
                if (function != null && "buscarEnTiendas".equals(function.get("name").asText())) {
                    String arguments = function.get("arguments").asText();
                    String query = objectMapper.readTree(arguments).get("query").asText();
                    log.info("IA solicitó busqueda: {}", query);

                    Map<String, Object> result = new HashMap<>();
                    result.put("action", "search");
                    result.put("query", query);
                    result.put("toolCallId", tc.get("id").asText());
                    result.put("arguments", arguments);
                    return result;
                }
            }

            String texto = message.has("content") && !message.get("content").isNull()
                    ? message.get("content").asText() : "";
            return Map.of("respuesta", texto);
        } catch (Exception e) {
            log.error("Error al comunicarse con OpenRouter", e);
            return null;
        }
    }

    private Map<String, Object> procesarResultadosBusqueda(
            String mensaje, List<Map<String, Object>> resultadosBusqueda,
            String toolCallId, String toolArguments,
            String systemPrompt) throws Exception {

        String toolResultsJson = objectMapper.writeValueAsString(resultadosBusqueda);

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(nuevoMap("role", "system", "content", systemPrompt));
        messages.add(nuevoMap("role", "user", "content", mensaje));

        Map<String, Object> assistantMsg = new HashMap<>();
        assistantMsg.put("role", "assistant");
        assistantMsg.put("content", null);
        Map<String, Object> tc = new HashMap<>();
        tc.put("id", toolCallId);
        tc.put("type", "function");
        Map<String, String> fn = new HashMap<>();
        fn.put("name", "buscarEnTiendas");
        fn.put("arguments", toolArguments);
        tc.put("function", fn);
        assistantMsg.put("tool_calls", List.of(tc));
        messages.add(assistantMsg);

        Map<String, Object> toolMsg = new HashMap<>();
        toolMsg.put("role", "tool");
        toolMsg.put("tool_call_id", toolCallId);
        toolMsg.put("content", toolResultsJson);
        messages.add(toolMsg);

        JsonNode data = llamarOpenRouter(messages, null);
        JsonNode choice = extraerPrimerChoice(data);

        if (choice == null) {
            log.error("OpenRouter: sin choices al procesar resultados");
            return null;
        }

        JsonNode message = choice.get("message");
        String texto = (message != null && message.has("content") && !message.get("content").isNull())
                ? message.get("content").asText() : "";

        Map<String, Object> result = new HashMap<>();
        result.put("respuesta", texto);
        return result;
    }

    private JsonNode extraerPrimerChoice(JsonNode data) {
        if (data == null) return null;
        JsonNode choices = data.get("choices");
        if (choices == null || !choices.isArray() || choices.isEmpty()) {
            if (data.has("error")) {
                log.error("OpenRouter error en body: {}", data.get("error"));
            }
            return null;
        }
        return choices.get(0);
    }

    private JsonNode llamarOpenRouter(List<Map<String, Object>> messages, List<Map<String, Object>> tools) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        boolean tieneTools = tools != null && !tools.isEmpty();
        if (tieneTools) {
            requestBody.put("tools", tools);
        }

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENROUTER_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode data = objectMapper.readTree(response.body());
            if (data.has("error")) {
                log.error("OpenRouter error en body 200: {}", data.get("error"));
                throw new RuntimeException("OpenRouter: " + data.get("error").get("message").asText());
            }
            return data;
        }

        log.error("OpenRouter error {}: {}", response.statusCode(), response.body());
        throw new RuntimeException("OpenRouter respondió con código " + response.statusCode());
    }

    private List<Map<String, Object>> construirToolBuscarEnTiendas() {
        Map<String, Object> tool = new HashMap<>();
        tool.put("type", "function");

        Map<String, Object> function = new HashMap<>();
        function.put("name", "buscarEnTiendas");
        function.put("description", "Busca productos en tiendas colombianas (Éxito, Olímpica, Surtifamiliar) y devuelve resultados con nombre, precio, tienda y link. Úsala cuando el usuario pida buscar productos, comparar precios, o cuando necesites información actualizada de productos del mercado colombiano.");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> queryProp = new HashMap<>();
        queryProp.put("type", "string");
        queryProp.put("description", "Término de búsqueda del producto (ej. 'arroz', 'huevos', 'pollo', 'aceite', 'leche')");
        properties.put("query", queryProp);
        parameters.put("properties", properties);
        parameters.put("required", List.of("query"));

        function.put("parameters", parameters);
        tool.put("function", function);

        return List.of(tool);
    }

    private String construirSystemPrompt(List<Map<String, Object>> favoritos) {
        StringBuilder sb = new StringBuilder();
        sb.append("Eres EcoIA, el asistente experto en ahorro de EcoMerk2 en Colombia. ")
                .append("Tu objetivo es ayudar con cocina económica y gestión de presupuesto. ")
                .append("Además, puedes usar Markdown para formatear tus respuestas y hacerlas más claras y atractivas.\n")
                .append("REGLAS DE FORMATO Y RESPUESTA:\n")
                .append("- Usa **negritas** para resaltar precios y nombres de productos.\n")
                .append("- Usa ### para títulos de secciones (ej. ### Receta Sugerida).\n")
                .append("- Usa listas con guiones para ingredientes o pasos.\n")
                .append("- Mantén un tono amable, natural y colombiano.\n")
                .append("- No tienes un historial del chat\n")
                .append("- Si recomiendas productos, prioriza los favoritos del usuario.\n")
                .append("- Si el usuario pide recetas, recomiendalas por mayor coincidencia con la lista de favoritos del usuario.\n")
                .append("- Si el usuario no tiene favoritos, recomienda cualquier receta.\n")
                .append("\nTIENES ACCESO A LA FUNCIÓN buscarEnTiendas:\n")
                .append("- Úsala cuando el usuario pida buscar productos específicos, comparar precios entre tiendas, o cuando necesites información actualizada de productos.\n")
                .append("- Ejemplos: 'busca arroz barato', 'cuánto cuesta el pollo en las tiendas', 'encuentra aceite de cocina'.\n")
                .append("- La función buscará automáticamente en Éxito, Olímpica y Surtifamiliar y te devolverá los resultados ordenados por precio.\n")
                .append("- IMPORTANTE: Cuando recibas los resultados, PRESÉNTALOS al usuario de forma amigable, indicando el producto, la tienda, el precio y el enlace tipo [Consultar](url). Usa los datos reales que recibiste.\n")
                .append("- Si un producto no se encuentra o no hay resultados, indícaselo amablemente al usuario.");

        if (favoritos != null && !favoritos.isEmpty()) {
            sb.append("\n### PRODUCTOS FAVORITOS DEL USUARIO ###\n");
            for (Map<String, Object> f : favoritos) {
                String nombre = f.getOrDefault("nombre", "Producto").toString();
                String tienda = f.getOrDefault("tienda", "Tienda desconocida").toString();
                Object precioObj = f.get("precio");
                String precio = precioObj != null ? precioObj.toString() : "Sin precio";
                Object hasProteinObj = f.get("hasProtein");
                String hasProtein = hasProteinObj != null ? Boolean.parseBoolean(hasProteinObj.toString()) ? "Sí" : "No" : "No especificado";
                sb.append("- ").append(nombre).append(" en ").append(tienda).append(": $ ").append(precio).append(" COP (Proteína: ").append(hasProtein).append(")\n");
            }
            sb.append("### FIN DE DATOS ###\n");
        }

        return sb.toString();
    }

    private Map<String, Object> nuevoMap(String k1, String v1, String k2, String v2) {
        Map<String, Object> m = new HashMap<>();
        m.put(k1, v1);
        m.put(k2, v2);
        return m;
    }
}
