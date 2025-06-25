package informviva.gest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para productos más vendidos
 * Usado en reportes y estadísticas de ventas
 *
 * @author Roberto Rivas
 * @version 1.1 - CORREGIDO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoVendidoDTO {

    private Long productoId;
    private String codigoProducto;
    private String nombreProducto;
    private String categoria;
    private String marca;
    private Integer cantidadVendida;
    private Double precioUnitario;
    private Double ingresoTotal;
    private Integer stockActual;
    private Double porcentajePArticipacion;
    private String periodo;

    // AGREGADO: Campo para porcentaje total
    private Double porcentajeTotal;

    // Constructor para casos básicos
    public ProductoVendidoDTO(Long productoId, String nombreProducto, Integer cantidadVendida) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidadVendida = cantidadVendida;
    }

    // Constructor para casos con ingresos
    public ProductoVendidoDTO(Long productoId, String nombreProducto, Integer cantidadVendida, Double ingresoTotal) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidadVendida = cantidadVendida;
        this.ingresoTotal = ingresoTotal;
    }

    // Constructor para reportes completos
    public ProductoVendidoDTO(Long productoId, String codigoProducto, String nombreProducto,
                              String categoria, Integer cantidadVendida, Double ingresoTotal) {
        this.productoId = productoId;
        this.codigoProducto = codigoProducto;
        this.nombreProducto = nombreProducto;
        this.categoria = categoria;
        this.cantidadVendida = cantidadVendida;
        this.ingresoTotal = ingresoTotal;
    }

    /**
     * AGREGADO: Método getIngresos() - alias para ingresoTotal
     */
    public Double getIngresos() {
        return ingresoTotal;
    }

    /**
     * AGREGADO: Método setIngresos() - alias para ingresoTotal
     */
    public void setIngresos(Double ingresos) {
        this.ingresoTotal = ingresos;
    }

    /**
     * AGREGADO: Método setPorcentajeTotal()
     */
    public void setPorcentajeTotal(Double porcentajeTotal) {
        this.porcentajeTotal = porcentajeTotal;
    }

    /**
     * AGREGADO: Método getPorcentajeTotal()
     */
    public Double getPorcentajeTotal() {
        return porcentajeTotal;
    }

    /**
     * Calcula el precio promedio por unidad
     */
    public Double getPrecioPromedio() {
        if (cantidadVendida != null && cantidadVendida > 0 && ingresoTotal != null) {
            return ingresoTotal / cantidadVendida;
        }
        return 0.0;
    }

    /**
     * Verifica si el producto tiene stock bajo
     */
    public boolean tieneBajoStock() {
        return stockActual != null && stockActual < 10;
    }

    /**
     * Obtiene el porcentaje de participación formateado
     */
    public String getPorcentajeFormateado() {
        if (porcentajePArticipacion != null) {
            return String.format("%.2f%%", porcentajePArticipacion);
        }
        return "0.00%";
    }

    /**
     * AGREGADO: Obtiene el porcentaje total formateado
     */
    public String getPorcentajeTotalFormateado() {
        if (porcentajeTotal != null) {
            return String.format("%.2f%%", porcentajeTotal);
        }
        return "0.00%";
    }

    @Override
    public String toString() {
        return "ProductoVendidoDTO{" +
                "productoId=" + productoId +
                ", nombreProducto='" + nombreProducto + '\'' +
                ", cantidadVendida=" + cantidadVendida +
                ", ingresoTotal=" + ingresoTotal +
                ", porcentajeTotal=" + porcentajeTotal +
                '}';
    }
}