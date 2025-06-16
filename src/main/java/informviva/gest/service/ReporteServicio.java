package informviva.gest.service;

import informviva.gest.dto.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ReporteServicio {

    VentaResumenDTO generarResumenVentas(LocalDate startDate, LocalDate endDate);

    List<VentaPorPeriodoDTO> obtenerVentasPorPeriodoEntreFechas(LocalDate inicio, LocalDate fin);

    List<VentaPorCategoriaDTO> obtenerVentasPorCategoriaEntreFechas(LocalDate inicio, LocalDate fin);

    Long contarClientesNuevosEntreFechas(LocalDate inicio, LocalDate fin);

    VentaResumenDTO generarResumenVentasConHora(LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<VentaPorPeriodoDTO> obtenerVentasPorPeriodoConHora(LocalDateTime inicio, LocalDateTime fin, String granularidad);

    List<VentaPorCategoriaDTO> obtenerVentasPorCategoriaConHora(LocalDateTime inicio, LocalDateTime fin);

    Long contarClientesNuevosConHora(LocalDateTime inicio, LocalDateTime fin);

    Map<String, Double> obtenerVentasPorFranjaHoraria(LocalDateTime fecha);

    List<Map<String, Object>> analizarTendenciasVentasPorHora(LocalDateTime inicio, LocalDateTime fin);

    Map<String, Object> generarReporteComparativo(
            LocalDateTime inicioActual, LocalDateTime finActual,
            LocalDateTime inicioAnterior, LocalDateTime finAnterior
    );

    Map<String, Object> obtenerMetricasTiempoReal();

    Map<String, Object> generarProyeccionVentas(LocalDateTime fechaInicio, int diasProyeccion);

    Map<String, Object> obtenerKpis();
}