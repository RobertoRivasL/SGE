package informviva.gest.dto;


import informviva.gest.model.Cliente;
import informviva.gest.model.Producto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricaDashboardDTO {
    private Double totalVentas;
    private Long totalTransacciones;
    private Double ticketPromedio;
    private Long clientesNuevos;
    private Long productosVendidos;
    private Double porcentajeCambioVentas;
    private Double porcentajeCambioTransacciones;
    private Double porcentajeCambioTicket;
    private Double porcentajeCambioClientes;
    private Double porcentajeCambioProductos;
}