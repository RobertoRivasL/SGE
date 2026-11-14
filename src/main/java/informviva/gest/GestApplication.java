package informviva.gest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación Spring Boot
 * Sistema Integrado de Gestión de Ventas y Productos - InformViva
// *
 * Aplica principios de Clean Architecture y SOLID:
 * - Modularidad: Configuración centralizada y modular
 * - Separación de Responsabilidades: Cada capa tiene su propósito específico
 * - Abstracción: Uso de interfaces y abstracciones de Spring
 * - Encapsulación: Configuración encapsulada en anotaciones
 * - Inversión de Dependencias: Inyección de dependencias de Spring
 *
 * @author Roberto Rivas Lopez
 * @version 3.0
 */
@SpringBootApplication(scanBasePackages = "informviva.gest")
public class GestApplication {

    private static final Logger logger = LoggerFactory.getLogger(GestApplication.class);

    public static void main(String[] args) {
        try {
            logger.info("=".repeat(70));
            logger.info("🚀 INICIANDO SISTEMA INFORMVIVA GESTIÓN DE VENTAS");
            logger.info("=".repeat(70));

            // Configuración inicial del sistema
            System.setProperty("spring.jpa.open-in-view", "false");
            System.setProperty("server.servlet.encoding.charset", "UTF-8");
            System.setProperty("spring.messages.encoding", "UTF-8");
            System.setProperty("file.encoding", "UTF-8");

            SpringApplication aplicacion = new SpringApplication(GestApplication.class);

            // Configuraciones adicionales de la aplicación
            aplicacion.setDefaultProperties(java.util.Map.of(
                    "spring.profiles.active", "development",
                    "logging.level.org.springframework.web", "INFO",
                    "spring.jpa.show-sql", "false",
                    "spring.jpa.hibernate.ddl-auto", "update"
            ));

            aplicacion.run(args);

            logger.info("✅ APLICACIÓN INICIADA CORRECTAMENTE");
            logger.info("🌐 Aplicación disponible en: http://localhost:8080");
            logger.info("📊 Sistema de Gestión de Ventas y Productos");
            logger.info("🔧 Autor: Roberto Rivas Lopez");
            logger.info("=".repeat(70));

        } catch (Exception e) {
            logger.error("❌ ERROR AL INICIAR LA APLICACIÓN: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}