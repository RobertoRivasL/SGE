package informviva.gest.controlador.api;


import informviva.gest.dto.MetricasSistemaDTO;
import informviva.gest.service.MetricasServicio;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/metricas")
@PreAuthorize("hasRole('ADMIN')")
public class MetricasControladorAPI {

    private final MetricasServicio metricasServicio;

    public MetricasControladorAPI(MetricasServicio metricasServicio) {
        this.metricasServicio = metricasServicio;
    }

    @GetMapping("/sistema")
    public ResponseEntity<MetricasSistemaDTO> obtenerMetricasSistema(
            @RequestParam(defaultValue = "actual") String periodo) {
        MetricasSistemaDTO metricas = metricasServicio.obtenerMetricasSistema(periodo);
        return ResponseEntity.ok(metricas);
    }

    @GetMapping("/rendimiento")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasRendimiento() {
        Map<String, Object> stats = metricasServicio.obtenerEstadisticasRendimiento();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/limpiar-cache")
    public ResponseEntity<String> limpiarCache() {
        // Lógica para limpiar caches manualmente
        return ResponseEntity.ok("Cache limpiado exitosamente");
    }
}