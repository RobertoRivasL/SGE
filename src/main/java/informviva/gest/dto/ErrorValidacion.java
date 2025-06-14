package informviva.gest.dto;

import informviva.gest.enums.NivelSeveridad;

import java.time.LocalDateTime;

/**
 * DTO que representa un error de validación específico
 *
 * @author Roberto Rivas
 * @version 2.0
 */
public class ErrorValidacion {

    private String campo;
    private String mensaje;
    private String descripcionDetallada;
    private NivelSeveridad severidad;
    private LocalDateTime fechaDeteccion;
    private String codigoError;
    private String valorRechazado;

    // Constructores
    public ErrorValidacion() {
        this.fechaDeteccion = LocalDateTime.now();
    }

    public ErrorValidacion(String campo, String mensaje, NivelSeveridad severidad) {
        this();
        this.campo = campo;
        this.mensaje = mensaje;
        this.severidad = severidad;
    }

    public ErrorValidacion(String campo, String mensaje, NivelSeveridad severidad, String descripcionDetallada) {
        this(campo, mensaje, severidad);
        this.descripcionDetallada = descripcionDetallada;
    }

    public ErrorValidacion(String campo, String mensaje, NivelSeveridad severidad,
                           String descripcionDetallada, String valorRechazado) {
        this(campo, mensaje, severidad, descripcionDetallada);
        this.valorRechazado = valorRechazado;
    }

    // Métodos de utilidad
    public boolean esCritico() {
        return severidad != null && severidad.esCritico();
    }

    public boolean esAdvertencia() {
        return severidad != null && severidad.esAdvertencia();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s",
                severidad != null ? severidad.getCodigo() : "UNKNOWN",
                campo != null ? campo : "CAMPO_DESCONOCIDO",
                mensaje != null ? mensaje : "Sin mensaje");
    }

    // Getters y Setters
    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getDescripcionDetallada() {
        return descripcionDetallada;
    }

    public void setDescripcionDetallada(String descripcionDetallada) {
        this.descripcionDetallada = descripcionDetallada;
    }

    public NivelSeveridad getSeveridad() {
        return severidad;
    }

    public void setSeveridad(NivelSeveridad severidad) {
        this.severidad = severidad;
    }

    public LocalDateTime getFechaDeteccion() {
        return fechaDeteccion;
    }

    public void setFechaDeteccion(LocalDateTime fechaDeteccion) {
        this.fechaDeteccion = fechaDeteccion;
    }

    public String getCodigoError() {
        return codigoError;
    }

    public void setCodigoError(String codigoError) {
        this.codigoError = codigoError;
    }

    public String getValorRechazado() {
        return valorRechazado;
    }

    public void setValorRechazado(String valorRechazado) {
        this.valorRechazado = valorRechazado;
    }
}