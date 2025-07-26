package informviva.gest.service.impl;

import informviva.gest.model.Venta;
import informviva.gest.service.VentaExportacionServicio;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implementación del servicio para exportación de ventas
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Service
public class VentaExportacionServicioImpl implements VentaExportacionServicio {

    private static final Logger logger = LoggerFactory.getLogger(VentaExportacionServicioImpl.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Exporta ventas a Excel con rango de fechas
     */
    @Override
    public byte[] exportarVentasAExcel(List<Venta> ventas, LocalDate fechaInicio, LocalDate fechaFin) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Crear hoja de resumen
            crearHojaResumen(workbook, ventas, fechaInicio, fechaFin);

            // Crear hoja de ventas detalladas
            crearHojaVentasDetalladas(workbook, ventas);

            workbook.write(outputStream);
            logger.info("Exportación de {} ventas completada con rango de fechas", ventas.size());
            return outputStream.toByteArray();

        } catch (IOException e) {
            logger.error("Error al exportar ventas a Excel: {}", e.getMessage());
            return new byte[0];
        }
    }

    /**
     * Exporta ventas a Excel (método simple)
     */
    @Override
    public byte[] exportarVentasAExcel(List<Venta> ventas) {
        return exportarVentasAExcel(ventas, null, null);
    }

    /**
     * Exporta ventas a CSV
     */
    @Override
    public byte[] exportarVentasACSV(List<Venta> ventas) {
        StringBuilder csv = new StringBuilder();

        // Encabezados
        csv.append("ID,Fecha,Cliente,Vendedor,Subtotal,Impuesto,Total,Método Pago,Estado,Observaciones\n");

        // Datos
        for (Venta venta : ventas) {
            csv.append(venta.getId()).append(",");
            csv.append(venta.getFecha().format(dateTimeFormatter)).append(",");
            csv.append(escaparCSV(venta.getCliente().getNombreCompleto())).append(",");
            csv.append(escaparCSV(venta.getVendedor().getNombreCompleto())).append(",");
            csv.append(venta.getSubtotal()).append(",");
            csv.append(venta.getImpuesto()).append(",");
            csv.append(venta.getTotal()).append(",");
            csv.append(escaparCSV(venta.getMetodoPago() != null ? venta.getMetodoPago().toString() : "")).append(",");
            csv.append(escaparCSV(venta.getEstado() != null ? venta.getEstado().toString() : "COMPLETADA")).append(",");
            csv.append(escaparCSV(venta.getObservaciones()));
            csv.append("\n");
        }

        logger.info("Exportación CSV de {} ventas completada", ventas.size());
        return csv.toString().getBytes();
    }

    /**
     * Exporta ventas a CSV con rango de fechas
     */
    @Override
    public byte[] exportarVentasACSV(List<Venta> ventas, LocalDate fechaInicio, LocalDate fechaFin) {
        // Para CSV, el rango de fechas se incluye como comentario al inicio
        StringBuilder csv = new StringBuilder();

        // Comentario con rango de fechas
        csv.append("# Reporte de Ventas - Período: ");
        csv.append(fechaInicio != null ? fechaInicio.format(dateFormatter) : "Inicio");
        csv.append(" a ");
        csv.append(fechaFin != null ? fechaFin.format(dateFormatter) : "Fin");
        csv.append("\n");

        // Resto del contenido igual al método simple
        csv.append("ID,Fecha,Cliente,Vendedor,Subtotal,Impuesto,Total,Método Pago,Estado,Observaciones\n");

        for (Venta venta : ventas) {
            csv.append(venta.getId()).append(",");
            csv.append(venta.getFecha().format(dateTimeFormatter)).append(",");
            csv.append(escaparCSV(venta.getCliente().getNombreCompleto())).append(",");
            csv.append(escaparCSV(venta.getVendedor().getNombreCompleto())).append(",");
            csv.append(venta.getSubtotal()).append(",");
            csv.append(venta.getImpuesto()).append(",");
            csv.append(venta.getTotal()).append(",");
            csv.append(escaparCSV(venta.getMetodoPago() != null ? venta.getMetodoPago().toString() : "")).append(",");
            csv.append(escaparCSV(venta.getEstado() != null ? venta.getEstado().toString() : "COMPLETADA")).append(",");
            csv.append(escaparCSV(venta.getObservaciones()));
            csv.append("\n");
        }

        logger.info("Exportación CSV de {} ventas completada con rango de fechas", ventas.size());
        return csv.toString().getBytes();
    }

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Crea hoja de resumen con métricas
     */
    private void crearHojaResumen(Workbook workbook, List<Venta> ventas, LocalDate fechaInicio, LocalDate fechaFin) {
        Sheet sheet = workbook.createSheet("Resumen");

        CellStyle titleStyle = crearEstiloTitulo(workbook);
        CellStyle headerStyle = crearEstiloEncabezado(workbook);

        int rowIdx = 0;

        // Título
        Row titleRow = sheet.createRow(rowIdx++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("RESUMEN DE VENTAS");
        titleCell.setCellStyle(titleStyle);

        // Período
        Row periodRow = sheet.createRow(rowIdx++);
        periodRow.createCell(0).setCellValue("Período: " +
                (fechaInicio != null ? fechaInicio.format(dateFormatter) : "Inicio") +
                " - " +
                (fechaFin != null ? fechaFin.format(dateFormatter) : "Fin"));

        rowIdx++; // Línea en blanco

        // Métricas principales
        double totalVentas = ventas.stream()
                .filter(v -> !"ANULADA".equals(v.getEstado() != null ? v.getEstado().toString() : ""))
                .mapToDouble(Venta::getTotal)
                .sum();

        long ventasActivas = ventas.stream()
                .filter(v -> !"ANULADA".equals(v.getEstado() != null ? v.getEstado().toString() : ""))
                .count();

        double ticketPromedio = ventasActivas > 0 ? totalVentas / ventasActivas : 0;

        // Encabezados de métricas
        Row metricHeaderRow = sheet.createRow(rowIdx++);
        metricHeaderRow.createCell(0).setCellValue("Métrica");
        metricHeaderRow.createCell(1).setCellValue("Valor");
        metricHeaderRow.getCell(0).setCellStyle(headerStyle);
        metricHeaderRow.getCell(1).setCellStyle(headerStyle);

        // Métricas
        String[][] metricas = {
                {"Total de Ventas", String.format("$%.2f", totalVentas)},
                {"Número de Transacciones", String.valueOf(ventasActivas)},
                {"Ticket Promedio", String.format("$%.2f", ticketPromedio)},
                {"Total de Registros", String.valueOf(ventas.size())},
                {"Ventas Anuladas", String.valueOf(ventas.size() - ventasActivas)}
        };

        for (String[] metrica : metricas) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(metrica[0]);
            row.createCell(1).setCellValue(metrica[1]);
        }

        // Ajustar columnas
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    /**
     * Crea hoja de ventas detalladas
     */
    private void crearHojaVentasDetalladas(Workbook workbook, List<Venta> ventas) {
        Sheet sheet = workbook.createSheet("Ventas Detalladas");

        CellStyle headerStyle = crearEstiloEncabezado(workbook);
        CellStyle currencyStyle = crearEstiloMoneda(workbook);
        CellStyle dateStyle = crearEstiloFecha(workbook);
        CellStyle centerStyle = crearEstiloCentrado(workbook);

        // Encabezados
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Fecha", "Cliente", "Vendedor", "Subtotal",
                "Impuesto", "Total", "Método Pago", "Estado", "Observaciones"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Datos
        int rowIdx = 1;
        for (Venta venta : ventas) {
            Row row = sheet.createRow(rowIdx++);

            Cell idCell = row.createCell(0);
            idCell.setCellValue(venta.getId());
            idCell.setCellStyle(centerStyle);

            Cell fechaCell = row.createCell(1);
            fechaCell.setCellValue(venta.getFecha().format(dateTimeFormatter));
            fechaCell.setCellStyle(dateStyle);

            row.createCell(2).setCellValue(venta.getCliente().getNombreCompleto());
            row.createCell(3).setCellValue(venta.getVendedor().getNombreCompleto());

            Cell subtotalCell = row.createCell(4);
            subtotalCell.setCellValue(venta.getSubtotal());
            subtotalCell.setCellStyle(currencyStyle);

            Cell impuestoCell = row.createCell(5);
            impuestoCell.setCellValue(venta.getImpuesto());
            impuestoCell.setCellStyle(currencyStyle);

            Cell totalCell = row.createCell(6);
            totalCell.setCellValue(venta.getTotal());
            totalCell.setCellStyle(currencyStyle);

            row.createCell(7).setCellValue(venta.getMetodoPago() != null ? venta.getMetodoPago().toString() : "");

            Cell estadoCell = row.createCell(8);
            estadoCell.setCellValue(venta.getEstado() != null ? venta.getEstado().toString() : "COMPLETADA");
            estadoCell.setCellStyle(centerStyle);

            row.createCell(9).setCellValue(venta.getObservaciones() != null ? venta.getObservaciones() : "");
        }

        // Ajustar columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    // ========== MÉTODOS DE ESTILOS ==========

    private CellStyle crearEstiloTitulo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

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
        style.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));
        return style;
    }

    private CellStyle crearEstiloCentrado(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
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