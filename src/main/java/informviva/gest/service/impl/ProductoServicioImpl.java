package informviva.gest.service.impl;

//**
// * @author Roberto Rivas
// * @version 2.0
// */


import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.model.Producto;
import informviva.gest.repository.ProductoRepositorio;
import informviva.gest.service.ProductoServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio para la gestión de productos
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Service
@Transactional
public class ProductoServicioImpl implements ProductoServicio {

    private static final Logger logger = LoggerFactory.getLogger(ProductoServicioImpl.class);
    private static final String PRODUCTO_NO_ENCONTRADO = "Producto no encontrado con ID: ";
    private static final String CODIGO_YA_EXISTE = "Ya existe un producto con el código: ";
    private static final String STOCK_INSUFICIENTE = "Stock insuficiente. Stock actual: %d, cantidad solicitada: %d";

    private final ProductoRepositorio productoRepositorio;

    public ProductoServicioImpl(ProductoRepositorio productoRepositorio) {
        this.productoRepositorio = productoRepositorio;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listar() {
        try {
            List<Producto> productos = productoRepositorio.findAll();
            logger.info("Se han recuperado {} productos del repositorio", productos.size());
            if (productos.isEmpty()) {
                logger.warn("La lista de productos está vacía");
            } else {
                logger.debug("Primer producto recuperado: {}", productos.get(0));
            }
            return productos;
        } catch (Exception e) {
            logger.error("Error al listar productos: {}", e.getMessage());
            throw new RuntimeException("Error al obtener la lista de productos", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> listarPaginados(Pageable pageable) {
        try {
            return productoRepositorio.findAll(pageable);
        } catch (Exception e) {
            logger.error("Error al listar productos paginados: {}", e.getMessage());
            throw new RuntimeException("Error al obtener productos paginados", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Producto buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }

        return productoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(PRODUCTO_NO_ENCONTRADO + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Producto buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código no puede estar vacío");
        }

        return productoRepositorio.findByCodigo(codigo.trim().toUpperCase());
    }

    @Override
    public Producto guardar(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }

        validarProducto(producto);

        try {
            // Si es un producto nuevo, establecer fecha de creación
            if (producto.getId() == null) {
                // Verificar que el código no exista
                if (existePorCodigo(producto.getCodigo())) {
                    throw new IllegalArgumentException(CODIGO_YA_EXISTE + producto.getCodigo());
                }

                producto.setFechaCreacion(LocalDateTime.now());
                producto.setActivo(true);
                logger.info("Creando nuevo producto con código: {}", producto.getCodigo());
            } else {
                // Si es actualización, verificar que el código no lo use otro producto
                Producto existente = buscarPorId(producto.getId());
                if (!existente.getCodigo().equals(producto.getCodigo()) && existePorCodigo(producto.getCodigo())) {
                    throw new IllegalArgumentException(CODIGO_YA_EXISTE + producto.getCodigo());
                }
                logger.info("Actualizando producto con ID: {}", producto.getId());
            }

            // Normalizar código
            producto.setCodigo(producto.getCodigo().trim().toUpperCase());
            producto.setFechaActualizacion(LocalDateTime.now());

            return productoRepositorio.save(producto);
        } catch (Exception e) {
            logger.error("Error al guardar producto: {}", e.getMessage());
            throw new RuntimeException("Error al guardar el producto", e);
        }
    }

    @Override
    public Producto actualizar(Long id, Producto producto) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }

        Producto existente = buscarPorId(id);

        // Actualizar campos
        existente.setCodigo(producto.getCodigo());
        existente.setNombre(producto.getNombre());
        existente.setDescripcion(producto.getDescripcion());
        existente.setPrecio(producto.getPrecio());
        existente.setStock(producto.getStock());
        existente.setCategoria(producto.getCategoria());
        existente.setMarca(producto.getMarca());
        existente.setModelo(producto.getModelo());
        existente.setActivo(producto.isActivo());
        existente.setFechaActualizacion(LocalDateTime.now());

        return guardar(existente);
    }

    @Override
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }

        try {
            Producto producto = buscarPorId(id);

            // En lugar de eliminar físicamente, marcar como inactivo
            producto.setActivo(false);
            producto.setFechaActualizacion(LocalDateTime.now());
            productoRepositorio.save(producto);

            logger.info("Producto marcado como inactivo con ID: {}", id);
        } catch (Exception e) {
            logger.error("Error al eliminar producto con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al eliminar el producto", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return false;
        }
        return productoRepositorio.existsByCodigo(codigo.trim().toUpperCase());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarConBajoStock(int umbral) {
        try {
            return productoRepositorio.findByStockLessThanOrderByStockAsc(umbral);
        } catch (Exception e) {
            logger.error("Error al listar productos con bajo stock: {}", e.getMessage());
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
            return productoRepositorio.findByNombreContainingIgnoreCase(nombre.trim());
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
            return productoRepositorio.findByCategoriaId(categoriaId);
        } catch (Exception e) {
            logger.error("Error al listar productos por categoría: {}", e.getMessage());
            throw new RuntimeException("Error al obtener productos por categoría", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarActivos() {
        try {
            return productoRepositorio.findByActivoTrue();
        } catch (Exception e) {
            logger.error("Error al listar productos activos: {}", e.getMessage());
            throw new RuntimeException("Error al obtener productos activos", e);
        }
    }

    @Override
    public boolean cambiarEstado(Long id, boolean activo) {
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

    @Override
    public Producto actualizarStock(Long id, Integer nuevoStock) {
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
    public Producto reducirStock(Long id, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }

        Producto producto = buscarPorId(id);
        Integer stockActual = producto.getStock() != null ? producto.getStock() : 0;

        if (stockActual < cantidad) {
            throw new IllegalArgumentException(String.format(STOCK_INSUFICIENTE, stockActual, cantidad));
        }

        return actualizarStock(id, stockActual - cantidad);
    }

    @Override
    public Producto aumentarStock(Long id, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }

        Producto producto = buscarPorId(id);
        Integer stockActual = producto.getStock() != null ? producto.getStock() : 0;

        return actualizarStock(id, stockActual + cantidad);
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarTodos() {
        try {
            return productoRepositorio.count();
        } catch (Exception e) {
            logger.error("Error al contar productos: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarActivos() {
        try {
            return productoRepositorio.countByActivoTrue();
        } catch (Exception e) {
            logger.error("Error al contar productos activos: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarConBajoStock(int umbral) {
        try {
            return productoRepositorio.countByStockLessThan(umbral);
        } catch (Exception e) {
            logger.error("Error al contar productos con bajo stock: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * Valida los datos del producto antes de guardarlo
     */
    private void validarProducto(Producto producto) {
        if (producto.getCodigo() == null || producto.getCodigo().trim().isEmpty()) {
            throw new IllegalArgumentException("El código del producto es obligatorio");
        }

        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }

        if (producto.getPrecio() == null || producto.getPrecio() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero");
        }

        if (producto.getStock() == null || producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
    }


    @Override
    public List<String> listarCategorias() {
        return productoRepositorio.obtenerCategorias();
    }

    @Override
    public boolean existePorCodigo(String codigo, Long id) {
        if (id == null) {
            return productoRepositorio.existsByCodigo(codigo);
        }
        return productoRepositorio.existsByCodigoAndIdNot(codigo, id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> buscarPorNombreOCodigoPaginado(String termino, Pageable pageable) {
        if (termino == null || termino.trim().isEmpty()) {
            throw new IllegalArgumentException("El término de búsqueda no puede estar vacío");
        }

        try {
            return productoRepositorio.buscarPorNombreOCodigo(termino.trim().toUpperCase(), pageable);
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
            return productoRepositorio.buscarPorCategoria(categoria.trim(), pageable);
        } catch (Exception e) {
            logger.error("Error al buscar productos por categoría: {}", e.getMessage());
            throw new RuntimeException("Error al buscar productos por categoría", e);
        }
    }

    // ProductoServicioImpl.java
    @Override
    public Producto save(Producto producto) {
        return productoRepositorio.save(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> listarConStockPaginado(Pageable pageable) {
        try {
            return productoRepositorio.listarConStock(pageable);
        } catch (Exception e) {
            logger.error("Error al listar productos con stock: {}", e.getMessage());
            throw new RuntimeException("Error al listar productos con stock", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> buscarPorNombre(String nombre, Pageable pageable) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }

        try {
            return productoRepositorio.findByNombreContainingIgnoreCase(nombre.trim(), pageable);
        } catch (Exception e) {
            logger.error("Error al buscar productos por nombre: {}", e.getMessage());
            throw new RuntimeException("Error al buscar productos por nombre", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> buscarTodosProductos(String termino, Pageable pageable) {
        if (termino == null || termino.trim().isEmpty()) {
            throw new IllegalArgumentException("El término de búsqueda no puede estar vacío");
        }
        return productoRepositorio.findByNombreContainingIgnoreCase(termino.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Producto findById(Long id) {
        return productoRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> findProductosBajoStock(int stockMinimo, Pageable pageable) {
        return productoRepositorio.findByStockLessThan(stockMinimo, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> findAll(Pageable pageable) {
        try {
            return productoRepositorio.findAll(pageable);
        } catch (Exception e) {
            logger.error("Error al listar todos los productos: {}", e.getMessage());
            throw new RuntimeException("Error al listar todos los productos", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> buscarProductos(String termino, Pageable pageable) {
        if (termino == null || termino.trim().isEmpty()) {
            throw new IllegalArgumentException("El término de búsqueda no puede estar vacío");
        }

        try {
            return productoRepositorio.buscarPorNombreOCodigo(termino.trim().toUpperCase(), pageable);
        } catch (Exception e) {
            logger.error("Error al buscar productos: {}", e.getMessage());
            throw new RuntimeException("Error al buscar productos", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> findAllInactivos(Pageable pageable) {
        try {
            return productoRepositorio.findByActivoFalse(pageable);
        } catch (Exception e) {
            logger.error("Error al listar productos inactivos: {}", e.getMessage());
            throw new RuntimeException("Error al listar productos inactivos", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> findAllActivos(Pageable pageable) {
        try {
            return productoRepositorio.findByActivoTrue(pageable);
        } catch (Exception e) {
            logger.error("Error al listar productos activos: {}", e.getMessage());
            throw new RuntimeException("Error al listar productos activos", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> findByCategoriaId(Long categoriaId, Pageable pageable) {
        if (categoriaId == null) {
            throw new IllegalArgumentException("El ID de la categoría no puede ser nulo");
        }

        try {
            return productoRepositorio.findByCategoriaId(categoriaId, pageable);
        } catch (Exception e) {
            logger.error("Error al buscar productos por categoría ID: {}", e.getMessage());
            throw new RuntimeException("Error al buscar productos por categoría", e);
        }
    }
}
