package informviva.gest.service;


import informviva.gest.model.Venta;
import informviva.gest.model.VentaDetalle;
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
 * Servicio para exportación de ventas
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Service
public class VentaExportacionServicio {

    private static final Logger logger = LoggerFactory.getLogger(VentaExportacionServicio.class);
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Exporta ventas a Excel con resumen
     */
    public byte[] exportarVentasAExcel(List<Venta> ventas, LocalDate fechaInicio, LocalDate fechaFin) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Hoja de resumen
            crearHojaResumen(workbook, ventas, fechaInicio, fechaFin);

            // Hoja de ventas detalladas
            crearHojaVentasDetalladas(workbook, ventas);

            // Hoja de productos vendidos
            crearHojaProductosVendidos(workbook, ventas);

            workbook.write(outputStream);
            logger.info("Exportación de {} ventas completada exitosamente", ventas.size());
            return outputStream.toByteArray();

        } catch (IOException e) {
            logger.error("Error al exportar ventas a Excel: {}", e.getMessage());
            return new byte[0];
        }
    }

    /**
     * Crea hoja de resumen de ventas
     */
    private void crearHojaResumen(Workbook workbook, List<Venta> ventas, LocalDate fechaInicio, LocalDate fechaFin) {
        Sheet sheet = workbook.createSheet("Resumen");

        CellStyle titleStyle = crearEstiloTitulo(workbook);
        CellStyle headerStyle = crearEstiloEncabezado(workbook);
        CellStyle currencyStyle = crearEstiloMoneda(workbook);

        int rowIdx = 0;

        // Título del reporte
        Row titleRow = sheet.createRow(rowIdx++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REPORTE DE VENTAS");
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
                .filter(v -> !"ANULADA".equals(v.getEstado()))
                .mapToDouble(Venta::getTotal)
                .sum();

        long ventasActivas = ventas.stream()
                .filter(v -> !"ANULADA".equals(v.getEstado()))
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

            row.createCell(7).setCellValue(venta.getMetodoPago() != null ? venta.getMetodoPago() : "");

            Cell estadoCell = row.createCell(8);
            estadoCell.setCellValue(venta.getEstado() != null ? venta.getEstado() : "COMPLETADA");
            estadoCell.setCellStyle(centerStyle);

            row.createCell(9).setCellValue(venta.getObservaciones() != null ? venta.getObservaciones() : "");
        }

        // Ajustar columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Crea hoja de productos vendidos (si tienes VentaDetalle)
     */
    private void crearHojaProductosVendidos(Workbook workbook, List<Venta> ventas) {
        Sheet sheet = workbook.createSheet("Productos Vendidos");

        CellStyle headerStyle = crearEstiloEncabezado(workbook);
        CellStyle currencyStyle = crearEstiloMoneda(workbook);
        CellStyle centerStyle = crearEstiloCentrado(workbook);

        // Encabezados
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID Venta", "Fecha", "Producto", "Cantidad",
                "Precio Unitario", "Descuento", "Subtotal", "Total"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Solo crear esta hoja si tienes relación con VentaDetalle
        // Por ahora comentado porque no veo la relación en tu modelo Venta
        /*
        int rowIdx = 1;
        for (Venta venta : ventas) {
            if (venta.getDetalles() != null) {
                for (VentaDetalle detalle : venta.getDetalles()) {
                    Row row = sheet.createRow(rowIdx++);

                    Cell idVentaCell = row.createCell(0);
                    idVentaCell.setCellValue(venta.getId());
                    idVentaCell.setCellStyle(centerStyle);

                    row.createCell(1).setCellValue(venta.getFecha().format(dateTimeFormatter));
                    row.createCell(2).setCellValue(detalle.getProducto().getNombre());

                    Cell cantidadCell = row.createCell(3);
                    cantidadCell.setCellValue(detalle.getCantidad());
                    cantidadCell.setCellStyle(centerStyle);

                    Cell precioCell = row.createCell(4);
                    precioCell.setCellValue(detalle.getPrecioUnitario());
                    precioCell.setCellStyle(currencyStyle);

                    Cell descuentoCell = row.createCell(5);
                    descuentoCell.setCellValue(detalle.getDescuento() != null ? detalle.getDescuento() : 0.0);
                    descuentoCell.setCellStyle(currencyStyle);

                    Cell subtotalCell = row.createCell(6);
                    subtotalCell.setCellValue(detalle.getSubtotal());
                    subtotalCell.setCellStyle(currencyStyle);

                    Cell totalCell = row.createCell(7);
                    totalCell.setCellValue(detalle.getTotal());
                    totalCell.setCellStyle(currencyStyle);
                }
            }
        }
        */

        // Mensaje informativo si no hay detalles
        Row infoRow = sheet.createRow(1);
        infoRow.createCell(0).setCellValue("Información de productos vendidos no disponible");

        // Ajustar columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Exporta ventas a CSV
     */
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
            csv.append(escaparCSV(venta.getMetodoPago())).append(",");
            csv.append(escaparCSV(venta.getEstado())).append(",");
            csv.append(escaparCSV(venta.getObservaciones()));
            csv.append("\n");
        }

        logger.info("Exportación CSV de {} ventas completada", ventas.size());
        return csv.toString().getBytes();
    }

    // Métodos auxiliares para estilos (reutilizando algunos del servicio anterior)
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