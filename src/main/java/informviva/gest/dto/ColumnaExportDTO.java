package informviva.gest.dto;

/**
 * @author Roberto Rivas
 * @version 2.0
 *
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para configuración de columnas en exportaciones
 *
 * @author Roberto Rivas
 * @version 3.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColumnaExportDTO {
    private String campo;
    private String titulo;
    private String tipo; // "STRING", "NUMBER", "DATE", "BOOLEAN"
    private Boolean visible;
    private Integer orden;
    private Integer ancho;
    private String formato; // Para fechas y números
    private String alineacion; // "LEFT", "CENTER", "RIGHT"
    private Boolean agrupable;
    private Boolean ordenable;
}