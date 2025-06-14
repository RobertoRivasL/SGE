package informviva.gest.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración para procesamiento asíncrono de importaciones
 *
 * @author Roberto Rivas
 * @version 2.1
 */
@Configuration
@EnableAsync
public class ImportacionAsyncConfig {

    /**
     * Configuración del pool de threads para importaciones
     */
    @Bean(name = "importacionTaskExecutor")
    public Executor importacionTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Configuración del pool
        executor.setCorePoolSize(2);           // Threads mínimos
        executor.setMaxPoolSize(5);            // Threads máximos
        executor.setQueueCapacity(10);         // Cola de espera
        executor.setKeepAliveSeconds(60);      // Tiempo de vida de threads inactivos

        // Nombres para debugging
        executor.setThreadNamePrefix("ImportacionAsync-");
        executor.setThreadGroupName("ImportacionGroup");

        // Política de rechazo: ejecutar en el thread que llama
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        // Esperar que terminen las tareas al cerrar
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }

    /**
     * Configuración adicional para monitoreo de performance
     */
    @Bean
    public ImportacionMetrics importacionMetrics() {
        return new ImportacionMetrics();
    }

    /**
     * Clase para recopilar métricas de importación
     */
    public static class ImportacionMetrics {
        private volatile long totalImportaciones = 0;
        private volatile long importacionesExitosas = 0;
        private volatile long importacionesConErrores = 0;
        private volatile long tiempoPromedioMs = 0;

        public void registrarImportacion(boolean exitosa, long tiempoMs) {
            totalImportaciones++;
            if (exitosa) {
                importacionesExitosas++;
            } else {
                importacionesConErrores++;
            }

            // Calcular promedio móvil simple
            tiempoPromedioMs = (tiempoPromedioMs + tiempoMs) / 2;
        }

        // Getters
        public long getTotalImportaciones() { return totalImportaciones; }
        public long getImportacionesExitosas() { return importacionesExitosas; }
        public long getImportacionesConErrores() { return importacionesConErrores; }
        public long getTiempoPromedioMs() { return tiempoPromedioMs; }

        public double getTasaExito() {
            return totalImportaciones > 0 ?
                    (double) importacionesExitosas / totalImportaciones * 100.0 : 0.0;
        }
    }
}