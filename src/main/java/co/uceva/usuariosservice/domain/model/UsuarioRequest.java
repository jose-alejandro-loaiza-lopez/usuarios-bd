package co.uceva.usuariosservice.domain.model;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UsuarioRequest {
    private String nombre;
    private String email;
    private String password;
    private LocalDate fechaNacimiento;

}
