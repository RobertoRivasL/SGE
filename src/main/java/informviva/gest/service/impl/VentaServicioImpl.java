package informviva.gest.service.impl;

import informviva.gest.dto.VentaDTO;
import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.exception.StockInsuficienteException;
import informviva.gest.model.*;
import informviva.gest.repository.*;
import informviva.gest.service.VentaServicio;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de ventas siguiendo principios SOLID
 *
 * - S: Responsabilidad única para gestión de ventas
 * - O: Abierto para extensión, cerrado para modificación
 * - L: Cumple el contrato de VentaServicio
 * - I: Implementa interface específica de ventas
 * - D: Depende de abstracciones (repositorios, mapper)
 *
 * @author Tu nombre
 * @version 1.0
 */
@Slf4j
@Service
@Transactional
public class VentaServicioImpl extends BaseServiceImpl<Venta, Long>
        implements VentaServicio {

    private final VentaRepositorio ventaRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final RepositorioUsuario repositorioUsuario;
    private final ModelMapper modelMapper;

    /**
     * Constructor con inyección de dependencias
     */
    public VentaServicioImpl(VentaRepositorio ventaRepositorio,
                             ClienteRepositorio clienteRepositorio,
                             ProductoRepositorio productoRepositorio,
                             RepositorioUsuario repositorioUsuario,
                             ModelMapper modelMapper) {
        super(ventaRepositorio);
        this.ventaRepositorio = ventaRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.productoRepositorio = productoRepositorio;
        this.repositorioUsuario = repositorioUsuario;
        this.modelMapper = modelMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<VentaDTO> buscarTodosDTO() {
        log.debug("Buscando todas las ventas como DTO");

        return ventaRepositorio.findAll()
                .stream()
                .map(venta -> modelMapper.map(venta, VentaDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<VentaDTO> buscarTodosDTO(Pageable pageable) {
        log.debug("Buscando ventas paginadas como DTO - Página: {}", pageable.getPageNumber());

        return ventaRepositorio.findAll(pageable)
                .map(venta -> modelMapper.map(venta, VentaDTO.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public VentaDTO buscarPorIdDTO(Long id) {
        log.debug("Buscando venta por ID como DTO: {}", id);

        Venta venta = obtenerPorId(id);
        return modelMapper.map(venta, VentaDTO.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VentaDTO crearVenta(VentaDTO ventaDTO) {
        log.debug("Creando nueva venta para cliente ID: {}", ventaDTO.getClienteId());

        validarVentaDTO(ventaDTO);

        // Obtener entidades relacionadas
        Cliente cliente = obtenerClientePorId(ventaDTO.getClienteId());
        Usuario vendedor = null;

        if (ventaDTO.getVendedorId() != null) {
            vendedor = obtenerUsuarioPorId(ventaDTO.getVendedorId());
        }

        // Crear la venta
        Venta venta = construirVentaDesdeDTO(ventaDTO, cliente, vendedor);

        // Procesar productos y actualizar stock
        procesarProductosVenta(venta, ventaDTO);

        // Calcular totales
        calcularTotalesVenta(venta);

        Venta ventaGuardada = guardar(venta);

        log.info("Venta creada exitosamente con ID: {}", ventaGuardada.getId());
        return modelMapper.map(ventaGuardada, VentaDTO.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VentaDTO actualizarVenta(Long id, VentaDTO ventaDTO) {
        log.debug("Actualizando venta ID: {}", id);

        Venta ventaExistente = obtenerPorId(id);
        validarVentaDTO(ventaDTO);

        // Solo actualizar ciertos campos (no productos una vez creada)
        actualizarCamposVenta(ventaExistente, ventaDTO);

        Venta ventaActualizada = guardar(ventaExistente);

        log.info("Venta actualizada exitosamente: {}", ventaActualizada.getId());
        return modelMapper.map(ventaActualizada, VentaDTO.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<VentaDTO> buscarPorCliente(Long clienteId) {
        log.debug("Buscando ventas por cliente ID: {}", clienteId);

        return ventaRepositorio.findByClienteId(clienteId)
                .stream()
                .map(venta -> modelMapper.map(venta, VentaDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<VentaDTO> buscarPorVendedor(Long vendedorId) {
        log.debug("Buscando ventas por vendedor ID: {}", vendedorId);

        return ventaRepositorio.findByVendedorId(vendedorId)
                .stream()
                .map(venta -> modelMapper.map(venta, VentaDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<VentaDTO> buscarPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.debug("Buscando ventas por rango de fechas: {} - {}", fechaInicio, fechaFin);

        return ventaRepositorio.findByFechaBetween(fechaInicio, fechaFin)
                .stream()
                .map(venta -> modelMapper.map(venta, VentaDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<VentaDTO> buscarPorEstado(String estado) {
        log.debug("Buscando ventas por estado: {}", estado);

        return ventaRepositorio.findByEstado(estado)
                .stream()
                .map(venta -> modelMapper.map(venta, VentaDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void anularVenta(Long id, String motivoAnulacion) {
        log.debug("Anulando venta ID: {} con motivo: {}", id, motivoAnulacion);

        Venta venta = obtenerPorId(id);

        if ("ANULADA".equals(venta.getEstado())) {
            throw new IllegalStateException("La venta ya está anulada");
        }

        // Restaurar stock de productos
        restaurarStockProductos(venta);

        // Actualizar estado de la venta
        venta.setEstado("ANULADA");
        venta.setObservaciones(venta.getObservaciones() + " | ANULADA: " + motivoAnulacion);
        venta.setFechaActualizacion(LocalDateTime.now());

        guardar(venta);

        log.info("Venta anulada exitosamente: {}", id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalVentasPorCliente(Long clienteId) {
        log.debug("Calculando total de ventas para cliente ID: {}", clienteId);

        List<Venta> ventas = ventaRepositorio.findByClienteId(clienteId);

        return ventas.stream()
                .filter(venta -> !"ANULADA".equals(venta.getEstado()))
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public long contarVentasPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.debug("Contando ventas por período: {} - {}", fechaInicio, fechaFin);

        return ventaRepositorio.countByFechaBetween(fechaInicio, fechaFin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getNombreEntidad() {
        return "Venta";
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Valida los datos del VentaDTO
     */
    private void validarVentaDTO(VentaDTO ventaDTO) {
        if (ventaDTO.getClienteId() == null) {
            throw new IllegalArgumentException("El cliente es obligatorio para la venta");
        }

        if (ventaDTO.getProductos() == null || ventaDTO.getProductos().isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un producto");
        }

        // Validar que todos los productos tengan cantidad positiva
        ventaDTO.getProductos().forEach(detalle -> {
            if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
            }

            if (detalle.getProductoId() == null) {
                throw new IllegalArgumentException("El ID del producto es obligatorio");
            }
        });
    }

    /**
     * Obtiene un cliente por ID y lanza excepción si no existe
     */
    private Cliente obtenerClientePorId(Long clienteId) {
        return clienteRepositorio.findById(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cliente no encontrado con ID: " + clienteId));
    }

    /**
     * Obtiene un usuario por ID y lanza excepción si no existe
     */
    private Usuario obtenerUsuarioPorId(Long usuarioId) {
        return repositorioUsuario.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario no encontrado con ID: " + usuarioId));
    }

    /**
     * Construye una entidad Venta desde un DTO
     */
    private Venta construirVentaDesdeDTO(VentaDTO ventaDTO, Cliente cliente, Usuario vendedor) {
        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setVendedor(vendedor);
        venta.setFecha(LocalDateTime.now());
        venta.setEstado("PENDIENTE");
        venta.setMetodoPago(ventaDTO.getMetodoPago());
        venta.setObservaciones(ventaDTO.getObservaciones());
        venta.setFechaCreacion(LocalDateTime.now());
        venta.setFechaActualizacion(LocalDateTime.now());

        return venta;
    }

    /**
     * Procesa los productos de la venta y actualiza el stock
     */
    private void procesarProductosVenta(Venta venta, VentaDTO ventaDTO) {
        ventaDTO.getProductos().forEach(detalleDTO -> {
            Producto producto = productoRepositorio.findById(detalleDTO.getProductoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Producto no encontrado con ID: " + detalleDTO.getProductoId()));

            // Verificar stock suficiente
            if (producto.getStock() < detalleDTO.getCantidad()) {
                throw new StockInsuficienteException(
                        "Stock insuficiente para el producto: " + producto.getNombre() +
                                ". Stock disponible: " + producto.getStock() +
                                ", cantidad solicitada: " + detalleDTO.getCantidad());
            }

            // Actualizar stock
            producto.setStock(producto.getStock() - detalleDTO.getCantidad());
            productoRepositorio.save(producto);

            // Crear detalle de venta
            VentaDetalle detalle = new VentaDetalle();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(detalleDTO.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(detalleDTO.getCantidad())));

            venta.getDetalles().add(detalle);
        });
    }

    /**
     * Calcula los totales de la venta
     */
    private void calcularTotalesVenta(Venta venta) {
        BigDecimal subtotal = venta.getDetalles().stream()
                .map(VentaDetalle::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        venta.setSubtotal(subtotal);

        // Calcular impuestos (19% IVA en Chile)
        BigDecimal impuestos = subtotal.multiply(new BigDecimal("0.19"));
        venta.setImpuestos(impuestos);

        // Total = subtotal + impuestos
        venta.setTotal(subtotal.add(impuestos));
    }

    /**
     * Actualiza los campos modificables de una venta
     */
    private void actualizarCamposVenta(Venta ventaExistente, VentaDTO ventaDTO) {
        ventaExistente.setEstado(ventaDTO.getEstado());
        ventaExistente.setMetodoPago(ventaDTO.getMetodoPago());
        ventaExistente.setObservaciones(ventaDTO.getObservaciones());
        ventaExistente.setFechaActualizacion(LocalDateTime.now());
    }

    /**
     * Restaura el stock de productos cuando se anula una venta
     */
    private void restaurarStockProductos(Venta venta) {
        venta.getDetalles().forEach(detalle -> {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoRepositorio.save(producto);
        });
    }
}