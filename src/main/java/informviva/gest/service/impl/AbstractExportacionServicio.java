package informviva.gest.service.impl;

/**
 * @author Roberto Rivas
 * @version 2.0
 *
 */


import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractExportacionServicio {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractExportacionServicio.class);

    protected CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return headerStyle;
    }

    // Otros métodos comunes pueden ir aquí
}