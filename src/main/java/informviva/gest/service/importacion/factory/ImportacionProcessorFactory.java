package informviva.gest.service.importacion.factory;


import informviva.gest.exception.ImportacionException;
import informviva.gest.service.importacion.ImportacionProcessor;
import informviva.gest.service.importacion.impl.CsvImportacionProcessor;
import informviva.gest.service.importacion.impl.ExcelImportacionProcessor;
import informviva.gest.util.ImportacionConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory para crear procesadores de importación específicos según el tipo de archivo.
 * Implementa el patrón Factory Method para desacoplar la creación de procesadores.
 *
 * @author Roberto Rivas
 * @version 1.0
 */
@Component
public class ImportacionProcessorFactory {

    private static final Logger logger = LoggerFactory.getLogger(ImportacionProcessorFactory.class);

    // Cache de procesadores para evitar crear instancias repetidas
    private final Map<String, ImportacionProcessor> processorCache = new ConcurrentHashMap<>();

    private final CsvImportacionProcessor csvProcessor;
    private final ExcelImportacionProcessor excelProcessor;

    @Autowired
    public ImportacionProcessorFactory(CsvImportacionProcessor csvProcessor,
                                       ExcelImportacionProcessor excelProcessor) {
        this.csvProcessor = csvProcessor;
        this.excelProcessor = excelProcessor;
        inicializarCache();
    }

    /**
     * Crea un procesador apropiado para el tipo de archivo.
     *
     * @param archivo El archivo a procesar
     * @return El procesador apropiado
     * @throws ImportacionException Si no se encuentra un procesador para el tipo de archivo
     */
    public ImportacionProcessor createProcessor(MultipartFile archivo) {
        if (archivo == null || archivo.getOriginalFilename() == null) {
            throw new ImportacionException("Archivo no válido para determinar el procesador");
        }

        String extension = ImportacionConstants.obtenerExtensionArchivo(archivo.getOriginalFilename());
        logger.debug("Creando procesador para archivo con extensión: {}", extension);

        ImportacionProcessor processor = processorCache.get(extension.toLowerCase());

        if (processor == null) {
            throw new ImportacionException(
                    String.format("No se encontró procesador para el tipo de archivo: %s. " +
                                    "Tipos soportados: %s",
                            extension,
                            String.join(", ", ImportacionConstants.FORMATOS_SOPORTADOS)));
        }

        // Verificación adicional
        if (!processor.puedeProcesar(archivo)) {
            throw new ImportacionException(
                    String.format("El procesador %s no puede procesar el archivo: %s",
                            processor.getClass().getSimpleName(),
                            archivo.getOriginalFilename()));
        }

        logger.debug("Procesador {} seleccionado para archivo: {}",
                processor.getClass().getSimpleName(),
                archivo.getOriginalFilename());

        return processor;
    }

    /**
     * Obtiene todos los procesadores disponibles.
     *
     * @return Lista de todos los procesadores
     */
    public List<ImportacionProcessor> getAllProcessors() {
        return Arrays.asList(csvProcessor, excelProcessor);
    }

    /**
     * Obtiene un procesador específico por tipo.
     *
     * @param tipo El tipo de procesador (csv, excel)
     * @return El procesador correspondiente
     * @throws ImportacionException Si no se encuentra el procesador
     */
    public ImportacionProcessor getProcessorByType(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new ImportacionException("Tipo de procesador no puede estar vacío");
        }

        ImportacionProcessor processor = processorCache.get(tipo.toLowerCase());

        if (processor == null) {
            throw new ImportacionException(
                    String.format("Procesador no encontrado para tipo: %s", tipo));
        }

        return processor;
    }

    /**
     * Verifica si existe un procesador para el tipo de archivo.
     *
     * @param archivo El archivo a verificar
     * @return true si existe un procesador, false en caso contrario
     */
    public boolean tieneProcessorPara(MultipartFile archivo) {
        if (archivo == null || archivo.getOriginalFilename() == null) {
            return false;
        }

        String extension = ImportacionConstants.obtenerExtensionArchivo(archivo.getOriginalFilename());
        return processorCache.containsKey(extension.toLowerCase());
    }

    /**
     * Obtiene información sobre todos los procesadores disponibles.
     *
     * @return Mapa con información de procesadores
     */
    public Map<String, Object> getProcessorInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("procesadores_disponibles", processorCache.keySet());
        info.put("total_procesadores", processorCache.size());
        info.put("tipos_soportados", ImportacionConstants.FORMATOS_SOPORTADOS);
        return info;
    }

    /**
     * Inicializa el cache de procesadores.
     */
    private void inicializarCache() {
        logger.info("Inicializando cache de procesadores de importación");

        // Registrar procesador CSV
        processorCache.put(ImportacionConstants.FORMATO_CSV, csvProcessor);

        // Registrar procesador Excel para ambas extensiones
        processorCache.put(ImportacionConstants.FORMATO_EXCEL_XLSX, excelProcessor);
        processorCache.put(ImportacionConstants.FORMATO_EXCEL_XLS, excelProcessor);

        logger.info("Cache de procesadores inicializado con {} procesadores: {}",
                processorCache.size(), processorCache.keySet());
    }

    /**
     * Limpia el cache de procesadores (útil para testing).
     */
    public void limpiarCache() {
        logger.debug("Limpiando cache de procesadores");
        processorCache.clear();
        inicializarCache();
    }
}