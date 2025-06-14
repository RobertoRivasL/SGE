package informviva.gest.service.importacion;


import informviva.gest.dto.ImportacionResultadoDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Interfaz base para todos los procesadores de importación.
 * Define el contrato común para procesar diferentes tipos de archivos.
 *
 * @author Roberto Rivas
 * @version 1.0
 */
public interface ImportacionProcessor {

    /**
     * Procesa un archivo de importación y devuelve el resultado.
     *
     * @param archivo El archivo a procesar
     * @param tipoEntidad El tipo de entidad a importar
     * @param resultado El objeto resultado a completar
     * @return El resultado completado de la importación
     * @throws IOException Si hay errores de lectura del archivo
     */
    ImportacionResultadoDTO procesar(MultipartFile archivo, String tipoEntidad,
                                     ImportacionResultadoDTO resultado) throws IOException;

    /**
     * Obtiene una vista previa de los datos del archivo.
     *
     * @param archivo El archivo a procesar
     * @param limite Número máximo de filas a incluir en la vista previa
     * @return Lista de mapas con los datos de la vista previa
     * @throws IOException Si hay errores de lectura del archivo
     */
    List<Map<String, Object>> obtenerVistaPrevia(MultipartFile archivo, int limite) throws IOException;

    /**
     * Verifica si este procesador puede manejar el tipo de archivo dado.
     *
     * @param archivo El archivo a verificar
     * @return true si puede procesar el archivo, false en caso contrario
     */
    boolean puedeProcesar(MultipartFile archivo);

    /**
     * Obtiene los tipos de archivo que este procesador puede manejar.
     *
     * @return Lista de extensiones de archivo soportadas
     */
    List<String> getTiposSoportados();
}