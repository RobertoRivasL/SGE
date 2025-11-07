package informviva.gest.dto;

import informviva.gest.model.OrdenCompra.EstadoOrden;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO (Data Transfer Object) para la entidad OrdenCompra
 *
 * Representa una orden de compra completa con sus detalles
 *
 * @author Roberto Rivas
 * @version 1.0
 * @since FASE 3 - Módulo de Compras
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenCompraDTO {

    /**
     * Identificador único de la orden
     */
    private Long id;

    /**
     * Número único de la orden de compra
     * Generado automáticamente
     */
    private String numeroOrden;

    /**
     * ID del proveedor
     */
    @NotNull(message = "El proveedor es obligatorio")
    private Long proveedorId;

    /**
     * Nombre del proveedor
     * Solo para visualización
     */
    private String proveedorNombre;

    /**
     * RUT del proveedor
     * Solo para visualización
     */
    private String proveedorRut;

    /**
     * Fecha de emisión de la orden
     */
    @NotNull(message = "La fecha de orden es obligatoria")
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
    @NotNull(message = "El estado es obligatorio")
    private EstadoOrden estado = EstadoOrden.PENDIENTE;

    /**
     * ID del usuario que creó la orden (comprador)
     */
    @NotNull(message = "El usuario comprador es obligatorio")
    private Long usuarioCompradorId;

    /**
     * Nombre del usuario comprador
     * Solo para visualización
     */
    private String usuarioCompradorNombre;

    /**
     * ID del usuario que aprobó la orden
     */
    private Long usuarioAprobadorId;

    /**
     * Nombre del usuario aprobador
     * Solo para visualización
     */
    private String usuarioAprobadorNombre;

    /**
     * Fecha de aprobación
     */
    private LocalDateTime fechaAprobacion;

    /**
     * ID del usuario que recibió la orden
     */
    private Long usuarioReceptorId;

    /**
     * Nombre del usuario receptor
     * Solo para visualización
     */
    private String usuarioReceptorNombre;

    /**
     * Fecha de recepción
     */
    private LocalDateTime fechaRecepcion;

    /**
     * Detalles de la orden (líneas de productos)
     */
    @NotNull(message = "La orden debe tener al menos un detalle")
    @Size(min = 1, message = "La orden debe tener al menos un detalle")
    private List<DetalleOrdenCompraDTO> detalles = new ArrayList<>();

    /**
     * Subtotal (suma de líneas sin impuestos)
     */
    @NotNull(message = "El subtotal es obligatorio")
    @PositiveOrZero(message = "El subtotal no puede ser negativo")
    @Digits(integer = 12, fraction = 2, message = "El subtotal debe tener máximo 12 dígitos enteros y 2 decimales")
    private BigDecimal subtotal = BigDecimal.ZERO;

    /**
     * Porcentaje de impuesto (IVA)
     */
    @PositiveOrZero(message = "El porcentaje de impuesto no puede ser negativo")
    @DecimalMax(value = "100.00", message = "El porcentaje de impuesto no puede ser mayor a 100%")
    @Digits(integer = 3, fraction = 2, message = "El porcentaje debe tener máximo 3 dígitos enteros y 2 decimales")
    private BigDecimal porcentajeImpuesto = new BigDecimal("19.00"); // IVA 19% por defecto

    /**
     * Monto del impuesto
     */
    @PositiveOrZero(message = "El monto del impuesto no puede ser negativo")
    @Digits(integer = 12, fraction = 2, message = "El monto del impuesto debe tener máximo 12 dígitos enteros y 2 decimales")
    private BigDecimal montoImpuesto = BigDecimal.ZERO;

    /**
     * Descuento aplicado
     */
    @PositiveOrZero(message = "El descuento no puede ser negativo")
    @Digits(integer = 12, fraction = 2, message = "El descuento debe tener máximo 12 dígitos enteros y 2 decimales")
    private BigDecimal descuento = BigDecimal.ZERO;

    /**
     * Total de la orden (subtotal + impuesto - descuento)
     */
    @NotNull(message = "El total es obligatorio")
    @PositiveOrZero(message = "El total no puede ser negativo")
    @Digits(integer = 12, fraction = 2, message = "El total debe tener máximo 12 dígitos enteros y 2 decimales")
    private BigDecimal total = BigDecimal.ZERO;

    /**
     * Observaciones o notas de la orden
     */
    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;

    /**
     * Condiciones de pago
     */
    @Size(max = 200, message = "Las condiciones de pago no pueden exceder 200 caracteres")
    private String condicionesPago;

    /**
     * Método de pago
     */
    @Size(max = 50, message = "El método de pago no puede exceder 50 caracteres")
    private String metodoPago;

    /**
     * Dirección de entrega
     */
    @Size(max = 500, message = "La dirección de entrega no puede exceder 500 caracteres")
    private String direccionEntrega;

    /**
     * Referencia del proveedor (número de cotización, etc.)
     */
    @Size(max = 100, message = "La referencia del proveedor no puede exceder 100 caracteres")
    private String referenciaProveedor;

    /**
     * Motivo de cancelación (si aplica)
     */
    @Size(max = 500, message = "El motivo de cancelación no puede exceder 500 caracteres")
    private String motivoCancelacion;

    /**
     * Fecha de cancelación
     */
    private LocalDateTime fechaCancelacion;

    /**
     * Fecha de creación del registro
     */
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de última actualización
     */
    private LocalDateTime fechaActualizacion;

    /**
     * Constructor para crear orden con datos básicos
     *
     * @param proveedorId ID del proveedor
     * @param usuarioCompradorId ID del usuario comprador
     */
    public OrdenCompraDTO(Long proveedorId, Long usuarioCompradorId) {
        this.proveedorId = proveedorId;
        this.usuarioCompradorId = usuarioCompradorId;
        this.fechaOrden = LocalDate.now();
        this.estado = EstadoOrden.BORRADOR;
        this.porcentajeImpuesto = new BigDecimal("19.00");
        this.detalles = new ArrayList<>();
    }

    /**
     * Agrega un detalle a la orden
     *
     * @param detalle Detalle a agregar
     */
    public void agregarDetalle(DetalleOrdenCompraDTO detalle) {
        if (this.detalles == null) {
            this.detalles = new ArrayList<>();
        }
        detalle.setNumeroLinea(this.detalles.size() + 1);
        this.detalles.add(detalle);
        calcularTotales();
    }

    /**
     * Elimina un detalle de la orden
     *
     * @param detalleId ID del detalle a eliminar
     */
    public void eliminarDetalle(Long detalleId) {
        if (this.detalles != null) {
            this.detalles.removeIf(d -> d.getId() != null && d.getId().equals(detalleId));
            recalcularNumerosLinea();
            calcularTotales();
        }
    }

    /**
     * Recalcula los números de línea de los detalles
     */
    private void recalcularNumerosLinea() {
        if (this.detalles != null) {
            for (int i = 0; i < this.detalles.size(); i++) {
                this.detalles.get(i).setNumeroLinea(i + 1);
            }
        }
    }

    /**
     * Calcula el subtotal sumando todos los detalles
     */
    public void calcularSubtotal() {
        if (this.detalles == null || this.detalles.isEmpty()) {
            this.subtotal = BigDecimal.ZERO;
            return;
        }

        this.subtotal = detalles.stream()
                .map(detalle -> {
                    detalle.calcularSubtotal();
                    return detalle.getSubtotal();
                })
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
    public void calcularTotales() {
        calcularTotal();
    }

    /**
     * Verifica si la orden puede ser modificada
     *
     * @return true si está en estado BORRADOR o PENDIENTE
     */
    public boolean esModificable() {
        return estado != null && estado.esModificable();
    }

    /**
     * Verifica si la orden puede recibir mercancía
     *
     * @return true si está en estado recibible
     */
    public boolean esRecibible() {
        return estado != null && estado.esRecibible();
    }

    /**
     * Verifica si la orden puede ser cancelada
     *
     * @return true si no está COMPLETADA ni CANCELADA
     */
    public boolean esCancelable() {
        return estado != null && estado.esCancelable();
    }

    /**
     * Obtiene el número de líneas/detalles
     *
     * @return Cantidad de detalles
     */
    public int getCantidadLineas() {
        return detalles != null ? detalles.size() : 0;
    }

    /**
     * Obtiene el total de unidades ordenadas
     *
     * @return Total de unidades en todos los detalles
     */
    public Integer getTotalUnidades() {
        if (detalles == null || detalles.isEmpty()) {
            return 0;
        }
        return detalles.stream()
                .map(DetalleOrdenCompraDTO::getCantidad)
                .reduce(0, Integer::sum);
    }

    /**
     * Obtiene el total de unidades recibidas
     *
     * @return Total de unidades recibidas en todos los detalles
     */
    public Integer getTotalUnidadesRecibidas() {
        if (detalles == null || detalles.isEmpty()) {
            return 0;
        }
        return detalles.stream()
                .map(DetalleOrdenCompraDTO::getCantidadRecibida)
                .reduce(0, Integer::sum);
    }

    /**
     * Obtiene el porcentaje de recepción de la orden
     *
     * @return Porcentaje de unidades recibidas vs ordenadas
     */
    public Double getPorcentajeRecepcion() {
        Integer totalUnidades = getTotalUnidades();
        if (totalUnidades == null || totalUnidades == 0) {
            return 0.0;
        }
        Integer totalRecibidas = getTotalUnidadesRecibidas();
        return (totalRecibidas.doubleValue() / totalUnidades.doubleValue()) * 100.0;
    }

    /**
     * Método para validar si el DTO tiene los datos mínimos requeridos
     *
     * @return true si tiene los datos básicos válidos
     */
    public boolean esValido() {
        return proveedorId != null
                && usuarioCompradorId != null
                && fechaOrden != null
                && estado != null
                && detalles != null && !detalles.isEmpty()
                && detalles.stream().allMatch(DetalleOrdenCompraDTO::esValido);
    }

    /**
     * Genera una representación compacta de la orden
     *
     * @return String con formato descriptivo
     */
    public String toStringCompacto() {
        return String.format("Orden %s - %s - Estado: %s - Total: $%s",
                numeroOrden != null ? numeroOrden : "N/A",
                proveedorNombre != null ? proveedorNombre : "Proveedor #" + proveedorId,
                estado != null ? estado.getDescripcion() : "N/A",
                total != null ? total.toString() : "0.00");
    }

    /**
     * Obtiene el estado como texto descriptivo
     *
     * @return Descripción del estado
     */
    public String getEstadoDescripcion() {
        return estado != null ? estado.getDescripcion() : "Sin estado";
    }

    /**
     * Verifica si todos los detalles están completamente recibidos
     *
     * @return true si todos los detalles están completos
     */
    public boolean todosLosDetallesRecibidos() {
        if (detalles == null || detalles.isEmpty()) {
            return false;
        }
        return detalles.stream().allMatch(DetalleOrdenCompraDTO::estaCompleta);
    }

    /**
     * Verifica si hay al menos un detalle parcialmente recibido
     *
     * @return true si hay recepción parcial
     */
    public boolean tieneRecepcionParcial() {
        if (detalles == null || detalles.isEmpty()) {
            return false;
        }
        return detalles.stream().anyMatch(detalle ->
                detalle.getCantidadRecibida() != null && detalle.getCantidadRecibida() > 0
                        && !detalle.estaCompleta());
    }
}
