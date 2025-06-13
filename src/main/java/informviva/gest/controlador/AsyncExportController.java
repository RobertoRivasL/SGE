// ===================================================================
// CONTROLADOR PARA EXPORTACIONES ASÍNCRONAS - AsyncExportController.java
// ===================================================================

package informviva.gest.controlador;

import informviva.gest.dto.ExportConfigDTO;
import informviva.gest.service.ExportacionServicio;
import informviva.gest.service.impl.ExportNotificationService;
import informviva.gest.service.impl.ExportCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Controlador para manejar exportaciones asíncronas de archivos grandes
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@RestController
@RequestMapping("/exportacion/async")
public class AsyncExportController {

    private static final Logger logger = LoggerFactory.getLogger(AsyncExportController.class);

    @Autowired
    private ExportacionServicio exportacionServicio;

    @Autowired
    private ExportNotificationService notificationService;

    @Autowired
    private ExportCacheService cacheService;

    /**
     * Inicia una exportación asíncrona grande
     */
    @PostMapping("/iniciar/{tipo}/{formato}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
    public ResponseEntity<Map<String, Object>> iniciarExportacionAsync(
            @PathVariable String tipo,
            @PathVariable String formato,
            @RequestBody ExportConfigDTO config,
            Authentication authentication) {

        try {
            // Generar ID único para la tarea
            String taskId = UUID.randomUUID().toString();
            String usuario = authentication.getName();

            logger.info("Iniciando exportación asíncrona: {} - {} - {}", tipo, formato, taskId);

            config.setTipo(tipo);
            config.setFormato(formato);
            config.setUsuarioSolicitante(usuario);

            // Iniciar procesamiento asíncrono
            procesarExportacionAsync(config, taskId, usuario);

            // Respuesta inmediata con ID de tarea
            Map<String, Object> response = Map.of(
                    "taskId", taskId,
                    "estado", "iniciado",
                    "mensaje", "La exportación ha sido iniciada. Recibirá una notificación cuando esté lista.",
                    "estimacion", exportacionServicio.obtenerEstimaciones(tipo, formato,
                            config.getFechaInicio(), config.getFechaFin())
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error iniciando exportación asíncrona: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error iniciando exportación: " + e.getMessage()));
        }
    }

    /**
     * Consulta el estado de una exportación asíncrona
     */
    @GetMapping("/estado/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
    public ResponseEntity<Map<String, Object>> consultarEstadoExportacion(@PathVariable String taskId) {

        try {
            Object estado = cacheService.recuperarDatosTemporales("export_status_" + taskId);

            if (estado != null) {
                return ResponseEntity.ok((Map<String, Object>) estado);
            } else {
                return ResponseEntity.ok(Map.of(
                        "taskId", taskId,
                        "estado", "no_encontrado",
                        "mensaje", "No se encontró la tarea o ya fue procesada"
                ));
            }

        } catch (Exception e) {
            logger.error("Error consultando estado de exportación: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error consultando estado"));
        }
    }

    /**
     * Procesa la exportación de forma asíncrona
     */
    @Async("exportacionTaskExecutor")
    private CompletableFuture<Void> procesarExportacionAsync(ExportConfigDTO config, String taskId, String usuario) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Actualizar estado a "procesando"
                cacheService.almacenarDatosTemporales("export_status_" + taskId, Map.of(
                        "taskId", taskId,
                        "estado", "procesando",
                        "progreso", 0,
                        "mensaje", "Preparando datos para exportación..."
                ));

                logger.info("Procesando exportación asíncrona: {}", taskId);

                // Simular progreso intermedio
                for (int i = 1; i <= 3; i++) {
                    Thread.sleep(2000); // Simular trabajo
                    cacheService.almacenarDatosTemporales("export_status_" + taskId, Map.of(
                            "taskId", taskId,
                            "estado", "procesando",
                            "progreso", i * 25,
                            "mensaje", "Procesando datos... " + (i * 25) + "%"
                    ));
                }

                // Realizar exportación real
                byte[] archivo;
                switch (config.getTipo().toLowerCase()) {
                    case "usuarios" -> archivo = exportacionServicio.exportarUsuarios(config);
                    case "clientes" -> archivo = exportacionServicio.exportarClientes(config);
                    case "productos" -> archivo = exportacionServicio.exportarProductos(config);
                    case "ventas" -> archivo = exportacionServicio.exportarVentas(config);
                    default -> throw new IllegalArgumentException("Tipo no soportado: " + config.getTipo());
                }

                // Guardar archivo temporalmente (en un sistema real, usar almacenamiento persistente)
                String fileKey = "export_file_" + taskId;
                cacheService.almacenarDatosTemporales(fileKey, archivo);

                // Actualizar estado a "completado"
                cacheService.almacenarDatosTemporales("export_status_" + taskId, Map.of(
                        "taskId", taskId,
                        "estado", "completado",
                        "progreso", 100,
                        "mensaje", "Exportación completada exitosamente",
                        "tamano", archivo.length,
                        "downloadUrl", "/exportacion/async/descargar/" + taskId
                ));

                logger.info("Exportación asíncrona completada: {} - {} bytes", taskId, archivo.length);

                // Enviar notificación por email (si está configurado)
                // notificationService.notificarExportacionCompletada(historial, userEmail);

            } catch (Exception e) {
                logger.error("Error en exportación asíncrona {}: {}", taskId, e.getMessage());

                // Actualizar estado a "error"
                cacheService.almacenarDatosTemporales("export_status_" + taskId, Map.of(
                        "taskId", taskId,
                        "estado", "error",
                        "progreso", 0,
                        "mensaje", "Error en exportación: " + e.getMessage()
                ));

                // Enviar notificación de error
                // notificationService.notificarErrorExportacion(historial, userEmail);
            }
        });
    }

    /**
     * Descarga un archivo de exportación asíncrona
     */
    @GetMapping("/descargar/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
    public ResponseEntity<byte[]> descargarExportacionAsync(@PathVariable String taskId) {

        try {
            Object archivo = cacheService.recuperarDatosTemporales("export_file_" + taskId);

            if (archivo instanceof byte[]) {
                return ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=export_" + taskId + ".dat")
                        .body((byte[]) archivo);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            logger.error("Error descargando exportación asíncrona: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}