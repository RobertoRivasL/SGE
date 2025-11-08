package informviva.gest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una orden de compra
 * Gestiona las compras realizadas a proveedores
 *
 * @author Roberto Rivas
 * @version 1.0
 * @since FASE 3 - Módulo de Compras
 */
@Entity
@Table(name = "ordenes_compra", indexes = {
    @Index(name = "idx_numero_orden", columnList = "numeroOrden"),
    @Index(name = "idx_proveedor", columnList = "proveedor_id"),
    @Index(name = "idx_estado", columnList = "estado"),
    @Index(name = "idx_fecha_orden", columnList = "fechaOrden"),
    @Index(name = "idx_usuario_comprador", columnList = "usuario_comprador_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Número único de la orden de compra (generado automáticamente)
     */
    @Column(unique = true, nullable = false, length = 50)
    private String numeroOrden;

    /**
     * Proveedor de la orden
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    @NotNull(message = "El proveedor es obligatorio")
    private Proveedor proveedor;

    /**
     * Fecha de emisión de la orden
     */
    @Column(nullable = false)
    private LocalDate fechaOrden;

    /**
     * Fecha estimada de entrega
     */
    private LocalDate fechaEntregaEstimada;

    /**
     * Fecha real de entrega/recepción
     */
    private LocalDate fechaEntregaReal;

    /**
     * Estado de la orden
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoOrden estado = EstadoOrden.PENDIENTE;

    /**
     * Usuario que creó la orden (comprador)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_comprador_id", nullable = false)
    @NotNull(message = "El usuario comprador es obligatorio")
    private Usuario usuarioComprador;

    /**
     * Usuario que aprobó la orden
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_aprobador_id")
    private Usuario usuarioAprobador;

    /**
     * Fecha de aprobación
     */
    private LocalDateTime fechaAprobacion;

    /**
     * Usuario que recibió la orden
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_receptor_id")
    private Usuario usuarioReceptor;

    /**
     * Fecha de recepción
     */
    private LocalDateTime fechaRecepcion;

    /**
     * Detalles de la orden (líneas de productos)
     */
    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleOrdenCompra> detalles = new ArrayList<>();

    /**
     * Subtotal (suma de líneas sin impuestos)
     */
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    /**
     * Porcentaje de impuesto (IVA)
     */
    @Column(precision = 5, scale = 2)
    private BigDecimal porcentajeImpuesto = new BigDecimal("19.00"); // IVA 19% por defecto

    /**
     * Monto del impuesto
     */
    @Column(precision = 12, scale = 2)
    private BigDecimal montoImpuesto = BigDecimal.ZERO;

    /**
     * Descuento aplicado
     */
    @Column(precision = 12, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    /**
     * Total de la orden (subtotal + impuesto - descuento)
     */
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal total = BigDecimal.ZERO;

    /**
     * Observaciones o notas de la orden
     */
    @Column(length = 1000)
    private String observaciones;

    /**
     * Condiciones de pago
     */
    @Column(length = 200)
    private String condicionesPago;

    /**
     * Método de pago
     */
    @Column(length = 50)
    private String metodoPago;

    /**
     * Dirección de entrega
     */
    @Column(length = 500)
    private String direccionEntrega;

    /**
     * Referencia del proveedor (número de cotización, etc.)
     */
    @Column(length = 100)
    private String referenciaProveedor;

    /**
     * Motivo de cancelación (si aplica)
     */
    @Column(length = 500)
    private String motivoCancelacion;

    /**
     * Fecha de cancelación
     */
    private LocalDateTime fechaCancelacion;

    /**
     * Fecha de creación del registro
     */
    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de última actualización
     */
    private LocalDateTime fechaActualizacion;

    /**
     * Enum para estados de la orden
     */
    public enum EstadoOrden {
        /**
         * Orden creada pero no aprobada
         */
        BORRADOR("Borrador"),

        /**
         * Orden aprobada pendiente de envío al proveedor
         */
        PENDIENTE("Pendiente"),

        /**
         * Orden enviada al proveedor
         */
        ENVIADA("Enviada"),

        /**
         * Orden confirmada por el proveedor
         */
        CONFIRMADA("Confirmada"),

        /**
         * Mercancía en tránsito
         */
        EN_TRANSITO("En Tránsito"),

        /**
         * Mercancía recibida parcialmente
         */
        RECIBIDA_PARCIAL("Recibida Parcial"),

        /**
         * Mercancía recibida completamente
         */
        RECIBIDA_COMPLETA("Recibida Completa"),

        /**
         * Orden completada y cerrada
         */
        COMPLETADA("Completada"),

        /**
         * Orden cancelada
         */
        CANCELADA("Cancelada");

        private final String descripcion;

        EstadoOrden(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public boolean esModificable() {
            return this == BORRADOR || this == PENDIENTE;
        }

        public boolean esRecibible() {
            return this == ENVIADA || this == CONFIRMADA || this == EN_TRANSITO || this == RECIBIDA_PARCIAL;
        }

        public boolean esCancelable() {
            return this != COMPLETADA && this != CANCELADA;
        }
    }

    /**
     * Establece fecha de creación y número de orden si no están definidos
     */
    @PrePersist
    public void establecerDatosCreacion() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.fechaOrden == null) {
            this.fechaOrden = LocalDate.now();
        }
        if (this.numeroOrden == null) {
            generarNumeroOrden();
        }
    }

    /**
     * Actualiza fecha de modificación
     */
    @PreUpdate
    public void actualizarFechaModificacion() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Genera número de orden automático
     * Formato: OC-YYYYMMDD-XXXXXX
     */
    private void generarNumeroOrden() {
        LocalDateTime ahora = LocalDateTime.now();
        String fecha = String.format("%04d%02d%02d",
                ahora.getYear(), ahora.getMonthValue(), ahora.getDayOfMonth());
        // El ID se agregará después de la persistencia
        this.numeroOrden = String.format("OC-%s-%06d", fecha,
                id != null ? id : 0);
    }

    /**
     * Agrega un detalle a la orden
     */
    public void agregarDetalle(DetalleOrdenCompra detalle) {
        detalles.add(detalle);
        detalle.setOrdenCompra(this);
    }

    /**
     * Elimina un detalle de la orden
     */
    public void eliminarDetalle(DetalleOrdenCompra detalle) {
        detalles.remove(detalle);
        detalle.setOrdenCompra(null);
    }

    /**
     * Calcula el subtotal sumando todos los detalles
     */
    public void calcularSubtotal() {
        this.subtotal = detalles.stream()
                .map(DetalleOrdenCompra::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula el impuesto basado en el subtotal
     */
    public void calcularImpuesto() {
        if (porcentajeImpuesto != null && subtotal != null) {
            this.montoImpuesto = subtotal
                    .multiply(porcentajeImpuesto)
                    .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            this.montoImpuesto = BigDecimal.ZERO;
        }
    }

    /**
     * Calcula el total de la orden
     */
    public void calcularTotal() {
        calcularSubtotal();
        calcularImpuesto();

        this.total = subtotal
                .add(montoImpuesto != null ? montoImpuesto : BigDecimal.ZERO)
                .subtract(descuento != null ? descuento : BigDecimal.ZERO);
    }

    /**
     * Recalcula todos los totales
     */
    public void recalcularTotales() {
        calcularTotal();
    }

    /**
     * Verifica si la orden puede ser modificada
     */
    @Transient
    public boolean esModificable() {
        return estado != null && estado.esModificable();
    }

    /**
     * Verifica si la orden puede recibir mercancía
     */
    @Transient
    public boolean esRecibible() {
        return estado != null && estado.esRecibible();
    }

    /**
     * Verifica si la orden puede ser cancelada
     */
    @Transient
    public boolean esCancelable() {
        return estado != null && estado.esCancelable();
    }

    /**
     * Obtiene el número de líneas/detalles
     */
    @Transient
    public int getCantidadLineas() {
        return detalles != null ? detalles.size() : 0;
    }

    /**
     * Obtiene el total de unidades ordenadas
     */
    @Transient
    public Integer getTotalUnidades() {
        return detalles.stream()
                .map(DetalleOrdenCompra::getCantidad)
                .reduce(0, Integer::sum);
    }
}
