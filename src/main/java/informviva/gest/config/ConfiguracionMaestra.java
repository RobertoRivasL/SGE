package informviva.gest.config;

import informviva.gest.model.Usuario;
import informviva.gest.repository.RepositorioUsuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * Configuración Maestra Unificada para InformViva Gestión
// *
 * Aplica principios SOLID:
 * - Responsabilidad Única: Centraliza toda la configuración de Spring
 * - Principio Abierto/Cerrado: Extensible sin modificar la estructura base
 * - Inversión de Dependencias: Usa abstracciones e inyección de dependencias
 * - Separación de Intereses: Organizada por módulos lógicos
 *
 * @author Roberto Rivas Lopez
 * @version 1.0
 */
@Configuration
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
@EntityScan(basePackages = "informviva.gest.model")
@EnableJpaRepositories(basePackages = "informviva.gest.repository")
@ComponentScan(basePackages = {
        "informviva.gest.service",
        "informviva.gest.service.impl",
        "informviva.gest.controlador",
        "informviva.gest.config",
        "informviva.gest.repository",
        "informviva.gest.dto",
        "informviva.gest.exception",
        "informviva.gest.util",
        "informviva.gest.validador",
        "informviva.gest.seguridad"
})
public class ConfiguracionMaestra {

    private static final Logger logger = LoggerFactory.getLogger(ConfiguracionMaestra.class);

    // =================== SEGURIDAD Y ENCRIPTACIÓN ===================

    /**
     * Bean principal para codificación de contraseñas
     * Principio de Responsabilidad Única: Una sola implementación
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Bean específico BCryptPasswordEncoder para compatibilidad con código existente
     * Principio de Inversión de Dependencias: Mantiene la interfaz conocida
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    // =================== CONFIGURACIÓN DE ARCHIVOS ===================

    /**
     * Configuración para carga de archivos multipart
     * Principio de Encapsulación: Configuración específica y aislada
     */
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    // =================== CONFIGURACIÓN DE TAREAS ASÍNCRONAS ===================

    /**
     * Configuración del pool de hilos para tareas generales
     * Principio de Separación de Intereses: Tareas generales vs específicas
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("InformViva-Task-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        return scheduler;
    }

    /**
     * Configuración del pool de hilos para exportaciones
     * Principio de Responsabilidad Única: Pool específico para exportaciones
     */
    @Bean(name = "exportacionTaskExecutor")
    public Executor exportacionTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Export-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * Configuración del pool de hilos para importaciones
     * Principio de Separación de Intereses: Pool específico para importaciones
     */
    @Bean(name = "importacionTaskExecutor")
    public Executor importacionTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("Import-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    // =================== INICIALIZACIÓN DE DATOS ===================

    /**
     * Inicializador de usuarios por defecto
     * Principio de Inversión de Dependencias: Depende de abstracciones
     */
    @Bean
    CommandLineRunner inicializarUsuarios(RepositorioUsuario repositorioUsuario,
                                          BCryptPasswordEncoder codificador) {
        return args -> {
            if (!repositorioUsuario.findByUsername("admin").isPresent()) {
                Usuario admin = new Usuario("admin", codificador.encode("admin123"));
                admin.setNombre("Administrador");
                admin.setApellido("General");
                admin.setEmail("admin@informviva.com");

                Set<String> roles = new HashSet<>();
                roles.add("ADMIN");
                admin.setRoles(roles);
                admin.setActivo(true);

                repositorioUsuario.save(admin);
                logger.info("✅ Usuario administrador creado: admin/admin123");
            } else {
                logger.info("ℹ️  El usuario administrador ya existe");
            }
        };
    }
}