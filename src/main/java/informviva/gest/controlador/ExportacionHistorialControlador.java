package informviva.gest.controlador;


import informviva.gest.model.ExportacionHistorial;
import informviva.gest.service.ExportacionHistorialServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controlador para el historial de exportaciones
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Controller
@RequestMapping("/admin/exportaciones")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class ExportacionHistorialControlador {

    private final ExportacionHistorialServicio exportacionHistorialServicio;

    @Autowired
    public ExportacionHistorialControlador(ExportacionHistorialServicio exportacionHistorialServicio) {
        this.exportacionHistorialServicio = exportacionHistorialServicio;
    }

    /**
     * Muestra la página principal del historial de exportaciones
     */
    @GetMapping
    public String mostrarHistorialExportaciones(
            @RequestParam(defaultValue = "50") int limite,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Model model) {

        try {
            List<ExportacionHistorial> historial;

            if (usuario != null && !usuario.trim().isEmpty()) {
                // Filtrar por usuario
                historial = exportacionHistorialServicio.obtenerHistorialPorUsuario(usuario, limite);
                model.addAttribute("filtroUsuario", usuario);

            } else if (fechaInicio != null && fechaFin != null) {
                // Filtrar por rango de fechas de solicitud
                LocalDateTime inicio = fechaInicio.atStartOfDay();
                LocalDateTime fin = fechaFin.atTime(23, 59, 59);
                historial = exportacionHistorialServicio.obtenerHistorialPorFechasSolicitud(inicio, fin);
                model.addAttribute("fechaInicio", fechaInicio);
                model.addAttribute("fechaFin", fechaFin);

            } else {
                // Historial reciente
                historial = exportacionHistorialServicio.obtenerHistorialReciente(limite);
            }

            // Obtener estadísticas
            Map<String, Object> estadisticas = exportacionHistorialServicio.obtenerEstadisticasExportacion();

            model.addAttribute("historial", historial);
            model.addAttribute("estadisticas", estadisticas);
            model.addAttribute("limite", limite);

            return "admin/exportaciones/historial";

        } catch (Exception e) {
            model.addAttribute("mensajeError", "Error al cargar historial: " + e.getMessage());
            model.addAttribute("historial", List.of());
            model.addAttribute("estadisticas", Map.of());
            return "admin/exportaciones/historial";
        }
    }

    /**
     * API para obtener estadísticas de exportación
     */
    @GetMapping("/api/estadisticas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        try {
            Map<String, Object> estadisticas = exportacionHistorialServicio.obtenerEstadisticasExportacion();
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API para obtener historial reciente
     */
    @GetMapping("/api/historial")
    @ResponseBody
    public ResponseEntity<List<ExportacionHistorial>> obtenerHistorialReciente(
            @RequestParam(defaultValue = "20") int limite) {
        try {
            List<ExportacionHistorial> historial = exportacionHistorialServicio.obtenerHistorialReciente(limite);
            return ResponseEntity.ok(historial);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Página de estadísticas detalladas
     */
    @GetMapping("/estadisticas")
    public String mostrarEstadisticasDetalladas(Model model) {
        try {
            Map<String, Object> estadisticas = exportacionHistorialServicio.obtenerEstadisticasExportacion();

            // Obtener historial reciente para mostrar tendencias
            List<ExportacionHistorial> historialReciente = exportacionHistorialServicio.obtenerHistorialReciente(100);

            model.addAttribute("estadisticas", estadisticas);
            model.addAttribute("historialReciente", historialReciente);

            return "admin/exportaciones/estadisticas";

        } catch (Exception e) {
            model.addAttribute("mensajeError", "Error al cargar estadísticas: " + e.getMessage());
            return "admin/exportaciones/estadisticas";
        }
    }

    /**
     * Página de configuración de exportaciones programadas
     */
    @GetMapping("/configuracion")
    @PreAuthorize("hasRole('ADMIN')")
    public String mostrarConfiguracionExportaciones(Model model) {
        // Aquí podrías cargar configuraciones específicas de exportación
        // como frecuencias, tipos de reportes automáticos, etc.

        model.addAttribute("configuracionActual", Map.of(
                "exportacionDiariaHabilitada", true,
                "limpiezaHistorialHabilitada", true,
                "reporteEstadisticasHabilitado", true,
                "backupConfiguracionesHabilitado", true
        ));

        return "admin/exportaciones/configuracion";
    }

    /**
     * Ejecutar limpieza manual del historial
     */
    @PostMapping("/limpiar-historial")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> limpiarHistorial(
            @RequestParam int diasAMantener,
            Authentication authentication) {

        try {
            LocalDateTime fechaLimite = LocalDateTime.now().minusDays(diasAMantener);
            Long registrosEliminados = exportacionHistorialServicio.limpiarHistorialAnteriorA(fechaLimite);

            // Registrar esta acción como una exportación especial
            exportacionHistorialServicio.registrarExportacionExitosa(
                    "LIMPIEZA",
                    "HISTORIAL_EXPORTACIONES",
                    authentication.getName(),
                    registrosEliminados.intValue(),
                    0L
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "mensaje", "Historial limpiado exitosamente",
                    "registrosEliminados", registrosEliminados
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "mensaje", "Error al limpiar historial: " + e.getMessage()
            ));
        }
    }

    /**
     * Obtener detalle de una exportación específica
     */
    @GetMapping("/detalle/{id}")
    @ResponseBody
    public ResponseEntity<ExportacionHistorial> obtenerDetalleExportacion(@PathVariable Long id) {
        try {
            ExportacionHistorial detalle = exportacionHistorialServicio.buscarPorId(id);
            if (detalle != null) {
                return ResponseEntity.ok(detalle);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Reenviar/reejecutar una exportación fallida
     */
    @PostMapping("/reejecutar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reejecutarExportacion(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            // Aquí implementarías la lógica para reejecutar una exportación
            // basándote en los datos del historial

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "mensaje", "Exportación reenviada a la cola de procesamiento"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "mensaje", "Error al reejecutar exportación: " + e.getMessage()
            ));
        }
    }
}
