package informviva.gest.service;



/**
 * @author Roberto Rivas
 * @version 2.0
 */

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Clase que encapsula el resultado de la validación de módulos de respaldo
 *
 * @author Roberto Rivas
 * @version 2.0
 */
public class ResultadoValidacionModulo {

    private boolean valido;
    private String mensaje;
    private String codigoError;
    private LocalDateTime fechaValidacion;
    private String nombreModulo;
    private String version;
    private List<String> erroresDetectados;
    private List<String> advertencias;
    private Map<String, Object> metadatos;
    private long tiempoValidacionMs;

    // Constructores
    public ResultadoValidacionModulo() {
        this.fechaValidacion = LocalDateTime.now();
        this.erroresDetectados = new ArrayList<>();
        this.advertencias = new ArrayList<>();
        this.metadatos = new HashMap<>();
    }

    public ResultadoValidacionModulo(boolean valido, String mensaje) {
        this();
        this.valido = valido;
        this.mensaje = mensaje;
    }

    public ResultadoValidacionModulo(boolean valido, String mensaje, String nombreModulo) {
        this(valido, mensaje);
        this.nombreModulo = nombreModulo;
    }

    // Métodos estáticos de fábrica para crear resultados comunes
    public static ResultadoValidacionModulo exitoso(String nombreModulo) {
        return new ResultadoValidacionModulo(true, "Módulo validado exitosamente", nombreModulo);
    }

    public static ResultadoValidacionModulo fallido(String nombreModulo, String razon) {
        return new ResultadoValidacionModulo(false, razon, nombreModulo);
    }

    public static ResultadoValidacionModulo error(String nombreModulo, String codigoError, String mensaje) {
        ResultadoValidacionModulo resultado = new ResultadoValidacionModulo(false, mensaje, nombreModulo);
        resultado.setCodigoError(codigoError);
        return resultado;
    }

    // Métodos de utilidad
    public void agregarError(String error) {
        if (this.erroresDetectados == null) {
            this.erroresDetectados = new ArrayList<>();
        }
        this.erroresDetectados.add(error);
        this.valido = false; // Si hay errores, no es válido
    }

    public void agregarAdvertencia(String advertencia) {
        if (this.advertencias == null) {
            this.advertencias = new ArrayList<>();
        }
        this.advertencias.add(advertencia);
    }

    public void agregarMetadato(String clave, Object valor) {
        if (this.metadatos == null) {
            this.metadatos = new HashMap<>();
        }
        this.metadatos.put(clave, valor);
    }

    public boolean tieneErrores() {
        return erroresDetectados != null && !erroresDetectados.isEmpty();
    }

    public boolean tieneAdvertencias() {
        return advertencias != null && !advertencias.isEmpty();
    }

    public int getNumeroErrores() {
        return erroresDetectados != null ? erroresDetectados.size() : 0;
    }

    public int getNumeroAdvertencias() {
        return advertencias != null ? advertencias.size() : 0;
    }

    /**
     * Combina este resultado con otro resultado de validación
     */
    public ResultadoValidacionModulo combinarCon(ResultadoValidacionModulo otro) {
        ResultadoValidacionModulo combinado = new ResultadoValidacionModulo();

        combinado.valido = this.valido && otro.valido;
        combinado.mensaje = this.mensaje + "; " + otro.mensaje;
        combinado.nombreModulo = this.nombreModulo;
        combinado.fechaValidacion = LocalDateTime.now();

        // Combinar errores
        if (this.erroresDetectados != null) {
            combinado.erroresDetectados.addAll(this.erroresDetectados);
        }
        if (otro.erroresDetectados != null) {
            combinado.erroresDetectados.addAll(otro.erroresDetectados);
        }

        // Combinar advertencias
        if (this.advertencias != null) {
            combinado.advertencias.addAll(this.advertencias);
        }
        if (otro.advertencias != null) {
            combinado.advertencias.addAll(otro.advertencias);
        }

        // Combinar metadatos
        if (this.metadatos != null) {
            combinado.metadatos.putAll(this.metadatos);
        }
        if (otro.metadatos != null) {
            combinado.metadatos.putAll(otro.metadatos);
        }

        return combinado;
    }

    /**
     * Genera un resumen textual del resultado
     */
    public String generarResumen() {
        StringBuilder resumen = new StringBuilder();

        resumen.append("Módulo: ").append(nombreModulo != null ? nombreModulo : "Desconocido");
        resumen.append(" | Estado: ").append(valido ? "VÁLIDO" : "INVÁLIDO");

        if (tieneErrores()) {
            resumen.append(" | Errores: ").append(getNumeroErrores());
        }

        if (tieneAdvertencias()) {
            resumen.append(" | Advertencias: ").append(getNumeroAdvertencias());
        }

        if (mensaje != null && !mensaje.isEmpty()) {
            resumen.append(" | Mensaje: ").append(mensaje);
        }

        return resumen.toString();
    }

    // Getters y Setters
    public boolean isValido() {
        return valido;
    }

    public void setValido(boolean valido) {
        this.valido = valido;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getCodigoError() {
        return codigoError;
    }

    public void setCodigoError(String codigoError) {
        this.codigoError = codigoError;
    }

    public LocalDateTime getFechaValidacion() {
        return fechaValidacion;
    }

    public void setFechaValidacion(LocalDateTime fechaValidacion) {
        this.fechaValidacion = fechaValidacion;
    }

    public String getNombreModulo() {
        return nombreModulo;
    }

    public void setNombreModulo(String nombreModulo) {
        this.nombreModulo = nombreModulo;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getErroresDetectados() {
        return erroresDetectados;
    }

    public void setErroresDetectados(List<String> erroresDetectados) {
        this.erroresDetectados = erroresDetectados;
    }

    public List<String> getAdvertencias() {
        return advertencias;
    }

    public void setAdvertencias(List<String> advertencias) {
        this.advertencias = advertencias;
    }

    public Map<String, Object> getMetadatos() {
        return metadatos;
    }

    public void setMetadatos(Map<String, Object> metadatos) {
        this.metadatos = metadatos;
    }

    public long getTiempoValidacionMs() {
        return tiempoValidacionMs;
    }

    public void setTiempoValidacionMs(long tiempoValidacionMs) {
        this.tiempoValidacionMs = tiempoValidacionMs;
    }

    @Override
    public String toString() {
        return "ResultadoValidacionModulo{" +
                "valido=" + valido +
                ", mensaje='" + mensaje + '\'' +
                ", nombreModulo='" + nombreModulo + '\'' +
                ", errores=" + getNumeroErrores() +
                ", advertencias=" + getNumeroAdvertencias() +
                ", fechaValidacion=" + fechaValidacion +
                '}';
    }
}
