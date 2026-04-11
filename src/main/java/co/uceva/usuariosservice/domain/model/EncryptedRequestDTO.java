package co.uceva.usuariosservice.domain.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EncryptedRequestDTO {
    private String encryptedAesKey; // Cifrada con RSA (Base64)
    private String iv;              // Vector de inicialización (Base64)
    private String encryptedData;   // El JSON del usuario cifrado con AES (Base64)
}