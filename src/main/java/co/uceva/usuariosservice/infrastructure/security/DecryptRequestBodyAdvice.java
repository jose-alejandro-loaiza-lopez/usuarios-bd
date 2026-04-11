package co.uceva.usuariosservice.infrastructure.security;

import co.uceva.usuariosservice.domain.model.EncryptedRequestDTO;
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

        // 1. Leemos el JSON cifrado que viene de Flutter
        EncryptedRequestDTO encryptedDto = objectMapper.readValue(inputMessage.getBody(), EncryptedRequestDTO.class);

        // 2. ¡OJO AQUÍ!: Debemos descifrar la llave AES antes de guardarla en el contexto
        // para que el ResponseBodyAdvice pueda usar la llave real (byte[]), no el texto cifrado.

        // Usamos el método de tu CriptoService para obtener las llaves reales
        byte[] aesKeyDescifrada = criptoService.obtenerLlaveAesLimpia(encryptedDto.getEncryptedAesKey());
        byte[] ivLimpio = java.util.Base64.getDecoder().decode(encryptedDto.getIv());

        // 3. Guardamos en el ThreadLocal para que esté disponible en la RESPUESTA
        CriptoContextHolder.setContext(aesKeyDescifrada, ivLimpio);

        // 4. Desciframos el objeto real (User, Producto, etc.)
        Object decryptedObject = criptoService.descifrarConLlave(encryptedDto, (Class<?>) targetType, aesKeyDescifrada, ivLimpio);

        // 5. Convertimos a JSON normal para que el Controller lo reciba
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
    }
}