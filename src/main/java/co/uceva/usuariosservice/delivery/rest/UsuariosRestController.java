package co.uceva.usuariosservice.delivery.rest;

import co.uceva.usuariosservice.domain.exception.NoHayUsuariosException;
import co.uceva.usuariosservice.domain.exception.PaginaSinUsuariosException;
import co.uceva.usuariosservice.domain.exception.UsuarioNoEncontradoException;
import co.uceva.usuariosservice.domain.exception.ValidationException;
import co.uceva.usuariosservice.domain.model.LoginRequest;
import co.uceva.usuariosservice.domain.model.LoginResponse;
import co.uceva.usuariosservice.domain.model.UsuarioRequest;
import co.uceva.usuariosservice.domain.model.Usuarios;
import co.uceva.usuariosservice.domain.service.IUsuariosService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuariosRestController {

    private final IUsuariosService usuariosService;

    private static final String MENSAJE = "mensaje";
    private static final String USUARIO = "usuario";
    private static final String USUARIOS = "usuarios";

    public UsuariosRestController(IUsuariosService usuariosService) {
        this.usuariosService = usuariosService;
    }

    /**
     * Listar todos los usuarios de EcoMerca2.
     * (Requiere Token, pero permite ver a otros según tu SecurityConfig actual)
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getUsuarios() {
        List<Usuarios> usuarios = usuariosService.findAll();
        if (usuarios.isEmpty()) {
            throw new NoHayUsuariosException();
        }
        Map<String, Object> response = new HashMap<>();
        response.put(USUARIOS, usuarios);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar usuarios con paginación.
     */
    @GetMapping("/page/{page}")
    public ResponseEntity<Object> index(@PathVariable Integer page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Usuarios> usuariosPage = usuariosService.findAll(pageable);
        if (usuariosPage.isEmpty()) {
            throw new PaginaSinUsuariosException(page);
        }
        return ResponseEntity.ok(usuariosPage);
    }

    /**
     * Registro de un nuevo usuario (Público).
     */
    @PostMapping("/")
    public ResponseEntity<Map<String, Object>> save(@Valid @RequestBody UsuarioRequest usuario, BindingResult result) {
        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
        Map<String, Object> response = new HashMap<>();
        Usuarios nuevoUsuario = usuariosService.save(usuario);
        response.put(MENSAJE, "¡El usuario ha sido creado con éxito!");
        response.put(USUARIO, nuevoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Eliminar un usuario por su ID.
     * PROTECCIÓN: Solo el dueño del token puede borrar su propio ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id, Authentication auth) {
        // auth.getName() obtiene el email del JWT
        usuariosService.delete(id, auth.getName());

        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El usuario ha sido eliminado con éxito.");
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar perfil del usuario.
     * PROTECCIÓN: Solo el dueño puede actualizar su perfil.
     */
    @PutMapping("/")
    public ResponseEntity<Map<String, Object>> update(
            @Valid @RequestBody Usuarios usuario,
            BindingResult result,
            Authentication auth) {

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }

        Usuarios usuarioActualizado = usuariosService.update(usuario, auth.getName());

        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "Perfil actualizado con éxito.");
        response.put(USUARIO, usuarioActualizado);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener un usuario por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> findById(@PathVariable Long id) {
        Usuarios usuario = usuariosService.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "Usuario encontrado.");
        response.put(USUARIO, usuario);
        return ResponseEntity.ok(response);
    }

    /**
     * Sincronizar alimentos favoritos.
     * PROTECCIÓN: Solo el dueño puede modificar sus favoritos.
     */
    @PatchMapping("/{id}/favoritos")
    public ResponseEntity<Map<String, Object>> actualizarFavoritos(
            @PathVariable Long id,
            @RequestBody List<String> favoritos,
            Authentication auth) {

        Usuarios usuarioActualizado = usuariosService.updateFavoritos(id, favoritos, auth.getName());

        Map<String, Object> response = new HashMap<>();
        response.put(USUARIO, usuarioActualizado);
        response.put(MENSAJE, "Lista de alimentos favoritos sincronizada con éxito.");

        return ResponseEntity.ok(response);
    }

    /**
     * Login para obtener el Token JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse authData = usuariosService.login(loginRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("token", authData.getToken());
        response.put("id", authData.getId());
        response.put(MENSAJE, "Bienvenido a EcoMerca2");

        return ResponseEntity.ok(response);
    }
}