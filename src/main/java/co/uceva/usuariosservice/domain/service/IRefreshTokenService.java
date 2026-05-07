package co.uceva.usuariosservice.domain.service;

import co.uceva.usuariosservice.domain.model.TokenRefreshResponse;
import co.uceva.usuariosservice.domain.model.Usuarios;

/**
 * Interfaz del servicio de refresh tokens.
 */
public interface IRefreshTokenService {

    /**
     * Crea un nuevo refresh token para el usuario dado y retorna el valor plano
     * que deberá enviarse al cliente. Revoca los tokens anteriores del usuario.
     */
    String crearRefreshToken(Usuarios usuario);

    /**
     * Valida un refresh token y retorna un nuevo access token y un nuevo
     * refresh token rotado (previniendo reuse).
     * @return TokenRefreshResponse con access token y refresh token nuevo
     * @throws RuntimeException si el token es inválido, expirado o revocado
     */
    TokenRefreshResponse renovarAccessToken(String refreshToken);
}
