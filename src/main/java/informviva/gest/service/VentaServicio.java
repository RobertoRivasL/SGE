package informviva.gest.service;

import informviva.gest.dto.VentaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interfaz del servicio de ventas que define el contrato
 * para la gestión de ventas en el sistema.
 *
 * IMPORTANTE: Todos los métodos públicos trabajan exclusivamente con DTOs.
 * Las entidades JPA son manejadas internamente por la implementación.
 *
 * Aplicación del principio de Inversión de Dependencias (D de SOLID):
 * - Los módulos de alto nivel no dependen de módulos de bajo nivel
 * - Ambos dependen de abstracciones (esta interfaz)
 *
 * @author Sistema de Gestión Empresarial
 * @version 2.0 - Refactorizado Fase 1
 */
public interface VentaServicio {

    // ============================================
    // OPERACIONES CRUD PRINCIPALES
    // ============================================

    /**
     * Obtiene todas las ventas
     *
     * @return Lista de VentaDTO
     */
    List<VentaDTO> buscarTodos();

    /**
     * Obtiene todas las ventas con paginación
     *
     * @param pageable Información de paginación
     * @return Página de VentaDTO
     */
    Page<VentaDTO> buscarTodos(Pageable pageable);

    /**
     * Busca una venta por su ID
     *
     * @param id Identificador de la venta
     * @return VentaDTO encontrada
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     */
    VentaDTO buscarPorId(Long id);

    /**
     * Guarda una nueva venta o actualiza una existente
     *
     * @param ventaDTO Datos de la venta a guardar
     * @return VentaDTO guardada con su ID asignado
     * @throws IllegalArgumentException si los datos no son válidos
     * @throws informviva.gest.exception.StockInsuficienteException si no hay stock suficiente
     */
    VentaDTO guardar(VentaDTO ventaDTO);

    /**
     * Actualiza una venta existente
     *
     * @param id Identificador de la venta a actualizar
     * @param ventaDTO Nuevos datos de la venta
     * @return VentaDTO actualizada
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     * @throws IllegalArgumentException si los datos no son válidos
     */
    VentaDTO actualizar(Long id, VentaDTO ventaDTO);

    /**
     * Elimina una venta por su ID
     *
     * @param id Identificador de la venta a eliminar
     * @throws informviva.gest.exception.RecursoNoEncontradoException si la venta no existe
     */
    void eliminar(Long id);

    // ============================================
    // BÚSQUEDAS ESPECÍFICAS
    // ============================================

    /**
     * Busca ventas por cliente
     *
     * @param clienteId Identificador del cliente
     * @return Lista de VentaDTO del cliente
     */
    List<VentaDTO> buscarPorCliente(Long clienteId);

    /**
     * Busca ventas por vendedor
     *
     * @param vendedorId Identificador del vendedor
     * @return Lista de VentaDTO del vendedor
     */
    List<VentaDTO> buscarPorVendedor(Long vendedorId);

    /**
     * Busca ventas en un rango de fechas
     *
     * @param fechaInicio Fecha de inicio (inclusive)
     * @param fechaFin Fecha de fin (inclusive)
     * @return Lista de VentaDTO en el rango
     */
    List<VentaDTO> buscarPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Busca ventas por estado
     *
     * @param estado Estado de la venta (PENDIENTE, COMPLETADA, ANULADA)
     * @return Lista de VentaDTO con el estado especificado
     */
    List<VentaDTO> buscarPorEstado(String estado);

    // ============================================
    // OPERACIONES DE NEGOCIO
    // ============================================

    /**
     * Anula una venta y restaura el stock de productos
     *
     * @param id Identificador de la venta a anular
     * @param motivoAnulacion Motivo de la anulación
     * @throws informviva.gest.exception.RecursoNoEncontradoException si la venta no existe
     * @throws IllegalStateException si la venta ya está anulada
     */
    void anularVenta(Long id, String motivoAnulacion);

    // ============================================
    // VALIDACIONES Y VERIFICACIONES
    // ============================================

    /**
     * Verifica si existe una venta con el ID dado
     *
     * @param id Identificador a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existe(Long id);

    // ============================================
    // CÁLCULOS Y ESTADÍSTICAS
    // ============================================

    /**
     * Calcula el total de ventas realizadas a un cliente
     *
     * @param clienteId Identificador del cliente
     * @return Total de ventas (excluyendo anuladas)
     */
    BigDecimal calcularTotalVentasPorCliente(Long clienteId);

    /**
     * Cuenta el número total de ventas
     *
     * @return Número total de ventas
     */
    long contar();

    /**
     * Cuenta el número de ventas en un período
     *
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Número de ventas en el período
     */
    long contarVentasPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}