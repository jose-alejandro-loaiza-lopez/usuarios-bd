package co.uceva.usuariosservice.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO para la creación de un nuevo mensaje de chat privado.
 * El `usuarioId` se infiere desde el token (no se envía en el body).
 */
@Getter
@Setter
public class MensajeChatRequest {

    @NotBlank(message = "El contenido del mensaje es obligatorio")
    private String contenido;

    @NotNull(message = "Indicar si el mensaje es de la IA")
    private Boolean esIa;
}
