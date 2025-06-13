package informviva.gest;

// ===================================================================
// TEST DE INTEGRACIÓN - ExportacionControladorIntegrationTest.java
// ===================================================================


import informviva.gest.controlador.ExportacionControlador;
import informviva.gest.service.ExportacionServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExportacionControlador.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ExportacionControladorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExportacionServicio exportacionServicio;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testExportarUsuariosExcel() throws Exception {
        // Arrange
        byte[] archivoMock = "contenido excel mock".getBytes();
        when(exportacionServicio.exportarUsuarios(any())).thenReturn(archivoMock);

        // Act & Assert
        mockMvc.perform(post("/exportacion/api/usuarios/excel")
                        .param("soloActivos", "true"))
                .andExpect(status().isOk())
                .andExpected(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(content().bytes(archivoMock));
    }

    @Test
    @WithMockUser(roles = "VENTAS")
    void testExportarClientesCSV() throws Exception {
        // Arrange
        byte[] archivoMock = "id,nombre,email\n1,Juan,juan@test.com".getBytes();
        when(exportacionServicio.exportarClientes(any())).thenReturn(archivoMock);

        // Act & Assert
        mockMvc.perform(post("/exportacion/api/clientes/csv")
                        .param("categoria", "Premium"))
                .andExpect(status().isOk())
                .andExpected(header().string("Content-Type", "text/csv"))
                .andExpect(content().bytes(archivoMock));
    }

    @Test
    @WithMockUser(roles = "VENTAS")
    void testObtenerEstimaciones() throws Exception {
        // Arrange
        Map<String, Object> estimaciones = new HashMap<>();
        estimaciones.put("registros", 100L);
        estimaciones.put("tamañoEstimado", "2.5 MB");
        estimaciones.put("tiempoEstimado", "5 segundos");

        when(exportacionServicio.obtenerEstimaciones(anyString(), anyString(), any(), any()))
                .thenReturn(estimaciones);

        // Act & Assert
        mockMvc.perform(get("/exportacion/api/estimaciones/productos")
                        .param("formato", "excel"))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.registros").value(100))
                .andExpected(jsonPath("$.tamañoEstimado").value("2.5 MB"));
    }

    @Test
    void testAccesoSinAutenticacion() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/exportacion/api/usuarios/excel"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "VENTAS") // Sin permisos de ADMIN
    void testExportarUsuariosSinPermisos() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/exportacion/api/usuarios/excel"))
                .andExpect(status().isForbidden());
    }
}