package informviva.gest.service.importacion.impl;

import informviva.gest.service.importacion.ImportacionProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ExcelImportacionProcessor implements ImportacionProcessor {

    @Override
    public boolean puedeProcesar(String tipoArchivo) {
        return "xlsx".equalsIgnoreCase(tipoArchivo) || "xls".equalsIgnoreCase(tipoArchivo);
    }

    @Override
    public Object procesar(MultipartFile archivo) {
        // Implementación temporal
        return null;
    }
}
