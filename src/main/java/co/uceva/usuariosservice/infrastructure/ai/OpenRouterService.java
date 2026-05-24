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
    private final BusquedaTiendaService busquedaTiendaService;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String preguntar(String mensaje, List<Map<String, Object>> favoritos) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("OPENROUTER_API_KEY no configurada");
        }

        try {
            String systemPrompt = construirSystemPrompt(favoritos);
            List<Map<String, Object>> tools = construirTools();

            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", mensaje));

            JsonNode primeraRespuesta = llamarOpenRouter(messages, tools);

            JsonNode primerMensaje = primeraRespuesta.get("choices").get(0).get("message");
            JsonNode toolCalls = primerMensaje.get("tool_calls");

            if (toolCalls != null && toolCalls.isArray() && !toolCalls.isEmpty()) {
                for (JsonNode tc : toolCalls) {
                    String functionName = tc.get("function").get("name").asText();
                    String arguments = tc.get("function").get("arguments").asText();
                    String toolCallId = tc.get("id").asText();

                    if ("buscarEnTiendas".equals(functionName)) {
                        String query = objectMapper.readTree(arguments).get("query").asText();
                        log.info("IA solicitó busqueda: {}", query);

                        List<Map<String, Object>> resultados = busquedaTiendaService.buscarEnTiendas(query);

                        messages.add(Map.of(
                                "role", "assistant",
                                "content", null,
                                "tool_calls", List.of(Map.of(
                                        "id", toolCallId,
                                        "type", "function",
                                        "function", Map.of(
                                                "name", functionName,
                                                "arguments", arguments
                                        )
                                ))
                        ));

                        List<Map<String, Object>> resultadosSimples = new ArrayList<>();
                        for (Map<String, Object> r : resultados) {
                            Map<String, Object> simple = new HashMap<>();
                            simple.put("nombre", r.get("nombre"));
                            simple.put("tienda", r.get("tienda"));
                            simple.put("precio", r.get("precio"));
                            simple.put("link", r.get("link"));
                            resultadosSimples.add(simple);
                        }

                        messages.add(Map.of(
                                "role", "tool",
                                "tool_call_id", toolCallId,
                                "content", objectMapper.writeValueAsString(resultadosSimples)
                        ));
                    }
                }

                JsonNode segundaRespuesta = llamarOpenRouter(messages, tools);
                return segundaRespuesta.get("choices").get(0).get("message").get("content").asText();
            }

            return primerMensaje.get("content").asText();
        } catch (Exception e) {
            log.error("Error al comunicarse con OpenRouter", e);
            return null;
        }
    }

    private JsonNode llamarOpenRouter(List<Map<String, Object>> messages, List<Map<String, Object>> tools) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        if (tools != null && !tools.isEmpty()) {
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
            return objectMapper.readTree(response.body());
        }

        log.error("OpenRouter error {}: {}", response.statusCode(), response.body());
        throw new RuntimeException("OpenRouter respondió con código " + response.statusCode());
    }

    private List<Map<String, Object>> construirTools() {
        Map<String, Object> tool = new HashMap<>();
        tool.put("type", "function");

        Map<String, Object> function = new HashMap<>();
        function.put("name", "buscarEnTiendas");
        function.put("description", "Busca productos en tiendas colombianas (Éxito, Olímpica, Surtifamiliar) y devuelve resultados con nombre, precio, tienda y link. Úsala cuando el usuario pida buscar productos, comparar precios, o cuando necesites información actualizada de productos del mercado colombiano para hacer una recomendación.");

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
                .append("- No tienes historial chat\n")
                .append("- Si recomiendas productos, prioriza los favoritos del usuario.\n")
                .append("- Si el usuario pide recetas, recomiendalas por mayor coincidencia con la lista de favoritos del usuario.\n")
                .append("- Si el usuario no tiene favoritos, recomienda cualquier receta.\n")
                .append("\nTIENES ACCESO A LA FUNCIÓN buscarEnTiendas:\n")
                .append("- Úsala cuando el usuario pida buscar productos específicos, comparar precios entre tiendas, o cuando necesites información actualizada de productos.\n")
                .append("- Ejemplos: 'busca arroz barato', 'cuánto cuesta el pollo en las tiendas', 'encuentra aceite de cocina'.\n")
                .append("- La función buscará automáticamente en Éxito, Olímpica y Surtifamiliar y te devolverá los resultados ordenados por precio.\n")
                .append("- IMPORTANTE: Cuando recibas los resultados de la búsqueda, PRESÉNTALOS al usuario de forma amigable, indicando el producto, la tienda y el precio. Usa los datos reales que recibiste.\n")
                .append("- Si un producto no se encuentra o no hay resultados, indícaselo amablemente al usuario.");

        if (favoritos != null && !favoritos.isEmpty()) {
            sb.append("\n### PRODUCTOS FAVORITOS DEL USUARIO (Contexto Real) ###\n");
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
}
