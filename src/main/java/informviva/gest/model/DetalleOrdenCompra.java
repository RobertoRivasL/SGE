package informviva.gest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entidad que representa una línea/detalle de una orden de compra
 * Cada detalle corresponde a un producto específico con su cantidad y precio
 *
 * @author Roberto Rivas
 * @version 1.0
 * @since FASE 3 - Módulo de Compras
 */
@Entity
@Table(name = "detalles_orden_compra", indexes = {
    @Index(name = "idx_orden_compra", columnList = "orden_compra_id"),
    @Index(name = "idx_producto_detalle", columnList = "producto_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleOrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Orden de compra a la que pertenece este detalle
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_compra_id", nullable = false)
    @NotNull(message = "La orden de compra es obligatoria")
    private OrdenCompra ordenCompra;

    /**
     * Producto ordenado
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @NotNull(message = "El producto es obligatorio")
    private Producto producto;

    /**
     * Cantidad ordenada
     */
    @Column(nullable = false)
    @Positive(message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;

    /**
     * Precio unitario del producto en esta orden
     * Puede diferir del precio actual del producto en el sistema
     */
    @Column(precision = 10, scale = 2, nullable = false)
    @Positive(message = "El precio unitario debe ser mayor que cero")
    private BigDecimal precioUnitario;

    /**
     * Descuento aplicado a esta línea (en porcentaje)
     */
    @Column(precision = 5, scale = 2)
    @PositiveOrZero(message = "El descuento no puede ser negativo")
    private BigDecimal porcentajeDescuento = BigDecimal.ZERO;

    /**
     * Monto del descuento aplicado
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal montoDescuento = BigDecimal.ZERO;

    /**
     * Subtotal de la línea (cantidad * precioUnitario - descuento)
     */
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal subtotal;

    /**
     * Cantidad recibida hasta el momento
     */
    @Column(nullable = false)
    @PositiveOrZero(message = "La cantidad recibida no puede ser negativa")
    private Integer cantidadRecibida = 0;

    /**
     * Observaciones específicas de esta línea
     */
    @Column(length = 500)
    private String observaciones;

    /**
     * Código del producto en el catálogo del proveedor
     */
    @Column(length = 100)
    private String codigoProveedor;

    /**
     * Nombre del producto según el proveedor
     * Puede diferir del nombre en el sistema
     */
    @Column(length = 200)
    private String nombreProveedor;

    /**
     * Número de línea en la orden (para ordenamiento)
     */
    private Integer numeroLinea;

    /**
     * Calcula el subtotal de la línea
     */
    @PrePersist
    @PreUpdate
    public void calcularSubtotal() {
        if (cantidad != null && precioUnitario != null) {
            BigDecimal subtotalBruto = precioUnitario.multiply(new BigDecimal(cantidad));

            // Calcular descuento
            if (porcentajeDescuento != null && porcentajeDescuento.compareTo(BigDecimal.ZERO) > 0) {
                this.montoDescuento = subtotalBruto
                        .multiply(porcentajeDescuento)
                        .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
            } else {
                this.montoDescuento = BigDecimal.ZERO;
            }

            this.subtotal = subtotalBruto.subtract(montoDescuento);
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }

    /**
     * Verifica si la línea está completamente recibida
     */
    @Transient
    public boolean estaCompleta() {
        return cantidadRecibida != null && cantidad != null
                && cantidadRecibida.equals(cantidad);
    }

    /**
     * Verifica si hay recepción parcial
     */
    @Transient
    public boolean tienePendiente() {
        return cantidadRecibida != null && cantidad != null
                && cantidadRecibida < cantidad;
    }

    /**
     * Obtiene la cantidad pendiente de recibir
     */
    @Transient
    public Integer getCantidadPendiente() {
        if (cantidad == null) return 0;
        if (cantidadRecibida == null) return cantidad;
        return cantidad - cantidadRecibida;
    }

    /**
     * Registra recepción de unidades
     *
     * @param cantidadARecibir Cantidad a marcar como recibida
     * @throws IllegalArgumentException si se intenta recibir más de lo ordenado
     */
    public void registrarRecepcion(Integer cantidadARecibir) {
        if (cantidadARecibir == null || cantidadARecibir <= 0) {
            throw new IllegalArgumentException("La cantidad a recibir debe ser mayor que cero");
        }

        if (cantidadRecibida == null) {
            cantidadRecibida = 0;
        }

        int nuevaCantidadRecibida = cantidadRecibida + cantidadARecibir;

        if (nuevaCantidadRecibida > cantidad) {
            throw new IllegalArgumentException(
                    String.format("No se puede recibir %d unidades. " +
                                    "Ordenado: %d, Ya recibido: %d, Pendiente: %d",
                            cantidadARecibir, cantidad, cantidadRecibida, getCantidadPendiente()));
        }

        this.cantidadRecibida = nuevaCantidadRecibida;
    }

    /**
     * Obtiene el porcentaje de cumplimiento de la recepción
     */
    @Transient
    public Double getPorcentajeRecibido() {
        if (cantidad == null || cantidad == 0) return 0.0;
        if (cantidadRecibida == null) return 0.0;
        return (cantidadRecibida.doubleValue() / cantidad.doubleValue()) * 100.0;
    }

    /**
     * Constructor de conveniencia para crear detalles
     */
    public DetalleOrdenCompra(Producto producto, Integer cantidad, BigDecimal precioUnitario) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.cantidadRecibida = 0;
        calcularSubtotal();
    }

    /**
     * Constructor de conveniencia con descuento
     */
    public DetalleOrdenCompra(Producto producto, Integer cantidad, BigDecimal precioUnitario,
                              BigDecimal porcentajeDescuento) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.porcentajeDescuento = porcentajeDescuento;
        this.cantidadRecibida = 0;
        calcularSubtotal();
    }
}
