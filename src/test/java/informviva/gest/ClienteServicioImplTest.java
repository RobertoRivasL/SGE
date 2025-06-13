package informviva.gest;



import informviva.gest.model.Cliente;
import informviva.gest.repository.ClienteRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Pruebas unitarias para la clase ClienteServicioImpl.
 * Utilizamos Mockito para simular el comportamiento del repositorio
 * y probar la lógica de negocio del servicio de forma aislada.
 */
@ExtendWith(MockitoExtension.class) // Habilita el uso de anotaciones de Mockito
class ClienteServicioImplTest {

    // @Mock crea un objeto simulado (mock) del repositorio.
    // No interactuaremos con la base de datos real.
    @Mock
    private ClienteRepositorio clienteRepositorio;

    // @InjectMocks crea una instancia de ClienteServicioImpl
    // e inyecta los mocks (en este caso, clienteRepositorio) en ella.
    @InjectMocks
    private ClienteServicioImpl clienteServicio;

    private Cliente cliente1;
    private Cliente cliente2;

    // El método anotado con @BeforeEach se ejecuta antes de cada prueba.
    // Es útil para inicializar datos de prueba comunes.
    @BeforeEach
    void setUp() {
        cliente1 = new Cliente();
        cliente1.setId(1L);
        cliente1.setNombre("Roberto");
        cliente1.setEmail("roberto@test.com");

        cliente2 = new Cliente();
        cliente2.setId(2L);
        cliente2.setNombre("Ana");
        cliente2.setEmail("ana@test.com");
    }

    @DisplayName("Test para obtener todos los clientes")
    @Test
    void testObtenerTodosLosClientes() {
        // Given (Dado): Configuramos el comportamiento esperado del mock.
        // Cuando se llame a clienteRepositorio.findAll(), debe devolver nuestra lista de clientes.
        given(clienteRepositorio.findAll()).willReturn(Arrays.asList(cliente1, cliente2));

        // When (Cuando): Ejecutamos el método que queremos probar.
        List<Cliente> clientes = clienteServicio.obtenerTodosLosClientes();

        // Then (Entonces): Verificamos los resultados.
        assertThat(clientes).isNotNull(); // La lista no debe ser nula.
        assertThat(clientes.size()).isEqualTo(2); // La lista debe contener 2 clientes.
        verify(clienteRepositorio).findAll(); // Verificamos que el método findAll() fue llamado.
    }

    @DisplayName("Test para guardar un nuevo cliente")
    @Test
    void testGuardarCliente() {
        // Given
        // Cuando se llame a clienteRepositorio.save() con cualquier objeto Cliente,
        // debe devolver el mismo cliente.
        given(clienteRepositorio.save(any(Cliente.class))).willReturn(cliente1);

        // When
        Cliente clienteGuardado = clienteServicio.guardarCliente(cliente1);

        // Then
        assertThat(clienteGuardado).isNotNull();
        assertThat(clienteGuardado.getId()).isEqualTo(1L);
        verify(clienteRepositorio).save(cliente1); // Verificamos que se llamó a save() con el cliente correcto.
    }

    @DisplayName("Test para obtener un cliente por su ID (cuando existe)")
    @Test
    void testObtenerClientePorId_cuandoExiste() {
        // Given
        Long idCliente = 1L;
        given(clienteRepositorio.findById(idCliente)).willReturn(Optional.of(cliente1));

        // When
        Cliente clienteEncontrado = clienteServicio.obtenerClientePorId(idCliente);

        // Then
        assertThat(clienteEncontrado).isNotNull();
        assertThat(clienteEncontrado.getId()).isEqualTo(idCliente);
        verify(clienteRepositorio).findById(idCliente);
    }

    @DisplayName("Test para obtener un cliente por su ID (cuando no existe)")
    @Test
    void testObtenerClientePorId_cuandoNoExiste() {
        // Given
        Long idCliente = 99L;
        given(clienteRepositorio.findById(idCliente)).willReturn(Optional.empty());

        // When
        Cliente clienteEncontrado = clienteServicio.obtenerClientePorId(idCliente);

        // Then
        assertThat(clienteEncontrado).isNull(); // Esperamos que devuelva null si no lo encuentra.
        verify(clienteRepositorio).findById(idCliente);
    }

    @DisplayName("Test para eliminar un cliente por su ID")
    @Test
    void testEliminarCliente() {
        // Given
        Long idCliente = 1L;
        // No necesitamos configurar un 'given' para métodos void como deleteById.

        // When
        clienteServicio.eliminarCliente(idCliente);

        // Then
        // Verificamos que el método deleteById() del repositorio fue llamado una vez
        // con el ID correcto.
        verify(clienteRepositorio).deleteById(idCliente);
    }
}
