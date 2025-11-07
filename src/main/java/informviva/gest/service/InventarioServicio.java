package informviva.gest.service;

import informviva.gest.dto.EstadisticasInventarioDTO;
import informviva.gest.dto.MovimientoInventarioDTO;
import informviva.gest.model.MovimientoInventario.TipoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para gestión de inventario
 * Maneja toda la lógica de negocio relacionada con inventario y movimientos
 *
 * Aplicación de principios SOLID:
 * - S: Responsabilidad única para lógica de inventario
 * - O: Abierto para extensión (nuevos tipos de movimientos)
 * - L: Substitución de Liskov (implementaciones intercambiables)
 * - I: Interface segregada específica para inventario
 * - D: Depende de abstracciones, no de implementaciones concretas
 *
 * @author Roberto Rivas
 * @version 1.0
 * @since FASE 2 - Módulo de Inventario
 */
public interface InventarioServicio {

    // ==================== MOVIMIENTOS ====================

    /**
     * Registra un movimiento de inventario
     * Actualiza automáticamente el stock del producto
     *
     * @param movimientoDTO Datos del movimiento a registrar
     * @return Movimiento registrado con ID asignado
     * @throws IllegalArgumentException si los datos son inválidos
     * @throws IllegalStateException si el movimiento causaría stock negativo
     */
    MovimientoInventarioDTO registrarMovimiento(MovimientoInventarioDTO movimientoDTO);

    /**
     * Registra un movimiento de venta
     * Disminuye el stock del producto
     *
     * @param productoId ID del producto
     * @param cantidad Cantidad vendida
     * @param ventaId ID de la venta (referencia externa)
     * @param usuarioId ID del usuario que registra
     * @return Movimiento registrado
     */
    MovimientoInventarioDTO registrarVenta(Long productoId, Integer cantidad,
                                          Long ventaId, Long usuarioId);

    /**
     * Registra un movimiento de compra
     * Aumenta el stock del producto
     *
     * @param productoId ID del producto
     * @param cantidad Cantidad comprada
     * @param compraId ID de la compra (referencia externa)
     * @param costoUnitario Costo unitario del producto
     * @param usuarioId ID del usuario que registra
     * @return Movimiento registrado
     */
    MovimientoInventarioDTO registrarCompra(Long productoId, Integer cantidad,
                                           Long compraId, Double costoUnitario,
                                           Long usuarioId);

    /**
     * Registra un ajuste de inventario
     * Puede ser positivo (aumenta stock) o negativo (disminuye stock)
     *
     * @param productoId ID del producto
     * @param cantidad Cantidad a ajustar (positiva o negativa)
     * @param motivo Razón del ajuste
     * @param usuarioId ID del usuario que registra
     * @return Movimiento registrado
     */
    MovimientoInventarioDTO registrarAjuste(Long productoId, Integer cantidad,
                                           String motivo, Long usuarioId);

    /**
     * Registra una devolución de cliente
     * Aumenta el stock del producto
     *
     * @param productoId ID del producto
     * @param cantidad Cantidad devuelta
     * @param ventaId ID de la venta original
     * @param motivo Motivo de la devolución
     * @param usuarioId ID del usuario que registra
     * @return Movimiento registrado
     */
    MovimientoInventarioDTO registrarDevolucionEntrada(Long productoId, Integer cantidad,
                                                      Long ventaId, String motivo,
                                                      Long usuarioId);

    /**
     * Busca un movimiento por ID
     *
     * @param id ID del movimiento
     * @return Movimiento encontrado o null
     */
    MovimientoInventarioDTO buscarPorId(Long id);

    /**
     * Busca todos los movimientos con paginación
     *
     * @param pageable Información de paginación
     * @return Página de movimientos
     */
    Page<MovimientoInventarioDTO> buscarTodos(Pageable pageable);

    /**
     * Busca movimientos de un producto específico
     *
     * @param productoId ID del producto
     * @param pageable Información de paginación
     * @return Página de movimientos del producto
     */
    Page<MovimientoInventarioDTO> buscarPorProducto(Long productoId, Pageable pageable);

    /**
     * Busca movimientos por tipo
     *
     * @param tipo Tipo de movimiento
     * @param pageable Información de paginación
     * @return Página de movimientos del tipo
     */
    Page<MovimientoInventarioDTO> buscarPorTipo(TipoMovimiento tipo, Pageable pageable);

    /**
     * Busca movimientos en un rango de fechas
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @param pageable Información de paginación
     * @return Página de movimientos en el rango
     */
    Page<MovimientoInventarioDTO> buscarPorFechas(LocalDateTime fechaInicio,
                                                   LocalDateTime fechaFin,
                                                   Pageable pageable);

    /**
     * Busca movimientos con criterios múltiples
     *
     * @param productoId ID del producto (opcional)
     * @param tipo Tipo de movimiento (opcional)
     * @param fechaInicio Fecha inicial (opcional)
     * @param fechaFin Fecha final (opcional)
     * @param usuarioId ID del usuario (opcional)
     * @param pageable Información de paginación
     * @return Página de movimientos que cumplen los criterios
     */
    Page<MovimientoInventarioDTO> buscarConCriterios(Long productoId, TipoMovimiento tipo,
                                                     LocalDateTime fechaInicio,
                                                     LocalDateTime fechaFin,
                                                     Long usuarioId, Pageable pageable);

    /**
     * Obtiene los últimos movimientos de un producto
     *
     * @param productoId ID del producto
     * @param limite Número máximo de movimientos
     * @return Lista de últimos movimientos
     */
    List<MovimientoInventarioDTO> obtenerUltimosMovimientos(Long productoId, int limite);

    // ==================== ESTADÍSTICAS ====================

    /**
     * Obtiene estadísticas completas del inventario
     *
     * @return Estadísticas del inventario
     */
    EstadisticasInventarioDTO obtenerEstadisticas();

    /**
     * Obtiene estadísticas del inventario en un período específico
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Estadísticas del período
     */
    EstadisticasInventarioDTO obtenerEstadisticasPeriodo(LocalDateTime fechaInicio,
                                                          LocalDateTime fechaFin);

    /**
     * Calcula el total de entradas de un producto en un período
     *
     * @param productoId ID del producto
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Total de unidades entradas
     */
    Long calcularTotalEntradas(Long productoId, LocalDateTime fechaInicio,
                               LocalDateTime fechaFin);

    /**
     * Calcula el total de salidas de un producto en un período
     *
     * @param productoId ID del producto
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Total de unidades salidas
     */
    Long calcularTotalSalidas(Long productoId, LocalDateTime fechaInicio,
                              LocalDateTime fechaFin);

    // ==================== ALERTAS ====================

    /**
     * Obtiene lista de productos con stock bajo
     *
     * @return Lista de productos debajo del stock mínimo
     */
    List<EstadisticasInventarioDTO.ProductoStockDTO> obtenerProductosStockBajo();

    /**
     * Obtiene lista de productos sin stock
     *
     * @return Lista de productos con stock en cero
     */
    List<EstadisticasInventarioDTO.ProductoStockDTO> obtenerProductosSinStock();

    /**
     * Verifica si un producto tiene stock suficiente
     *
     * @param productoId ID del producto
     * @param cantidad Cantidad requerida
     * @return true si hay stock suficiente
     */
    boolean verificarStockDisponible(Long productoId, Integer cantidad);

    /**
     * Obtiene el stock actual de un producto
     *
     * @param productoId ID del producto
     * @return Stock actual
     */
    Integer obtenerStockActual(Long productoId);

    // ==================== VALIDACIONES ====================

    /**
     * Valida si un movimiento es posible
     * Verifica stock disponible para salidas
     *
     * @param productoId ID del producto
     * @param tipo Tipo de movimiento
     * @param cantidad Cantidad del movimiento
     * @return true si el movimiento es válido
     */
    boolean validarMovimiento(Long productoId, TipoMovimiento tipo, Integer cantidad);

    /**
     * Obtiene el mensaje de error de validación
     *
     * @param productoId ID del producto
     * @param tipo Tipo de movimiento
     * @param cantidad Cantidad del movimiento
     * @return Mensaje de error o null si es válido
     */
    String obtenerMensajeValidacion(Long productoId, TipoMovimiento tipo, Integer cantidad);

    // ==================== UTILIDADES ====================

    /**
     * Cuenta total de movimientos registrados
     *
     * @return Número total de movimientos
     */
    long contarMovimientos();

    /**
     * Cuenta movimientos de un producto
     *
     * @param productoId ID del producto
     * @return Número de movimientos del producto
     */
    long contarMovimientosProducto(Long productoId);

    /**
     * Cuenta movimientos en un período
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Número de movimientos en el período
     */
    long contarMovimientosPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
