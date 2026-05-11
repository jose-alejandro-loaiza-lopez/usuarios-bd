package co.uceva.usuariosservice.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoFavorito {

    @NotBlank(message = "El productId del producto es obligatorio")
    @Column(name = "product_id", nullable = false, columnDefinition = "TEXT")
    private String productId;

    @Column(name = "notificaciones", nullable = false)
    private Boolean notificaciones = false;
}