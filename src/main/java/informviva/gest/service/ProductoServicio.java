package informviva.gest.service;

import informviva.gest.dto.ProductoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interfaz del servicio de productos que define el contrato
 * para la gestión de productos en el sistema.
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
public interface ProductoServicio {

    // ============================================
    // OPERACIONES CRUD PRINCIPALES
    // ============================================

    /**
     * Obtiene todos los productos
     *
     * @return Lista de ProductoDTO
     */
    List<ProductoDTO> buscarTodos();

    /**
     * Obtiene todos los productos con paginación
     *
     * @param pageable Información de paginación
     * @return Página de ProductoDTO
     */
    Page<ProductoDTO> buscarTodos(Pageable pageable);

    /**
     * Busca un producto por su ID
     *
     * @param id Identificador del producto
     * @return ProductoDTO encontrado
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     */
    ProductoDTO buscarPorId(Long id);

    /**
     * Guarda un nuevo producto o actualiza uno existente
     *
     * @param productoDTO Datos del producto a guardar
     * @return ProductoDTO guardado con su ID asignado
     * @throws IllegalArgumentException si los datos no son válidos
     */
    ProductoDTO guardar(ProductoDTO productoDTO);

    /**
     * Actualiza un producto existente
     *
     * @param id Identificador del producto a actualizar
     * @param productoDTO Nuevos datos del producto
     * @return ProductoDTO actualizado
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     * @throws IllegalArgumentException si los datos no son válidos
     */
    ProductoDTO actualizar(Long id, ProductoDTO productoDTO);

    /**
     * Elimina un producto por su ID
     *
     * @param id Identificador del producto a eliminar
     * @throws informviva.gest.exception.RecursoNoEncontradoException si el producto no existe
     */
    void eliminar(Long id);

    // ============================================
    // BÚSQUEDAS ESPECÍFICAS
    // ============================================

    /**
     * Busca productos por nombre (búsqueda parcial, insensible a mayúsculas)
     *
     * @param nombre Nombre o parte del nombre a buscar
     * @return Lista de ProductoDTO que coinciden
     */
    List<ProductoDTO> buscarPorNombre(String nombre);

    /**
     * Busca productos por categoría
     *
     * @param categoria Categoría del producto
     * @return Lista de ProductoDTO de la categoría
     */
    List<ProductoDTO> buscarPorCategoria(String categoria);

    /**
     * Busca productos dentro de un rango de precios
     *
     * @param precioMin Precio mínimo (inclusive)
     * @param precioMax Precio máximo (inclusive)
     * @return Lista de ProductoDTO en el rango de precios
     */
    List<ProductoDTO> buscarPorRangoPrecio(BigDecimal precioMin, BigDecimal precioMax);

    /**
     * Busca productos con stock bajo (menor al mínimo especificado)
     *
     * @param minimoStock Cantidad mínima de stock
     * @return Lista de ProductoDTO con stock bajo
     */
    List<ProductoDTO> buscarConStockBajo(Integer minimoStock);

    /**
     * Busca todos los productos activos
     *
     * @return Lista de ProductoDTO activos
     */
    List<ProductoDTO> buscarActivos();

    /**
     * Busca productos activos con paginación
     *
     * @param pageable Información de paginación
     * @return Página de ProductoDTO activos
     */
    Page<ProductoDTO> buscarActivos(Pageable pageable);

    /**
     * Lista todas las categorías distintas de productos
     *
     * @return Lista de nombres de categorías
     */
    List<String> listarCategorias();

    // ============================================
    // OPERACIONES DE STOCK
    // ============================================

    /**
     * Actualiza el stock de un producto
     *
     * @param id Identificador del producto
     * @param cantidad Cantidad a sumar/restar del stock (positiva para agregar, negativa para quitar)
     * @throws informviva.gest.exception.RecursoNoEncontradoException si el producto no existe
     * @throws informviva.gest.exception.StockInsuficienteException si se intenta restar más stock del disponible
     */
    void actualizarStock(Long id, Integer cantidad);

    // ============================================
    // OPERACIONES DE ACTIVACIÓN/DESACTIVACIÓN
    // ============================================

    /**
     * Activa un producto (marca como activo)
     *
     * @param id Identificador del producto
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     */
    void activar(Long id);

    /**
     * Desactiva un producto (marca como inactivo, no se elimina)
     *
     * @param id Identificador del producto
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     */
    void desactivar(Long id);

    // ============================================
    // VALIDACIONES Y VERIFICACIONES
    // ============================================

    /**
     * Verifica si existe un producto con el ID dado
     *
     * @param id Identificador a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existe(Long id);

    // ============================================
    // CONTADORES Y ESTADÍSTICAS
    // ============================================

    /**
     * Cuenta el número total de productos
     *
     * @return Número total de productos
     */
    long contar();

    /**
     * Cuenta el número de productos activos
     *
     * @return Número de productos activos
     */
    long contarActivos();

    /**
     * Cuenta productos con stock bajo (menor al mínimo)
     *
     * @param minimoStock Cantidad mínima de stock
     * @return Número de productos con stock bajo
     */
    long contarConBajoStock(Integer minimoStock);

    // ============================================
    // MÉTODOS ADICIONALES PARA CONTROLADORES
    // ============================================

    /**
     * Lista productos con bajo stock (alias de buscarConStockBajo)
     * Método agregado para compatibilidad con controladores existentes
     *
     * @param umbral Cantidad de stock mínima
     * @return Lista de ProductoDTO con stock bajo
     */
    List<ProductoDTO> listarConBajoStock(Integer umbral);

    /**
     * Busca productos por nombre o código (SKU) con paginación
     * Busca coincidencias parciales en nombre o SKU
     *
     * @param search Término de búsqueda (nombre o código)
     * @param pageable Información de paginación
     * @return Página de ProductoDTO que coinciden
     */
    Page<ProductoDTO> buscarPorNombreOCodigoPaginado(String search, Pageable pageable);

    /**
     * Busca productos por categoría con paginación
     *
     * @param categoria Categoría del producto
     * @param pageable Información de paginación
     * @return Página de ProductoDTO de la categoría
     */
    Page<ProductoDTO> buscarPorCategoriaPaginado(String categoria, Pageable pageable);

    /**
     * Lista productos con stock disponible (stock > 0) con paginación
     *
     * @param pageable Información de paginación
     * @return Página de ProductoDTO con stock
     */
    Page<ProductoDTO> listarConStockPaginado(Pageable pageable);

    /**
     * Lista todos los productos con paginación (alias de buscarTodos)
     * Método agregado para compatibilidad con controladores existentes
     *
     * @param pageable Información de paginación
     * @return Página de ProductoDTO
     */
    Page<ProductoDTO> listarPaginados(Pageable pageable);

    // ============================================
    // MÉTODOS LEGACY PARA COMPATIBILIDAD
    // ============================================

    /**
     * Busca productos por término de búsqueda con paginación
     * Método legacy para compatibilidad con ProductoControlador
     *
     * @param termino Término de búsqueda
     * @param pageable Información de paginación
     * @return Página de ProductoDTO que coinciden
     */
    Page<ProductoDTO> buscarProductos(String termino, Pageable pageable);

    /**
     * Busca productos por ID de categoría con paginación
     *
     * @param categoriaId ID de la categoría
     * @param pageable Información de paginación
     * @return Página de ProductoDTO de la categoría
     */
    Page<ProductoDTO> findByCategoriaId(Long categoriaId, Pageable pageable);

    /**
     * Alias de buscarActivos(Pageable)
     * Método legacy para compatibilidad
     *
     * @param pageable Información de paginación
     * @return Página de ProductoDTO activos
     */
    Page<ProductoDTO> findAllActivos(Pageable pageable);

    /**
     * Busca todos los productos por término (activos e inactivos)
     *
     * @param termino Término de búsqueda
     * @param pageable Información de paginación
     * @return Página de ProductoDTO que coinciden
     */
    Page<ProductoDTO> buscarTodosProductos(String termino, Pageable pageable);

    /**
     * Busca productos inactivos con paginación
     *
     * @param pageable Información de paginación
     * @return Página de ProductoDTO inactivos
     */
    Page<ProductoDTO> findAllInactivos(Pageable pageable);

    /**
     * Alias de buscarTodos(Pageable)
     * Método legacy para compatibilidad
     *
     * @param pageable Información de paginación
     * @return Página de ProductoDTO
     */
    Page<ProductoDTO> findAll(Pageable pageable);

    /**
     * Busca productos con stock bajo con paginación
     *
     * @param stockMinimo Cantidad mínima de stock
     * @param pageable Información de paginación
     * @return Página de ProductoDTO con stock bajo
     */
    Page<ProductoDTO> findProductosBajoStock(Integer stockMinimo, Pageable pageable);

    /**
     * Alias de buscarPorId(Long)
     * Método legacy para compatibilidad
     *
     * @param id Identificador del producto
     * @return ProductoDTO encontrado
     */
    ProductoDTO findById(Long id);

    /**
     * Cambia el estado activo/inactivo de un producto
     * Método legacy - Se recomienda usar activar()/desactivar()
     *
     * @param id Identificador del producto
     * @param activo true para activar, false para desactivar
     */
    void cambiarEstado(Long id, boolean activo);

    // ============================================
    // MÉTODOS ADICIONALES REQUERIDOS POR SERVICIOS DE IMPORTACIÓN
    // ============================================

    /**
     * Verifica si existe un producto con el código especificado
     *
     * @param codigo Código del producto
     * @return true si existe, false en caso contrario
     */
    boolean existePorCodigo(String codigo);

    /**
     * Verifica si existe un producto con el código especificado, excluyendo un ID
     *
     * @param codigo Código del producto
     * @param id ID a excluir de la búsqueda
     * @return true si existe otro producto con ese código
     */
    boolean existePorCodigo(String codigo, Long id);

    /**
     * Busca un producto por su código
     *
     * @param codigo Código del producto
     * @return ProductoDTO encontrado
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     */
    ProductoDTO buscarPorCodigo(String codigo);

    /**
     * Lista todos los productos (sin paginación)
     * Método legacy para compatibilidad con servicios de exportación/validación
     *
     * @return Lista de todos los ProductoDTO
     */
    List<ProductoDTO> listar();

    /**
     * Lista todos los productos activos (sin paginación)
     * Método legacy para compatibilidad con servicios de exportación
     *
     * @return Lista de ProductoDTO activos
     */
    List<ProductoDTO> listarActivos();
}