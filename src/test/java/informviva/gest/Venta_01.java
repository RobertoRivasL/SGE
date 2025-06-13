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

        LocalDate fechaEsperada = LocalDate.of(2023, 10, 1);
        assertEquals(fechaEsperada, venta.getFechaAsLocalDate());
    }

    @Test
    void testIsClienteNuevo() {
        Cliente cliente = new Cliente();
        cliente.setFechaRegistro(LocalDate.now().minusDays(15));

        Venta venta = new Venta();
        venta.setCliente(cliente);

        assertTrue(venta.isClienteNuevo());

        cliente.setFechaRegistro(LocalDate.now().minusDays(40));
        assertFalse(venta.isClienteNuevo());
    }

    @Test
    void testGetTotalConImpuestos() {
        Venta venta = new Venta();
        venta.setSubtotal(100.0);
        venta.setImpuesto(18.0);

        assertEquals(118.0, venta.getTotalConImpuestos());
    }
}