package informviva.gest.service;

import informviva.gest.dto.ProductoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interface del servicio de productos que define el contrato
 * para la gestión de productos en el sistema.
 *
 * Aplicación del principio de Inversión de Dependencias (D de SOLID):
 * - Los módulos de alto nivel no dependen de módulos de bajo nivel
 * - Ambos dependen de abstracciones (esta interface)
 *
 * @author Tu nombre
 * @version 1.0
 */
public interface ProductoServicio {

    /**
     * Obtiene todos los productos como DTOs
     *
     * @return Lista de ProductoDTO
     */
    List<ProductoDTO> buscarTodosDTO();

    /**
     * Obtiene todos los productos con paginación como DTOs
     *
     * @param pageable Información de paginación
     * @return Página de ProductoDTO
     */
    Page<ProductoDTO> buscarTodosDTO(Pageable pageable);

    /**
     * Busca un producto por su ID y lo devuelve como DTO
     *
     * @param id Identificador del producto
     * @return ProductoDTO encontrado
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     */
    ProductoDTO buscarPorIdDTO(Long id);

    /**
     * Guarda un nuevo producto desde un DTO
     *
     * @param productoDTO Datos del producto a guardar
     * @return ProductoDTO guardado con su ID asignado
     * @throws IllegalArgumentException si los datos no son válidos
     */
    ProductoDTO guardarDTO(ProductoDTO productoDTO);

    /**
     * Actualiza un producto existente desde un DTO
     *
     * @param id Identificador del producto a actualizar
     * @param productoDTO Nuevos datos del producto
     * @return ProductoDTO actualizado
     * @throws informviva.gest.exception.RecursoNoEncontradoException si no existe
     * @throws IllegalArgumentException si los datos no son válidos
     */
    ProductoDTO actualizarDTO(Long id, ProductoDTO productoDTO);

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
     * Actualiza el stock de un producto
     *
     * @param id Identificador del producto
     * @param cantidad Cantidad a sumar/restar del stock (positiva para agregar, negativa para quitar)
     * @throws informviva.gest.exception.RecursoNoEncontradoException si el producto no existe
     * @throws informviva.gest.exception.StockInsuficienteException si se intenta restar más stock del disponible
     */
    void actualizarStock(Long id, Integer cantidad);

    /**
     * Busca productos con stock bajo (menor al mínimo especificado)
     *
     * @param minimoStock Cantidad mínima de stock
     * @return Lista de ProductoDTO con stock bajo
     */
    List<ProductoDTO> buscarConStockBajo(Integer minimoStock);

    /**
     * Elimina un producto por su ID
     *
     * @param id Identificador del producto a eliminar
     * @throws informviva.gest.exception.RecursoNoEncontradoException si el producto no existe
     */
    void eliminar(Long id);

    /**
     * Verifica si existe un producto con el ID dado
     *
     * @param id Identificador a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existe(Long id);

    /**
     * Cuenta el número total de productos
     *
     * @return Número total de productos
     */
    long contar();
}