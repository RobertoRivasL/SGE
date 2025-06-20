package informviva.gest.service;

import informviva.gest.dto.ImportacionResultadoDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * Servicio para importación de clientes
 *
 * @author Roberto Rivas
 * @version 2.0
 */
public interface ClienteImportacionServicio {

    /**
     * Importa clientes desde archivo Excel
     */
    ImportacionResultadoDTO importarClientes(MultipartFile archivo);

    /**
     * Valida datos de clientes antes de importar
     */
    boolean validarDatosClientes(MultipartFile archivo);

    /**
     * Genera plantilla de importación para clientes
     */
    byte[] generarPlantillaImportacion();
}