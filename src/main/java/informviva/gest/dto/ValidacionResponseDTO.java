package informviva.gest.dto;


/**
 * @author Roberto Rivas
 * @version 2.0
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para las respuestas de validación del sistema
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidacionResponseDTO {

    /**
     * Indica si la validación fue exitosa
     */
    private Boolean valido;

    /**
     * Mensaje descriptivo del resultado de la validación
     */
    private String mensaje;

    /**
     * Valor validado (puede ser formateado)
     */
    private String valor;

    /**
     * Timestamp de cuando se realizó la validación
     */
    private LocalDateTime timestamp;

    /**
     * Constructor de conveniencia para validaciones simples
     */
    public ValidacionResponseDTO(Boolean valido, String mensaje) {
        this.valido = valido;
        this.mensaje = mensaje;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor de conveniencia para validaciones con valor
     */
    public ValidacionResponseDTO(Boolean valido, String mensaje, String valor) {
        this.valido = valido;
        this.mensaje = mensaje;
        this.valor = valor;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Método estático para crear respuestas exitosas
     */
    public static ValidacionResponseDTO exito(String mensaje, String valor) {
        return new ValidacionResponseDTO(true, mensaje, valor, LocalDateTime.now());
    }

    /**
     * Método estático para crear respuestas de error
     */
    public static ValidacionResponseDTO error(String mensaje, String valor) {
        return new ValidacionResponseDTO(false, mensaje, valor, LocalDateTime.now());
    }

    /**
     * Método estático para crear respuestas de error sin valor
     */
    public static ValidacionResponseDTO error(String mensaje) {
        return new ValidacionResponseDTO(false, mensaje, null, LocalDateTime.now());
    }
}