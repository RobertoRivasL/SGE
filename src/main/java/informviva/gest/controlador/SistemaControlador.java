package informviva.gest.controlador;

// =================== CONTROLADOR DE SISTEMA Y BACKUP ===================


import informviva.gest.service.BackupServicio;
import informviva.gest.service.MetricasServicio;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/sistema")
@PreAuthorize("hasRole('ADMIN')")
public class SistemaControlador {

    private final BackupServicio backupServicio;
    private final MetricasServicio metricasServicio;

    public SistemaControlador(BackupServicio backupServicio, MetricasServicio metricasServicio) {
        this.backupServicio = backupServicio;
        this.metricasServicio = metricasServicio;
    }

    @GetMapping
    public String mostrarPanelSistema(Model model) {
        model.addAttribute("metricas", metricasServicio.obtenerMetricasSistema("actual"));
        model.addAttribute("rendimiento", metricasServicio.obtenerEstadisticasRendimiento());
        return "admin/sistema";
    }

    @PostMapping("/backup")
    public String realizarBackupManual(RedirectAttributes redirectAttributes) {
        try {
            backupServicio.realizarBackupManual();
            redirectAttributes.addFlashAttribute("mensajeExito", "Backup realizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al realizar backup: " + e.getMessage());
        }
        return "redirect:/admin/sistema";
    }

    @GetMapping("/api/estado")
    @ResponseBody
    public ResponseEntity<String> verificarEstado() {
        return ResponseEntity.ok("Sistema operativo - " + java.time.LocalDateTime.now());
    }
}
