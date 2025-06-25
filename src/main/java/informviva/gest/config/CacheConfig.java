package informviva.gest.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(
                // ===== CACHE EXISTENTES =====
                "productos",
                "clientes",
                "usuarios",
                "configuracion",
                "metricas-dashboard",
                "reportes",

                // ===== NUEVOS CACHE PARA VENTAS =====
                "ventas-recientes",           // Ventas de los últimos 30 días
                "ventas-por-cliente",         // Ventas agrupadas por cliente
                "ventas-por-vendedor",        // Ventas agrupadas por vendedor
                "productos-mas-vendidos",     // Top productos más vendidos
                "estadisticas-ventas",        // Estadísticas generales de ventas
                "resumen-ventas-periodo",     // Resúmenes por período
                "export-estimations",         // Estimaciones de exportación
                "validaciones-venta"          // Cache de validaciones frecuentes
        );
        return cacheManager;
    }
}

