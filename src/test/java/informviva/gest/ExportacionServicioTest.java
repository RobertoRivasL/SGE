package informviva.gest;

// ===================================================================
// TESTS UNITARIOS - ExportacionServicioTest.java
// ===================================================================

import informviva.gest.dto.ExportConfigDTO;
import informviva.gest.model.Usuario;
import informviva.gest.model.Producto;
import informviva.gest.service.impl.GeneradorArchivosServicio;
import informviva.gest.service.impl.ExportacionHistorialServicio;
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

@ExtendWith(MockitoExtension.class)
class ExportacionServicioTest {

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

    @InjectMocks
    private ExportacionServicio exportacionServicio;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.isAuthenticated()).thenReturn(true);
    }

    @Test
    void testExportarUsuariosExcel() {
        // Arrange
        ExportConfigDTO config = new ExportConfigDTO();
        config.setTipo("usuarios");
        config.setFormato("excel");
        config.addFiltro("soloActivos", true);

        Usuario usuario1 = new Usuario();
        usuario1.setId(1L);
        usuario1.setUsername("admin");
        usuario1.setNombre("Administrador");
        usuario1.setActivo(true);
        usuario1.setRoles(Set.of("ADMIN"));

        Usuario usuario2 = new Usuario();
        usuario2.setId(2L);
        usuario2.setUsername("vendedor");
        usuario2.setNombre("Vendedor");
        usuario2.setActivo(true);
        usuario2.setRoles(Set.of("VENTAS"));

        List<Usuario> usuarios = Arrays.asList(usuario1, usuario2);
        byte[] archivoEsperado = "contenido excel".getBytes();

        when(usuarioServicio.listarTodos()).thenReturn(usuarios);
        when(generadorArchivosServicio.generarUsuariosExcel(anyList())).thenReturn(archivoEsperado);
        when(historialServicio.registrarExportacion(anyString(), anyString(), anyString(),
                anyLong(), anyString(), any(), any()))
                .thenReturn(new informviva.gest.model.ExportacionHistorial());

        // Act
        byte[] resultado = exportacionServicio.exportarUsuarios(config);

        // Assert
        assertNotNull(resultado);
        assertEquals(archivoEsperado.length, resultado.length);
        verify(usuarioServicio).listarTodos();
        verify(generadorArchivosServicio).generarUsuariosExcel(usuarios);
        verify(historialServicio).registrarExportacion(eq("usuarios"), eq("excel"),
                eq("testuser"), eq(0L), eq("Procesando"), any(), any());
    }

    @Test
    void testExportarProductosConFiltros() {
        // Arrange
        ExportConfigDTO config = new ExportConfigDTO();
        config.setTipo("productos");
        config.setFormato("csv");
        config.addFiltro("soloActivos", true);
        config.addFiltro("categoria", "Electrónicos");

        Producto producto1 = new Producto();
        producto1.setId(1L);
        producto1.setCodigo("PROD001");
        producto1.setNombre("Laptop");
        producto1.setActivo(true);

        List<Producto> productos = Arrays.asList(producto1);
        byte[] archivoEsperado = "contenido csv".getBytes();

        when(productoServicio.listarActivos()).thenReturn(productos);
        when(generadorArchivosServicio.generarProductosCSV(anyList())).thenReturn(archivoEsperado);
        when(historialServicio.registrarExportacion(anyString(), anyString(), anyString(),
                anyLong(), anyString(), any(), any()))
                .thenReturn(new informviva.gest.model.ExportacionHistorial());

        // Act
        byte[] resultado = exportacionServicio.exportarProductos(config);

        // Assert
        assertNotNull(resultado);
        assertEquals(archivoEsperado.length, resultado.length);
        verify(productoServicio).listarActivos();
        verify(generadorArchivosServicio).generarProductosCSV(productos);
    }

    @Test
    void testObtenerEstimaciones() {
        // Arrange
        when(usuarioServicio.listarTodos()).thenReturn(Arrays.asList(new Usuario(), new Usuario()));

        // Act
        var estimaciones = exportacionServicio.obtenerEstimaciones("usuarios", "excel", null, null);

        // Assert
        assertNotNull(estimaciones);
        assertTrue(estimaciones.containsKey("registros"));
        assertTrue(estimaciones.containsKey("tamañoEstimado"));
        assertTrue(estimaciones.containsKey("tiempoEstimado"));
        assertEquals(2L, estimaciones.get("registros"));
    }

    @Test
    void testFormatoNoSoportado() {
        // Arrange
        ExportConfigDTO config = new ExportConfigDTO();
        config.setTipo("usuarios");
        config.setFormato("xml"); // Formato no soportado

        when(usuarioServicio.listarTodos()).thenReturn(Arrays.asList(new Usuario()));
        when(historialServicio.registrarExportacion(anyString(), anyString(), anyString(),
                anyLong(), anyString(), any(), any()))
                .thenReturn(new informviva.gest.model.ExportacionHistorial());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            exportacionServicio.exportarUsuarios(config);
        });

        assertTrue(exception.getMessage().contains("Error en exportación de usuarios"));
    }
}