package informviva.gest.service.impl;

import informviva.gest.dto.ImportacionResultadoDTO;
import informviva.gest.service.ImportacionServicio;
import informviva.gest.service.importacion.ImportacionProcessor;
import informviva.gest.service.importacion.ImportacionValidador;
import informviva.gest.service.importacion.PlantillaGenerator;
import informviva.gest.service.importacion.factory.ImportacionProcessorFactory;
import informviva.gest.util.ImportacionConstants;
import informviva.gest.util.MensajesConstantes;
import informviva.gest.util.ValidacionConstantes;
import informviva.gest.exception.ImportacionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Implementación refactorizada del servicio para la gestión de importaciones.
 *
 * Esta clase actúa como fachada principal del sistema de importación,
 * delegando la lógica específica a componentes especializados.
 *
 * @author Roberto Rivas
 * @version 3.0
 */
@Service
@Transactional
public class ImportacionServicioImpl implements ImportacionServicio {

    private static final Logger logger = LoggerFactory.getLogger(ImportacionServicioImpl.class);

    private final ImportacionProcessorFactory processorFactory;
    private final ImportacionValidador validador;
    private final PlantillaGenerator plantillaGenerator;

    @Autowired
    public ImportacionServicioImpl(
            ImportacionProcessorFactory processorFactory,
            ImportacionValidador validador,
            PlantillaGenerator plantillaGenerator) {
        this.processorFactory = processorFactory;
        this.validador = validador;
        this.plantillaGenerator = plantillaGenerator;
    }

    @Override
    public ImportacionResultadoDTO importarClientes(MultipartFile archivo) {
        logger.info("Iniciando importación de clientes desde archivo: {}",
                obtenerNombreArchivoSeguro(archivo));

        return procesarImportacion(archivo, ImportacionConstants.TIPO_CLIENTE);
    }

    @Override
    public ImportacionResultadoDTO importarProductos(MultipartFile archivo) {
        logger.info("Iniciando importación de productos desde archivo: {}",
                obtenerNombreArchivoSeguro(archivo));

        return procesarImportacion(archivo, ImportacionConstants.TIPO_PRODUCTO);
    }

    @Override
    public ImportacionResultadoDTO importarUsuarios(MultipartFile archivo) {
        logger.info("Iniciando importación de usuarios desde archivo: {}",
                obtenerNombreArchivoSeguro(archivo));

        return procesarImportacion(archivo, ImportacionConstants.TIPO_USUARIO);
    }

    @Override
    public Map<String, Object> validarArchivoImportacion(MultipartFile archivo, String tipoEntidad) {
        logger.debug("Validando archivo para importación de tipo: {}", tipoEntidad);

        try {
            validarParametrosEntrada(archivo, tipoEntidad);
            return validador.validarArchivo(archivo, tipoEntidad);

        } catch (Exception e) {
            return manejarErrorValidacion(e, archivo);
        }
    }

    @Override
    public byte[] generarPlantillaImportacion(String tipoEntidad, String formato) {
        logger.debug("Generando plantilla para tipo: {} en formato: {}", tipoEntidad, formato);

        try {
            validarTipoEntidad(tipoEntidad);
            validarFormato(formato);

            return plantillaGenerator.generarPlantilla(tipoEntidad, formato);

        } catch (Exception e) {
            logger.error("Error generando plantilla para {}: {}", tipoEntidad, e.getMessage());
            throw new ImportacionException("Error generando plantilla", e);
        }
    }

    @Override
    public List<Map<String, Object>> obtenerVistaPreviaArchivo(MultipartFile archivo, int limite) {
        logger.debug("Obteniendo vista previa del archivo con límite: {}", limite);

        try {
            validarArchivoEntrada(archivo);
            validarLimiteVistaPrevia(limite);

            ImportacionProcessor processor = processorFactory.createProcessor(archivo);
            return processor.obtenerVistaPrevia(archivo, limite);

        } catch (Exception e) {
            logger.error("Error obteniendo vista previa: {}", e.getMessage());
            throw new ImportacionException("Error procesando vista previa", e);
        }
    }

    @Override
    public List<String> obtenerFormatosSoportados() {
        return ImportacionConstants.FORMATOS_SOPORTADOS;
    }

    @Override
    public List<String> obtenerColumnasRequeridas(String tipoEntidad) {
        try {
            validarTipoEntidad(tipoEntidad);
            return ImportacionConstants.obtenerColumnasRequeridas(tipoEntidad);

        } catch (Exception e) {
            logger.error("Error obteniendo columnas requeridas para {}: {}", tipoEntidad, e.getMessage());
            throw new ImportacionException("Tipo de entidad no válido", e);
        }
    }

    /**
     * Método central para procesar cualquier tipo de importación.
     * Implementa el patrón Template Method para unificar el flujo de procesamiento.
     */
    private ImportacionResultadoDTO procesarImportacion(MultipartFile archivo, String tipoEntidad) {
        ImportacionResultadoDTO resultado = inicializarResultado(archivo, tipoEntidad);

        try {
            // 1. Validar parámetros de entrada
            validarParametrosEntrada(archivo, tipoEntidad);

            // 2. Obtener processor específico para el tipo de archivo
            ImportacionProcessor processor = processorFactory.createProcessor(archivo);

            // 3. Ejecutar importación usando el processor
            resultado = processor.procesar(archivo, tipoEntidad, resultado);

            // 4. Log de resultados
            logResultadoImportacion(resultado);

        } catch (ImportacionException e) {
            manejarErrorImportacion(e, resultado);
        } catch (Exception e) {
            manejarErrorInesperado(e, resultado);
        }

        return resultado;
    }

    /**
     * Inicializa el objeto resultado con información básica.
     */
    private ImportacionResultadoDTO inicializarResultado(MultipartFile archivo, String tipoEntidad) {
        ImportacionResultadoDTO resultado = new ImportacionResultadoDTO();
        resultado.setTipoEntidad(capitalize(tipoEntidad));
        resultado.setNombreArchivo(obtenerNombreArchivoSeguro(archivo));
        resultado.setFechaImportacion(java.time.LocalDateTime.now());
        return resultado;
    }

    /**
     * Validaciones centralizadas de parámetros de entrada.
     */
    private void validarParametrosEntrada(MultipartFile archivo, String tipoEntidad) {
        validarArchivoEntrada(archivo);
        validarTipoEntidad(tipoEntidad);
    }

    private void validarArchivoEntrada(MultipartFile archivo) {
        if (archivo == null) {
            throw new ImportacionException(MensajesConstantes.ERROR_ARCHIVO_VACIO);
        }

        if (archivo.isEmpty()) {
            throw new ImportacionException(MensajesConstantes.ERROR_ARCHIVO_VACIO);
        }

        if (archivo.getSize() > ImportacionConstants.TAMAÑO_MAXIMO_ARCHIVO) {
            throw new ImportacionException(MensajesConstantes.ERROR_ARCHIVO_MUY_GRANDE);
        }

        String nombreArchivo = archivo.getOriginalFilename();
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            throw new ImportacionException("El nombre del archivo no es válido");
        }

        if (!validador.esFormatoSoportado(nombreArchivo)) {
            throw new ImportacionException(MensajesConstantes.ERROR_FORMATO_NO_SOPORTADO);
        }
    }

    private void validarTipoEntidad(String tipoEntidad) {
        if (tipoEntidad == null || tipoEntidad.trim().isEmpty()) {
            throw new ImportacionException(MensajesConstantes.ERROR_TIPO_ENTIDAD_INVALIDO);
        }

        if (!ImportacionConstants.TIPOS_ENTIDAD_VALIDOS.contains(tipoEntidad.toLowerCase())) {
            throw new ImportacionException(MensajesConstantes.ERROR_TIPO_ENTIDAD_INVALIDO + ": " + tipoEntidad);
        }
    }

    private void validarFormato(String formato) {
        if (formato == null || formato.trim().isEmpty()) {
            throw new ImportacionException("El formato no puede estar vacío");
        }

        if (!ImportacionConstants.FORMATOS_SOPORTADOS.contains(formato.toLowerCase())) {
            throw new ImportacionException(
                    String.format("Formato no soportado: %s. Formatos válidos: %s",
                            formato, String.join(", ", ImportacionConstants.FORMATOS_SOPORTADOS)));
        }
    }

    private void validarLimiteVistaPrevia(int limite) {
        if (limite <= ImportacionConstants.LIMITE_VISTA_PREVIA_MINIMO) {
            throw new ImportacionException(
                    String.format("El límite debe ser mayor a %d", ImportacionConstants.LIMITE_VISTA_PREVIA_MINIMO));
        }

        if (limite > ImportacionConstants.LIMITE_VISTA_PREVIA_MAXIMO) {
            throw new ImportacionException(
                    String.format("El límite no puede ser mayor a %d", ImportacionConstants.LIMITE_VISTA_PREVIA_MAXIMO));
        }
    }

    /**
     * Manejo centralizado de errores de importación.
     */
    private void manejarErrorImportacion(ImportacionException e, ImportacionResultadoDTO resultado) {
        logger.error("Error durante importación: {}", e.getMessage());
        resultado.agregarError(MensajesConstantes.ERROR_IMPORTACION + e.getMessage());
        resultado.calcularTotales();
    }

    private void manejarErrorInesperado(Exception e, ImportacionResultadoDTO resultado) {
        logger.error("Error inesperado durante importación", e);
        resultado.agregarError(MensajesConstantes.ERROR_IMPORTACION + "Error inesperado del sistema");
        resultado.calcularTotales();
    }

    private Map<String, Object> manejarErrorValidacion(Exception e, MultipartFile archivo) {
        logger.error("Error validando archivo {}: {}", obtenerNombreArchivoSeguro(archivo), e.getMessage());
        return Map.of(
                "valido", false,
                "errores", List.of(MensajesConstantes.ERROR_IMPORTACION + e.getMessage()),
                "nombreArchivo", obtenerNombreArchivoSeguro(archivo)
        );
    }

    /**
     * Registra el resultado de la importación en los logs.
     */
    private void logResultadoImportacion(ImportacionResultadoDTO resultado) {
        if (resultado.getTotalErrores() == ImportacionConstants.CERO_ERRORES) {
            logger.info("Importación completada exitosamente - Procesados: {}, Exitosos: {}",
                    resultado.getTotalProcesados(), resultado.getTotalExitosos());
        } else {
            logger.warn("Importación completada con errores - Procesados: {}, Exitosos: {}, Errores: {}",
                    resultado.getTotalProcesados(), resultado.getTotalExitosos(), resultado.getTotalErrores());
        }
    }

    /**
     * Utilitarios para manejo seguro de datos.
     */
    private String obtenerNombreArchivoSeguro(MultipartFile archivo) {
        return archivo != null && archivo.getOriginalFilename() != null
                ? archivo.getOriginalFilename()
                : "archivo_sin_nombre";
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(ImportacionConstants.INDICE_PRIMERA_LETRA, ImportacionConstants.INDICE_SEGUNDA_LETRA).toUpperCase()
                + str.substring(ImportacionConstants.INDICE_SEGUNDA_LETRA).toLowerCase();
    }
}// Validaciones previas
