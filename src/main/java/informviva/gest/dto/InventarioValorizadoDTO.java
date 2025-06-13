package informviva.gest.dto;

/**
 * DTO para exportación de inventario valorizado
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
class InventarioValorizadoDTO {
    private Long productoId;
    private String codigo;
    private String nombre;
    private String categoria;
    private Integer stockActual;
    private Double precioUnitario;
    private Double valorInventario;
    private String estadoStock; // "CRITICO", "BAJO", "NORMAL", "ALTO"
    private LocalDateTime ultimaActualizacion;
}