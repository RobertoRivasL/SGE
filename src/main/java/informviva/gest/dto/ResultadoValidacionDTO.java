package informviva.gest.dto;

import java.util.List;

public class ResultadoValidacionDTO {
    private boolean valido;
    private String mensaje;
    private List<String> errores;

    public ResultadoValidacionDTO() {}

    public ResultadoValidacionDTO(boolean valido, String mensaje) {
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
}
