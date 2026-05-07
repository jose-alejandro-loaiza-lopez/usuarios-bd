package co.uceva.usuariosservice.domain.service;

import co.uceva.usuariosservice.domain.model.MensajeChat;
import co.uceva.usuariosservice.domain.model.MensajeChatRequest;

import java.util.List;

/**
 * Interfaz del servicio de mensajes de chat.
 */
public interface IMensajeChatService {

    /**
     * Obtiene los últimos N mensajes del chat de un usuario (primera carga del frontend).
     */
    List<MensajeChat> obtenerUltimosMensajes(Long usuarioId, int cantidad);

    /**
     * Obtiene los siguientes N mensajes anteriores al cursor (scroll hacia arriba) para un usuario.
     * @param usuarioId ID del usuario dueño del chat
     * @param cursorId ID del mensaje más antiguo que el frontend ya tiene
     * @param cantidad Número de mensajes a obtener
     */
    List<MensajeChat> obtenerMensajesAnteriores(Long usuarioId, Long cursorId, int cantidad);

    /**
     * Guarda un nuevo mensaje de chat asociado al usuario.
     */
    MensajeChat guardarMensaje(Long usuarioId, MensajeChatRequest request);
}
