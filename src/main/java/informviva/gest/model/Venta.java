package informviva.gest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa una venta en el sistema
 */
@Entity
@Table(name = "ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @OneToMany(mappedBy = "venta")
    private List<VentaDetalle> detalles;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La fecha no puede ser nula")
    private LocalDateTime fecha;

    @NotNull(message = "El cliente no puede ser nulo")
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @NotNull(message = "El vendedor no puede ser nulo")
    @ManyToOne
    @JoinColumn(name = "vendedor_id")
    private Usuario vendedor;

    // @Column(...) // Añade anotaciones de columna si necesitas especificar nombre, precisión, etc.
    private Double monto; //

    // @Column(...) // Añade anotaciones de columna si necesitas especificar nombre, precisión, etc.
    private Integer cantidad; //

    // Relación con Producto si existe
    @ManyToOne // O la anotación de relación correcta
    @JoinColumn(name = "producto_id") // O el nombre de columna de la FK
    private Producto producto;


    @NotNull(message = "El subtotal no puede ser nulo")
    @Positive(message = "El subtotal debe ser mayor que cero")
    private Double subtotal;

    @NotNull(message = "El impuesto no puede ser nulo")
    @Positive(message = "El impuesto debe ser mayor que cero")
    private Double impuesto;

    @NotNull(message = "El total no puede ser nulo")
    @Positive(message = "El total debe ser mayor que cero")
    private Double total;

    private String metodoPago;

    private String observaciones;

    private String estado;

    private String categoria;

    /**
     * Obtiene la fecha como LocalDate (sin la hora)
     *
     * @return La fecha de la venta
     */
    @Transient
    public LocalDate getFechaAsLocalDate() {
        return fecha != null ? fecha.toLocalDate() : null;
    }

    /**
     * Verifica si la venta pertenece a un cliente nuevo
     * Este es un método de ejemplo, en un sistema real la lógica sería más compleja
     *
     * @return true si el cliente es nuevo, false en caso contrario
     */
    @Transient
    public boolean isClienteNuevo() {
        // Lógica simplificada para identificar a un cliente nuevo
        return cliente != null && cliente.getFechaRegistroAsLocalDate() != null &&
                cliente.getFechaRegistroAsLocalDate().isAfter(LocalDate.now().minusDays(30));
    }

    /**
     * Método adicional para obtener el monto total con impuestos incluidos
     *
     * @return El total con impuestos
     */
    @Transient
    public Double getTotalConImpuestos() {
        return subtotal + impuesto;
    }

}
