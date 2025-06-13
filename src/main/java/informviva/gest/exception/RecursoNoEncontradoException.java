package informviva.gest.exception;

/**
 * @author Roberto Rivas
 * @version 2.0
 */


/**
 * Excepción lanzada cuando un recurso solicitado no se encuentra en el sistema.
 * Se utiliza para manejar situaciones donde se busca una entidad por ID y no existe.
 *
 * @author Roberto Rivas
 * @version 1.0
 */
public class RecursoNoEncontradoException extends RuntimeException {

    /**
     * Constructor por defecto
     */
    public RecursoNoEncontradoException() {
        super("El recurso solicitado no fue encontrado");
    }

    /**
     * Constructor con mensaje personalizado
     *
     * @param mensaje Mensaje de error personalizado
     */
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    /**
     * Constructor con mensaje y causa
     *
     * @param mensaje Mensaje de error personalizado
     * @param causa   Causa original de la excepción
     */
    public RecursoNoEncontradoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
