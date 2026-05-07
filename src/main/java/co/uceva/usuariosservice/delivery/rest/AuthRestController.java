package co.uceva.usuariosservice.delivery.rest;

import co.uceva.usuariosservice.domain.exception.ValidationException;
import co.uceva.usuariosservice.domain.model.RefreshTokenRequest;
import co.uceva.usuariosservice.domain.model.TokenRefreshResponse;
import co.uceva.usuariosservice.domain.service.IRefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para la renovación de tokens JWT.
 *
 * Endpoint:
 *   POST /api/v1/auth/refresh → Obtener un nuevo access token usando el refresh token
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final IRefreshTokenService refreshTokenService;

    /**
     * Renueva el access token JWT usando un refresh token válido.
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            BindingResult result) {

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }

        TokenRefreshResponse tokens = refreshTokenService.renovarAccessToken(request.getRefreshToken());

        Map<String, Object> response = new HashMap<>();
        response.put("token", tokens.getToken());
        response.put("refreshToken", tokens.getRefreshToken());
        response.put("mensaje", "Token renovado con éxito");

        return ResponseEntity.ok(response);
    }
}
