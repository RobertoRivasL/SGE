package informviva.gest.dto;

import java.util.List;

/**
 * DTO con resultado de validación general
 */
public class ResultadoValidacion {
    private boolean valido;
    private String mensaje;
    private List<String> errores;
    private List<String> advertencias;

    public ResultadoValidacion() {}

    public ResultadoValidacion(boolean valido, String mensaje) {
        this.valido = valido;
        this.mensaje = mensaje;
    }

    // Getters y setters
    public boolean isValido() { return valido; }
    public void setValido(boolean valido) { this.valido = valido; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public List<String> getErrores() { return errores; }
    public void setErrores(List<String> errores) { this.errores = errores; }

    public List<String> getAdvertencias() { return advertencias; }
    public void setAdvertencias(List<String> advertencias) { this.advertencias = advertencias; }
}
