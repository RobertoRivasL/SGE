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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de ventas siguiendo principios SOLID
 *
 * - S: Responsabilidad única para gestión de ventas
 * - O: Abierto para extensión, cerrado para modificación
 * - L: Cumple el contrato de VentaServicio
 * - I: Implementa interfaz específica de ventas
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

    // ============================================
    // OPERACIONES CRUD PRINCIPALES
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public List<VentaDTO> buscarTodos() {
        log.debug("Buscando todas las ventas");

        return ventaRepositorio.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaDTO> buscarTodos(Pageable pageable) {
        log.debug("Buscando ventas paginadas - Página: {}", pageable.getPageNumber());

        return ventaRepositorio.findAll(pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaDTO buscarPorId(Long id) {
        log.debug("Buscando venta por ID: {}", id);

        Venta venta = obtenerEntidadPorId(id);
        return convertirADTO(venta);
    }

    @Override
    public VentaDTO guardar(VentaDTO ventaDTO) {
        log.debug("Guardando nueva venta para cliente ID: {}", ventaDTO.getClienteId());

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

        Venta ventaGuardada = guardarEntidad(venta);

        log.info("Venta guardada exitosamente con ID: {}", ventaGuardada.getId());
        return convertirADTO(ventaGuardada);
    }

    @Override
    public VentaDTO actualizar(Long id, VentaDTO ventaDTO) {
        log.debug("Actualizando venta ID: {}", id);

        Venta ventaExistente = obtenerEntidadPorId(id);
        validarVentaDTO(ventaDTO);

        // Solo actualizar ciertos campos (no productos una vez creada)
        actualizarCamposVenta(ventaExistente, ventaDTO);

        Venta ventaActualizada = guardarEntidad(ventaExistente);

        log.info("Venta actualizada exitosamente: {}", ventaActualizada.getId());
        return convertirADTO(ventaActualizada);
    }

    @Override
    public void eliminar(Long id) {
        log.debug("Eliminando venta ID: {}", id);

        if (!existe(id)) {
            throw new RecursoNoEncontradoException("Venta no encontrada con ID: " + id);
        }

        ventaRepositorio.deleteById(id);
        log.info("Venta eliminada exitosamente: {}", id);
    }

    // ============================================
    // BÚSQUEDAS ESPECÍFICAS
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public List<VentaDTO> buscarPorCliente(Long clienteId) {
        log.debug("Buscando ventas por cliente ID: {}", clienteId);

        return ventaRepositorio.findByClienteId(clienteId)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaDTO> buscarPorVendedor(Long vendedorId) {
        log.debug("Buscando ventas por vendedor ID: {}", vendedorId);

        return ventaRepositorio.findByVendedorId(vendedorId)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaDTO> buscarPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.debug("Buscando ventas por rango de fechas: {} - {}", fechaInicio, fechaFin);

        return ventaRepositorio.findByFechaBetween(fechaInicio, fechaFin)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaDTO> buscarPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable) {
        log.debug("Buscando ventas por rango de fechas paginado: {} - {}", fechaInicio, fechaFin);

        return ventaRepositorio.findByFechaBetween(fechaInicio, fechaFin, pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaDTO> buscarPorEstado(String estado) {
        log.debug("Buscando ventas por estado: {}", estado);

        return ventaRepositorio.findByEstado(estado)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // ============================================
    // OPERACIONES DE NEGOCIO
    // ============================================

    @Override
    public void anularVenta(Long id, String motivoAnulacion) {
        log.debug("Anulando venta ID: {} con motivo: {}", id, motivoAnulacion);

        Venta venta = obtenerEntidadPorId(id);

        if ("ANULADA".equals(venta.getEstado())) {
            throw new IllegalStateException("La venta ya está anulada");
        }

        // Restaurar stock de productos
        restaurarStockProductos(venta);

        // Actualizar estado de la venta
        venta.setEstado("ANULADA");
        venta.setObservaciones(venta.getObservaciones() + " | ANULADA: " + motivoAnulacion);
        venta.setFechaActualizacion(LocalDateTime.now());

        guardarEntidad(venta);

        log.info("Venta anulada exitosamente: {}", id);
    }

    // ============================================
    // VALIDACIONES Y VERIFICACIONES
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public boolean existe(Long id) {
        return ventaRepositorio.existsById(id);
    }

    // ============================================
    // CÁLCULOS Y ESTADÍSTICAS
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalVentasPorCliente(Long clienteId) {
        log.debug("Calculando total de ventas para cliente ID: {}", clienteId);

        List<Venta> ventas = ventaRepositorio.findByClienteId(clienteId);

        return ventas.stream()
                .filter(venta -> !"ANULADA".equals(venta.getEstado()))
                .map(Venta::getTotal)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public long contar() {
        return ventaRepositorio.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long contarVentasPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.debug("Contando ventas por período: {} - {}", fechaInicio, fechaFin);

        return ventaRepositorio.countByFechaBetween(fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalVentas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.debug("Calculando total de ventas por período: {} - {}", fechaInicio, fechaFin);

        List<Venta> ventas = ventaRepositorio.findByFechaBetween(fechaInicio, fechaFin);

        return ventas.stream()
                .filter(venta -> !"ANULADA".equals(venta.getEstado()))
                .map(Venta::getTotal)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarArticulosVendidos(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.debug("Contando artículos vendidos por período: {} - {}", fechaInicio, fechaFin);

        List<Venta> ventas = ventaRepositorio.findByFechaBetween(fechaInicio, fechaFin);

        return ventas.stream()
                .filter(venta -> !"ANULADA".equals(venta.getEstado()))
                .flatMap(venta -> venta.getDetalles().stream())
                .mapToLong(detalle -> detalle.getCantidad())
                .sum();
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarUnidadesVendidasPorProducto(Long productoId) {
        log.debug("Contando unidades vendidas del producto ID: {}", productoId);

        // TODO: Implementar query eficiente en repositorio
        List<Venta> ventas = ventaRepositorio.findAll();

        return ventas.stream()
                .filter(venta -> !"ANULADA".equals(venta.getEstado()))
                .flatMap(venta -> venta.getDetalles().stream())
                .filter(detalle -> detalle.getProducto().getId().equals(productoId))
                .mapToLong(detalle -> detalle.getCantidad())
                .sum();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularIngresosPorProducto(Long productoId) {
        log.debug("Calculando ingresos del producto ID: {}", productoId);

        // TODO: Implementar query eficiente en repositorio
        List<Venta> ventas = ventaRepositorio.findAll();

        return ventas.stream()
                .filter(venta -> !"ANULADA".equals(venta.getEstado()))
                .flatMap(venta -> venta.getDetalles().stream())
                .filter(detalle -> detalle.getProducto().getId().equals(productoId))
                .map(detalle -> BigDecimal.valueOf(detalle.getSubtotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaDTO> buscarVentasRecientesPorProducto(Long productoId, int limite) {
        log.debug("Buscando ventas recientes del producto ID: {}, límite: {}", productoId, limite);

        // TODO: Implementar query eficiente en repositorio
        List<Venta> ventas = ventaRepositorio.findAll();

        return ventas.stream()
                .filter(venta -> venta.getDetalles().stream()
                        .anyMatch(detalle -> detalle.getProducto().getId().equals(productoId)))
                .sorted((v1, v2) -> v2.getFecha().compareTo(v1.getFecha()))
                .limit(limite)
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existenVentasPorCliente(Long clienteId) {
        log.debug("Verificando si existen ventas para cliente ID: {}", clienteId);
        return !ventaRepositorio.findByClienteId(clienteId).isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existenVentasPorProducto(Long productoId) {
        log.debug("Verificando si existen ventas para producto ID: {}", productoId);

        // TODO: Implementar query eficiente en repositorio
        List<Venta> ventas = ventaRepositorio.findAll();

        return ventas.stream()
                .anyMatch(venta -> venta.getDetalles().stream()
                        .anyMatch(detalle -> detalle.getProducto().getId().equals(productoId)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getNombreEntidad() {
        return "Venta";
    }

    // ============================================
    // MÉTODOS PRIVADOS Y HELPER
    // ============================================

    /**
     * Obtiene una entidad Venta por ID (uso interno)
     *
     * @param id ID de la venta
     * @return Entidad Venta
     * @throws RecursoNoEncontradoException si no existe
     */
    private Venta obtenerEntidadPorId(Long id) {
        return ventaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Venta no encontrada con ID: " + id));
    }

    /**
     * Guarda una entidad Venta (uso interno)
     *
     * @param venta Entidad a guardar
     * @return Entidad guardada
     */
    private Venta guardarEntidad(Venta venta) {
        return ventaRepositorio.save(venta);
    }

    /**
     * Convierte entidad Venta a VentaDTO
     *
     * @param venta Entidad
     * @return DTO
     */
    private VentaDTO convertirADTO(Venta venta) {
        return modelMapper.map(venta, VentaDTO.class);
    }

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
            // Subtotal se calcula automáticamente en getSubtotal()

            venta.getDetalles().add(detalle);
        });
    }

    /**
     * Calcula los totales de la venta
     */
    private void calcularTotalesVenta(Venta venta) {
        double subtotal = venta.getDetalles().stream()
                .mapToDouble(VentaDetalle::getSubtotal)
                .sum();

        venta.setSubtotal(subtotal);

        // Calcular impuesto (19% IVA en Chile)
        double impuesto = subtotal * 0.19;
        venta.setImpuesto(impuesto);

        // Total = subtotal + impuesto
        venta.setTotal(subtotal + impuesto);
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

    // ============================================
    // MÉTODOS LEGACY PARA COMPATIBILIDAD CON CONTROLADORES
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public List<VentaDTO> buscarVentasParaExportar(LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                                   String estado, String metodoPago, Long vendedorId) {
        log.debug("Buscando ventas para exportar - Fechas: {} a {}, Estado: {}, Método pago: {}, Vendedor: {}",
                fechaInicio, fechaFin, estado, metodoPago, vendedorId);

        List<Venta> ventas = ventaRepositorio.findByFechaBetween(fechaInicio, fechaFin);

        return ventas.stream()
                .filter(venta -> estado == null || estado.isEmpty() || estado.equals(venta.getEstado()))
                .filter(venta -> metodoPago == null || metodoPago.isEmpty() || metodoPago.equals(venta.getMetodoPago()))
                .filter(venta -> vendedorId == null || (venta.getVendedor() != null && venta.getVendedor().getId().equals(vendedorId)))
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaDTO> buscarPorVendedorYFechas(Long vendedorId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.debug("Buscando ventas por vendedor {} y fechas: {} a {}", vendedorId, fechaInicio, fechaFin);

        List<Venta> ventas = ventaRepositorio.findByFechaBetween(fechaInicio, fechaFin);

        return ventas.stream()
                .filter(venta -> venta.getVendedor() != null && venta.getVendedor().getId().equals(vendedorId))
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaDTO> listarTodas() {
        log.debug("Listando todas las ventas (legacy)");
        return buscarTodos();
    }

    @Override
    public void anular(Long id) {
        log.debug("Anulando venta ID: {} (legacy - sin motivo específico)", id);
        anularVenta(id, "Anulada por el usuario");
    }

    @Override
    public VentaDTO duplicarVenta(VentaDTO ventaOriginalDTO) {
        log.debug("Duplicando venta (legacy)");

        // Crear nueva venta con los mismos datos pero nueva fecha
        VentaDTO ventaNuevaDTO = new VentaDTO();
        ventaNuevaDTO.setClienteId(ventaOriginalDTO.getClienteId());
        ventaNuevaDTO.setVendedorId(ventaOriginalDTO.getVendedorId());
        ventaNuevaDTO.setMetodoPago(ventaOriginalDTO.getMetodoPago());
        ventaNuevaDTO.setObservaciones("Duplicada de venta original - " +
                (ventaOriginalDTO.getObservaciones() != null ? ventaOriginalDTO.getObservaciones() : ""));
        ventaNuevaDTO.setProductos(ventaOriginalDTO.getProductos());

        return guardar(ventaNuevaDTO);
    }

    // ============================================
    // MÉTODOS ADICIONALES REQUERIDOS POR CONTROLADORES
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public long contarTransacciones(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.debug("Contando transacciones entre {} y {}", fechaInicio, fechaFin);

        return ventaRepositorio.findByFechaBetween(fechaInicio, fechaFin)
                .stream()
                .filter(venta -> "COMPLETADA".equals(venta.getEstado()))
                .count();
    }

    @Override
    @Transactional(readOnly = true)
    public Double calcularTicketPromedio(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.debug("Calculando ticket promedio entre {} y {}", fechaInicio, fechaFin);

        List<Venta> ventas = ventaRepositorio.findByFechaBetween(fechaInicio, fechaFin);

        List<Venta> ventasValidas = ventas.stream()
                .filter(venta -> !"ANULADA".equals(venta.getEstado()))
                .collect(Collectors.toList());

        if (ventasValidas.isEmpty()) {
            return 0.0;
        }

        BigDecimal total = ventasValidas.stream()
                .map(Venta::getTotal)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.divide(BigDecimal.valueOf(ventasValidas.size()), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    @Override
    @Transactional(readOnly = true)
    public Double calcularPorcentajeCambio(Double valorActual, Double valorAnterior) {
        log.debug("Calculando porcentaje de cambio: actual={}, anterior={}", valorActual, valorAnterior);

        if (valorAnterior == null || valorAnterior == 0.0) {
            return valorActual != null && valorActual > 0.0 ? 100.0 : 0.0;
        }

        if (valorActual == null) {
            return -100.0;
        }

        return ((valorActual - valorAnterior) / valorAnterior) * 100.0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaDTO> buscarPorClienteId(Long clienteId) {
        log.debug("Buscando ventas por cliente ID: {}", clienteId);

        return ventaRepositorio.findByClienteId(clienteId)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long contarVentasHoy() {
        log.debug("Contando ventas de hoy");

        Long count = ventaRepositorio.countVentasHoy();
        return count != null ? count : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public long contarVentasPorCliente(Long clienteId) {
        log.debug("Contando ventas para cliente ID: {}", clienteId);

        return ventaRepositorio.countByClienteId(clienteId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaDTO> obtenerTodasLasVentas() {
        log.debug("Obteniendo todas las ventas");

        return ventaRepositorio.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
}