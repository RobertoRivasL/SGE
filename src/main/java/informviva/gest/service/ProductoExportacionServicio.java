package informviva.gest.service;

import informviva.gest.model.Producto;

import java.util.List;

/**
 * Interfaz para el servicio de exportación de productos
 *
 * @author Roberto Rivas
 * @version 2.0
 */
public interface ProductoExportacionServicio {

    /**
     * Exporta lista de productos a Excel
     *
     * @param productos Lista de productos a exportar
     * @return Array de bytes del archivo Excel
     */
    byte[] exportarProductosAExcel(List<Producto> productos);

    /**
     * Exporta productos a CSV
     *
     * @param productos Lista de productos a exportar
     * @return Array de bytes del archivo CSV
     */
    byte[] exportarProductosACSV(List<Producto> productos);

    /**
     * Exporta productos con bajo stock
     *
     * @param productos Lista de productos con bajo stock
     * @param umbral Umbral de stock bajo
     * @return Array de bytes del archivo Excel
     */
    byte[] exportarProductosBajoStock(List<Producto> productos, int umbral);
}