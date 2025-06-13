package informviva.gest.service;


import informviva.gest.dto.ClienteReporteDTO;
import informviva.gest.model.Cliente;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Servicio para la generación de reportes de clientes
 *
 * @author Roberto Rivas
 * @version 2.0
 */
public interface ReporteClienteServicio {

    /**
     * Genera un reporte detallado de clientes con sus estadísticas de compras
     *
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin    Fecha de fin del período
     * @return Lista de DTOs con información de clientes y sus compras
     */
    List<ClienteReporteDTO> generarReporteClientes(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Obtiene las estadísticas generales de clientes
     *
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin    Fecha de fin del período
     * @return Mapa con estadísticas clave
     */
    Map<String, Object> obtenerEstadisticasGenerales(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Obtiene los clientes más valiosos (top compradores)
     *
     * @param limite      Número máximo de clientes a retornar
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin    Fecha de fin del período
     * @return Lista de clientes top
     */
    List<ClienteReporteDTO> obtenerTopClientes(int limite, LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Analiza la distribución de clientes por antigüedad
     *
     * @return Mapa con rangos de antigüedad y cantidades
     */
    Map<String, Long> analizarDistribucionAntiguedad();

    /**
     * Obtiene clientes inactivos (sin compras en período determinado)
     *
     * @param diasInactividad Número de días sin compras para considerar inactivo
     * @return Lista de clientes inactivos
     */
    List<Cliente> obtenerClientesInactivos(int diasInactividad);

    /**
     * Calcula métricas de retención de clientes
     *
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin    Fecha de fin del período
     * @return Mapa con métricas de retención
     */
    Map<String, Object> calcularMetricasRetencion(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Exporta el reporte de clientes a diferentes formatos
     *
     * @param clientes Lista de clientes a exportar
     * @param formato  Formato de exportación (PDF, EXCEL, CSV)
     * @return Datos del archivo en bytes
     */
    byte[] exportarReporteClientes(List<ClienteReporteDTO> clientes, String formato);
}
