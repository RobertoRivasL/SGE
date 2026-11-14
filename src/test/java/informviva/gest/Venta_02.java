package informviva.gest;


import informviva.gest.model.Venta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Venta_02 {

    @Test
    void testSetAndGetEstado() {
        Venta venta = new Venta();
        venta.setEstado("COMPLETADA");

        assertEquals(Venta.EstadoVenta.COMPLETADA, venta.getEstado());
    }

    @Test
    void testSetAndGetMetodoPago() {
        Venta venta = new Venta();
        venta.setMetodoPago("TARJETA_CREDITO");

        assertEquals(Venta.MetodoPago.TARJETA_CREDITO, venta.getMetodoPago());
    }

    @Test
    void testSetAndGetObservaciones() {
        Venta venta = new Venta();
        venta.setObservaciones("Entrega programada para el lunes");

        assertEquals("Entrega programada para el lunes", venta.getObservaciones());
    }

    @Test
    void testTotalConValoresNulos() {
        Venta venta = new Venta();
        venta.setTotal(0.0);

        assertEquals(0.0, venta.getTotal());
    }

    @Test
    void testTotalConValoresNegativos() {
        Venta venta = new Venta();
        venta.setTotal(-118.0);

        assertEquals(-118.0, venta.getTotal());
    }
}