package informviva.gest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa el historial de exportaciones del sistema
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Entity
@Table(name = "exportacion_historial")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportacionHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El tipo de exportación es obligatorio")
    @Column(name = "tipo_exportacion")
    private String tipoExportacion; // Clientes, Productos, Ventas, Reportes

    @NotBlank(message = "El formato es obligatorio")
    @Column(name = "formato")
    private String formato; // PDF, Excel, CSV

    @NotBlank(message = "El usuario solicitante es obligatorio")
    @Column(name = "usuario_solicitante")
    private String usuarioSolicitante; // Usuario que realizó la exportación

    @NotNull(message = "La fecha de solicitud es obligatoria")
    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_inicio_datos")
    private LocalDate fechaInicioDatos; // Fecha inicio del rango de datos

    @Column(name = "fecha_fin_datos")
    private LocalDate fechaFinDatos; // Fecha fin del rango de datos

    @Column(name = "tamano_archivo")
    private Long tamanoArchivo; // Tamaño del archivo en bytes

    @NotNull(message = "El estado es obligatorio")
    @Column(name = "estado", length = 30)
    private String estado = "Procesando"; // Procesando, Completado, Error

    @Column(name = "mensaje_error", columnDefinition = "TEXT")
    private String mensajeError; // Mensaje de error si falló

    @Column(name = "numero_registros")
    private Integer numeroRegistros; // Cantidad de registros exportados

    @Column(name = "tiempo_procesamiento")
    private Long tiempoProcesamiento; // Tiempo en milisegundos

    @Column(name = "ruta_archivo", length = 500)
    private String rutaArchivo; // Ruta donde se guardó el archivo

    /**
     * Establece la fecha de solicitud antes de persistir
     */
    @PrePersist
    public void establecerFechaSolicitud() {
        if (fechaSolicitud == null) {
            this.fechaSolicitud = LocalDateTime.now();
        }
    }

    /**
     * Verifica si la exportación fue completada exitosamente
     *
     * @return true si fue completada
     */
    @Transient
    public boolean isCompletada() {
        return "Completado".equals(estado);
    }

    /**
     * Verifica si la exportación está en proceso
     *
     * @return true si está procesando
     */
    @Transient
    public boolean isProcesando() {
        return "Procesando".equals(estado);
    }

    /**
     * Verifica si la exportación tuvo error
     *
     * @return true si tuvo error
     */
    @Transient
    public boolean isError() {
        return "Error".equals(estado);
    }

    /**
     * Obtiene el tamaño del archivo formateado
     *
     * @return Tamaño formateado (ej: "1.5 MB")
     */
    @Transient
    public String getTamanoArchivoFormateado() {
        if (tamanoArchivo == null || tamanoArchivo == 0) {
            return "N/A";
        }

        final long kilobyte = 1024;
        final long megabyte = kilobyte * 1024;
        final long gigabyte = megabyte * 1024;

        if (tamanoArchivo >= gigabyte) {
            return String.format("%.1f GB", (double) tamanoArchivo / gigabyte);
        } else if (tamanoArchivo >= megabyte) {
            return String.format("%.1f MB", (double) tamanoArchivo / megabyte);
        } else if (tamanoArchivo >= kilobyte) {
            return String.format("%.1f KB", (double) tamanoArchivo / kilobyte);
        } else {
            return tamanoArchivo + " bytes";
        }
    }

    /**
     * Obtiene la duración formateada del procesamiento
     *
     * @return Duración formateada
     */
    @Transient
    public String getTiempoProcesamientoFormateado() {
        if (tiempoProcesamiento == null) {
            return "N/A";
        }

        if (tiempoProcesamiento < 1000) {
            return tiempoProcesamiento + " ms";
        } else {
            return String.format("%.2f s", tiempoProcesamiento / 1000.0);
        }
    }
}