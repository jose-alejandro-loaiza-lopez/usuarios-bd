package co.uceva.usuariosservice.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusquedaTiendaService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public List<Map<String, Object>> buscarEnTiendas(String query) {
        List<Map<String, Object>> todos = new ArrayList<>();

        try {
            List<Map<String, Object>> exito = buscarEnExito(query);
            List<Map<String, Object>> olimpica = buscarEnOlimpica(query);
            List<Map<String, Object>> surti = buscarEnSurtifamiliar(query);

            todos.addAll(exito);
            todos.addAll(olimpica);
            todos.addAll(surti);

            todos.removeIf(p -> p.get("precio") == null || ((Number) p.get("precio")).doubleValue() <= 0);
            todos.sort((a, b) -> Double.compare(
                    ((Number) a.get("precio")).doubleValue(),
                    ((Number) b.get("precio")).doubleValue()
            ));
        } catch (Exception e) {
            log.error("Error en busqueda de tiendas", e);
        }

        return todos;
    }

    private List<Map<String, Object>> buscarEnExito(String query) {
        List<Map<String, Object>> resultados = new ArrayList<>();
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://www.exito.com/api/catalog_system/pub/products/search?ft=" + encoded
                    + "&O=OrderByScoreDESC&_from=0&_to=9";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 206) {
                JsonNode data = objectMapper.readTree(response.body());
                if (data.isArray()) {
                    for (JsonNode product : data) {
                        try {
                            JsonNode item = product.get("items").get(0);
                            JsonNode seller = item.get("sellers").get(0);
                            JsonNode offer = seller.get("commertialOffer");
                            double precio = offer.get("Price").asDouble();
                            if (precio <= 0) continue;

                            Map<String, Object> p = new HashMap<>();
                            p.put("id", product.get("productId").asText());
                            p.put("nombre", product.get("productName").asText("Producto Éxito"));
                            p.put("precio", precio);
                            p.put("tienda", "Éxito");
                            p.put("imagen", item.get("images").get(0).get("imageUrl").asText(""));
                            p.put("link", "https://www.exito.com/" + product.get("linkText").asText() + "/p");
                            resultados.add(p);
                        } catch (Exception e) {
                            log.debug("Error parseando producto Éxito", e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error buscando en Éxito", e);
        }
        return resultados;
    }

    private List<Map<String, Object>> buscarEnOlimpica(String query) {
        List<Map<String, Object>> resultados = new ArrayList<>();
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://www.olimpica.com/api/catalog_system/pub/products/search/" + encoded
                    + "?O=OrderByScoreDESC&_from=0&_to=9";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 206) {
                JsonNode data = objectMapper.readTree(response.body());
                if (data.isArray()) {
                    for (JsonNode product : data) {
                        try {
                            JsonNode item = product.get("items").get(0);
                            JsonNode seller = item.get("sellers").get(0);
                            JsonNode offer = seller.get("commertialOffer");
                            double precio = offer.get("Price").asDouble();
                            if (precio <= 0) continue;

                            Map<String, Object> p = new HashMap<>();
                            p.put("id", product.get("productId").asText());
                            p.put("nombre", product.get("productName").asText("Producto Olímpica"));
                            p.put("precio", precio);
                            p.put("tienda", "Olímpica");
                            p.put("imagen", item.get("images").get(0).get("imageUrl").asText(""));
                            p.put("link", product.get("link").asText(""));
                            resultados.add(p);
                        } catch (Exception e) {
                            log.debug("Error parseando producto Olímpica", e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error buscando en Olímpica", e);
        }
        return resultados;
    }

    private List<Map<String, Object>> buscarEnSurtifamiliar(String query) {
        List<Map<String, Object>> resultados = new ArrayList<>();
        try {
            String url = "https://ecommerce.surtifamiliar.com/backend/admin/frontend/web/index.php/categoria-info/show-items-by-cattegory";

            Map<String, Object> body = new HashMap<>();
            body.put("id", null);
            body.put("slug", "");
            body.put("pageSize", 10);
            body.put("searchText", query);
            body.put("internSearchText", "");
            body.put("cartId", "undefined");
            body.put("userId", "");
            body.put("slugPromition", null);
            body.put("filters", Map.of(
                    "pageNumber", 1,
                    "attributes", new ArrayList<>(),
                    "productHighPrice", 0,
                    "productLowPrice", 0,
                    "sort", "1"
            ));
            body.put("typeProducts", null);

            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Origin", "https://www.surtifamiliar.com")
                    .header("Referer", "https://www.surtifamiliar.com/")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode data = objectMapper.readTree(response.body());
                JsonNode items = data.get("items");
                if (items != null && items.isArray()) {
                    String baseImgUrl = "https://ecommerce.surtifamiliar.com/backend/admin/backend/web/archivosDelCliente/items/images/";

                    for (JsonNode item : items) {
                        try {
                            double precio = item.get("currentPrice").asDouble();
                            if (precio <= 0) continue;

                            String slug = item.get("slug").asText("");
                            String productId = slug.contains("/") ? slug.substring(slug.lastIndexOf("/") + 1) : slug;

                            Map<String, Object> p = new HashMap<>();
                            p.put("id", productId);
                            p.put("nombre", item.get("name").asText("Producto Surtifamiliar"));
                            p.put("precio", precio);
                            p.put("tienda", "Surtifamiliar");
                            p.put("imagen", baseImgUrl + item.get("principalImage").asText(""));
                            p.put("link", slug);
                            resultados.add(p);
                        } catch (Exception e) {
                            log.debug("Error parseando producto Surtifamiliar", e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error buscando en Surtifamiliar", e);
        }
        return resultados;
    }
}
