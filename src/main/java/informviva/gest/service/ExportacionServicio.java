package informviva.gest.service;

import informviva.gest.dto.ExportConfigDTO;
import informviva.gest.enums.FormatoExportacion;
import informviva.gest.model.ExportacionHistorial;
import informviva.gest.model.Venta;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public interface ExportacionServicio {

    // Constantes estáticas para acceso desde controladores
    interface FormatoExportacion {
        informviva.gest.enums.FormatoExportacion PDF = informviva.gest.enums.FormatoExportacion.PDF;
        informviva.gest.enums.FormatoExportacion EXCEL = informviva.gest.enums.FormatoExportacion.EXCEL;
        informviva.gest.enums.FormatoExportacion CSV = informviva.gest.enums.FormatoExportacion.CSV;
    }

    /**
     * Exportar usuarios con ExportConfigDTO (que contiene LocalDateTime)
     */
    byte[] exportarUsuarios(ExportConfigDTO config);

    /**
     * Exportar clientes con ExportConfigDTO (que contiene LocalDateTime)
     */
    byte[] exportarClientes(ExportConfigDTO config);

    /**
     * Exportar productos con ExportConfigDTO (que contiene LocalDateTime)
     */
    byte[] exportarProductos(ExportConfigDTO config);

    /**
     * Exportar ventas con ExportConfigDTO (que contiene LocalDateTime)
     */
    byte[] exportarVentas(ExportConfigDTO config);

    /**
     * Exportar reportes
     */
    byte[] exportarReportes(ExportConfigDTO config);

    /**
     * Obtener estadísticas de exportación
     */
    Map<String, Object> obtenerEstadisticasExportacion();

    /**
     * Obtener historial reciente
     */
    List<ExportacionHistorial> obtenerHistorialReciente(int limite);

    /**
     * Obtener estimaciones - MIGRADO A LocalDateTime
     */
    Map<String, Object> obtenerEstimaciones(String tipo, String formato,
                                            LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Método de compatibilidad para controladores que aún usan LocalDate
     */
    default Map<String, Object> obtenerEstimaciones(String tipo, String formato,
                                                    LocalDate fechaInicio, LocalDate fechaFin) {
        return obtenerEstimaciones(tipo, formato,
                fechaInicio.atStartOfDay(),
                fechaFin.atTime(23, 59, 59));
    }

    /**
     * Obtener historial paginado
     */
    Page<ExportacionHistorial> obtenerHistorialPaginado(int page, int size);

    // MANTENER métodos legacy temporalmente para compatibilidad:
    @Deprecated
    byte[] exportarUsuarios(String formato, Map<String, Object> filtros);

    @Deprecated
    byte[] exportarClientes(String formato, LocalDateTime fechaInicio,
                            LocalDateTime fechaFin, Map<String, Object> filtros);

    @Deprecated
    byte[] exportarProductos(String formato, Map<String, Object> filtros);

    @Deprecated
    byte[] exportarVentas(String formato, LocalDateTime fechaInicio,
                          LocalDateTime fechaFin, Map<String, Object> filtros);
}