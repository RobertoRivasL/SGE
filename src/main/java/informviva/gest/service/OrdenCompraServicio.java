package informviva.gest.service;

import informviva.gest.dto.DetalleOrdenCompraDTO;
import informviva.gest.dto.EstadisticasComprasDTO;
import informviva.gest.dto.OrdenCompraDTO;
import informviva.gest.model.OrdenCompra.EstadoOrden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Servicio para gestión de órdenes de compra
 * Maneja toda la lógica de negocio relacionada con órdenes de compra
 *
 * Aplicación de principios SOLID:
 * - S: Responsabilidad única para lógica de órdenes de compra
 * - O: Abierto para extensión (nuevos estados, operaciones)
 * - L: Substitución de Liskov (implementaciones intercambiables)
 * - I: Interface segregada específica para órdenes
 * - D: Depende de abstracciones, no de implementaciones concretas
 *
 * @author Roberto Rivas
 * @version 1.0
 * @since FASE 3 - Módulo de Compras
 */
public interface OrdenCompraServicio {

    // ==================== OPERACIONES CRUD ====================

    /**
     * Crea una nueva orden de compra en estado BORRADOR
     *
     * @param ordenCompraDTO Datos de la orden a crear
     * @return Orden creada con ID asignado
     * @throws IllegalArgumentException si los datos son inválidos
     * @throws informviva.gest.exception.RecursoNoEncontradoException si el proveedor no existe
     */
    OrdenCompraDTO crear(OrdenCompraDTO ordenCompraDTO);

    /**
     * Actualiza una orden de compra existente
     * Solo se pueden modificar órdenes en estado BORRADOR o PENDIENTE
     *
     * @param id ID de la orden a actualizar
     * @param ordenCompraDTO Datos actualizados
     * @return Orden actualizada
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     * @throws IllegalStateException si la orden no es modificable
     */
    OrdenCompraDTO actualizar(Long id, OrdenCompraDTO ordenCompraDTO);

    /**
     * Busca una orden por ID
     *
     * @param id ID de la orden
     * @return Orden encontrada
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     */
    OrdenCompraDTO buscarPorId(Long id);

    /**
     * Busca todas las órdenes con paginación
     *
     * @param pageable Información de paginación
     * @return Página de órdenes
     */
    Page<OrdenCompraDTO> buscarTodos(Pageable pageable);

    /**
     * Busca todas las órdenes sin paginación
     *
     * @return Lista de todas las órdenes
     */
    List<OrdenCompraDTO> buscarTodos();

    // ==================== BÚSQUEDAS ESPECÍFICAS ====================

    /**
     * Busca órdenes de un proveedor
     *
     * @param proveedorId ID del proveedor
     * @param pageable Información de paginación
     * @return Página de órdenes del proveedor
     */
    Page<OrdenCompraDTO> buscarPorProveedor(Long proveedorId, Pageable pageable);

    /**
     * Busca órdenes por estado
     *
     * @param estado Estado de la orden
     * @param pageable Información de paginación
     * @return Página de órdenes en ese estado
     */
    Page<OrdenCompraDTO> buscarPorEstado(EstadoOrden estado, Pageable pageable);

    /**
     * Busca órdenes en un rango de fechas
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @param pageable Información de paginación
     * @return Página de órdenes en el rango
     */
    Page<OrdenCompraDTO> buscarPorFechas(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable);

    /**
     * Busca una orden por su número único
     *
     * @param numeroOrden Número de la orden
     * @return Orden encontrada
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     */
    OrdenCompraDTO buscarPorNumeroOrden(String numeroOrden);

    /**
     * Busca órdenes con criterios múltiples
     *
     * @param proveedorId ID del proveedor (opcional)
     * @param estado Estado (opcional)
     * @param fechaInicio Fecha inicial (opcional)
     * @param fechaFin Fecha final (opcional)
     * @param usuarioId ID del usuario comprador (opcional)
     * @param pageable Información de paginación
     * @return Página de órdenes que cumplen los criterios
     */
    Page<OrdenCompraDTO> buscarConCriterios(Long proveedorId, EstadoOrden estado,
                                            LocalDate fechaInicio, LocalDate fechaFin,
                                            Long usuarioId, Pageable pageable);

    /**
     * Busca órdenes próximas a entregar (en los próximos 7 días)
     *
     * @return Lista de órdenes próximas
     */
    List<OrdenCompraDTO> buscarOrdenesProximas();

    /**
     * Busca órdenes atrasadas (fecha estimada pasada y no recibidas)
     *
     * @return Lista de órdenes atrasadas
     */
    List<OrdenCompraDTO> buscarOrdenesAtrasadas();

    // ==================== GESTIÓN DE DETALLES ====================

    /**
     * Agrega un detalle a una orden existente
     * Solo se puede agregar a órdenes en estado BORRADOR o PENDIENTE
     *
     * @param ordenId ID de la orden
     * @param detalle Detalle a agregar
     * @return Orden actualizada
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe la orden
     * @throws IllegalStateException si la orden no es modificable
     */
    OrdenCompraDTO agregarDetalle(Long ordenId, DetalleOrdenCompraDTO detalle);

    /**
     * Actualiza un detalle de una orden
     * Solo se puede modificar en órdenes BORRADOR o PENDIENTE
     *
     * @param ordenId ID de la orden
     * @param detalleId ID del detalle
     * @param detalle Datos actualizados del detalle
     * @return Orden actualizada
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     * @throws IllegalStateException si la orden no es modificable
     */
    OrdenCompraDTO actualizarDetalle(Long ordenId, Long detalleId, DetalleOrdenCompraDTO detalle);

    /**
     * Elimina un detalle de una orden
     * Solo se puede eliminar de órdenes BORRADOR o PENDIENTE
     *
     * @param ordenId ID de la orden
     * @param detalleId ID del detalle a eliminar
     * @return Orden actualizada
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     * @throws IllegalStateException si la orden no es modificable
     */
    OrdenCompraDTO eliminarDetalle(Long ordenId, Long detalleId);

    // ==================== CAMBIOS DE ESTADO ====================

    /**
     * Aprueba una orden (cambia de BORRADOR a PENDIENTE)
     * Valida que la orden tenga todos los datos necesarios
     *
     * @param id ID de la orden
     * @param usuarioAprobadorId ID del usuario que aprueba
     * @return Orden aprobada
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     * @throws IllegalStateException si el estado actual no permite aprobación
     */
    OrdenCompraDTO aprobar(Long id, Long usuarioAprobadorId);

    /**
     * Envía una orden al proveedor (cambia a ENVIADA)
     *
     * @param id ID de la orden
     * @return Orden enviada
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     * @throws IllegalStateException si el estado actual no permite envío
     */
    OrdenCompraDTO enviar(Long id);

    /**
     * Marca una orden como confirmada por el proveedor
     *
     * @param id ID de la orden
     * @return Orden confirmada
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     * @throws IllegalStateException si el estado actual no permite confirmación
     */
    OrdenCompraDTO confirmar(Long id);

    /**
     * Registra la recepción completa de una orden
     * Actualiza el inventario automáticamente para todos los productos
     *
     * @param id ID de la orden
     * @param usuarioReceptorId ID del usuario que recibe
     * @return Orden recibida
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     * @throws IllegalStateException si el estado actual no permite recepción
     */
    OrdenCompraDTO recibirCompleta(Long id, Long usuarioReceptorId);

    /**
     * Registra la recepción parcial de una orden
     * Actualiza el inventario solo para las cantidades recibidas
     *
     * @param id ID de la orden
     * @param cantidadesPorDetalle Map con ID del detalle y cantidad recibida
     * @param usuarioReceptorId ID del usuario que recibe
     * @return Orden actualizada
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     * @throws IllegalStateException si el estado actual no permite recepción
     * @throws IllegalArgumentException si las cantidades son inválidas
     */
    OrdenCompraDTO recibirParcial(Long id, Map<Long, Integer> cantidadesPorDetalle,
                                   Long usuarioReceptorId);

    /**
     * Cancela una orden
     *
     * @param id ID de la orden
     * @param motivo Motivo de la cancelación
     * @return Orden cancelada
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     * @throws IllegalStateException si el estado actual no permite cancelación
     */
    OrdenCompraDTO cancelar(Long id, String motivo);

    /**
     * Completa una orden (cierra el ciclo)
     * Solo se puede completar si está totalmente recibida
     *
     * @param id ID de la orden
     * @return Orden completada
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     * @throws IllegalStateException si no está completamente recibida
     */
    OrdenCompraDTO completar(Long id);

    // ==================== CÁLCULOS ====================

    /**
     * Calcula y actualiza los totales de una orden
     * Recalcula subtotal, impuesto y total
     *
     * @param id ID de la orden
     * @return Orden con totales actualizados
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     */
    OrdenCompraDTO calcularTotales(Long id);

    /**
     * Calcula el monto total de órdenes por estado
     *
     * @param estado Estado de las órdenes
     * @return Monto total
     */
    java.math.BigDecimal calcularMontoTotalPorEstado(EstadoOrden estado);

    /**
     * Calcula el monto total de órdenes en un período
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Monto total
     */
    java.math.BigDecimal calcularMontoTotalPeriodo(LocalDate fechaInicio, LocalDate fechaFin);

    // ==================== ESTADÍSTICAS ====================

    /**
     * Obtiene estadísticas generales de compras
     *
     * @return Estadísticas completas
     */
    EstadisticasComprasDTO obtenerEstadisticas();

    /**
     * Obtiene estadísticas de compras en un período
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Estadísticas del período
     */
    EstadisticasComprasDTO obtenerEstadisticasPeriodo(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Cuenta órdenes por estado
     *
     * @param estado Estado a contar
     * @return Número de órdenes
     */
    long contarPorEstado(EstadoOrden estado);

    /**
     * Cuenta órdenes de un proveedor
     *
     * @param proveedorId ID del proveedor
     * @return Número de órdenes
     */
    long contarPorProveedor(Long proveedorId);

    /**
     * Cuenta órdenes en un rango de fechas
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Número de órdenes
     */
    long contarPorPeriodo(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Cuenta el total de órdenes
     *
     * @return Número total de órdenes
     */
    long contarTodos();

    /**
     * Calcula el promedio de días de entrega
     *
     * @return Promedio de días
     */
    Double calcularPromedioTiempoEntrega();

    /**
     * Calcula la tasa de cumplimiento de proveedores
     * Órdenes entregadas a tiempo vs total de órdenes
     *
     * @return Porcentaje de cumplimiento
     */
    Double calcularTasaCumplimiento();

    // ==================== VALIDACIONES ====================

    /**
     * Verifica si una orden existe
     *
     * @param id ID de la orden
     * @return true si existe
     */
    boolean existe(Long id);

    /**
     * Verifica si una orden es modificable
     *
     * @param id ID de la orden
     * @return true si está en estado BORRADOR o PENDIENTE
     */
    boolean esModificable(Long id);

    /**
     * Verifica si una orden puede recibir mercancía
     *
     * @param id ID de la orden
     * @return true si está en estado recibible
     */
    boolean esRecibible(Long id);

    /**
     * Verifica si una orden puede ser cancelada
     *
     * @param id ID de la orden
     * @return true si no está COMPLETADA ni CANCELADA
     */
    boolean esCancelable(Long id);

    /**
     * Verifica si todos los detalles de una orden están recibidos
     *
     * @param id ID de la orden
     * @return true si todos los detalles están completos
     */
    boolean todosLosDetallesRecibidos(Long id);

    // ==================== OPERACIONES DE ELIMINACIÓN ====================

    /**
     * Elimina una orden (solo si está en BORRADOR y no tiene impacto en inventario)
     *
     * @param id ID de la orden
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     * @throws IllegalStateException si no se puede eliminar
     */
    void eliminar(Long id);
}
