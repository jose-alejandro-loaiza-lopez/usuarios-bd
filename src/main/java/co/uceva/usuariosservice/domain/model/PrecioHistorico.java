package co.uceva.usuariosservice.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "precio_historico")
public class PrecioHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "precio", nullable = false)
    private Double precio;

    @Column(name = "fecha_guardado", nullable = false)
    private LocalDateTime fechaGuardado;
}
