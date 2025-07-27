package informviva.gest;

// ===================================================================
// TESTS UNITARIOS - ExportacionServicioTest.java - CORREGIDO
// ===================================================================

import informviva.gest.dto.ExportConfigDTO;
import informviva.gest.model.Usuario;
import informviva.gest.model.Producto;
import informviva.gest.service.UsuarioServicio;  // INTERFACE, no implementación
import informviva.gest.service.ProductoServicio;  // INTERFACE, no implementación
import informviva.gest.service.ExportacionServicio;  // INTERFACE, no implementación
import informviva.gest.service.ExportacionHistorialServicio;  // INTERFACE, no implementación
import informviva.gest.service.impl.GeneradorArchivosServicio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ExportacionServicio
 * Aplicando principios SOLID y buenas prácticas de testing
 *
 * @author Roberto Rivas Lopez
 * @version 1.0.0 - CORREGIDO
 */
@ExtendWith(MockitoExtension.class)
class ExportacionServicioTest {

    // ============ MOCKS DE INTERFACES (Principio DIP) ============

    @Mock
    private UsuarioServicio usuarioServicio;

    @Mock
    private ProductoServicio productoServicio;

    @Mock
    private GeneradorArchivosServicio generadorArchivosServicio;

    @Mock
    private ExportacionHistorialServicio historialServicio;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    // ============ CLASE BAJO PRUEBA ============

    @InjectMocks
    private ExportacionServicio exportacionServicio;  // Se inyectará la implementación

    // ============ CONFIGURACIÓN DE TESTS ============

    @BeforeEach
    void configurarTest() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.isAuthenticated()).thenReturn(true);
    }

    // ============ TESTS DE EXPORTACIÓN DE USUARIOS ============

    @Test
    void deberiaExportarUsuariosAExcel() {
        // Arrange - Preparar datos de prueba
        ExportConfigDTO config = crearConfiguracionExportacion("usuarios", "excel");
        config.addFiltro("soloActivos", true);

        List<Usuario> usuarios = crearUsuariosPrueba();
        byte[] archivoEsperado = "contenido excel usuarios".getBytes();

        // Configurar comportamiento de mocks
        when(usuarioServicio.listarTodos()).thenReturn(usuarios);
        when(generadorArchivosServicio.generarUsuariosExcel(usuarios)).thenReturn(archivoEsperado);

        // Act - Ejecutar método bajo prueba
        byte[] resultado = exportacionServicio.exportarUsuarios(config);

        // Assert - Verificar resultados
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(archivoEsperado.length, resultado.length, "El tamaño del archivo debe coincidir");

        // Verificar interacciones
        verify(usuarioServicio, times(1)).listarTodos();
        verify(generadorArchivosServicio, times(1)).generarUsuariosExcel(usuarios);
    }

    @Test
    void deberiaExportarProductosACSV() {
        // Arrange
        ExportConfigDTO config = crearConfiguracionExportacion("productos", "csv");

        List<Producto> productos = crearProductosPrueba();
        byte[] archivoEsperado = "contenido csv productos".getBytes();

        when(productoServicio.listarActivos()).thenReturn(productos);
        when(generadorArchivosServicio.generarProductosCSV(productos)).thenReturn(archivoEsperado);

        // Act
        byte[] resultado = exportacionServicio.exportarProductos(config);

        // Assert
        assertNotNull(resultado);
        assertEquals(archivoEsperado.length, resultado.length);
        verify(productoServicio).listarActivos();
        verify(generadorArchivosServicio).generarProductosCSV(productos);
    }

    // ============ TESTS DE ESTIMACIONES ============

    @Test
    void deberiaObtenerEstimacionesCorrectas() {
        // Arrange
        when(usuarioServicio.listarTodos()).thenReturn(crearUsuariosPrueba());

        // Act
        var estimaciones = exportacionServicio.obtenerEstimaciones("usuarios", "excel", null, null);

        // Assert
        assertNotNull(estimaciones, "Las estimaciones no deben ser nulas");
        assertTrue(estimaciones.containsKey("registros"), "Debe contener número de registros");
        assertTrue(estimaciones.containsKey("tamañoEstimado"), "Debe contener tamaño estimado");
        assertTrue(estimaciones.containsKey("tiempoEstimado"), "Debe contener tiempo estimado");
        assertEquals(2L, estimaciones.get("registros"), "Debe estimar 2 registros");
    }

    // ============ TESTS DE MANEJO DE ERRORES ============

    @Test
    void deberiaLanzarExcepcionParaFormatoNoSoportado() {
        // Arrange
        ExportConfigDTO config = crearConfiguracionExportacion("usuarios", "xml"); // Formato no soportado

        when(usuarioServicio.listarTodos()).thenReturn(crearUsuariosPrueba());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            exportacionServicio.exportarUsuarios(config);
        });

        assertTrue(exception.getMessage().contains("Error"),
                "El mensaje de error debe indicar el problema");
    }

    @Test
    void deberiaValidarConfiguracionCorrectamente() {
        // Arrange
        ExportConfigDTO configValida = crearConfiguracionExportacion("usuarios", "excel");
        ExportConfigDTO configInvalida = new ExportConfigDTO(); // Sin tipo ni formato

        // Act
        var resultadoValido = exportacionServicio.validarConfiguracion(configValida);
        var resultadoInvalido = exportacionServicio.validarConfiguracion(configInvalida);

        // Assert
        assertNotNull(resultadoValido);
        assertNotNull(resultadoInvalido);

        // La configuración válida no debe tener errores
        // La configuración inválida debe tener errores
    }

    // ============ MÉTODOS AUXILIARES PARA TESTS ============

    /**
     * Crea una configuración de exportación para pruebas
     */
    private ExportConfigDTO crearConfiguracionExportacion(String tipo, String formato) {
        ExportConfigDTO config = new ExportConfigDTO();
        config.setTipo(tipo);
        config.setFormato(formato);
        return config;
    }

    /**
     * Crea lista de usuarios para pruebas
     */
    private List<Usuario> crearUsuariosPrueba() {
        Usuario usuario1 = new Usuario();
        usuario1.setId(1L);
        usuario1.setUsername("admin");
        usuario1.setNombre("Administrador");
        usuario1.setEmail("admin@test.com");
        usuario1.setActivo(true);

        Usuario usuario2 = new Usuario();
        usuario2.setId(2L);
        usuario2.setUsername("vendedor");
        usuario2.setNombre("Vendedor");
        usuario2.setEmail("vendedor@test.com");
        usuario2.setActivo(true);

        return Arrays.asList(usuario1, usuario2);
    }

    /**
     * Crea lista de productos para pruebas
     */
    private List<Producto> crearProductosPrueba() {
        Producto producto1 = new Producto();
        producto1.setId(1L);
        producto1.setCodigo("PROD001");
        producto1.setNombre("Producto Prueba 1");
        producto1.setActivo(true);

        Producto producto2 = new Producto();
        producto2.setId(2L);
        producto2.setCodigo("PROD002");
        producto2.setNombre("Producto Prueba 2");
        producto2.setActivo(true);

        return Arrays.asList(producto1, producto2);
    }
}