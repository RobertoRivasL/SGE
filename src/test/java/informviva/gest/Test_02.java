package informviva.gest;

import informviva.gest.model.Producto;
import informviva.gest.repository.ProductoRepositorio;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.impl.ProductoServicioImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class Test_02 {

    private ProductoServicio productoServicio;

    @Mock
    private ProductoRepositorio productoRepositorio;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productoServicio = new ProductoServicioImpl(productoRepositorio); // Usa la implementación real
    }

    @Test
    void testFindById() {
        Long productoId = 1L;
        Producto mockProducto = new Producto(productoId, "Producto 1", true);
        when(productoRepositorio.findById(productoId)).thenReturn(java.util.Optional.of(mockProducto));

        Producto result = productoServicio.findById(productoId);

        assertNotNull(result); // Verifica que el resultado no sea nulo
        assertEquals(productoId, result.getId()); // Verifica que el ID sea el esperado
        assertEquals("Producto 1", result.getNombre()); // Verifica el nombre del producto
        verify(productoRepositorio, times(1)).findById(productoId); // Verifica la interacción con el repositorio
    }
}