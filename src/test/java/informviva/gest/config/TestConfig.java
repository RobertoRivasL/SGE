package informviva.gest.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.modelmapper.ModelMapper;

/**
 * Configuración específica para tests
 *
 * Esta clase resuelve los problemas de dependencias en los tests
 * proporcionando beans mock y configuraciones específicas para testing
 *
 * @author Tu nombre
 * @version 1.0
 */
@TestConfiguration
@Profile("test")
@EnableJpaRepositories(basePackages = "informviva.gest.repository")
@EntityScan(basePackages = "informviva.gest.model")
@EnableTransactionManagement
public class TestConfig {

    /**
     * Bean ModelMapper para tests
     *
     * @return ModelMapper configurado para testing
     */
    @Bean
    @Primary
    public ModelMapper testModelMapper() {
        return new ModelMapper();
    }
}
