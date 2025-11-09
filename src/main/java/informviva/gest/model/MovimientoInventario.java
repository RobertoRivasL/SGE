package informviva.gest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad para registrar todos los movimientos de inventario
 * Permite trazabilidad completa de entradas y salidas de productos
 *
 * @author Roberto Rivas
 * @version 1.0
 * @since FASE 2 - Módulo de Inventario
 */
@Entity
@Table(name = "movimientos_inventario", indexes = {
    @Index(name = "idx_producto", columnList = "producto_id"),
    @Index(name = "idx_tipo", columnList = "tipo"),
    @Index(name = "idx_fecha", columnList = "fecha"),
    @Index(name = "idx_usuario", columnList = "usuario_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Producto afectado por el movimiento
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @NotNull(message = "El producto es obligatorio")
    private Producto producto;

    /**
     * Tipo de movimiento
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "El tipo de movimiento es obligatorio")
    private TipoMovimiento tipo;

    /**
     * Cantidad del movimiento (positiva para entradas, valor absoluto para salidas)
     */
    @Column(nullable = false)
    @Positive(message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;

    /**
     * Stock anterior antes del movimiento
     */
    @Column(nullable = false)
    private Integer stockAnterior;

    /**
     * Stock resultante después del movimiento
     */
    @Column(nullable = false)
    private Integer stockNuevo;

    /**
     * Motivo o razón del movimiento
     */
    @Column(length = 500)
    private String motivo;

    /**
     * Referencia externa (ID de venta, compra, ajuste, etc.)
     */
    @Column(length = 50)
    private String referenciaExterna;

    /**
     * Usuario que registró el movimiento
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @NotNull(message = "El usuario es obligatorio")
    private Usuario usuario;

    /**
     * Fecha y hora del movimiento
     */
    @Column(nullable = false)
    private LocalDateTime fecha;

    /**
     * Observaciones adicionales
     */
    @Column(length = 1000)
    private String observaciones;

    /**
     * Costo unitario del producto en el momento del movimiento (para valorización)
     */
    @Column(precision = 10, scale = 2)
    private Double costoUnitario;

    /**
     * Costo total del movimiento (cantidad * costoUnitario)
     */
    @Column(precision = 10, scale = 2)
    private Double costoTotal;

    /**
     * Enum para tipos de movimiento de inventario
     */
    public enum TipoMovimiento {
        /**
         * Entrada por compra a proveedor
         */
        COMPRA("Compra", true),

        /**
         * Entrada por devolución de cliente
         */
        DEVOLUCION_ENTRADA("Devolución Entrada", true),

        /**
         * Salida por venta a cliente
         */
        VENTA("Venta", false),

        /**
         * Salida por devolución a proveedor
         */
        DEVOLUCION_SALIDA("Devolución Salida", false),

        /**
         * Ajuste positivo (corrección, inventario encontrado, etc.)
         */
        AJUSTE_POSITIVO("Ajuste Positivo", true),

        /**
         * Ajuste negativo (merma, robo, daño, etc.)
         */
        AJUSTE_NEGATIVO("Ajuste Negativo", false),

        /**
         * Transferencia entre bodegas/sucursales (entrada)
         */
        TRANSFERENCIA_ENTRADA("Transferencia Entrada", true),

        /**
         * Transferencia entre bodegas/sucursales (salida)
         */
        TRANSFERENCIA_SALIDA("Transferencia Salida", false),

        /**
         * Entrada inicial (inventario inicial al crear producto)
         */
        INVENTARIO_INICIAL("Inventario Inicial", true);

        private final String descripcion;
        private final boolean esEntrada;

        TipoMovimiento(String descripcion, boolean esEntrada) {
            this.descripcion = descripcion;
            this.esEntrada = esEntrada;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public boolean esEntrada() {
            return esEntrada;
        }

        public boolean esSalida() {
            return !esEntrada;
        }
    }

    /**
     * Constructor de conveniencia para crear movimientos
     */
    public MovimientoInventario(Producto producto, TipoMovimiento tipo, Integer cantidad,
                                Integer stockAnterior, Usuario usuario, String motivo) {
        this.producto = producto;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.stockAnterior = stockAnterior;
        this.stockNuevo = calcularStockNuevo(stockAnterior, cantidad, tipo);
        this.usuario = usuario;
        this.motivo = motivo;
        this.fecha = LocalDateTime.now();
    }

    /**
     * Calcula el stock nuevo basado en el tipo de movimiento
     */
    private Integer calcularStockNuevo(Integer stockAnterior, Integer cantidad, TipoMovimiento tipo) {
        if (stockAnterior == null) stockAnterior = 0;
        if (cantidad == null) cantidad = 0;

        return tipo.esEntrada() ? stockAnterior + cantidad : stockAnterior - cantidad;
    }

    /**
     * Callback antes de persistir: establece fecha y calcula costo total
     */
    @PrePersist
    public void antesDeGuardar() {
        // Establecer fecha si no está definida
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }

        // Calcular costo total
        if (this.costoUnitario != null && this.cantidad != null) {
            this.costoTotal = this.costoUnitario * this.cantidad;
        }
    }

    /**
     * Callback antes de actualizar: recalcula costo total
     */
    @PreUpdate
    public void antesDeActualizar() {
        if (this.costoUnitario != null && this.cantidad != null) {
            this.costoTotal = this.costoUnitario * this.cantidad;
        }
    }

    /**
     * Verifica si el movimiento es una entrada
     */
    @Transient
    public boolean esEntrada() {
        return tipo != null && tipo.esEntrada();
    }

    /**
     * Verifica si el movimiento es una salida
     */
    @Transient
    public boolean esSalida() {
        return tipo != null && tipo.esSalida();
    }

    /**
     * Obtiene la diferencia de stock (con signo)
     */
    @Transient
    public Integer getDiferenciaStock() {
        if (stockNuevo == null || stockAnterior == null) return 0;
        return stockNuevo - stockAnterior;
    }
}
