package co.uceva.usuariosservice.domain.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ChatIaRequest {

    @NotBlank(message = "El mensaje es obligatorio")
    private String mensaje;

    private List<Map<String, Object>> favoritos;

    private List<Map<String, Object>> resultadosBusqueda;

    private String toolCallId;

    private String arguments;

    private List<Map<String, Object>> historialBusquedas;
}
