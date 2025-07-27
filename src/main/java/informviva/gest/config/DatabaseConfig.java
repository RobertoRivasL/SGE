package informviva.gest.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

/**
 * Configuración de Base de Datos y Componentes de Persistencia
 *
 * Esta clase centraliza la configuración de:
 * - Repositorios JPA
 * - Entidades
 * - Transacciones
 * - ModelMapper para DTOs
 *
 * @author Tu nombre
 * @version 1.0
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "informviva.gest.repository",
        enableDefaultTransactions = true
)
@EntityScan(basePackages = "informviva.gest.model")
@EnableTransactionManagement
public class DatabaseConfig {

    /**
     * Configuración del ModelMapper para conversión entre entidades y DTOs
     *
     * @return ModelMapper configurado con estrategia estricta
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        // Configuración estricta para evitar mapeos ambiguos
        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        return mapper;
    }
}