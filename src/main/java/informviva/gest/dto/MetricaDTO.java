package informviva.gest.dto;

/**
 * @author Roberto Rivas
 * @version 2.0
 */


/**
 * DTO para representar una métrica con su valor y porcentaje de cambio
 */
public record MetricaDTO(Double valor, Double porcentajeCambio) {

    /**
     * Constructor para valores Long
     */
    public MetricaDTO(Long valor, Double porcentajeCambio) {
        this(valor != null ? valor.doubleValue() : 0.0, porcentajeCambio);
    }

    /**
     * Método de fábrica para crear desde diferentes tipos numéricos
     */
    public static MetricaDTO of(Number valor, Double porcentajeCambio) {
        return new MetricaDTO(
                valor != null ? valor.doubleValue() : 0.0,
                porcentajeCambio
        );
    }
}