package informviva.gest.exception;

/**
 * @author Roberto Rivas
 * @version 2.0
 */

import java.time.LocalDateTime;

/**
 * Clase que representa la estructura de respuesta para errores.
 * Se utiliza para proporcionar un formato consistente de errores en la API.
 *
 * @author Roberto Rivas
 * @version 1.0
 */
public class ErrorResponse {

    private int estado;
    private String mensaje;
    private String ruta;
    private LocalDateTime timestamp;

    /**
     * Constructor con todos los campos
     *
     * @param estado    Código de estado HTTP
     * @param mensaje   Mensaje descriptivo del error
     * @param ruta      Ruta de la solicitud que causó el error
     * @param timestamp Fecha y hora en que ocurrió el error
     */
    public ErrorResponse(int estado, String mensaje, String ruta, LocalDateTime timestamp) {
        this.estado = estado;
        this.mensaje = mensaje;
        this.ruta = ruta;
        this.timestamp = timestamp;
    }

    // Getters y setters

    /**
     * Obtiene el código de estado HTTP
     *
     * @return Código de estado
     */
    public int getEstado() {
        return estado;
    }

    /**
     * Establece el código de estado HTTP
     *
     * @param estado Nuevo código de estado
     */
    public void setEstado(int estado) {
        this.estado = estado;
    }

    /**
     * Obtiene el mensaje de error
     *
     * @return Mensaje de error
     */
    public String getMensaje() {
        return mensaje;
    }

    /**
     * Establece el mensaje de error
     *
     * @param mensaje Nuevo mensaje de error
     */
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    /**
     * Obtiene la ruta de la solicitud
     *
     * @return Ruta de la solicitud
     */
    public String getRuta() {
        return ruta;
    }

    /**
     * Establece la ruta de la solicitud
     *
     * @param ruta Nueva ruta de solicitud
     */
    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    /**
     * Obtiene la fecha y hora del error
     *
     * @return Fecha y hora del error
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Establece la fecha y hora del error
     *
     * @param timestamp Nueva fecha y hora
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "estado=" + estado +
                ", mensaje='" + mensaje + '\'' +
                ", ruta='" + ruta + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}