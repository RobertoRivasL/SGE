package informviva.gest.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuración para habilitar la programación de tareas
 * necesaria para el módulo de respaldo automático
 *
 * @author Roberto Rivas
 * @version 3.0
 */
@Configuration
@EnableScheduling
public class ConfiguracionProgramacionTareas {
    // Esta clase habilita @Scheduled en la aplicación
    // No requiere métodos adicionales
}
