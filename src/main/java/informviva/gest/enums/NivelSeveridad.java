package informviva.gest.enums;

/**
 * Enum para niveles de severidad en validaciones del sistema
 * Define la criticidad de errores y advertencias encontrados
 *
 * @author Roberto Rivas
 * @version 2.0
 */
public enum NivelSeveridad {



    /**
     * Información general - No impacta funcionamiento
     */
    INFORMACION("info", "text-info", "fas fa-info-circle", 1),

    /**
     * Advertencia - Posible problema pero no crítico
     */
    ADVERTENCIA("warning", "text-warning", "fas fa-exclamation-triangle", 2),

    /**
     * Error crítico - Impide funcionamiento correcto
     */
    CRITICO("error", "text-danger", "fas fa-times-circle", 3);

    private final String codigo;
    private final String claseCSS;
    private final String iconoCSS;
    private final int prioridad;

    /**
     * Constructor del enum
     *
     * @param codigo Código identificador del nivel
     * @param claseCSS Clase CSS para mostrar en UI (Bootstrap)
     * @param iconoCSS Icono CSS (Font Awesome)
     * @param prioridad Prioridad numérica (mayor = más crítico)
     */
    NivelSeveridad(String codigo, String claseCSS, String iconoCSS, int prioridad) {
        this.codigo = codigo;
        this.claseCSS = claseCSS;
        this.iconoCSS = iconoCSS;
        this.prioridad = prioridad;
    }

    // Getters
    public String getCodigo() {
        return codigo;
    }

    public String getClaseCSS() {
        return claseCSS;
    }

    public String getIconoCSS() {
        return iconoCSS;
    }

    public int getPrioridad() {
        return prioridad;
    }

    // Métodos de utilidad

    /**
     * Verifica si este nivel es más crítico que otro
     *
     * @param otro Nivel a comparar
     * @return true si este nivel es más crítico
     */
    public boolean esMasCriticoQue(NivelSeveridad otro) {
        return this.prioridad > otro.prioridad;
    }

    /**
     * Verifica si este nivel es crítico
     *
     * @return true si es nivel CRITICO
     */
    public boolean esCritico() {
        return this == CRITICO;
    }

    /**
     * Verifica si este nivel es advertencia o superior
     *
     * @return true si es ADVERTENCIA o CRITICO
     */
    public boolean esAdvertenciaOMayor() {
        return this.prioridad >= ADVERTENCIA.prioridad;
    }

    public boolean esAdvertencia() {
        return this == ADVERTENCIA;
    }

    /**
     * Obtiene el nivel más crítico entre dos niveles
     *
     * @param nivel1 Primer nivel
     * @param nivel2 Segundo nivel
     * @return El nivel más crítico
     */
    public static NivelSeveridad getMasCritico(NivelSeveridad nivel1, NivelSeveridad nivel2) {
        return nivel1.esMasCriticoQue(nivel2) ? nivel1 : nivel2;
    }

    /**
     * Convierte un string a NivelSeveridad
     *
     * @param codigo Código del nivel
     * @return NivelSeveridad correspondiente
     * @throws IllegalArgumentException si el código no es válido
     */
    public static NivelSeveridad fromCodigo(String codigo) {
        for (NivelSeveridad nivel : values()) {
            if (nivel.codigo.equalsIgnoreCase(codigo)) {
                return nivel;
            }
        }
        throw new IllegalArgumentException("Código de severidad no válido: " + codigo);
    }

    @Override
    public String toString() {
        return codigo.toUpperCase();
    }
}