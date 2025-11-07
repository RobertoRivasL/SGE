package informviva.gest.controlador;

import informviva.gest.dto.VentaResumenDTO;
import informviva.gest.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ReporteControlador {

    private static final int MESES_POR_DEFECTO = 3;
    private static final int LIMITE_TOP_CLIENTES = 10;

    @Autowired
    private ReporteServicio reporteServicio;

    @Autowired
    private ReporteClienteServicio reporteClienteServicio;

    @Autowired
    private VentaServicio ventaServicio;

    @Autowired
    private ProductoServicio productoServicio;

    @Autowired
    private ClienteServicio clienteServicio;

    @GetMapping("/reportes/panel-ventas")
    public String mostrarPanelVentas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model modelo
    ) {
        LocalDate[] fechas = validarFechas(startDate, endDate, 1);
        VentaResumenDTO resumen = reporteServicio.generarResumenVentas(fechas[0], fechas[1]);

        modelo.addAttribute("ventaSummary", resumen);
        modelo.addAttribute("startDate", fechas[0]);
        modelo.addAttribute("endDate", fechas[1]);

        return "reportes";
    }

    @GetMapping("/reportes/dashboard-ejecutivo")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
    public String mostrarDashboardEjecutivo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            Model modelo) {

        LocalDate[] fechas = validarFechas(inicio, fin, MESES_POR_DEFECTO);

        try {
            modelo.addAttribute("resumenVentas", reporteServicio.generarResumenVentas(fechas[0], fechas[1]));
            modelo.addAttribute("estadisticasClientes", reporteClienteServicio.obtenerEstadisticasGenerales(fechas[0], fechas[1]));
            modelo.addAttribute("distribucionAntiguedad", reporteClienteServicio.analizarDistribucionAntiguedad());
            modelo.addAttribute("metricasRetencion", reporteClienteServicio.calcularMetricasRetencion(fechas[0], fechas[1]));
            modelo.addAttribute("topClientes", reporteClienteServicio.obtenerTopClientes(LIMITE_TOP_CLIENTES, fechas[0], fechas[1]));
            modelo.addAttribute("kpis", calcularKPIs(fechas[0], fechas[1]));
        } catch (Exception e) {
            modelo.addAttribute("error", "Error al cargar el dashboard: " + e.getMessage());
        }

        modelo.addAttribute("fechaInicio", fechas[0]);
        modelo.addAttribute("fechaFin", fechas[1]);

        return "dashboard";
    }

    private Map<String, Object> calcularKPIs(LocalDate inicio, LocalDate fin) {
        Map<String, Object> kpis = new HashMap<>();
        try {
            kpis.put("totalProductos", productoServicio.contarTodos());
            kpis.put("productosActivos", productoServicio.contarActivos());
            kpis.put("productosBajoStock", productoServicio.contarConBajoStock(5));
            kpis.put("totalClientes", clienteServicio.obtenerTodos().size());
            kpis.put("clientesActivos", clienteServicio.contarActivos());
            kpis.put("clientesNuevos", clienteServicio.contarClientesNuevos(inicio, fin));

            LocalDateTime inicioDateTime = inicio.atStartOfDay();
            LocalDateTime finDateTime = fin.atTime(23, 59, 59);
            Double totalVentas = ventaServicio.calcularTotalVentas(inicioDateTime, finDateTime);
            Long totalTransacciones = ventaServicio.contarTransacciones(inicioDateTime, finDateTime);
            Double ticketPromedio = ventaServicio.calcularTicketPromedio(inicioDateTime, finDateTime);
            kpis.put("totalVentas", totalVentas);
            kpis.put("totalTransacciones", totalTransacciones);
            kpis.put("ticketPromedio", ticketPromedio);


            // Período anterior
            // Período anterior
            LocalDate[] fechasAnteriores = calcularPeriodoAnterior(inicio, fin);
            LocalDateTime inicioAnteriorDateTime = fechasAnteriores[0].atStartOfDay();
            LocalDateTime finAnteriorDateTime = fechasAnteriores[1].atTime(23, 59, 59);

            Double ventasAnterior = ventaServicio.calcularTotalVentas(inicioAnteriorDateTime, finAnteriorDateTime);
            Long transaccionesAnterior = ventaServicio.contarTransacciones(inicioAnteriorDateTime, finAnteriorDateTime);

            kpis.put("variacionVentas", ventaServicio.calcularPorcentajeCambio(totalVentas, ventasAnterior));
            kpis.put("variacionTransacciones", ventaServicio.calcularPorcentajeCambio(
                    totalTransacciones.doubleValue(), transaccionesAnterior.doubleValue()));
        } catch (Exception e) {
            kpis.put("error", "Algunos KPIs no pudieron ser calculados");
        }
        return kpis;
    }

    private LocalDate[] validarFechas(LocalDate inicio, LocalDate fin, int mesesPorDefecto) {
        if (inicio == null) {
            inicio = LocalDate.now().minusMonths(mesesPorDefecto);
        }
        if (fin == null) {
            fin = LocalDate.now();
        }
        if (inicio.isAfter(fin)) {
            inicio = fin.minusMonths(1);
        }
        return new LocalDate[]{inicio, fin};
    }

    private LocalDate[] calcularPeriodoAnterior(LocalDate inicio, LocalDate fin) {
        return new LocalDate[]{inicio.minusMonths(MESES_POR_DEFECTO), inicio.minusDays(1)};
    }
}