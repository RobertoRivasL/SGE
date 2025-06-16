package informviva.gest.service.impl;

import informviva.gest.dto.*;
import informviva.gest.repository.ReporteRepositorio;
import informviva.gest.service.ReporteServicio;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteServicioImpl implements ReporteServicio {

    @PersistenceContext
    private EntityManager entityManager;

    private final ReporteRepositorio reporteRepositorio;

    @Override
    public VentaResumenDTO generarResumenVentasConHora(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        VentaResumenDTO resumen = new VentaResumenDTO();

        resumen.setTotalVentas(reporteRepositorio.sumarTotalVentasEntreFechasConHora(startDateTime, endDateTime));
        resumen.setTotalTransacciones(reporteRepositorio.contarVentasEntreFechasConHora(startDateTime, endDateTime));
        resumen.setTotalArticulosVendidos(reporteRepositorio.sumarCantidadArticulosVendidosConHora(startDateTime, endDateTime));

        LocalDate startDate = startDateTime.toLocalDate();
        LocalDate endDate = endDateTime.toLocalDate();
        resumen.setClientesNuevos(reporteRepositorio.contarClientesNuevosEntreFechas(startDate, endDate));

        BigDecimal ticketPromedio = BigDecimal.ZERO;
        if (resumen.getTotalTransacciones() != null && resumen.getTotalTransacciones() > 0 && resumen.getTotalVentas() != null) {
            ticketPromedio = resumen.getTotalVentas().divide(BigDecimal.valueOf(resumen.getTotalTransacciones()), 2, RoundingMode.HALF_UP);
        }
        resumen.setTicketPromedio(ticketPromedio);

        List<ProductoVendidoDTO> productosVendidos = reporteRepositorio.obtenerProductosMasVendidosEntreFechas(startDate, endDate);

        BigDecimal totalVentasGeneral = resumen.getTotalVentas() != null ? resumen.getTotalVentas() : BigDecimal.ZERO;
        if (totalVentasGeneral.compareTo(BigDecimal.ZERO) > 0) {
            productosVendidos.forEach(producto -> {
                BigDecimal ingresosBigDecimal = BigDecimal.valueOf(producto.getIngresos());
                double porcentaje = ingresosBigDecimal
                        .divide(totalVentasGeneral, 4, RoundingMode.HALF_UP)
                        .doubleValue() * 100.0;
                producto.setPorcentajeTotal(porcentaje);
            });
        }
        resumen.setProductosMasVendidos(productosVendidos);

        resumen.setVentasPorPeriodo(obtenerVentasPorPeriodoConHora(startDateTime, endDateTime, "HORA"));
        resumen.setVentasPorCategoria(obtenerVentasPorCategoriaConHora(startDateTime, endDateTime));
        resumen.setVentasPorVendedor(reporteRepositorio.obtenerVentasPorVendedorEntreFechas(startDate, endDate));

        return resumen;
    }

    @Override
    public List<VentaPorPeriodoDTO> obtenerVentasPorPeriodoConHora(LocalDateTime inicio, LocalDateTime fin, String granularidad) {
        return reporteRepositorio.obtenerVentasPorPeriodoConGranularidad(inicio, fin, granularidad);
    }

    @Override
    public List<VentaPorCategoriaDTO> obtenerVentasPorCategoriaConHora(LocalDateTime inicio, LocalDateTime fin) {
        LocalDate startDate = inicio.toLocalDate();
        LocalDate endDate = fin.toLocalDate();
        return reporteRepositorio.obtenerVentasPorCategoriaEntreFechas(startDate, endDate);
    }

    @Override
    public Long contarClientesNuevosConHora(LocalDateTime inicio, LocalDateTime fin) {
        LocalDate startDate = inicio.toLocalDate();
        LocalDate endDate = fin.toLocalDate();
        return reporteRepositorio.contarClientesNuevosEntreFechas(startDate, endDate);
    }

    @Override
    public Map<String, Double> obtenerVentasPorFranjaHoraria(LocalDateTime fecha) {
        List<Object[]> resultados = reporteRepositorio.obtenerVentasPorFranjaHoraria(fecha);
        Map<String, Double> ventasPorFranja = new LinkedHashMap<>();

        ventasPorFranja.put("Madrugada", 0.0);
        ventasPorFranja.put("Mañana", 0.0);
        ventasPorFranja.put("Tarde", 0.0);
        ventasPorFranja.put("Noche", 0.0);

        for (Object[] resultado : resultados) {
            String franja = (String) resultado[0];
            Double total = ((Number) resultado[1]).doubleValue();
            ventasPorFranja.put(franja, total);
        }

        return ventasPorFranja;
    }

    @Override
    public List<Map<String, Object>> analizarTendenciasVentasPorHora(LocalDateTime inicio, LocalDateTime fin) {
        List<Object[]> resultados = reporteRepositorio.analizarTendenciasPorHora(inicio, fin);

        return resultados.stream()
                .map(resultado -> {
                    Map<String, Object> tendencia = new HashMap<>();
                    tendencia.put("hora", resultado[0]);
                    tendencia.put("transacciones", resultado[1]);
                    tendencia.put("total", ((Number) resultado[2]).doubleValue());
                    tendencia.put("promedio", ((Number) resultado[3]).doubleValue());
                    return tendencia;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> generarReporteComparativo(
            LocalDateTime inicioActual, LocalDateTime finActual,
            LocalDateTime inicioAnterior, LocalDateTime finAnterior) {

        Map<String, Object> reporte = new HashMap<>();

        VentaResumenDTO resumenActual = generarResumenVentasConHora(inicioActual, finActual);
        VentaResumenDTO resumenAnterior = generarResumenVentasConHora(inicioAnterior, finAnterior);

        reporte.put("periodoActual", Map.of(
                "inicio", inicioActual,
                "fin", finActual,
                "resumen", resumenActual
        ));

        reporte.put("periodoAnterior", Map.of(
                "inicio", inicioAnterior,
                "fin", finAnterior,
                "resumen", resumenAnterior
        ));

        Map<String, Object> diferencias = new HashMap<>();

        BigDecimal ventasActuales = resumenActual.getTotalVentas() != null ? resumenActual.getTotalVentas() : BigDecimal.ZERO;
        BigDecimal ventasAnteriores = resumenAnterior.getTotalVentas() != null ? resumenAnterior.getTotalVentas() : BigDecimal.ZERO;

        if (ventasAnteriores.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal cambioVentas = ventasActuales.subtract(ventasAnteriores)
                    .divide(ventasAnteriores, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            diferencias.put("porcentajeCambioVentas", cambioVentas.doubleValue());
        } else {
            diferencias.put("porcentajeCambioVentas", ventasActuales.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0);
        }

        Long transaccionesActuales = resumenActual.getTotalTransacciones() != null ? resumenActual.getTotalTransacciones() : 0L;
        Long transaccionesAnteriores = resumenAnterior.getTotalTransacciones() != null ? resumenAnterior.getTotalTransacciones() : 0L;

        if (transaccionesAnteriores > 0) {
            double cambioTransacciones = ((double)(transaccionesActuales - transaccionesAnteriores) / transaccionesAnteriores) * 100;
            diferencias.put("porcentajeCambioTransacciones", cambioTransacciones);
        } else {
            diferencias.put("porcentajeCambioTransacciones", transaccionesActuales > 0 ? 100.0 : 0.0);
        }

        reporte.put("diferencias", diferencias);

        return reporte;
    }

    @Override
    public Map<String, Object> obtenerMetricasTiempoReal() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicioDelDia = ahora.toLocalDate().atStartOfDay();

        Map<String, Object> metricas = new HashMap<>();

        BigDecimal ventasHoy = reporteRepositorio.sumarTotalVentasEntreFechasConHora(inicioDelDia, ahora);
        Long transaccionesHoy = reporteRepositorio.contarVentasEntreFechasConHora(inicioDelDia, ahora);
        Long articulosHoy = reporteRepositorio.sumarCantidadArticulosVendidosConHora(inicioDelDia, ahora);

        metricas.put("ventasHoy", ventasHoy);
        metricas.put("transaccionesHoy", transaccionesHoy);
        metricas.put("articulosVendidosHoy", articulosHoy);

        if (transaccionesHoy != null && transaccionesHoy > 0) {
            BigDecimal ticketPromedio = ventasHoy.divide(BigDecimal.valueOf(transaccionesHoy), 2, RoundingMode.HALF_UP);
            metricas.put("ticketPromedioHoy", ticketPromedio);
        } else {
            metricas.put("ticketPromedioHoy", BigDecimal.ZERO);
        }

        Map<String, Double> ventasPorFranja = obtenerVentasPorFranjaHoraria(ahora);
        metricas.put("ventasPorFranjaHoraria", ventasPorFranja);

        LocalDateTime inicioAyer = inicioDelDia.minusDays(1);
        LocalDateTime finAyer = ahora.minusDays(1);

        BigDecimal ventasAyer = reporteRepositorio.sumarTotalVentasEntreFechasConHora(inicioAyer, finAyer);
        Long transaccionesAyer = reporteRepositorio.contarVentasEntreFechasConHora(inicioAyer, finAyer);

        if (ventasAyer.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal cambioVsAyer = ventasHoy.subtract(ventasAyer)
                    .divide(ventasAyer, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            metricas.put("cambioVsAyer", cambioVsAyer.doubleValue());
        } else {
            metricas.put("cambioVsAyer", ventasHoy.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0);
        }

        metricas.put("fechaActualizacion", ahora);

        return metricas;
    }

    @Override
    public Map<String, Object> generarProyeccionVentas(LocalDateTime fechaInicio, int diasProyeccion) {
        Map<String, Object> proyeccion = new HashMap<>();

        LocalDateTime inicioHistorico = fechaInicio.minusDays(30);
        List<Map<String, Object>> tendenciasHistoricas = analizarTendenciasVentasPorHora(inicioHistorico, fechaInicio);

        BigDecimal ventasHistoricas = reporteRepositorio.sumarTotalVentasEntreFechasConHora(inicioHistorico, fechaInicio);
        double promedioDiario = ventasHistoricas.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP).doubleValue();

        List<Map<String, Object>> proyeccionDias = new ArrayList<>();
        LocalDateTime fechaProyeccion = fechaInicio;

        for (int i = 0; i < diasProyeccion; i++) {
            Map<String, Object> diaProyectado = new HashMap<>();
            diaProyectado.put("fecha", fechaProyeccion.toLocalDate());
            diaProyectado.put("ventasProyectadas", promedioDiario);

            double factorTendencia = 1.0 + (Math.random() * 0.1 - 0.05);
            diaProyectado.put("ventasConTendencia", promedioDiario * factorTendencia);

            proyeccionDias.add(diaProyectado);
            fechaProyeccion = fechaProyeccion.plusDays(1);
        }

        proyeccion.put("promedioDiarioHistorico", promedioDiario);
        proyeccion.put("proyeccionDias", proyeccionDias);
        proyeccion.put("totalProyectado", promedioDiario * diasProyeccion);
        proyeccion.put("fechaGeneracion", LocalDateTime.now());
        proyeccion.put("basadoEnDatos", "Últimos 30 días");

        return proyeccion;
    }

}