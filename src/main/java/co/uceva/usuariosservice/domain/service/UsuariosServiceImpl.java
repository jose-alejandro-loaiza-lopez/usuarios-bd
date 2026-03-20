package co.uceva.usuariosservice.domain.service;

import co.uceva.usuariosservice.domain.exception.AccesoDenegadoException;
import co.uceva.usuariosservice.domain.exception.UsuarioExistenteException;
import co.uceva.usuariosservice.domain.exception.UsuarioNoEncontradoException;
import co.uceva.usuariosservice.domain.model.LoginRequest;
import co.uceva.usuariosservice.domain.model.LoginResponse;
import co.uceva.usuariosservice.domain.model.UsuarioRequest;
import co.uceva.usuariosservice.domain.model.Usuarios;
import co.uceva.usuariosservice.domain.repository.IUsuariosRepository;
import co.uceva.usuariosservice.infrastructure.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de usuarios para EcoMerca2.
 * Gestiona la lógica de negocio, incluyendo la encriptación de credenciales.
 */
@Service
@RequiredArgsConstructor
public class UsuariosServiceImpl implements IUsuariosService {

    private final IUsuariosRepository usuariosRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public Usuarios save(UsuarioRequest request) {

        if (usuariosRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UsuarioExistenteException(request.getEmail());
        }

        Usuarios usuario = new Usuarios();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setFechaNacimiento(request.getFechaNacimiento());
        // 🔐 encriptación
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRole("ROLE_USER");

        return usuariosRepository.save(usuario);
    }

    @Override
    @Transactional
    public void delete(Long id, String emailFromToken) {
        Usuarios operando = usuariosRepository.findByEmail(emailFromToken)
                .orElseThrow(() -> new AccesoDenegadoException("Usuario no encontrado"));

        Usuarios objetivo = usuariosRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));

        boolean esDueno = objetivo.getEmail().equals(emailFromToken);
        boolean esAdmin = operando.getRole().equals("ROLE_ADMIN");

        if (esDueno || esAdmin) {
            usuariosRepository.delete(objetivo);
        } else {
            throw new AccesoDenegadoException("No tienes rango suficiente para borrar a otro usuario.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuarios> findById(Long id) {
        return usuariosRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuarios> findByEmail(String email) {
        return usuariosRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public Usuarios update(Long id, UsuarioRequest request, String emailFromToken) {
        // 1. Buscamos al usuario que opera (quien tiene el token)
        Usuarios operando = usuariosRepository.findByEmail(emailFromToken)
                .orElseThrow(() -> new AccesoDenegadoException("Sesión inválida"));

        // 2. Buscamos el registro real en la BD usando el ID de la URL
        Usuarios existente = usuariosRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));

        // 3. Verificación de seguridad: ¿Es dueño O es admin?
        boolean esDueno = existente.getEmail().equals(emailFromToken);
        boolean esAdmin = operando.getRole().equals("ROLE_ADMIN");

        if (esDueno || esAdmin) {

            // 4. Validación de Email (Si intenta cambiarlo)
            if (!existente.getEmail().equals(request.getEmail())) {
                if (usuariosRepository.findByEmail(request.getEmail()).isPresent()) {
                    throw new UsuarioExistenteException(request.getEmail());
                }
                existente.setEmail(request.getEmail());
            }

            // 5. Actualizamos los demás campos del DTO
            existente.setNombre(request.getNombre());
            existente.setFechaNacimiento(request.getFechaNacimiento());
            existente.setPassword(passwordEncoder.encode(request.getPassword()));

            // El ID, ROL y FAVORITOS se mantienen intactos en 'existente'
            return usuariosRepository.save(existente);
        } else {
            throw new AccesoDenegadoException("No tienes permiso para modificar este perfil.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuarios> findAll() {
        return usuariosRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Usuarios> findAll(Pageable pageable) {
        return usuariosRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Usuarios updateFavoritos(Long id, List<String> nuevosFavoritos, String emailFromToken) {
        Usuarios operando = usuariosRepository.findByEmail(emailFromToken)
                .orElseThrow(() -> new AccesoDenegadoException("Usuario no encontrado"));

        Usuarios usuario = usuariosRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));

        boolean esDueno = usuario.getEmail().equals(emailFromToken);
        boolean esAdmin = operando.getRole().equals("ROLE_ADMIN");

        if (esDueno || esAdmin) {
            usuario.setAlimentosFavoritos(nuevosFavoritos);
        } else {
            throw new AccesoDenegadoException("No puedes modificar los favoritos de otra persona.");
        }

        return usuariosRepository.save(usuario);
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 1. Buscamos al usuario por email
        Usuarios usuario = usuariosRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        // 2. Comparamos la contraseña
        if (passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword())) {
            // 3. Generamos el Token
            String token = jwtUtils.generateToken(usuario);

            // 4. Retornamos solo Token e ID
            return new LoginResponse(token, usuario.getId());
        } else {
            throw new RuntimeException("Credenciales inválidas");
        }
    }
}