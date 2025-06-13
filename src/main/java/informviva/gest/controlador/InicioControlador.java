package informviva.gest.controlador;

import informviva.gest.dto.MetricaDTO;
import informviva.gest.dto.VentaResumenDTO;
import informviva.gest.model.Producto;
import informviva.gest.model.Venta;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.ReporteServicio;
import informviva.gest.service.VentaServicio;
import informviva.gest.util.RutasConstantes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Controller
public class InicioControlador {

    private static final Logger logger = LoggerFactory.getLogger(InicioControlador.class);
    private static final int LIMITE_PRODUCTOS_BAJO_STOCK = 5;
    private static final int LIMITE_VENTAS_RECIENTES = 10;

    private final VentaServicio ventaServicio;
    private final ProductoServicio productoServicio;
    private final ReporteServicio reporteServicio;

    public InicioControlador(VentaServicio ventaServicio,
                             ProductoServicio productoServicio,
                             ReporteServicio reporteServicio) {
        this.ventaServicio = ventaServicio;
        this.productoServicio = productoServicio;
        this.reporteServicio = reporteServicio;
    }

    @GetMapping({"/", "/inicio"})
    public String mostrarInicio(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            cargarPanel(model);
        }
        return "inicio";
    }

    private void cargarPanel(Model model) {
        VentaResumenDTO resumen = obtenerResumenVentas();
        cargarVentasDelDia(model);
        cargarProductosBajoStock(model);
        cargarMetricas(model, resumen);
        cargarVentasRecientes(model);
        cargarVentasPorResumen(model, resumen);
    }

    private VentaResumenDTO obtenerResumenVentas() {
        try {
            LocalDate hoy = LocalDate.now();
            LocalDate inicioMes = hoy.withDayOfMonth(1);
            return reporteServicio.generarResumenVentas(inicioMes, hoy);
        } catch (Exception e) {
            logger.error("Error generando resumen de ventas: {}", e.getMessage());
            return crearResumenVentasVacio();
        }
    }

    private VentaResumenDTO crearResumenVentasVacio() {
        VentaResumenDTO dto = new VentaResumenDTO();
        dto.setTotalVentas(BigDecimal.ZERO);
        dto.setTotalTransacciones(0L);
        dto.setTicketPromedio(BigDecimal.ZERO);
        dto.setClientesNuevos(0L);
        dto.setProductosMasVendidos(Collections.emptyList());
        dto.setVentasPorPeriodo(Collections.emptyList());
        dto.setVentasPorCategoria(Collections.emptyList());
        return dto;
    }

    private void cargarVentasDelDia(Model model) {
        try {
            LocalDate hoy = LocalDate.now();
            LocalDateTime inicioDia = hoy.atStartOfDay();
            LocalDateTime finDia = hoy.atTime(LocalTime.MAX);
            Long ventasHoy = ventaServicio.contarTransacciones(inicioDia, finDia);
            model.addAttribute(RutasConstantes.VENTAS_HOY, ventasHoy);
        } catch (Exception e) {
            logger.error("Error contando ventas del día: {}", e.getMessage());
            model.addAttribute(RutasConstantes.VENTAS_HOY, 0L);
        }
    }

    private void cargarProductosBajoStock(Model model) {
        try {
            List<Producto> productos = productoServicio.listarConBajoStock(LIMITE_PRODUCTOS_BAJO_STOCK);
            model.addAttribute(RutasConstantes.PRODUCTOS_BAJO_STOCK, productos);
            model.addAttribute(RutasConstantes.PRODUCTOS_BAJOS, (long) productos.size());
        } catch (Exception e) {
            logger.error("Error obteniendo productos con bajo stock: {}", e.getMessage());
            model.addAttribute(RutasConstantes.PRODUCTOS_BAJO_STOCK, Collections.emptyList());
            model.addAttribute(RutasConstantes.PRODUCTOS_BAJOS, 0L);
        }
    }

    private void cargarMetricas(Model model, VentaResumenDTO resumen) {
        model.addAttribute(RutasConstantes.INGRESOS_MES, resumen.getTotalVentas());
        model.addAttribute("ventasMetrica", MetricaDTO.of(resumen.getTotalVentas(), resumen.getPorcentajeCambioVentas()));
        model.addAttribute("transaccionesMetrica", MetricaDTO.of(resumen.getTotalTransacciones(), resumen.getPorcentajeCambioTransacciones()));
        model.addAttribute("ticketMetrica", MetricaDTO.of(resumen.getTicketPromedio(), resumen.getPorcentajeCambioTicketPromedio()));
        model.addAttribute("clientesMetrica", MetricaDTO.of(resumen.getClientesNuevos(), resumen.getPorcentajeCambioClientesNuevos()));
        model.addAttribute("clientesNuevos", resumen.getClientesNuevos());
        model.addAttribute("productosSinVentas", resumen.getProductosSinVentas());
        model.addAttribute("promedioPrecio", resumen.getPromedioPrecio());
        model.addAttribute("totalIngresos", resumen.getTotalIngresos());
    }

    private void cargarVentasRecientes(Model model) {
        try {
            LocalDate hoy = LocalDate.now();
            LocalDate inicioMes = hoy.withDayOfMonth(1);
            LocalDateTime inicioMesDT = inicioMes.atStartOfDay();
            LocalDateTime hoyDT = hoy.atTime(LocalTime.MAX);
            List<Venta> ventas = ventaServicio.buscarPorRangoFechas(inicioMesDT, hoyDT);
            model.addAttribute(RutasConstantes.VENTAS_RECIENTES, limitarLista(ventas, LIMITE_VENTAS_RECIENTES));
        } catch (Exception e) {
            logger.error("Error buscando ventas recientes: {}", e.getMessage());
            model.addAttribute(RutasConstantes.VENTAS_RECIENTES, Collections.emptyList());
        }
    }

    private void cargarVentasPorResumen(Model model, VentaResumenDTO resumen) {
        model.addAttribute("productosMasVendidos", inicializarLista(resumen.getProductosMasVendidos()));
        model.addAttribute("ventasPorPeriodo", inicializarLista(resumen.getVentasPorPeriodo()));
        model.addAttribute("ventasPorCategoria", inicializarLista(resumen.getVentasPorCategoria()));
        model.addAttribute("resumenVentas", resumen);
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
            model.addAttribute(RutasConstantes.VENTA_SUMMARY, crearResumenVentasVacio());
            model.addAttribute("startDate", LocalDate.now().withDayOfMonth(1));
            model.addAttribute("endDate", LocalDate.now());
        }

        return "panel-ventas";
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

    private <T> List<T> inicializarLista(List<T> lista) {
        return lista != null ? lista : Collections.emptyList();
    }

    private <T> List<T> limitarLista(List<T> lista, int limite) {
        return lista.size() > limite ? lista.subList(0, limite) : lista;
    }

    private void inicializarResumenVentas(VentaResumenDTO resumen) {
        resumen.setVentasPorVendedor(inicializarLista(resumen.getVentasPorVendedor()));
        resumen.setVentasPorPeriodo(inicializarLista(resumen.getVentasPorPeriodo()));
        resumen.setVentasPorCategoria(inicializarLista(resumen.getVentasPorCategoria()));
        resumen.setProductosMasVendidos(inicializarLista(resumen.getProductosMasVendidos()));
    }

    @GetMapping("/contacto")
    public String mostrarContacto() {
        return "contacto";
    }

}