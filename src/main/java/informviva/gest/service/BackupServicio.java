package informviva.gest.service;

import java.nio.file.Path;

/**
 * Servicio para crear respaldos exportables de los datos de la aplicación.
 */
public interface BackupServicio {

    /**
     * Crea un respaldo manual y lanza una excepción si no puede completarse.
     */
    void realizarBackupManual();

    /**
     * Crea un respaldo y devuelve la ruta del archivo generado.
     */
    Path crearBackup();
}
