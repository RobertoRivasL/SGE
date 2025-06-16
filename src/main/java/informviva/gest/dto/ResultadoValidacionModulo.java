package informviva.gest.dto;


import informviva.gest.dto.ErrorValidacion;
import informviva.gest.enums.NivelSeveridad;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase que encapsula el resultado de la validación de módulos del sistema
 * Actualizada con métodos faltantes para compatibilidad con ValidacionDatosServicio
 *
 * @author Roberto Rivas
 * @version 2.1
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

    // ⭐ NUEVOS CAMPOS AÑADIDOS - Necesarios para ValidacionDatosServicio
    private int erroresCriticos = 0;
    private int advertenciasCount = 0;
    private Map<String, List<ErrorValidacion>> registrosConErrores = new HashMap<>();
    private int totalRegistros = 0;

    // Constructores
    public ResultadoValidacionModulo() {
        this.fechaValidacion = LocalDateTime.now();
        this.erroresDetectados = new ArrayList<>();
        this.advertencias = new ArrayList<>();
        this.metadatos = new HashMap<>();
        this.registrosConErrores = new HashMap<>();
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

    // ⭐ NUEVO CONSTRUCTOR - Para compatibilidad con ValidacionDatosServicio
    public ResultadoValidacionModulo(String nombreModulo) {
        this();
        this.nombreModulo = nombreModulo;
        this.valido = true; // Inicia como válido, se marca como false si hay errores críticos
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

    // ⭐ MÉTODOS NUEVOS - Necesarios para ValidacionDatosServicio

    /**
     * Obtiene el número de errores críticos
     */
    public int getErroresCriticos() {
        return erroresCriticos;
    }

    public void setErroresCriticos(int erroresCriticos) {
        this.erroresCriticos = erroresCriticos;
    }

    /**
     * Obtiene el número de advertencias
     */
    public int getAdvertencias() {
        return advertenciasCount;
    }

    public void setAdvertencias(int advertencias) {
        this.advertenciasCount = advertencias;
    }

    /**
     * Obtiene el mapa de registros con errores
     */
    public Map<String, List<ErrorValidacion>> getRegistrosConErrores() {
        return registrosConErrores;
    }

    public void setRegistrosConErrores(Map<String, List<ErrorValidacion>> registrosConErrores) {
        this.registrosConErrores = registrosConErrores;
    }

    /**
     * Obtiene el total de registros procesados
     */
    public int getTotalRegistros() {
        return totalRegistros;
    }

    public void setTotalRegistros(int totalRegistros) {
        this.totalRegistros = totalRegistros;
    }

    /**
     * Calcula estadísticas basadas en los errores registrados
     * MÉTODO CRÍTICO - Requerido por ValidacionDatosServicio
     */
    public void calcularEstadisticas() {
        erroresCriticos = 0;
        advertenciasCount = 0;

        // Contar errores por severidad
        for (List<ErrorValidacion> errores : registrosConErrores.values()) {
            for (ErrorValidacion error : errores) {
                if (error.getSeveridad() == NivelSeveridad.CRITICO) {
                    erroresCriticos++;
                } else if (error.getSeveridad() == NivelSeveridad.ADVERTENCIA) {
                    advertenciasCount++;
                }
            }
        }

        // Sincronizar con listas legacy
        erroresDetectados.clear();
        advertencias.clear();

        for (Map.Entry<String, List<ErrorValidacion>> entry : registrosConErrores.entrySet()) {
            for (ErrorValidacion error : entry.getValue()) {
                if (error.esCritico()) {
                    erroresDetectados.add(error.toString());
                } else if (error.esAdvertencia()) {
                    advertencias.add(error.toString());
                }
            }
        }

        // Marcar como inválido si hay errores críticos
        if (erroresCriticos > 0) {
            this.valido = false;
            if (this.mensaje == null || this.mensaje.isEmpty()) {
                this.mensaje = "Validación fallida: " + erroresCriticos + " errores críticos encontrados";
            }
        }

        // Actualizar metadatos
        metadatos.put("erroresCriticos", erroresCriticos);
        metadatos.put("advertencias", advertenciasCount);
        metadatos.put("totalRegistros", totalRegistros);
        metadatos.put("porcentajeExito", calcularPorcentajeExito());
    }

    /**
     * Calcula el porcentaje de éxito de la validación
     */
    private double calcularPorcentajeExito() {
        if (totalRegistros == 0) return 100.0;
        int registrosConErrores = this.registrosConErrores.size();
        return ((double) (totalRegistros - registrosConErrores) / totalRegistros) * 100.0;
    }

    // ⭐ MÉTODOS DE UTILIDAD ADICIONALES

    /**
     * Agrega un error al registro de errores
     */
    public void agregarError(String identificadorRegistro, ErrorValidacion error) {
        registrosConErrores.computeIfAbsent(identificadorRegistro, k -> new ArrayList<>()).add(error);
    }

    /**
     * Agrega múltiples errores para un registro
     */
    public void agregarErrores(String identificadorRegistro, List<ErrorValidacion> errores) {
        registrosConErrores.computeIfAbsent(identificadorRegistro, k -> new ArrayList<>()).addAll(errores);
    }

    /**
     * Verifica si un registro específico tiene errores
     */
    public boolean tieneErrores(String identificadorRegistro) {
        List<ErrorValidacion> errores = registrosConErrores.get(identificadorRegistro);
        return errores != null && !errores.isEmpty();
    }

    /**
     * Obtiene los errores de un registro específico
     */
    public List<ErrorValidacion> obtenerErrores(String identificadorRegistro) {
        return registrosConErrores.getOrDefault(identificadorRegistro, new ArrayList<>());
    }

    /**
     * Obtiene todos los errores críticos
     */
    public List<ErrorValidacion> obtenerErroresCriticos() {
        List<ErrorValidacion> criticos = new ArrayList<>();
        for (List<ErrorValidacion> errores : registrosConErrores.values()) {
            errores.stream()
                    .filter(ErrorValidacion::esCritico)
                    .forEach(criticos::add);
        }
        return criticos;
    }

    /**
     * Obtiene todas las advertencias
     */
    public List<ErrorValidacion> obtenerAdvertencias() {
        List<ErrorValidacion> advertenciasList = new ArrayList<>();
        for (List<ErrorValidacion> errores : registrosConErrores.values()) {
            errores.stream()
                    .filter(ErrorValidacion::esAdvertencia)
                    .forEach(advertenciasList::add);
        }
        return advertenciasList;
    }

    // MÉTODOS ORIGINALES - Mantenidos para compatibilidad

    public void agregarError(String error) {
        if (this.erroresDetectados == null) {
            this.erroresDetectados = new ArrayList<>();
        }
        this.erroresDetectados.add(error);
        this.valido = false;
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
        return (erroresDetectados != null && !erroresDetectados.isEmpty()) || erroresCriticos > 0;
    }

    public boolean tieneAdvertencias() {
        return (advertencias != null && !advertencias.isEmpty()) || advertenciasCount > 0;
    }

    public int getNumeroErrores() {
        int erroresLegacy = erroresDetectados != null ? erroresDetectados.size() : 0;
        return Math.max(erroresLegacy, erroresCriticos);
    }

    public int getNumeroAdvertencias() {
        int advertenciasLegacy = advertencias != null ? advertencias.size() : 0;
        return Math.max(advertenciasLegacy, advertenciasCount);
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

        // Combinar errores legacy
        if (this.erroresDetectados != null) {
            combinado.erroresDetectados.addAll(this.erroresDetectados);
        }
        if (otro.erroresDetectados != null) {
            combinado.erroresDetectados.addAll(otro.erroresDetectados);
        }

        // Combinar advertencias legacy
        if (this.advertencias != null) {
            combinado.advertencias.addAll(this.advertencias);
        }
        if (otro.advertencias != null) {
            combinado.advertencias.addAll(otro.advertencias);
        }

        // Combinar registros con errores
        combinado.registrosConErrores.putAll(this.registrosConErrores);
        combinado.registrosConErrores.putAll(otro.registrosConErrores);

        // Combinar estadísticas
        combinado.erroresCriticos = this.erroresCriticos + otro.erroresCriticos;
        combinado.advertenciasCount = this.advertenciasCount + otro.advertenciasCount;
        combinado.totalRegistros = this.totalRegistros + otro.totalRegistros;

        // Combinar metadatos
        if (this.metadatos != null) {
            combinado.metadatos.putAll(this.metadatos);
        }
        if (otro.metadatos != null) {
            combinado.metadatos.putAll(otro.metadatos);
        }

        // Recalcular estadísticas
        combinado.calcularEstadisticas();

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

        if (totalRegistros > 0) {
            resumen.append(" | Registros procesados: ").append(totalRegistros);
            resumen.append(" | Éxito: ").append(String.format("%.1f%%", calcularPorcentajeExito()));
        }

        if (mensaje != null && !mensaje.isEmpty()) {
            resumen.append(" | Mensaje: ").append(mensaje);
        }

        return resumen.toString();
    }

    // Getters y Setters originales
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

    public List<String> getAdvertenciasLegacy() {
        return advertencias;
    }

    public void setAdvertenciasLegacy(List<String> advertencias) {
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
                ", registros=" + totalRegistros +
                ", fechaValidacion=" + fechaValidacion +
                '}';
    }
}