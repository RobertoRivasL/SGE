package informviva.gest.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO que encapsula el resultado de una operación de importación
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportacionResultadoDTO {

    private String tipoEntidad;
    private String nombreArchivo;
    private LocalDateTime fechaImportacion;

    // Contadores
    private int registrosProcesados = 0;
    private int registrosExitosos = 0;
    private int registrosConError = 0;
    private int registrosOmitidos = 0;

    // Mensajes
    private List<String> errores = new ArrayList<>();
    private List<String> advertencias = new ArrayList<>();
    private List<String> mensajesInfo = new ArrayList<>();

    // Estadísticas adicionales
    private long tiempoProcesamientoMs;
    private double porcentajeExito;

    /**
     * Agrega un error al resultado
     */
    public void agregarError(String error) {
        this.errores.add(error);
    }

    /**
     * Agrega una advertencia al resultado
     */
    public void agregarAdvertencia(String advertencia) {
        this.advertencias.add(advertencia);
    }

    /**
     * Agrega un mensaje informativo al resultado
     */
    public void agregarMensajeInfo(String mensaje) {
        this.mensajesInfo.add(mensaje);
    }

    /**
     * Incrementa el contador de registros exitosos
     */
    public void incrementarExitosos() {
        this.registrosExitosos++;
    }

    /**
     * Incrementa el contador de registros con error
     */
    public void incrementarErrores() {
        this.registrosConError++;
    }

    /**
     * Incrementa el contador de registros omitidos
     */
    public void incrementarOmitidos() {
        this.registrosOmitidos++;
    }

    /**
     * Calcula los totales y porcentajes
     */
    public void calcularTotales() {
        this.registrosProcesados = this.registrosExitosos + this.registrosConError + this.registrosOmitidos;

        if (this.registrosProcesados > 0) {
            this.porcentajeExito = ((double) this.registrosExitosos / this.registrosProcesados) * 100.0;
        } else {
            this.porcentajeExito = 0.0;
        }
    }

    /**
     * Verifica si hay errores en el resultado
     */
    public boolean tieneErrores() {
        return !this.errores.isEmpty() || this.registrosConError > 0;
    }

    /**
     * Verifica si hay advertencias en el resultado
     */
    public boolean tieneAdvertencias() {
        return !this.advertencias.isEmpty() || this.registrosOmitidos > 0;
    }

    /**
     * Obtiene un resumen textual del resultado
     */
    public String getResumen() {
        StringBuilder resumen = new StringBuilder();
        resumen.append("Importación de ").append(tipoEntidad).append(" completada.\n");
        resumen.append("Archivo: ").append(nombreArchivo).append("\n");
        resumen.append("Fecha: ").append(fechaImportacion).append("\n\n");

        resumen.append("Resultados:\n");
        resumen.append("- Total procesados: ").append(registrosProcesados).append("\n");
        resumen.append("- Exitosos: ").append(registrosExitosos).append("\n");
        resumen.append("- Con errores: ").append(registrosConError).append("\n");
        resumen.append("- Omitidos: ").append(registrosOmitidos).append("\n");
        resumen.append("- Porcentaje de éxito: ").append(String.format("%.2f", porcentajeExito)).append("%\n");

        if (tiempoProcesamientoMs > 0) {
            resumen.append("- Tiempo de procesamiento: ").append(tiempoProcesamientoMs).append(" ms\n");
        }

        return resumen.toString();
    }

    /**
     * Obtiene el estado general de la importación
     */
    public EstadoImportacion getEstado() {
        if (registrosProcesados == 0) {
            return EstadoImportacion.SIN_PROCESAR;
        } else if (registrosConError == 0) {
            return registrosOmitidos > 0 ? EstadoImportacion.EXITOSO_CON_ADVERTENCIAS : EstadoImportacion.EXITOSO;
        } else if (registrosExitosos > 0) {
            return EstadoImportacion.PARCIAL;
        } else {
            return EstadoImportacion.FALLIDO;
        }
    }

    /**
     * Enum para los posibles estados de una importación
     */
    public enum EstadoImportacion {
        SIN_PROCESAR("Sin procesar"),
        EXITOSO("Exitoso"),
        EXITOSO_CON_ADVERTENCIAS("Exitoso con advertencias"),
        PARCIAL("Parcialmente exitoso"),
        FALLIDO("Fallido");

        private final String descripcion;

        EstadoImportacion(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }
}
