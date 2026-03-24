package co.uceva.usuariosservice.domain.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoFavorito {
    private String nombre;
    private String precio;
    private String tienda;
    private String imagen;
    private String link;
    private String marca;
}