package informviva.gest.controlador.api;

//=================== HEALTH CHECK CONTROLLER ===================
//        package informviva.gest.controlador.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthCheckController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("version", "2.0");

        // Verificaciones adicionales
        health.put("database", verificarBaseDatos());
        health.put("memoria", verificarMemoria());

        return ResponseEntity.ok(health);
    }

    @GetMapping("/ready")
    public ResponseEntity<String> readinessCheck() {
        // Verificaciones para Kubernetes readiness probe
        return ResponseEntity.ok("READY");
    }

    @GetMapping("/live")
    public ResponseEntity<String> livenessCheck() {
        // Verificaciones para Kubernetes liveness probe
        return ResponseEntity.ok("ALIVE");
    }

    private String verificarBaseDatos() {
        try {
            // Aquí harías una consulta simple a la base de datos
            return "OK";
        } catch (Exception e) {
            return "ERROR";
        }
    }

    private String verificarMemoria() {
        Runtime runtime = Runtime.getRuntime();
        long memoriaUsada = runtime.totalMemory() - runtime.freeMemory();
        long memoriaTotal = runtime.totalMemory();
        double porcentajeUso = (double) memoriaUsada / memoriaTotal * 100;

        return porcentajeUso < 85 ? "OK" : "HIGH";
    }
}