package informviva.gest;

import informviva.gest.service.ExportacionServicio;
import informviva.gest.dto.ExportConfigDTO;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

// CORRECCIÓN: Import correcto para AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests de integración para ExportacionControlador
 * Corregidos los imports y dependencias
 *
 * @author Roberto Rivas Lopez
 * @version 1.0.0 - CORREGIDO
 */
@WebMvcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ExportacionControladorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExportacionServicio exportacionServicio;

    @Test
    @WithMockUser(username = "testuser", roles = "ADMIN")
    void deberiaExportarUsuariosExitosamente() throws Exception {
        // Arrange
        byte[] archivoMock = "contenido del archivo".getBytes();
        when(exportacionServicio.exportarUsuarios(any(ExportConfigDTO.class)))
                .thenReturn(archivoMock);

        // Act & Assert
        mockMvc.perform(post("/exportar/usuarios")
                        .param("formato", "excel")
                        .param("soloActivos", "true"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        verify(exportacionServicio, times(1)).exportarUsuarios(any(ExportConfigDTO.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void deberiaExportarProductosEnCSV() throws Exception {
        // Arrange
        byte[] archivoMock = "codigo,nombre,precio\nPROD001,Producto 1,100.00".getBytes();
        when(exportacionServicio.exportarProductos(any(ExportConfigDTO.class)))
                .thenReturn(archivoMock);

        // Act & Assert
        mockMvc.perform(post("/exportar/productos")
                        .param("formato", "csv")
                        .param("incluirInactivos", "false"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"));

        verify(exportacionServicio, times(1)).exportarProductos(any(ExportConfigDTO.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "ADMIN")
    void deberiaObtenerEstimacionesCorrectamente() throws Exception {
        // Arrange
        java.util.Map<String, Object> estimaciones = java.util.Map.of(
                "registros", 100L,
                "tamañoEstimado", 50000L,
                "tiempoEstimado", 5L
        );

        when(exportacionServicio.obtenerEstimaciones(anyString(), anyString(), any(), any()))
                .thenReturn(estimaciones);

        // Act & Assert
        mockMvc.perform(get("/exportar/estimaciones")
                        .param("tipo", "clientes")
                        .param("formato", "excel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registros").value(100))
                .andExpect(jsonPath("$.tamañoEstimado").value(50000))
                .andExpect(jsonPath("$.tiempoEstimado").value(5));
    }

    @Test
    void deberiaDenegarAccesoSinAutenticacion() throws Exception {
        mockMvc.perform(post("/exportar/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void deberiaValidarParametrosRequeridos() throws Exception {
        // Intentar exportar sin especificar formato
        mockMvc.perform(post("/exportar/usuarios"))
                .andExpect(status().isBadRequest());
    }
}