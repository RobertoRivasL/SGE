package informviva.gest.service.impl;

import informviva.gest.model.Producto;
import informviva.gest.service.ProductoExportacionServicio;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implementación del servicio para exportación de productos
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Service
public class ProductoExportacionServicioImpl implements ProductoExportacionServicio {

    private static final Logger logger = LoggerFactory.getLogger(ProductoExportacionServicioImpl.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Exporta lista de productos a Excel
     */
    @Override
    public byte[] exportarProductosAExcel(List<Producto> productos) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Productos");

            // Crear estilos
            CellStyle headerStyle = crearEstiloEncabezado(workbook);
            CellStyle currencyStyle = crearEstiloMoneda(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook);
            CellStyle centerStyle = crearEstiloCentrado(workbook);

            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Código", "Nombre", "Descripción", "Precio", "Stock",
                    "Categoría", "Marca", "Modelo", "Estado", "Fecha Creación"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Llenar datos
            int rowIdx = 1;
            for (Producto producto : productos) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(producto.getCodigo());
                row.createCell(1).setCellValue(producto.getNombre());
                row.createCell(2).setCellValue(producto.getDescripcion() != null ? producto.getDescripcion() : "");

                Cell precioCell = row.createCell(3);
                precioCell.setCellValue(producto.getPrecio());
                precioCell.setCellStyle(currencyStyle);

                Cell stockCell = row.createCell(4);
                stockCell.setCellValue(producto.getStock());
                stockCell.setCellStyle(centerStyle);

                row.createCell(5).setCellValue(producto.getCategoria() != null ?
                        producto.getCategoria().getNombre() : "Sin categoría");
                row.createCell(6).setCellValue(producto.getMarca() != null ? producto.getMarca() : "");
                row.createCell(7).setCellValue(producto.getModelo() != null ? producto.getModelo() : "");

                Cell estadoCell = row.createCell(8);
                estadoCell.setCellValue(producto.isActivo() ? "Activo" : "Inactivo");
                estadoCell.setCellStyle(centerStyle);

                if (producto.getFechaCreacion() != null) {
                    Cell fechaCell = row.createCell(9);
                    fechaCell.setCellValue(producto.getFechaCreacion().format(formatter));
                    fechaCell.setCellStyle(dateStyle);
                }
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            logger.info("Exportación de {} productos completada exitosamente", productos.size());
            return outputStream.toByteArray();

        } catch (IOException e) {
            logger.error("Error al exportar productos a Excel: {}", e.getMessage());
            return new byte[0];
        }
    }

    /**
     * Exporta productos a CSV
     */
    @Override
    public byte[] exportarProductosACSV(List<Producto> productos) {
        StringBuilder csv = new StringBuilder();

        // Encabezados
        csv.append("Código,Nombre,Descripción,Precio,Stock,Categoría,Marca,Modelo,Estado,Fecha Creación\n");

        // Datos
        for (Producto producto : productos) {
            csv.append(escaparCSV(producto.getCodigo())).append(",");
            csv.append(escaparCSV(producto.getNombre())).append(",");
            csv.append(escaparCSV(producto.getDescripcion())).append(",");
            csv.append(producto.getPrecio()).append(",");
            csv.append(producto.getStock()).append(",");
            csv.append(escaparCSV(producto.getCategoria() != null ?
                    producto.getCategoria().getNombre() : "")).append(",");
            csv.append(escaparCSV(producto.getMarca())).append(",");
            csv.append(escaparCSV(producto.getModelo())).append(",");
            csv.append(producto.isActivo() ? "Activo" : "Inactivo").append(",");
            csv.append(producto.getFechaCreacion() != null ?
                    producto.getFechaCreacion().format(formatter) : "");
            csv.append("\n");
        }

        logger.info("Exportación CSV de {} productos completada", productos.size());
        return csv.toString().getBytes();
    }

    /**
     * Exporta productos con bajo stock
     */
    @Override
    public byte[] exportarProductosBajoStock(List<Producto> productos, int umbral) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Productos Bajo Stock");

            // Crear estilos
            CellStyle headerStyle = crearEstiloEncabezado(workbook);
            CellStyle alertStyle = crearEstiloAlerta(workbook);
            CellStyle currencyStyle = crearEstiloMoneda(workbook);

            // Título
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("REPORTE DE PRODUCTOS CON BAJO STOCK (Umbral: " + umbral + ")");
            titleCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 6));

            // Encabezados
            Row headerRow = sheet.createRow(2);
            String[] headers = {"Código", "Nombre", "Stock Actual", "Precio", "Categoría", "Estado", "Valor Inventario"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            int rowIdx = 3;
            double valorTotalInventario = 0.0;

            for (Producto producto : productos) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(producto.getCodigo());
                row.createCell(1).setCellValue(producto.getNombre());

                Cell stockCell = row.createCell(2);
                stockCell.setCellValue(producto.getStock());
                stockCell.setCellStyle(alertStyle);

                Cell precioCell = row.createCell(3);
                precioCell.setCellValue(producto.getPrecio());
                precioCell.setCellStyle(currencyStyle);

                row.createCell(4).setCellValue(producto.getCategoria() != null ?
                        producto.getCategoria().getNombre() : "Sin categoría");
                row.createCell(5).setCellValue(producto.isActivo() ? "Activo" : "Inactivo");

                double valorProducto = producto.getPrecio() * producto.getStock();
                valorTotalInventario += valorProducto;
                Cell valorCell = row.createCell(6);
                valorCell.setCellValue(valorProducto);
                valorCell.setCellStyle(currencyStyle);
            }

            // Fila de totales
            Row totalRow = sheet.createRow(rowIdx + 1);
            totalRow.createCell(5).setCellValue("TOTAL INVENTARIO:");
            Cell totalCell = totalRow.createCell(6);
            totalCell.setCellValue(valorTotalInventario);
            totalCell.setCellStyle(currencyStyle);

            // Ajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            logger.error("Error al exportar productos con bajo stock: {}", e.getMessage());
            return new byte[0];
        }
    }

    // Métodos auxiliares para estilos
    private CellStyle crearEstiloEncabezado(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle crearEstiloMoneda(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));
        return style;
    }

    private CellStyle crearEstiloFecha(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy"));
        return style;
    }

    private CellStyle crearEstiloCentrado(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle crearEstiloAlerta(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private String escaparCSV(String valor) {
        if (valor == null) return "";
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}