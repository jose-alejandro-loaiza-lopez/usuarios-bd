package co.uceva.usuariosservice.domain.service;

import co.uceva.usuariosservice.domain.model.RefreshToken;
import co.uceva.usuariosservice.domain.model.TokenRefreshResponse;
import co.uceva.usuariosservice.domain.model.Usuarios;
import co.uceva.usuariosservice.domain.repository.IRefreshTokenRepository;
import co.uceva.usuariosservice.infrastructure.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.UUID;

/**
 * Implementación del servicio de refresh tokens.
 * Ahora almacena los tokens hasheados y aplica rotación al usarlos.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements IRefreshTokenService {

    private final IRefreshTokenRepository refreshTokenRepository;
    private final JwtUtils jwtUtils;

    @Value("${jwt.refresh-expiration:604800000}") // 7 días por defecto
    private long refreshExpirationMs;

    // Se usa una "sal" configurable para derivar el hash (evita colisiones simples)
    @Value("${jwt.refresh-secret:default_refresh_secret}")
    private String refreshSecret;

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest((token + refreshSecret).getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al hashear token", e);
        }
    }

    @Override
    @Transactional
    public String crearRefreshToken(Usuarios usuario) {
        // Revocar tokens anteriores del usuario (rotación de sesión al crear uno nuevo)
        refreshTokenRepository.revocarTokensDeUsuario(usuario.getId());

        String rawToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        String hashed = hashToken(rawToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsuario(usuario);
        refreshToken.setToken(hashed);
        refreshToken.setFechaExpiracion(Instant.now().plusMillis(refreshExpirationMs));
        refreshToken.setRevocado(false);

        refreshTokenRepository.save(refreshToken);

        // Retornamos el token en texto plano sólo para enviarlo al cliente
        return rawToken;
    }

    @Override
    @Transactional
    public TokenRefreshResponse renovarAccessToken(String refreshTokenStr) {
        String hashed = hashToken(refreshTokenStr);

        RefreshToken refreshToken = refreshTokenRepository.findByToken(hashed)
                .orElseThrow(() -> new RuntimeException("Refresh token no encontrado"));

        if (refreshToken.isRevocado()) {
            throw new RuntimeException("El refresh token ha sido revocado");
        }

        if (refreshToken.getFechaExpiracion().isBefore(Instant.now())) {
            refreshToken.setRevocado(true);
            refreshTokenRepository.save(refreshToken);
            throw new RuntimeException("El refresh token ha expirado. Inicie sesión nuevamente");
        }

        // Marcar el token usado como revocado para evitar double-use (reuse)
        refreshToken.setRevocado(true);
        refreshTokenRepository.save(refreshToken);

        // Generar nuevo refresh token (creará uno nuevo y revocará previos)
        String newRawRefresh = crearRefreshToken(refreshToken.getUsuario());

        // Generar nuevo access token JWT
        String newAccessToken = jwtUtils.generateToken(refreshToken.getUsuario());

        return new TokenRefreshResponse(newAccessToken, newRawRefresh);
    }
}

