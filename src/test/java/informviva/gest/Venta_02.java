package informviva.gest;


import informviva.gest.model.Venta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Venta_02 {

    @Test
    void testSetAndGetEstado() {
        Venta venta = new Venta();
        venta.setEstado("COMPLETADA");

        assertEquals("COMPLETADA", venta.getEstado());
    }

    @Test
    void testSetAndGetMetodoPago() {
        Venta venta = new Venta();
        venta.setMetodoPago("Tarjeta de Crédito");

        assertEquals("Tarjeta de Crédito", venta.getMetodoPago());
    }

    @Test
    void testSetAndGetObservaciones() {
        Venta venta = new Venta();
        venta.setObservaciones("Entrega programada para el lunes");

        assertEquals("Entrega programada para el lunes", venta.getObservaciones());
    }

    @Test
    void testCalculoTotalConValoresNulos() {
        Venta venta = new Venta();
        venta.setSubtotal(0.0);
        venta.setImpuesto(0.0);

        assertEquals(0.0, venta.getTotalConImpuestos());
    }

    @Test
    void testCalculoTotalConValoresNegativos() {
        Venta venta = new Venta();
        venta.setSubtotal(-100.0);
        venta.setImpuesto(-18.0);

        assertEquals(-118.0, venta.getTotalConImpuestos());
    }
}