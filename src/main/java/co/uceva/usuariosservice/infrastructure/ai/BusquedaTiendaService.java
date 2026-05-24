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

    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public List<Map<String, Object>> buscarEnTiendas(String query) {
        List<Map<String, Object>> todos = new ArrayList<>();

        List<Map<String, Object>> exito = buscarEnExito(query);
        List<Map<String, Object>> olimpica = buscarEnOlimpica(query);
        List<Map<String, Object>> surti = buscarEnSurtifamiliar(query);

        log.info("Resultados: Éxito={}, Olímpica={}, Surtifamiliar={}", exito.size(), olimpica.size(), surti.size());

        todos.addAll(exito);
        todos.addAll(olimpica);
        todos.addAll(surti);

        todos.removeIf(p -> p.get("precio") == null || ((Number) p.get("precio")).doubleValue() <= 0);
        todos.sort((a, b) -> Double.compare(
                ((Number) a.get("precio")).doubleValue(),
                ((Number) b.get("precio")).doubleValue()
        ));

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
                    .header("User-Agent", UA)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Éxito status: {}, body length: {}", response.statusCode(), response.body().length());

            if (response.statusCode() == 200 || response.statusCode() == 206) {
                JsonNode data = objectMapper.readTree(response.body());
                if (data.isArray()) {
                    log.info("Éxito productos encontrados: {}", data.size());
                    for (JsonNode product : data) {
                        try {
                            JsonNode items = product.get("items");
                            if (items == null || !items.isArray() || items.isEmpty()) continue;
                            JsonNode item = items.get(0);
                            JsonNode sellers = item.get("sellers");
                            if (sellers == null || !sellers.isArray() || sellers.isEmpty()) continue;
                            JsonNode seller = sellers.get(0);
                            JsonNode offer = seller.get("commertialOffer");
                            if (offer == null) continue;
                            double precio = offer.get("Price").asDouble();
                            if (precio <= 0) continue;

                            Map<String, Object> p = new HashMap<>();
                            p.put("id", product.get("productId").asText());
                            p.put("nombre", product.get("productName").asText("Producto Éxito"));
                            p.put("precio", precio);
                            p.put("tienda", "Éxito");
                            JsonNode images = item.get("images");
                            p.put("imagen", images != null && images.isArray() && !images.isEmpty()
                                    ? images.get(0).get("imageUrl").asText("") : "");
                            p.put("link", "https://www.exito.com/" + product.get("linkText").asText() + "/p");
                            resultados.add(p);
                        } catch (Exception e) {
                            log.debug("Error parseando producto Éxito: {}", e.getMessage());
                        }
                    }
                } else {
                    log.warn("Éxito: respuesta no es array, es: {}", data.getNodeType());
                }
            } else {
                log.warn("Éxito: status no esperado {}, body: {}", response.statusCode(), response.body().substring(0, Math.min(200, response.body().length())));
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
                    .header("User-Agent", UA)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Olímpica status: {}, body length: {}", response.statusCode(), response.body().length());

            if (response.statusCode() == 200 || response.statusCode() == 206) {
                JsonNode data = objectMapper.readTree(response.body());
                if (data.isArray()) {
                    log.info("Olímpica productos encontrados: {}", data.size());
                    for (JsonNode product : data) {
                        try {
                            JsonNode items = product.get("items");
                            if (items == null || !items.isArray() || items.isEmpty()) continue;
                            JsonNode item = items.get(0);
                            JsonNode sellers = item.get("sellers");
                            if (sellers == null || !sellers.isArray() || sellers.isEmpty()) continue;
                            JsonNode seller = sellers.get(0);
                            JsonNode offer = seller.get("commertialOffer");
                            if (offer == null) continue;
                            double precio = offer.get("Price").asDouble();
                            if (precio <= 0) continue;

                            Map<String, Object> p = new HashMap<>();
                            p.put("id", product.get("productId").asText());
                            p.put("nombre", product.get("productName").asText("Producto Olímpica"));
                            p.put("precio", precio);
                            p.put("tienda", "Olímpica");
                            JsonNode images = item.get("images");
                            p.put("imagen", images != null && images.isArray() && !images.isEmpty()
                                    ? images.get(0).get("imageUrl").asText("") : "");
                            p.put("link", product.get("link").asText(""));
                            resultados.add(p);
                        } catch (Exception e) {
                            log.debug("Error parseando producto Olímpica: {}", e.getMessage());
                        }
                    }
                } else {
                    log.warn("Olímpica: respuesta no es array, es: {}", data.getNodeType());
                }
            } else {
                log.warn("Olímpica: status no esperado {}, body: {}", response.statusCode(), response.body().substring(0, Math.min(200, response.body().length())));
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
            Map<String, Object> filters = new HashMap<>();
            filters.put("pageNumber", 1);
            filters.put("attributes", new ArrayList<>());
            filters.put("productHighPrice", 0);
            filters.put("productLowPrice", 0);
            filters.put("sort", "1");
            body.put("filters", filters);
            body.put("typeProducts", null);

            String jsonBody = objectMapper.writeValueAsString(body);
            log.info("Surtifamiliar request body: {}", jsonBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Origin", "https://www.surtifamiliar.com")
                    .header("Referer", "https://www.surtifamiliar.com/")
                    .header("User-Agent", UA)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Surtifamiliar status: {}, body length: {}", response.statusCode(), response.body().length());

            if (response.statusCode() == 200) {
                JsonNode data = objectMapper.readTree(response.body());
                JsonNode items = data.get("items");
                if (items != null && items.isArray()) {
                    log.info("Surtifamiliar productos encontrados: {}", items.size());
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
                            log.debug("Error parseando producto Surtifamiliar: {}", e.getMessage());
                        }
                    }
                } else {
                    log.warn("Surtifamiliar: respuesta incompleta, keys: {}", data.fieldNames().next());
                }
            } else {
                log.warn("Surtifamiliar: status no esperado {}, body: {}", response.statusCode(), response.body().substring(0, Math.min(200, response.body().length())));
            }
        } catch (Exception e) {
            log.error("Error buscando en Surtifamiliar", e);
        }
        return resultados;
    }
}
