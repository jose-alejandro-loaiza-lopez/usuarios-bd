package co.uceva.usuariosservice.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class Usuarios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Email(message = "Debe ser un correo electrónico válido")
    @NotBlank(message = "El email es obligatorio")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @ElementCollection
    @CollectionTable(name = "usuario_alimentos_favoritos", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "alimento")
    private List<String> alimentosFavoritos = new ArrayList<>();

    @Column(nullable = false)
    @NotBlank(message = "El rol es obligatorio")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String role;
}