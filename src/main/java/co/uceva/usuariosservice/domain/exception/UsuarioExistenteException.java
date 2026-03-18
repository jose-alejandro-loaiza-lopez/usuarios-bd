package co.uceva.usuariosservice.domain.exception;

public class UsuarioExistenteException extends RuntimeException {
    public UsuarioExistenteException(String email) {
        super("El correo electrónico '" + email + "' ya está registrado en EcoMerca2.");
    }
}