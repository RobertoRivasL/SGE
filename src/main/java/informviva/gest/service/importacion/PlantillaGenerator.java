package informviva.gest.service.importacion;


import informviva.gest.exception.ImportacionException;
import informviva.gest.util.ImportacionConstants;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Componente especializado en generar plantillas de importación.
 * Centraliza la lógica de creación de plantillas para diferentes formatos.
 *
 * @author Roberto Rivas
 * @version 1.0
 */
@Component
public class PlantillaGenerator {

    private static final Logger logger = LoggerFactory.getLogger(PlantillaGenerator.class);

    // Datos de ejemplo para las plantillas
    private static final Map<String, List<String[]>> DATOS_EJEMPLO = Map.of(
            ImportacionConstants.TIPO_CLIENTE, Arrays.asList(
                    new String[]{"12345678-9", "Juan", "Pérez", "juan.perez@email.com", "+56912345678", "Av. Principal 123", "premium"},
                    new String[]{"87654321-0", "María", "González", "maria.gonzalez@email.com", "+56987654321", "Calle Secundaria 456", "regular"}
            ),
            ImportacionConstants.TIPO_PRODUCTO, Arrays.asList(
                    new String[]{"PROD001", "Producto Ejemplo 1", "Descripción del producto 1", "15990", "100", "Marca A", "Modelo X"},
                    new String[]{"PROD002", "Producto Ejemplo 2", "Descripción del producto 2", "25990", "50", "Marca B", "Modelo Y"}
            ),
            ImportacionConstants.TIPO_USUARIO, Arrays.asList(
                    new String[]{"usuario1", "password123", "Admin", "Sistema", "admin@empresa.com", "ADMIN", "true"},
                    new String[]{"usuario2", "password456", "Operador", "Ventas", "operador@empresa.com", "USER", "true"}
            )
    );

    /**
     * Genera una plantilla de importación en el formato especificado.
     *
     * @param tipoEntidad El tipo de entidad para la plantilla
     * @param formato El formato de la plantilla (CSV o EXCEL)
     * @return Array de bytes con la plantilla generada
     * @throws ImportacionException Si hay errores generando la plantilla
     */
    public byte[] generarPlantilla(String tipoEntidad, String formato) {
        logger.debug("Generando plantilla para tipo: {} en formato: {}", tipoEntidad, formato);

        try {
            if (ImportacionConstants.FORMATO_CSV.equalsIgnoreCase(formato)) {
                return generarPlantillaCSV(tipoEntidad);
            } else if (ImportacionConstants.FORMATO_EXCEL_XLSX.equalsIgnoreCase(formato) ||
                    "excel".equalsIgnoreCase(formato)) {
                return generarPlantillaExcel(tipoEntidad);
            } else {
                throw new ImportacionException("Formato de plantilla no soportado: " + formato);
            }

        } catch (Exception e) {
            logger.error("Error generando plantilla para {} en formato {}: {}",
                    tipoEntidad, formato, e.getMessage());
            throw new ImportacionException("Error generando plantilla", e);
        }
    }

    /**
     * Genera una plantilla en formato CSV.
     */
    private byte[] generarPlantillaCSV(String tipoEntidad) {
        StringBuilder csv = new StringBuilder();

        // Agregar encabezados
        List<String> columnas = ImportacionConstants.obtenerColumnasRequeridas(tipoEntidad);
        csv.append(String.join(ImportacionConstants.SEPARADOR_CSV, columnas));
        csv.append("\n");

        // Agregar línea de comentario con instrucciones
        csv.append("# Plantilla de importación para ").append(tipoEntidad.toUpperCase());
        csv.append(" - Generada el ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        csv.append("\n");

        // Agregar línea de ejemplo de formato
        csv.append("# Formato: ").append(String.join(", ", columnas));
        csv.append("\n");

        // Agregar datos de ejemplo
        List<String[]> datosEjemplo = DATOS_EJEMPLO.get(tipoEntidad);
        if (datosEjemplo != null) {
            for (String[] fila : datosEjemplo) {
                csv.append(String.join(ImportacionConstants.SEPARADOR_CSV, fila));
                csv.append("\n");
            }
        }

        return csv.toString().getBytes();
    }

    /**
     * Genera una plantilla en formato Excel.
     */
    private byte[] generarPlantillaExcel(String tipoEntidad) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Crear hoja principal
            Sheet sheet = workbook.createSheet("Plantilla " + tipoEntidad.toUpperCase());

            // Crear hoja de instrucciones
            Sheet instructionsSheet = workbook.createSheet("Instrucciones");

            // Configurar plantilla principal
            configurarHojaPrincipal(sheet, tipoEntidad, workbook);

            // Configurar hoja de instrucciones
            configurarHojaInstrucciones(instructionsSheet, tipoEntidad, workbook);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Configura la hoja principal con datos y formato.
     */
    private void configurarHojaPrincipal(Sheet sheet, String tipoEntidad, Workbook workbook) {
        List<String> columnas = ImportacionConstants.obtenerColumnasRequeridas(tipoEntidad);

        // Crear estilos
        CellStyle estiloEncabezado = crearEstiloEncabezado(workbook);
        CellStyle estiloEjemplo = crearEstiloEjemplo(workbook);

        // Crear fila de encabezados
        Row filaEncabezados = sheet.createRow(ImportacionConstants.INDICE_PRIMERA_FILA);

        for (int i = 0; i < columnas.size(); i++) {
            Cell celda = filaEncabezados.createCell(i);
            celda.setCellValue(columnas.get(i));
            celda.setCellStyle(estiloEncabezado);

            // Auto-ajustar ancho de columna
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i), 3000));
        }

        // Agregar datos de ejemplo
        List<String[]> datosEjemplo = DATOS_EJEMPLO.get(tipoEntidad);
        if (datosEjemplo != null) {
            for (int filaIndex = 0; filaIndex < datosEjemplo.size(); filaIndex++) {
                Row fila = sheet.createRow(filaIndex + 1);
                String[] datosFila = datosEjemplo.get(filaIndex);

                for (int colIndex = 0; colIndex < datosFila.length && colIndex < columnas.size(); colIndex++) {
                    Cell celda = fila.createCell(colIndex);
                    celda.setCellValue(datosFila[colIndex]);
                    celda.setCellStyle(estiloEjemplo);
                }
            }
        }

        // Congelar primera fila
        sheet.createFreezePane(0, 1);
    }

    /**
     * Configura la hoja de instrucciones.
     */
    private void configurarHojaInstrucciones(Sheet sheet, String tipoEntidad, Workbook workbook) {
        CellStyle estiloTitulo = crearEstiloTitulo(workbook);
        CellStyle estiloTexto = crearEstiloTexto(workbook);

        int filaActual = 0;

        // Título
        Row fila = sheet.createRow(filaActual++);
        Cell celda = fila.createCell(0);
        celda.setCellValue("INSTRUCCIONES DE IMPORTACIÓN - " + tipoEntidad.toUpperCase());
        celda.setCellStyle(estiloTitulo);

        filaActual++; // Línea en blanco

        // Instrucciones generales
        String[] instrucciones = obtenerInstrucciones(tipoEntidad);
        for (String instruccion : instrucciones) {
            fila = sheet.createRow(filaActual++);
            celda = fila.createCell(0);
            celda.setCellValue(instruccion);
            celda.setCellStyle(estiloTexto);
        }

        // Auto-ajustar columna
        sheet.autoSizeColumn(0);
        sheet.setColumnWidth(0, Math.max(sheet.getColumnWidth(0), 8000));
    }

    /**
     * Obtiene las instrucciones específicas para cada tipo de entidad.
     */
    private String[] obtenerInstrucciones(String tipoEntidad) {
        String[] instruccionesComunes = {
                "1. Complete todos los campos requeridos",
                "2. No modifique los nombres de las columnas",
                "3. Mantenga el formato de los datos como se muestra en los ejemplos",
                "4. Elimine las filas de ejemplo antes de importar sus datos",
                "5. Guarde el archivo antes de importar",
                "",
                "COLUMNAS REQUERIDAS:"
        };

        List<String> columnas = ImportacionConstants.obtenerColumnasRequeridas(tipoEntidad);
        String[] instruccionesColumnas = new String[columnas.size()];
        for (int i = 0; i < columnas.size(); i++) {
            instruccionesColumnas[i] = "- " + columnas.get(i) + ": " + obtenerDescripcionColumna(tipoEntidad, columnas.get(i));
        }

        // Combinar instrucciones
        String[] todasInstrucciones = new String[instruccionesComunes.length + instruccionesColumnas.length];
        System.arraycopy(instruccionesComunes, 0, todasInstrucciones, 0, instruccionesComunes.length);
        System.arraycopy(instruccionesColumnas, 0, todasInstrucciones, instruccionesComunes.length, instruccionesColumnas.length);

        return todasInstrucciones;
    }

    /**
     * Obtiene la descripción de una columna específica.
     */
    private String obtenerDescripcionColumna(String tipoEntidad, String columna) {
        Map<String, Map<String, String>> descripciones = Map.of(
                ImportacionConstants.TIPO_CLIENTE, Map.of(
                        "rut", "RUT del cliente (formato: 12345678-9)",
                        "nombre", "Nombre del cliente",
                        "apellido", "Apellido del cliente",
                        "email", "Correo electrónico válido",
                        "telefono", "Teléfono de contacto",
                        "direccion", "Dirección completa",
                        "categoria", "Categoría del cliente (premium, regular, etc.)"
                ),
                ImportacionConstants.TIPO_PRODUCTO, Map.of(
                        "codigo", "Código único del producto",
                        "nombre", "Nombre del producto",
                        "descripcion", "Descripción detallada",
                        "precio", "Precio en pesos (sin puntos ni comas)",
                        "stock", "Cantidad en stock (número entero)",
                        "marca", "Marca del producto",
                        "modelo", "Modelo del producto"
                ),
                ImportacionConstants.TIPO_USUARIO, Map.of(
                        "username", "Nombre de usuario único",
                        "password", "Contraseña del usuario",
                        "nombre", "Nombre del usuario",
                        "apellido", "Apellido del usuario",
                        "email", "Correo electrónico válido",
                        "roles", "Roles del usuario (ADMIN, USER, etc.)",
                        "activo", "Estado del usuario (true/false)"
                )
        );

        return descripciones.getOrDefault(tipoEntidad, Map.of()).getOrDefault(columna, "Campo requerido");
    }

    // Métodos para crear estilos de Excel
    private CellStyle crearEstiloEncabezado(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fuente = workbook.createFont();
        fuente.setBold(true);
        fuente.setColor(IndexedColors.WHITE.getIndex());
        estilo.setFont(fuente);
        estilo.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        return estilo;
    }

    private CellStyle crearEstiloEjemplo(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        estilo.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        return estilo;
    }

    private CellStyle crearEstiloTitulo(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fuente = workbook.createFont();
        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 14);
        estilo.setFont(fuente);
        return estilo;
    }

    private CellStyle crearEstiloTexto(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        estilo.setWrapText(true);
        return estilo;
    }
}