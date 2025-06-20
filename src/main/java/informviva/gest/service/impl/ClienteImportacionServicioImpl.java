package informviva.gest.service.impl;

/**
 * @author Roberto Rivas
 * @version 2.0
 *
 */


import informviva.gest.dto.ImportacionResultadoDTO;
import informviva.gest.service.ClienteImportacionServicio;
import informviva.gest.service.ImportacionServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ClienteImportacionServicioImpl implements ClienteImportacionServicio {

    @Autowired
    private ImportacionServicio importacionServicio;

    @Override
    public ImportacionResultadoDTO importarClientes(MultipartFile archivo) {
        return importacionServicio.importarClientes(archivo);
    }

    @Override
    public boolean validarDatosClientes(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            return false;
        }

        String filename = archivo.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return false;
        }

        // Validación adicional del contenido del archivo aquí si es necesario
        return true;
    }

    @Override
    public byte[] generarPlantillaImportacion() {
        try {
            return importacionServicio.generarPlantillaClientes();
        } catch (Exception e) {
            throw new RuntimeException("Error generando plantilla de clientes", e);
        }
    }
}
