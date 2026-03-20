package co.uceva.usuariosservice.domain.service;

import co.uceva.usuariosservice.domain.model.LoginRequest;
import co.uceva.usuariosservice.domain.model.LoginResponse;
import co.uceva.usuariosservice.domain.model.UsuarioRequest;
import co.uceva.usuariosservice.domain.model.Usuarios;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz que define las operaciones de negocio para los usuarios de EcoMerca2.
 */
public interface IUsuariosService {
    Usuarios save(UsuarioRequest usuario);
    void delete(Long id, String emailFromToken);
    Optional<Usuarios> findById(Long id);
    Usuarios updateFavoritos(Long id, List<String> nuevosFavoritos, String emailFromToken);
    LoginResponse login(LoginRequest loginRequest);
    Optional<Usuarios> findByEmail(String email);
    Usuarios update(Long id, UsuarioRequest usuario, String emailFromToken);
    List<Usuarios> findAll();
    Page<Usuarios> findAll(Pageable pageable);
}