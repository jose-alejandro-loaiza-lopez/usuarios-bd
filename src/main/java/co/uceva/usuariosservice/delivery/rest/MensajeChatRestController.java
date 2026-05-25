package co.uceva.usuariosservice.delivery.rest;

import co.uceva.usuariosservice.domain.exception.ValidationException;
import co.uceva.usuariosservice.domain.exception.AccesoDenegadoException;
import co.uceva.usuariosservice.domain.model.ChatIaRequest;
import co.uceva.usuariosservice.domain.model.MensajeChat;
import co.uceva.usuariosservice.domain.model.Usuarios;
import co.uceva.usuariosservice.domain.service.IMensajeChatService;
import co.uceva.usuariosservice.domain.service.IUsuariosService;
import co.uceva.usuariosservice.infrastructure.ai.OpenRouterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el chat con IA de EcoMerk2.
 *
 * Endpoints:
 *   GET  /api/v1/chat/mensajes            → Últimos 10 mensajes (primera carga)
 *   GET  /api/v1/chat/mensajes?antes=ID   → Siguientes 10 mensajes anteriores al cursor
 *   POST /api/v1/chat/ia                  → Enviar mensaje a la IA y guardar historial
 */
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class MensajeChatRestController {

    private final IMensajeChatService mensajeChatService;
    private final IUsuariosService usuariosService;
    private final OpenRouterService openRouterService;

    private static final String MENSAJE = "mensaje";
    private static final String MENSAJES = "mensajes";
    private static final int CANTIDAD_POR_PAGINA = 10;

    /**
     * Obtiene los últimos 10 mensajes del chat.
     * Si se envía el parámetro "antes" (cursor), devuelve los 10 mensajes
     * anteriores a ese ID para implementar scroll infinito.
     *
     * PROTECCIÓN: Requiere autenticación (JWT válido)
     *
     * @param antes ID del mensaje más antiguo que el frontend ya tiene (opcional)
     */
    @GetMapping("/mensajes")
    public ResponseEntity<Map<String, Object>> obtenerMensajes(
            @RequestParam(required = false) Long antes,
            Authentication auth) {

        Usuarios usuario = usuariosService.findByEmail(auth.getName())
                .orElseThrow(() -> new AccesoDenegadoException("Usuario no encontrado"));

        Long usuarioId = usuario.getId();

        List<MensajeChat> mensajes;

        if (antes != null) {
            mensajes = mensajeChatService.obtenerMensajesAnteriores(usuarioId, antes, CANTIDAD_POR_PAGINA);
        } else {
            mensajes = mensajeChatService.obtenerUltimosMensajes(usuarioId, CANTIDAD_POR_PAGINA);
        }

        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJES, mensajes);
        response.put("cantidad", mensajes.size());
        response.put("hayMas", mensajes.size() == CANTIDAD_POR_PAGINA);

        return ResponseEntity.ok(response);
    }

    /**
     * Envía un mensaje a la IA (OpenRouter) y guarda tanto el mensaje del
     * usuario como la respuesta de la IA en el historial.
     *
     * La API key de OpenRouter se encuentra en el servidor (variable de entorno).
     *
     * Flujo en dos fases:
     *   FASE 1: El usuario envía un mensaje. Si la IA decide buscar productos,
     *           responde con { action: "search", query, toolCallId, arguments }.
     *   FASE 2: Flutter ejecuta MarketApiService y reenvía el mensaje con
     *           resultadosBusqueda + toolCallId + arguments. La IA genera la
     *           respuesta final con los datos reales.
     *
     * PROTECCIÓN: Requiere autenticación (JWT válido)
     */
    @PostMapping("/ia")
    public ResponseEntity<Map<String, Object>> preguntarIa(
            @Valid @RequestBody ChatIaRequest request,
            BindingResult result,
            Authentication auth) {

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }

        Usuarios usuario = usuariosService.findByEmail(auth.getName())
                .orElseThrow(() -> new AccesoDenegadoException("Usuario no encontrado"));

        Long usuarioId = usuario.getId();

        // FASE 2: Viene con resultados de búsqueda → no guardar mensaje otra vez
        if (request.getToolCallId() == null || request.getToolCallId().isBlank()) {
            mensajeChatService.guardarMensaje(usuarioId, request.getMensaje(), false);
        }

        // Consultar a la IA
        Map<String, Object> resultado = openRouterService.preguntar(
                request.getMensaje(),
                request.getFavoritos(),
                request.getResultadosBusqueda(),
                request.getToolCallId(),
                request.getArguments(),
                request.getHistorialBusquedas()
        );

        if (resultado == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put(MENSAJE, "Error al obtener respuesta de la IA");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

        // Si la IA quiere buscar (FASE 1), devolver action sin guardar respuesta
        if (resultado.containsKey("action")) {
            return ResponseEntity.ok(resultado);
        }

        // Tiene respuesta textual → guardar y devolver
        String respuesta = (String) resultado.get("respuesta");
        mensajeChatService.guardarMensaje(usuarioId, respuesta, true);

        Map<String, Object> response = new HashMap<>();
        response.put("respuesta", respuesta);

        return ResponseEntity.ok(response);
    }
}
