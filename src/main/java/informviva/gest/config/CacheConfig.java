// =================== CONFIGURACIÓN DE CACHE ===================
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
                "productos",
                "clientes",
                "usuarios",
                "ventas-recientes",
                "reportes",
                "configuracion",
                "metricas-dashboard"
        );
        return cacheManager;
    }
}

