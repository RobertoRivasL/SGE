package informviva.gest.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO con resultado de una operación de restauración
 */
public class ResultadoRestauracion {
    private boolean exitoso;
    private String mensaje;
    private LocalDateTime fechaRestauracion;
    private int registrosRestaurados;
    private List<String> errores;
    private List<String> advertencias;

    public ResultadoRestauracion() {}

    public ResultadoRestauracion(boolean exitoso, String mensaje) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.fechaRestauracion = LocalDateTime.now();
    }

    // Getters y setters
    public boolean isExitoso() { return exitoso; }
    public void setExitoso(boolean exitoso) { this.exitoso = exitoso; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public LocalDateTime getFechaRestauracion() { return fechaRestauracion; }
    public void setFechaRestauracion(LocalDateTime fechaRestauracion) { this.fechaRestauracion = fechaRestauracion; }

    public int getRegistrosRestaurados() { return registrosRestaurados; }
    public void setRegistrosRestaurados(int registrosRestaurados) { this.registrosRestaurados = registrosRestaurados; }

    public List<String> getErrores() { return errores; }
    public void setErrores(List<String> errores) { this.errores = errores; }

    public List<String> getAdvertencias() { return advertencias; }
    public void setAdvertencias(List<String> advertencias) { this.advertencias = advertencias; }
}
