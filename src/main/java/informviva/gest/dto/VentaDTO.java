package informviva.gest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VentaDTO {

    // ==================== PROPIEDADES PRINCIPALES ====================

    private Long id;

    @NotNull(message = "Debe seleccionar un cliente.")
    private Long clienteId;

    private Long vendedorId;

    private LocalDateTime fecha;

    // ✅ Para compatibilidad con código existente
    private LocalDateTime fechaVenta;

    @NotEmpty(message = "Debe añadir al menos un producto a la venta.")
    @Valid
    private List<ProductoVentaDTO> productos;

    // ==================== PROPIEDADES FINANCIERAS ====================

    private Double subtotal;

    private Double impuesto;

    private Double total;

    private String metodoPago;

    // ==================== PROPIEDADES DE ESTADO Y CONTROL ====================

    private String estado;

    private String observaciones;

    // ==================== PROPIEDADES ADICIONALES ====================

    private String clienteNombre;

    private String vendedorNombre;

    private Integer totalItems;

    // ==================== CLASE INTERNA PARA PRODUCTOS ====================

    @Data
    public static class ProductoVentaDTO {

        @NotNull(message = "Debe seleccionar un producto.")
        private Long productoId;

        @Min(value = 1, message = "La cantidad debe ser al menos 1.")
        private Integer cantidad;

        private Double precioUnitario;

        private Double subtotal;

        private String productoNombre;

        private String productoCodigo;

        // ==================== CONSTRUCTORES ====================

        public ProductoVentaDTO() {}

        public ProductoVentaDTO(Long productoId, Integer cantidad) {
            this.productoId = productoId;
            this.cantidad = cantidad;
        }

        public ProductoVentaDTO(Long productoId, Integer cantidad, Double precioUnitario) {
            this.productoId = productoId;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.subtotal = cantidad * precioUnitario;
        }

        // ==================== MÉTODOS DE CÁLCULO ====================

        public Double calcularSubtotal() {
            if (cantidad != null && precioUnitario != null) {
                return cantidad * precioUnitario;
            }
            return 0.0;
        }

        public void actualizarSubtotal() {
            this.subtotal = calcularSubtotal();
        }
    }

    // ==================== CONSTRUCTORES ====================

    public VentaDTO() {}

    public VentaDTO(Long clienteId, List<ProductoVentaDTO> productos) {
        this.clienteId = clienteId;
        this.productos = productos;
        this.fecha = LocalDateTime.now();
        this.fechaVenta = this.fecha; // Para compatibilidad
        calcularTotales();
    }

    // ==================== MÉTODOS DE CÁLCULO ====================

    public void calcularTotales() {
        if (productos == null || productos.isEmpty()) {
            this.subtotal = 0.0;
            this.impuesto = 0.0;
            this.total = 0.0;
            this.totalItems = 0;
            return;
        }

        this.subtotal = productos.stream()
                .mapToDouble(ProductoVentaDTO::calcularSubtotal)
                .sum();

        this.impuesto = this.subtotal * 0.19; // 19% IVA - ajustar según tu país

        this.total = this.subtotal + this.impuesto;

        this.totalItems = productos.stream()
                .mapToInt(p -> p.getCantidad() != null ? p.getCantidad() : 0)
                .sum();
    }

    public void actualizarTotales() {
        calcularTotales();
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    public void agregarProducto(Long productoId, Integer cantidad, Double precio) {
        if (this.productos == null) {
            this.productos = new java.util.ArrayList<>();
        }

        ProductoVentaDTO nuevoProducto = new ProductoVentaDTO(productoId, cantidad, precio);
        this.productos.add(nuevoProducto);
        calcularTotales();
    }

    public void eliminarProducto(Long productoId) {
        if (this.productos != null) {
            this.productos.removeIf(p -> p.getProductoId().equals(productoId));
            calcularTotales();
        }
    }

    public boolean tieneProductos() {
        return productos != null && !productos.isEmpty();
    }

    public int getCantidadProductos() {
        return productos != null ? productos.size() : 0;
    }

    // ==================== GETTERS Y SETTERS PERSONALIZADOS ====================

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
        this.fechaVenta = fecha; // Mantener sincronizados para compatibilidad
    }

    public void setFechaVenta(LocalDateTime fechaVenta) {
        this.fechaVenta = fechaVenta;
        this.fecha = fechaVenta; // Mantener sincronizados para compatibilidad
    }

    public LocalDateTime getFechaVenta() {
        return this.fechaVenta != null ? this.fechaVenta : this.fecha;
    }

    // ==================== MÉTODOS DE VALIDACIÓN ====================

    public boolean esValida() {
        return clienteId != null &&
                productos != null &&
                !productos.isEmpty() &&
                productos.stream().allMatch(p ->
                        p.getProductoId() != null &&
                                p.getCantidad() != null &&
                                p.getCantidad() > 0
                );
    }

    public String obtenerErroresValidacion() {
        if (clienteId == null) {
            return "Debe seleccionar un cliente";
        }
        if (productos == null || productos.isEmpty()) {
            return "Debe agregar al menos un producto";
        }
        for (ProductoVentaDTO producto : productos) {
            if (producto.getProductoId() == null) {
                return "Todos los productos deben estar seleccionados";
            }
            if (producto.getCantidad() == null || producto.getCantidad() <= 0) {
                return "Todas las cantidades deben ser mayores a 0";
            }
        }
        return null;
    }

    // ==================== MÉTODOS toString, equals, hashCode ====================

    @Override
    public String toString() {
        return "VentaDTO{" +
                "id=" + id +
                ", clienteId=" + clienteId +
                ", fecha=" + fecha +
                ", total=" + total +
                ", estado='" + estado + '\'' +
                ", productos=" + (productos != null ? productos.size() + " productos" : "sin productos") +
                '}';
    }
}