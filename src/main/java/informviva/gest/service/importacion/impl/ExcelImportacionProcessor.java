package informviva.gest.service.importacion.impl;

import informviva.gest.service.importacion.ImportacionProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ExcelImportacionProcessor implements ImportacionProcessor {

    @Override
    public boolean puedeProcesar(MultipartFile archivo) {
        if (archivo == null || archivo.getOriginalFilename() == null) {
            return false;
        }
        String tipoArchivo = archivo.getOriginalFilename();
        return tipoArchivo.endsWith(".xlsx") || tipoArchivo.endsWith(".xls");
    }

    @Override
    public Object procesar(MultipartFile archivo) {
        // Implementación temporal
        return null;
    }
}