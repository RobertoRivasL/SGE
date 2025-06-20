package informviva.gest.service;

import informviva.gest.dto.ImportacionResultadoDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * Servicio para importación de productos
 *
 * @author Roberto Rivas
 * @version 2.0
 */
public interface ProductoImportacionServicio {

    /**
     * Importa productos desde archivo Excel
     */
    ImportacionResultadoDTO importarProductos(MultipartFile archivo);

    /**
     * Valida formato de archivo de productos
     */
    boolean validarFormatoArchivo(MultipartFile archivo);

    /**
     * Genera plantilla de importación para productos
     */
    byte[] generarPlantillaImportacion();
}