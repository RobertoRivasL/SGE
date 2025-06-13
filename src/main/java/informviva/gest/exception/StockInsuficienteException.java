package informviva.gest.exception;

/**
 * @author Roberto Rivas
 * @version 2.0
 */


/**
 * Excepción lanzada cuando no hay suficiente stock de un producto para completar una operación.
 * Se utiliza principalmente al crear o actualizar ventas cuando la cantidad solicitada
 * supera el stock disponible.
 *
 * @author Roberto Rivas
 * @version 1.0
 */
public class StockInsuficienteException extends RuntimeException {

    /**
     * Constructor por defecto
     */
    public StockInsuficienteException() {
        super("No hay suficiente stock disponible");
    }

    /**
     * Constructor con mensaje personalizado
     *
     * @param mensaje Mensaje de error personalizado
     */
    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }

    /**
     * Constructor con mensaje y causa
     *
     * @param mensaje Mensaje de error personalizado
     * @param causa   Causa original de la excepción
     */
    public StockInsuficienteException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    /**
     * Constructor con información detallada del producto
     *
     * @param productoId         ID del producto
     * @param nombreProducto     Nombre del producto
     * @param stockDisponible    Cantidad disponible en stock
     * @param cantidadSolicitada Cantidad solicitada en la operación
     */
    public StockInsuficienteException(Long productoId, String nombreProducto, int stockDisponible, int cantidadSolicitada) {
        super(String.format("Stock insuficiente para el producto '%s' (ID: %d). Disponible: %d, Solicitado: %d",
                nombreProducto, productoId, stockDisponible, cantidadSolicitada));
    }
}