package informviva.gest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad refactorizada que representa una venta en el sistema
 * Corrige relaciones, mejora validaciones y aplica mejores prácticas
 *
 * @author Roberto Rivas
 * @version 4.0 - REFACTORIZADO COMPLETO
 */
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
@ToString(exclude = {"detalles"}) // Evitar recursión en toString
public class Venta {

    // ==================== IDENTIFICACIÓN ====================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ==================== FECHAS ====================

    @NotNull(message = "La fecha de la venta no puede ser nula")
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    // ==================== RELACIONES PRINCIPALES ====================

    @NotNull(message = "El cliente no puede ser nulo")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_venta_cliente"))
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id",
            foreignKey = @ForeignKey(name = "fk_venta_vendedor"))
    private Usuario vendedor;

    // ✅ RELACIÓN CORREGIDA: OneToMany bidireccional
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    private List<VentaDetalle> detalles = new ArrayList<>();

    // ==================== INFORMACIÓN FINANCIERA ====================

    @Positive(message = "El subtotal debe ser mayor que cero")
    @Column(name = "subtotal", precision = 10, scale = 2)
    private Double subtotal;

    @Positive(message = "El impuesto debe ser mayor que cero")
    @Column(name = "impuesto", precision = 10, scale = 2)
    private Double impuesto;

    @NotNull(message = "El total no puede ser nulo")
    @Positive(message = "El total debe ser mayor que cero")
    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private Double total;

    // ==================== CAMPOS ADICIONALES ====================

    @Column(name = "metodo_pago", length = 50)
    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    @Column(name = "estado", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoVenta estado = EstadoVenta.PENDIENTE;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "numero_factura", length = 50, unique = true)
    private String numeroFactura;

    @Column(name = "descuento", precision = 10, scale = 2)
    private Double descuento = 0.0;

    // ==================== CAMPOS DE AUDITORÍA ====================

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "usuario_creacion", length = 100)
    private String usuarioCreacion;

    @Column(name = "usuario_actualizacion", length = 100)
    private String usuarioActualizacion;

    // ==================== CAMPOS LEGACY (MANTENER COMPATIBILIDAD) ====================

    @Deprecated
    @Column(name = "monto", precision = 10, scale = 2)
    private Double monto; // Para compatibilidad con código existente

    @Deprecated
    @Column(name = "cantidad")
    private Integer cantidad; // Para compatibilidad con código existente

    @Deprecated
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto; // Para compatibilidad con código existente

    @Deprecated
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario; // Para compatibilidad con código existente

    @Deprecated
    @Column(name = "categoria", length = 100)
    private String categoria; // Para compatibilidad con código existente

    // ==================== ENUMS ====================

    public enum EstadoVenta {
        PENDIENTE("Pendiente"),
        EN_PROCESO("En Proceso"),
        COMPLETADA("Completada"),
        ANULADA("Anulada"),
        PARCIALMENTE_PAGADA("Parcialmente Pagada");

        private final String descripcion;

        EstadoVenta(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    public enum MetodoPago {
        EFECTIVO("Efectivo"),
        TARJETA_CREDITO("Tarjeta de Crédito"),
        TARJETA_DEBITO("Tarjeta de Débito"),
        TRANSFERENCIA("Transferencia Bancaria"),
        CHEQUE("Cheque"),
        CREDITO("Crédito");

        private final String descripcion;

        MetodoPago(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    // ==================== CALLBACKS JPA ====================

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (fechaCreacion == null) {
            fechaCreacion = now;
        }
        if (fecha == null) {
            fecha = now;
        }
        if (estado == null) {
            estado = EstadoVenta.PENDIENTE;
        }
        fechaActualizacion = now;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Obtiene la fecha como LocalDate (sin la hora)
     */
    @Transient
    public LocalDate getFechaAsLocalDate() {
        return fecha != null ? fecha.toLocalDate() : null;
    }

    /**
     * Verifica si la venta pertenece a un cliente nuevo
     */
    @Transient
    public boolean isClienteNuevo() {
        return cliente != null &&
                cliente.getFechaRegistro() != null &&
                cliente.getFechaRegistro().isAfter(LocalDate.now().minusDays(30).atStartOfDay());
    }

    /**
     * Obtiene el total con descuentos aplicados
     */
    @Transient
    public Double getTotalConDescuento() {
        if (total == null) return 0.0;
        if (descuento == null) return total;
        return total - descuento;
    }

    /**
     * Verifica si la venta está anulada
     */
    @Transient
    public boolean isAnulada() {
        return EstadoVenta.ANULADA.equals(estado);
    }

    /**
     * Verifica si la venta está completada
     */
    @Transient
    public boolean isCompletada() {
        return EstadoVenta.COMPLETADA.equals(estado);
    }

    /**
     * Verifica si la venta se puede editar
     */
    @Transient
    public boolean isEditable() {
        return !EstadoVenta.ANULADA.equals(estado) && !EstadoVenta.COMPLETADA.equals(estado);
    }

    /**
     * Obtiene la cantidad total de items en la venta
     */
    @Transient
    public Integer getTotalItems() {
        if (detalles == null || detalles.isEmpty()) {
            return cantidad != null ? cantidad : 0; // Compatibilidad legacy
        }

        return detalles.stream()
                .mapToInt(VentaDetalle::getCantidad)
                .sum();
    }

    /**
     * Obtiene la cantidad de productos diferentes en la venta
     */
    @Transient
    public Integer getCantidadProductosDiferentes() {
        if (detalles == null || detalles.isEmpty()) {
            return producto != null ? 1 : 0; // Compatibilidad legacy
        }

        return detalles.size();
    }

    /**
     * Calcula el subtotal basado en los detalles
     */
    @Transient
    public Double calcularSubtotalFromDetalles() {
        if (detalles == null || detalles.isEmpty()) {
            return monto != null ? monto : 0.0; // Compatibilidad legacy
        }

        return detalles.stream()
                .mapToDouble(detalle -> detalle.getCantidad() * detalle.getPrecioUnitario())
                .sum();
    }

    /**
     * Obtiene el nombre del cliente de forma segura
     */
    @Transient
    public String getNombreCliente() {
        return cliente != null ? cliente.getNombreCompleto() : "Cliente no especificado";
    }

    /**
     * Obtiene el nombre del vendedor de forma segura
     */
    @Transient
    public String getNombreVendedor() {
        return vendedor != null ? vendedor.getNombreCompleto() : "Vendedor no especificado";
    }

    /**
     * Obtiene la descripción del método de pago
     */
    @Transient
    public String getDescripcionMetodoPago() {
        return metodoPago != null ? metodoPago.getDescripcion() : "No especificado";
    }

    /**
     * Obtiene la descripción del estado
     */
    @Transient
    public String getDescripcionEstado() {
        return estado != null ? estado.getDescripcion() : "No especificado";
    }

    // ==================== MÉTODOS DE GESTIÓN DE DETALLES ====================

    /**
     * Agrega un detalle a la venta
     */
    public void agregarDetalle(VentaDetalle detalle) {
        if (detalles == null) {
            detalles = new ArrayList<>();
        }
        detalle.setVenta(this);
        detalles.add(detalle);
    }

    /**
     * Remueve un detalle de la venta
     */
    public void removerDetalle(VentaDetalle detalle) {
        if (detalles != null) {
            detalles.remove(detalle);
            detalle.setVenta(null);
        }
    }

    /**
     * Limpia todos los detalles
     */
    public void limpiarDetalles() {
        if (detalles != null) {
            detalles.forEach(detalle -> detalle.setVenta(null));
            detalles.clear();
        }
    }

    // ==================== MÉTODOS DE COMPATIBILIDAD LEGACY ====================

    /**
     * @deprecated Usar getTotalConDescuento() en su lugar
     */
    @Deprecated
    @Transient
    public Double getTotalConImpuestos() {
        if (subtotal == null) return total != null ? total : 0.0;
        if (impuesto == null) return subtotal;
        return subtotal + impuesto;
    }

    /**
     * Setter para compatibilidad con metodoPago como String
     */
    public void setMetodoPago(String metodoPagoStr) {
        if (metodoPagoStr != null && !metodoPagoStr.trim().isEmpty()) {
            try {
                this.metodoPago = MetodoPago.valueOf(metodoPagoStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Si no se puede convertir, mantener como null o usar EFECTIVO como default
                this.metodoPago = MetodoPago.EFECTIVO;
            }
        }
    }

    /**
     * Getter para compatibilidad con metodoPago como String
     */
    public String getMetodoPagoAsString() {
        return metodoPago != null ? metodoPago.name() : null;
    }

    /**
     * Setter para compatibilidad con estado como String
     */
    public void setEstado(String estadoStr) {
        if (estadoStr != null && !estadoStr.trim().isEmpty()) {
            try {
                this.estado = EstadoVenta.valueOf(estadoStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Si no se puede convertir, usar PENDIENTE como default
                this.estado = EstadoVenta.PENDIENTE;
            }
        }
    }

    /**
     * Getter para compatibilidad con estado como String
     */
    public String getEstadoAsString() {
        return estado != null ? estado.name() : null;
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