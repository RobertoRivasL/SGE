package informviva.gest.service;


import informviva.gest.dto.ImportacionResultadoDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Servicio principal para la gestión de importaciones
 *
 * @author Roberto Rivas
 * @version 2.0
 */
public interface ImportacionServicio {

    /**
     * Importa clientes desde un archivo
     *
     * @param archivo Archivo CSV o Excel con datos de clientes
     * @return Resultado de la importación con estadísticas y errores
     */
    ImportacionResultadoDTO importarClientes(MultipartFile archivo);

    /**
     * Importa productos desde un archivo
     *
     * @param archivo Archivo CSV o Excel con datos de productos
     * @return Resultado de la importación con estadísticas y errores
     */
    ImportacionResultadoDTO importarProductos(MultipartFile archivo);

    /**
     * Importa usuarios desde un archivo
     *
     * @param archivo Archivo CSV o Excel con datos de usuarios
     * @return Resultado de la importación con estadísticas y errores
     */
    ImportacionResultadoDTO importarUsuarios(MultipartFile archivo);

    /**
     * Valida el formato y estructura de un archivo antes de importar
     *
     * @param archivo Archivo a validar
     * @param tipoEntidad Tipo de entidad (cliente, producto, usuario)
     * @return Mapa con resultado de validación y errores encontrados
     */
    Map<String, Object> validarArchivoImportacion(MultipartFile archivo, String tipoEntidad);

    /**
     * Obtiene una plantilla de ejemplo para importación
     *
     * @param tipoEntidad Tipo de entidad para la plantilla
     * @param formato Formato del archivo (CSV o EXCEL)
     * @return Bytes del archivo plantilla
     */
    byte[] generarPlantillaImportacion(String tipoEntidad, String formato);

    /**
     * Procesa un archivo y devuelve vista previa de los datos
     *
     * @param archivo Archivo a procesar
     * @param limite Número máximo de filas para la vista previa
     * @return Lista de mapas con los datos procesados
     */
    List<Map<String, Object>> obtenerVistaPreviaArchivo(MultipartFile archivo, int limite);

    /**
     * Obtiene los formatos de archivo soportados para importación
     *
     * @return Lista de extensiones soportadas
     */
    List<String> obtenerFormatosSoportados();

    /**
     * Obtiene las columnas requeridas para cada tipo de entidad
     *
     * @param tipoEntidad Tipo de entidad
     * @return Lista de nombres de columnas requeridas
     */
    List<String> obtenerColumnasRequeridas(String tipoEntidad);
}