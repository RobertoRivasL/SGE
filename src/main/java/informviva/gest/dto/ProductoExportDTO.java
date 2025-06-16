package informviva.gest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para exportación de productos
 * Migrado para usar LocalDateTime
 *
 * @author Roberto Rivas
 * @version 3.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoExportDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock;
    private String categoria;
    private String marca;
    private String modelo;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // Campos adicionales para exportación
    private Double precioCompra;
    private String proveedor;
    private String ubicacion;
    private Integer stockMinimo;
    private String estado; // "ACTIVO", "INACTIVO", "BAJO_STOCK", "AGOTADO"
}