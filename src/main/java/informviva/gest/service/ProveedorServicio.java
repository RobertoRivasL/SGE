package informviva.gest.service;

import informviva.gest.dto.EstadisticasComprasDTO;
import informviva.gest.dto.ProveedorDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Servicio para gestión de proveedores
 * Maneja toda la lógica de negocio relacionada con proveedores
 *
 * Aplicación de principios SOLID:
 * - S: Responsabilidad única para lógica de proveedores
 * - O: Abierto para extensión (nuevas operaciones)
 * - L: Substitución de Liskov (implementaciones intercambiables)
 * - I: Interface segregada específica para proveedores
 * - D: Depende de abstracciones, no de implementaciones concretas
 *
 * @author Roberto Rivas
 * @version 1.0
 * @since FASE 3 - Módulo de Compras
 */
public interface ProveedorServicio {

    // ==================== OPERACIONES CRUD ====================

    /**
     * Guarda un nuevo proveedor
     *
     * @param proveedorDTO Datos del proveedor a guardar
     * @return Proveedor guardado con ID asignado
     * @throws IllegalArgumentException si los datos son inválidos
     */
    ProveedorDTO guardar(ProveedorDTO proveedorDTO);

    /**
     * Actualiza un proveedor existente
     *
     * @param id ID del proveedor a actualizar
     * @param proveedorDTO Datos actualizados del proveedor
     * @return Proveedor actualizado
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe el proveedor
     * @throws IllegalArgumentException si los datos son inválidos
     */
    ProveedorDTO actualizar(Long id, ProveedorDTO proveedorDTO);

    /**
     * Busca un proveedor por ID
     *
     * @param id ID del proveedor
     * @return Proveedor encontrado
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     */
    ProveedorDTO buscarPorId(Long id);

    /**
     * Busca todos los proveedores con paginación
     *
     * @param pageable Información de paginación
     * @return Página de proveedores
     */
    Page<ProveedorDTO> buscarTodos(Pageable pageable);

    /**
     * Busca todos los proveedores sin paginación
     *
     * @return Lista de todos los proveedores
     */
    List<ProveedorDTO> buscarTodos();

    /**
     * Busca proveedores activos sin paginación
     *
     * @return Lista de proveedores activos
     */
    List<ProveedorDTO> buscarActivos();

    /**
     * Busca proveedores activos con paginación
     *
     * @param pageable Información de paginación
     * @return Página de proveedores activos
     */
    Page<ProveedorDTO> buscarActivos(Pageable pageable);

    /**
     * Busca proveedores inactivos con paginación
     *
     * @param pageable Información de paginación
     * @return Página de proveedores inactivos
     */
    Page<ProveedorDTO> buscarInactivos(Pageable pageable);

    /**
     * Busca proveedores por término de búsqueda (nombre, RUT, email, etc.)
     *
     * @param termino Término de búsqueda
     * @param pageable Información de paginación
     * @return Página de proveedores que coinciden
     */
    Page<ProveedorDTO> buscarPorTermino(String termino, Pageable pageable);

    /**
     * Busca proveedores por nombre
     *
     * @param nombre Nombre o parte del nombre
     * @param pageable Información de paginación
     * @return Página de proveedores que coinciden
     */
    Page<ProveedorDTO> buscarPorNombre(String nombre, Pageable pageable);

    /**
     * Busca un proveedor por RUT
     *
     * @param rut RUT del proveedor
     * @return Proveedor encontrado
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     */
    ProveedorDTO buscarPorRut(String rut);

    /**
     * Busca proveedores por categoría
     *
     * @param categoria Categoría del proveedor
     * @param pageable Información de paginación
     * @return Página de proveedores de la categoría
     */
    Page<ProveedorDTO> buscarPorCategoria(String categoria, Pageable pageable);

    /**
     * Busca proveedores por ciudad
     *
     * @param ciudad Ciudad del proveedor
     * @param pageable Información de paginación
     * @return Página de proveedores de la ciudad
     */
    Page<ProveedorDTO> buscarPorCiudad(String ciudad, Pageable pageable);

    /**
     * Busca proveedores con calificación mínima
     *
     * @param calificacion Calificación mínima
     * @param pageable Información de paginación
     * @return Página de proveedores con calificación suficiente
     */
    Page<ProveedorDTO> buscarPorCalificacionMinima(Integer calificacion, Pageable pageable);

    /**
     * Busca proveedores con criterios múltiples
     *
     * @param nombre Nombre del proveedor (opcional)
     * @param categoria Categoría (opcional)
     * @param ciudad Ciudad (opcional)
     * @param soloActivos Si solo buscar activos
     * @param pageable Información de paginación
     * @return Página de proveedores que cumplen los criterios
     */
    Page<ProveedorDTO> buscarConCriterios(String nombre, String categoria, String ciudad,
                                          boolean soloActivos, Pageable pageable);

    // ==================== OPERACIONES DE ESTADO ====================

    /**
     * Activa un proveedor
     *
     * @param id ID del proveedor
     * @return Proveedor activado
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     */
    ProveedorDTO activar(Long id);

    /**
     * Desactiva un proveedor
     *
     * @param id ID del proveedor
     * @return Proveedor desactivado
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     */
    ProveedorDTO desactivar(Long id);

    /**
     * Elimina un proveedor (eliminación lógica)
     *
     * @param id ID del proveedor
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     * @throws IllegalStateException si el proveedor tiene órdenes asociadas
     */
    void eliminar(Long id);

    // ==================== VALIDACIONES ====================

    /**
     * Verifica si existe un proveedor con el RUT dado
     *
     * @param rut RUT del proveedor
     * @return true si existe
     */
    boolean existeRut(String rut);

    /**
     * Verifica si existe un proveedor con el RUT dado, excluyendo un ID
     *
     * @param rut RUT del proveedor
     * @param id ID a excluir
     * @return true si existe otro proveedor con ese RUT
     */
    boolean existeRutExcluyendo(String rut, Long id);

    /**
     * Verifica si un proveedor existe por ID
     *
     * @param id ID del proveedor
     * @return true si existe
     */
    boolean existe(Long id);

    /**
     * Verifica si un proveedor tiene órdenes de compra asociadas
     *
     * @param id ID del proveedor
     * @return true si tiene órdenes
     */
    boolean tieneOrdenesCompra(Long id);

    // ==================== ESTADÍSTICAS ====================

    /**
     * Obtiene estadísticas generales de proveedores
     *
     * @return Estadísticas de proveedores
     */
    EstadisticasComprasDTO.ProveedorEstadisticaDTO obtenerEstadisticas();

    /**
     * Obtiene la lista de categorías únicas
     *
     * @return Lista de categorías
     */
    List<String> obtenerCategorias();

    /**
     * Cuenta el total de proveedores
     *
     * @return Número total de proveedores
     */
    long contarTodos();

    /**
     * Cuenta proveedores activos
     *
     * @return Número de proveedores activos
     */
    long contarActivos();

    /**
     * Cuenta proveedores inactivos
     *
     * @return Número de proveedores inactivos
     */
    long contarInactivos();

    /**
     * Cuenta proveedores por categoría
     *
     * @param categoria Categoría
     * @return Número de proveedores en la categoría
     */
    long contarPorCategoria(String categoria);

    /**
     * Obtiene top proveedores por número de órdenes
     *
     * @param limite Número máximo de proveedores
     * @return Lista de proveedores destacados
     */
    List<ProveedorDTO> obtenerTopProveedoresPorOrdenes(int limite);

    /**
     * Obtiene top proveedores por monto total de compras
     *
     * @param limite Número máximo de proveedores
     * @return Lista de proveedores destacados
     */
    List<ProveedorDTO> obtenerTopProveedoresPorMonto(int limite);

    /**
     * Obtiene proveedores sin órdenes
     *
     * @return Lista de proveedores sin órdenes
     */
    List<ProveedorDTO> obtenerProveedoresSinOrdenes();

    /**
     * Cuenta proveedores que ofrecen crédito
     *
     * @return Número de proveedores con crédito
     */
    long contarProveedoresConCredito();

    /**
     * Obtiene el número de órdenes de un proveedor
     *
     * @param id ID del proveedor
     * @return Número de órdenes
     */
    long contarOrdenesProveedor(Long id);

    /**
     * Calcula el monto total de compras a un proveedor
     *
     * @param id ID del proveedor
     * @return Monto total
     */
    java.math.BigDecimal calcularMontoTotalProveedor(Long id);
}
