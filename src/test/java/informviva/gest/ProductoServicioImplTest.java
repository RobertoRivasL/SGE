package informviva.gest;

import informviva.gest.model.Producto;
import informviva.gest.repository.ProductoRepositorio;
import informviva.gest.service.impl.ProductoServicioImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ProductoServicioImplTest {

    @Mock
    private ProductoRepositorio productoRepositorio;

    @InjectMocks
    private ProductoServicioImpl productoServicio;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testBuscarProductos() {
        // Datos de prueba
        String termino = "test";
        Pageable pageable = PageRequest.of(0, 10);
        Producto producto = new Producto();
        producto.setNombre("Producto Test");
        Page<Producto> productosPaginados = new PageImpl<>(List.of(producto));

        // Configurar el mock
        when(productoRepositorio.buscarPorNombreOCodigo(eq(termino.toUpperCase()), eq(pageable)))
                .thenReturn(productosPaginados);

        // Llamar al método
        Page<Producto> resultado = productoServicio.buscarProductos(termino, pageable);

        // Verificar resultados
        assertEquals(1, resultado.getTotalElements());
        assertEquals("Producto Test", resultado.getContent().get(0).getNombre());

        // Verificar interacción con el repositorio
        verify(productoRepositorio, times(1)).buscarPorNombreOCodigo(eq(termino.toUpperCase()), eq(pageable));
    }
}