package informviva.gest;

import informviva.gest.model.Producto;
import informviva.gest.repository.ProductoRepositorio;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.impl.ProductoServicioImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class Test_01 {

    private ProductoServicio productoServicio;

    @Mock
    private ProductoRepositorio productoRepositorio;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productoServicio = new ProductoServicioImpl(productoRepositorio); // Usa la implementación real
    }

    @Test
    void testFindAllActivos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Producto> mockPage = new PageImpl<>(List.of(new Producto(1L, "Producto 1", true)));
        when(productoRepositorio.findByActivoTrue(pageable)).thenReturn(mockPage);

        Page<Producto> result = productoServicio.findAllActivos(pageable);

        assertNotNull(result); // Verifica que el resultado no sea nulo
        assertEquals(1, result.getTotalElements()); // Verifica el número de elementos
        verify(productoRepositorio, times(1)).findByActivoTrue(pageable); // Verifica la interacción con el repositorio
    }
}