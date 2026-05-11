package co.uceva.usuariosservice.domain.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrecioRequest {
    @NotNull(message = "El precio es obligatorio")
    private Double precio;
}
