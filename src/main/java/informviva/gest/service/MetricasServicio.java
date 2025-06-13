package informviva.gest.service;


// =================== SERVICIO DE MÉTRICAS Y MONITOREO ===================

import informviva.gest.dto.MetricasSistemaDTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class MetricasServicio {

    private final VentaServicio ventaServicio;
    private final ProductoServicio productoServicio;
    private final ClienteServicio clienteServicio;

    public MetricasServicio(VentaServicio ventaServicio,
                            ProductoServicio productoServicio,
                            ClienteServicio clienteServicio) {
        this.ventaServicio = ventaServicio;
        this.productoServicio = productoServicio;
        this.clienteServicio = clienteServicio;
    }

    @Cacheable(value = "metricas-dashboard", key = "#periodo", unless = "#result == null")
    public MetricasSistemaDTO obtenerMetricasSistema(String periodo) {
        MetricasSistemaDTO metricas = new MetricasSistemaDTO();

        try {
            // Métricas de memoria
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long memoriaUsada = memoryBean.getHeapMemoryUsage().getUsed();
            long memoriaTotal = memoryBean.getHeapMemoryUsage().getMax();

            metricas.setMemoriaUsada(memoriaUsada / (1024 * 1024)); // MB
            metricas.setMemoriaTotal(memoriaTotal / (1024 * 1024)); // MB
            metricas.setPorcentajeMemoria((double) memoriaUsada / memoriaTotal * 100);

            // Tiempo de actividad
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            long uptime = runtimeBean.getUptime();
            metricas.setTiempoActividad(uptime / 1000); // segundos

            // Métricas de negocio
            metricas.setTotalProductos(productoServicio.contarTodos());
            metricas.setTotalClientes(clienteServicio.obtenerTodos().size());
            metricas.setProductosStockBajo(productoServicio.contarConBajoStock(5));

            // Rendimiento
            metricas.setTiempoRespuestaPromedio(calcularTiempoRespuestaPromedio());
            metricas.setFechaActualizacion(LocalDateTime.now());

        } catch (Exception e) {
            // Log error pero retorna métricas parciales
            metricas.setError("Error al obtener algunas métricas: " + e.getMessage());
        }

        return metricas;
    }

    private double calcularTiempoRespuestaPromedio() {
        // Simulación - en producción se obtendría de logs o métricas reales
        return Math.random() * 200 + 50; // Entre 50-250ms
    }

    public Map<String, Object> obtenerEstadisticasRendimiento() {
        Map<String, Object> stats = new HashMap<>();

        // Métricas JVM
        Runtime runtime = Runtime.getRuntime();
        stats.put("memoriaLibre", runtime.freeMemory() / (1024 * 1024));
        stats.put("memoriaTotal", runtime.totalMemory() / (1024 * 1024));
        stats.put("memoriaMaxima", runtime.maxMemory() / (1024 * 1024));
        stats.put("procesadores", runtime.availableProcessors());

        // Métricas del sistema
        stats.put("timestamp", LocalDateTime.now());
        stats.put("version", "2.0");

        return stats;
    }
}