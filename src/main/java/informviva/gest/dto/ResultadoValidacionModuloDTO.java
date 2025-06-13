package informviva.gest.dto;

/**
 * @author Roberto Rivas
 * @version 2.0
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para el resultado de operaciones del módulo de respaldo
 * Consistente con el patrón de DTOs del proyecto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoValidacionModuloDTO {

    /**
     * Indica si la operación fue exitosa
     */
    private boolean exitoso;

    /**
     * Mensaje descriptivo del resultado
     */
    private String mensaje;

    /**
     * Ruta del archivo generado o procesado (opcional)
     */
    private String rutaArchivo;

    /**
     * Tamaño del archivo en bytes (opcional)
     */
    private long tamañoArchivo;

    /**
     * Constructor para casos de éxito sin archivo
     */
    public ResultadoValidacionModuloDTO(boolean exitoso, String mensaje) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.rutaArchivo = null;
        this.tamañoArchivo = 0;
    }

    /**
     * Método de conveniencia para crear resultado exitoso
     */
    public static ResultadoValidacionModuloDTO exito(String mensaje) {
        return new ResultadoValidacionModuloDTO(true, mensaje);
    }

    /**
     * Método de conveniencia para crear resultado exitoso con archivo
     */
    public static ResultadoValidacionModuloDTO exito(String mensaje, String rutaArchivo, long tamaño) {
        return new ResultadoValidacionModuloDTO(true, mensaje, rutaArchivo, tamaño);
    }

    /**
     * Método de conveniencia para crear resultado de error
     */
    public static ResultadoValidacionModuloDTO error(String mensaje) {
        return new ResultadoValidacionModuloDTO(false, mensaje);
    }

    /**
     * Obtiene el tamaño del archivo formateado
     */
    public String getTamañoFormateado() {
        if (tamañoArchivo == 0) {
            return "0 B";
        }

        String[] unidades = {"B", "KB", "MB", "GB"};
        int unidadIndex = 0;
        double tamaño = tamañoArchivo;

        while (tamaño >= 1024 && unidadIndex < unidades.length - 1) {
            tamaño /= 1024;
            unidadIndex++;
        }

        return String.format("%.1f %s", tamaño, unidades[unidadIndex]);
    }
}