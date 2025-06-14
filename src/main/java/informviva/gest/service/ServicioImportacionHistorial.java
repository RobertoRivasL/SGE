package informviva.gest.service;


import informviva.gest.dto.ImportacionResultadoDTO;
import informviva.gest.model.ImportacionHistorial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Servicio para gestionar el historial de importaciones
 *
 * @author Roberto Rivas
 * @version 2.0
 */
public interface ServicioImportacionHistorial {

    /**
     * Registra una importación en el historial
     */
    ImportacionHistorial registrarImportacion(ImportacionResultadoDTO resultado);

    /**
     * Obtiene el historial completo con paginación
     */
    Page<ImportacionHistorial> obtenerHistorial(Pageable pageable);

    /**
     * Obtiene el historial por tipo de importación
     */
    Page<ImportacionHistorial> obtenerHistorialPorTipo(String tipo, Pageable pageable);

    /**
     * Obtiene el historial de un usuario específico
     */
    List<ImportacionHistorial> obtenerHistorialPorUsuario(Long usuarioId);

    /**
     * Obtiene el historial en un rango de fechas
     */
    List<ImportacionHistorial> obtenerHistorialPorFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Obtiene las últimas importaciones realizadas
     */
    List<ImportacionHistorial> obtenerUltimasImportaciones();

    /**
     * Obtiene estadísticas de importaciones por tipo
     */
    Map<String, Object> obtenerEstadisticasPorTipo(LocalDateTime fechaInicio);

    /**
     * Obtiene estadísticas generales de importaciones
     */
    Map<String, Object> obtenerEstadisticasGenerales();

    /**
     * Busca importaciones por nombre de archivo
     */
    List<ImportacionHistorial> buscarPorNombreArchivo(String nombreArchivo);

    /**
     * Elimina registros de historial antiguos
     */
    int limpiarHistorialAntiguo(int diasAntiguos);

    /**
     * Exporta el historial de importaciones a Excel
     */
    byte[] exportarHistorialAExcel(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
