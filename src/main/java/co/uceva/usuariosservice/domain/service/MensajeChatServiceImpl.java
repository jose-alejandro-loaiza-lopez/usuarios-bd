package co.uceva.usuariosservice.domain.service;

import co.uceva.usuariosservice.domain.model.MensajeChat;
import co.uceva.usuariosservice.domain.model.MensajeChatRequest;
import co.uceva.usuariosservice.domain.repository.IMensajeChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación del servicio de mensajes de chat.
 * Usa paginación basada en cursor para carga eficiente del historial.
 */
@Service
@RequiredArgsConstructor
public class MensajeChatServiceImpl implements IMensajeChatService {

    private final IMensajeChatRepository mensajeChatRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MensajeChat> obtenerUltimosMensajes(Long usuarioId, int cantidad) {
        Pageable pageable = PageRequest.of(0, cantidad);
        return mensajeChatRepository.findByUsuarioIdOrderByIdDesc(usuarioId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeChat> obtenerMensajesAnteriores(Long usuarioId, Long cursorId, int cantidad) {
        Pageable pageable = PageRequest.of(0, cantidad);
        return mensajeChatRepository.findByUsuarioIdAndIdLessThanOrderByIdDesc(usuarioId, cursorId, pageable);
    }

    @Override
    @Transactional
    public MensajeChat guardarMensaje(Long usuarioId, MensajeChatRequest request) {
        MensajeChat mensaje = new MensajeChat();
        mensaje.setUsuarioId(usuarioId);
        mensaje.setContenido(request.getContenido());
        mensaje.setEsIa(request.getEsIa());
        return mensajeChatRepository.save(mensaje);
    }

    @Override
    @Transactional
    public MensajeChat guardarMensaje(Long usuarioId, String contenido, Boolean esIa) {
        MensajeChat mensaje = new MensajeChat();
        mensaje.setUsuarioId(usuarioId);
        mensaje.setContenido(contenido);
        mensaje.setEsIa(esIa);
        return mensajeChatRepository.save(mensaje);
    }
}
