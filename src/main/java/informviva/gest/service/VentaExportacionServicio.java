package informviva.gest.service;

import informviva.gest.model.Venta;

import java.time.LocalDate;
import java.util.List;

/**
 * Interfaz para el servicio de exportación de ventas
 *
 * @author Roberto Rivas
 * @version 2.0
 */
public interface VentaExportacionServicio {

    /**
     * Exporta ventas a Excel con rango de fechas
     *
     * @param ventas Lista de ventas a exportar
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin Fecha de fin del período
     * @return Array de bytes del archivo Excel
     */
    byte[] exportarVentasAExcel(List<Venta> ventas, LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Exporta ventas a Excel (método simple)
     *
     * @param ventas Lista de ventas a exportar
     * @return Array de bytes del archivo Excel
     */
    byte[] exportarVentasAExcel(List<Venta> ventas);

    /**
     * Exporta ventas a CSV
     *
     * @param ventas Lista de ventas a exportar
     * @return Array de bytes del archivo CSV
     */
    byte[] exportarVentasACSV(List<Venta> ventas);

    /**
     * Exporta ventas a CSV con rango de fechas
     *
     * @param ventas Lista de ventas a exportar
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin Fecha de fin del período
     * @return Array de bytes del archivo CSV
     */
    byte[] exportarVentasACSV(List<Venta> ventas, LocalDate fechaInicio, LocalDate fechaFin);
}