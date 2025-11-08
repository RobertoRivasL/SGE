package informviva.gest.dto;

import informviva.gest.model.MovimientoInventario.TipoMovimiento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para movimientos de inventario
 * Separa la capa de presentación del modelo de datos
 *
 * @author Roberto Rivas
 * @version 1.0
 * @since FASE 2 - Módulo de Inventario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInventarioDTO {

    private Long id;

    /**
     * ID del producto afectado
     */
    @NotNull(message = "El producto es obligatorio")
    private Long productoId;

    /**
     * Nombre del producto (para visualización)
     */
    private String productoNombre;

    /**
     * Código del producto (para visualización)
     */
    private String productoCodigo;

    /**
     * Tipo de movimiento
     */
    @NotNull(message = "El tipo de movimiento es obligatorio")
    private TipoMovimiento tipo;

    /**
     * Descripción del tipo de movimiento
     */
    private String tipoDescripcion;

    /**
     * Cantidad del movimiento
     */
    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;

    /**
     * Stock anterior
     */
    private Integer stockAnterior;

    /**
     * Stock nuevo después del movimiento
     */
    private Integer stockNuevo;

    /**
     * Diferencia de stock (con signo)
     */
    private Integer diferenciaStock;

    /**
     * Motivo del movimiento
     */
    private String motivo;

    /**
     * Referencia externa (ID de venta, compra, etc.)
     */
    private String referenciaExterna;

    /**
     * ID del usuario que registró el movimiento
     */
    @NotNull(message = "El usuario es obligatorio")
    private Long usuarioId;

    /**
     * Nombre del usuario que registró el movimiento
     */
    private String usuarioNombre;

    /**
     * Fecha y hora del movimiento
     */
    private LocalDateTime fecha;

    /**
     * Observaciones adicionales
     */
    private String observaciones;

    /**
     * Costo unitario
     */
    private Double costoUnitario;

    /**
     * Costo total
     */
    private Double costoTotal;

    /**
     * Indica si es una entrada
     */
    private Boolean esEntrada;

    /**
     * Indica si es una salida
     */
    private Boolean esSalida;

    /**
     * Constructor de conveniencia para creación simple
     */
    public MovimientoInventarioDTO(Long productoId, TipoMovimiento tipo, Integer cantidad,
                                   Integer stockAnterior, Long usuarioId, String motivo) {
        this.productoId = productoId;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.stockAnterior = stockAnterior;
        this.stockNuevo = tipo.esEntrada() ? stockAnterior + cantidad : stockAnterior - cantidad;
        this.diferenciaStock = this.stockNuevo - stockAnterior;
        this.usuarioId = usuarioId;
        this.motivo = motivo;
        this.fecha = LocalDateTime.now();
        this.esEntrada = tipo.esEntrada();
        this.esSalida = tipo.esSalida();
        this.tipoDescripcion = tipo.getDescripcion();
    }

    /**
     * Método para calcular valores derivados
     */
    public void calcularValoresDerivados() {
        if (tipo != null) {
            this.tipoDescripcion = tipo.getDescripcion();
            this.esEntrada = tipo.esEntrada();
            this.esSalida = tipo.esSalida();
        }

        if (stockNuevo != null && stockAnterior != null) {
            this.diferenciaStock = stockNuevo - stockAnterior;
        }

        if (costoUnitario != null && cantidad != null) {
            this.costoTotal = costoUnitario * cantidad;
        }
    }
}
