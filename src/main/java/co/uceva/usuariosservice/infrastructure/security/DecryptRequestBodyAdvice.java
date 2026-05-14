package co.uceva.usuariosservice.infrastructure.security;

import co.uceva.usuariosservice.domain.exception.CifradoRequeridoException;
import co.uceva.usuariosservice.domain.model.EncryptedRequestDTO;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

@ControllerAdvice
public class DecryptRequestBodyAdvice extends RequestBodyAdviceAdapter {

    private final CriptoService criptoService;
    private final ObjectMapper objectMapper;

    public DecryptRequestBodyAdvice(CriptoService criptoService, ObjectMapper objectMapper) {
        this.criptoService = criptoService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Solo desciframos si el objeto de destino NO es el DTO cifrado
        // Esto evita bucles infinitos
        return !targetType.equals(EncryptedRequestDTO.class);
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        try {
            EncryptedRequestDTO encryptedDto = objectMapper.readValue(inputMessage.getBody(), EncryptedRequestDTO.class);

            byte[] aesKeyDescifrada = criptoService.obtenerLlaveAesLimpia(encryptedDto.getEncryptedAesKey());
            byte[] ivLimpio = java.util.Base64.getDecoder().decode(encryptedDto.getIv());

            CriptoContextHolder.setContext(aesKeyDescifrada, ivLimpio);

            // 1. Obtenemos el tipo real que espera el Controller (puede ser List<ProductoFavorito>)
            JavaType javaType = objectMapper.getTypeFactory().constructType(targetType);

            // 2. Llamamos al nuevo método pasándole el javaType
            Object decryptedObject = criptoService.descifrarConLlave(encryptedDto, javaType, aesKeyDescifrada, ivLimpio);

            // 3. Convertimos de nuevo a bytes para que Spring siga su camino
            byte[] decryptedData = objectMapper.writeValueAsBytes(decryptedObject);

            return new HttpInputMessage() {
                @Override
                public InputStream getBody() throws IOException {
                    return new ByteArrayInputStream(decryptedData);
                }
                @Override
                public HttpHeaders getHeaders() {
                    return inputMessage.getHeaders();
                }
            };
        } catch (Exception e) {
            throw new CifradoRequeridoException("La petición debe ir cifrada. Envía el payload como EncryptedRequestDTO (encryptedAesKey, iv, encryptedData).");
        }
    }
}