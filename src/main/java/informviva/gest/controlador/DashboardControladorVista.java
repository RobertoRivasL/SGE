package informviva.gest.controlador;

import informviva.gest.dto.KpisDTO;
import informviva.gest.dto.MetricaDTO;
import informviva.gest.dto.ProductoDTO;
import informviva.gest.dto.VentaDTO;
import informviva.gest.dto.VentaPorCategoriaDTO;
import informviva.gest.dto.VentaPorPeriodoDTO;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.ReporteServicio;
import informviva.gest.service.VentaServicio;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardControladorVista {

    private final VentaServicio ventaServicio;
    private final ProductoServicio productoServicio;
    private final ReporteServicio reporteServicio;

    public DashboardControladorVista(VentaServicio ventaServicio,
                                     ProductoServicio productoServicio,
                                     ReporteServicio reporteServicio) {
        this.ventaServicio = ventaServicio;
        this.productoServicio = productoServicio;
        this.reporteServicio = reporteServicio;
    }

    @GetMapping
    public String mostrarDashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        model.addAttribute("username", username);

        LocalDate hoy = LocalDate.now();
        model.addAttribute("currentDate", hoy);

        // Rango de la semana actual
        LocalDateTime[] semanaActual = obtenerRangoSemana(hoy);
        LocalDateTime[] semanaAnterior = obtenerRangoSemana(hoy.minusWeeks(1));

        // KPIs de la semana actual
        MetricaDTO ventasMetrica = calcularMetrica(
                ventaServicio.calcularTotalVentas(semanaActual[0], semanaActual[1]),
                ventaServicio.calcularTotalVentas(semanaAnterior[0], semanaAnterior[1])
        );

        MetricaDTO transaccionesMetrica = calcularMetrica(
                ventaServicio.contarTransacciones(semanaActual[0], semanaActual[1]),
                ventaServicio.contarTransacciones(semanaAnterior[0], semanaAnterior[1])
        );

        MetricaDTO ticketMetrica = calcularMetrica(
                ventaServicio.calcularTicketPromedio(semanaActual[0], semanaActual[1]),
                ventaServicio.calcularTicketPromedio(semanaAnterior[0], semanaAnterior[1])
        );

        MetricaDTO clientesMetrica = calcularMetrica(
                reporteServicio.contarClientesNuevosEntreFechas(semanaActual[0].toLocalDate(), semanaActual[1].toLocalDate()),
                reporteServicio.contarClientesNuevosEntreFechas(semanaAnterior[0].toLocalDate(), semanaAnterior[1].toLocalDate())
        );

        MetricaDTO productosMetrica = calcularMetrica(
                ventaServicio.contarArticulosVendidos(semanaActual[0], semanaActual[1]),
                ventaServicio.contarArticulosVendidos(semanaAnterior[0], semanaAnterior[1])
        );

        // Datos adicionales
        List<VentaPorPeriodoDTO> ventasPorDia = reporteServicio.obtenerVentasPorPeriodoEntreFechas(semanaActual[0].toLocalDate(), semanaActual[1].toLocalDate());
        List<VentaPorCategoriaDTO> ventasPorCategoria = reporteServicio.obtenerVentasPorCategoriaEntreFechas(semanaActual[0].toLocalDate(), semanaActual[1].toLocalDate());
        List<VentaDTO> ventasRecientes = ventaServicio.buscarPorRangoFechas(semanaActual[0], LocalDateTime.now());
        List<ProductoDTO> productosConBajoStock = productoServicio.listarConBajoStock(5);

        // Agregar datos al modelo
        model.addAttribute("ventasMetrica", ventasMetrica);
        model.addAttribute("transaccionesMetrica", transaccionesMetrica);
        model.addAttribute("ticketMetrica", ticketMetrica);
        model.addAttribute("clientesMetrica", clientesMetrica);
        model.addAttribute("productosMetrica", productosMetrica);
        model.addAttribute("ventasPorDiaData", ventasPorDia);
        model.addAttribute("ventasPorCategoriaData", ventasPorCategoria);
        model.addAttribute("ventasRecientes", ventasRecientes);
        model.addAttribute("productosConBajoStock", productosConBajoStock);

        return "dashboard";
    }

    private LocalDateTime[] obtenerRangoSemana(LocalDate fecha) {
        LocalDate inicioSemana = fecha.minusDays(fecha.getDayOfWeek().getValue() - 1);
        LocalDate finSemana = inicioSemana.plusDays(6);
        return new LocalDateTime[]{inicioSemana.atStartOfDay(), finSemana.atTime(LocalTime.MAX)};
    }

    private MetricaDTO calcularMetrica(Double valorActual, Double valorAnterior) {
        valorActual = valorActual != null ? valorActual : 0.0;
        valorAnterior = valorAnterior != null ? valorAnterior : 0.0;
        Double porcentajeCambio = ventaServicio.calcularPorcentajeCambio(valorActual, valorAnterior);
        return new MetricaDTO(valorActual, porcentajeCambio);
    }

    private MetricaDTO calcularMetrica(Long valorActual, Long valorAnterior) {
        return calcularMetrica(
                valorActual != null ? valorActual.doubleValue() : 0.0,
                valorAnterior != null ? valorAnterior.doubleValue() : 0.0
        );
    }
}