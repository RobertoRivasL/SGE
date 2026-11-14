package informviva.gest;

/**
 * @author Roberto Rivas
 * @version 2.0
 *
 */


import informviva.gest.dto.VentaResumenDTO;
import org.junit.jupiter.api.Test;
import informviva.gest.service.ResumenVentasServicio;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ResumenVentasServicioTest {

    private final ResumenVentasServicio resumenVentasServicio = new ResumenVentasServicio();

    @Test
    void testInicializarMapa() {
        Map<String, Double> mapaNulo = null;
        Map<String, Object> mapaInicializado = resumenVentasServicio.inicializarMapa(mapaNulo);
        assertNotNull(mapaInicializado);
        assertTrue(mapaInicializado.isEmpty());

        Map<String, Double> mapaConDatos = new HashMap<>();
        mapaConDatos.put("clave1", 10.0);
        mapaConDatos.put("clave2", 20.0);
        Map<String, Object> mapaResultado = resumenVentasServicio.inicializarMapa(mapaConDatos);
        assertEquals(mapaConDatos, mapaResultado);
    }

    @Test
    void testCrearResumenVentasVacio() {
        VentaResumenDTO resumenVacio = resumenVentasServicio.crearResumenVentasVacio();
        assertNotNull(resumenVacio);
        assertEquals(BigDecimal.ZERO, resumenVacio.getTotalVentas());
        assertEquals(0L, resumenVacio.getTotalTransacciones());
        assertEquals(BigDecimal.ZERO, resumenVacio.getTicketPromedio());
        assertEquals(0L, resumenVacio.getClientesNuevos());
        assertTrue(resumenVacio.getProductosMasVendidos().isEmpty());
        assertTrue(resumenVacio.getVentasPorPeriodo().isEmpty());
        assertTrue(resumenVacio.getVentasPorCategoria().isEmpty());
    }

}