package co.uceva.usuariosservice.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que representa un mensaje del chat privado de IA asociado a un usuario.
 * Cada mensaje pertenece a un `usuarioId` y contiene sólo el `contenido` y
 * un flag `esIa` que indica si proviene de la IA o del usuario.
 */
@Entity
@Table(name = "mensajes_chat")
@Getter
@Setter
public class MensajeChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ID del usuario es obligatorio")
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @NotBlank(message = "El contenido del mensaje es obligatorio")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @NotNull(message = "Indicar si el mensaje es de la IA")
    @Column(name = "es_ia", nullable = false)
    private Boolean esIa;
}
