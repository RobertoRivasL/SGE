package informviva.gest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
// Eliminamos la referencia a application-test.properties que no existe
class GestApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Verifica que la aplicación inicia correctamente")
    void speak() {
        // Aserciones para validar que el contexto se ha cargado correctamente
        assertNotNull(applicationContext, "El contexto de Spring debería cargarse correctamente");

        // Verificamos que la clase principal de la aplicación está disponible
        assertTrue(applicationContext.containsBeanDefinition("gestApplication") ||
                        applicationContext.containsBean("gestApplication"),
                "La aplicación principal debería estar disponible en el contexto");
    }
}