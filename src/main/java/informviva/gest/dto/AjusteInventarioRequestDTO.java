package informviva.gest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitudes de ajuste de inventario
 * Utilizado en el API REST para registrar ajustes manuales
 *
 * @author Sistema de Gestión Empresarial
 * @version 1.0
 * @since FASE 2 - Módulo de Inventario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AjusteInventarioRequestDTO {

    /**
     * ID del producto a ajustar
     */
    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;

    /**
     * Cantidad a ajustar (puede ser positiva o negativa)
     * Positiva: incrementa stock
     * Negativa: decrementa stock
     */
    @NotNull(message = "La cantidad es obligatoria")
    private Integer cantidad;

    /**
     * Motivo del ajuste
     */
    @NotNull(message = "El motivo es obligatorio")
    @Size(min = 5, max = 500, message = "El motivo debe tener entre 5 y 500 caracteres")
    private String motivo;

    /**
     * Observaciones adicionales (opcional)
     */
    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;

    /**
     * Costo unitario del producto (opcional, para ajustes con valorización)
     */
    private Double costoUnitario;
}
