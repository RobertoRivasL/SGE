package informviva.gest.service.impl;

import informviva.gest.model.Venta;
import informviva.gest.service.VentaExportacionServicio;
import informviva.gest.service.VentaServicio;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio refactorizado de exportación de ventas
 * Elimina dependencia de AbstractExportacionServicio y mejora manejo de errores
 *
 * @author Roberto Rivas
 * @version 3.0 - REFACTORIZADO COMPLETO
 */
@Service
public class VentaExportacionServicioImpl implements VentaExportacionServicio {

    private static final Logger logger = LoggerFactory.getLogger(VentaExportacionServicioImpl.class);

    private final VentaServicio ventaServicio;

    // ===== CONSTANTES =====
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] HEADERS_EXCEL = {
            "ID", "Fecha", "Cliente", "Vendedor", "Subtotal", "Impuesto", "Total", "Método Pago", "Estado"
    };

    private static final String ENCODING_UTF8 = "UTF-8";
    private static final String CONTENT_TYPE_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    // ===== CONSTRUCTOR =====
    public VentaExportacionServicioImpl(VentaServicio ventaServicio) {
        this.ventaServicio = ventaServicio;
    }

    // ==================== IMPLEMENTACIÓN DE MÉTODOS ====================

    @Override
    public byte[] exportarVentasAExcel(List<Venta> ventas) {
        logger.info("Iniciando exportación de {} ventas a Excel", ventas != null ? ventas.size() : 0);

        if (ventas == null || ventas.isEmpty()) {
            logger.warn("Lista de ventas vacía o nula para exportación Excel");
            return crearExcelVacio();
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Ventas");

            // Configurar estilos
            CellStyle headerStyle = crearEstiloEncabezado(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook);
            CellStyle currencyStyle = crearEstiloMoneda(workbook);

            // Crear encabezados
            crearEncabezadosExcel(sheet, headerStyle);

            // Llenar datos
            llenarDatosExcel(sheet, ventas, dateStyle, currencyStyle);

            // Ajustar ancho de columnas
            ajustarAnchoColumnas(sheet);

            workbook.write(outputStream);

            logger.info("Exportación Excel completada exitosamente");
            return outputStream.toByteArray();

        } catch (Exception e) {
            logger.error("Error al exportar ventas a Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Error al exportar ventas a Excel", e);
        }
    }

    @Override
    public byte[] exportarVentasAPDF(List<Venta> ventas, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        logger.info("Iniciando exportación de {} ventas a PDF", ventas != null ? ventas.size() : 0);

        if (ventas == null || ventas.isEmpty()) {
            logger.warn("Lista de ventas vacía o nula para exportación PDF");
            return crearPDFVacio(fechaInicio, fechaFin);
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Agregar contenido
            agregarTituloPDF(document, fechaInicio, fechaFin);
            agregarTablaPDF(document, ventas);
            agregarResumenPDF(document, ventas);

            document.close();

            logger.info("Exportación PDF completada exitosamente");
            return outputStream.toByteArray();

        } catch (Exception e) {
            logger.error("Error al exportar ventas a PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Error al exportar ventas a PDF", e);
        }
    }

    @Override
    public byte[] exportarVentasACSV(List<Venta> ventas) {
        logger.info("Iniciando exportación de {} ventas a CSV", ventas != null ? ventas.size() : 0);

        if (ventas == null || ventas.isEmpty()) {
            logger.warn("Lista de ventas vacía o nula para exportación CSV");
            return crearCSVVacio();
        }

        try {
            StringBuilder csv = new StringBuilder();

            // Encabezados
            csv.append("ID,Fecha,Cliente_RUT,Cliente_Nombre,Vendedor,Subtotal,Impuesto,Total,Método_Pago,Estado,Observaciones\n");

            // Datos
            for (Venta venta : ventas) {
                agregarFilaCSV(csv, venta);
            }

            logger.info("Exportación CSV completada exitosamente");
            return csv.toString().getBytes(ENCODING_UTF8);

        } catch (Exception e) {
            logger.error("Error al exportar ventas a CSV: {}", e.getMessage(), e);
            throw new RuntimeException("Error al exportar ventas a CSV", e);
        }
    }

    @Override
    public byte[] exportarResumenVentasPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        logger.info("Generando resumen de ventas del período {} al {}", fechaInicio, fechaFin);

        try {
            validarRangoFechas(fechaInicio, fechaFin);
            List<Venta> ventas = ventaServicio.buscarPorRangoFechas(fechaInicio, fechaFin);
            return exportarVentasAPDF(ventas, fechaInicio, fechaFin);

        } catch (Exception e) {
            logger.error("Error al generar resumen de ventas: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar resumen de ventas", e);
        }
    }

    // ==================== MÉTODOS PRIVADOS - EXCEL ====================

    private CellStyle crearEstiloEncabezado(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();

        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 12);

        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // Bordes
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    private CellStyle crearEstiloFecha(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));
        return style;
    }

    private CellStyle crearEstiloMoneda(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("$#,##0.00"));
        return style;
    }

    private void crearEncabezadosExcel(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < HEADERS_EXCEL.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADERS_EXCEL[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void llenarDatosExcel(Sheet sheet, List<Venta> ventas, CellStyle dateStyle, CellStyle currencyStyle) {
        int rowNum = 1;

        for (Venta venta : ventas) {
            Row row = sheet.createRow(rowNum++);
            llenarFilaExcel(row, venta, dateStyle, currencyStyle);
        }
    }

    private void llenarFilaExcel(Row row, Venta venta, CellStyle dateStyle, CellStyle currencyStyle) {
        int colNum = 0;

        // ID
        row.createCell(colNum++).setCellValue(venta.getId() != null ? venta.getId() : 0);

        // Fecha
        Cell fechaCell = row.createCell(colNum++);
        if (venta.getFecha() != null) {
            fechaCell.setCellValue(venta.getFecha().format(DATE_FORMATTER));
        }

        // Cliente
        row.createCell(colNum++).setCellValue(
                venta.getCliente() != null ? venta.getCliente().getNombreCompleto() : "Sin cliente"
        );

        // Vendedor
        row.createCell(colNum++).setCellValue(
                venta.getVendedor() != null ? venta.getVendedor().getNombreCompleto() : "Sin vendedor"
        );

        // Subtotal
        Cell subtotalCell = row.createCell(colNum++);
        subtotalCell.setCellValue(venta.getSubtotal() != null ? venta.getSubtotal() : 0.0);
        subtotalCell.setCellStyle(currencyStyle);

        // Impuesto
        Cell impuestoCell = row.createCell(colNum++);
        impuestoCell.setCellValue(venta.getImpuesto() != null ? venta.getImpuesto() : 0.0);
        impuestoCell.setCellStyle(currencyStyle);

        // Total
        Cell totalCell = row.createCell(colNum++);
        totalCell.setCellValue(venta.getTotal() != null ? venta.getTotal() : 0.0);
        totalCell.setCellStyle(currencyStyle);

        // Método de pago - CORREGIDO: Usar getDescripcionMetodoPago()
        row.createCell(colNum++).setCellValue(venta.getDescripcionMetodoPago());

        // Estado - CORREGIDO: Usar getDescripcionEstado()
        row.createCell(colNum++).setCellValue(venta.getDescripcionEstado());
    }

    private void ajustarAnchoColumnas(Sheet sheet) {
        for (int i = 0; i < HEADERS_EXCEL.length; i++) {
            sheet.autoSizeColumn(i);
            // Establecer un ancho mínimo y máximo
            int currentWidth = sheet.getColumnWidth(i);
            if (currentWidth < 2000) {
                sheet.setColumnWidth(i, 2000);
            } else if (currentWidth > 6000) {
                sheet.setColumnWidth(i, 6000);
            }
        }
    }

    private byte[] crearExcelVacio() {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Ventas");
            CellStyle headerStyle = crearEstiloEncabezado(workbook);
            crearEncabezadosExcel(sheet, headerStyle);

            // Agregar fila indicando que no hay datos
            Row row = sheet.createRow(1);
            Cell cell = row.createCell(0);
            cell.setCellValue("No hay datos para exportar");

            ajustarAnchoColumnas(sheet);
            workbook.write(outputStream);

            return outputStream.toByteArray();

        } catch (Exception e) {
            logger.error("Error creando Excel vacío: {}", e.getMessage());
            return new byte[0];
        }
    }

    // ==================== MÉTODOS PRIVADOS - PDF ====================

    private void agregarTituloPDF(Document document, LocalDateTime fechaInicio, LocalDateTime fechaFin) throws DocumentException {
        // Título
        com.lowagie.text.Font titleFont = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD
        );
        Paragraph title = new Paragraph("Reporte de Ventas", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // Período
        if (fechaInicio != null && fechaFin != null) {
            com.lowagie.text.Font subtitleFont = new com.lowagie.text.Font(
                    com.lowagie.text.Font.HELVETICA, 12
            );
            Paragraph period = new Paragraph(
                    "Período: " + fechaInicio.format(DATE_ONLY_FORMATTER) +
                            " - " + fechaFin.format(DATE_ONLY_FORMATTER),
                    subtitleFont
            );
            period.setAlignment(Element.ALIGN_CENTER);
            document.add(period);
        }

        document.add(new Paragraph(" "));
    }

    private void agregarTablaPDF(Document document, List<Venta> ventas) throws DocumentException {
        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100);

        // Anchos de columnas
        float[] columnWidths = {1f, 2.5f, 3f, 2.5f, 2f, 2f, 2f, 2f, 1.5f};
        table.setWidths(columnWidths);

        // Encabezados
        com.lowagie.text.Font headerFont = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 8, com.lowagie.text.Font.BOLD, Color.WHITE
        );

        for (String header : HEADERS_EXCEL) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(Color.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(3);
            table.addCell(cell);
        }

        // Datos
        com.lowagie.text.Font dataFont = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 7
        );

        for (Venta venta : ventas) {
            agregarFilaPDF(table, venta, dataFont);
        }

        document.add(table);
    }

    private void agregarFilaPDF(PdfPTable table, Venta venta, com.lowagie.text.Font dataFont) {
        // ID
        table.addCell(new Phrase(String.valueOf(venta.getId() != null ? venta.getId() : 0), dataFont));

        // Fecha
        table.addCell(new Phrase(
                venta.getFecha() != null ? venta.getFecha().format(DATE_ONLY_FORMATTER) : "",
                dataFont
        ));

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

        // Montos
        table.addCell(new Phrase("$" + String.format("%.2f", venta.getSubtotal() != null ? venta.getSubtotal() : 0.0), dataFont));
        table.addCell(new Phrase("$" + String.format("%.2f", venta.getImpuesto() != null ? venta.getImpuesto() : 0.0), dataFont));
        table.addCell(new Phrase("$" + String.format("%.2f", venta.getTotal() != null ? venta.getTotal() : 0.0), dataFont));

        // Método de pago y estado - CORREGIDO
        table.addCell(new Phrase(venta.getDescripcionMetodoPago(), dataFont));
        table.addCell(new Phrase(venta.getDescripcionEstado(), dataFont));
    }

    private void agregarResumenPDF(Document document, List<Venta> ventas) throws DocumentException {
        document.add(new Paragraph(" "));

        // Calcular totales
        double totalGeneral = ventas.stream()
                .filter(v -> v.getTotal() != null)
                .mapToDouble(Venta::getTotal)
                .sum();

        // Total general
        com.lowagie.text.Font totalFont = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 14, com.lowagie.text.Font.BOLD
        );
        Paragraph totalParagraph = new Paragraph(
                "Total General: $" + String.format("%.2f", totalGeneral),
                totalFont
        );
        totalParagraph.setAlignment(Element.ALIGN_RIGHT);
        document.add(totalParagraph);

        // Estadísticas adicionales
        document.add(new Paragraph(" "));
        com.lowagie.text.Font statsFont = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 10
        );

        Paragraph statsGeneral = new Paragraph("Estadísticas del Período:", statsFont);
        statsGeneral.setAlignment(Element.ALIGN_LEFT);
        document.add(statsGeneral);

        Paragraph statsVentas = new Paragraph("• Total de ventas: " + ventas.size(), statsFont);
        document.add(statsVentas);

        if (ventas.size() > 0) {
            double promedio = totalGeneral / ventas.size();
            Paragraph statsPromedio = new Paragraph("• Venta promedio: $" + String.format("%.2f", promedio), statsFont);
            document.add(statsPromedio);
        }
    }

    private byte[] crearPDFVacio(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            agregarTituloPDF(document, fechaInicio, fechaFin);

            com.lowagie.text.Font messageFont = new com.lowagie.text.Font(
                    com.lowagie.text.Font.HELVETICA, 12
            );
            Paragraph message = new Paragraph("No se encontraron ventas para el período especificado.", messageFont);
            message.setAlignment(Element.ALIGN_CENTER);
            document.add(message);

            document.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            logger.error("Error creando PDF vacío: {}", e.getMessage());
            return new byte[0];
        }
    }

    // ==================== MÉTODOS PRIVADOS - CSV ====================

    private void agregarFilaCSV(StringBuilder csv, Venta venta) {
        csv.append(venta.getId() != null ? venta.getId() : 0).append(",");
        csv.append("\"").append(venta.getFecha() != null ? venta.getFecha().format(DATE_FORMATTER) : "").append("\",");
        csv.append("\"").append(obtenerClienteRut(venta)).append("\",");
        csv.append("\"").append(obtenerClienteNombre(venta)).append("\",");
        csv.append("\"").append(obtenerVendedorNombre(venta)).append("\",");
        csv.append(venta.getSubtotal() != null ? venta.getSubtotal() : 0.0).append(",");
        csv.append(venta.getImpuesto() != null ? venta.getImpuesto() : 0.0).append(",");
        csv.append(venta.getTotal() != null ? venta.getTotal() : 0.0).append(",");
        // CORREGIDO: Usar getDescripcionMetodoPago() y escapar
        csv.append("\"").append(escaparCSV(venta.getDescripcionMetodoPago())).append("\",");
        // CORREGIDO: Usar getDescripcionEstado() y escapar
        csv.append("\"").append(escaparCSV(venta.getDescripcionEstado())).append("\",");
        csv.append("\"").append(escaparCSV(venta.getObservaciones())).append("\"");
        csv.append("\n");
    }

    private String obtenerClienteRut(Venta venta) {
        return venta.getCliente() != null && venta.getCliente().getRut() != null ?
                escaparCSV(venta.getCliente().getRut()) : "";
    }

    private String obtenerClienteNombre(Venta venta) {
        return venta.getCliente() != null ?
                escaparCSV(venta.getCliente().getNombreCompleto()) : "";
    }

    private String obtenerVendedorNombre(Venta venta) {
        return venta.getVendedor() != null ?
                escaparCSV(venta.getVendedor().getNombreCompleto()) : "";
    }

    private String escaparCSV(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("\"", "\"\"");
    }

    private byte[] crearCSVVacio() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Fecha,Cliente_RUT,Cliente_Nombre,Vendedor,Subtotal,Impuesto,Total,Método_Pago,Estado,Observaciones\n");
        csv.append("\"No hay datos para exportar\",\"\",\"\",\"\",\"\",0,0,0,\"\",\"\",\"\"\n");

        try {
            return csv.toString().getBytes(ENCODING_UTF8);
        } catch (Exception e) {
            logger.error("Error creando CSV vacío: {}", e.getMessage());
            return new byte[0];
        }
    }

    // ==================== MÉTODOS DE VALIDACIÓN ====================

    private void validarRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias");
        }

        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        // Validar que el rango no sea excesivamente amplio (más de 5 años)
        if (fechaInicio.plusYears(5).isBefore(fechaFin)) {
            throw new IllegalArgumentException("El rango de fechas no puede ser superior a 5 años");
        }
    }
}