package informviva.gest.service.impl;

import informviva.gest.dto.ProductoDTO;
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
 * - I: Implementa interface específica de productos
 * - D: Depende de abstracciones (repository, mapper)
 *
 * @author Tu nombre
 * @version 1.0
 */
@Slf4j
@Service
@Transactional
public class ProductoServicioImpl extends BaseServiceImpl<Producto, Long>
        implements ProductoServicio {

    private final ProductoRepositorio productoRepository;
    private final ModelMapper modelMapper;

    /**
     * Constructor con inyección de dependencias
     *
     * @param productoRepository Repositorio de productos
     * @param modelMapper Mapper para conversión DTO/Entidad
     */
    public ProductoServicioImpl(ProductoRepositorio productoRepository,
                                ModelMapper modelMapper) {
        super(productoRepository);
        this.productoRepository = productoRepository;
        this.modelMapper = modelMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarTodosDTO() {
        log.debug("Buscando todos los productos como DTO");

        return productoRepository.findAll()
                .stream()
                .map(producto -> modelMapper.map(producto, ProductoDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductoDTO> buscarTodosDTO(Pageable pageable) {
        log.debug("Buscando productos paginados como DTO - Página: {}", pageable.getPageNumber());

        return productoRepository.findAll(pageable)
                .map(producto -> modelMapper.map(producto, ProductoDTO.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ProductoDTO buscarPorIdDTO(Long id) {
        log.debug("Buscando producto por ID como DTO: {}", id);

        Producto producto = obtenerPorId(id);
        return modelMapper.map(producto, ProductoDTO.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProductoDTO guardarDTO(ProductoDTO productoDTO) {
        log.debug("Guardando producto DTO: {}", productoDTO.getNombre());

        validarProductoDTO(productoDTO);

        Producto producto = modelMapper.map(productoDTO, Producto.class);
        Producto productoGuardado = guardar(producto);

        log.info("Producto guardado exitosamente con ID: {}", productoGuardado.getId());
        return modelMapper.map(productoGuardado, ProductoDTO.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProductoDTO actualizarDTO(Long id, ProductoDTO productoDTO) {
        log.debug("Actualizando producto ID: {} con datos: {}", id, productoDTO.getNombre());

        Producto productoExistente = obtenerPorId(id);
        validarProductoDTO(productoDTO);

        // Mapear solo los campos actualizables
        actualizarCamposProducto(productoExistente, productoDTO);

        Producto productoActualizado = guardar(productoExistente);

        log.info("Producto actualizado exitosamente: {}", productoActualizado.getId());
        return modelMapper.map(productoActualizado, ProductoDTO.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarPorNombre(String nombre) {
        log.debug("Buscando productos por nombre: {}", nombre);

        return productoRepository.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(producto -> modelMapper.map(producto, ProductoDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarPorCategoria(String categoria) {
        log.debug("Buscando productos por categoría: {}", categoria);

        return productoRepository.findByCategoriaIgnoreCase(categoria)
                .stream()
                .map(producto -> modelMapper.map(producto, ProductoDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarPorRangoPrecio(BigDecimal precioMin, BigDecimal precioMax) {
        log.debug("Buscando productos por rango de precio: {} - {}", precioMin, precioMax);

        return productoRepository.findByPrecioBetween(precioMin, precioMax)
                .stream()
                .map(producto -> modelMapper.map(producto, ProductoDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actualizarStock(Long id, Integer cantidad) {
        log.debug("Actualizando stock del producto ID: {} con cantidad: {}", id, cantidad);

        Producto producto = obtenerPorId(id);

        if (cantidad < 0 && Math.abs(cantidad) > producto.getStock()) {
            throw new StockInsuficienteException(
                    "Stock insuficiente. Stock actual: " + producto.getStock() +
                            ", cantidad solicitada: " + Math.abs(cantidad));
        }

        producto.setStock(producto.getStock() + cantidad);
        guardar(producto);

        log.info("Stock actualizado para producto ID: {}. Nuevo stock: {}",
                id, producto.getStock());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarConStockBajo(Integer minimoStock) {
        log.debug("Buscando productos con stock bajo (menor a {})", minimoStock);

        return productoRepository.findByStockLessThan(minimoStock)
                .stream()
                .map(producto -> modelMapper.map(producto, ProductoDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getNombreEntidad() {
        return "Producto";
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