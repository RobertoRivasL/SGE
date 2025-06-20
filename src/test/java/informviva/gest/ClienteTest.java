package informviva.gest;

/**
 * @author Roberto Rivas
 * @version 2.0
 *
 */


import informviva.gest.model.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setNombre("Juan");
        cliente.setApellido("Pérez");
        cliente.setCiudad("Santiago");
        cliente.setTotalCompras(BigDecimal.ZERO);
        cliente.setNumeroCompras(0);
        cliente.setActivo(true);
    }

    @Test
    void testGetNombreCompleto() {
        assertEquals("Juan Pérez", cliente.getNombreCompleto());
    }

    @Test
    void testActualizarCiudad() {
        cliente.actualizarCiudad("Valparaíso");
        assertEquals("Valparaíso", cliente.getCiudad());
        assertNotNull(cliente.getFechaModificacion());
    }

    @Test
    void testRegistrarCompra() {
        cliente.registrarCompra(new BigDecimal("100.50"));
        assertEquals(new BigDecimal("100.50"), cliente.getTotalCompras());
        assertEquals(1, cliente.getNumeroCompras());
        assertNotNull(cliente.getFechaUltimaCompra());
    }

    @Test
    void testRegistrarCompraMontoNegativo() {
        assertThrows(IllegalArgumentException.class, () -> cliente.registrarCompra(new BigDecimal("-50.00")));
    }

    @Test
    void testActivarCliente() {
        cliente.desactivarCliente();
        cliente.activarCliente();
        assertTrue(cliente.isActivo());
        assertNotNull(cliente.getFechaModificacion());
    }

    @Test
    void testDesactivarCliente() {
        cliente.desactivarCliente();
        assertFalse(cliente.isActivo());
        assertNotNull(cliente.getFechaModificacion());
    }

    @Test
    void testSetTotalComprasNegativo() {
        assertThrows(IllegalArgumentException.class, () -> cliente.setTotalCompras(new BigDecimal("-10.00")));
    }

    @Test
    void testSetNumeroComprasNegativo() {
        assertThrows(IllegalArgumentException.class, () -> cliente.setNumeroCompras(-1));
    }
}