package co.uceva.usuariosservice.infrastructure.security;

import co.uceva.usuariosservice.domain.model.EncryptedRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class CriptoService {

    private final AesWrapper aesWrapper;
    private final RsaEngine rsaEngine;
    private final ObjectMapper objectMapper;

    public <T> T descifrarRequest(EncryptedRequestDTO dto, Class<T> claseDestino) {
        try {
            // 1. RSA: Descifrar la llave AES
            byte[] aesKeyEncrypted = Base64.getDecoder().decode(dto.getEncryptedAesKey());
            BigInteger aesKeyNumber = rsaEngine.decrypt(new BigInteger(1, aesKeyEncrypted));
            byte[] aesKey = fixKeyLength(aesKeyNumber.toByteArray());

            // 2. AES: Descifrar los datos
            byte[] iv = Base64.getDecoder().decode(dto.getIv());
            byte[] dataEncrypted = Base64.getDecoder().decode(dto.getEncryptedData());
            byte[] dataDecrypted = aesWrapper.decryptCBC(dataEncrypted, aesKey, iv);

            // 3. JSON: Convertir bytes a objeto
            String json = new String(dataDecrypted);
            return objectMapper.readValue(json, claseDestino);

        } catch (Exception e) {
            throw new RuntimeException("Error en el descifrado: " + e.getMessage());
        }
    }

    public EncryptedRequestDTO encriptarRespuesta(Object body) {
        try {
            byte[] key = CriptoContextHolder.getKey();
            byte[] iv = CriptoContextHolder.getIv();

            if (key == null || iv == null) return null; // No cifrar si no hay llaves

            // 1. Convertir objeto a JSON
            String json = objectMapper.writeValueAsString(body);

            // 2. Cifrar con AES
            byte[] encryptedData = aesWrapper.encryptCBC(json.getBytes(), key, iv);

            // 3. Empaquetar (No necesitamos reenviar la llave RSA, Flutter ya la conoce)
            EncryptedRequestDTO responseDto = new EncryptedRequestDTO();
            responseDto.setEncryptedData(Base64.getEncoder().encodeToString(encryptedData));
            responseDto.setIv(Base64.getEncoder().encodeToString(iv));

            return responseDto;
        } catch (Exception e) {
            throw new RuntimeException("Error al cifrar respuesta: " + e.getMessage());
        }
    }

    private byte[] fixKeyLength(byte[] key) {
        if (key.length == 16) return key;
        byte[] fixed = new byte[16];
        int start = Math.max(0, key.length - 16);
        System.arraycopy(key, start, fixed, 0, 16);
        return fixed;
    }

    // Añade esto a CriptoService.java
    public byte[] obtenerLlaveAesLimpia(String encryptedAesKeyBase64) {
        try {
            byte[] aesKeyEncrypted = java.util.Base64.getDecoder().decode(encryptedAesKeyBase64);
            java.math.BigInteger aesKeyNumber = rsaEngine.decrypt(new java.math.BigInteger(1, aesKeyEncrypted));
            return fixKeyLength(aesKeyNumber.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar llave RSA: " + e.getMessage());
        }
    }

    // Y este para descifrar usando la llave que ya tenemos
    public <T> T descifrarConLlave(EncryptedRequestDTO dto, Class<T> claseDestino, byte[] aesKey, byte[] iv) {
        try {
            byte[] dataEncrypted = java.util.Base64.getDecoder().decode(dto.getEncryptedData());
            byte[] dataDecrypted = aesWrapper.decryptCBC(dataEncrypted, aesKey, iv);
            return objectMapper.readValue(new String(dataDecrypted), claseDestino);
        } catch (Exception e) {
            throw new RuntimeException("Error al descifrar datos con AES: " + e.getMessage());
        }
    }
}