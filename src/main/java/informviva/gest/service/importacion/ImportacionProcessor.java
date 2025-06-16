package informviva.gest.service.importacion;

import informviva.gest.service.importacion.ImportacionProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

public interface ImportacionProcessor {
    boolean puedeProcesar(MultipartFile archivo);
    Object procesar(MultipartFile archivo);
}
