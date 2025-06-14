package informviva.gest.service.importacion;

import informviva.gest.exception.ImportacionException;
import informviva.gest.util.ImportacionConstants;
import informviva.gest.util.MensajesConstantes;
import informviva.gest.util.ValidacionConstantes;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Componente especializado en validar archivos de importación.
 * Centraliza toda la lógica de validación y proporciona feedback detallado.
 *
 * @author Roberto Rivas
 * @version 1.0
 */
@Component
public class ImportacionValidador {

    private static final Logger logger = LoggerFactory.getLogger(ImportacionValidador.class);

    private final Pattern emailPattern = Pattern.compile(ImportacionConstants.PATRON_EMAIL);
    private final Pattern rutPattern = Pattern.compile(ImportacionConstants.PATRON_RUT);
    private final Pattern telefonoPattern = Pattern.compile(ImportacionConstants.PATRON_TELEFONO);

    /**
     * Valida completamente un archivo de importación.
     *
     * @param archivo El archivo a validar
     * @param tipoEntidad El tipo de entidad que se va a importar
     * @return Mapa con resultado de validación y detalles
     */
    public Map<String, Object> validarArchivo(MultipartFile archivo, String tipoEntidad) {
        logger.debug("Iniciando validación de archivo: {} para tipo: {}",
                archivo.getOriginalFilename(), tipoEntidad);

        Map<String, Object> resultado = new HashMap<>();
        List<String> errores = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();

        try {
            // Validaciones básicas
            validarArchivoBasico(archivo, errores);

            if (!errores.isEmpty()) {
                return construirResultadoValidacion(false, errores, advertencias, archivo);
            }

            // Validación específica por formato
            String extension = ImportacionConstants.obtenerExtensionArchivo(archivo.getOriginalFilename());

            if (ImportacionConstants.FORMATO_CSV.equals(extension)) {
                validarArchivoCSV(archivo, tipoEntidad, errores, advertencias);
            } else if (ImportacionConstants.FORMATO_EXCEL_XLSX.equals(extension) ||
                    ImportacionConstants.FORMATO_EXCEL_XLS.equals(extension)) {
                validarArchivoExcel(archivo, tipoEntidad, errores, advertencias);
            }

            boolean esValido = errores.isEmpty();
            resultado = construirResultadoValidacion(esValido, errores, advertencias, archivo);

            logger.debug("Validación completada - Válido: {}, Errores: {}, Advertencias: {}",
                    esValido, errores.size(), advertencias.size());

        } catch (Exception e) {
            logger.error("Error durante validación del archivo", e);
            errores.add("Error inesperado durante la validación: " + e.getMessage());
            resultado = construirResultadoValidacion(false, errores, advertencias, archivo);
        }

        return resultado;
    }

    /**
     * Verifica si un formato de archivo es soportado.
     */
    public boolean esFormatoSoportado(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            return false;
        }

        String extension = ImportacionConstants.obtenerExtensionArchivo(nombreArchivo);
        return ImportacionConstants.esFormatoSoportado(extension);
    }

    /**
     * Validaciones básicas del archivo.
     */
    private void validarArchivoBasico(MultipartFile archivo, List<String> errores) {
        if (archivo.isEmpty()) {
            errores.add(MensajesConstantes.ERROR_ARCHIVO_VACIO);
            return;
        }

        if (archivo.getSize() > ImportacionConstants.TAMAÑO_MAXIMO_ARCHIVO) {
            errores.add(MensajesConstantes.ERROR_ARCHIVO_MUY_GRANDE);
        }

        String nombreArchivo = archivo.getOriginalFilename();
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            errores.add("El nombre del archivo no es válido");
            return;
        }

        if (!esFormatoSoportado(nombreArchivo)) {
            errores.add(MensajesConstantes.ERROR_FORMATO_NO_SOPORTADO);
        }
    }

    /**
     * Validación específica para archivos CSV.
     */
    private void validarArchivoCSV(MultipartFile archivo, String tipoEntidad,
                                   List<String> errores, List<String> advertencias) throws IOException {

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(archivo.getInputStream(), ImportacionConstants.ENCODING_UTF8))) {

            String primeraLinea = reader.readLine();
            if (primeraLinea == null || primeraLinea.trim().isEmpty()) {
                errores.add("El archivo CSV está vacío");
                return;
            }

            // Validar encabezados
            String[] encabezados = primeraLinea.split(ImportacionConstants.SEPARADOR_CSV);
            validarEncabezados(encabezados, tipoEntidad, errores, advertencias);

            // Validar contenido (muestra)
            validarContenidoCSV(reader, encabezados.length, errores, advertencias);

        } catch (IOException e) {
            logger.error("Error leyendo archivo CSV", e);
            errores.add("Error leyendo el archivo CSV: " + e.getMessage());
        }
    }

    /**
     * Validación específica para archivos Excel.
     */
    private void validarArchivoExcel(MultipartFile archivo, String tipoEntidad,
                                     List<String> errores, List<String> advertencias) throws IOException {

        try (Workbook workbook = new XSSFWorkbook(archivo.getInputStream())) {

            if (workbook.getNumberOfSheets() == 0) {
                errores.add("El archivo Excel no contiene hojas");
                return;
            }

            Sheet sheet = workbook.getSheetAt(ImportacionConstants.PRIMERA_HOJA_EXCEL);

            if (sheet.getPhysicalNumberOfRows() == 0) {
                errores.add("La hoja de Excel está vacía");
                return;
            }

            // Validar encabezados
            Row primeraFila = sheet.getRow(ImportacionConstants.INDICE_PRIMERA_FILA);
            if (primeraFila == null) {
                errores.add("No se encontraron encabezados en el archivo");
                return;
            }

            String[] encabezados = extraerEncabezadosExcel(primeraFila);
            validarEncabezados(encabezados, tipoEntidad, errores, advertencias);

            // Validar contenido (muestra)
            validarContenidoExcel(sheet, encabezados.length, errores, advertencias);

        } catch (Exception e) {
            logger.error("Error leyendo archivo Excel", e);
            errores.add("Error leyendo el archivo Excel: " + e.getMessage());
        }
    }

    /**
     * Valida los encabezados del archivo contra las columnas requeridas.
     */
    private void validarEncabezados(String[] encabezados, String tipoEntidad,
                                    List<String> errores, List<String> advertencias) {

        List<String> columnasRequeridas = ImportacionConstants.obtenerColumnasRequeridas(tipoEntidad);
        Set<String> encabezadosArchivo = new HashSet<>();

        // Normalizar encabezados del archivo
        for (String encabezado : encabezados) {
            if (encabezado != null) {
                encabezadosArchivo.add(encabezado.toLowerCase().trim().replace("\"", ""));
            }
        }

        // Verificar columnas faltantes
        List<String> columnasFaltantes = new ArrayList<>();
        for (String columnaRequerida : columnasRequeridas) {
            if (!encabezadosArchivo.contains(columnaRequerida.toLowerCase())) {
                columnasFaltantes.add(columnaRequerida);
            }
        }

        if (!columnasFaltantes.isEmpty()) {
            errores.add(String.format("Columnas faltantes: %s", String.join(", ", columnasFaltantes)));
        }

        // Verificar columnas adicionales
        List<String> columnasAdicionales = new ArrayList<>();
        for (String encabezado : encabezadosArchivo) {
            boolean encontrada = columnasRequeridas.stream()
                    .anyMatch(req -> req.toLowerCase().equals(encabezado.toLowerCase()));
            if (!encontrada && !encabezado.isEmpty()) {
                columnasAdicionales.add(encabezado);
            }
        }

        if (!columnasAdicionales.isEmpty()) {
            advertencias.add(String.format("Columnas adicionales encontradas (serán ignoradas): %s",
                    String.join(", ", columnasAdicionales)));
        }
    }

    /**
     * Valida una muestra del contenido del archivo CSV.
     */
    private void validarContenidoCSV(BufferedReader reader, int numeroColumnasEsperadas,
                                     List<String> errores, List<String> advertencias) throws IOException {

        String linea;
        int numeroFila = 2; // Empezamos desde la segunda fila (después de encabezados)
        int filasValidadas = 0;
        int maxFilasValidar = 10; // Validar solo una muestra

        while ((linea = reader.readLine()) != null && filasValidadas < maxFilasValidar) {
            if (linea.trim().isEmpty()) {
                numeroFila++;
                continue;
            }

            String[] valores = linea.split(ImportacionConstants.SEPARADOR_CSV);

            if (valores.length != numeroColumnasEsperadas) {
                advertencias.add(String.format("Fila %d: número incorrecto de columnas (esperadas: %d, encontradas: %d)",
                        numeroFila, numeroColumnasEsperadas, valores.length));
            }

            filasValidadas++;
            numeroFila++;
        }

        if (filasValidadas == 0) {
            errores.add("El archivo no contiene datos, solo encabezados");
        }
    }

    /**
     * Valida una muestra del contenido del archivo Excel.
     */
    private void validarContenidoExcel(Sheet sheet, int numeroColumnasEsperadas,
                                       List<String> errores, List<String> advertencias) {

        int ultimaFila = Math.min(sheet.getLastRowNum(), 10); // Validar solo primeras 10 filas
        int filasConDatos = 0;

        for (int i = 1; i <= ultimaFila; i++) { // Empezar desde fila 1 (después de encabezados)
            Row fila = sheet.getRow(i);

            if (fila == null || esFilaVaciaExcel(fila)) {
                continue;
            }

            filasConDatos++;

            int columnasConDatos = fila.getLastCellNum();
            if (columnasConDatos != numeroColumnasEsperadas) {
                advertencias.add(String.format("Fila %d: número incorrecto de columnas con datos", i + 1));
            }
        }

        if (filasConDatos == 0) {
            errores.add("El archivo no contiene datos, solo encabezados");
        }
    }

    /**
     * Extrae los encabezados de una fila de Excel.
     */
    private String[] extraerEncabezadosExcel(Row fila) {
        int numeroColumnas = fila.getLastCellNum();
        String[] encabezados = new String[numeroColumnas];

        for (int i = 0; i < numeroColumnas; i++) {
            if (fila.getCell(i) != null) {
                encabezados[i] = fila.getCell(i).toString().trim();
            } else {
                encabezados[i] = "";
            }
        }

        return encabezados;
    }

    /**
     * Verifica si una fila de Excel está vacía.
     */
    private boolean esFilaVaciaExcel(Row fila) {
        if (fila == null) {
            return true;
        }

        for (int i = fila.getFirstCellNum(); i < fila.getLastCellNum(); i++) {
            if (fila.getCell(i) != null && !fila.getCell(i).toString().trim().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Construye el resultado de la validación.
     */
    private Map<String, Object> construirResultadoValidacion(boolean esValido, List<String> errores,
                                                             List<String> advertencias, MultipartFile archivo) {
        Map<String, Object> resultado = new HashMap<>();

        resultado.put("valido", esValido);
        resultado.put("errores", errores);
        resultado.put("advertencias", advertencias);
        resultado.put("nombreArchivo", archivo.getOriginalFilename());
        resultado.put("tamañoArchivo", archivo.getSize());
        resultado.put("tipoArchivo", archivo.getContentType());

        return resultado;
    }
}