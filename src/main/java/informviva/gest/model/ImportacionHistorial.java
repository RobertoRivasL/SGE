package informviva.gest.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad para registrar el historial de importaciones realizadas
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Entity
@Table(name = "importacion_historial")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportacionHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_importacion", nullable = false)
    private String tipoImportacion; // PRODUCTOS, CLIENTES, USUARIOS

    @Column(name = "nombre_archivo", nullable = false)
    private String nombreArchivo;

    @Column(name = "fecha_importacion", nullable = false)
    private LocalDateTime fechaImportacion;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "total_registros")
    private Integer totalRegistros;

    @Column(name = "registros_exitosos")
    private Integer registrosExitosos;

    @Column(name = "registros_con_error")
    private Integer registrosConError;

    @Column(name = "tiempo_procesamiento_ms")
    private Long tiempoProcesamientoMs;

    @Column(name = "exitoso")
    private Boolean exitoso;

    @Column(name = "resumen", columnDefinition = "TEXT")
    private String resumen;

    @Column(name = "errores", columnDefinition = "TEXT")
    private String errores; // JSON string con los errores

    @Column(name = "advertencias", columnDefinition = "TEXT")
    private String advertencias; // JSON string con las advertencias

    /**
     * Calcula el porcentaje de éxito de la importación
     */
    @Transient
    public double getPorcentajeExito() {
        if (totalRegistros == null || totalRegistros == 0) {
            return 0.0;
        }
        return (registrosExitosos.doubleValue() / totalRegistros.doubleValue()) * 100.0;
    }

    /**
     * Verifica si la importación fue completamente exitosa
     */
    @Transient
    public boolean isCompletamenteExitoso() {
        return exitoso && (registrosConError == null || registrosConError == 0);
    }

    /**
     * Obtiene el tiempo de procesamiento en segundos
     */
    @Transient
    public double getTiempoProcesamientoSegundos() {
        if (tiempoProcesamientoMs == null) {
            return 0.0;
        }
        return tiempoProcesamientoMs / 1000.0;
    }
}
