package informviva.gest.controlador;

import informviva.gest.dto.VentaResumenDTO;
import informviva.gest.service.ResumenVentasServicio;
import informviva.gest.service.ReporteServicio;
import informviva.gest.util.RutasConstantes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class InicioControlador {

    private static final String VISTA_PANEL_VENTAS = "panel-ventas";
    private static final Logger logger = LoggerFactory.getLogger(InicioControlador.class);

    private final ResumenVentasServicio resumenVentasServicio;
    private final ReporteServicio reporteServicio;

    public InicioControlador(ResumenVentasServicio resumenVentasServicio, ReporteServicio reporteServicio) {
        this.resumenVentasServicio = resumenVentasServicio;
        this.reporteServicio = reporteServicio;
    }

    @GetMapping("/panel-ventas")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_VENTAS')")
    public String mostrarPanelVentas(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {
        try {
            LocalDate[] fechas = validarFechas(startDate, endDate);
            model.addAttribute("startDate", fechas[0]);
            model.addAttribute("endDate", fechas[1]);

            VentaResumenDTO resumenVentas = reporteServicio.generarResumenVentas(fechas[0], fechas[1]);
            inicializarResumenVentas(resumenVentas);
            model.addAttribute(RutasConstantes.VENTA_SUMMARY, resumenVentas);

        } catch (Exception e) {
            logger.error("Error cargando datos para panel de ventas: {}", e.getMessage());
            model.addAttribute(RutasConstantes.VENTA_SUMMARY, resumenVentasServicio.crearResumenVentasVacio());
            model.addAttribute("startDate", LocalDate.now().withDayOfMonth(1));
            model.addAttribute("endDate", LocalDate.now());
        }

        return VISTA_PANEL_VENTAS;
    }

    private LocalDate[] validarFechas(String startDate, String endDate) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate fechaInicio = inicioMes;
        LocalDate fechaFin = hoy;

        if (startDate != null && !startDate.isEmpty()) {
            fechaInicio = LocalDate.parse(startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            fechaFin = LocalDate.parse(endDate);
        }
        if (fechaInicio.isAfter(fechaFin)) {
            fechaInicio = inicioMes;
            fechaFin = hoy;
        }
        return new LocalDate[]{fechaInicio, fechaFin};
    }

    private void inicializarResumenVentas(VentaResumenDTO resumen) {
        resumen.setVentasPorVendedor(resumenVentasServicio.inicializarLista(resumen.getVentasPorVendedor()));
        resumen.setVentasPorPeriodo(resumenVentasServicio.inicializarLista(resumen.getVentasPorPeriodo()));
        resumen.setVentasPorCategoria(resumenVentasServicio.inicializarMapa(resumen.getVentasPorCategoria()));
        resumen.setProductosMasVendidos(resumenVentasServicio.inicializarLista(resumen.getProductosMasVendidos()));
    }
}