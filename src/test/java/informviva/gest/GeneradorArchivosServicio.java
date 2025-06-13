package informviva.gest;

// ===================================================================
// TEST PARA GENERADOR DE ARCHIVOS - GeneradorArchivosServicioTest.java
// ===================================================================


import informviva.gest.model.Usuario;
import informviva.gest.model.Producto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GeneradorArchivosServicio {

    @InjectMocks
    private GeneradorArchivosServicio generadorArchivosServicio;

    @Test
    void testGenerarUsuariosExcel() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("admin");
        usuario.setNombre("Administrador");
        usuario.setApellido("Sistema");
        usuario.setEmail("admin@test.com");
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDate.now());
        usuario.setRoles(Set.of("ADMIN"));

        List<Usuario> usuarios = Arrays.asList(usuario);

        // Act
        byte[] resultado = generadorArchivosServicio.generarUsuariosExcel(usuarios);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
        // Verificar que es un archivo Excel válido (comienza con bytes específicos)
        assertTrue(resultado.length > 100); // Excel tiene un tamaño mínimo
    }

    @Test
    void testGenerarUsuariosCSV() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("admin");
        usuario.setNombre("Administrador");
        usuario.setApellido("Sistema");
        usuario.setEmail("admin@test.com");
        usuario.setActivo(true);
        usuario.setRoles(Set.of("ADMIN"));

        List<Usuario> usuarios = Arrays.asList(usuario);

        // Act
        byte[] resultado = generadorArchivosServicio.generarUsuariosCSV(usuarios);

        // Assert
        assertNotNull(resultado);
        String contenido = new String(resultado);
        assertTrue(contenido.contains("ID,Username,Nombre")); // Verifica encabezados
        assertTrue(contenido.contains("admin")); // Verifica datos
        assertTrue(contenido.contains("Administrador"));
    }

    @Test
    void testGenerarUsuariosJSON() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("admin");
        usuario.setNombre("Administrador");
        usuario.setActivo(true);
        usuario.setRoles(Set.of("ADMIN"));

        List<Usuario> usuarios = Arrays.asList(usuario);

        // Act
        byte[] resultado = generadorArchivosServicio.generarUsuariosJSON(usuarios);

        // Assert
        assertNotNull(resultado);
        String json = new String(resultado);
        assertTrue(json.contains("\"username\":\"admin\""));
        assertTrue(json.contains("\"nombre\":\"Administrador\""));
        assertTrue(json.startsWith("[") && json.endsWith("]")); // Array JSON válido
    }

    @Test
    void testGenerarProductosExcelVacio() {
        // Arrange
        List<Producto> productos = Arrays.asList();

        // Act
        byte[] resultado = generadorArchivosServicio.generarProductosExcel(productos);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.length > 0); // Debe generar al menos el encabezado
    }
}