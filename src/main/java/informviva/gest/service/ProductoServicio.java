package informviva.gest.service;

import informviva.gest.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interfaz del servicio para la gestión de productos del sistema.
 * 100% Compatible con ProductoServicioImpl existente.
 *
 * @author Roberto Rivas Lopez
 * @version 2.1.0 - EXACTAMENTE COMPATIBLE CON IMPLEMENTACIÓN
 */
public interface ProductoServicio {

    // ===================== MÉTODOS DE CONSULTA BÁSICA =====================

    /**
     * Lista todos los productos del sistema
     */
    List<Producto> listar();

    /**
     * Lista productos con paginación
     */
    Page<Producto> listarPaginados(Pageable pageable);

    /**
     * Busca producto por ID - RETORNA PRODUCTO DIRECTAMENTE
     */
    Producto buscarPorId(Long id);

    /**
     * Busca producto por código
     */
    Producto buscarPorCodigo(String codigo);

    /**
     * Lista solo productos activos
     */
    List<Producto> listarActivos();

    /**
     * Lista productos con bajo stock
     */
    List<Producto> listarConBajoStock(int umbral);

    // ===================== MÉTODOS DE BÚSQUEDA =====================

    /**
     * Busca productos por nombre
     */
    List<Producto> buscarPorNombre(String nombre);

    /**
     * Busca productos por nombre con paginación
     */
    Page<Producto> buscarPorNombre(String nombre, Pageable pageable);

    /**
     * Lista productos por categoría ID
     */
    List<Producto> listarPorCategoria(Long categoriaId);

    /**
     * Busca productos por nombre o código (paginado)
     */
    Page<Producto> buscarPorNombreOCodigoPaginado(String search, Pageable pageable);

    /**
     * Busca productos por categoría (paginado)
     */
    Page<Producto> buscarPorCategoriaPaginado(String categoria, Pageable pageable);

    /**
     * Lista productos con stock disponible (paginado)
     */
    Page<Producto> listarConStockPaginado(Pageable pageable);

    /**
     * Busca productos por categoría ID con paginación
     */
    Page<Producto> findByCategoriaId(Long categoriaId, Pageable pageable);

    /**
     * Lista productos activos con paginación
     */
    Page<Producto> findAllActivos(Pageable pageable);

    /**
     * Lista productos inactivos con paginación
     */
    Page<Producto> findAllInactivos(Pageable pageable);

    /**
     * Busca todos los productos con término de búsqueda
     */
    Page<Producto> buscarTodosProductos(String termino, Pageable pageable);

    // ===================== MÉTODOS DE PERSISTENCIA =====================

    /**
     * Guarda o actualiza un producto
     */
    Producto guardar(Producto producto);

    /**
     * Actualiza un producto existente
     */
    Producto actualizar(Long id, Producto producto);

    /**
     * Elimina un producto por ID (eliminación lógica)
     */
    void eliminar(Long id);

    // ===================== MÉTODOS DE GESTIÓN DE STOCK =====================

    /**
     * Actualiza el stock de un producto
     */
    Producto actualizarStock(Long id, Integer nuevoStock);

    /**
     * Reduce el stock de un producto
     */
    Producto reducirStock(Long id, Integer cantidad);

    /**
     * Aumenta el stock de un producto
     */
    Producto aumentarStock(Long id, Integer cantidad);

    // ===================== MÉTODOS DE ESTADO =====================

    /**
     * Cambia el estado activo/inactivo de un producto
     */
    boolean cambiarEstado(Long id, boolean activo);

    // ===================== MÉTODOS DE VALIDACIÓN =====================

    /**
     * Verifica si existe un producto con el código dado
     */
    boolean existePorCodigo(String codigo);

    /**
     * Verifica si existe un producto con el código dado, excluyendo un ID
     */
    boolean existePorCodigo(String codigo, Long excludeId);

    // ===================== MÉTODOS ESTADÍSTICOS =====================

    /**
     * Cuenta el total de productos
     */
    Long contarTodos();

    /**
     * Cuenta productos activos
     */
    Long contarActivos();

    /**
     * Cuenta productos con stock bajo
     */
    Long contarConBajoStock(int umbral);

    // ===================== MÉTODOS ADICIONALES =====================

    /**
     * Lista categorías disponibles
     */
    List<String> listarCategorias();

    /**
     * Busca productos (método genérico)
     */
    Page<Producto> buscarProductos(String buscar, Pageable pageable);

    // ===================== MÉTODOS DE COMPATIBILIDAD SPRING DATA =====================

    /**
     * Método de compatibilidad - busca por ID
     */
    Producto findById(Long id);

    /**
     * Método de compatibilidad - lista todos paginado
     */
    Page<Producto> findAll(Pageable pageable);

    /**
     * Método de compatibilidad - productos con bajo stock
     */
    Page<Producto> findProductosBajoStock(int stockMinimo, Pageable pageable);

    /**
     * Método de compatibilidad - guardar
     */
    Producto save(Producto producto);

    // ===================== MÉTODOS DE CACHE =====================

    /**
     * Limpia el cache de productos
     */
    void limpiarCacheProductos();
}