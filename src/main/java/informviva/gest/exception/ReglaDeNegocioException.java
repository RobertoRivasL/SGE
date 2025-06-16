package informviva.gest.exception;

/**
 * Excepción lanzada cuando se viola una regla de negocio del sistema.
 * Se utiliza para manejar situaciones donde los datos o acciones son técnicamente válidos
 * pero violan las reglas específicas del dominio del negocio.
 *
 * @author Roberto Rivas
 * @version 2.0
 */
public class ReglaDeNegocioException extends RuntimeException {

    /**
     * Constructor por defecto
     */
    public ReglaDeNegocioException() {
        super("Se ha violado una regla de negocio");
    }

    /**
     * Constructor con mensaje personalizado
     *
     * @param mensaje Mensaje de error personalizado
     */
    public ReglaDeNegocioException(String mensaje) {
        super(mensaje);
    }

    /**
     * Constructor con mensaje y causa
     *
     * @param mensaje Mensaje de error personalizado
     * @param causa   Causa original de la excepción
     */
    public ReglaDeNegocioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    /**
     * Constructor específico para reglas de negocio con código de error
     *
     * @param codigoError Código identificador del error
     * @param mensaje     Mensaje descriptivo del error
     */
    public ReglaDeNegocioException(String codigoError, String mensaje) {
        super(String.format("[%s] %s", codigoError, mensaje));
    }

    /**
     * Constructor para reglas de negocio con entidad y operación específica
     *
     * @param entidad   Nombre de la entidad afectada
     * @param operacion Operación que se intentó realizar
     * @param motivo    Motivo por el cual no se puede realizar
     */
    public ReglaDeNegocioException(String entidad, String operacion, String motivo) {
        super(String.format("No se puede %s %s: %s", operacion, entidad, motivo));
    }

  }