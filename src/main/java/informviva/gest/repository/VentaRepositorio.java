package informviva.gest.repository;

import informviva.gest.model.Cliente;
import informviva.gest.model.Usuario;
import informviva.gest.model.Venta;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Repositorio para acceder a las entidades Venta en la base de datos.
 * Proporciona métodos para consultas específicas relacionadas con ventas.
 *
 * @author Roberto Rivas
 * @version 2.1
 */
@Repository
public interface VentaRepositorio extends JpaRepository<Venta, Long> {

    /**
     * Verifica si existen ventas para un cliente específico
     *
     * @param clienteId ID del cliente
     * @return true si existen ventas, false en caso contrario
     */
    boolean existsByClienteId(Long clienteId);

    /**
     * Verifica si existen ventas que contienen un producto específico
     *
     * @param productoId ID del producto
     * @return true si existen ventas con el producto, false en caso contrario
     */
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Venta v WHERE EXISTS (SELECT 1 FROM VentaDetalle vd WHERE vd.venta = v AND vd.producto.id = :productoId)")
    boolean existsByDetallesProductoId(@Param("productoId") Long productoId);

    /**
     * Busca ventas de un cliente específico
     *
     * @param cliente Entidad Cliente
     * @return Lista de ventas del cliente
     */
    List<Venta> findByCliente(Cliente cliente);

    /**
     * Busca ventas de un vendedor específico
     *
     * @param vendedor Entidad Usuario que representa al vendedor
     * @return Lista de ventas del vendedor
     */
    List<Venta> findByVendedor(Usuario vendedor);

    /**
     * Busca ventas en un rango de fechas
     * IMPORTANTE: Ahora recibe LocalDateTime para compatibilidad con el modelo
     *
     * @param start Fecha y hora de inicio
     * @param end   Fecha y hora de fin
     * @return Lista de ventas en el rango de fechas
     */
    @Query("SELECT v FROM Venta v WHERE v.fecha BETWEEN :start AND :end")
    List<Venta> findByFechaBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Cuenta el número de ventas en un rango de fechas excluyendo un estado específico
     *
     * @param inicio Fecha y hora de inicio
     * @param fin    Fecha y hora de fin
     * @param estado Estado a excluir
     * @return Número de ventas
     */
    @Query("SELECT COUNT(v) FROM Venta v WHERE v.fecha BETWEEN :inicio AND :fin AND v.estado != :estado")
    Long countByFechaBetweenAndEstadoNot(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, @Param("estado") String estado);

    /**
     * Calcula el total de ingresos en un rango de fechas
     *
     * @param start Fecha y hora de inicio
     * @param end   Fecha y hora de fin
     * @return Total de ingresos
     */
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.fecha BETWEEN :start AND :end AND v.estado != 'ANULADA'")
    Double calcularTotalIngresos(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Cuenta el número de transacciones en un rango de fechas
     *
     * @param start Fecha y hora de inicio
     * @param end   Fecha y hora de fin
     * @return Número de transacciones
     */
    @Query("SELECT COUNT(v) FROM Venta v WHERE v.fecha BETWEEN :start AND :end AND v.estado != 'ANULADA'")
    Long contarTransacciones(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Cuenta la cantidad de artículos vendidos en un rango de fechas
     *
     * @param start Fecha y hora de inicio
     * @param end   Fecha y hora de fin
     * @return Cantidad de artículos vendidos
     */
    @Query("SELECT COALESCE(SUM(vd.cantidad), 0) FROM VentaDetalle vd JOIN vd.venta v WHERE v.fecha BETWEEN :start AND :end AND v.estado != 'ANULADA'")
    Long contarArticulosVendidos(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Método helper para contar artículos vendidos entre fechas (compatibilidad)
     *
     * @param inicio Fecha y hora de inicio
     * @param fin    Fecha y hora de fin
     * @return Cantidad de artículos vendidos
     */
    default Long countArticulosVendidosBetweenFechas(LocalDateTime inicio, LocalDateTime fin) {
        return contarArticulosVendidos(inicio, fin);
    }

    @Query("SELECT COUNT(v) FROM Venta v WHERE v.cliente.id = :clienteId")
    Long countByClienteId(Long clienteId);

    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.cliente.id = :clienteId")
    Double calcularTotalPorCliente(Long clienteId);

    @Query("SELECT SUM(v.cantidad) FROM Venta v WHERE v.producto.id = :productoId")
    Long contarUnidadesVendidasPorProducto(Long productoId);

    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.producto.id = :productoId")
    Double calcularIngresosPorProducto(Long productoId);

    @Query("SELECT v FROM Venta v WHERE v.producto.id = :productoId ORDER BY v.fecha DESC")
    List<Venta> buscarVentasRecientesPorProducto(Long productoId, Pageable limite);

    @Query("SELECT v FROM Venta v WHERE v.cliente.id = :clienteId ORDER BY v.fecha DESC")
    List<Venta> findTopByClienteIdOrderByFechaDesc(@Param("clienteId") Long clienteId, Pageable pageable);

    @Query("SELECT v FROM Venta v WHERE DATE(v.fecha) BETWEEN :inicio AND :fin")
    List<Venta> findByFechaBetween(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);


    @Query("SELECT COUNT(v) FROM Venta v WHERE v.fecha BETWEEN :inicio AND :fin AND v.estado != :estado")
    Long countByFechaBetweenAndEstadoNot(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin, @Param("estado") String estado);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.fecha BETWEEN :start AND :end AND v.estado != 'ANULADA'")
    Double calcularTotalIngresos(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(v) FROM Venta v WHERE v.fecha BETWEEN :start AND :end AND v.estado != 'ANULADA'")
    Long contarTransacciones(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(vd.cantidad), 0) FROM VentaDetalle vd JOIN vd.venta v WHERE v.fecha BETWEEN :start AND :end AND v.estado != 'ANULADA'")
    Long contarArticulosVendidos(@Param("start") LocalDate start, @Param("end") LocalDate end);

    // NUEVOS MÉTODOS CON LocalDateTime

    /**
     * Busca ventas por rango de fechas con hora específica
     */
    @Query("SELECT v FROM Venta v WHERE v.fecha BETWEEN :inicio AND :fin ORDER BY v.fecha DESC")
    List<Venta> findByFechaBetweenDateTime(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Cuenta transacciones en un rango de fechas con hora específica
     */
    @Query("SELECT COUNT(v) FROM Venta v WHERE v.fecha BETWEEN :inicio AND :fin AND v.estado != :estado")
    Long countByFechaBetweenDateTimeAndEstadoNot(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, @Param("estado") String estado);

    /**
     * Calcula total de ingresos con rango de fecha y hora específica
     */
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.fecha BETWEEN :inicio AND :fin AND v.estado != 'ANULADA'")
    Double calcularTotalIngresosDateTime(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Cuenta artículos vendidos en rango de fecha y hora específica
     */
    @Query("SELECT COALESCE(SUM(vd.cantidad), 0) FROM VentaDetalle vd JOIN vd.venta v WHERE v.fecha BETWEEN :inicio AND :fin AND v.estado != 'ANULADA'")
    Long contarArticulosVendidosDateTime(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Obtiene ventas de un cliente en un período específico con hora
     */
    @Query("SELECT v FROM Venta v WHERE v.cliente.id = :clienteId AND v.fecha BETWEEN :inicio AND :fin ORDER BY v.fecha DESC")
    List<Venta> findByClienteIdAndFechaBetween(@Param("clienteId") Long clienteId, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Obtiene ventas de un vendedor en un período específico con hora
     */
    @Query("SELECT v FROM Venta v WHERE v.vendedor.id = :vendedorId AND v.fecha BETWEEN :inicio AND :fin ORDER BY v.fecha DESC")
    List<Venta> findByVendedorIdAndFechaBetween(@Param("vendedorId") Long vendedorId, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Obtiene las ventas más recientes hasta una fecha específica
     */
    @Query("SELECT v FROM Venta v WHERE v.fecha <= :hastaFecha ORDER BY v.fecha DESC")
    List<Venta> findVentasRecientesHastaFecha(@Param("hastaFecha") LocalDateTime hastaFecha, Pageable pageable);

    /**
     * Obtiene ventas agrupadas por hora del día
     */
    @Query(value = "SELECT HOUR(v.fecha) as hora, SUM(v.total) as total FROM ventas v " +
            "WHERE DATE(v.fecha) = DATE(:fecha) AND v.estado != 'ANULADA' " +
            "GROUP BY HOUR(v.fecha) ORDER BY hora", nativeQuery = true)
    List<Object[]> findVentasPorHoraDelDia(@Param("fecha") LocalDateTime fecha);

    /**
     * Obtiene resumen de ventas agrupado por hora en un período
     */
    @Query(value = "SELECT DATE_FORMAT(v.fecha, '%Y-%m-%d %H:00:00') as periodo, " +
            "COUNT(v.id) as transacciones, SUM(v.total) as total, AVG(v.total) as promedio " +
            "FROM ventas v WHERE v.fecha BETWEEN :inicio AND :fin AND v.estado != 'ANULADA' " +
            "GROUP BY DATE_FORMAT(v.fecha, '%Y-%m-%d %H:00:00') " +
            "ORDER BY periodo", nativeQuery = true)
    List<Object[]> findResumenVentasPorHora(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Obtiene ventas del día actual agrupadas por franja horaria
     */
    @Query(value = "SELECT " +
            "CASE " +
            "    WHEN HOUR(v.fecha) BETWEEN 6 AND 11 THEN 'Mañana' " +
            "    WHEN HOUR(v.fecha) BETWEEN 12 AND 17 THEN 'Tarde' " +
            "    WHEN HOUR(v.fecha) BETWEEN 18 AND 23 THEN 'Noche' " +
            "    ELSE 'Madrugada' " +
            "END as franja, " +
            "SUM(v.total) as total " +
            "FROM ventas v " +
            "WHERE DATE(v.fecha) = DATE(:fecha) AND v.estado != 'ANULADA' " +
            "GROUP BY franja", nativeQuery = true)
    List<Object[]> findVentasPorFranjaHoraria(@Param("fecha") LocalDateTime fecha);

    // Métodos existentes sin cambios
    default Long countArticulosVendidosBetweenFechas(LocalDate inicio, LocalDate fin) {
        return contarArticulosVendidos(inicio, fin);
    }


}
