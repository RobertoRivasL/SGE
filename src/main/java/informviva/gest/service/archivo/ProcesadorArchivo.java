package informviva.gest.service.archivo;


import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Servicio especializado en procesamiento de archivos
 *
 * @author Roberto Rivas
 * @version 2.1
 */
@Service
public class ProcesadorArchivo {

    private static final Logger logger = LoggerFactory.getLogger(ProcesadorArchivo.class);

    /**
     * Procesa un archivo y devuelve los datos como lista de mapas
     */
    public List<Map<String, Object>> procesarArchivo(MultipartFile archivo) throws IOException {
        String nombreArchivo = archivo.getOriginalFilename();
        if (nombreArchivo == null) {
            throw new IllegalArgumentException("Nombre de archivo no válido");
        }

        String extension = obtenerExtension(nombreArchivo).toLowerCase();

        switch (extension) {
            case "csv":
                return procesarCSV(archivo);
            case "xlsx":
            case "xls":
                return procesarExcel(archivo);
            default:
                throw new IllegalArgumentException("Formato de archivo no soportado: " + extension);
        }
    }

    /**
     * Procesa archivos CSV
     */
    private List<Map<String, Object>> procesarCSV(MultipartFile archivo) throws IOException {
        List<Map<String, Object>> datos = new ArrayList<>();

        try (Scanner scanner = new Scanner(new InputStreamReader(archivo.getInputStream(), "UTF-8"))) {
            if (!scanner.hasNextLine()) {
                return datos;
            }

            // Leer y procesar header
            String headerLine = scanner.nextLine();
            List<String> headers = procesarLineaCSV(headerLine);

            // Limpiar headers
            headers = headers.stream()
                    .map(String::trim)
                    .map(h -> h.replace("\"", ""))
                    .filter(h -> !h.isEmpty())
                    .toList();

            if (headers.isEmpty()) {
                throw new IllegalArgumentException("El archivo CSV no tiene headers válidos");
            }

            // Leer datos
            int numeroLinea = 1;
            while (scanner.hasNextLine()) {
                numeroLinea++;
                String line = scanner.nextLine().trim();

                if (line.isEmpty()) {
                    continue; // Saltar líneas vacías
                }

                try {
                    List<String> valores = procesarLineaCSV(line);
                    Map<String, Object> fila = new HashMap<>();

                    // Mapear valores a headers
                    for (int i = 0; i < headers.size(); i++) {
                        String valor = (i < valores.size()) ? valores.get(i).trim() : "";
                        valor = valor.replace("\"", ""); // Remover comillas
                        fila.put(headers.get(i), valor);
                    }

                    datos.add(fila);

                } catch (Exception e) {
                    logger.warn("Error procesando línea {} del CSV: {}", numeroLinea, e.getMessage());
                    // Continuar con la siguiente línea en lugar de fallar completamente
                }
            }
        }

        logger.info("CSV procesado exitosamente: {} filas, {} columnas", datos.size(),
                datos.isEmpty() ? 0 : datos.get(0).size());

        return datos;
    }

    /**
     * Procesa una línea CSV respetando comillas y comas
     */
    private List<String> procesarLineaCSV(String linea) {
        List<String> valores = new ArrayList<>();
        StringBuilder valorActual = new StringBuilder();
        boolean dentroComillas = false;

        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);

            if (c == '"') {
                // Manejar comillas dobles escapadas
                if (i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    valorActual.append('"');
                    i++; // Saltar la siguiente comilla
                } else {
                    dentroComillas = !dentroComillas;
                }
            } else if (c == ',' && !dentroComillas) {
                valores.add(valorActual.toString());
                valorActual.setLength(0);
            } else {
                valorActual.append(c);
            }
        }

        // Agregar el último valor
        valores.add(valorActual.toString());

        return valores;
    }

    /**
     * Procesa archivos Excel
     */
    private List<Map<String, Object>> procesarExcel(MultipartFile archivo) throws IOException {
        List<Map<String, Object>> datos = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(archivo.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); // Usar la primera hoja

            if (sheet.getPhysicalNumberOfRows() == 0) {
                return datos;
            }

            // Leer header
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("El archivo Excel no tiene headers");
            }

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                String header = obtenerValorCelda(cell).trim();
                if (!header.isEmpty()) {
                    headers.add(header);
                }
            }

            if (headers.isEmpty()) {
                throw new IllegalArgumentException("El archivo Excel no tiene headers válidos");
            }

            // Leer datos
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue; // Saltar filas vacías
                }

                Map<String, Object> fila = new HashMap<>();
                boolean filaVacia = true;

                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    String valor = cell != null ? obtenerValorCelda(cell) : "";

                    if (!valor.trim().isEmpty()) {
                        filaVacia = false;
                    }

                    fila.put(headers.get(j), valor);
                }

                // Solo agregar filas que no estén completamente vacías
                if (!filaVacia) {
                    datos.add(fila);
                }
            }
        }

        logger.info("Excel procesado exitosamente: {} filas, {} columnas", datos.size(),
                datos.isEmpty() ? 0 : datos.get(0).size());

        return datos;
    }

    /**
     * Convierte el valor de una celda Excel a String
     */
    private String obtenerValorCelda(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Verificar si es un número entero para evitar .0
                    double valor = cell.getNumericCellValue();
                    if (valor == Math.floor(valor)) {
                        return String.valueOf((long) valor);
                    } else {
                        return String.valueOf(valor);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            case BLANK:
                return "";
            default:
                return cell.toString().trim();
        }
    }

    private String obtenerExtension(String nombreArchivo) {
        int ultimoPunto = nombreArchivo.lastIndexOf('.');
        return ultimoPunto > 0 ? nombreArchivo.substring(ultimoPunto + 1) : "";
    }
}