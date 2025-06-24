package informviva.gest.service.impl;

/**
 * Servicio de exportación de productos - CORREGIDO
 * @author Roberto Rivas
 * @version 2.0
 */

import com.lowagie.text.Font;
import informviva.gest.model.Producto;
import informviva.gest.service.ProductoExportacionServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ✅ Apache POI para Excel - SIN DUPLICADOS
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// ✅ OpenPDF para PDF - CORREGIDO
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoExportacionServicioImpl extends AbstractExportacionServicio implements ProductoExportacionServicio {

    private static final Logger logger = LoggerFactory.getLogger(ProductoExportacionServicioImpl.class);

    private void addPdfCell(PdfPTable table, String value, Font font, Color backgroundColor) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        if (backgroundColor != null) {
            cell.setBackgroundColor(backgroundColor);
        }
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void createCell(Row row, int columnIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        if (value != null) {
            if (value instanceof String) {
                cell.setCellValue((String) value);
            } else if (value instanceof Double) {
                cell.setCellValue((Double) value);
            } else if (value instanceof Integer) {
                cell.setCellValue((Integer) value);
            } else if (value instanceof Boolean) {
                cell.setCellValue((Boolean) value ? "Sí" : "No");
            }
        }
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    @Override
    public byte[] exportarProductosExcel(List<Producto> productos) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Productos");

            // Usar el método createHeaderStyle
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Código", "Nombre", "Descripción", "Precio", "Stock", "Categoría", "Marca", "Modelo", "Activo"};

            for (int i = 0; i < headers.length; i++) {
                createCell(headerRow, i, headers[i], headerStyle);
            }

            // Llenar datos
            int rowNum = 1;
            for (Producto producto : productos) {
                Row row = sheet.createRow(rowNum++);

                createCell(row, 0, producto.getId(), null);
                createCell(row, 1, producto.getCodigo(), null);
                createCell(row, 2, producto.getNombre(), null);
                createCell(row, 3, producto.getDescripcion(), null);
                createCell(row, 4, producto.getPrecio(), null);
                createCell(row, 5, producto.getStock(), null);
                createCell(row, 6, producto.getCategoria() != null ? producto.getCategoria().getNombre() : "Sin categoría", null);
                createCell(row, 7, producto.getMarca(), null);
                createCell(row, 8, producto.getModelo(), null);
                createCell(row, 9, producto.getActivo(), null);
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            logger.error("Error al exportar productos a Excel", e);
            throw new RuntimeException("Error al exportar productos a Excel", e);
        }
    }

    @Override
    public byte[] exportarProductosPDF(List<Producto> productos) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Título
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Listado de Productos", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // Tabla
            PdfPTable table = new PdfPTable(7); // 7 columnas principales
            table.setWidthPercentage(100);

            // Anchos de columnas
            float[] columnWidths = {1f, 2f, 3f, 4f, 2f, 1.5f, 2f};
            table.setWidths(columnWidths);

            // Encabezados
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            String[] headers = {"ID", "Código", "Nombre", "Descripción", "Precio", "Stock", "Categoría"};

            for (String header : headers) {
                addPdfCell(table, header, headerFont, Color.DARK_GRAY);
            }

            // Datos
            Font dataFont = new Font(Font.HELVETICA, 8);
            for (Producto producto : productos) {
                addPdfCell(table, String.valueOf(producto.getId() != null ? producto.getId() : 0), dataFont, null);
                addPdfCell(table, producto.getCodigo() != null ? producto.getCodigo() : "", dataFont, null);
                addPdfCell(table, producto.getNombre() != null ? producto.getNombre() : "", dataFont, null);

                String descripcion = producto.getDescripcion() != null ? producto.getDescripcion() : "";
                if (descripcion.length() > 50) {
                    descripcion = descripcion.substring(0, 47) + "...";
                }
                addPdfCell(table, descripcion, dataFont, null);

                addPdfCell(table, "$" + String.format("%.2f", producto.getPrecio() != null ? producto.getPrecio() : 0.0), dataFont, null);
                addPdfCell(table, String.valueOf(producto.getStock() != null ? producto.getStock() : 0), dataFont, null);
                addPdfCell(table, producto.getCategoria() != null ?
                        (producto.getCategoria().getNombre() != null ? producto.getCategoria().getNombre() : "Sin categoría") : "Sin categoría", dataFont, null);
            }

            document.add(table);
            document.close();
            return outputStream.toByteArray();

        } catch (DocumentException e) {
            throw new IOException("Error al exportar productos a PDF", e);
        }
    }


    @Override
    public byte[] exportarProductosCSV(List<Producto> productos) {
        try {
            StringBuilder csv = new StringBuilder();

            // Encabezados
            csv.append("ID,Código,Nombre,Descripción,Precio,Stock,Categoría,Marca,Modelo,Activo\n");

            // Datos
            for (Producto producto : productos) {
                csv.append(producto.getId() != null ? producto.getId() : 0).append(",");
                csv.append("\"").append(producto.getCodigo() != null ? producto.getCodigo().replace("\"", "\"\"") : "").append("\",");
                csv.append("\"").append(producto.getNombre() != null ? producto.getNombre().replace("\"", "\"\"") : "").append("\",");
                csv.append("\"").append(producto.getDescripcion() != null ? producto.getDescripcion().replace("\"", "\"\"") : "").append("\",");
                csv.append(producto.getPrecio() != null ? producto.getPrecio() : 0.0).append(",");
                csv.append(producto.getStock() != null ? producto.getStock() : 0).append(",");
                csv.append("\"").append(producto.getCategoria() != null ?
                        (producto.getCategoria().getNombre() != null ? producto.getCategoria().getNombre().replace("\"", "\"\"") : "Sin categoría") : "Sin categoría").append("\",");
                csv.append("\"").append(producto.getMarca() != null ? producto.getMarca().replace("\"", "\"\"") : "").append("\",");
                csv.append("\"").append(producto.getModelo() != null ? producto.getModelo().replace("\"", "\"\"") : "").append("\",");
                csv.append("\"").append(producto.getActivo() != null ? (producto.getActivo() ? "Sí" : "No") : "No").append("\"");
                csv.append("\n");
            }

            return csv.toString().getBytes("UTF-8");

        } catch (Exception e) {
            throw new RuntimeException("Error al exportar productos a CSV", e);
        }
    }

    // ✅ Métodos adicionales que podrían estar en la interfaz

    @Override
    public byte[] exportarProductosBajoStock(List<Producto> productos, int umbral) throws IOException {
        // Filtrar productos con stock bajo
        List<Producto> productosBajoStock = productos.stream()
                .filter(p -> p.getStock() != null && p.getStock() <= umbral)
                .collect(Collectors.toList());

        return exportarProductosExcel(productosBajoStock);
    }

}