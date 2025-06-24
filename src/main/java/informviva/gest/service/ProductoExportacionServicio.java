package informviva.gest.service;

import informviva.gest.model.Producto;
import java.io.IOException;
import java.util.List;

/**
 * Servicio para exportación de productos
 *
 * @author Roberto Rivas
 * @version 2.1
 */
public interface ProductoExportacionServicio {

    /**
     * Exporta productos a Excel
     */
    byte[] exportarProductosExcel(List<Producto> productos) throws IOException;

    /**
     * Exporta productos con stock bajo a Excel
     */
    byte[] exportarProductosBajoStock(List<Producto> productos, int umbral) throws IOException;

    /**
     * Exporta productos a PDF
     */
    byte[] exportarProductosPDF(List<Producto> productos) throws IOException;

    /**
     * Exporta productos a CSV
     */
    byte[] exportarProductosCSV(List<Producto> productos) throws IOException;
}