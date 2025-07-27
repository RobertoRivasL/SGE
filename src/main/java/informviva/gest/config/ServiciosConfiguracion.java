package informviva.gest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Configuración específica para servicios y dependencias
 * Resuelve problemas de dependencias circulares y configuración de beans
 *
 * @author Roberto Rivas Lopez
 * @version 1.0.0
 */
@Configuration
@EnableTransactionManagement
@EnableCaching
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {
        "informviva.gest.service",
        "informviva.gest.service.impl"
})
public class ServiciosConfiguracion {

    // Esta clase asegura que Spring escanee correctamente todos los servicios
    // y resuelva las dependencias de forma apropiada
}