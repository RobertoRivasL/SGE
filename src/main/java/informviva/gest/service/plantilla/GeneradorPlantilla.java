package informviva.gest.service.plantilla;

// ============================================================================


import informviva.gest.config.ImportacionConfig;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Servicio para generar plantillas de importación
 *
 * @author Roberto Rivas
 * @version 2.1
 */
@Service
public class GeneradorPlantilla {

    private static final Logger logger = LoggerFactory.getLogger(GeneradorPlantilla.class);

    @Autowired
    private ImportacionConfig configuracion;

    /**
     * Genera una plantilla de importación
     */
    public byte[] generarPlantilla(String tipoEntidad, String formato) {
        try {
            List<String> columnas = configuracion.getConfiguracionEntidad(tipoEntidad).getCamposObligatorios();
            List<List<String>> datosEjemplo = obtenerDatosEjemplo(tipoEntidad);

            if ("CSV".equalsIgnoreCase(formato)) {
                return generarPlantillaCSV(columnas, datosEjemplo);
            } else {
                return generarPlantillaExcel(columnas, datosEjemplo, tipoEntidad);
            }
        } catch (Exception e) {
            logger.error("Error generando plantilla para {}: {}", tipoEntidad, e.getMessage());
            return new byte[0];
        }
    }

    /**
     * Genera plantilla en formato CSV
     */
    private byte[] generarPlantillaCSV(List<String> columnas, List<List<String>> datosEjemplo) {
        StringBuilder csv = new StringBuilder();

        // Header
        csv.append(String.join(",", columnas)).append("\n");

        // Comentarios explicativos
        csv.append("# Esta es una plantilla de ejemplo\n");
        csv.append("# Elimine esta línea y las siguientes antes de importar\n");
        csv.append("# Asegúrese de que los datos cumplan con las validaciones\n");

        // Datos de ejemplo
        for (List<String> fila : datosEjemplo) {
            csv.append(String.join(",", fila)).append("\n");
        }

        return csv.toString().getBytes();
    }

    /**
     * Genera plantilla en formato Excel
     */
    private byte[] generarPlantillaExcel(List<String> columnas, List<List<String>> datosEjemplo, String tipoEntidad)
            throws IOException {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Crear hoja principal
            Sheet sheet = workbook.createSheet("Plantilla " + capitalizarPrimeraLetra(tipoEntidad));

            // Crear estilos
            CellStyle headerStyle = crearEstiloHeader(workbook);
            CellStyle exampleStyle = crearEstiloEjemplo(workbook);
            CellStyle instructionStyle = crearEstiloInstrucciones(workbook);

            int rowIndex = 0;

            // Título y fecha
            Row titleRow = sheet.createRow(rowIndex++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Plantilla de Importación - " + capitalizarPrimeraLetra(tipoEntidad));
            titleCell.setCellStyle(headerStyle);

            Row dateRow = sheet.createRow(rowIndex++);
            Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue("Generada el: " + java.time.LocalDateTime.now().toString());
            dateCell.setCellStyle(instructionStyle);

            // Fila vacía
            rowIndex++;

            // Instrucciones
            String[] instrucciones = {
                    "INSTRUCCIONES:",
                    "1. Complete los datos en las columnas correspondientes",
                    "2. No modifique los nombres de las columnas",
                    "3. Elimine las filas de ejemplo antes de importar",
                    "4. Los campos marcados con * son obligatorios",
                    "5. Respete los formatos indicados en los ejemplos"
            };

            for (String instruccion : instrucciones) {
                Row instrRow = sheet.createRow(rowIndex++);
                Cell instrCell = instrRow.createCell(0);
                instrCell.setCellValue(instruccion);
                instrCell.setCellStyle(instructionStyle);
            }

            // Fila vacía
            rowIndex++;

            // Headers
            Row headerRow = sheet.createRow(rowIndex++);
            for (int i = 0; i < columnas.size(); i++) {
                Cell cell = headerRow.createCell(i);
                String columna = columnas.get(i);
                cell.setCellValue(esColumnaObligatoria(columna, tipoEntidad) ? columna + " *" : columna);
                cell.setCellStyle(headerStyle);
            }

            // Datos de ejemplo
            for (List<String> fila : datosEjemplo) {
                Row row = sheet.createRow(rowIndex++);
                for (int j = 0; j < fila.size() && j < columnas.size(); j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(fila.get(j));
                    cell.setCellStyle(exampleStyle);
                }
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < columnas.size(); i++) {
                sheet.autoSizeColumn(i);
                // Asegurar un ancho mínimo
                if (sheet.getColumnWidth(i) < 3000) {
                    sheet.setColumnWidth(i, 3000);
                }
            }

            // Crear hoja de validaciones
            crearHojaValidaciones(workbook, tipoEntidad);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Crea una hoja adicional con información de validaciones
     */
    private void crearHojaValidaciones(Workbook workbook, String tipoEntidad) {
        Sheet validationSheet = workbook.createSheet("Validaciones");

        CellStyle headerStyle = crearEstiloHeader(workbook);
        CellStyle normalStyle = workbook.createCellStyle();

        int rowIndex = 0;

        // Título
        Row titleRow = validationSheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Reglas de Validación - " + capitalizarPrimeraLetra(tipoEntidad));
        titleCell.setCellStyle(headerStyle);

        rowIndex++; // Fila vacía

        // Obtener reglas específicas por tipo
        Map<String, String> reglas = obtenerReglasValidacion(tipoEntidad);

        for (Map.Entry<String, String> regla : reglas.entrySet()) {
            Row row = validationSheet.createRow(rowIndex++);

            Cell campoCell = row.createCell(0);
            campoCell.setCellValue(regla.getKey());
            campoCell.setCellStyle(headerStyle);

            Cell descripcionCell = row.createCell(1);
            descripcionCell.setCellValue(regla.getValue());
            descripcionCell.setCellStyle(normalStyle);
        }

        // Ajustar anchos
        validationSheet.autoSizeColumn(0);
        validationSheet.autoSizeColumn(1);
    }

    private Map<String, String> obtenerReglasValidacion(String tipoEntidad) {
        switch (tipoEntidad.toLowerCase()) {
            case "cliente":
                return Map.of(
                        "nombre", "Texto, máximo 50 caracteres",
                        "apellido", "Texto, máximo 50 caracteres",
                        "email", "Formato de email válido",
                        "rut", "RUT chileno válido con guión y dígito verificador",
                        "telefono", "Opcional, solo números y caracteres básicos",
                        "direccion", "Opcional, texto libre",
                        "categoria", "Opcional, texto libre"
                );
            case "producto":
                return Map.of(
                        "codigo", "Texto único, 3-20 caracteres, solo letras mayúsculas y números",
                        "nombre", "Texto, máximo 100 caracteres",
                        "descripcion", "Opcional, texto libre",
                        "precio", "Número decimal positivo, sin símbolos de moneda",
                        "stock", "Número entero, 0 o positivo",
                        "marca", "Opcional, texto",
                        "modelo", "Opcional, texto"
                );
            case "usuario":
                return Map.of(
                        "username", "3-20 caracteres, letras, números, puntos, guiones",
                        "password", "Mínimo 6 caracteres",
                        "nombre", "Texto, máximo 50 caracteres",
                        "apellido", "Texto, máximo 50 caracteres",
                        "email", "Formato de email válido",
                        "roles", "ADMIN, VENTAS, PRODUCTOS, GERENTE, USER (separados por ;)",
                        "activo", "true/false, sí/no, 1/0"
                );
            default:
                return Map.of();
        }
    }

    private CellStyle crearEstiloHeader(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle crearEstiloEjemplo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle crearEstiloInstrucciones(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setItalic(true);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFont(font);
        return style;
    }

    private List<List<String>> obtenerDatosEjemplo(String tipoEntidad) {
        switch (tipoEntidad.toLowerCase()) {
            case "cliente":
                return Arrays.asList(
                        Arrays.asList("Juan", "Pérez", "juan.perez@email.com", "12345678-9", "987654321", "Av. Principal 123", "VIP"),
                        Arrays.asList("María", "González", "maria.gonzalez@email.com", "98765432-1", "912345678", "Calle Secundaria 456", "Regular"),
                        Arrays.asList("Carlos", "Rodríguez", "carlos.rodriguez@email.com", "11111111-1", "956789123", "Pasaje Los Álamos 789", "Premium")
                );
            case "producto":
                return Arrays.asList(
                        Arrays.asList("PROD001", "Laptop Dell", "Laptop Dell Inspiron 15 de alta gama", "599990", "10", "Dell", "Inspiron 15"),
                        Arrays.asList("PROD002", "Mouse Logitech", "Mouse óptico inalámbrico", "25990", "50", "Logitech", "M220"),
                        Arrays.asList("PROD003", "Teclado Mecánico", "Teclado mecánico RGB para gaming", "89990", "25", "Corsair", "K70 RGB")
                );
            case "usuario":
                return Arrays.asList(
                        Arrays.asList("jperez", "password123", "Juan", "Pérez", "juan.perez@empresa.com", "VENTAS", "true"),
                        Arrays.asList("mgonzalez", "password456", "María", "González", "maria.gonzalez@empresa.com", "ADMIN;GERENTE", "true"),
                        Arrays.asList("crodriguez", "password789", "Carlos", "Rodríguez", "carlos.rodriguez@empresa.com", "PRODUCTOS", "false")
                );
            default:
                return Arrays.asList();
        }
    }

    private boolean esColumnaObligatoria(String columna, String tipoEntidad) {
        List<String> obligatorias = configuracion.getConfiguracionEntidad(tipoEntidad).getCamposObligatorios();
        return obligatorias.contains(columna);
    }

    private String capitalizarPrimeraLetra(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        return texto.substring(0, 1).toUpperCase() + texto.substring(1).toLowerCase();
    }
}