package informviva.gest.dto;

/**
 * DTO para configuración de exportación
 *
 * @author Roberto Rivas
 * @version 2.0
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
class ConfiguracionExportDTO {
    private String formato; // PDF, EXCEL, CSV
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String tipoExport; // CLIENTES, PRODUCTOS, VENTAS, etc.
    private Map<String, Object> filtros;
    private List<String> columnasIncluir;
    private Boolean incluirTotales;
    private Boolean incluirGraficos;
    private String ordenarPor;
    private String direccionOrden; // ASC, DESC
    private Integer limiteRegistros;
}