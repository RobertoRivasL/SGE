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
    void testInicializarLista() {
        List<String> listaNula = null;
        List<String> listaInicializada = resumenVentasServicio.inicializarLista(listaNula);
        assertNotNull(listaInicializada);
        assertTrue(listaInicializada.isEmpty());

        List<String> listaConDatos = Arrays.asList("dato1", "dato2");
        List<String> listaResultado = resumenVentasServicio.inicializarLista(listaConDatos);
        assertEquals(listaConDatos, listaResultado);
    }

    @Test
    void testInicializarMapa() {
        Map<String, String> mapaNulo = null;
        Map<String, String> mapaInicializado = resumenVentasServicio.inicializarMapa(mapaNulo);
        assertNotNull(mapaInicializado);
        assertTrue(mapaInicializado.isEmpty());

        Map<String, String> mapaConDatos = new HashMap<>();
        mapaConDatos.put("clave1", "valor1");
        mapaConDatos.put("clave2", "valor2");
        Map<String, String> mapaResultado = resumenVentasServicio.inicializarMapa(mapaConDatos);
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

    @Test
    void testValidarFechas() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);

        LocalDate[] fechasPorDefecto = resumenVentasServicio.validarFechas(null, null);
        assertEquals(inicioMes, fechasPorDefecto[0]);
        assertEquals(hoy, fechasPorDefecto[1]);

        LocalDate[] fechasValidas = resumenVentasServicio.validarFechas("2023-01-01", "2023-01-31");
        assertEquals(LocalDate.of(2023, 1, 1), fechasValidas[0]);
        assertEquals(LocalDate.of(2023, 1, 31), fechasValidas[1]);

        LocalDate[] fechasInvertidas = resumenVentasServicio.validarFechas("2023-01-31", "2023-01-01");
        assertEquals(inicioMes, fechasInvertidas[0]);
        assertEquals(hoy, fechasInvertidas[1]);
    }
}