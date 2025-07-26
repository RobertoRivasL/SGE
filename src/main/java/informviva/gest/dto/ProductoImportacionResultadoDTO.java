package informviva.gest.dto;


import informviva.gest.model.Producto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO extendido para importación de productos que incluye la lista de productos exitosos
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductoImportacionResultadoDTO extends ImportacionResultadoDTO {

    private List<Producto> productosExitosos = new ArrayList<>();

    /**
     * Constructor que copia los datos del DTO base
     */
    public ProductoImportacionResultadoDTO(ImportacionResultadoDTO base) {
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
    public ProductoImportacionResultadoDTO() {
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
     * Agrega un producto a la lista de exitosos
     */
    public void agregarProductoExitoso(Producto producto) {
        if (this.productosExitosos == null) {
            this.productosExitosos = new ArrayList<>();
        }
        this.productosExitosos.add(producto);
    }
}