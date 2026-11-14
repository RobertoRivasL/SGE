package informviva.gest;


import informviva.gest.model.Cliente;
import informviva.gest.model.Venta;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class Venta_01 {

    @Test
    void testGetFechaAsLocalDate() {
        Venta venta = new Venta();
        venta.setFecha(LocalDateTime.of(2023, 10, 1, 10, 30));

        assertEquals(LocalDate.of(2023, 10, 1), venta.getFechaAsLocalDate());
    }

    @Test
    void testIsClienteNuevo() {
        Cliente cliente = new Cliente();
        cliente.setFechaRegistro(LocalDateTime.now().minusDays(15));
        Venta venta = new Venta();
        venta.setCliente(cliente);

        assertTrue(venta.isClienteNuevo());

        cliente.setFechaRegistro(LocalDateTime.now().minusDays(40));
        assertFalse(venta.isClienteNuevo());
    }

    @Test
    void testGetTotal() {
        Venta venta = new Venta();
        venta.setTotal(118.0);

        assertEquals(118.0, venta.getTotal());
    }
}