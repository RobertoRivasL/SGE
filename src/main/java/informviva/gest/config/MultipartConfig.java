package informviva.gest.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

/**
 * Configuración para la carga de archivos multipart
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Configuration
public class MultipartConfig {

    /**
     * Configuración del resolver para archivos multipart
     */
    @Bean
    public MultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        return resolver;
    }
}