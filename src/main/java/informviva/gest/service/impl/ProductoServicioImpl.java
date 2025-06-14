package informviva.gest.service.impl;

import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.model.Producto;
import informviva.gest.repository.ProductoRepositorio;
import informviva.gest.service.ProductoServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio para la gestión de productos.
 * Proporciona operaciones CRUD y funcionalidades de negocio para productos.
 * Incluye cache integrado para optimizar el rendimiento.
 *
 * @author Roberto Rivas
 * @version 2.1
 */
@Service
@Transactional
public class ProductoServicioImpl implements ProductoServicio {

    private static final Logger logger = LoggerFactory.getLogger(ProductoServicioImpl.class);

    // Constantes para mensajes de error
    private static final String PRODUCTO_NO_ENCONTRADO = "Producto no encontrado con ID: ";
    private static final String CODIGO_YA_EXISTE = "Ya existe un producto con el código: ";
    private static final String STOCK_INSUFICIENTE = "Stock insuficiente. Stock actual: %d, cantidad solicitada: %d";
    private static final String PRODUCTO_NULO = "El producto no puede ser nulo";
    private static final String ID_INVALIDO = "El ID del producto debe ser mayor a 0";
    private static final String CODIGO_REQUERIDO = "El código del producto es requerido";
    private static final String NOMBRE_REQUERIDO = "El nombre del producto es requerido";

    private final ProductoRepositorio productoRepositorio;

    public ProductoServicioImpl(ProductoRepositorio productoRepositorio) {
        this.productoRepositorio = productoRepositorio;
    }

    // ===================== MÉTODOS DE CONSULTA =====================

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listar() {
        logger.debug("Listando todos los productos");
        try {
            List<Producto> productos = productoRepositorio.findAll();
            logger.info("Se han recuperado {} productos del repositorio", productos.size());
            return productos;
        } catch (Exception e) {
            logger.error("Error al listar productos: {}", e.getMessage());
            throw new RuntimeException("Error al obtener la lista de productos", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> listarPaginados(Pageable pageable) {
        logger.debug("Listando productos paginados: página {}, tamaño {}",
                pageable.getPageNumber(), pageable.getPageSize());
        try {
            return productoRepositorio.findAll(pageable);
        } catch (Exception e) {
            logger.error("Error al listar productos paginados: {}", e.getMessage());
            throw new RuntimeException("Error al obtener productos paginados", e);
        }
    }

    @Override
    @Cacheable(value = "productos", key = "#id")
    @Transactional(readOnly = true)
    public Producto buscarPorId(Long id) {
        validarId(id);
        logger.debug("Buscando producto por ID: {}", id);

        return productoRepositorio.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Producto no encontrado con ID: {}", id);
                    return new RecursoNoEncontradoException(PRODUCTO_NO_ENCONTRADO + id);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Producto buscarPorCodigo(String codigo) {
        logger.debug("Buscando producto por código: {}", codigo);
        if (!codigoValido(codigo)) {
            return null;
        }

        return productoRepositorio.findByCodigo(codigo.trim().toUpperCase())
                .orElse(null);  // CORRECCIÓN: Este orElse() está correcto porque findByCodigo retorna Optional<Producto>
    }

    @Override
    @Cacheable(value = "productos", key = "'activos'", unless = "#result.size() == 0")
    @Transactional(readOnly = true)
    public List<Producto> listarActivos() {
        logger.debug("Listando productos activos");
        try {
            return productoRepositorio.findByActivoTrue();
        } catch (Exception e) {
            logger.error("Error al listar productos activos: {}", e.getMessage());
            throw new RuntimeException("Error al obtener productos activos", e);
        }
    }

    @Override
    @Cacheable(value = "productos", key = "'bajo-stock-' + #umbral")
    @Transactional(readOnly = true)
    public List<Producto> listarConBajoStock(int umbral) {
        logger.debug("Listando productos con bajo stock (umbral: {})", umbral);
        if (umbral < 0) {
            throw new IllegalArgumentException("El umbral de stock no puede ser negativo");
        }

        try {
            return productoRepositorio.findByStockLessThanOrderByStockAsc(umbral);
        } catch (Exception e) {
            logger.error("Error al obtener productos con bajo stock: {}", e.getMessage());
            throw new RuntimeException("Error al obtener productos con bajo stock", e);
        }
    }

    // CORRECCIÓN 1: Implementar el método que falta en la interfaz
    @Override
    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String nombre) {
        logger.debug("Buscando productos por nombre: {}", nombre);
        if (nombre == null || nombre.trim().isEmpty()) {
            return List.of();
        }

        try {
            return productoRepositorio.findByNombreContainingIgnoreCase(nombre.trim());
        } catch (Exception e) {
            logger.error("Error al buscar productos por nombre: {}", e.getMessage());
            throw new RuntimeException("Error al buscar productos por nombre", e);
        }
    }

    // CORRECCIÓN 1: Agregar el método con Pageable que requiere la interfaz
    @Override
    @Transactional(readOnly = true)
    public Page<Producto> buscarPorNombre(String nombre, Pageable pageable) {
        logger.debug("Buscando productos por nombre con paginación: {}", nombre);
        if (nombre == null || nombre.trim().isEmpty()) {
            return Page.empty(pageable);
        }

        try {
            return productoRepositorio.findByNombreContainingIgnoreCase(nombre.trim(), pageable);
        } catch (Exception e) {
            logger.error("Error al buscar productos por nombre con paginación: {}", e.getMessage());
            throw new RuntimeException("Error al buscar productos por nombre", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarPorCategoria(Long categoriaId) {
        logger.debug("Listando productos por categoría ID: {}", categoriaId);
        if (categoriaId == null) {
            throw new IllegalArgumentException("El ID de categoría no puede ser nulo");
        }

        try {
            return productoRepositorio.findByCategoriaId(categoriaId);
        } catch (Exception e) {
            logger.error("Error al listar productos por categoría: {}", e.getMessage());
            throw new RuntimeException("Error al obtener productos por categoría", e);
        }
    }

    // ===================== MÉTODOS DE PERSISTENCIA =====================

    @Override
    @CacheEvict(value = "productos", allEntries = true)
    public Producto guardar(Producto producto) {
        logger.debug("Guardando producto: {}", producto != null ? producto.getNombre() : "null");

        validarProducto(producto);

        // Normalizar datos
        producto.setCodigo(producto.getCodigo().trim().toUpperCase());
        producto.setNombre(producto.getNombre().trim());

        // Validar duplicado para nuevo producto
        if (producto.getId() == null && existePorCodigo(producto.getCodigo())) {
            logger.warn("Intento de crear producto con código duplicado: {}", producto.getCodigo());
            throw new IllegalArgumentException(CODIGO_YA_EXISTE + producto.getCodigo());
        }

        // Validar duplicado para producto existente
        if (producto.getId() != null && existePorCodigo(producto.getCodigo(), producto.getId())) {
            logger.warn("Intento de actualizar producto con código duplicado: {}", producto.getCodigo());
            throw new IllegalArgumentException(CODIGO_YA_EXISTE + producto.getCodigo());
        }

        // Establecer fechas
        if (producto.getId() == null) {
            producto.setFechaCreacion(LocalDateTime.now());
            producto.setActivo(true);
        }
        producto.setFechaActualizacion(LocalDateTime.now());

        try {
            Producto productoGuardado = productoRepositorio.save(producto);
            logger.info("Producto guardado exitosamente con ID: {}", productoGuardado.getId());
            return productoGuardado;
        } catch (Exception e) {
            logger.error("Error al guardar producto: {}", e.getMessage());
            throw new RuntimeException("Error al guardar el producto", e);
        }
    }

    @Override
    @CacheEvict(value = "productos", allEntries = true)
    public Producto actualizar(Long id, Producto producto) {
        logger.debug("Actualizando producto ID: {} con datos: {}", id, producto.getNombre());

        validarId(id);
        validarProducto(producto);

        Producto existente = buscarPorId(id);

        // Actualizar campos
        existente.setCodigo(producto.getCodigo().trim().toUpperCase());
        existente.setNombre(producto.getNombre().trim());
        existente.setDescripcion(producto.getDescripcion());
        existente.setPrecio(producto.getPrecio());
        existente.setStock(producto.getStock());
        existente.setCategoria(producto.getCategoria());
        existente.setMarca(producto.getMarca());
        existente.setModelo(producto.getModelo());
        // CORRECCIÓN 3: Usar isActivo() en lugar de getActivo()
        existente.setActivo(producto.isActivo());
        existente.setFechaActualizacion(LocalDateTime.now());

        return guardar(existente);
    }

    @Override
    @CacheEvict(value = "productos", allEntries = true)
    public void eliminar(Long id) {
        logger.debug("Eliminando producto ID: {}", id);
        validarId(id);

        try {
            Producto producto = buscarPorId(id);

            // Eliminación lógica: marcar como inactivo
            producto.setActivo(false);
            producto.setFechaActualizacion(LocalDateTime.now());
            productoRepositorio.save(producto);

            logger.info("Producto marcado como inactivo con ID: {}", id);
        } catch (Exception e) {
            logger.error("Error al eliminar producto con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al eliminar el producto", e);
        }
    }

    // ===================== MÉTODOS DE GESTIÓN DE STOCK =====================

    @Override
    @CacheEvict(value = "productos", allEntries = true)
    public Producto actualizarStock(Long id, Integer nuevoStock) {
        logger.debug("Actualizando stock del producto ID: {} a {}", id, nuevoStock);

        if (nuevoStock == null || nuevoStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }

        Producto producto = buscarPorId(id);
        Integer stockAnterior = producto.getStock();

        producto.setStock(nuevoStock);
        producto.setFechaActualizacion(LocalDateTime.now());

        Producto actualizado = productoRepositorio.save(producto);
        logger.info("Stock del producto {} actualizado de {} a {}", id, stockAnterior, nuevoStock);

        return actualizado;
    }

    @Override
    @CacheEvict(value = "productos", allEntries = true)
    public Producto reducirStock(Long id, Integer cantidad) {
        logger.debug("Reduciendo stock del producto ID: {} en {}", id, cantidad);

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }

        Producto producto = buscarPorId(id);
        Integer stockActual = producto.getStock() != null ? producto.getStock() : 0;

        if (stockActual < cantidad) {
            logger.warn("Stock insuficiente para producto ID: {}. Stock: {}, Solicitado: {}",
                    id, stockActual, cantidad);
            throw new IllegalArgumentException(String.format(STOCK_INSUFICIENTE, stockActual, cantidad));
        }

        return actualizarStock(id, stockActual - cantidad);
    }

    @Override
    @CacheEvict(value = "productos", allEntries = true)
    public Producto aumentarStock(Long id, Integer cantidad) {
        logger.debug("Aumentando stock del producto ID: {} en {}", id, cantidad);

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }

        Producto producto = buscarPorId(id);
        Integer stockActual = producto.getStock() != null ? producto.getStock() : 0;

        return actualizarStock(id, stockActual + cantidad);
    }

    // ===================== MÉTODOS DE ESTADO =====================

    @Override
    @CacheEvict(value = "productos", allEntries = true)
    public boolean cambiarEstado(Long id, boolean activo) {
        logger.debug("Cambiando estado del producto ID: {} a activo: {}", id, activo);

        try {
            Producto producto = buscarPorId(id);
            producto.setActivo(activo);
            producto.setFechaActualizacion(LocalDateTime.now());
            productoRepositorio.save(producto);

            logger.info("Estado del producto {} cambiado a: {}", id, activo ? "activo" : "inactivo");
            return true;
        } catch (Exception e) {
            logger.error("Error al cambiar estado del producto {}: {}", id, e.getMessage());
            return false;
        }
    }

    // ===================== MÉTODOS DE VALIDACIÓN =====================

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCodigo(String codigo) {
        if (!codigoValido(codigo)) {
            return false;
        }
        return productoRepositorio.existsByCodigo(codigo.trim().toUpperCase());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCodigo(String codigo, Long excludeId) {
        if (!codigoValido(codigo) || excludeId == null) {
            return existePorCodigo(codigo);
        }
        return productoRepositorio.existsByCodigoAndIdNot(codigo.trim().toUpperCase(), excludeId);
    }

    // ===================== MÉTODOS DE BÚSQUEDA PAGINADA =====================

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> buscarPorNombreOCodigoPaginado(String search, Pageable pageable) {
        logger.debug("Buscando productos por nombre o código: '{}' con paginación", search);

        if (search == null || search.trim().isEmpty()) {
            return listarPaginados(pageable);
        }

        try {
            return productoRepositorio.buscarPorNombreOCodigo(search.trim().toUpperCase(), pageable);
        } catch (Exception e) {
            logger.error("Error al buscar productos por nombre o código: {}", e.getMessage());
            throw new RuntimeException("Error al buscar productos", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> buscarPorCategoriaPaginado(String categoria, Pageable pageable) {
        logger.debug("Buscando productos por categoría: '{}'", categoria);

        if (categoria == null || categoria.trim().isEmpty()) {
            throw new IllegalArgumentException("La categoría no puede estar vacía");
        }

        try {
            return productoRepositorio.buscarPorCategoria(categoria.trim(), pageable);
        } catch (Exception e) {
            logger.error("Error al buscar productos por categoría: {}", e.getMessage());
            throw new RuntimeException("Error al buscar productos por categoría", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> listarConStockPaginado(Pageable pageable) {
        logger.debug("Listando productos con stock disponible");

        try {
            return productoRepositorio.listarConStock(pageable);
        } catch (Exception e) {
            logger.error("Error al listar productos con stock: {}", e.getMessage());
            throw new RuntimeException("Error al listar productos con stock", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> findByCategoriaId(Long categoriaId, Pageable pageable) {
        logger.debug("Listando productos por categoría ID: {} con paginación", categoriaId);

        if (categoriaId == null) {
            throw new IllegalArgumentException("El ID de categoría no puede ser nulo");
        }

        try {
            return productoRepositorio.findByCategoriaId(categoriaId, pageable);
        } catch (Exception e) {
            logger.error("Error al listar productos por categoría: {}", e.getMessage());
            throw new RuntimeException("Error al obtener productos por categoría", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> findAllActivos(Pageable pageable) {
        logger.debug("Listando productos activos con paginación");

        try {
            return productoRepositorio.findByActivoTrue(pageable);
        } catch (Exception e) {
            logger.error("Error al listar productos activos: {}", e.getMessage());
            throw new RuntimeException("Error al obtener productos activos", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> findAllInactivos(Pageable pageable) {
        logger.debug("Listando productos inactivos con paginación");

        try {
            return productoRepositorio.findByActivoFalse(pageable);
        } catch (Exception e) {
            logger.error("Error al listar productos inactivos: {}", e.getMessage());
            throw new RuntimeException("Error al obtener productos inactivos", e);
        }
    }

    // ===================== MÉTODOS DE CONTEO =====================

    @Override
    @Transactional(readOnly = true)
    public Long contarTodos() {
        return productoRepositorio.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarActivos() {
        return productoRepositorio.countByActivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarConBajoStock(int umbral) {
        return productoRepositorio.countByStockLessThan(umbral);
    }

    // ===================== MÉTODOS ADICIONALES =====================

    @Override
    @Transactional(readOnly = true)
    public List<String> listarCategorias() {
        logger.debug("Obteniendo lista de categorías disponibles");

        try {
            return productoRepositorio.obtenerCategorias();
        } catch (Exception e) {
            logger.error("Error al obtener categorías: {}", e.getMessage());
            throw new RuntimeException("Error al obtener categorías", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> buscarProductos(String buscar, Pageable pageable) {
        return buscarPorNombreOCodigoPaginado(buscar, pageable);
    }

    // ===================== MÉTODOS DE COMPATIBILIDAD =====================

    @Override
    @Transactional(readOnly = true)
    public Producto findById(Long id) {
        return buscarPorId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> findAll(Pageable pageable) {
        return listarPaginados(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> findProductosBajoStock(int stockMinimo, Pageable pageable) {
        return productoRepositorio.findByStockLessThan(stockMinimo, pageable);
    }

    @Override
    public Producto save(Producto producto) {
        return guardar(producto);
    }

    // Método adicional requerido por la interfaz
    @Override
    @Transactional(readOnly = true)
    public Page<Producto> buscarTodosProductos(String termino, Pageable pageable) {
        return buscarPorNombreOCodigoPaginado(termino, pageable);
    }

    // ===================== MÉTODOS DE CACHE =====================

    /**
     * Limpia manualmente el cache de productos
     */
    @CacheEvict(value = "productos", allEntries = true)
    public void limpiarCacheProductos() {
        logger.info("Cache de productos limpiado manualmente");
    }

    // ===================== MÉTODOS PRIVADOS DE VALIDACIÓN =====================

    /**
     * Valida que el ID sea válido
     */
    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(ID_INVALIDO);
        }
    }

    /**
     * Valida que el producto tenga los datos requeridos
     */
    private void validarProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException(PRODUCTO_NULO);
        }

        if (!codigoValido(producto.getCodigo())) {
            throw new IllegalArgumentException(CODIGO_REQUERIDO);
        }

        if (!nombreValido(producto.getNombre())) {
            throw new IllegalArgumentException(NOMBRE_REQUERIDO);
        }

        if (producto.getPrecio() != null && producto.getPrecio() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }

        if (producto.getStock() != null && producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
    }

    /**
     * Valida que el código sea válido (no nulo ni vacío)
     */
    private boolean codigoValido(String codigo) {
        return codigo != null && !codigo.trim().isEmpty();
    }

    /**
     * Valida que el nombre sea válido (no nulo ni vacío)
     */
    private boolean nombreValido(String nombre) {
        return nombre != null && !nombre.trim().isEmpty();
    }
}