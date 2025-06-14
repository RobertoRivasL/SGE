package informviva.gest.service.importacion;

import org.springframework.web.multipart.MultipartFile;

public interface ImportacionProcessor {
    boolean puedeProcesar(String tipoArchivo);
    Object procesar(MultipartFile archivo);
}
