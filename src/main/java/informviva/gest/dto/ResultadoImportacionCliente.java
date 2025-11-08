package informviva.gest.dto;

import informviva.gest.model.Cliente;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para el resultado de importación de clientes
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ResultadoImportacionCliente extends ImportacionResultadoDTO {

    private List<Cliente> clientesExitosos = new ArrayList<>();

    /**
     * Constructor que copia los datos del DTO base
     */
    public ResultadoImportacionCliente(ImportacionResultadoDTO base) {
        this.setTipoEntidad(base.getTipoEntidad());
        this.setNombreArchivo(base.getNombreArchivo());
        this.setFechaImportacion(base.getFechaImportacion());
        this.setRegistrosProcesados(base.getRegistrosProcesados());
        this.setRegistrosExitosos(base.getRegistrosExitosos());
        this.setRegistrosConError(base.getRegistrosConError());
        this.setRegistrosOmitidos(base.getRegistrosOmitidos());
        this.setErrores(base.getErrores());
        this.setAdvertencias(base.getAdvertencias());
        this.setMensajesInfo(base.getMensajesInfo());
        this.setTiempoProcesamientoMs(base.getTiempoProcesamientoMs());
        this.setPorcentajeExito(base.getPorcentajeExito());
    }

    /**
     * Constructor vacío
     */
    public ResultadoImportacionCliente() {
        super();
    }

    /**
     * Método para compatibilidad con controlador
     */
    public int getTotalExitosos() {
        return getRegistrosExitosos();
    }

    /**
     * Método para compatibilidad con controlador
     */
    public int getTotalErrores() {
        return getRegistrosConError();
    }

    /**
     * Método para compatibilidad con controlador
     */
    public int getTotalProcesados() {
        return getRegistrosProcesados();
    }

    /**
     * Agrega un cliente a la lista de exitosos
     */
    public void agregarClienteExitoso(Cliente cliente) {
        if (this.clientesExitosos == null) {
            this.clientesExitosos = new ArrayList<>();
        }
        this.clientesExitosos.add(cliente);
    }

    /**
     * Obtiene la cantidad de clientes exitosos
     */
    public int getCantidadClientesExitosos() {
        return this.clientesExitosos != null ? this.clientesExitosos.size() : 0;
    }
}
