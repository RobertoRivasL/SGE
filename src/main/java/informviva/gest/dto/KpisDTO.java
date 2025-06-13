package informviva.gest.dto;


import java.math.BigDecimal;

public class KpisDTO {
    private BigDecimal totalVentas = BigDecimal.ZERO;
    private BigDecimal variacionVentas = BigDecimal.ZERO;
    private BigDecimal variacionTransacciones = BigDecimal.ZERO;
    private BigDecimal ticketPromedio = BigDecimal.ZERO;
    private Integer totalProductos = 0;
    private Integer productosActivos = 0;
    private Integer productosBajoStock = 0;
    private Integer totalClientes = 0;

    // Getters y Setters
    public BigDecimal getTotalVentas() {
        return totalVentas;
    }

    public void setTotalVentas(BigDecimal totalVentas) {
        this.totalVentas = totalVentas;
    }

    public BigDecimal getVariacionVentas() {
        return variacionVentas;
    }

    public void setVariacionVentas(BigDecimal variacionVentas) {
        this.variacionVentas = variacionVentas;
    }

    public BigDecimal getVariacionTransacciones() {
        return variacionTransacciones;
    }

    public void setVariacionTransacciones(BigDecimal variacionTransacciones) {
        this.variacionTransacciones = variacionTransacciones;
    }

    public BigDecimal getTicketPromedio() {
        return ticketPromedio;
    }

    public void setTicketPromedio(BigDecimal ticketPromedio) {
        this.ticketPromedio = ticketPromedio;
    }

    public Integer getTotalProductos() {
        return totalProductos;
    }

    public void setTotalProductos(Integer totalProductos) {
        this.totalProductos = totalProductos;
    }

    public Integer getProductosActivos() {
        return productosActivos;
    }

    public void setProductosActivos(Integer productosActivos) {
        this.productosActivos = productosActivos;
    }

    public Integer getProductosBajoStock() {
        return productosBajoStock;
    }

    public void setProductosBajoStock(Integer productosBajoStock) {
        this.productosBajoStock = productosBajoStock;
    }

    public Integer getTotalClientes() {
        return totalClientes;
    }

    public void setTotalClientes(Integer totalClientes) {
        this.totalClientes = totalClientes;
    }
}
