package informviva.gest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para exportación de ventas
 * Migrado para usar LocalDateTime
 *
 * @author Roberto Rivas
 * @version 3.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaExportDTO {
    private Long id;
    private LocalDateTime fecha;
    private String clienteNombre;
    private String clienteRut;
    private String clienteEmail;
    private String vendedorNombre;
    private String vendedorEmail;
    private Double subtotal;
    private Double impuesto;
    private Double descuento;
    private Double total;
    private String metodoPago;
    private String estado;
    private String observaciones;

    // Información adicional para reportes
    private Integer cantidadItems;
    private String tipoVenta; // "CONTADO", "CREDITO"
    private String sucursal;
    private String numeroFactura;
    private List<VentaDetalleExportDTO> detalles;
}