package informviva.gest.enums;

/**
 * Enum para representar las tendencias de ventas
 *
 * UBICACIÓN CORRECTA: src/main/java/informviva/gest/enums/TendenciaVenta.java
 * PACKAGE CORRECTO: informviva.gest.enums
 *
 * @author Roberto Rivas
 * @version 2.0
 */
public enum TendenciaVenta {

    CRECIENTE("Creciente", "↗", "#28a745", "Las ventas están aumentando"),
    ESTABLE("Estable", "→", "#ffc107", "Las ventas se mantienen constantes"),
    DECRECIENTE("Decreciente", "↘", "#dc3545", "Las ventas están disminuyendo"),
    VOLATIL("Volátil", "~", "#6f42c1", "Las ventas tienen fluctuaciones significativas"),
    SIN_DATOS("Sin datos", "?", "#6c757d", "No hay suficientes datos para determinar la tendencia");

    private final String descripcion;
    private final String icono;
    private final String color;
    private final String explicacion;

    TendenciaVenta(String descripcion, String icono, String color, String explicacion) {
        this.descripcion = descripcion;
        this.icono = icono;
        this.color = color;
        this.explicacion = explicacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getIcono() {
        return icono;
    }

    public String getColor() {
        return color;
    }

    public String getExplicacion() {
        return explicacion;
    }

    /**
     * Obtiene la tendencia basada en un porcentaje de cambio
     *
     * @param porcentajeCambio Porcentaje de cambio en las ventas
     * @return TendenciaVenta correspondiente
     */
    public static TendenciaVenta obtenerTendencia(Double porcentajeCambio) {
        if (porcentajeCambio == null) {
            return SIN_DATOS;
        }

        if (porcentajeCambio > 10) {
            return CRECIENTE;
        } else if (porcentajeCambio < -10) {
            return DECRECIENTE;
        } else if (Math.abs(porcentajeCambio) <= 5) {
            return ESTABLE;
        } else {
            return VOLATIL;
        }
    }

    /**
     * Obtiene la tendencia basada en dos valores
     *
     * @param valorActual Valor actual
     * @param valorAnterior Valor anterior
     * @return TendenciaVenta correspondiente
     */
    public static TendenciaVenta obtenerTendencia(Double valorActual, Double valorAnterior) {
        if (valorActual == null || valorAnterior == null || valorAnterior == 0) {
            return SIN_DATOS;
        }

        double porcentajeCambio = ((valorActual - valorAnterior) / valorAnterior) * 100;
        return obtenerTendencia(porcentajeCambio);
    }

    /**
     * Obtiene la clase CSS Bootstrap correspondiente al color
     *
     * @return Clase CSS
     */
    public String getClaseCSS() {
        return switch (this) {
            case CRECIENTE -> "text-success";
            case ESTABLE -> "text-warning";
            case DECRECIENTE -> "text-danger";
            case VOLATIL -> "text-info";
            case SIN_DATOS -> "text-muted";
        };
    }

    /**
     * Obtiene el icono de Bootstrap correspondiente
     *
     * @return Clase de icono Bootstrap
     */
    public String getIconoBootstrap() {
        return switch (this) {
            case CRECIENTE -> "bi bi-arrow-up-right";
            case ESTABLE -> "bi bi-arrow-right";
            case DECRECIENTE -> "bi bi-arrow-down-right";
            case VOLATIL -> "bi bi-graph-up";
            case SIN_DATOS -> "bi bi-question-circle";
        };
    }

    /**
     * Alias para obtener la tendencia basada en un porcentaje de cambio
     *
     * @param porcentajeCambio Porcentaje de cambio en las ventas
     * @return TendenciaVenta correspondiente
     */
    public static TendenciaVenta determinarTendencia(Double porcentajeCambio) {
        return obtenerTendencia(porcentajeCambio);
    }
}