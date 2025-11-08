package informviva.gest.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class VentaDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- REFACTORIZACIÓN CLAVE ---
    // La relación @ManyToOne es fundamental para que 'mappedBy' en Venta.java funcione.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id")
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    private int cantidad;
    private double precioUnitario;

    /**
     * Calcula el subtotal del detalle (cantidad * precio unitario)
     */
    public double getSubtotal() {
        return cantidad * precioUnitario;
    }
}
