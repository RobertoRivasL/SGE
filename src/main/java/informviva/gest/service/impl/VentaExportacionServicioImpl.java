package informviva.gest.service.impl;

/**
 * Servicio de exportación de ventas - CORREGIDO COMPLETAMENTE
 * @author Roberto Rivas
 * @version 2.0
 */

import informviva.gest.model.Venta;
import informviva.gest.service.VentaExportacionServicio;
import informviva.gest.service.VentaServicio;
import informviva.gest.model.Venta;
import informviva.gest.service.VentaExportacionServicio;

// ✅ Apache POI para Excel
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// ✅ OpenPDF para PDF
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class VentaExportacionServicioImpl extends AbstractExportacionServicio implements VentaExportacionServicio {

    private final VentaServicio ventaServicio;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Constructor con inyección de dependencias
    public VentaExportacionServicioImpl(VentaServicio ventaServicio) {
        this.ventaServicio = ventaServicio;
    }


    @Override
    public byte[] exportarVentasAExcel(List<Venta> ventas) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Ventas");

            // Crear el estilo del encabezado
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Fecha", "Cliente", "Vendedor", "Subtotal", "Impuesto", "Total", "Método Pago", "Estado"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            // ✅ Llenar datos usando métodos CORRECTOS del modelo Venta
            int rowNum = 1;
            for (Venta venta : ventas) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(venta.getId() != null ? venta.getId() : 0);
                // ✅ CORREGIDO: getFecha() en lugar de getFechaVenta()
                row.createCell(1).setCellValue(venta.getFecha() != null ? venta.getFecha().format(DATE_FORMATTER) : "");
                row.createCell(2).setCellValue(venta.getCliente() != null ? venta.getCliente().getNombreCompleto() : "");
                row.createCell(3).setCellValue(venta.getVendedor() != null ? venta.getVendedor().getNombreCompleto() : "");
                row.createCell(4).setCellValue(venta.getSubtotal() != null ? venta.getSubtotal() : 0.0);
                row.createCell(5).setCellValue(venta.getImpuesto() != null ? venta.getImpuesto() : 0.0);
                row.createCell(6).setCellValue(venta.getTotal() != null ? venta.getTotal() : 0.0);
                row.createCell(7).setCellValue(venta.getMetodoPago() != null ? venta.getMetodoPago() : "");
                row.createCell(8).setCellValue(venta.getEstado() != null ? venta.getEstado() : "");
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al exportar ventas a Excel", e);
        }
    }

    @Override
    public byte[] exportarVentasAPDF(List<Venta> ventas, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // ✅ Título usando Font de OpenPDF
            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
            Paragraph title = new Paragraph("Reporte de Ventas", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Período
            if (fechaInicio != null && fechaFin != null) {
                com.lowagie.text.Font subtitleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12);
                Paragraph period = new Paragraph("Período: " + fechaInicio.format(DATE_ONLY_FORMATTER) +
                        " - " + fechaFin.format(DATE_ONLY_FORMATTER), subtitleFont);
                period.setAlignment(Element.ALIGN_CENTER);
                document.add(period);
            }
            document.add(new Paragraph(" "));

            // Tabla
            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);

            // Anchos de columnas
            float[] columnWidths = {1f, 2.5f, 3f, 2.5f, 2f, 2f, 2f, 2f, 1.5f};
            table.setWidths(columnWidths);

            // ✅ Encabezados usando Font de OpenPDF especificando paquete completo
            com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 8, com.lowagie.text.Font.BOLD, Color.WHITE);
            String[] headers = {"ID", "Fecha", "Cliente", "Vendedor", "Subtotal", "Impuesto", "Total", "Método", "Estado"};

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(Color.DARK_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(3);
                table.addCell(cell);
            }

            // ✅ Datos usando Font de OpenPDF
            com.lowagie.text.Font dataFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 7);
            double totalGeneral = 0;

            for (Venta venta : ventas) {
                table.addCell(new Phrase(String.valueOf(venta.getId() != null ? venta.getId() : 0), dataFont));
                // ✅ CORREGIDO: getFecha() en lugar de getFechaVenta()
                table.addCell(new Phrase(venta.getFecha() != null ? venta.getFecha().format(DATE_ONLY_FORMATTER) : "", dataFont));

                // Cliente
                String clienteNombre = venta.getCliente() != null ? venta.getCliente().getNombreCompleto() : "N/A";
                if (clienteNombre.length() > 20) {
                    clienteNombre = clienteNombre.substring(0, 17) + "...";
                }
                table.addCell(new Phrase(clienteNombre, dataFont));

                // Vendedor
                String vendedorNombre = venta.getVendedor() != null ? venta.getVendedor().getNombreCompleto() : "N/A";
                if (vendedorNombre.length() > 20) {
                    vendedorNombre = vendedorNombre.substring(0, 17) + "...";
                }
                table.addCell(new Phrase(vendedorNombre, dataFont));

                table.addCell(new Phrase("$" + String.format("%.2f", venta.getSubtotal() != null ? venta.getSubtotal() : 0.0), dataFont));
                table.addCell(new Phrase("$" + String.format("%.2f", venta.getImpuesto() != null ? venta.getImpuesto() : 0.0), dataFont));
                table.addCell(new Phrase("$" + String.format("%.2f", venta.getTotal() != null ? venta.getTotal() : 0.0), dataFont));
                table.addCell(new Phrase(venta.getMetodoPago() != null ? venta.getMetodoPago() : "", dataFont));
                table.addCell(new Phrase(venta.getEstado() != null ? venta.getEstado() : "", dataFont));

                totalGeneral += (venta.getTotal() != null ? venta.getTotal() : 0.0);
            }

            document.add(table);

            // Total general
            document.add(new Paragraph(" "));
            com.lowagie.text.Font totalFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 14, com.lowagie.text.Font.BOLD);
            Paragraph totalParagraph = new Paragraph("Total General: $" + String.format("%.2f", totalGeneral), totalFont);
            totalParagraph.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalParagraph);

            // Estadísticas adicionales
            document.add(new Paragraph(" "));
            com.lowagie.text.Font statsFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10);
            Paragraph statsParagraph = new Paragraph("Total de ventas: " + ventas.size(), statsFont);
            statsParagraph.setAlignment(Element.ALIGN_LEFT);
            document.add(statsParagraph);

            document.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al exportar ventas a PDF", e);
        }
    }

    @Override
    public byte[] exportarVentasACSV(List<Venta> ventas) {
        try {
            StringBuilder csv = new StringBuilder();

            // Encabezados
            csv.append("ID,Fecha,Cliente_RUT,Cliente_Nombre,Vendedor,Subtotal,Impuesto,Total,Método_Pago,Estado,Observaciones\n");

            // Datos
            for (Venta venta : ventas) {
                csv.append(venta.getId() != null ? venta.getId() : 0).append(",");
                // ✅ CORREGIDO: getFecha() en lugar de getFechaVenta()
                csv.append("\"").append(venta.getFecha() != null ? venta.getFecha().format(DATE_FORMATTER) : "").append("\",");
                csv.append("\"").append(venta.getCliente() != null && venta.getCliente().getRut() != null ?
                        venta.getCliente().getRut().replace("\"", "\"\"") : "").append("\",");
                csv.append("\"").append(venta.getCliente() != null ?
                        venta.getCliente().getNombreCompleto().replace("\"", "\"\"") : "").append("\",");
                csv.append("\"").append(venta.getVendedor() != null ?
                        venta.getVendedor().getNombreCompleto().replace("\"", "\"\"") : "").append("\",");
                csv.append(venta.getSubtotal() != null ? venta.getSubtotal() : 0.0).append(",");
                csv.append(venta.getImpuesto() != null ? venta.getImpuesto() : 0.0).append(",");
                csv.append(venta.getTotal() != null ? venta.getTotal() : 0.0).append(",");
                csv.append("\"").append(venta.getMetodoPago() != null ? venta.getMetodoPago().replace("\"", "\"\"") : "").append("\",");
                csv.append("\"").append(venta.getEstado() != null ? venta.getEstado().replace("\"", "\"\"") : "").append("\",");
                csv.append("\"").append(venta.getObservaciones() != null ? venta.getObservaciones().replace("\"", "\"\"") : "").append("\"");
                csv.append("\n");
            }

            return csv.toString().getBytes("UTF-8");

        } catch (Exception e) {
            throw new RuntimeException("Error al exportar ventas a CSV", e);
        }
    }

    // ✅ Métodos de apoyo CORREGIDOS para usar LocalDateTime

    public byte[] exportarResumenVentasPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<Venta> ventas = ventaServicio.buscarPorRangoFechas(fechaInicio, fechaFin);
        return exportarVentasAPDF(ventas, fechaInicio, fechaFin);
    }

}