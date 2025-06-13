package informviva.gest.service;

// =================== SERVICIO DE CACHE OPTIMIZADO ===================

import informviva.gest.dto.*;
import informviva.gest.model.*;
import org.springframework.cache.annotation.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CacheServicio {

    @Cacheable(value = "productos", key = "#activos ? 'activos' : 'todos'")
    public List<Producto> obtenerProductosCache(boolean activos) {
        // Este método será implementado por los servicios que lo usen
        return null;
    }

    @CacheEvict(value = "productos", allEntries = true)
    public void limpiarCacheProductos() {
        // Limpia todo el cache de productos
    }

    @Cacheable(value = "metricas-dashboard", key = "#periodo")
    public MetricaDashboardDTO obtenerMetricasDashboard(String periodo) {
        // Cachea las métricas del dashboard por período
        return null;
    }

    @CacheEvict(value = {"productos", "ventas-recientes", "metricas-dashboard"}, allEntries = true)
    @Scheduled(fixedRate = 300000) // Cada 5 minutos
    public void limpiarCachesPeriodicamente() {
        // Limpia caches periódicamente para mantener datos frescos
    }

    @Cacheable(value = "configuracion")
    public ConfiguracionSistema obtenerConfiguracionCache() {
        return null;
    }

    @CacheEvict(value = "configuracion", allEntries = true)
    public void limpiarCacheConfiguracion() {
        // Se ejecuta cuando se actualiza la configuración
    }
}

