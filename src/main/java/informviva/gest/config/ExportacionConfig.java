// ===================================================================
// CONFIGURACIÓN AVANZADA - ExportacionConfig.java
// ===================================================================

package informviva.gest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración avanzada para el módulo de exportación
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Configuration
public class ExportacionConfig {

    @Value("${exportacion.async.core-pool-size:2}")
    private int corePoolSize;

    @Value("${exportacion.async.max-pool-size:5}")
    private int maxPoolSize;

    @Value("${exportacion.async.queue-capacity:100}")
    private int queueCapacity;

    /**
     * Configuración del pool de hilos para exportaciones asíncronas
     */
    @Bean(name = "exportacionTaskExecutor")
    public Executor exportacionTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("Export-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}