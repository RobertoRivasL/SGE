package informviva.gest.service.impl;

/**
 * @author Roberto Rivas
 * @version 2.0
 *
 */


import informviva.gest.dto.ImportacionResultadoDTO;
import informviva.gest.service.ImportacionServicio;
import informviva.gest.service.ProductoImportacionServicio;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductoImportacionServicioImpl implements ProductoImportacionServicio {

    @Autowired
    private ImportacionServicio importacionServicio;

    @Override
    public ImportacionResultadoDTO importarProductos(MultipartFile archivo) {
        return importacionServicio.importarProductos(archivo);
    }

    @Override
    public boolean validarFormatoArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            return false;
        }

        String filename = archivo.getOriginalFilename();
        return filename != null && (filename.endsWith(".xlsx") || filename.endsWith(".xls"));
    }



    @Override
    public byte[] generarPlantillaImportacion() {
        try {
            return importacionServicio.generarPlantillaProductos();
        } catch (Exception e) {
            throw new RuntimeException("Error generando plantilla de productos", e);
        }
    }
}