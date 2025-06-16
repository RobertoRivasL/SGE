package informviva.gest.service;

import informviva.gest.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;

/**
 * Interfaz para la gestión de productos
 *
 * @author Roberto Rivas
 * @version 2.0
 */

public interface ProductoServicio {

    /**
     * Obtiene todos los productos
     *
     * @return Lista de productos
     */
    List<Producto> listar();

    /**
     * Obtiene productos paginados
     *
     * @param pageable Configuración de paginación
     * @return Página de productos
     */
    Page<Producto> listarPaginados(Pageable pageable);

    /**
     * Busca un producto por su ID
     *
     * @param id ID del producto
     * @return Producto encontrado o null si no existe
     */
    Producto buscarPorId(Long id);

    /**
     * Busca un producto por su código
     *
     * @param codigo Código del producto
     * @return Producto encontrado o null si no existe
     */
    Producto buscarPorCodigo(String codigo);

    /**
     * Guarda un nuevo producto o actualiza uno existente
     *
     * @param producto Producto a guardar
     * @return Producto guardado
     */
    Producto guardar(Producto producto);

    /**
     * Actualiza un producto existente
     *
     * @param id       ID del producto
     * @param producto Datos actualizados del producto
     * @return Producto actualizado
     */
    Producto actualizar(Long id, Producto producto);

    /**
     * Elimina un producto por su ID
     *
     * @param id ID del producto
     */
    void eliminar(Long id);

    /**
     * Verifica si existe un producto con el código dado
     *
     * @param codigo Código del producto
     * @return true si existe, false en caso contrario
     */
    boolean existePorCodigo(String codigo);

    /**
     * Obtiene productos con stock menor al umbral especificado
     *
     * @param umbral Cantidad máxima de stock para considerar como bajo
     * @return Lista de productos con bajo stock
     */
    List<Producto> listarConBajoStock(int umbral);

    /**
     * Busca productos por nombre (contiene el texto)
     *
     * @param nombre Texto a buscar en el nombre
     * @return Lista de productos que coinciden
     */
    List<Producto> buscarPorNombre(String nombre);

    /**
     * Obtiene productos por categoría
     *
     * @param categoriaId ID de la categoría
     * @return Lista de productos de la categoría
     */
    List<Producto> listarPorCategoria(Long categoriaId);

    /**
     * Obtiene productos activos solamente
     *
     * @return Lista de productos activos
     */
    List<Producto> listarActivos();

    /**
     * Activa o desactiva un producto
     *
     * @param id     ID del producto
     * @param activo true para activar, false para desactivar
     * @return true si se cambió correctamente, false en caso contrario
     */
    boolean cambiarEstado(Long id, boolean activo);

    /**
     * Actualiza el stock de un producto
     *
     * @param id         ID del producto
     * @param nuevoStock Nuevo valor de stock
     * @return Producto actualizado
     */
    Producto actualizarStock(Long id, Integer nuevoStock);

    /**
     * Reduce el stock de un producto (para ventas)
     *
     * @param id       ID del producto
     * @param cantidad Cantidad a reducir
     * @return Producto actualizado
     * @throws IllegalArgumentException si no hay suficiente stock
     */
    Producto reducirStock(Long id, Integer cantidad);

    /**
     * Aumenta el stock de un producto (para compras/devoluciones)
     *
     * @param id       ID del producto
     * @param cantidad Cantidad a aumentar
     * @return Producto actualizado
     */
    Producto aumentarStock(Long id, Integer cantidad);

    /**
     * Cuenta el total de productos
     *
     * @return Número total de productos
     */
    Long contarTodos();

    /**
     * Cuenta productos activos
     *
     * @return Número de productos activos
     */
    Long contarActivos();

    /**
     * Cuenta productos con bajo stock
     *
     * @param umbral Umbral de stock bajo
     * @return Número de productos con bajo stock
     */
    Long contarConBajoStock(int umbral);

    Page<Producto> buscarPorNombreOCodigoPaginado(String search, Pageable pageable);

    Page<Producto> buscarPorCategoriaPaginado(String categoria, Pageable pageable);

    Page<Producto> listarConStockPaginado(Pageable pageable);


    boolean existePorCodigo(String codigo, Long excludeId);

    //
    List<String> listarCategorias();

    /**
     * Busca productos por nombre o código con paginación.
     *
     * @param buscar   Texto a buscar.
     * @param pageable Configuración de paginación.
     * @return Página de productos encontrados.
     */
    Page<Producto> buscarProductos(String buscar, Pageable pageable);

    /**
     * Obtiene productos por categoría con paginación.
     *
     * @param categoriaId ID de la categoría.
     * @param pageable    Configuración de paginación.
     * @return Página de productos de la categoría.
     */
    Page<Producto> findByCategoriaId(Long categoriaId, Pageable pageable);

    /**
     * Obtiene todos los productos activos con paginación.
     *
     * @param pageable Configuración de paginación.
     * @return Página de productos activos.
     */
    Page<Producto> findAllActivos(Pageable pageable);

    /**
     * Obtiene todos los productos inactivos con paginación.
     *
     * @param pageable Configuración de paginación.
     * @return Página de productos inactivos.
     */
    Page<Producto> findAllInactivos(Pageable pageable);

    /**
     * Obtiene todos los productos con paginación.
     *
     * @param pageable Configuración de paginación.
     * @return Página de productos.
     */
    Page<Producto> findAll(Pageable pageable);

    Producto save(Producto producto);

    Page<Producto> buscarPorNombre(String nombre, Pageable pageable);

    Page<Producto> buscarTodosProductos(String termino, Pageable pageable);

    Producto findById(Long id);

    Page<Producto> findProductosBajoStock(int stockMinimo, Pageable pageable);


}