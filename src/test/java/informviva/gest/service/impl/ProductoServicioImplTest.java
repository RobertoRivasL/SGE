package informviva.gest.service.impl;

import informviva.gest.config.TestConfig;
import informviva.gest.dto.ProductoDTO;
import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.model.Producto;
import informviva.gest.repository.ProductoRepositorio;
import informviva.gest.service.ProductoServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ProductoServicioImpl
 *
 * Siguiendo principios de testing:
 * - Aislamiento: Cada test es independiente
 * - Repetibilidad: Los tests dan el mismo resultado siempre
 * - Rapidez: Tests unitarios rápidos con mocks
 * - Verificación automática: Assertions claras
 *
 * @author Tu nombre
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@DisplayName("Tests de ProductoServicioImpl")
class ProductoServicioImplTest {

    @Mock
    private ProductoRepositorio productoRepository;

    private ModelMapper modelMapper;
    private ProductoServicio productoServicio;

    // Datos de prueba
    private Producto producto;
    private ProductoDTO productoDTO;

    @BeforeEach
    void setUp() {
        modelMapper = new ModelMapper();
        productoServicio = new ProductoServicioImpl(productoRepository, modelMapper);

        // Configurar datos de prueba
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Producto Test");
        producto.setDescripcion("Descripción de prueba");
        producto.setPrecio(new BigDecimal("99.99"));
        producto.setStock(10);
        producto.setCategoria("Categoría Test");
        producto.setActivo(true);

        productoDTO = new ProductoDTO();
        productoDTO.setId(1L);
        productoDTO.setNombre("Producto Test");
        productoDTO.setDescripcion("Descripción de prueba");
        productoDTO.setPrecio(new BigDecimal("99.99"));
        productoDTO.setStock(10);
        productoDTO.setCategoria("Categoría Test");
        productoDTO.setActivo(true);
    }

    @Test
    @DisplayName("Debería buscar todos los productos como DTO")
    void buscarTodosDTO_DeberiaRetornarListaDeProductosDTO() {
        // Given
        List<Producto> productos = Arrays.asList(producto);
        when(productoRepository.findAll()).thenReturn(productos);

        // When
        List<ProductoDTO> resultado = productoServicio.buscarTodosDTO();

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Producto Test", resultado.get(0).getNombre());

        verify(productoRepository).findAll();
    }

    @Test
    @DisplayName("Debería buscar producto por ID como DTO")
    void buscarPorIdDTO_ConIdValido_DeberiaRetornarProductoDTO() {
        // Given
        Long id = 1L;
        when(productoRepository.findById(id)).thenReturn(Optional.of(producto));

        // When
        ProductoDTO resultado = productoServicio.buscarPorIdDTO(id);

        // Then
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        assertEquals("Producto Test", resultado.getNombre());

        verify(productoRepository).findById(id);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando producto no existe")
    void buscarPorIdDTO_ConIdInvalido_DeberiaLanzarExcepcion() {
        // Given
        Long id = 999L;
        when(productoRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RecursoNoEncontradoException.class,
                () -> productoServicio.buscarPorIdDTO(id));

        verify(productoRepository).findById(id);
    }

    @Test
    @DisplayName("Debería guardar producto DTO correctamente")
    void guardarDTO_ConDatosValidos_DeberiaGuardarYRetornarDTO() {
        // Given
        ProductoDTO nuevoProductoDTO = new ProductoDTO(
                "Nuevo Producto",
                new BigDecimal("149.99"),
                5,
                "Nueva Categoría"
        );

        Producto productoGuardado = new Producto();
        productoGuardado.setId(2L);
        productoGuardado.setNombre("Nuevo Producto");
        productoGuardado.setPrecio(new BigDecimal("149.99"));
        productoGuardado.setStock(5);
        productoGuardado.setCategoria("Nueva Categoría");
        productoGuardado.setActivo(true);

        when(productoRepository.save(any(Producto.class))).thenReturn(productoGuardado);

        // When
        ProductoDTO resultado = productoServicio.guardarDTO(nuevoProductoDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(2L, resultado.getId());
        assertEquals("Nuevo Producto", resultado.getNombre());
        assertEquals(new BigDecimal("149.99"), resultado.getPrecio());

        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción al guardar producto con datos inválidos")
    void guardarDTO_ConDatosInvalidos_DeberiaLanzarExcepcion() {
        // Given
        ProductoDTO productoInvalido = new ProductoDTO();
        productoInvalido.setNombre(""); // Nombre vacío
        productoInvalido.setPrecio(BigDecimal.ZERO); // Precio cero

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> productoServicio.guardarDTO(productoInvalido));

        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debería buscar productos por nombre")
    void buscarPorNombre_ConNombreValido_DeberiaRetornarProductos() {
        // Given
        String nombre = "Test";
        List<Producto> productos = Arrays.asList(producto);
        when(productoRepository.findByNombreContainingIgnoreCase(nombre))
                .thenReturn(productos);

        // When
        List<ProductoDTO> resultado = productoServicio.buscarPorNombre(nombre);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Producto Test", resultado.get(0).getNombre());

        verify(productoRepository).findByNombreContainingIgnoreCase(nombre);
    }

    @Test
    @DisplayName("Debería buscar productos por categoría")
    void buscarPorCategoria_ConCategoriaValida_DeberiaRetornarProductos() {
        // Given
        String categoria = "Categoría Test";
        List<Producto> productos = Arrays.asList(producto);
        when(productoRepository.findByCategoriaIgnoreCase(categoria))
                .thenReturn(productos);

        // When
        List<ProductoDTO> resultado = productoServicio.buscarPorCategoria(categoria);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(categoria, resultado.get(0).getCategoria());

        verify(productoRepository).findByCategoriaIgnoreCase(categoria);
    }

    @Test
    @DisplayName("Debería actualizar stock correctamente")
    void actualizarStock_ConCantidadValida_DeberiaActualizarStock() {
        // Given
        Long id = 1L;
        Integer cantidadAAgregar = 5;
        Integer stockInicial = 10;
        Integer stockEsperado = 15;

        producto.setStock(stockInicial);
        when(productoRepository.findById(id)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        // When
        productoServicio.actualizarStock(id, cantidadAAgregar);

        // Then
        verify(productoRepository).findById(id);
        verify(productoRepository).save(producto);
        assertEquals(stockEsperado, producto.getStock());
    }

    @Test
    @DisplayName("Debería buscar productos con stock bajo")
    void buscarConStockBajo_ConMinimoStock_DeberiaRetornarProductosConStockBajo() {
        // Given
        Integer minimoStock = 15;
        producto.setStock(5); // Stock menor al mínimo
        List<Producto> productosStockBajo = Arrays.asList(producto);

        when(productoRepository.findByStockLessThan(minimoStock))
                .thenReturn(productosStockBajo);

        // When
        List<ProductoDTO> resultado = productoServicio.buscarConStockBajo(minimoStock);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getStock() < minimoStock);

        verify(productoRepository).findByStockLessThan(minimoStock);
    }
}