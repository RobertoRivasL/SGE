package informviva.gest.service;

import informviva.gest.dto.ImportacionResultadoDTO;
import informviva.gest.dto.ResultadoImportacionCliente;
import informviva.gest.model.Cliente;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Servicio especializado para importación de clientes
 *
 * @author Roberto Rivas
 * @version 2.1 - INTERFAZ CORREGIDA
 */
public interface ClienteImportacionServicio {

    /**
     * Importa clientes desde archivo Excel o CSV (método genérico)
     */
    ImportacionResultadoDTO importarClientes(MultipartFile archivo);

    /**
     * Importa clientes desde archivo CSV
     *
     * @param archivo Archivo CSV con datos de clientes
     * @return Resultado de la importación
     */
    ResultadoImportacionCliente importarClientesDesdeCSV(MultipartFile archivo);

    /**
     * Importa clientes desde archivo Excel
     *
     * @param archivo Archivo Excel con datos de clientes
     * @return Resultado de la importación
     */
    ResultadoImportacionCliente importarClientesDesdeExcel(MultipartFile archivo);

    /**
     * Guarda la lista de clientes importados exitosamente
     *
     * @param clientes Lista de clientes a guardar
     * @return Resultado del guardado
     */
    ResultadoImportacionCliente guardarClientesImportados(List<Cliente> clientes);

    /**
     * Valida el formato del archivo antes de procesar
     *
     * @param archivo Archivo a validar
     * @return true si el formato es válido
     */
    boolean validarFormatoArchivo(MultipartFile archivo);

    /**
     * Obtiene vista previa de los datos del archivo
     *
     * @param archivo Archivo a procesar
     * @param limite Número máximo de registros para vista previa
     * @return Lista de clientes en vista previa
     */
    List<Cliente> obtenerVistaPreviaClientes(MultipartFile archivo, int limite);

    /**
     * Genera plantilla de importación para clientes
     *
     * @return Bytes del archivo plantilla Excel
     */
    byte[] generarPlantillaImportacion();
}