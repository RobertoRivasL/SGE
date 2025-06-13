package informviva.gest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO para configuración de exportación
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportConfigDTO {
    private String tipo;              // usuarios, clientes, productos, ventas, reportes
    private String formato;           // pdf, excel, csv, json
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Map<String, Object> filtros = new HashMap<>();
    private String usuarioSolicitante;
    private LocalDateTime fechaSolicitud;

    /**
     * Agrega un filtro a la configuración
     */
    public void addFiltro(String clave, Object valor) {
        if (valor != null) {
            filtros.put(clave, valor);
        }
    }

    /**
     * Obtiene un filtro específico
     */
    @SuppressWarnings("unchecked")
    public <T> T getFiltro(String clave, Class<T> tipo) {
        Object valor = filtros.get(clave);
        if (valor != null && tipo.isAssignableFrom(valor.getClass())) {
            return (T) valor;
        }
        return null;
    }

    /**
     * Verifica si un filtro existe y es verdadero
     */
    public boolean isFiltroActivo(String clave) {
        Object valor = filtros.get(clave);
        if (valor instanceof Boolean) {
            return (Boolean) valor;
        }
        return false;
    }
}