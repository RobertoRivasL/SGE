package informviva.gest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Objeto de Transferencia de Datos (DTO) que representa un producto vendido.
 * Esta clase encapsula la información sobre un producto que ha sido vendido,
 * incluyendo su nombre, cantidad, precio total y porcentaje de ventas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoVendidoDTO {
    private String producto;
    private Long cantidad;
    private Double total;
    private Double ingresos;
    private Double porcentajeTotal;

    public ProductoVendidoDTO(String producto, Long cantidad, Double total, Double ingresos) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.total = total;
        this.ingresos = ingresos;
    }

    // Constructor adicional para resolver el error
    public ProductoVendidoDTO(String producto, Long cantidad, Double total, int extra) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.total = total;
        this.ingresos = (double) extra; // Conversión de int a Double
    }

    public Double getIngresos() {
        return ingresos;
    }

    public void setPorcentajeTotal(Double porcentajeTotal) {
        this.porcentajeTotal = porcentajeTotal;
    }
}