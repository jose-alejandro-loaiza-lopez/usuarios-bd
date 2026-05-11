package co.uceva.usuariosservice.delivery.rest;

import co.uceva.usuariosservice.domain.model.PrecioHistorico;
import co.uceva.usuariosservice.domain.model.PrecioRequest;
import co.uceva.usuariosservice.domain.service.IPrecioHistoricoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoRestController {

    private final IPrecioHistoricoService precioHistoricoService;
    private static final String MENSAJE = "mensaje";

    public ProductoRestController(IPrecioHistoricoService precioHistoricoService) {
        this.precioHistoricoService = precioHistoricoService;
    }

    @GetMapping("/{productId}/precios")
    public ResponseEntity<Map<String, Object>> getPriceHistory(@PathVariable String productId) {
        List<PrecioHistorico> historial = precioHistoricoService.getPriceHistory(productId);
        Map<String, Object> response = new HashMap<>();
        response.put("productId", productId);
        response.put("historial", historial);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{productId}/precios")
    public ResponseEntity<Map<String, Object>> addPrice(@PathVariable String productId, @Valid @RequestBody PrecioRequest request) {
        PrecioHistorico saved = precioHistoricoService.addPrice(productId, request.getPrecio());
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "Precio agregado correctamente");
        response.put("precio", saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
