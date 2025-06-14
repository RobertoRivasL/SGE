package informviva.gest.service.impl;

import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.exception.ReglaDeNegocioException;
import informviva.gest.model.Producto;
import informviva.gest.repository.ProductoRepositorio;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.CacheServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación optimizada del servicio para la gestión de productos
 * Incluye funcionalidades de cache para mejorar el rendimiento
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Service
@Transactional
public class ProductoServicioImpl implements ProductoServicio {

    private static final Logger logger = LoggerFactory.getLogger(ProductoServicioImpl.class);

    // Constantes para mensajes de error
    private static final String PRODUCTO_NO_ENCONTRADO = "Producto no encontrado con ID: ";
    private static final String CODIGO_YA_EXISTE = "Ya existe un producto con el código: ";
    private static final String STOCK_INSUFICIENTE = "Stock insuficiente. Stock actual: %d, cantidad solicitada: %d";
    private static final String PRODUCTO_INACTIVO = "El producto está inactivo";
    private static final String PRODUCTO_CON_VENTAS = "No se puede eliminar el producto porque tiene ventas registradas";

    private final ProductoRepositorio productoRepositorio;
    private final CacheServicio cacheServicio;

    public ProductoServicioImpl(ProductoRepositorio productoRepositorio,
                                @Autowired(required = false) CacheServicio cacheServicio) {
        this.productoRepositorio = productoRepositorio;
        this.cacheServicio = cacheServicio;
    }

    @Override
    @Cacheable(value = "productos", key = "'all'", unless = "#result.size() == 0")
    @Transactional(readOnly = true)
    public List<Producto> listar() {
        try {
            List<Producto> productos = productoRepositorio.findAll();
            logger.info("Se han recuperado {} productos del repositorio", productos.size());

            if (productos.isEmpty()) {
                logger.warn("La lista de productos está vacía");
            } else {
                logger.debug("Primer producto recuperado: ID={}, Nombre={}",
                        productos.get(0).getId(), productos.get(0).getNombre());
            }

            return productos;
        } catch (Exception e) {
            logger.error("Error al listar productos: {}", e.getMessage());
            throw new RuntimeException("Error al obtener la lista de productos", e);
        }
    }

    @Override
    @Cacheable(value = "productos", key = "'paginated-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<Producto> listarPaginados(Pageable pageable) {
        try {
            Page<Producto> productos = productoRepositorio.findAll(pageable);
            logger.debug("Productos paginados recuperados: página {}, total elementos: {}",
                    pageable.getPageNumber(), productos.getTotalElements());
            return productos;
        } catch (Exception e) {
            logger.error("Error al listar productos paginados: {}", e.getMessage());
            throw new RuntimeException("Error al obtener productos paginados", e);
        }
    }

    @Override
    @Cacheable(value = "productos", key = "'id-' + #id")
    @Transactional(readOnly = true)
    public Producto buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }

        logger.debug("Buscando producto con ID: {}", id);
        return productoRepositorio.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Producto no encontrado con ID: {}", id);
                    return new RecursoNoEncontradoException(PRODUCTO_NO_ENCONTRADO + id);
                });
    }

    @Override
    @Cacheable(value = "productos", key = "'codigo-' + #codigo")
    @Transactional(readOnly = true)
    public Producto buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código no puede estar vacío");
        }

        String codigoNormalizado = codigo.trim().toUpperCase();
        logger.debug("Buscando producto con código: {}", codigoNormalizado);

        try {
            Optional<Producto> producto = productoRepositorio.findByCodigo(codigoNormalizado);
            return producto.orElse(null);
        } catch (Exception e) {
            logger.error("Error al buscar producto por código: {}", e.getMessage());
            throw new RuntimeException("Error al buscar el producto", e);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "productos", allEntries = true),
            @CacheEvict(value = "productos-activos", allEntries = true),
            @CacheEvict(value = "productos-stock", allEntries = true)
    })
    public Producto guardar(Producto producto) {
        try {
            validarProducto(producto);

            // Validar código único
            if (existePorCodigo(producto.getCodigo(), producto.getId())) {
                throw new ReglaDeNegocioException(CODIGO_YA_EXISTE + producto.getCodigo());
            }

            // Normalizar código
            if (producto.getCodigo() != null) {
                producto.setCodigo(producto.getCodigo().trim().toUpperCase());
            }

            // Establecer fechas
            if (producto.getId() == null) {
                producto.setFechaCreacion(LocalDateTime.now());
                producto.setActivo(true);
            }
            producto.setFechaActualizacion(LocalDateTime.now());

            Producto productoGuardado = productoRepositorio.save(producto);

            // Limpiar cache si está disponible
            if (cacheServicio != null) {
                cacheServicio.limpiarCacheProductos();
            }

            logger.info("Producto guardado exitosamente: ID={}, Código={}",
                    productoGuardado.getId(), productoGuardado.getCodigo());

            return productoGuardado;

        } catch (Exception e) {
            logger.error("Error al guardar producto: {}", e.getMessage());
            throw new RuntimeException("Error al guardar el producto", e);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "productos", allEntries = true),
            @CacheEvict(value = "productos-activos", allEntries = true),
            @CacheEvict(value = "productos-stock", allEntries = true)
    })
    public Producto actualizar(Long id, Producto producto) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }

        try {
            Producto productoExistente = buscarPorId(id);

            validarProducto(producto);

            // Validar código único (excluyendo el producto actual)
            if (existePorCodigo(producto.getCodigo(), id)) {
                throw new ReglaDeNegocioException(CODIGO_YA_EXISTE + producto.getCodigo());
            }

            // Actualizar campos
            productoExistente.setNombre(producto.getNombre());
            productoExistente.setDescripcion(producto.getDescripcion());
            productoExistente.setCodigo(producto.getCodigo().trim().toUpperCase());
            productoExistente.setPrecio(producto.getPrecio());
            productoExistente.setStock(producto.getStock());
            productoExistente.setStockMinimo(producto.getStockMinimo());
            productoExistente.setCategoria(producto.getCategoria());
            productoExistente.setFechaActualizacion(LocalDateTime.now());

            Producto productoActualizado = productoRepositorio.save(productoExistente);

            // Limpiar cache
            if (cacheServicio != null) {
                cacheServicio.limpiarCacheProductos();
            }

            logger.info("Producto actualizado: ID={}, Código={}", id, productoActualizado.getCodigo());

            return productoActualizado;

        } catch (Exception e) {
            logger.error("Error al actualizar producto con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al actualizar el producto", e);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "productos", allEntries = true),
            @CacheEvict(value = "productos-activos", allEntries = true),
            @CacheEvict(value = "productos-stock", allEntries = true)
    })
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }

        try {
            Producto producto = buscarPorId(id);

            // Verificar si el producto tiene ventas (regla de negocio)
            if (tieneVentasAsociadas(id)) {
                throw new ReglaDeNegocioException(PRODUCTO_CON_VENTAS);
            }

            productoRepositorio.deleteById(id);

            // Limpiar cache
            if (cacheServicio != null) {
                cacheServicio.limpiarCacheProductos();
            }

            logger.info("Producto eliminado: ID={}, Código={}", id, producto.getCodigo());

        } catch (RecursoNoEncontradoException | ReglaDeNegocioException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al eliminar producto con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al eliminar el producto", e);
        }
    }

    @Override
    @Cacheable(value = "productos-activos", key = "'active'", unless = "#result.size() == 0")
    @Transactional(readOnly = true)
    public List<Producto> listarActivos() {
        try {
            List<Producto> productosActivos = productoRepositorio.findByActivoTrue();
            logger.debug("Se encontraron {} productos activos", productosActivos.size());
            return productosActivos;
        } catch (Exception e) {
            logger.error("Error al listar productos activos: {}", e.getMessage());
            throw new RuntimeException("Error al obtener productos activos", e);
        }
    }

    @Override
    @Cacheable(value = "productos-stock", key = "'bajo-stock-' + #umbral")
    @Transactional(readOnly = true)
    public List<Producto> listarConBajoStock(int umbral) {
        if (umbral < 0) {
            throw new IllegalArgumentException("El umbral no puede ser negativo");
        }

        try {
            List<Producto> productos = productoRepositorio.findByStockLessThanOrderByStockAsc(umbral);
            logger.info("Se encontraron {} productos con stock bajo (umbral: {})", productos.size(), umbral);
            return productos;
        } catch (Exception e) {
            logger.error("Error al obtener productos con bajo stock: {}", e.getMessage());
            throw new RuntimeException("Error al obtener productos con bajo stock", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return List.of();
        }

        try {
            String nombreBusqueda = nombre.trim();
            List<Producto> productos = productoRepositorio.findByNombreContainingIgnoreCase(nombreBusqueda);
            logger.debug("Búsqueda por nombre '{}': {} resultados", nombreBusqueda, productos.size());
            return productos;
        } catch (Exception e) {
            logger.error("Error al buscar productos por nombre: {}", e.getMessage());
            throw new RuntimeException("Error al buscar productos por nombre", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarPorCategoria(Long categoriaId) {
        if (categoriaId == null) {
            throw new IllegalArgumentException("El ID de categoría no puede ser nulo");
        }

        try {
            List<Producto> productos = productoRepositorio.findByCategoriaId(categoriaId);
            logger.debug("Productos por categoría {}: {} encontrados", categoriaId, productos.size());
            return productos;
        } catch (Exception e) {
            logger.error("Error al listar productos por categoría: {}", e.getMessage());
            throw new RuntimeException("Error al obtener productos por categoría", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> buscarPorNombreOCodigoPaginado(String termino, Pageable pageable) {
        if (termino == null || termino.trim().isEmpty()) {
            throw new IllegalArgumentException("El término de búsqueda no puede estar vacío");
        }

        try {
            String terminoBusqueda = termino.trim().toUpperCase();
            Page<Producto> productos = productoRepositorio.buscarPorNombreOCodigo(terminoBusqueda, pageable);
            logger.debug("Búsqueda paginada por '{}': {} resultados en página {}",
                    terminoBusqueda, productos.getTotalElements(), pageable.getPageNumber());
            return productos;
        } catch (Exception e) {
            logger.error("Error al buscar productos por nombre o código: {}", e.getMessage());
            throw new RuntimeException("Error al buscar productos", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> buscarPorCategoriaPaginado(String categoria, Pageable pageable) {
        if (categoria == null || categoria.trim().isEmpty()) {
            throw new IllegalArgumentException("La categoría no puede estar vacía");
        }

        try {
            String categoriaBusqueda = categoria.trim();
            Page<Producto> productos = productoRepositorio.buscarPorCategoria(categoriaBusqueda, pageable);
            logger.debug("Búsqueda paginada por categoría '{}': {} resultados",
                    categoriaBusqueda, productos.getTotalElements());
            return productos;
        } catch (Exception e) {
            logger.error("Error al buscar productos por categoría: {}", e.getMessage());
            throw new RuntimeException("Error al buscar productos por categoría", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> listarConStockPaginado(Pageable pageable) {
        try {
            Page<Producto> productos = productoRepositorio.listarConStock(pageable);
            logger.debug("Productos con stock paginados: {} elementos en página {}",
                    productos.getTotalElements(), pageable.getPageNumber());
            return productos;
        } catch (Exception e) {
            logger.error("Error al listar productos con stock: {}", e.getMessage());
            throw new RuntimeException("Error al obtener productos con stock", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCodigo(String codigo, Long id) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return false;
        }

        String codigoNormalizado = codigo.trim().toUpperCase();

        try {
            if (id == null) {
                return productoRepositorio.existsByCodigo(codigoNormalizado);
            } else {
                return productoRepositorio.existsByCodigoAndIdNot(codigoNormalizado, id);
            }
        } catch (Exception e) {
            logger.error("Error al verificar existencia de código: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "productos", key = "'id-' + #id"),
            @CacheEvict(value = "productos-activos", allEntries = true)
    })
    public boolean cambiarEstado(Long id, boolean activo) {
        try {
            Producto producto = buscarPorId(id);
            producto.setActivo(activo);
            producto.setFechaActualizacion(LocalDateTime.now());

            productoRepositorio.save(producto);

            // Limpiar cache específico
            if (cacheServicio != null) {
                cacheServicio.limpiarCacheProductos();
            }

            logger.info("Estado del producto {} cambiado a: {}", id, activo ? "ACTIVO" : "INACTIVO");
            return true;

        } catch (Exception e) {
            logger.error("Error al cambiar estado del producto {}: {}", id, e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public boolean descontarStock(Long id, Integer cantidad) {
        if (id == null || cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("ID y cantidad deben ser válidos y positivos");
        }

        try {
            Producto producto = buscarPorId(id);

            if (!producto.getActivo()) {
                throw new ReglaDeNegocioException(PRODUCTO_INACTIVO);
            }

            if (producto.getStock() == null || producto.getStock() < cantidad) {
                throw new ReglaDeNegocioException(
                        String.format(STOCK_INSUFICIENTE,
                                producto.getStock() != null ? producto.getStock() : 0, cantidad)
                );
            }

            producto.setStock(producto.getStock() - cantidad);
            producto.setFechaActualizacion(LocalDateTime.now());

            productoRepositorio.save(producto);

            // Limpiar cache relacionado con stock
            if (cacheServicio != null) {
                cacheServicio.limpiarCacheProductos();
            }

            logger.info("Stock descontado para producto {}: {} unidades. Stock restante: {}",
                    id, cantidad, producto.getStock());

            return true;

        } catch (ReglaDeNegocioException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al descontar stock del producto {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al descontar stock", e);
        }
    }

    @Override
    @Transactional
    public boolean aumentarStock(Long id, Integer cantidad) {
        if (id == null || cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("ID y cantidad deben ser válidos y positivos");
        }

        try {
            Producto producto = buscarPorId(id);

            int stockActual = producto.getStock() != null ? producto.getStock() : 0;
            producto.setStock(stockActual + cantidad);
            producto.setFechaActualizacion(LocalDateTime.now());

            productoRepositorio.save(producto);

            // Limpiar cache relacionado con stock
            if (cacheServicio != null) {
                cacheServicio.limpiarCacheProductos();
            }

            logger.info("Stock aumentado para producto {}: {} unidades. Stock total: {}",
                    id, cantidad, producto.getStock());

            return true;

        } catch (Exception e) {
            logger.error("Error al aumentar stock del producto {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al aumentar stock", e);
        }
    }

    @Override
    public Producto save(Producto producto) {
        return guardar(producto);
    }

    // Métodos auxiliares privados

    /**
     * Valida los datos básicos del producto
     */
    private void validarProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }

        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }

        if (producto.getCodigo() == null || producto.getCodigo().trim().isEmpty()) {
            throw new IllegalArgumentException("El código del producto es obligatorio");
        }

        if (producto.getPrecio() == null || producto.getPrecio().doubleValue() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero");
        }

        if (producto.getStock() != null && producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
    }

    /**
     * Verifica si el producto tiene ventas asociadas
     */
    private boolean tieneVentasAsociadas(Long productoId) {
        // Implementar lógica para verificar si el producto tiene ventas
        // Por ahora retorna false, pero debería consultar la tabla de ventas
        try {
            // return ventaRepositorio.existsByProductoId(productoId);
            return false; // Temporal
        } catch (Exception e) {
            logger.warn("Error al verificar ventas asociadas para producto {}: {}", productoId, e.getMessage());
            return false;
        }
    }
}