package informviva.gest.service.impl;

/**
 * Servicio de exportación de productos - CORREGIDO
 * @author Roberto Rivas
 * @version 2.0
 */

import com.lowagie.text.Font;
import informviva.gest.model.Producto;
import informviva.gest.service.ProductoExportacionServicio;

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
public class ProductoExportacionServicioImpl implements ProductoExportacionServicio {

    public byte[] exportarProductosAExcel(List<Producto> productos) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Productos");

            // Crear estilo para encabezados
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Código", "Nombre", "Descripción", "Precio", "Stock", "Categoría", "Marca", "Modelo", "Activo"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Llenar datos
            int rowNum = 1;
            for (Producto producto : productos) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(producto.getId() != null ? producto.getId() : 0);
                row.createCell(1).setCellValue(producto.getCodigo() != null ? producto.getCodigo() : "");
                row.createCell(2).setCellValue(producto.getNombre() != null ? producto.getNombre() : "");
                row.createCell(3).setCellValue(producto.getDescripcion() != null ? producto.getDescripcion() : "");
                row.createCell(4).setCellValue(producto.getPrecio() != null ? producto.getPrecio() : 0.0);
                row.createCell(5).setCellValue(producto.getStock() != null ? producto.getStock() : 0);
                row.createCell(6).setCellValue(producto.getCategoria() != null ?
                        (producto.getCategoria().getNombre() != null ? producto.getCategoria().getNombre() : "Sin categoría") : "Sin categoría");
                row.createCell(7).setCellValue(producto.getMarca() != null ? producto.getMarca() : "");
                row.createCell(8).setCellValue(producto.getModelo() != null ? producto.getModelo() : "");
                row.createCell(9).setCellValue(producto.getActivo() != null ? (producto.getActivo() ? "Sí" : "No") : "No");
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
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
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(Color.DARK_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Datos
            Font dataFont = new Font(Font.HELVETICA, 8);
            for (Producto producto : productos) {
                table.addCell(new Phrase(String.valueOf(producto.getId() != null ? producto.getId() : 0), dataFont));
                table.addCell(new Phrase(producto.getCodigo() != null ? producto.getCodigo() : "", dataFont));
                table.addCell(new Phrase(producto.getNombre() != null ? producto.getNombre() : "", dataFont));

                String descripcion = producto.getDescripcion() != null ? producto.getDescripcion() : "";
                if (descripcion.length() > 50) {
                    descripcion = descripcion.substring(0, 47) + "...";
                }
                table.addCell(new Phrase(descripcion, dataFont));

                table.addCell(new Phrase("$" + String.format("%.2f", producto.getPrecio() != null ? producto.getPrecio() : 0.0), dataFont));
                table.addCell(new Phrase(String.valueOf(producto.getStock() != null ? producto.getStock() : 0), dataFont));
                table.addCell(new Phrase(producto.getCategoria() != null ?
                        (producto.getCategoria().getNombre() != null ? producto.getCategoria().getNombre() : "Sin categoría") : "Sin categoría", dataFont));
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

    public byte[] exportarProductosBajoStock(List<Producto> productos, int umbral) {
        // Filtrar productos con stock bajo
        List<Producto> productosBajoStock = productos.stream()
                .filter(p -> p.getStock() != null && p.getStock() <= umbral)
                .collect(Collectors.toList());

        return exportarProductosAExcel(productosBajoStock);
    }

    @Override
    public byte[] exportarProductosExcel(List<Producto> productos) {
        return exportarProductosAExcel(productos);
    }

}