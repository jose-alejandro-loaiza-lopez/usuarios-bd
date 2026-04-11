package co.uceva.usuariosservice.infrastructure.security;

import co.uceva.usuariosservice.domain.model.EncryptedRequestDTO;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class EncryptResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final CriptoService criptoService;

    public EncryptResponseBodyAdvice(CriptoService criptoService) {
        this.criptoService = criptoService;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // Evitamos cifrar la llave pública y las respuestas que ya están cifradas
        String methodName = returnType.getMethod().getName();
        return !methodName.equals("getPublicKey") && !returnType.getParameterType().equals(EncryptedRequestDTO.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

        try {
            if (CriptoContextHolder.getKey() != null) {
                return criptoService.encriptarRespuesta(body);
            }
        } finally {
            // CRÍTICO: Limpiar el hilo para evitar fugas de memoria
            CriptoContextHolder.clear();
        }
        return body;
    }
}