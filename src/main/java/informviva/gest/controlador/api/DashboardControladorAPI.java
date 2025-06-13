package informviva.gest.controlador.api;

import informviva.gest.dto.MetricaDTO;
import informviva.gest.dto.VentaPorCategoriaDTO;
import informviva.gest.dto.VentaPorPeriodoDTO;
import informviva.gest.model.Producto;
import informviva.gest.model.Venta;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.ReporteServicio;
import informviva.gest.service.VentaServicio;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardControladorAPI {

    private final VentaServicio ventaServicio;
    private final ProductoServicio productoServicio;
    private final ReporteServicio reporteServicio;

    public DashboardControladorAPI(VentaServicio ventaServicio,
                                   ProductoServicio productoServicio,
                                   ReporteServicio reporteServicio) {
        this.ventaServicio = ventaServicio;
        this.productoServicio = productoServicio;
        this.reporteServicio = reporteServicio;
    }

    @GetMapping("/datos")
    public ResponseEntity<Map<String, Object>> obtenerDatosDashboard(
            @RequestParam(required = false, defaultValue = "semana") String periodo) {

        LocalDate hoy = LocalDate.now();
        LocalDate inicio;
        LocalDate fin = hoy;

        LocalDate inicioAnterior;
        LocalDate finAnterior;

        switch (periodo) {
            case "hoy":
                inicio = hoy;
                inicioAnterior = hoy.minusDays(1);
                finAnterior = inicioAnterior;
                break;
            case "mes":
                inicio = hoy.withDayOfMonth(1);
                inicioAnterior = inicio.minusMonths(1);
                finAnterior = inicioAnterior.plusMonths(1).minusDays(1);
                break;
            case "trimestre":
                int quarterMonth = (hoy.getMonthValue() - 1) / 3 * 3 + 1;
                inicio = hoy.withMonth(quarterMonth).withDayOfMonth(1);
                inicioAnterior = inicio.minusMonths(3);
                finAnterior = inicioAnterior.plusMonths(3).minusDays(1);
                break;
            case "año":
                inicio = hoy.withDayOfYear(1);
                inicioAnterior = inicio.minusYears(1);
                finAnterior = inicioAnterior.plusYears(1).minusDays(1);
                break;
            case "semana":
            default:
                inicio = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1);
                inicioAnterior = inicio.minusWeeks(1);
                finAnterior = inicioAnterior.plusDays(6);
                break;
        }

        // Conversión a LocalDateTime para los métodos de VentaServicio
        LocalDateTime inicioDT = inicio.atStartOfDay();
        LocalDateTime finDT = fin.atTime(LocalTime.MAX);
        LocalDateTime inicioAnteriorDT = inicioAnterior.atStartOfDay();
        LocalDateTime finAnteriorDT = finAnterior.atTime(LocalTime.MAX);

        // Métricas para el período actual
        Double totalVentas = ventaServicio.calcularTotalVentas(inicioDT, finDT);
        Long totalTransacciones = ventaServicio.contarTransacciones(inicioDT, finDT);
        Double ticketPromedio = ventaServicio.calcularTicketPromedio(inicioDT, finDT);
        Long clientesNuevos = reporteServicio.contarClientesNuevosEntreFechas(inicio, fin);
        Long productosVendidos = ventaServicio.contarArticulosVendidos(inicioDT, finDT);

        // Métricas para el período anterior
        Double totalVentasAnterior = ventaServicio.calcularTotalVentas(inicioAnteriorDT, finAnteriorDT);
        Long totalTransaccionesAnterior = ventaServicio.contarTransacciones(inicioAnteriorDT, finAnteriorDT);
        Double ticketPromedioAnterior = ventaServicio.calcularTicketPromedio(inicioAnteriorDT, finAnteriorDT);
        Long clientesNuevosAnterior = reporteServicio.contarClientesNuevosEntreFechas(inicioAnterior, finAnterior);
        Long productosVendidosAnterior = ventaServicio.contarArticulosVendidos(inicioAnteriorDT, finAnteriorDT);

        // Cálculo de porcentajes de cambio
        Double porcentajeCambioVentas = ventaServicio.calcularPorcentajeCambio(totalVentas, totalVentasAnterior);
        Double porcentajeCambioTransacciones = ventaServicio.calcularPorcentajeCambio(
                totalTransacciones != null ? totalTransacciones.doubleValue() : 0.0,
                totalTransaccionesAnterior != null ? totalTransaccionesAnterior.doubleValue() : 0.0);
        Double porcentajeCambioTicket = ventaServicio.calcularPorcentajeCambio(ticketPromedio, ticketPromedioAnterior);
        Double porcentajeCambioClientes = ventaServicio.calcularPorcentajeCambio(
                clientesNuevos != null ? clientesNuevos.doubleValue() : 0.0,
                clientesNuevosAnterior != null ? clientesNuevosAnterior.doubleValue() : 0.0);
        Double porcentajeCambioProductos = ventaServicio.calcularPorcentajeCambio(
                productosVendidos != null ? productosVendidos.doubleValue() : 0.0,
                productosVendidosAnterior != null ? productosVendidosAnterior.doubleValue() : 0.0);

        MetricaDTO ventasMetrica = new MetricaDTO(totalVentas, porcentajeCambioVentas);
        MetricaDTO transaccionesMetrica = new MetricaDTO(totalTransacciones, porcentajeCambioTransacciones);
        MetricaDTO ticketMetrica = new MetricaDTO(ticketPromedio, porcentajeCambioTicket);
        MetricaDTO clientesMetrica = new MetricaDTO(clientesNuevos, porcentajeCambioClientes);
        MetricaDTO productosMetrica = new MetricaDTO(productosVendidos, porcentajeCambioProductos);

        List<VentaPorPeriodoDTO> ventasPorPeriodo = reporteServicio.obtenerVentasPorPeriodoEntreFechas(inicio, fin);
        List<VentaPorCategoriaDTO> ventasPorCategoria = reporteServicio.obtenerVentasPorCategoriaEntreFechas(inicio, fin);

        List<Venta> ventasRecientes = ventaServicio.buscarPorRangoFechas(inicioDT, finDT)
                .stream()
                .limit(10)
                .collect(Collectors.toList());

        List<Map<String, Object>> ventasRecientesDTO = ventasRecientes.stream()
                .map(v -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", v.getId());
                    dto.put("fecha", v.getFecha());
                    dto.put("cliente", v.getCliente().getNombreCompleto());
                    dto.put("vendedor", v.getVendedor().getNombreCompleto());
                    dto.put("total", v.getTotal());
                    dto.put("estado", v.getEstado());
                    return dto;
                })
                .collect(Collectors.toList());

        List<Producto> productosConBajoStock = productoServicio.listarConBajoStock(5);

        List<Map<String, Object>> productosConBajoStockDTO = productosConBajoStock.stream()
                .map(p -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", p.getId());
                    dto.put("nombre", p.getNombre());
                    dto.put("codigo", p.getCodigo());
                    dto.put("stock", p.getStock());
                    dto.put("categoria", p.getCategoria() != null ? p.getCategoria().getNombre() : "Sin categoría");
                    return dto;
                })
                .collect(Collectors.toList());

        Map<String, Object> metricas = new HashMap<>();
        metricas.put("ventas", ventasMetrica);
        metricas.put("transacciones", transaccionesMetrica);
        metricas.put("ticket", ticketMetrica);
        metricas.put("clientes", clientesMetrica);
        metricas.put("productos", productosMetrica);

        Map<String, Object> graficos = new HashMap<>();
        graficos.put("ventasPorPeriodo", ventasPorPeriodo);
        graficos.put("ventasPorCategoria", ventasPorCategoria);

        Map<String, Object> tablas = new HashMap<>();
        tablas.put("ventasRecientes", ventasRecientesDTO);
        tablas.put("productosConBajoStock", productosConBajoStockDTO);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("periodo", periodo);
        respuesta.put("metricas", metricas);
        respuesta.put("graficos", graficos);
        respuesta.put("tablas", tablas);

        return ResponseEntity.ok(respuesta);
    }
}