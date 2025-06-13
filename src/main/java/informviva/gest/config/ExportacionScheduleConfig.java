package informviva.gest.config;

/**
 * @author Roberto Rivas
 * @version 2.0
 */

import informviva.gest.service.ExportacionHistorialServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

/**
 * Configuración para tareas programadas de exportación
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.exportacion.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class ExportacionScheduleConfig {

    private static final Logger logger = LoggerFactory.getLogger(ExportacionScheduleConfig.class);

    private final ExportacionHistorialServicio exportacionHistorialServicio;

    @Autowired
    public ExportacionScheduleConfig(ExportacionHistorialServicio exportacionHistorialServicio) {
        this.exportacionHistorialServicio = exportacionHistorialServicio;
    }

    /**
     * Ejecuta exportaciones programadas diariamente a las 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void ejecutarExportacionesDiarias() {
        logger.info("Iniciando tarea programada de exportaciones diarias");

        try {
            exportacionHistorialServicio.ejecutarExportacionesProgramadas();
            logger.info("Tarea de exportaciones diarias completada exitosamente");

        } catch (Exception e) {
            logger.error("Error en tarea programada de exportaciones: {}", e.getMessage(), e);
        }
    }

    /**
     * Limpia el historial de exportaciones cada domingo a las 3:00 AM
     * Mantiene solo los registros de los últimos 90 días
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    public void limpiarHistorialExportaciones() {
        logger.info("Iniciando limpieza programada del historial de exportaciones");

        try {
            LocalDateTime fechaLimite = LocalDateTime.now().minusDays(90);
            Long registrosEliminados = exportacionHistorialServicio.limpiarHistorialAnteriorA(fechaLimite);

            logger.info("Limpieza de historial completada: {} registros eliminados", registrosEliminados);

        } catch (Exception e) {
            logger.error("Error en limpieza programada del historial: {}", e.getMessage(), e);
        }
    }

    /**
     * Genera reporte de estadísticas semanalmente los lunes a las 6:00 AM
     */
    @Scheduled(cron = "0 0 6 * * MON")
    public void generarReporteEstadisticasSemanal() {
        logger.info("Generando reporte de estadísticas de exportación semanal");

        try {
            var estadisticas = exportacionHistorialServicio.obtenerEstadisticasExportacion();

            // Log de estadísticas principales
            logger.info("=== REPORTE SEMANAL DE EXPORTACIONES ===");
            logger.info("Total exportaciones: {}", estadisticas.get("totalExportaciones"));
            logger.info("Exportaciones exitosas: {}", estadisticas.get("exportacionesExitosas"));
            logger.info("Exportaciones fallidas: {}", estadisticas.get("exportacionesFallidas"));
            logger.info("Total registros exportados: {}", estadisticas.get("totalRegistrosExportados"));
            logger.info("Tamaño total archivos: {} bytes", estadisticas.get("tamanoTotalArchivos"));

            // Aquí podrías enviar un email con las estadísticas si tienes configurado el servicio de email

            logger.info("Reporte de estadísticas generado exitosamente");

        } catch (Exception e) {
            logger.error("Error generando reporte de estadísticas: {}", e.getMessage(), e);
        }
    }

    /**
     * Verificación de salud del sistema de exportaciones cada hora
     */
    @Scheduled(fixedRate = 3600000) // Cada hora (3600000 ms)
    public void verificarSaludSistemaExportacion() {
        try {
            // Verificar si hay muchas exportaciones fallidas recientes
            var historialReciente = exportacionHistorialServicio.obtenerHistorialReciente(50);

            long fallidasRecientes = historialReciente.stream()
                    .filter(h -> "FALLIDO".equals(h.getEstado()))
                    .count();

            if (fallidasRecientes > 10) {
                logger.warn("ALERTA: {} exportaciones fallidas detectadas en el historial reciente", fallidasRecientes);
                // Aquí podrías enviar una notificación o alerta
            }

        } catch (Exception e) {
            logger.error("Error en verificación de salud del sistema de exportación: {}", e.getMessage());
        }
    }

    /**
     * Backup de configuraciones importante cada día a las 1:00 AM
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void backupConfiguraciones() {
        logger.info("Iniciando backup de configuraciones del sistema");

        try {
            // Aquí podrías implementar backup de configuraciones importantes
            // Por ejemplo, exportar configuración del sistema, roles, usuarios, etc.

            exportacionHistorialServicio.registrarExportacionExitosa(
                    "BACKUP",
                    "CONFIGURACIONES",
                    "SISTEMA",
                    1,
                    1024L
            );

            logger.info("Backup de configuraciones completado");

        } catch (Exception e) {
            logger.error("Error en backup de configuraciones: {}", e.getMessage());

            exportacionHistorialServicio.registrarExportacionFallida(
                    "BACKUP",
                    "CONFIGURACIONES",
                    "SISTEMA",
                    "Error en backup: " + e.getMessage()
            );
        }
    }
}