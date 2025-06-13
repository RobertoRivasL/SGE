package informviva.gest.dto;

/**
 * DTO para respuesta de exportación
 *
 * @author Roberto Rivas
 * @version 2.0
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
class ExportacionResponseDTO {
    private String nombreArchivo;
    private String mimeType;
    private Long tamanioBytes;
    private Integer totalRegistros;
    private LocalDateTime fechaGeneracion;
    private String estadoExportacion; // "EXITOSO", "ERROR", "PARCIAL"
    private String mensajeError;
    private Map<String, Object> metadatos;
}