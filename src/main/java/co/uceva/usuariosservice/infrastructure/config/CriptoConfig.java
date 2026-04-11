package co.uceva.usuariosservice.infrastructure.config;

import co.uceva.usuariosservice.infrastructure.security.AesWrapper;
import co.uceva.usuariosservice.infrastructure.security.RsaEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CriptoConfig {

    @Bean
    public RsaEngine rsaEngine() {
        // Esto se ejecuta UNA sola vez al iniciar el servidor
        return new RsaEngine(2048);
    }

    @Bean
    public AesWrapper aesWrapper() {
        return new AesWrapper();
    }
}