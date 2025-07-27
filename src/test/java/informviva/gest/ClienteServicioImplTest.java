package informviva.gest;

import informviva.gest.model.Cliente;
import informviva.gest.service.ClienteServicio;  // INTERFACE, no implementación
import informviva.gest.service.impl.ClienteServicioImpl;  // Implementación específica para testing
import informviva.gest.repository.ClienteRepositorio;
import informviva.gest.repository.VentaRepositorio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ClienteServicioImpl
 * Aplicando principios SOLID y buenas prácticas de testing
 *
 * @author Roberto Rivas Lopez
 * @version 1.0.0 - CORREGIDO
 */
@ExtendWith(MockitoExtension.class)
class ClienteServicioImplTest {

    // ============ MOCKS DE DEPENDENCIAS ============

    @Mock
    private ClienteRepositorio clienteRepositorio;

    @Mock
    private VentaRepositorio ventaRepositorio;

    // ============ CLASE BAJO PRUEBA ============

    @InjectMocks
    private ClienteServicioImpl clienteServicio;

    // ============ DATOS DE PRUEBA ============

    private Cliente clientePrueba;
    private List<Cliente> listaClientesPrueba;

    // ============ CONFIGURACIÓN DE TESTS ============

    @BeforeEach
    void configurarTest() {
        clientePrueba = crearClientePrueba();
        listaClientesPrueba = crearListaClientesPrueba();
    }

    // ============ TESTS DE MÉTODOS CRUD ============

    @Test
    void deberiaObtenerTodosLosClientes() {
        // Arrange
        when(clienteRepositorio.findAll()).thenReturn(listaClientesPrueba);

        // Act
        List<Cliente> resultado = clienteServicio.obtenerTodos();

        // Assert
        assertNotNull(resultado, "La lista no debe ser nula");
        assertEquals(2, resultado.size(), "Debe retornar 2 clientes");
        verify(clienteRepositorio, times(1)).findAll();
    }

    @Test
    void deberiaBuscarClientePorId() {
        // Arrange
        Long idCliente = 1L;
        when(clienteRepositorio.findById(idCliente)).thenReturn(Optional.of(clientePrueba));

        // Act
        Cliente resultado = clienteServicio.buscarPorId(idCliente);

        // Assert
        assertNotNull(resultado, "El cliente encontrado no debe ser nulo");
        assertEquals(clientePrueba.getId(), resultado.getId(), "El ID debe coincidir");
        assertEquals(clientePrueba.getRut(), resultado.getRut(), "El RUT debe coincidir");
        verify(clienteRepositorio, times(1)).findById(idCliente);
    }

    @Test
    void deberiaRetornarNullCuandoClienteNoExiste() {
        // Arrange
        Long idInexistente = 999L;
        when(clienteRepositorio.findById(idInexistente)).thenReturn(Optional.empty());

        // Act
        Cliente resultado = clienteServicio.buscarPorId(idInexistente);

        // Assert
        assertNull(resultado, "Debe retornar null cuando el cliente no existe");
        verify(clienteRepositorio, times(1)).findById(idInexistente);
    }

    @Test
    void deberiaGuardarClienteCorrectamente() {
        // Arrange
        when(clienteRepositorio.save(any(Cliente.class))).thenReturn(clientePrueba);

        // Act
        Cliente resultado = clienteServicio.guardar(clientePrueba);

        // Assert
        assertNotNull(resultado, "El cliente guardado no debe ser nulo");
        assertEquals(clientePrueba.getRut(), resultado.getRut(), "El RUT debe coincidir");
        verify(clienteRepositorio, times(1)).save(clientePrueba);
    }

    @Test
    void deberiaEliminarClientePorId() {
        // Arrange
        Long idCliente = 1L;
        when(clienteRepositorio.existsById(idCliente)).thenReturn(true);
        when(ventaRepositorio.countByClienteId(idCliente)).thenReturn(0L);
        doNothing().when(clienteRepositorio).deleteById(idCliente);

        // Act & Assert
        assertDoesNotThrow(() -> clienteServicio.eliminar(idCliente));
        verify(clienteRepositorio, times(1)).deleteById(idCliente);
    }

    @Test
    void deberiaLanzarExcepcionAlEliminarClienteConVentas() {
        // Arrange
        Long idCliente = 1L;
        when(clienteRepositorio.existsById(idCliente)).thenReturn(true);
        when(ventaRepositorio.countByClienteId(idCliente)).thenReturn(5L);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            clienteServicio.eliminar(idCliente);
        });

        assertTrue(exception.getMessage().contains("ventas asociadas"),
                "El mensaje debe indicar que hay ventas asociadas");
        verify(clienteRepositorio, never()).deleteById(idCliente);
    }

    // ============ TESTS DE VALIDACIÓN ============

    @Test
    void deberiaValidarRutCorrectamente() {
        // Arrange
        String rutValido = "12345678-9";
        String rutInvalido = "12345678-0";

        // Act & Assert
        // Estos tests dependerán de la implementación específica de validación de RUT
        // Se asume que existe un método de validación en el servicio
    }

    @Test
    void deberiaValidarEmailCorrectamente() {
        // Arrange
        Cliente clienteConEmailValido = crearClientePrueba();
        clienteConEmailValido.setEmail("test@example.com");

        Cliente clienteConEmailInvalido = crearClientePrueba();
        clienteConEmailInvalido.setEmail("email-invalido");

        // Act & Assert
        // Tests específicos para validación de email
    }

    // ============ TESTS DE MÉTODOS DE BÚSQUEDA ============

    @Test
    void deberiaBuscarClientesPorNombre() {
        // Arrange
        String nombreBusqueda = "Juan";
        when(clienteRepositorio.findByNombreContainingIgnoreCase(nombreBusqueda))
                .thenReturn(Arrays.asList(clientePrueba));

        // Act
        List<Cliente> resultado = clienteServicio.buscarPorNombreContiene(nombreBusqueda);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(clienteRepositorio).findByNombreContainingIgnoreCase(nombreBusqueda);
    }

    @Test
    void deberiaBuscarClientePorRut() {
        // Arrange
        String rut = "12345678-9";
        when(clienteRepositorio.findByRut(rut)).thenReturn(Optional.of(clientePrueba));

        // Act
        Cliente resultado = clienteServicio.buscarPorRut(rut);

        // Assert
        assertNotNull(resultado);
        assertEquals(rut, resultado.getRut());
        verify(clienteRepositorio).findByRut(rut);
    }

    // ============ MÉTODOS AUXILIARES PARA TESTS ============

    /**
     * Crea un cliente de prueba con datos válidos
     */
    private Cliente crearClientePrueba() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setRut("12345678-9");
        cliente.setNombre("Juan");
        cliente.setApellido("Pérez");
        cliente.setEmail("juan.perez@email.com");
        cliente.setTelefono("+56912345678");
        cliente.setDireccion("Calle Falsa 123");
        cliente.setFechaRegistro(LocalDate.now());
        cliente.setActivo(true);
        return cliente;
    }

    /**
     * Crea una lista de clientes de prueba
     */
    private List<Cliente> crearListaClientesPrueba() {
        Cliente cliente1 = crearClientePrueba();

        Cliente cliente2 = new Cliente();
        cliente2.setId(2L);
        cliente2.setRut("98765432-1");
        cliente2.setNombre("María");
        cliente2.setApellido("González");
        cliente2.setEmail("maria.gonzalez@email.com");
        cliente2.setTelefono("+56987654321");
        cliente2.setActivo(true);

        return Arrays.asList(cliente1, cliente2);
    }
}