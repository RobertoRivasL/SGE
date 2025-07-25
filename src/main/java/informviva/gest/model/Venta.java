// src/main/java/informviva/gest/model/Venta.java
package informviva.gest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas", indexes = {
        @Index(name = "idx_venta_fecha", columnList = "fecha"),
        @Index(name = "idx_venta_cliente", columnList = "cliente_id"),
        @Index(name = "idx_venta_vendedor", columnList = "vendedor_id"),
        @Index(name = "idx_venta_estado", columnList = "estado")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"detalles"})
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La fecha de la venta no puede ser nula")
    private LocalDateTime fecha;

    @NotNull(message = "El cliente no puede ser nulo")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    private Usuario vendedor;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<VentaDetalle> detalles = new ArrayList<>();

    @Positive(message = "El subtotal debe ser mayor que cero")
    private Double subtotal;

    @Positive(message = "El impuesto debe ser mayor que cero")
    private Double impuesto;

    @NotNull(message = "El total no puede ser nulo")
    @Positive(message = "El total debe ser mayor que cero")
    private Double total;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    private EstadoVenta estado = EstadoVenta.PENDIENTE;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(length = 50, unique = true)
    private String numeroFactura;

    private Double descuento = 0.0;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    @Column(length = 100)
    private String usuarioCreacion;

    @Column(length = 100)
    private String usuarioActualizacion;

    // ==================== ENUMS ====================

    public enum EstadoVenta {
        PENDIENTE("Pendiente"),
        EN_PROCESO("En Proceso"),
        COMPLETADA("Completada"),
        ANULADA("Anulada"),
        PARCIALMENTE_PAGADA("Parcialmente Pagada");

        private final String descripcion;
        EstadoVenta(String descripcion) { this.descripcion = descripcion; }
        public String getDescripcion() { return descripcion; }
    }

    public enum MetodoPago {
        EFECTIVO("Efectivo"),
        TARJETA_CREDITO("Tarjeta de Crédito"),
        TARJETA_DEBITO("Tarjeta de Débito"),
        TRANSFERENCIA("Transferencia Bancaria"),
        CHEQUE("Cheque"),
        CREDITO("Crédito");

        private final String descripcion;
        MetodoPago(String descripcion) { this.descripcion = descripcion; }
        public String getDescripcion() { return descripcion; }
    }

    // ==================== CALLBACKS JPA ====================

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (fechaCreacion == null) fechaCreacion = now;
        if (fecha == null) fecha = now;
        if (estado == null) estado = EstadoVenta.PENDIENTE;
        fechaActualizacion = now;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    @Transient
    public LocalDate getFechaAsLocalDate() {
        return fecha != null ? fecha.toLocalDate() : null;
    }

    @Transient
    public boolean isClienteNuevo() {
        return cliente != null &&
                cliente.getFechaRegistro() != null &&
                cliente.getFechaRegistro().isAfter(LocalDate.now().minusDays(30).atStartOfDay());
    }

    @Transient
    public Double getTotalConDescuento() {
        if (total == null) return 0.0;
        if (descuento == null) return total;
        return total - descuento;
    }

    @Transient
    public boolean isAnulada() {
        return EstadoVenta.ANULADA.equals(estado);
    }

    @Transient
    public boolean isCompletada() {
        return EstadoVenta.COMPLETADA.equals(estado);
    }

    @Transient
    public boolean isEditable() {
        return !EstadoVenta.ANULADA.equals(estado) && !EstadoVenta.COMPLETADA.equals(estado);
    }

    @Transient
    public Integer getTotalItems() {
        if (detalles == null || detalles.isEmpty()) return 0;
        return detalles.stream().mapToInt(VentaDetalle::getCantidad).sum();
    }

    @Transient
    public Integer getCantidadProductosDiferentes() {
        if (detalles == null) return 0;
        return detalles.size();
    }

    @Transient
    public Double calcularSubtotalFromDetalles() {
        if (detalles == null || detalles.isEmpty()) return 0.0;
        return detalles.stream()
                .mapToDouble(detalle -> detalle.getCantidad() * detalle.getPrecioUnitario())
                .sum();
    }

    @Transient
    public String getNombreCliente() {
        return cliente != null ? cliente.getNombreCompleto() : "Cliente no especificado";
    }

    @Transient
    public String getNombreVendedor() {
        return vendedor != null ? vendedor.getNombreCompleto() : "Vendedor no especificado";
    }

    @Transient
    public String getDescripcionMetodoPago() {
        return metodoPago != null ? metodoPago.getDescripcion() : "No especificado";
    }

    @Transient
    public String getDescripcionEstado() {
        return estado != null ? estado.getDescripcion() : "No especificado";
    }

    // ==================== MÉTODOS DE GESTIÓN DE DETALLES ====================

    public void agregarDetalle(VentaDetalle detalle) {
        if (detalles == null) detalles = new ArrayList<>();
        detalle.setVenta(this);
        detalles.add(detalle);
    }

    public void removerDetalle(VentaDetalle detalle) {
        if (detalles != null) {
            detalles.remove(detalle);
            detalle.setVenta(null);
        }
    }

    public void limpiarDetalles() {
        if (detalles != null) {
            detalles.forEach(detalle -> detalle.setVenta(null));
            detalles.clear();
        }
    }

    // ==================== MÉTODOS EQUALS Y HASHCODE ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Venta)) return false;
        Venta venta = (Venta) o;
        return id != null && id.equals(venta.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}