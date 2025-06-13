package informviva.gest.exception;


/**
 * Excepción específica para errores durante el proceso de importación
 *
 * @author Roberto Rivas
 * @version 2.0
 */
public class ImportacionException extends RuntimeException {

    private final String tipoEntidad;
    private final String nombreArchivo;
    private final int numeroFila;

    /**
     * Constructor básico
     */
    public ImportacionException(String mensaje) {
        super(mensaje);
        this.tipoEntidad = null;
        this.nombreArchivo = null;
        this.numeroFila = -1;
    }

    /**
     * Constructor con información detallada
     */
    public ImportacionException(String mensaje, String tipoEntidad, String nombreArchivo) {
        super(mensaje);
        this.tipoEntidad = tipoEntidad;
        this.nombreArchivo = nombreArchivo;
        this.numeroFila = -1;
    }

    /**
     * Constructor con número de fila específico
     */
    public ImportacionException(String mensaje, String tipoEntidad, String nombreArchivo, int numeroFila) {
        super(mensaje);
        this.tipoEntidad = tipoEntidad;
        this.nombreArchivo = nombreArchivo;
        this.numeroFila = numeroFila;
    }

    /**
     * Constructor con causa
     */
    public ImportacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.tipoEntidad = null;
        this.nombreArchivo = null;
        this.numeroFila = -1;
    }

    /**
     * Constructor completo
     */
    public ImportacionException(String mensaje, Throwable causa, String tipoEntidad, String nombreArchivo, int numeroFila) {
        super(mensaje, causa);
        this.tipoEntidad = tipoEntidad;
        this.nombreArchivo = nombreArchivo;
        this.numeroFila = numeroFila;
    }

    // Getters
    public String getTipoEntidad() {
        return tipoEntidad;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public int getNumeroFila() {
        return numeroFila;
    }

    @Override
    public String getMessage() {
        StringBuilder mensaje = new StringBuilder(super.getMessage());

        if (tipoEntidad != null) {
            mensaje.append(" [Tipo: ").append(tipoEntidad).append("]");
        }

        if (nombreArchivo != null) {
            mensaje.append(" [Archivo: ").append(nombreArchivo).append("]");
        }

        if (numeroFila > 0) {
            mensaje.append(" [Fila: ").append(numeroFila).append("]");
        }

        return mensaje.toString();
    }
}