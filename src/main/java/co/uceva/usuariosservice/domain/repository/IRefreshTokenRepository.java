package co.uceva.usuariosservice.domain.repository;

import co.uceva.usuariosservice.domain.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * Repositorio para la gestión de refresh tokens.
 */
public interface IRefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    /**
     * Revoca todos los refresh tokens activos de un usuario (al hacer logout o al emitir uno nuevo).
     */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revocado = true WHERE r.usuario.id = :usuarioId AND r.revocado = false")
    void revocarTokensDeUsuario(Long usuarioId);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.usuario.id = :usuarioId")
    void eliminarPorUsuarioId(Long usuarioId);
}
