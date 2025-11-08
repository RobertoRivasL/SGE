package informviva.gest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Map;

/**
 * DTO mejorado que encapsula el resultado de una operación de importación
 *
 * @author Roberto Rivas
 * @version 2.1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportacionResultadoDTO {

    private String tipoEntidad;
    private String nombreArchivo;
    private Long usuarioId;
    private String tipoImportacion;
    private Integer totalRegistros;
    private boolean exitoso;
    private String resumen;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaImportacion;

    // Contadores
    private int registrosProcesados = 0;
    private int registrosExitosos = 0;
    private int registrosConError = 0;
    private int registrosOmitidos = 0;

    // Mensajes categorizados
    private List<String> errores = new ArrayList<>();
    private List<String> advertencias = new ArrayList<>();
    private List<String> mensajesInfo = new ArrayList<>();

    // Estadísticas adicionales
    private long tiempoProcesamientoMs = 0;
    private double porcentajeExito = 0.0;

    // Información del archivo
    private long tamanoArchivoBytes = 0;
    private String formatoArchivo;

    // Detalles de procesamiento
    private List<DetalleError> erroresDetallados = new ArrayList<>();
    private String resumeProcesamiento;

    /**
     * Constructor de conveniencia para inicialización básica
     */
    public ImportacionResultadoDTO(String tipoEntidad, String nombreArchivo) {
        this.tipoEntidad = tipoEntidad;
        this.nombreArchivo = nombreArchivo;
        this.fechaImportacion = LocalDateTime.now();
    }

    /**
     * Agrega un error al resultado
     */
    public void agregarError(String error) {
        if (error != null && !error.trim().isEmpty()) {
            this.errores.add(error.trim());
        }
    }

    /**
     * Agrega un error detallado al resultado
     */
    public void agregarErrorDetallado(int fila, String campo, String valor, String descripcion) {
        DetalleError detalle = new DetalleError(fila, campo, valor, descripcion);
        this.erroresDetallados.add(detalle);
        agregarError("Fila " + fila + ": " + descripcion);
    }

    /**
     * Agrega una advertencia al resultado
     */
    public void agregarAdvertencia(String advertencia) {
        if (advertencia != null && !advertencia.trim().isEmpty()) {
            this.advertencias.add(advertencia.trim());
        }
    }

    /**
     * Agrega un mensaje informativo al resultado
     */
    public void agregarMensajeInfo(String mensaje) {
        if (mensaje != null && !mensaje.trim().isEmpty()) {
            this.mensajesInfo.add(mensaje.trim());
        }
    }

    /**
     * Incrementa el contador de registros exitosos
     */
    public void incrementarExitosos() {
        this.registrosExitosos++;
    }

    /**
     * Incrementa el contador de registros con error
     */
    public void incrementarErrores() {
        this.registrosConError++;
    }

    /**
     * Incrementa el contador de registros omitidos
     */
    public void incrementarOmitidos() {
        this.registrosOmitidos++;
    }

    /**
     * Incrementa múltiples contadores de una vez
     */
    public void incrementarContadores(int exitosos, int errores, int omitidos) {
        this.registrosExitosos += exitosos;
        this.registrosConError += errores;
        this.registrosOmitidos += omitidos;
    }

    /**
     * Calcula los totales y porcentajes
     */
    public void calcularTotales() {
        this.registrosProcesados = this.registrosExitosos + this.registrosConError + this.registrosOmitidos;

        if (this.registrosProcesados > 0) {
            this.porcentajeExito = Math.round(((double) this.registrosExitosos / this.registrosProcesados) * 100.0 * 100.0) / 100.0;
        } else {
            this.porcentajeExito = 0.0;
        }

        // Generar resumen de procesamiento
        generarResumenProcesamiento();
    }

    /**
     * Verifica si hay errores en el resultado
     */
    public boolean tieneErrores() {
        return !this.errores.isEmpty() || this.registrosConError > 0;
    }

    /**
     * Verifica si hay advertencias en el resultado
     */
    public boolean tieneAdvertencias() {
        return !this.advertencias.isEmpty() || this.registrosOmitidos > 0;
    }

    /**
     * Verifica si la importación fue completamente exitosa
     */
    @JsonIgnore
    public boolean esExitoso() {
        return !tieneErrores() && registrosExitosos > 0;
    }

    /**
     * Verifica si la importación falló completamente
     */
    @JsonIgnore
    public boolean esFallido() {
        return registrosExitosos == 0 && (tieneErrores() || registrosProcesados == 0);
    }

    /**
     * Obtiene un resumen textual del resultado
     */
    public String getResumen() {
        StringBuilder resumen = new StringBuilder();
        resumen.append("Importación de ").append(tipoEntidad).append(" completada.\n");
        resumen.append("Archivo: ").append(nombreArchivo).append("\n");
        resumen.append("Fecha: ").append(fechaImportacion).append("\n\n");

        resumen.append("Resultados:\n");
        resumen.append("- Total procesados: ").append(registrosProcesados).append("\n");
        resumen.append("- Exitosos: ").append(registrosExitosos).append("\n");
        resumen.append("- Con errores: ").append(registrosConError).append("\n");
        resumen.append("- Omitidos: ").append(registrosOmitidos).append("\n");
        resumen.append("- Porcentaje de éxito: ").append(String.format("%.2f", porcentajeExito)).append("%\n");

        if (tiempoProcesamientoMs > 0) {
            resumen.append("- Tiempo de procesamiento: ").append(formatearTiempo(tiempoProcesamientoMs)).append("\n");
        }

        if (tamanoArchivoBytes > 0) {
            resumen.append("- Tamaño del archivo: ").append(formatearTamano(tamanoArchivoBytes)).append("\n");
        }

        return resumen.toString();
    }

    /**
     * Obtiene el estado general de la importación
     */
    public EstadoImportacion getEstado() {
        if (registrosProcesados == 0) {
            return EstadoImportacion.SIN_PROCESAR;
        } else if (registrosConError == 0 && registrosExitosos > 0) {
            return registrosOmitidos > 0 ? EstadoImportacion.EXITOSO_CON_ADVERTENCIAS : EstadoImportacion.EXITOSO;
        } else if (registrosExitosos > 0) {
            return EstadoImportacion.PARCIAL;
        } else {
            return EstadoImportacion.FALLIDO;
        }
    }

    /**
     * Obtiene estadísticas resumidas para dashboards
     */
    @JsonIgnore
    public EstadisticasResumen getEstadisticasResumen() {
        return new EstadisticasResumen(
                registrosProcesados,
                registrosExitosos,
                registrosConError,
                registrosOmitidos,
                porcentajeExito,
                tiempoProcesamientoMs,
                getEstado()
        );
    }

    /**
     * Valida la consistencia interna de los datos
     */
    public boolean validarConsistencia() {
        return registrosProcesados == (registrosExitosos + registrosConError + registrosOmitidos) &&
                porcentajeExito >= 0.0 && porcentajeExito <= 100.0 &&
                tiempoProcesamientoMs >= 0;
    }

    /**
     * Combina resultados de múltiples importaciones
     */
    public void combinarCon(ImportacionResultadoDTO otro) {
        if (otro == null) return;

        this.registrosExitosos += otro.registrosExitosos;
        this.registrosConError += otro.registrosConError;
        this.registrosOmitidos += otro.registrosOmitidos;

        this.errores.addAll(otro.errores);
        this.advertencias.addAll(otro.advertencias);
        this.mensajesInfo.addAll(otro.mensajesInfo);
        this.erroresDetallados.addAll(otro.erroresDetallados);

        this.tiempoProcesamientoMs += otro.tiempoProcesamientoMs;

        calcularTotales();
    }

    // Métodos auxiliares privados

    private void generarResumenProcesamiento() {
        StringBuilder resumen = new StringBuilder();

        if (esExitoso()) {
            resumen.append("Importación completada exitosamente. ");
        } else if (esFallido()) {
            resumen.append("Importación fallida. ");
        } else {
            resumen.append("Importación completada parcialmente. ");
        }

        resumen.append(String.format("Procesados: %d, Exitosos: %d (%.1f%%)",
                registrosProcesados, registrosExitosos, porcentajeExito));

        if (registrosConError > 0) {
            resumen.append(String.format(", Errores: %d", registrosConError));
        }

        if (registrosOmitidos > 0) {
            resumen.append(String.format(", Omitidos: %d", registrosOmitidos));
        }

        this.resumeProcesamiento = resumen.toString();
    }

    private String formatearTiempo(long milisegundos) {
        if (milisegundos < 1000) {
            return milisegundos + " ms";
        } else if (milisegundos < 60000) {
            return String.format("%.1f s", milisegundos / 1000.0);
        } else {
            long minutos = milisegundos / 60000;
            long segundos = (milisegundos % 60000) / 1000;
            return String.format("%d m %d s", minutos, segundos);
        }
    }

    private String formatearTamano(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    // Clases internas

    /**
     * Clase para detalles específicos de errores
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleError {
        private int fila;
        private String campo;
        private String valor;
        private String descripcion;
        private LocalDateTime timestamp = LocalDateTime.now();

        public DetalleError(int fila, String campo, String valor, String descripcion) {
            this.fila = fila;
            this.campo = campo;
            this.valor = valor;
            this.descripcion = descripcion;
            this.timestamp = LocalDateTime.now();
        }
    }

    /**
     * Clase para estadísticas resumidas
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstadisticasResumen {
        private int totalProcesados;
        private int exitosos;
        private int errores;
        private int omitidos;
        private double porcentajeExito;
        private long tiempoMs;
        private EstadoImportacion estado;
    }

    /**
     * Enum para los posibles estados de una importación
     */
    public enum EstadoImportacion {
        SIN_PROCESAR("Sin procesar", "secondary"),
        EXITOSO("Exitoso", "success"),
        EXITOSO_CON_ADVERTENCIAS("Exitoso con advertencias", "warning"),
        PARCIAL("Parcialmente exitoso", "info"),
        FALLIDO("Fallido", "danger");

        private final String descripcion;
        private final String colorBootstrap;

        EstadoImportacion(String descripcion, String colorBootstrap) {
            this.descripcion = descripcion;
            this.colorBootstrap = colorBootstrap;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public String getColorBootstrap() {
            return colorBootstrap;
        }

        public boolean esExitoso() {
            return this == EXITOSO || this == EXITOSO_CON_ADVERTENCIAS;
        }

        public boolean tieneFallas() {
            return this == FALLIDO || this == PARCIAL;
        }
    }

    // Override métodos Object para mejor funcionalidad

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImportacionResultadoDTO that = (ImportacionResultadoDTO) o;
        return Objects.equals(tipoEntidad, that.tipoEntidad) &&
                Objects.equals(nombreArchivo, that.nombreArchivo) &&
                Objects.equals(fechaImportacion, that.fechaImportacion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipoEntidad, nombreArchivo, fechaImportacion);
    }

    @Override
    public String toString() {
        return String.format("ImportacionResultadoDTO{tipo='%s', archivo='%s', exitosos=%d, errores=%d, omitidos=%d, exito=%.1f%%}",
                tipoEntidad, nombreArchivo, registrosExitosos, registrosConError, registrosOmitidos, porcentajeExito);
    }

    public Long getUsuarioId() { return usuarioId; }
    public String getTipoImportacion() { return tipoImportacion; }
    public Integer getTotalRegistros() { return totalRegistros; }
    public boolean isExitoso() { return exitoso; }
}