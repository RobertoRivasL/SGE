package informviva.gest.dto;

/**
 * DTO para exportación de productos
 *
 * @author Roberto Rivas
 * @version 2.0
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


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
    private Long unidadesVendidas;
    private Double ingresosTotales;
}