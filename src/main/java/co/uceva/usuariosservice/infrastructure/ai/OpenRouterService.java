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

    public String preguntar(String mensaje, List<Map<String, Object>> favoritos) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("OPENROUTER_API_KEY no configurada");
        }

        try {
            String systemPrompt = construirSystemPrompt(favoritos);

            Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", mensaje)
                ),
                "reasoning", Map.of("enabled", true)
            );

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
                return data.get("choices").get(0).get("message").get("content").asText();
            }

            log.error("OpenRouter error {}: {}", response.statusCode(), response.body());
            return null;
        } catch (Exception e) {
            log.error("Error al comunicarse con OpenRouter", e);
            return null;
        }
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
          .append("- Si recomiendas productos, prioriza los favoritos del usuario.\n");

        if (favoritos != null && !favoritos.isEmpty()) {
            sb.append("\n### PRODUCTOS FAVORITOS DEL USUARIO (Contexto Real) ###\n");
            for (Map<String, Object> f : favoritos) {
                String nombre = f.getOrDefault("nombre", "Producto").toString();
                String tienda = f.getOrDefault("tienda", "Tienda desconocida").toString();
                Object precioObj = f.get("precio");
                String precio = precioObj != null ? precioObj.toString() : "Sin precio";
                sb.append("- ").append(nombre).append(" en ").append(tienda).append(": $ ").append(precio).append(" COP\n");
            }
            sb.append("### FIN DE DATOS ###\n");
        }

        return sb.toString();
    }
}
