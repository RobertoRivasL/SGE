package informviva.gest.model;

/**
 * @author Roberto Rivas
 * @version 2.0
 * @author Roberto Rivas
 * @version 2.0
 */


/**
 * @author Roberto Rivas
 * @version 2.0
 */


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un detalle de venta (línea de venta)
 */
@Entity
@Table(name = "venta_detalles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    @NotNull(message = "El precio unitario no puede ser nulo")
    @Positive(message = "El precio unitario debe ser mayor que cero")
    private Double precioUnitario;

    @NotNull(message = "El subtotal no puede ser nulo")
    @Positive(message = "El subtotal debe ser mayor que cero")
    private Double subtotal;

    private Double descuento;

    @NotNull(message = "El total no puede ser nulo")
    @Positive(message = "El total debe ser mayor que cero")
    private Double total;

    /**
     * Calcula el subtotal (precio unitario * cantidad)
     * @return Subtotal calculado
     */
    public Double calcularSubtotal() {
        return precioUnitario * cantidad;
    }

    /**
     * Calcula el total (subtotal - descuento)
     * @return Total calculado
     */
    public Double calcularTotal() {
        double totalCalculado = subtotal;
        if (descuento != null && descuento > 0) {
            totalCalculado -= descuento;
        }
        return totalCalculado;
    }
}

