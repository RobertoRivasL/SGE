package informviva.gest.service.impl;

// ===================================================================
// SERVICIO DE STREAMING PARA ARCHIVOS GRANDES - StreamingExportService.java
// ===================================================================


import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * Servicio para manejar exportaciones de archivos grandes mediante streaming
 * Optimizado para manejar grandes volúmenes de datos sin saturar la memoria
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Service
public class StreamingExportService {

    private static final Logger logger = LoggerFactory.getLogger(StreamingExportService.class);

    @Value("${exportacion.streaming.batch-size:1000}")
    private int batchSize;

    @Value("${exportacion.streaming.excel-window-size:100}")
    private int excelWindowSize;

    /**
     * Exporta datos a Excel usando streaming para manejar grandes volúmenes
     */
    public void exportarExcelStreaming(HttpServletResponse response, String fileName,
                                       String[] headers, Iterator<Object[]> dataIterator) throws IOException {

        logger.info("Iniciando exportación Excel streaming: {}", fileName);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(excelWindowSize)) {
            // Configurar compresión temporal
            workbook.setCompressTempFiles(true);

            Sheet sheet = workbook.createSheet("Datos");

            // Crear estilos
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Procesar datos en lotes
            int rowNumber = 1;
            int totalProcessed = 0;

            while (dataIterator.hasNext()) {
                for (int i = 0; i < batchSize && dataIterator.hasNext(); i++) {
                    Object[] rowData = dataIterator.next();
                    Row row = sheet.createRow(rowNumber++);

                    for (int j = 0; j < rowData.length && j < headers.length; j++) {
                        Cell cell = row.createCell(j);
                        setCellValue(cell, rowData[j]);
                    }

                    totalProcessed++;
                }

                // Log progreso cada lote
                if (totalProcessed % (batchSize * 10) == 0) {
                    logger.info("Procesados {} registros en exportación Excel", totalProcessed);
                }
            }

            // Ajustar ancho de columnas (solo para las primeras columnas para eficiencia)
            int maxColumns = Math.min(headers.length, 10);
            for (int i = 0; i < maxColumns; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
            logger.info("Exportación Excel streaming completada: {} registros", totalProcessed);

        } catch (Exception e) {
            logger.error("Error en exportación Excel streaming: {}", e.getMessage());
            throw new IOException("Error en exportación streaming", e);
        }
    }

    /**
     * Exporta datos a CSV usando streaming
     */
    public void exportarCSVStreaming(HttpServletResponse response, String fileName,
                                     String[] headers, Iterator<Object[]> dataIterator) throws IOException {

        logger.info("Iniciando exportación CSV streaming: {}", fileName);

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        try (PrintWriter writer = response.getWriter()) {
            // Escribir BOM para UTF-8
            writer.print('\ufeff');

            // Escribir encabezados
            for (int i = 0; i < headers.length; i++) {
                if (i > 0) writer.print(",");
                writer.print(escapeCSV(headers[i]));
            }
            writer.println();

            // Procesar datos en lotes
            int totalProcessed = 0;

            while (dataIterator.hasNext()) {
                for (int i = 0; i < batchSize && dataIterator.hasNext(); i++) {
                    Object[] rowData = dataIterator.next();

                    for (int j = 0; j < rowData.length && j < headers.length; j++) {
                        if (j > 0) writer.print(",");
                        writer.print(escapeCSV(String.valueOf(rowData[j])));
                    }
                    writer.println();

                    totalProcessed++;
                }

                // Flush cada lote para mejorar rendimiento
                writer.flush();

                if (totalProcessed % (batchSize * 10) == 0) {
                    logger.info("Procesados {} registros en exportación CSV", totalProcessed);
                }
            }

            logger.info("Exportación CSV streaming completada: {} registros", totalProcessed);

        } catch (Exception e) {
            logger.error("Error en exportación CSV streaming: {}", e.getMessage());
            throw new IOException("Error en exportación streaming", e);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "";

        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }

        return value;
    }
}