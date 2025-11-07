package informviva.gest.service.impl;

import informviva.gest.dto.EstadisticasInventarioDTO;
import informviva.gest.dto.MovimientoInventarioDTO;
import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.exception.StockInsuficienteException;
import informviva.gest.model.MovimientoInventario;
import informviva.gest.model.MovimientoInventario.TipoMovimiento;
import informviva.gest.model.Producto;
import informviva.gest.model.Usuario;
import informviva.gest.repository.MovimientoInventarioRepositorio;
import informviva.gest.repository.ProductoRepositorio;
import informviva.gest.repository.RepositorioUsuario;
import informviva.gest.service.InventarioServicio;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de inventario siguiendo principios SOLID
 *
 * Principios aplicados:
 * - S: Responsabilidad única para gestión de inventario y movimientos
 * - O: Abierto para extensión (nuevos tipos de movimientos)
 * - L: Cumple el contrato de InventarioServicio
 * - I: Implementa interfaz específica de inventario
 * - D: Depende de abstracciones (repositorios, mapper)
 *
 * IMPORTANTE:
 * - Todos los métodos públicos trabajan con DTOs
 * - Los métodos privados manejan entidades JPA
 * - Usa ModelMapper para conversiones automáticas
 * - Validaciones estrictas de stock antes de movimientos de salida
 * - Actualización automática de stock en productos
 * - Transacciones para garantizar integridad de datos
 *
 * @author Roberto Rivas
 * @version 1.0
 * @since FASE 2 - Módulo de Inventario
 */
@Slf4j
@Service
@Transactional
public class InventarioServicioImpl extends BaseServiceImpl<MovimientoInventario, Long>
        implements InventarioServicio {

    private final MovimientoInventarioRepositorio movimientoInventarioRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final RepositorioUsuario repositorioUsuario;
    private final ModelMapper modelMapper;

    /**
     * Constructor con inyección de dependencias
     * NO usa @Autowired en campos, sino inyección por constructor
     */
    public InventarioServicioImpl(MovimientoInventarioRepositorio movimientoInventarioRepositorio,
                                   ProductoRepositorio productoRepositorio,
                                   RepositorioUsuario repositorioUsuario,
                                   ModelMapper modelMapper) {
        super(movimientoInventarioRepositorio);
        this.movimientoInventarioRepositorio = movimientoInventarioRepositorio;
        this.productoRepositorio = productoRepositorio;
        this.repositorioUsuario = repositorioUsuario;
        this.modelMapper = modelMapper;
    }

    // ============================================
    // REGISTRO DE MOVIMIENTOS
    // ============================================

    @Override
    public MovimientoInventarioDTO registrarMovimiento(MovimientoInventarioDTO movimientoDTO) {
        log.debug("Registrando movimiento de inventario - Producto ID: {}, Tipo: {}, Cantidad: {}",
                movimientoDTO.getProductoId(), movimientoDTO.getTipo(), movimientoDTO.getCantidad());

        // Validar datos básicos
        validarMovimientoDTO(movimientoDTO);

        // Obtener entidades relacionadas
        Producto producto = obtenerProducto(movimientoDTO.getProductoId());
        Usuario usuario = obtenerUsuario(movimientoDTO.getUsuarioId());

        // Validar que el movimiento no cause stock negativo
        if (!validarMovimiento(producto.getId(), movimientoDTO.getTipo(), movimientoDTO.getCantidad())) {
            String mensaje = obtenerMensajeValidacion(producto.getId(), movimientoDTO.getTipo(),
                    movimientoDTO.getCantidad());
            throw new StockInsuficienteException(mensaje);
        }

        // Obtener stock anterior
        Integer stockAnterior = producto.getStock() != null ? producto.getStock() : 0;

        // Crear movimiento
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setTipo(movimientoDTO.getTipo());
        movimiento.setCantidad(movimientoDTO.getCantidad());
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setUsuario(usuario);
        movimiento.setMotivo(movimientoDTO.getMotivo());
        movimiento.setReferenciaExterna(movimientoDTO.getReferenciaExterna());
        movimiento.setObservaciones(movimientoDTO.getObservaciones());
        movimiento.setFecha(LocalDateTime.now());

        // Calcular costo
        if (movimientoDTO.getCostoUnitario() != null) {
            movimiento.setCostoUnitario(movimientoDTO.getCostoUnitario());
        } else if (producto.getPrecio() != null) {
            movimiento.setCostoUnitario(producto.getPrecio());
        }

        // Calcular stock nuevo
        Integer stockNuevo = calcularStockNuevo(stockAnterior, movimientoDTO.getCantidad(),
                movimientoDTO.getTipo());
        movimiento.setStockNuevo(stockNuevo);

        // Actualizar stock del producto
        actualizarStockProducto(producto.getId(), movimientoDTO.getTipo(), movimientoDTO.getCantidad());

        // Guardar movimiento
        MovimientoInventario movimientoGuardado = movimientoInventarioRepositorio.save(movimiento);

        log.info("Movimiento registrado exitosamente - ID: {}, Producto: {}, Stock: {} -> {}",
                movimientoGuardado.getId(), producto.getNombre(), stockAnterior, stockNuevo);

        return convertirADTO(movimientoGuardado);
    }

    @Override
    public MovimientoInventarioDTO registrarVenta(Long productoId, Integer cantidad,
                                                   Long ventaId, Long usuarioId) {
        log.debug("Registrando venta - Producto ID: {}, Cantidad: {}, Venta ID: {}",
                productoId, cantidad, ventaId);

        MovimientoInventarioDTO movimiento = new MovimientoInventarioDTO();
        movimiento.setProductoId(productoId);
        movimiento.setTipo(TipoMovimiento.VENTA);
        movimiento.setCantidad(cantidad);
        movimiento.setUsuarioId(usuarioId);
        movimiento.setReferenciaExterna("VENTA-" + ventaId);
        movimiento.setMotivo("Venta de producto");

        return registrarMovimiento(movimiento);
    }

    @Override
    public MovimientoInventarioDTO registrarCompra(Long productoId, Integer cantidad,
                                                    Long compraId, Double costoUnitario,
                                                    Long usuarioId) {
        log.debug("Registrando compra - Producto ID: {}, Cantidad: {}, Compra ID: {}, Costo: {}",
                productoId, cantidad, compraId, costoUnitario);

        MovimientoInventarioDTO movimiento = new MovimientoInventarioDTO();
        movimiento.setProductoId(productoId);
        movimiento.setTipo(TipoMovimiento.COMPRA);
        movimiento.setCantidad(cantidad);
        movimiento.setUsuarioId(usuarioId);
        movimiento.setReferenciaExterna("COMPRA-" + compraId);
        movimiento.setMotivo("Compra a proveedor");
        movimiento.setCostoUnitario(costoUnitario);

        return registrarMovimiento(movimiento);
    }

    @Override
    public MovimientoInventarioDTO registrarAjuste(Long productoId, Integer cantidad,
                                                    String motivo, Long usuarioId) {
        log.debug("Registrando ajuste - Producto ID: {}, Cantidad: {}, Motivo: {}",
                productoId, cantidad, motivo);

        // Determinar tipo de ajuste según el signo de la cantidad
        TipoMovimiento tipo = cantidad >= 0 ? TipoMovimiento.AJUSTE_POSITIVO : TipoMovimiento.AJUSTE_NEGATIVO;
        Integer cantidadAbsoluta = Math.abs(cantidad);

        MovimientoInventarioDTO movimiento = new MovimientoInventarioDTO();
        movimiento.setProductoId(productoId);
        movimiento.setTipo(tipo);
        movimiento.setCantidad(cantidadAbsoluta);
        movimiento.setUsuarioId(usuarioId);
        movimiento.setMotivo(motivo);

        return registrarMovimiento(movimiento);
    }

    @Override
    public MovimientoInventarioDTO registrarDevolucionEntrada(Long productoId, Integer cantidad,
                                                               Long ventaId, String motivo,
                                                               Long usuarioId) {
        log.debug("Registrando devolución entrada - Producto ID: {}, Cantidad: {}, Venta ID: {}",
                productoId, cantidad, ventaId);

        MovimientoInventarioDTO movimiento = new MovimientoInventarioDTO();
        movimiento.setProductoId(productoId);
        movimiento.setTipo(TipoMovimiento.DEVOLUCION_ENTRADA);
        movimiento.setCantidad(cantidad);
        movimiento.setUsuarioId(usuarioId);
        movimiento.setReferenciaExterna("VENTA-" + ventaId);
        movimiento.setMotivo(motivo);

        return registrarMovimiento(movimiento);
    }

    // ============================================
    // BÚSQUEDAS
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public MovimientoInventarioDTO buscarPorId(Long id) {
        log.debug("Buscando movimiento por ID: {}", id);

        return movimientoInventarioRepositorio.findById(id)
                .map(this::convertirADTO)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovimientoInventarioDTO> buscarTodos(Pageable pageable) {
        log.debug("Buscando todos los movimientos - Página: {}", pageable.getPageNumber());

        return movimientoInventarioRepositorio.findAll(pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovimientoInventarioDTO> buscarPorProducto(Long productoId, Pageable pageable) {
        log.debug("Buscando movimientos por producto ID: {}", productoId);

        return movimientoInventarioRepositorio.findByProductoId(productoId, pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovimientoInventarioDTO> buscarPorTipo(TipoMovimiento tipo, Pageable pageable) {
        log.debug("Buscando movimientos por tipo: {}", tipo);

        return movimientoInventarioRepositorio.findByTipo(tipo, pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovimientoInventarioDTO> buscarPorFechas(LocalDateTime fechaInicio,
                                                          LocalDateTime fechaFin,
                                                          Pageable pageable) {
        log.debug("Buscando movimientos por fechas: {} - {}", fechaInicio, fechaFin);

        return movimientoInventarioRepositorio.findByFechaBetween(fechaInicio, fechaFin, pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovimientoInventarioDTO> buscarConCriterios(Long productoId, TipoMovimiento tipo,
                                                             LocalDateTime fechaInicio,
                                                             LocalDateTime fechaFin,
                                                             Long usuarioId, Pageable pageable) {
        log.debug("Buscando movimientos con criterios - Producto: {}, Tipo: {}, Usuario: {}",
                productoId, tipo, usuarioId);

        return movimientoInventarioRepositorio.buscarConCriterios(
                productoId, tipo, fechaInicio, fechaFin, usuarioId, pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventarioDTO> obtenerUltimosMovimientos(Long productoId, int limite) {
        log.debug("Obteniendo últimos {} movimientos del producto ID: {}", limite, productoId);

        Pageable pageable = PageRequest.of(0, limite);
        List<MovimientoInventario> movimientos =
                movimientoInventarioRepositorio.findUltimosMovimientosPorProducto(productoId, pageable);

        return movimientos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // ============================================
    // ESTADÍSTICAS
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public EstadisticasInventarioDTO obtenerEstadisticas() {
        log.debug("Obteniendo estadísticas de inventario");

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicioMes = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime finMes = YearMonth.now().atEndOfMonth().atTime(23, 59, 59);
        LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();

        EstadisticasInventarioDTO estadisticas = new EstadisticasInventarioDTO(ahora);
        estadisticas.setPeriodoAnalizado("Mes actual: " + YearMonth.now());

        // Estadísticas de productos
        estadisticas.setTotalProductos(productoRepositorio.count());
        estadisticas.setProductosActivos(productoRepositorio.countByActivoTrue());
        estadisticas.setProductosInactivos(estadisticas.getTotalProductos() - estadisticas.getProductosActivos());

        // Stock total y valor
        List<Producto> todosProductos = productoRepositorio.findAll();
        long totalUnidades = todosProductos.stream()
                .filter(p -> p.getStock() != null)
                .mapToLong(Producto::getStock)
                .sum();
        estadisticas.setTotalUnidadesStock(totalUnidades);

        BigDecimal valorTotal = todosProductos.stream()
                .filter(p -> p.getStock() != null && p.getPrecio() != null)
                .map(p -> BigDecimal.valueOf(p.getPrecio() * p.getStock()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        estadisticas.setValorTotalInventario(valorTotal);

        // Alertas de stock
        List<Producto> productosStockBajo = todosProductos.stream()
                .filter(p -> p.getStock() != null && p.getStockMinimo() != null)
                .filter(p -> p.getStock() < p.getStockMinimo())
                .collect(Collectors.toList());
        estadisticas.setProductosStockBajo((long) productosStockBajo.size());

        long productosSinStock = todosProductos.stream()
                .filter(p -> p.getStock() == null || p.getStock() == 0)
                .count();
        estadisticas.setProductosSinStock(productosSinStock);
        estadisticas.setProductosStockCritico(productosSinStock);

        // Lista de productos con stock bajo
        List<EstadisticasInventarioDTO.ProductoStockDTO> listaStockBajo = productosStockBajo.stream()
                .map(p -> new EstadisticasInventarioDTO.ProductoStockDTO(
                        p.getId(),
                        p.getCodigo(),
                        p.getNombre(),
                        p.getStock(),
                        p.getStockMinimo()
                ))
                .collect(Collectors.toList());
        estadisticas.setProductosConStockBajo(listaStockBajo);

        // Movimientos
        estadisticas.setTotalMovimientos(movimientoInventarioRepositorio.count());
        estadisticas.setMovimientosHoy(movimientoInventarioRepositorio.countByFechaBetween(inicioHoy, ahora));
        estadisticas.setMovimientosMes(movimientoInventarioRepositorio.countByFechaBetween(inicioMes, finMes));

        // Contar entradas y salidas del mes
        long totalEntradas = movimientoInventarioRepositorio.countByTipo(TipoMovimiento.COMPRA) +
                movimientoInventarioRepositorio.countByTipo(TipoMovimiento.DEVOLUCION_ENTRADA) +
                movimientoInventarioRepositorio.countByTipo(TipoMovimiento.AJUSTE_POSITIVO);
        long totalSalidas = movimientoInventarioRepositorio.countByTipo(TipoMovimiento.VENTA) +
                movimientoInventarioRepositorio.countByTipo(TipoMovimiento.DEVOLUCION_SALIDA) +
                movimientoInventarioRepositorio.countByTipo(TipoMovimiento.AJUSTE_NEGATIVO);
        estadisticas.setTotalEntradasMes(totalEntradas);
        estadisticas.setTotalSalidasMes(totalSalidas);

        // Unidades entradas y salidas del mes
        List<MovimientoInventario> movimientosMes = movimientoInventarioRepositorio
                .findByFechaBetween(inicioMes, finMes, Pageable.unpaged())
                .getContent();

        long unidadesEntradas = movimientosMes.stream()
                .filter(m -> m.getTipo().esEntrada())
                .mapToLong(MovimientoInventario::getCantidad)
                .sum();
        long unidadesSalidas = movimientosMes.stream()
                .filter(m -> m.getTipo().esSalida())
                .mapToLong(MovimientoInventario::getCantidad)
                .sum();
        estadisticas.setUnidadesEntradasMes(unidadesEntradas);
        estadisticas.setUnidadesSalidasMes(unidadesSalidas);

        // Costos
        Double costoEntradas = movimientoInventarioRepositorio.calcularCostoTotalEntradas(inicioMes, finMes);
        Double costoSalidas = movimientoInventarioRepositorio.calcularCostoTotalSalidas(inicioMes, finMes);
        estadisticas.setCostoEntradasMes(costoEntradas != null ? BigDecimal.valueOf(costoEntradas) : BigDecimal.ZERO);
        estadisticas.setCostoSalidasMes(costoSalidas != null ? BigDecimal.valueOf(costoSalidas) : BigDecimal.ZERO);

        // Calcular métricas derivadas
        estadisticas.calcularMetricasDerivadas();

        log.info("Estadísticas generadas - Total productos: {}, Stock total: {}, Valor: {}",
                estadisticas.getTotalProductos(), estadisticas.getTotalUnidadesStock(),
                estadisticas.getValorTotalInventario());

        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public EstadisticasInventarioDTO obtenerEstadisticasPeriodo(LocalDateTime fechaInicio,
                                                                  LocalDateTime fechaFin) {
        log.debug("Obteniendo estadísticas del período: {} - {}", fechaInicio, fechaFin);

        EstadisticasInventarioDTO estadisticas = new EstadisticasInventarioDTO(LocalDateTime.now());
        estadisticas.setPeriodoAnalizado("Período: " + fechaInicio.toLocalDate() + " a " + fechaFin.toLocalDate());

        // Movimientos del período
        long movimientosPeriodo = movimientoInventarioRepositorio.countByFechaBetween(fechaInicio, fechaFin);
        estadisticas.setTotalMovimientos(movimientosPeriodo);

        // Entradas y salidas
        List<MovimientoInventario> movimientos = movimientoInventarioRepositorio
                .findByFechaBetween(fechaInicio, fechaFin, Pageable.unpaged())
                .getContent();

        long unidadesEntradas = movimientos.stream()
                .filter(m -> m.getTipo().esEntrada())
                .mapToLong(MovimientoInventario::getCantidad)
                .sum();
        long unidadesSalidas = movimientos.stream()
                .filter(m -> m.getTipo().esSalida())
                .mapToLong(MovimientoInventario::getCantidad)
                .sum();

        estadisticas.setUnidadesEntradasMes(unidadesEntradas);
        estadisticas.setUnidadesSalidasMes(unidadesSalidas);

        // Costos
        Double costoEntradas = movimientoInventarioRepositorio.calcularCostoTotalEntradas(fechaInicio, fechaFin);
        Double costoSalidas = movimientoInventarioRepositorio.calcularCostoTotalSalidas(fechaInicio, fechaFin);
        estadisticas.setCostoEntradasMes(costoEntradas != null ? BigDecimal.valueOf(costoEntradas) : BigDecimal.ZERO);
        estadisticas.setCostoSalidasMes(costoSalidas != null ? BigDecimal.valueOf(costoSalidas) : BigDecimal.ZERO);

        // Calcular métricas derivadas
        estadisticas.calcularMetricasDerivadas();

        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public Long calcularTotalEntradas(Long productoId, LocalDateTime fechaInicio,
                                       LocalDateTime fechaFin) {
        log.debug("Calculando total de entradas - Producto ID: {}, Período: {} - {}",
                productoId, fechaInicio, fechaFin);

        return movimientoInventarioRepositorio.calcularTotalEntradas(productoId, fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public Long calcularTotalSalidas(Long productoId, LocalDateTime fechaInicio,
                                      LocalDateTime fechaFin) {
        log.debug("Calculando total de salidas - Producto ID: {}, Período: {} - {}",
                productoId, fechaInicio, fechaFin);

        return movimientoInventarioRepositorio.calcularTotalSalidas(productoId, fechaInicio, fechaFin);
    }

    // ============================================
    // ALERTAS Y VERIFICACIONES
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public List<EstadisticasInventarioDTO.ProductoStockDTO> obtenerProductosStockBajo() {
        log.debug("Obteniendo productos con stock bajo");

        List<Producto> productos = productoRepositorio.findAll();

        return productos.stream()
                .filter(p -> p.getStock() != null && p.getStockMinimo() != null)
                .filter(p -> p.getStock() < p.getStockMinimo())
                .map(p -> new EstadisticasInventarioDTO.ProductoStockDTO(
                        p.getId(),
                        p.getCodigo(),
                        p.getNombre(),
                        p.getStock(),
                        p.getStockMinimo()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstadisticasInventarioDTO.ProductoStockDTO> obtenerProductosSinStock() {
        log.debug("Obteniendo productos sin stock");

        List<Producto> productos = productoRepositorio.findByStock(0);

        return productos.stream()
                .map(p -> new EstadisticasInventarioDTO.ProductoStockDTO(
                        p.getId(),
                        p.getCodigo(),
                        p.getNombre(),
                        p.getStock(),
                        p.getStockMinimo()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verificarStockDisponible(Long productoId, Integer cantidad) {
        log.debug("Verificando stock disponible - Producto ID: {}, Cantidad: {}", productoId, cantidad);

        Producto producto = obtenerProducto(productoId);
        Integer stockActual = producto.getStock() != null ? producto.getStock() : 0;

        return stockActual >= cantidad;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer obtenerStockActual(Long productoId) {
        log.debug("Obteniendo stock actual - Producto ID: {}", productoId);

        Producto producto = obtenerProducto(productoId);
        return producto.getStock() != null ? producto.getStock() : 0;
    }

    // ============================================
    // VALIDACIONES
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public boolean validarMovimiento(Long productoId, TipoMovimiento tipo, Integer cantidad) {
        log.debug("Validando movimiento - Producto ID: {}, Tipo: {}, Cantidad: {}",
                productoId, tipo, cantidad);

        // Si es entrada, siempre es válido
        if (tipo.esEntrada()) {
            return true;
        }

        // Si es salida, verificar stock disponible
        Producto producto = obtenerProducto(productoId);
        Integer stockActual = producto.getStock() != null ? producto.getStock() : 0;

        return stockActual >= cantidad;
    }

    @Override
    @Transactional(readOnly = true)
    public String obtenerMensajeValidacion(Long productoId, TipoMovimiento tipo, Integer cantidad) {
        if (validarMovimiento(productoId, tipo, cantidad)) {
            return null;
        }

        Producto producto = obtenerProducto(productoId);
        Integer stockActual = producto.getStock() != null ? producto.getStock() : 0;

        return String.format(
                "Stock insuficiente para el producto '%s' (ID: %d). " +
                        "Stock actual: %d, cantidad solicitada: %d, déficit: %d",
                producto.getNombre(),
                productoId,
                stockActual,
                cantidad,
                cantidad - stockActual
        );
    }

    // ============================================
    // UTILIDADES
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public long contarMovimientos() {
        return movimientoInventarioRepositorio.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long contarMovimientosProducto(Long productoId) {
        log.debug("Contando movimientos del producto ID: {}", productoId);
        return movimientoInventarioRepositorio.countByProductoId(productoId);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarMovimientosPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.debug("Contando movimientos del período: {} - {}", fechaInicio, fechaFin);
        return movimientoInventarioRepositorio.countByFechaBetween(fechaInicio, fechaFin);
    }

    // ============================================
    // MÉTODO ABSTRACTO DE BaseServiceImpl
    // ============================================

    @Override
    protected String getNombreEntidad() {
        return "MovimientoInventario";
    }

    // ============================================
    // MÉTODOS PRIVADOS Y HELPER
    // ============================================

    /**
     * Convierte una entidad MovimientoInventario a DTO
     *
     * @param movimiento Entidad a convertir
     * @return DTO del movimiento
     */
    private MovimientoInventarioDTO convertirADTO(MovimientoInventario movimiento) {
        MovimientoInventarioDTO dto = modelMapper.map(movimiento, MovimientoInventarioDTO.class);

        // Establecer campos adicionales que ModelMapper no mapea automáticamente
        if (movimiento.getProducto() != null) {
            dto.setProductoId(movimiento.getProducto().getId());
            dto.setProductoNombre(movimiento.getProducto().getNombre());
            dto.setProductoCodigo(movimiento.getProducto().getCodigo());
        }

        if (movimiento.getUsuario() != null) {
            dto.setUsuarioId(movimiento.getUsuario().getId());
            dto.setUsuarioNombre(movimiento.getUsuario().getNombre());
        }

        // Calcular valores derivados
        dto.calcularValoresDerivados();

        return dto;
    }

    /**
     * Convierte un DTO a entidad MovimientoInventario
     *
     * @param dto DTO a convertir
     * @return Entidad del movimiento
     */
    private MovimientoInventario convertirAEntidad(MovimientoInventarioDTO dto) {
        MovimientoInventario movimiento = modelMapper.map(dto, MovimientoInventario.class);

        // Establecer relaciones que no se mapean automáticamente
        if (dto.getProductoId() != null) {
            Producto producto = obtenerProducto(dto.getProductoId());
            movimiento.setProducto(producto);
        }

        if (dto.getUsuarioId() != null) {
            Usuario usuario = obtenerUsuario(dto.getUsuarioId());
            movimiento.setUsuario(usuario);
        }

        return movimiento;
    }

    /**
     * Obtiene un producto por ID y lanza excepción si no existe
     *
     * @param productoId ID del producto
     * @return Producto encontrado
     * @throws RecursoNoEncontradoException si el producto no existe
     */
    private Producto obtenerProducto(Long productoId) {
        return productoRepositorio.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Producto no encontrado con ID: " + productoId));
    }

    /**
     * Obtiene un usuario por ID y lanza excepción si no existe
     *
     * @param usuarioId ID del usuario
     * @return Usuario encontrado
     * @throws RecursoNoEncontradoException si el usuario no existe
     */
    private Usuario obtenerUsuario(Long usuarioId) {
        return repositorioUsuario.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario no encontrado con ID: " + usuarioId));
    }

    /**
     * Actualiza el stock de un producto según el tipo de movimiento
     *
     * @param productoId ID del producto
     * @param tipo Tipo de movimiento
     * @param cantidad Cantidad del movimiento
     */
    private void actualizarStockProducto(Long productoId, TipoMovimiento tipo, Integer cantidad) {
        Producto producto = obtenerProducto(productoId);

        Integer stockActual = producto.getStock() != null ? producto.getStock() : 0;
        Integer nuevoStock;

        if (tipo.esEntrada()) {
            nuevoStock = stockActual + cantidad;
        } else {
            nuevoStock = stockActual - cantidad;
        }

        // Validar que no sea negativo
        if (nuevoStock < 0) {
            throw new StockInsuficienteException(
                    String.format("El stock resultante sería negativo (%d) para el producto '%s'",
                            nuevoStock, producto.getNombre())
            );
        }

        producto.setStock(nuevoStock);
        productoRepositorio.save(producto);

        log.debug("Stock actualizado - Producto ID: {}, Stock anterior: {}, Stock nuevo: {}",
                productoId, stockActual, nuevoStock);
    }

    /**
     * Calcula el stock nuevo según el tipo de movimiento
     *
     * @param stockAnterior Stock actual
     * @param cantidad Cantidad del movimiento
     * @param tipo Tipo de movimiento
     * @return Stock calculado
     */
    private Integer calcularStockNuevo(Integer stockAnterior, Integer cantidad, TipoMovimiento tipo) {
        if (stockAnterior == null) {
            stockAnterior = 0;
        }

        if (tipo.esEntrada()) {
            return stockAnterior + cantidad;
        } else {
            return stockAnterior - cantidad;
        }
    }

    /**
     * Valida los datos básicos de un MovimientoInventarioDTO
     *
     * @param movimientoDTO DTO a validar
     * @throws IllegalArgumentException si los datos son inválidos
     */
    private void validarMovimientoDTO(MovimientoInventarioDTO movimientoDTO) {
        if (movimientoDTO.getProductoId() == null) {
            throw new IllegalArgumentException("El ID del producto es obligatorio");
        }

        if (movimientoDTO.getTipo() == null) {
            throw new IllegalArgumentException("El tipo de movimiento es obligatorio");
        }

        if (movimientoDTO.getCantidad() == null || movimientoDTO.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }

        if (movimientoDTO.getUsuarioId() == null) {
            throw new IllegalArgumentException("El ID del usuario es obligatorio");
        }
    }
}
