package informviva.gest.exception;

/**
 * Excepción personalizada para errores relacionados con el proceso de importación.
 * Proporciona un manejo de errores más específico y granular.
 *
 * @author Roberto Rivas
 * @version 1.0
 */
public class ImportacionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String codigoError;
    private final String archivoAfectado;
    private final Integer filaAfectada;

    /**
     * Constructor básico con mensaje.
     */
    public ImportacionException(String mensaje) {
        super(mensaje);
        this.codigoError = null;
        this.archivoAfectado = null;
        this.filaAfectada = null;
    }

    /**
     * Constructor con mensaje y causa.
     */
    public ImportacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigoError = null;
        this.archivoAfectado = null;
        this.filaAfectada = null;
    }

    /**
     * Constructor completo con información detallada del error.
     */
    public ImportacionException(String mensaje, String codigoError, String archivoAfectado, Integer filaAfectada) {
        super(mensaje);
        this.codigoError = codigoError;
        this.archivoAfectado = archivoAfectado;
        this.filaAfectada = filaAfectada;
    }

    /**
     * Constructor completo con causa.
     */
    public ImportacionException(String mensaje, Throwable causa, String codigoError,
                                String archivoAfectado, Integer filaAfectada) {
        super(mensaje, causa);
        this.codigoError = codigoError;
        this.archivoAfectado = archivoAfectado;
        this.filaAfectada = filaAfectada;
    }

    public String getCodigoError() {
        return codigoError;
    }

    public String getArchivoAfectado() {
        return archivoAfectado;
    }

    public Integer getFilaAfectada() {
        return filaAfectada;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ImportacionException: ").append(getMessage());

        if (codigoError != null) {
            sb.append(" [Código: ").append(codigoError).append("]");
        }

        if (archivoAfectado != null) {
            sb.append(" [Archivo: ").append(archivoAfectado).append("]");
        }

        if (filaAfectada != null) {
            sb.append(" [Fila: ").append(filaAfectada).append("]");
        }

        return sb.toString();
    }

    /**
     * Métodos de factory para crear excepciones específicas
     */
    public static ImportacionException archivoVacio(String nombreArchivo) {
        return new ImportacionException(
                "El archivo está vacío o no contiene datos válidos",
                "ARCHIVO_VACIO",
                nombreArchivo,
                null
        );
    }

    public static ImportacionException formatoNoSoportado(String nombreArchivo, String formato) {
        return new ImportacionException(
                String.format("Formato de archivo no soportado: %s", formato),
                "FORMATO_NO_SOPORTADO",
                nombreArchivo,
                null
        );
    }

    public static ImportacionException errorEnFila(String nombreArchivo, int numeroFila, String detalle) {
        return new ImportacionException(
                String.format("Error procesando fila %d: %s", numeroFila, detalle),
                "ERROR_FILA",
                nombreArchivo,
                numeroFila
        );
    }

    public static ImportacionException columnasFaltantes(String nombreArchivo, String columnasFaltantes) {
        return new ImportacionException(
                String.format("Columnas requeridas faltantes: %s", columnasFaltantes),
                "COLUMNAS_FALTANTES",
                nombreArchivo,
                null
        );
    }

    public static ImportacionException archivoCorrupto(String nombreArchivo, Throwable causa) {
        return new ImportacionException(
                "El archivo parece estar corrupto o dañado",
                causa,
                "ARCHIVO_CORRUPTO",
                nombreArchivo,
                null
        );
    }
}