package co.uceva.usuariosservice.domain.repository;

import co.uceva.usuariosservice.domain.model.MensajeChat;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio para operaciones sobre los mensajes de chat.
 * Soporta paginación basada en cursor (ID) para carga eficiente del historial.
 */
public interface IMensajeChatRepository extends JpaRepository<MensajeChat, Long> {

    /**
     * Obtiene los mensajes más recientes de un usuario (primera carga).
     */
    List<MensajeChat> findByUsuarioIdOrderByIdDesc(Long usuarioId, Pageable pageable);

    /**
     * Obtiene mensajes anteriores al cursor dado para un usuario.
     */
    List<MensajeChat> findByUsuarioIdAndIdLessThanOrderByIdDesc(Long usuarioId, Long id, Pageable pageable);
}
