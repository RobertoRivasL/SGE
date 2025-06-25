package informviva.gest.service;

/**
 * Interface para servicios de exportación de ventas - CORREGIDA
 * @author Roberto Rivas
 * @version 2.0
 */

import informviva.gest.model.Venta;
import java.time.LocalDateTime;
import java.util.List;

public interface VentaExportacionServicio {

    /**
     * Exporta ventas a formato Excel
     * @param ventas Lista de ventas a exportar
     * @return Bytes del archivo Excel generado
     */
    byte[] exportarVentasAExcel(List<Venta> ventas);

    /**
     * Exporta ventas a formato PDF con período específico
     * @param ventas Lista de ventas a exportar
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin Fecha de fin del período
     * @return Bytes del archivo PDF generado
     */
    byte[] exportarVentasAPDF(List<Venta> ventas, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Exporta ventas a formato CSV
     * @param ventas Lista de ventas a exportar
     * @return Bytes del archivo CSV generado
     */
    byte[] exportarVentasACSV(List<Venta> ventas);

    /**
     * Exporta resumen de ventas por período
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Bytes del archivo PDF generado
     */
    byte[] exportarResumenVentasPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin);

//    // ✅ Métodos comentados hasta verificar que existen en VentaServicio
//    /*
//    byte[] exportarVentasPorVendedor(Long vendedorId, LocalDateTime fechaInicio, LocalDateTime fechaFin);
//    byte[] exportarVentasPorCliente(Long clienteId);
//    */
}
