package informviva.gest.service.impl;

import informviva.gest.dto.ProductoDTO;
import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.exception.StockInsuficienteException;
import informviva.gest.model.Producto;
import informviva.gest.repository.ProductoRepositorio;
import informviva.gest.service.ProductoServicio;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de productos siguiendo principios SOLID
 *
 * - S: Responsabilidad única para gestión de productos
 * - O: Abierto para extensión, cerrado para modificación
 * - L: Cumple el contrato de ProductoServicio
 * - I: Implementa interfaz específica de productos
 * - D: Depende de abstracciones (repositorios, mapper)
 *
 * IMPORTANTE:
 * - Todos los métodos públicos trabajan con DTOs
 * - Los métodos privados manejan entidades JPA
 * - Usa ModelMapper para conversiones automáticas
 *
 * @author Sistema de Gestión Empresarial
 * @version 2.0 - Refactorizado Fase 1
 */
@Slf4j
@Service
@Transactional
public class ProductoServicioImpl extends BaseServiceImpl<Producto, Long>
        implements ProductoServicio {

    private final ProductoRepositorio productoRepositorio;
    private final ModelMapper modelMapper;

    /**
     * Constructor con inyección de dependencias
     *
     * @param productoRepositorio Repositorio de productos
     * @param modelMapper Mapper para conversión DTO/Entidad
     */
    public ProductoServicioImpl(ProductoRepositorio productoRepositorio,
                                ModelMapper modelMapper) {
        super(productoRepositorio);
        this.productoRepositorio = productoRepositorio;
        this.modelMapper = modelMapper;
    }

    // ============================================
    // OPERACIONES CRUD PRINCIPALES
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarTodos() {
        log.debug("Buscando todos los productos");

        return productoRepositorio.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoDTO> buscarTodos(Pageable pageable) {
        log.debug("Buscando productos paginados - Página: {}", pageable.getPageNumber());

        return productoRepositorio.findAll(pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDTO buscarPorId(Long id) {
        log.debug("Buscando producto por ID: {}", id);

        Producto producto = obtenerEntidadPorId(id);
        return convertirADTO(producto);
    }

    @Override
    public ProductoDTO guardar(ProductoDTO productoDTO) {
        log.debug("Guardando producto: {}", productoDTO.getNombre());

        validarProductoDTO(productoDTO);

        Producto producto = convertirAEntidad(productoDTO);
        Producto productoGuardado = guardarEntidad(producto);

        log.info("Producto guardado exitosamente con ID: {}", productoGuardado.getId());
        return convertirADTO(productoGuardado);
    }

    @Override
    public ProductoDTO actualizar(Long id, ProductoDTO productoDTO) {
        log.debug("Actualizando producto ID: {} con datos: {}", id, productoDTO.getNombre());

        Producto productoExistente = obtenerEntidadPorId(id);
        validarProductoDTO(productoDTO);

        // Mapear solo los campos actualizables
        actualizarCamposProducto(productoExistente, productoDTO);

        Producto productoActualizado = guardarEntidad(productoExistente);

        log.info("Producto actualizado exitosamente: {}", productoActualizado.getId());
        return convertirADTO(productoActualizado);
    }

    @Override
    public void eliminar(Long id) {
        log.debug("Eliminando producto ID: {}", id);

        if (!existe(id)) {
            throw new RecursoNoEncontradoException("Producto no encontrado con ID: " + id);
        }

        productoRepositorio.deleteById(id);
        log.info("Producto eliminado exitosamente: {}", id);
    }

    // ============================================
    // BÚSQUEDAS ESPECÍFICAS
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarPorNombre(String nombre) {
        log.debug("Buscando productos por nombre: {}", nombre);

        return productoRepositorio.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarPorCategoria(String categoria) {
        log.debug("Buscando productos por categoría: {}", categoria);

        return productoRepositorio.findByCategoriaIgnoreCase(categoria)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarPorRangoPrecio(BigDecimal precioMin, BigDecimal precioMax) {
        log.debug("Buscando productos por rango de precio: {} - {}", precioMin, precioMax);

        return productoRepositorio.findByPrecioBetween(precioMin, precioMax)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarConStockBajo(Integer minimoStock) {
        log.debug("Buscando productos con stock bajo (menor a {})", minimoStock);

        return productoRepositorio.findByStockLessThan(minimoStock)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarActivos() {
        log.debug("Buscando productos activos");

        return productoRepositorio.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoDTO> buscarActivos(Pageable pageable) {
        log.debug("Buscando productos activos con paginación");

        return productoRepositorio.findByActivoTrue(pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listarCategorias() {
        log.debug("Listando categorías distintas de productos");
        return productoRepositorio.findDistinctCategorias();
    }

    // ============================================
    // OPERACIONES DE STOCK
    // ============================================

    @Override
    public void actualizarStock(Long id, Integer cantidad) {
        log.debug("Actualizando stock del producto ID: {} con cantidad: {}", id, cantidad);

        Producto producto = obtenerEntidadPorId(id);

        if (cantidad < 0 && Math.abs(cantidad) > producto.getStock()) {
            throw new StockInsuficienteException(
                    "Stock insuficiente. Stock actual: " + producto.getStock() +
                            ", cantidad solicitada: " + Math.abs(cantidad));
        }

        producto.setStock(producto.getStock() + cantidad);
        guardarEntidad(producto);

        log.info("Stock actualizado para producto ID: {}. Nuevo stock: {}",
                id, producto.getStock());
    }

    // ============================================
    // OPERACIONES DE ACTIVACIÓN/DESACTIVACIÓN
    // ============================================

    @Override
    public void activar(Long id) {
        log.debug("Activando producto ID: {}", id);

        Producto producto = obtenerEntidadPorId(id);
        producto.setActivo(true);
        guardarEntidad(producto);

        log.info("Producto activado: {}", id);
    }

    @Override
    public void desactivar(Long id) {
        log.debug("Desactivando producto ID: {}", id);

        Producto producto = obtenerEntidadPorId(id);
        producto.setActivo(false);
        guardarEntidad(producto);

        log.info("Producto desactivado: {}", id);
    }

    // ============================================
    // VALIDACIONES Y VERIFICACIONES
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public boolean existe(Long id) {
        return productoRepositorio.existsById(id);
    }

    // ============================================
    // CONTADORES Y ESTADÍSTICAS
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public long contar() {
        return productoRepositorio.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long contarActivos() {
        return productoRepositorio.countByActivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public long contarConBajoStock(Integer minimoStock) {
        log.debug("Contando productos con stock bajo (menor a {})", minimoStock);
        return productoRepositorio.findByStockLessThan(minimoStock).size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getNombreEntidad() {
        return "Producto";
    }

    // ============================================
    // MÉTODOS PRIVADOS Y HELPER
    // ============================================

    /**
     * Obtiene una entidad Producto por ID (uso interno)
     *
     * @param id ID del producto
     * @return Entidad Producto
     * @throws RecursoNoEncontradoException si no existe
     */
    private Producto obtenerEntidadPorId(Long id) {
        return productoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Producto no encontrado con ID: " + id));
    }

    /**
     * Guarda una entidad Producto (uso interno)
     *
     * @param producto Entidad a guardar
     * @return Entidad guardada
     */
    private Producto guardarEntidad(Producto producto) {
        return productoRepositorio.save(producto);
    }

    /**
     * Convierte entidad Producto a ProductoDTO
     *
     * @param producto Entidad
     * @return DTO
     */
    private ProductoDTO convertirADTO(Producto producto) {
        return modelMapper.map(producto, ProductoDTO.class);
    }

    /**
     * Convierte ProductoDTO a entidad Producto
     *
     * @param dto DTO
     * @return Entidad
     */
    private Producto convertirAEntidad(ProductoDTO dto) {
        return modelMapper.map(dto, Producto.class);
    }

    /**
     * Valida los datos del ProductoDTO
     *
     * @param productoDTO DTO a validar
     * @throws IllegalArgumentException si los datos no son válidos
     */
    private void validarProductoDTO(ProductoDTO productoDTO) {
        if (productoDTO.getNombre() == null || productoDTO.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }

        if (productoDTO.getPrecio() == null || productoDTO.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero");
        }

        if (productoDTO.getStock() == null || productoDTO.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
    }

    /**
     * Actualiza los campos modificables de un producto existente
     *
     * @param productoExistente Producto a actualizar
     * @param productoDTO Datos nuevos
     */
    private void actualizarCamposProducto(Producto productoExistente, ProductoDTO productoDTO) {
        productoExistente.setNombre(productoDTO.getNombre());
        productoExistente.setDescripcion(productoDTO.getDescripcion());
        productoExistente.setPrecio(productoDTO.getPrecio());
        productoExistente.setStock(productoDTO.getStock());
        productoExistente.setCategoria(productoDTO.getCategoria());
        productoExistente.setActivo(productoDTO.getActivo());
    }
}