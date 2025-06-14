package informviva.gest.service;


import informviva.gest.dto.ImportacionResultadoDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Servicio principal para la gestión de importaciones
 *
 * @author Roberto Rivas
 * @version 2.0
 */
public interface ImportacionServicio {



    /**
     * Importa productos desde un archivo CSV o Excel
     *
     * @param archivo Archivo a importar
     * @param usuarioId ID del usuario que realiza la importación
     * @return Resultado de la importación
     */
    ImportacionResultadoDTO importarProductos(MultipartFile archivo, Long usuarioId);

    /**
     * Importa clientes desde un archivo CSV o Excel
     *
     * @param archivo Archivo a importar
     * @param usuarioId ID del usuario que realiza la importación
     * @return Resultado de la importación
     */
    ImportacionResultadoDTO importarClientes(MultipartFile archivo, Long usuarioId);

    /**
     * Importa usuarios desde un archivo CSV o Excel
     *
     * @param archivo Archivo a importar
     * @param usuarioId ID del usuario que realiza la importación
     * @return Resultado de la importación
     */
    ImportacionResultadoDTO importarUsuarios(MultipartFile archivo, Long usuarioId);

    /**
     * Valida el formato del archivo
     *
     * @param archivo Archivo a validar
     * @return true si el formato es válido
     */
    boolean validarFormatoArchivo(MultipartFile archivo);

    /**
     * Obtiene los formatos de archivo soportados
     *
     * @return Lista de extensiones soportadas
     */
    List<String> obtenerFormatosSoportados();

    /**
     * Genera un archivo de ejemplo para importación de productos
     *
     * @return Contenido del archivo de ejemplo en bytes
     */
    byte[] generarArchivoEjemploProductos();

    /**
     * Genera un archivo de ejemplo para importación de clientes
     *
     * @return Contenido del archivo de ejemplo en bytes
     */
    byte[] generarArchivoEjemploClientes();

    /**
     * Genera un archivo de ejemplo para importación de usuarios
     *
     * @return Contenido del archivo de ejemplo en bytes
     */
    byte[] generarArchivoEjemploUsuarios();


    class ConfiguracionImportacion {
        private String separador;
        private boolean detenerEnError;

        // Getters y setters
        public String getSeparador() {
            return separador;
        }

        public void setSeparador(String separador) {
            this.separador = separador;
        }

        public boolean isDetenerEnError() {
            return detenerEnError;
        }

        public void setDetenerEnError(boolean detenerEnError) {
            this.detenerEnError = detenerEnError;
        }
    }
}