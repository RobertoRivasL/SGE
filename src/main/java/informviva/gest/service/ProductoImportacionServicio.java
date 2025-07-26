package informviva.gest.service;

import informviva.gest.dto.ImportacionResultadoDTO;
import informviva.gest.dto.ProductoImportacionResultadoDTO;
import informviva.gest.model.Producto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Servicio especializado para importación de productos
 *
 * @author Roberto Rivas
 * @version 2.2 - CORREGIDA COMPATIBILIDAD
 */
public interface ProductoImportacionServicio {

    /**
     * Importa productos desde archivo Excel o CSV (método genérico)
     */
    ImportacionResultadoDTO importarProductos(MultipartFile archivo);

    /**
     * Importa productos desde archivo Excel
     *
     * @param archivo Archivo Excel con datos de productos
     * @return Resultado de la importación
     */
    ProductoImportacionResultadoDTO importarProductosDesdeExcel(MultipartFile archivo);

    /**
     * Guarda la lista de productos importados exitosamente
     *
     * @param productos Lista de productos a guardar
     * @return Resultado del guardado
     */
    ProductoImportacionResultadoDTO guardarProductosImportados(List<Producto> productos);

    /**
     * Valida el formato del archivo antes de procesar
     *
     * @param archivo Archivo a validar
     * @return true si el formato es válido
     */
    boolean validarFormatoArchivo(MultipartFile archivo);

    /**
     * Obtiene vista previa de los datos del archivo
     *
     * @param archivo Archivo a procesar
     * @param limite Número máximo de registros para vista previa
     * @return Resultado con productos en vista previa (CORREGIDO)
     */
    ProductoImportacionResultadoDTO obtenerVistaPreviaProductos(MultipartFile archivo, int limite);

    /**
     * Genera plantilla de importación para productos
     *
     * @return Bytes del archivo plantilla Excel
     * @throws IOException Si ocurre error al generar el archivo (AGREGADO)
     */
    byte[] generarPlantillaImportacion() throws IOException;
}