package informviva.gest.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO específico para el resultado de importación de clientes
 * Extiende ImportacionResultadoDTO para reutilizar funcionalidad común
 * y permitir extensiones específicas si se requieren en el futuro
 *
 * @author Roberto Rivas
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ResultadoImportacionCliente extends ImportacionResultadoDTO {

    /**
     * Constructor por defecto
     */
    public ResultadoImportacionCliente() {
        super();
    }

    /**
     * Constructor de conveniencia para inicialización básica
     */
    public ResultadoImportacionCliente(String nombreArchivo) {
        super("Cliente", nombreArchivo);
    }

    // NOTA: Esta clase actualmente no agrega campos adicionales,
    // pero existe como tipo específico para:
    // 1. Claridad en las firmas de métodos (type safety)
    // 2. Permitir extensiones futuras específicas de clientes
    // 3. Mantener consistencia con el patrón de diseño del sistema
}
