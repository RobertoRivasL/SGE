package informviva.gest.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO con información de un respaldo
 */
public class InfoRestauracion {
    private String nombreArchivo;
    private long tamaño;
    private LocalDateTime fechaCreacion;
    private boolean valido;
    private String mensajeError;
    private List<String> modulos;
    private int totalRegistros;

    public InfoRestauracion() {}

    // Getters y setters
    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public long getTamaño() { return tamaño; }
    public void setTamaño(long tamaño) { this.tamaño = tamaño; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public boolean isValido() { return valido; }
    public void setValido(boolean valido) { this.valido = valido; }

    public String getMensajeError() { return mensajeError; }
    public void setMensajeError(String mensajeError) { this.mensajeError = mensajeError; }

    public List<String> getModulos() { return modulos; }
    public void setModulos(List<String> modulos) { this.modulos = modulos; }

    public int getTotalRegistros() { return totalRegistros; }
    public void setTotalRegistros(int totalRegistros) { this.totalRegistros = totalRegistros; }
}
