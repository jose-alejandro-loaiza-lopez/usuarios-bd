package co.uceva.usuariosservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class UsuariosServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsuariosServiceApplication.class, args);
    }

}
