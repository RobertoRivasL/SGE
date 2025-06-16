package informviva.gest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para análisis de rendimiento de exportaciones
 *
 * @author Roberto Rivas
 * @version 3.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportacionAnalisisDTO {

    // ==================== DATOS BÁSICOS ====================

    /**
     * Tipo de exportación (usuarios, clientes, productos, ventas, etc.)
     */
    private String tipo;

    /**
     * Formato de exportación (PDF, EXCEL, CSV, JSON)
     */
    private String formato;

    /**
     * Fecha y hora de solicitud de la exportación
     */
    private LocalDateTime fechaSolicitud;

    /**
     * Fecha y hora de completado de la exportación
     */
    private LocalDateTime fechaCompletado;

    /**
     * Tiempo de procesamiento en milisegundos
     */
    private Long tiempoProcesamientoMs;

    /**
     * Número de registros procesados
     */
    private Integer registrosProcesados;

    /**
     * Tamaño del archivo generado en bytes
     */
    private Long tamanoArchivoBytes;

    /**
     * Usuario que solicitó la exportación
     */
    private String usuario;

    /**
     * Estado de la exportación: "COMPLETADO", "ERROR", "PROCESANDO"
     */
    private String estado;

    /**
     * Mensaje de error (si aplica)
     */
    private String mensajeError;

    // ==================== MÉTRICAS CALCULADAS ====================

    /**
     * Tiempo de procesamiento en formato legible (ej: "2.5s", "1.2min")
     */
    private String tiempoProcesamientoLegible;

    /**
     * Tamaño del archivo en formato legible (ej: "1.5MB", "340KB")
     */
    private String tamanoArchivoLegible;

    /**
     * Registros procesados por segundo
     */
    private Double registrosPorSegundo;

    /**
     * Eficiencia general: "ALTA", "MEDIA", "BAJA"
     */
    private String eficiencia;

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Calcula y establece las métricas automáticamente
     */
    public void calcularMetricas() {
        // Calcular tiempo legible
        this.tiempoProcesamientoLegible = formatearTiempo(this.tiempoProcesamientoMs);

        // Calcular tamaño legible
        this.tamanoArchivoLegible = formatearTamano(this.tamanoArchivoBytes);

        // Calcular registros por segundo
        if (this.tiempoProcesamientoMs != null && this.tiempoProcesamientoMs > 0 && this.registrosProcesados != null) {
            this.registrosPorSegundo = (this.registrosProcesados * 1000.0) / this.tiempoProcesamientoMs;
        }

        // Determinar eficiencia
        this.eficiencia = determinarEficiencia();
    }

    /**
     * Formatea el tiempo en milisegundos a formato legible
     */
    private String formatearTiempo(Long millis) {
        if (millis == null) return "0ms";

        if (millis < 1000) {
            return millis + "ms";
        } else if (millis < 60000) {
            return String.format("%.1fs", millis / 1000.0);
        } else {
            return String.format("%.1fmin", millis / 60000.0);
        }
    }

    /**
     * Formatea el tamaño en bytes a formato legible
     */
    private String formatearTamano(Long bytes) {
        if (bytes == null) return "0B";

        if (bytes < 1024) {
            return bytes + "B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1fKB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1fMB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1fGB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Determina la eficiencia basada en registros por segundo
     */
    private String determinarEficiencia() {
        if (registrosPorSegundo == null) return "DESCONOCIDA";

        if (registrosPorSegundo > 1000) return "ALTA";
        if (registrosPorSegundo > 100) return "MEDIA";
        return "BAJA";
    }

    /**
     * Verifica si la exportación fue exitosa
     */
    public boolean fueExitosa() {
        return "COMPLETADO".equals(estado);
    }

    /**
     * Obtiene la duración total del proceso
     */
    public Long getDuracionTotal() {
        if (fechaSolicitud != null && fechaCompletado != null) {
            return java.time.Duration.between(fechaSolicitud, fechaCompletado).toMillis();
        }
        return tiempoProcesamientoMs;
    }

    /**
     * Obtiene el rendimiento como descripción
     */
    public String getRendimientoDescripcion() {
        if (!fueExitosa()) {
            return "Exportación fallida";
        }

        return switch (eficiencia) {
            case "ALTA" -> "Excelente rendimiento";
            case "MEDIA" -> "Rendimiento aceptable";
            case "BAJA" -> "Rendimiento lento";
            default -> "Rendimiento desconocido";
        };
    }
}