package co.uceva.usuariosservice.domain.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO para solicitar un nuevo access token usando el refresh token.
 */
@Getter
@Setter
public class RefreshTokenRequest {

    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;
}
