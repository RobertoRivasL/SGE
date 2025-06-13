package informviva.gest.service;

import informviva.gest.dto.*;
import informviva.gest.repository.ReporteRepositorio;
import org.apache.xmlbeans.impl.xb.xmlconfig.Extensionconfig;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
public class ReporteServicio {

    private final ReporteRepositorio reporteRepository;

    public ReporteServicio(ReporteRepositorio reporteRepository) {
        this.reporteRepository = reporteRepository;
    }

    public VentaResumenDTO generarResumenVentas(LocalDate startDate, LocalDate endDate) {
        VentaResumenDTO resumen = new VentaResumenDTO();

        // Convertir fechas
        LocalDateTime startDateTime = toStartOfDay(startDate);
        LocalDateTime endDateTime = toEndOfDay(endDate);

        // Obtener datos principales
        resumen.setTotalVentas(getSafeBigDecimal(reporteRepository.sumarTotalVentasEntreFechas(startDateTime, endDateTime)));
        resumen.setTotalTransacciones(getSafeLong(reporteRepository.contarVentasEntreFechas(startDateTime, endDateTime)));
        resumen.setTotalArticulosVendidos(getSafeLong(reporteRepository.sumarCantidadArticulosVendidosEntreFechas(startDateTime, endDateTime)));
        resumen.setClientesNuevos(getSafeLong(reporteRepository.contarClientesNuevosEntreFechas(startDate, endDate)));

        // Calcular ticket promedio
        resumen.setTicketPromedio(calcularTicketPromedio(resumen.getTotalVentas(), resumen.getTotalTransacciones()));

        // Calcular porcentajes de cambio (placeholder para lógica futura)
        inicializarPorcentajesCambio(resumen);

        // Procesar productos más vendidos
        List<ProductoVendidoDTO> productosVendidos = reporteRepository.obtenerProductosMasVendidosEntreFechas(startDateTime, endDateTime);
        calcularPorcentajeProductosVendidos(productosVendidos, resumen.getTotalVentas());
        resumen.setProductosMasVendidos(productosVendidos);

        // Obtener datos adicionales
        resumen.setVentasPorPeriodo(reporteRepository.obtenerVentasPorPeriodoEntreFechas(startDateTime, endDateTime));
        resumen.setVentasPorCategoria(reporteRepository.obtenerVentasPorCategoriaEntreFechas(startDateTime, endDateTime));
        resumen.setVentasPorVendedor(reporteRepository.obtenerVentasPorVendedorEntreFechas(startDateTime, endDateTime));

        return resumen;
    }

    public List<VentaPorPeriodoDTO> obtenerVentasPorPeriodoEntreFechas(LocalDate inicio, LocalDate fin) {
        return reporteRepository.obtenerVentasPorPeriodoEntreFechas(toStartOfDay(inicio), toEndOfDay(fin));
    }

    public List<VentaPorCategoriaDTO> obtenerVentasPorCategoriaEntreFechas(LocalDate inicio, LocalDate fin) {
        return reporteRepository.obtenerVentasPorCategoriaEntreFechas(toStartOfDay(inicio), toEndOfDay(fin));
    }

    public Long contarClientesNuevosEntreFechas(LocalDate inicio, LocalDate fin) {
        return getSafeLong(reporteRepository.contarClientesNuevosEntreFechas(inicio, fin));
    }

    public KpisDTO obtenerKpis() {
        KpisDTO kpis = new KpisDTO();

        kpis.setTotalVentas(getSafeBigDecimal(reporteRepository.sumarTotalVentas()));
        kpis.setVariacionVentas(getSafeBigDecimal(reporteRepository.calcularVariacionVentas()));
        kpis.setTicketPromedio(getSafeBigDecimal(reporteRepository.calcularTicketPromedio()));
        kpis.setTotalProductos(getSafeLong(reporteRepository.contarTotalProductos()).intValue());
        kpis.setProductosActivos(getSafeLong(reporteRepository.contarProductosActivos()).intValue());
        kpis.setProductosBajoStock(getSafeLong(reporteRepository.contarProductosBajoStock()).intValue());
        kpis.setTotalClientes(getSafeLong(reporteRepository.contarTotalClientes()).intValue());

        return kpis;
    }

    // Métodos auxiliares

    private LocalDateTime toStartOfDay(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    private LocalDateTime toEndOfDay(LocalDate date) {
        return date != null ? date.atTime(LocalTime.MAX) : null;
    }

    private BigDecimal getSafeBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private Long getSafeLong(Long value) {
        return value != null ? value : 0L;
    }

    private BigDecimal calcularTicketPromedio(BigDecimal totalVentas, Long totalTransacciones) {
        if (totalVentas != null && totalTransacciones != null && totalTransacciones > 0) {
            return totalVentas.divide(BigDecimal.valueOf(totalTransacciones), 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private void inicializarPorcentajesCambio(VentaResumenDTO resumen) {
        resumen.setPorcentajeCambioVentas(null);
        resumen.setPorcentajeCambioTransacciones(null);
        resumen.setPorcentajeCambioTicketPromedio(null);
        resumen.setPorcentajeCambioClientesNuevos(null);
    }

    private void calcularPorcentajeProductosVendidos(List<ProductoVendidoDTO> productosVendidos, BigDecimal totalVentas) {
        BigDecimal totalVentasGeneral = getSafeBigDecimal(totalVentas);
        if (totalVentasGeneral.compareTo(BigDecimal.ZERO) > 0) {
            productosVendidos.forEach(producto -> {
                BigDecimal ingresos = BigDecimal.valueOf(producto.getIngresos());
                double porcentaje = ingresos.divide(totalVentasGeneral, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
                producto.setPorcentajeTotal(porcentaje);
            });
        } else {
            productosVendidos.forEach(producto -> producto.setPorcentajeTotal(0.0));
        }
    }

    // NUEVOS MÉTODOS CON LocalDateTime

    /**
     * Genera resumen de ventas con granularidad de hora
     */
    public VentaResumenDTO generarResumenVentasConHora(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Implementación pendiente
        return null;
    }

    /**
     * Obtiene ventas por período con hora específica
     */
    public List<VentaPorPeriodoDTO> obtenerVentasPorPeriodoConHora(LocalDateTime inicio, LocalDateTime fin, String granularidad) {
        // Implementación pendiente
        return null;
    }

    /**
     * Obtiene ventas por categoría en un período con hora
     */
    public List<VentaPorCategoriaDTO> obtenerVentasPorCategoriaConHora(LocalDateTime inicio, LocalDateTime fin) {
        // Implementación pendiente
        return null;
    }

    /**
     * Cuenta clientes nuevos en un período con hora específica
     */
    public Long contarClientesNuevosConHora(LocalDateTime inicio, LocalDateTime fin) {
        // Implementación pendiente
        return null;
    }

    /**
     * Genera reporte de ventas por franjas horarias
     */
    public Map<String, Double> obtenerVentasPorFranjaHoraria(LocalDateTime fecha) {
        // Implementación pendiente
        return null;
    }

    /**
     * Obtiene el análisis de tendencias de ventas por hora
     */
    public List<Map<String, Object>> analizarTendenciasVentasPorHora(LocalDateTime inicio, LocalDateTime fin) {
        // Implementación pendiente
        return null;
    }

    /**
     * Genera reporte comparativo entre períodos con hora específica
     */
    public Map<String, Object> generarReporteComparativo(
            LocalDateTime inicioActual, LocalDateTime finActual,
            LocalDateTime inicioAnterior, LocalDateTime finAnterior
    ) {
        // Implementación pendiente
        return null;
    }

    /**
     * Obtiene métricas en tiempo real
     */
    public Map<String, Object> obtenerMetricasTiempoReal() {
        // Implementación pendiente
        return null;
    }

    /**
     * Genera proyección de ventas basada en datos históricos
     */
    public Map<String, Object> generarProyeccionVentas(LocalDateTime fechaInicio, int diasProyeccion) {
        // Implementación pendiente
        return null;
    }
}