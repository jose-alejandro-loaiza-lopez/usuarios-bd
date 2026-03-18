package co.uceva.usuariosservice.domain.exception;

public class NoHayUsuariosException extends RuntimeException {
    public NoHayUsuariosException() {
        super("Actualmente no existen usuarios registrados en el sistema.");
    }
}