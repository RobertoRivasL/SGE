package informviva.gest.exception;

/**
 * @author Roberto Rivas
 * @version 2.0
 *
 */

public class ClienteNoEncontradoException extends RuntimeException {
    public ClienteNoEncontradoException(Long clienteId) {
        super("Cliente no encontrado con ID: " + clienteId);
    }
}