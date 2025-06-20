package informviva.gest.service;

import informviva.gest.model.ExportacionHistorial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para la gestión del historial de exportaciones
 *
 * @author Roberto Rivas
 * @version 2.0
 */
public interface ExportacionHistorialServicio {

    /**
     * Registra una nueva exportación en el historial
     *
     * @param tipoExportacion Tipo de exportación (Clientes, Productos, Ventas, etc.)
     * @param formato Formato de exportación (PDF, Excel, CSV)
     * @param usuarioSolicitante Usuario que realizó la exportación
     * @param fechaInicioDatos Fecha inicio del rango de datos (opcional)
     * @param fechaFinDatos Fecha fin del rango de datos (opcional)
     * @return Historial de exportación creado
     */
    ExportacionHistorial registrarExportacion(String tipoExportacion, String formato, String usuarioSolicitante,
                                              LocalDate fechaInicioDatos, LocalDate fechaFinDatos);

    /**
     * Registra una exportación completada exitosamente
     *
     * @param id ID de la exportación a actualizar
     * @param registros Número de registros exportados
     * @param tamanoArchivo Tamaño del archivo en bytes
     * @param rutaArchivo Ruta donde se guardó el archivo
     * @return Historial actualizado
     */
    ExportacionHistorial marcarComoCompletada(Long id, Integer registros, Long tamanoArchivo, String rutaArchivo);

    /**
     * Registra una exportación fallida
     *
     * @param id ID de la exportación a actualizar
     * @param mensajeError Mensaje de error
     * @return Historial actualizado
     */
    ExportacionHistorial marcarComoError(Long id, String mensajeError);

    /**
     * Registra el tiempo de procesamiento de una exportación
     *
     * @param id ID de la exportación
     * @param tiempoProcesamiento Tiempo en milisegundos
     * @return Historial actualizado
     */
    ExportacionHistorial actualizarTiempoProcesamiento(Long id, Long tiempoProcesamiento);

    /**
     * Obtiene el historial de exportaciones ordenado por fecha descendente
     *
     * @param limite Número máximo de registros a retornar
     * @return Lista de exportaciones
     */
    List<ExportacionHistorial> obtenerHistorialReciente(int limite);

    /**
     * Obtiene el historial de exportaciones por usuario
     *
     * @param usuarioSolicitante Nombre de usuario
     * @param limite Número máximo de registros
     * @return Lista de exportaciones del usuario
     */
    List<ExportacionHistorial> obtenerHistorialPorUsuario(String usuarioSolicitante, int limite);

    /**
     * Obtiene el historial de exportaciones por rango de fechas de solicitud
     *
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de exportaciones en el rango
     */
    List<ExportacionHistorial> obtenerHistorialPorFechasSolicitud(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Obtiene el historial de exportaciones por rango de fechas de datos
     *
     * @param fechaInicio Fecha de inicio de datos
     * @param fechaFin Fecha de fin de datos
     * @return Lista de exportaciones con ese rango de datos
     */
    List<ExportacionHistorial> obtenerHistorialPorFechasDatos(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Obtiene estadísticas de exportaciones
     *
     * @return Mapa con estadísticas
     */
    java.util.Map<String, Object> obtenerEstadisticasExportacion();

    /**
     * Limpia el historial de exportaciones anterior a una fecha
     *
     * @param fechaLimite Fecha límite para mantener registros
     * @return Número de registros eliminados
     */
    Long limpiarHistorialAnteriorA(LocalDateTime fechaLimite);

    /**
     * Exporta programáticamente los reportes configurados
     */
    void ejecutarExportacionesProgramadas();

    /**
     * Busca una exportación por su ID
     *
     * @param id ID de la exportación
     * @return Exportación encontrada o null si no existe
     */
    ExportacionHistorial buscarPorId(Long id);

    Page<ExportacionHistorial> obtenerHistorialPaginado(Pageable pageable);
}