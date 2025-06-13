package informviva.gest.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para encapsular resultados de validación
 *
 * @author Roberto Rivas
 * @version 2.1
 */
@Data
@NoArgsConstructor
public class ValidacionResultadoDTO {

    private boolean valido = true;
    private String nombreArchivo;
    private long tamahoArchivo;
    private String tipoEntidad;
    private Integer numeroFila;

    private List<String> errores = new ArrayList<>();
    private List<String> advertencias = new ArrayList<>();
    private List<String> informacion = new ArrayList<>();

    // Estadísticas de validación
    private int totalFilas = 0;
    private int filasValidas = 0;
    private int filasConErrores = 0;
    private int filasConAdvertencias = 0;

    /**
     * Agrega un error y marca como inválido
     */
    public void agregarError(String error) {
        this.errores.add(error);
        this.valido = false;
    }

    /**
     * Agrega una advertencia (no marca como inválido)
     */
    public void agregarAdvertencia(String advertencia) {
        this.advertencias.add(advertencia);
    }

    /**
     * Agrega información adicional
     */
    public void agregarInformacion(String info) {
        this.informacion.add(info);
    }

    /**
     * Combina este resultado con otro
     */
    public void combinarCon(ValidacionResultadoDTO otro) {
        this.errores.addAll(otro.getErrores());
        this.advertencias.addAll(otro.getAdvertencias());
        this.informacion.addAll(otro.getInformacion());

        if (!otro.isValido()) {
            this.valido = false;
        }

        this.filasConErrores += otro.getFilasConErrores();
        this.filasConAdvertencias += otro.getFilasConAdvertencias();
        this.filasValidas += otro.getFilasValidas();
    }

    /**
     * Verifica si hay errores
     */
    public boolean tieneErrores() {
        return !errores.isEmpty();
    }

    /**
     * Verifica si hay advertencias
     */
    public boolean tieneAdvertencias() {
        return !advertencias.isEmpty();
    }

    /**
     * Obtiene el número total de problemas encontrados
     */
    public int getTotalProblemas() {
        return errores.size() + advertencias.size();
    }

    /**
     * Obtiene un resumen textual de la validación
     */
    public String getResumen() {
        StringBuilder resumen = new StringBuilder();

        if (nombreArchivo != null) {
            resumen.append("Archivo: ").append(nombreArchivo).append("\n");
        }

        if (numeroFila != null) {
            resumen.append("Fila: ").append(numeroFila).append("\n");
        }

        resumen.append("Estado: ").append(valido ? "Válido" : "Con errores").append("\n");

        if (!errores.isEmpty()) {
            resumen.append("Errores (").append(errores.size()).append("):\n");
            errores.forEach(error -> resumen.append("  - ").append(error).append("\n"));
        }

        if (!advertencias.isEmpty()) {
            resumen.append("Advertencias (").append(advertencias.size()).append("):\n");
            advertencias.forEach(adv -> resumen.append("  - ").append(adv).append("\n"));
        }

        return resumen.toString();
    }

    /**
     * Actualiza estadísticas después de validar una fila
     */
    public void actualizarEstadisticas() {
        totalFilas++;

        if (tieneErrores()) {
            filasConErrores++;
        } else if (tieneAdvertencias()) {
            filasConAdvertencias++;
        } else {
            filasValidas++;
        }
    }

    /**
     * Calcula el porcentaje de éxito en la validación
     */
    public double getPorcentajeExito() {
        if (totalFilas == 0) return 0.0;
        return ((double) filasValidas / totalFilas) * 100.0;
    }

    /**
     * Obtiene el nivel de severidad más alto encontrado
     */
    public SeveridadValidacion getSeveridadMaxima() {
        if (tieneErrores()) {
            return SeveridadValidacion.ERROR;
        } else if (tieneAdvertencias()) {
            return SeveridadValidacion.ADVERTENCIA;
        } else {
            return SeveridadValidacion.INFORMACION;
        }
    }

    /**
     * Enum para niveles de severidad
     */
    public enum SeveridadValidacion {
        INFORMACION("info", "text-info"),
        ADVERTENCIA("warning", "text-warning"),
        ERROR("error", "text-danger");

        private final String nivel;
        private final String claseCSS;

        SeveridadValidacion(String nivel, String claseCSS) {
            this.nivel = nivel;
            this.claseCSS = claseCSS;
        }

        public String getNivel() {
            return nivel;
        }

        public String getClaseCSS() {
            return claseCSS;
        }
    }
}