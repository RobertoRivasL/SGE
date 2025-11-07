package informviva.gest.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) para la entidad DetalleOrdenCompra
 *
 * Representa una línea/detalle de una orden de compra con información
 * del producto, cantidades y precios
 *
 * @author Roberto Rivas
 * @version 1.0
 * @since FASE 3 - Módulo de Compras
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleOrdenCompraDTO {

    /**
     * Identificador único del detalle
     */
    private Long id;

    /**
     * ID de la orden de compra a la que pertenece
     */
    @NotNull(message = "El ID de la orden de compra es obligatorio")
    private Long ordenCompraId;

    /**
     * ID del producto ordenado
     */
    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;

    /**
     * Nombre del producto
     * Solo para visualización, no se persiste
     */
    private String productoNombre;

    /**
     * Código/SKU del producto
     * Solo para visualización
     */
    private String productoCodigo;

    /**
     * Cantidad ordenada
     */
    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;

    /**
     * Precio unitario del producto en esta orden
     */
    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio unitario debe ser mayor que cero")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 dígitos enteros y 2 decimales")
    private BigDecimal precioUnitario;

    /**
     * Porcentaje de descuento aplicado a esta línea
     */
    @PositiveOrZero(message = "El descuento no puede ser negativo")
    @DecimalMax(value = "100.00", message = "El descuento no puede ser mayor a 100%")
    @Digits(integer = 3, fraction = 2, message = "El descuento debe tener máximo 3 dígitos enteros y 2 decimales")
    private BigDecimal porcentajeDescuento = BigDecimal.ZERO;

    /**
     * Monto del descuento aplicado
     * Calculado automáticamente
     */
    private BigDecimal montoDescuento = BigDecimal.ZERO;

    /**
     * Subtotal de la línea (cantidad * precioUnitario - descuento)
     * Calculado automáticamente
     */
    private BigDecimal subtotal;

    /**
     * Cantidad recibida hasta el momento
     */
    @NotNull(message = "La cantidad recibida es obligatoria")
    @PositiveOrZero(message = "La cantidad recibida no puede ser negativa")
    private Integer cantidadRecibida = 0;

    /**
     * Observaciones específicas de esta línea
     */
    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;

    /**
     * Código del producto en el catálogo del proveedor
     */
    @Size(max = 100, message = "El código del proveedor no puede exceder 100 caracteres")
    private String codigoProveedor;

    /**
     * Nombre del producto según el proveedor
     */
    @Size(max = 200, message = "El nombre del proveedor no puede exceder 200 caracteres")
    private String nombreProveedor;

    /**
     * Número de línea en la orden (para ordenamiento)
     */
    private Integer numeroLinea;

    /**
     * Constructor para crear detalle con datos básicos
     *
     * @param productoId ID del producto
     * @param cantidad Cantidad a ordenar
     * @param precioUnitario Precio unitario
     */
    public DetalleOrdenCompraDTO(Long productoId, Integer cantidad, BigDecimal precioUnitario) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.cantidadRecibida = 0;
        this.porcentajeDescuento = BigDecimal.ZERO;
        calcularSubtotal();
    }

    /**
     * Constructor con descuento
     *
     * @param productoId ID del producto
     * @param cantidad Cantidad a ordenar
     * @param precioUnitario Precio unitario
     * @param porcentajeDescuento Descuento en porcentaje
     */
    public DetalleOrdenCompraDTO(Long productoId, Integer cantidad, BigDecimal precioUnitario,
                                 BigDecimal porcentajeDescuento) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.porcentajeDescuento = porcentajeDescuento != null ? porcentajeDescuento : BigDecimal.ZERO;
        this.cantidadRecibida = 0;
        calcularSubtotal();
    }

    /**
     * Calcula el subtotal de la línea
     * Aplica la fórmula: (cantidad * precioUnitario) - descuento
     */
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
            this.montoDescuento = BigDecimal.ZERO;
        }
    }

    /**
     * Verifica si la línea está completamente recibida
     *
     * @return true si se ha recibido toda la cantidad ordenada
     */
    public boolean estaCompleta() {
        return cantidadRecibida != null && cantidad != null
                && cantidadRecibida.equals(cantidad);
    }

    /**
     * Verifica si hay recepción parcial
     *
     * @return true si se ha recibido algo pero no todo
     */
    public boolean tienePendiente() {
        return cantidadRecibida != null && cantidad != null
                && cantidadRecibida < cantidad;
    }

    /**
     * Obtiene la cantidad pendiente de recibir
     *
     * @return Cantidad que falta por recibir
     */
    public Integer getCantidadPendiente() {
        if (cantidad == null) return 0;
        if (cantidadRecibida == null) return cantidad;
        return cantidad - cantidadRecibida;
    }

    /**
     * Registra recepción de unidades
     *
     * @param cantidadARecibir Cantidad a marcar como recibida
     * @throws IllegalArgumentException si la cantidad es inválida
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
     *
     * @return Porcentaje de la cantidad recibida vs ordenada
     */
    public Double getPorcentajeRecibido() {
        if (cantidad == null || cantidad == 0) return 0.0;
        if (cantidadRecibida == null) return 0.0;
        return (cantidadRecibida.doubleValue() / cantidad.doubleValue()) * 100.0;
    }

    /**
     * Método para validar si el DTO tiene los datos mínimos requeridos
     *
     * @return true si tiene los datos básicos válidos
     */
    public boolean esValido() {
        return productoId != null
                && cantidad != null && cantidad > 0
                && precioUnitario != null && precioUnitario.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Genera una representación compacta del detalle
     *
     * @return String con formato descriptivo
     */
    public String toStringCompacto() {
        return String.format("%s - Cant: %d - Precio: $%s - Subtotal: $%s",
                productoNombre != null ? productoNombre : "Producto #" + productoId,
                cantidad != null ? cantidad : 0,
                precioUnitario != null ? precioUnitario.toString() : "0.00",
                subtotal != null ? subtotal.toString() : "0.00");
    }

    /**
     * Obtiene el estado de recepción como texto
     *
     * @return Estado de recepción
     */
    public String getEstadoRecepcion() {
        if (estaCompleta()) {
            return "COMPLETA";
        } else if (tienePendiente() && cantidadRecibida != null && cantidadRecibida > 0) {
            return "PARCIAL";
        } else {
            return "PENDIENTE";
        }
    }
}
