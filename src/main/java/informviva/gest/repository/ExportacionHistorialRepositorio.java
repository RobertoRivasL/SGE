package informviva.gest.repository;

import informviva.gest.model.ExportacionHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para el historial de exportaciones
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Repository
public interface ExportacionHistorialRepositorio extends JpaRepository<ExportacionHistorial, Long> {

    /**
     * Obtiene el historial ordenado por fecha descendente
     *
     * @return Lista ordenada de exportaciones
     */
    List<ExportacionHistorial> findAllByOrderByFechaSolicitudDesc();

    /**
     * Obtiene los últimos N registros de exportación
     *
     * @param limite Número de registros
     * @return Lista de exportaciones
     */
    @Query("SELECT e FROM ExportacionHistorial e ORDER BY e.fechaSolicitud DESC")
    List<ExportacionHistorial> findTopByOrderByFechaSolicitudDesc(@Param("limite") int limite);

    /**
     * Obtiene el historial por usuario solicitante
     *
     * @param usuarioSolicitante Nombre de usuario
     * @return Lista de exportaciones del usuario
     */
    List<ExportacionHistorial> findByUsuarioSolicitanteOrderByFechaSolicitudDesc(String usuarioSolicitante);

    /**
     * Obtiene el historial por tipo de exportación
     *
     * @param tipoExportacion Tipo de exportación
     * @return Lista de exportaciones del tipo
     */
    List<ExportacionHistorial> findByTipoExportacionOrderByFechaSolicitudDesc(String tipoExportacion);

    /**
     * Obtiene el historial por formato
     *
     * @param formato Formato de exportación (PDF, Excel, CSV)
     * @return Lista de exportaciones del formato
     */
    List<ExportacionHistorial> findByFormatoOrderByFechaSolicitudDesc(String formato);

    /**
     * Obtiene el historial por estado
     *
     * @param estado Estado de la exportación
     * @return Lista de exportaciones con el estado
     */
    List<ExportacionHistorial> findByEstadoOrderByFechaSolicitudDesc(String estado);

    /**
     * Obtiene el historial por rango de fechas de solicitud
     *
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de exportaciones en el rango
     */
    List<ExportacionHistorial> findByFechaSolicitudBetweenOrderByFechaSolicitudDesc(
            LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Obtiene el historial por rango de fechas de datos
     *
     * @param fechaInicio Fecha de inicio de datos
     * @param fechaFin Fecha de fin de datos
     * @return Lista de exportaciones con ese rango de datos
     */
    List<ExportacionHistorial> findByFechaInicioDatosBetweenOrderByFechaSolicitudDesc(
            LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Cuenta las exportaciones por usuario
     *
     * @param usuarioSolicitante Nombre de usuario
     * @return Número de exportaciones
     */
    Long countByUsuarioSolicitante(String usuarioSolicitante);

    /**
     * Cuenta las exportaciones completadas
     *
     * @return Número de exportaciones completadas
     */
    @Query("SELECT COUNT(e) FROM ExportacionHistorial e WHERE e.estado = 'Completado'")
    Long countExportacionesCompletadas();

    /**
     * Cuenta las exportaciones con error
     *
     * @return Número de exportaciones con error
     */
    @Query("SELECT COUNT(e) FROM ExportacionHistorial e WHERE e.estado = 'Error'")
    Long countExportacionesConError();

    /**
     * Cuenta las exportaciones en proceso
     *
     * @return Número de exportaciones en proceso
     */
    @Query("SELECT COUNT(e) FROM ExportacionHistorial e WHERE e.estado = 'Procesando'")
    Long countExportacionesProcesando();

    /**
     * Obtiene estadísticas por tipo de exportación
     *
     * @return Lista de objetos con tipo y cantidad
     */
    @Query("SELECT e.tipoExportacion as tipo, COUNT(e) as cantidad FROM ExportacionHistorial e " +
            "GROUP BY e.tipoExportacion ORDER BY cantidad DESC")
    List<Object[]> obtenerEstadisticasPorTipo();

    /**
     * Obtiene estadísticas por formato
     *
     * @return Lista de objetos con formato y cantidad
     */
    @Query("SELECT e.formato as formato, COUNT(e) as cantidad FROM ExportacionHistorial e " +
            "GROUP BY e.formato ORDER BY cantidad DESC")
    List<Object[]> obtenerEstadisticasPorFormato();

    /**
     * Obtiene el total de registros exportados exitosamente
     *
     * @return Suma total de registros
     */
    @Query("SELECT COALESCE(SUM(e.numeroRegistros), 0) FROM ExportacionHistorial e WHERE e.estado = 'Completado'")
    Long obtenerTotalRegistrosExportados();

    /**
     * Obtiene el tamaño total de archivos exportados
     *
     * @return Suma total de bytes
     */
    @Query("SELECT COALESCE(SUM(e.tamanoArchivo), 0) FROM ExportacionHistorial e WHERE e.estado = 'Completado'")
    Long obtenerTamanoTotalArchivos();

    /**
     * Elimina registros anteriores a una fecha
     *
     * @param fechaLimite Fecha límite
     * @return Número de registros eliminados
     */
    Long deleteByFechaSolicitudBefore(LocalDateTime fechaLimite);

    /**
     * Obtiene las exportaciones del día actual
     *
     * @param fechaInicio Inicio del día
     * @param fechaFin Fin del día
     * @return Lista de exportaciones del día
     */
    @Query("SELECT e FROM ExportacionHistorial e WHERE e.fechaSolicitud BETWEEN :fechaInicio AND :fechaFin " +
            "ORDER BY e.fechaSolicitud DESC")
    List<ExportacionHistorial> obtenerExportacionesDelDia(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    Page<ExportacionHistorial> findAllByOrderByFechaSolicitudDesc(Pageable pageable);
}