package informviva.gest.service.impl;

import informviva.gest.dto.VentaDTO;
import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.exception.StockInsuficienteException;
import informviva.gest.model.*;
import informviva.gest.repository.*;
import informviva.gest.service.VentaServicio;
import informviva.gest.service.CacheServicio;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación refactorizada del servicio de ventas con Cache
 * Aplica principios SOLID, manejo robusto de errores y cache inteligente
 * ✅ CORREGIDO: Usando RepositorioUsuario (nombre correcto)
 * ✅ ACTUALIZADO: Con implementación completa de cache para optimizar rendimiento
 *
 * @author Roberto Rivas
 * @version 5.0 - ACTUALIZADO con Cache y optimizaciones de rendimiento
 */
@Service
@RequiredArgsConstructor
@Transactional
@CacheConfig(cacheNames = {"ventas-cache"})
public class VentaServicioImpl implements VentaServicio {

    private static final Logger logger = LoggerFactory.getLogger(VentaServicioImpl.class);

    // ==================== INYECCIÓN DE DEPENDENCIAS ====================
    private final VentaRepositorio ventaRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final RepositorioUsuario repositorioUsuario;
    private final VentaDetalleRepositorio ventaDetalleRepositorio;
    private final CacheServicio cacheServicio; // ✅ NUEVO: Servicio de cache

    // ==================== MÉTODOS PRINCIPALES ====================

    @Override
    @CacheEvict(value = {"ventas-recientes", "ventas-por-cliente", "estadisticas-ventas", "productos-mas-vendidos"}, allEntries = true)
    public Venta crearNuevaVenta(VentaDTO ventaDTO) {
        logger.debug("Creando nueva venta para cliente ID: {}", ventaDTO.getClienteId());

        try {
            validarDatosVenta(ventaDTO);

            Cliente cliente = buscarClienteSeguro(ventaDTO.getClienteId());
            Usuario vendedor = null;

            if (ventaDTO.getVendedorId() != null) {
                vendedor = buscarUsuarioSeguro(ventaDTO.getVendedorId());
            }

            Venta nuevaVenta = construirVentaDesdeDTO(ventaDTO, cliente, vendedor);
            procesarDetallesVenta(nuevaVenta, ventaDTO);

            Venta ventaGuardada = ventaRepositorio.save(nuevaVenta);
            logger.info("Venta creada exitosamente con ID: {}", ventaGuardada.getId());

            // Limpiar cache después de crear nueva venta
            cacheServicio.limpiarCacheVentasAlModificar();

            return ventaGuardada;

        } catch (Exception e) {
            logger.error("Error creando venta: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear la venta: " + e.getMessage(), e);
        }
    }

    @Override
    @Cacheable(value = "ventas-recientes", key = "'todas_' + #root.methodName")
    public List<Venta> obtenerTodasLasVentas() {
        try {
            return ventaRepositorio.findAllByOrderByFechaDesc();
        } catch (Exception e) {
            logger.error("Error obteniendo todas las ventas: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    @Cacheable(value = "ventas-recientes", key = "'venta_' + #id")
    public Venta obtenerVentaPorId(Long id) {
        return buscarPorId(id).orElse(null);
    }

    // ==================== MÉTODOS DE BÚSQUEDA ====================

    @Override
    @Cacheable(value = "estadisticas-ventas",
            key = "#fechaInicio.toString() + '_' + #fechaFin.toString() + '_rango'")
    public List<Venta> buscarPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        try {
            validarRangoFechas(fechaInicio, fechaFin);
            return ventaRepositorio.findByFechaBetween(fechaInicio, fechaFin);
        } catch (Exception e) {
            logger.error("Error buscando ventas por rango de fechas: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public Page<Venta> buscarPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable) {
        try {
            validarRangoFechas(fechaInicio, fechaFin);
            return ventaRepositorio.findByFechaBetween(fechaInicio, fechaFin, pageable);
        } catch (Exception e) {
            logger.error("Error buscando ventas paginadas: {}", e.getMessage());
            return Page.empty(pageable);
        }
    }

    @Override
    @Cacheable(value = "ventas-por-cliente", key = "#clienteId")
    public List<Venta> buscarPorCliente(Long clienteId) {
        try {
            if (clienteId == null || clienteId <= 0) {
                return Collections.emptyList();
            }
            return ventaRepositorio.findByClienteId(clienteId);
        } catch (Exception e) {
            logger.error("Error buscando ventas por cliente ID {}: {}", clienteId, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Venta> buscarPorCliente(Cliente cliente) {
        if (cliente != null && cliente.getId() != null) {
            return buscarPorCliente(cliente.getId());
        }
        return Collections.emptyList();
    }

    @Override
    @Cacheable(value = "ventas-por-vendedor",
            key = "#vendedorId + '_' + #fechaInicio.toString() + '_' + #fechaFin.toString()")
    public List<Venta> buscarPorVendedorYFechas(Long vendedorId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        try {
            if (vendedorId == null || vendedorId <= 0) {
                return Collections.emptyList();
            }
            validarRangoFechas(fechaInicio, fechaFin);
            return ventaRepositorio.findByVendedorIdAndFechaBetween(vendedorId, fechaInicio, fechaFin);
        } catch (Exception e) {
            logger.error("Error buscando ventas por vendedor: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Venta> buscarVentasParaExportar(LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                                String estado, String metodoPago, String vendedor) {
        List<Venta> ventas = buscarPorRangoFechas(fechaInicio, fechaFin);

        return ventas.stream()
                .filter(venta -> filtrarPorEstado(venta, estado))
                .filter(venta -> filtrarPorMetodoPago(venta, metodoPago))
                .filter(venta -> filtrarPorVendedor(venta, vendedor))
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "productos-mas-vendidos",
            key = "#productoId + '_recientes_' + #limite")
    public List<Venta> buscarVentasRecientesPorProducto(Long productoId, int limite) {
        try {
            if (productoId == null || productoId <= 0 || limite <= 0) {
                return Collections.emptyList();
            }
            return ventaRepositorio.findVentasRecentesByProductoId(productoId)
                    .stream()
                    .limit(limite)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error buscando ventas recientes por producto: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    @Cacheable(value = "ventas-por-cliente",
            key = "#clienteId + '_recientes_' + #limite")
    public List<Venta> buscarVentasRecientesPorCliente(Long clienteId, int limite) {
        try {
            if (clienteId == null || clienteId <= 0 || limite <= 0) {
                return Collections.emptyList();
            }
            return ventaRepositorio.findByClienteIdOrderByFechaDesc(clienteId)
                    .stream()
                    .limit(limite)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error buscando ventas recientes por cliente: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // ==================== MÉTODOS CRUD ====================

    @Override
    @Cacheable(value = "ventas-recientes", key = "'buscar_' + #id")
    public Optional<Venta> buscarPorId(Long id) {
        try {
            if (id == null || id <= 0) {
                return Optional.empty();
            }
            return ventaRepositorio.findById(id);
        } catch (Exception e) {
            logger.error("Error buscando venta por ID {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<Venta> listarTodas() {
        return obtenerTodasLasVentas();
    }

    @Override
    @CacheEvict(value = {"ventas-recientes", "ventas-por-cliente", "estadisticas-ventas"}, allEntries = true)
    public Venta guardar(Venta venta) {
        try {
            validarVentaParaGuardar(venta);
            if (venta.getFecha() == null) {
                venta.setFecha(LocalDateTime.now());
            }
            Venta ventaGuardada = ventaRepositorio.save(venta);

            // Limpiar cache después de guardar
            cacheServicio.limpiarCacheVentasAlModificar();

            return ventaGuardada;
        } catch (Exception e) {
            logger.error("Error guardando venta: {}", e.getMessage());
            throw new RuntimeException("Error al guardar la venta", e);
        }
    }

    @Override
    @CacheEvict(value = {"ventas-recientes", "ventas-por-cliente", "estadisticas-ventas"}, allEntries = true)
    public Venta guardar(VentaDTO ventaDTO) {
        try {
            Venta ventaGuardada = crearNuevaVenta(ventaDTO);
            // Notificar al servicio de cache para limpieza
            cacheServicio.limpiarCacheVentasAlModificar();
            return ventaGuardada;
        } catch (Exception e) {
            logger.error("Error guardando venta: {}", e.getMessage());
            throw new RuntimeException("Error al guardar la venta", e);
        }
    }

    @Override
    @CacheEvict(value = {"ventas-recientes", "ventas-por-cliente", "estadisticas-ventas"}, allEntries = true)
    public Venta actualizar(Venta venta) {
        try {
            validarVentaParaActualizar(venta);
            Venta ventaActualizada = ventaRepositorio.save(venta);

            // Limpiar cache después de actualizar
            cacheServicio.limpiarCacheVentasAlModificar();

            return ventaActualizada;
        } catch (Exception e) {
            logger.error("Error actualizando venta: {}", e.getMessage());
            throw new RuntimeException("Error al actualizar la venta", e);
        }
    }

    @Override
    @CacheEvict(value = {"ventas-recientes", "ventas-por-cliente", "estadisticas-ventas"}, allEntries = true)
    public Venta actualizar(Long id, VentaDTO ventaDTO) {
        try {
            Venta ventaExistente = buscarVentaPorIdSeguro(id);
            actualizarCamposVenta(ventaExistente, ventaDTO);
            Venta ventaActualizada = ventaRepositorio.save(ventaExistente);

            // Limpiar cache después de actualizar
            cacheServicio.limpiarCacheVentasAlModificar();

            return ventaActualizada;
        } catch (Exception e) {
            logger.error("Error actualizando venta con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al actualizar la venta", e);
        }
    }

    @Override
    @CacheEvict(value = {"ventas-recientes", "ventas-por-cliente", "estadisticas-ventas"}, allEntries = true)
    public void eliminar(Long id) {
        try {
            Venta venta = buscarVentaPorIdSeguro(id);
            ventaRepositorio.delete(venta);
            logger.info("Venta eliminada con ID: {}", id);

            // Limpiar cache después de eliminar
            cacheServicio.limpiarCacheVentasAlModificar();

        } catch (Exception e) {
            logger.error("Error eliminando venta con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al eliminar la venta", e);
        }
    }

    @Override
    @CacheEvict(value = {"ventas-recientes", "ventas-por-cliente", "estadisticas-ventas"}, allEntries = true)
    public Venta anular(Long id) {
        try {
            Venta venta = buscarVentaPorIdSeguro(id);
            if ("ANULADA".equals(venta.getEstadoAsString())) {
                throw new IllegalStateException("La venta ya está anulada");
            }

            venta.setEstado("ANULADA");
            restaurarStockProductos(venta);

            Venta ventaAnulada = ventaRepositorio.save(venta);
            logger.info("Venta anulada con ID: {}", id);

            // Limpiar cache después de anular
            cacheServicio.limpiarCacheVentasAlModificar();

            return ventaAnulada;

        } catch (Exception e) {
            logger.error("Error anulando venta con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al anular la venta", e);
        }
    }

    @Override
    @CacheEvict(value = {"ventas-recientes", "ventas-por-cliente", "estadisticas-ventas"}, allEntries = true)
    public Venta duplicarVenta(Venta ventaOriginal) {
        try {
            if (ventaOriginal == null) {
                throw new IllegalArgumentException("La venta original no puede ser nula");
            }

            Venta nuevaVenta = new Venta();
            copiarDatosVenta(ventaOriginal, nuevaVenta);
            nuevaVenta.setEstado("PENDIENTE");
            nuevaVenta.setFecha(LocalDateTime.now());

            Venta ventaDuplicada = ventaRepositorio.save(nuevaVenta);

            // Limpiar cache después de duplicar
            cacheServicio.limpiarCacheVentasAlModificar();

            return ventaDuplicada;

        } catch (Exception e) {
            logger.error("Error duplicando venta: {}", e.getMessage());
            throw new RuntimeException("Error al duplicar la venta", e);
        }
    }

    // ==================== MÉTODOS DE CONVERSIÓN ====================

    @Override
    public VentaDTO convertirADTO(Venta venta) {
        if (venta == null) {
            return null;
        }

        try {
            VentaDTO dto = new VentaDTO();

            dto.setId(venta.getId());
            dto.setFecha(venta.getFecha());
            dto.setClienteId(venta.getCliente() != null ? venta.getCliente().getId() : null);
            dto.setVendedorId(venta.getVendedor() != null ? venta.getVendedor().getId() : null);

            dto.setSubtotal(venta.getSubtotal() != null ? venta.getSubtotal() : 0.0);
            dto.setImpuesto(venta.getImpuesto() != null ? venta.getImpuesto() : 0.0);
            dto.setTotal(venta.getTotal() != null ? venta.getTotal() : 0.0);

            dto.setMetodoPago(venta.getMetodoPagoAsString());
            dto.setEstado(venta.getEstadoAsString());
            dto.setObservaciones(venta.getObservaciones());

            // Convertir detalles si existen
            if (venta.getDetalles() != null && !venta.getDetalles().isEmpty()) {
                List<VentaDTO.ProductoVentaDTO> productosDTO = convertirDetallesADTO(venta.getDetalles());
                dto.setProductos(productosDTO);
            }

            return dto;

        } catch (Exception e) {
            logger.error("Error convirtiendo venta a DTO: {}", e.getMessage());
            return null;
        }
    }

    // ==================== MÉTODOS DE CÁLCULO Y ESTADÍSTICAS ====================

    @Override
    @Cacheable(value = "estadisticas-ventas",
            key = "#fechaInicio.toString() + '_' + #fechaFin.toString() + '_count'")
    public Long contarTransacciones(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        try {
            validarRangoFechas(fechaInicio, fechaFin);
            return ventaRepositorio.countByFechaBetween(fechaInicio, fechaFin);
        } catch (Exception e) {
            logger.error("Error contando transacciones: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    @Cacheable(value = "ventas-por-cliente", key = "#clienteId + '_count'")
    public Long contarVentasPorCliente(Long clienteId) {
        try {
            if (clienteId == null || clienteId <= 0) {
                return 0L;
            }
            return ventaRepositorio.countByClienteId(clienteId);
        } catch (Exception e) {
            logger.error("Error contando ventas por cliente: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    @Cacheable(value = "estadisticas-ventas", key = "'ventas_hoy'")
    public Long contarVentasHoy() {
        LocalDateTime inicioHoy = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime finHoy = inicioHoy.plusDays(1).minusNanos(1);
        return contarTransacciones(inicioHoy, finHoy);
    }

    @Override
    @Cacheable(value = "ventas-por-cliente", key = "#clienteId + '_total'")
    public Double calcularTotalVentasPorCliente(Long clienteId) {
        try {
            if (clienteId == null || clienteId <= 0) {
                return 0.0;
            }
            Double total = ventaRepositorio.sumTotalByClienteId(clienteId);
            return total != null ? total : 0.0;
        } catch (Exception e) {
            logger.error("Error calculando total de ventas por cliente: {}", e.getMessage());
            return 0.0;
        }
    }

    @Override
    @Cacheable(value = "estadisticas-ventas",
            key = "#inicio.toString() + '_' + #fin.toString() + '_total'")
    public Double calcularTotalVentas(LocalDateTime inicio, LocalDateTime fin) {
        try {
            validarRangoFechas(inicio, fin);
            Double total = ventaRepositorio.sumTotalByFechaBetween(inicio, fin);
            return total != null ? total : 0.0;
        } catch (Exception e) {
            logger.error("Error calculando total de ventas: {}", e.getMessage());
            return 0.0;
        }
    }

    @Override
    public Double calcularPorcentajeCambio(Double actual, Double anterior) {
        if (anterior == null || anterior == 0) {
            return actual != null && actual > 0 ? 100.0 : 0.0;
        }
        if (actual == null) {
            return -100.0;
        }
        return ((actual - anterior) / anterior) * 100;
    }

    @Override
    @Cacheable(value = "estadisticas-ventas",
            key = "#inicio.toString() + '_' + #fin.toString() + '_ticket_promedio'")
    public Double calcularTicketPromedio(LocalDateTime inicio, LocalDateTime fin) {
        try {
            Double totalVentas = calcularTotalVentas(inicio, fin);
            Long totalTransacciones = contarTransacciones(inicio, fin);

            if (totalTransacciones > 0) {
                return totalVentas / totalTransacciones;
            }
            return 0.0;
        } catch (Exception e) {
            logger.error("Error calculando ticket promedio: {}", e.getMessage());
            return 0.0;
        }
    }

    // ==================== MÉTODOS DE VALIDACIÓN ====================

    @Override
    @Cacheable(value = "validaciones-venta", key = "#clienteId + '_exists'")
    public boolean existenVentasPorCliente(Long clienteId) {
        try {
            if (clienteId == null || clienteId <= 0) {
                return false;
            }
            return ventaRepositorio.existsByClienteId(clienteId);
        } catch (Exception e) {
            logger.error("Error verificando ventas por cliente: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Cacheable(value = "validaciones-venta", key = "#productoId + '_exists'")
    public boolean existenVentasPorProducto(Long productoId) {
        try {
            if (productoId == null || productoId <= 0) {
                return false;
            }
            return ventaDetalleRepositorio.existsByProductoId(productoId);
        } catch (Exception e) {
            logger.error("Error verificando ventas por producto: {}", e.getMessage());
            return false;
        }
    }

    // ==================== MÉTODOS DE ARTÍCULOS Y PRODUCTOS ====================

    @Override
    @Cacheable(value = "estadisticas-ventas",
            key = "#fechaInicio.toString() + '_' + #fechaFin.toString() + '_articulos_vendidos'")
    public Long contarArticulosVendidos(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        try {
            validarRangoFechas(fechaInicio, fechaFin);
            Long total = ventaDetalleRepositorio.sumCantidadByFechaBetween(fechaInicio, fechaFin);
            return total != null ? total : 0L;
        } catch (Exception e) {
            logger.error("Error contando artículos vendidos: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    @Cacheable(value = "productos-mas-vendidos", key = "#productoId + '_unidades'")
    public Long contarUnidadesVendidasPorProducto(Long productoId) {
        try {
            if (productoId == null || productoId <= 0) {
                return 0L;
            }
            Long total = ventaDetalleRepositorio.sumCantidadByProductoId(productoId);
            return total != null ? total : 0L;
        } catch (Exception e) {
            logger.error("Error contando unidades vendidas por producto: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    @Cacheable(value = "productos-mas-vendidos", key = "#productoId + '_ingresos'")
    public Double calcularIngresosPorProducto(Long productoId) {
        try {
            if (productoId == null || productoId <= 0) {
                return 0.0;
            }
            Double ingresos = ventaDetalleRepositorio.sumIngresosByProductoId(productoId);
            return ingresos != null ? ingresos : 0.0;
        } catch (Exception e) {
            logger.error("Error calculando ingresos por producto: {}", e.getMessage());
            return 0.0;
        }
    }

    // ==================== MÉTODOS PRIVADOS DE APOYO ====================

    private void validarDatosVenta(VentaDTO ventaDTO) {
        if (ventaDTO == null) {
            throw new IllegalArgumentException("Los datos de la venta no pueden ser nulos");
        }
        if (ventaDTO.getClienteId() == null) {
            throw new IllegalArgumentException("Debe especificar un cliente para la venta");
        }
        if (ventaDTO.getProductos() == null || ventaDTO.getProductos().isEmpty()) {
            throw new IllegalArgumentException("La venta debe incluir al menos un producto");
        }
    }

    private void validarRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin no pueden ser nulas");
        }
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
    }

    private Cliente buscarClienteSeguro(Long clienteId) {
        return clienteRepositorio.findById(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con ID: " + clienteId));
    }

    private Usuario buscarUsuarioSeguro(Long usuarioId) {
        return repositorioUsuario.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + usuarioId));
    }

    private Venta buscarVentaPorIdSeguro(Long id) {
        return ventaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada con ID: " + id));
    }

    private Venta construirVentaDesdeDTO(VentaDTO dto, Cliente cliente, Usuario vendedor) {
        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setVendedor(vendedor);
        venta.setFecha(dto.getFecha() != null ? dto.getFecha() : LocalDateTime.now());
        venta.setMetodoPago(dto.getMetodoPago());
        venta.setObservaciones(dto.getObservaciones());
        venta.setEstado(dto.getEstado() != null ? dto.getEstado() : "PENDIENTE");
        return venta;
    }

    private void procesarDetallesVenta(Venta venta, VentaDTO dto) {
        if (dto.getProductos() != null && !dto.getProductos().isEmpty()) {
            List<VentaDetalle> detalles = new ArrayList<>();
            double subtotal = 0.0;

            for (VentaDTO.ProductoVentaDTO productoDTO : dto.getProductos()) {
                VentaDetalle detalle = crearDetalleVenta(venta, productoDTO);
                detalles.add(detalle);
                subtotal += detalle.getCantidad() * detalle.getPrecioUnitario();
            }

            venta.setDetalles(detalles);
            venta.setSubtotal(subtotal);
            venta.setImpuesto(subtotal * 0.19); // 19% IVA
            venta.setTotal(subtotal + venta.getImpuesto());
        }
    }

    private VentaDetalle crearDetalleVenta(Venta venta, VentaDTO.ProductoVentaDTO productoDTO) {
        Producto producto = productoRepositorio.findById(productoDTO.getProductoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));

        // Verificar stock
        if (producto.getStock() < productoDTO.getCantidad()) {
            throw new StockInsuficienteException(
                    producto.getId(),
                    producto.getNombre(),
                    producto.getStock(),
                    productoDTO.getCantidad()
            );
        }

        // Actualizar stock
        producto.setStock(producto.getStock() - productoDTO.getCantidad());
        productoRepositorio.save(producto);

        // Crear detalle
        VentaDetalle detalle = new VentaDetalle();
        detalle.setVenta(venta);
        detalle.setProducto(producto);
        detalle.setCantidad(productoDTO.getCantidad());
        detalle.setPrecioUnitario(productoDTO.getPrecioUnitario() != null ?
                productoDTO.getPrecioUnitario() : producto.getPrecio());

        return detalle;
    }

    private void validarVentaParaGuardar(Venta venta) {
        if (venta == null) {
            throw new IllegalArgumentException("La venta no puede ser nula");
        }
        if (venta.getCliente() == null) {
            throw new IllegalArgumentException("La venta debe tener un cliente");
        }
    }

    private void validarVentaParaActualizar(Venta venta) {
        validarVentaParaGuardar(venta);
        if (venta.getId() == null) {
            throw new IllegalArgumentException("La venta debe tener un ID para actualizar");
        }
    }

    private void actualizarCamposVenta(Venta venta, VentaDTO dto) {
        if (dto.getSubtotal() != null) venta.setSubtotal(dto.getSubtotal());
        if (dto.getImpuesto() != null) venta.setImpuesto(dto.getImpuesto());
        if (dto.getTotal() != null) venta.setTotal(dto.getTotal());
        if (dto.getMetodoPago() != null) venta.setMetodoPago(dto.getMetodoPago());
        if (dto.getEstado() != null) venta.setEstado(dto.getEstado());
        if (dto.getObservaciones() != null) venta.setObservaciones(dto.getObservaciones());
    }

    private void restaurarStockProductos(Venta venta) {
        if (venta.getDetalles() != null) {
            for (VentaDetalle detalle : venta.getDetalles()) {
                Producto producto = detalle.getProducto();
                producto.setStock(producto.getStock() + detalle.getCantidad());
                productoRepositorio.save(producto);
            }
        }
    }

    private void copiarDatosVenta(Venta origen, Venta destino) {
        destino.setCliente(origen.getCliente());
        destino.setVendedor(origen.getVendedor());
        destino.setSubtotal(origen.getSubtotal());
        destino.setImpuesto(origen.getImpuesto());
        destino.setTotal(origen.getTotal());
        destino.setMetodoPago(origen.getMetodoPagoAsString());
        destino.setObservaciones(origen.getObservaciones());
    }

    private List<VentaDTO.ProductoVentaDTO> convertirDetallesADTO(List<VentaDetalle> detalles) {
        return detalles.stream()
                .map(detalle -> {
                    VentaDTO.ProductoVentaDTO dto = new VentaDTO.ProductoVentaDTO();
                    dto.setProductoId(detalle.getProducto().getId());
                    dto.setCantidad(detalle.getCantidad());
                    dto.setPrecioUnitario(detalle.getPrecioUnitario());
                    dto.setProductoNombre(detalle.getProducto().getNombre());
                    dto.setProductoCodigo(detalle.getProducto().getCodigo());
                    dto.setSubtotal(detalle.getCantidad() * detalle.getPrecioUnitario());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private boolean filtrarPorEstado(Venta venta, String estado) {
        return estado == null || estado.isEmpty() || estado.equals(venta.getEstadoAsString());
    }

    private boolean filtrarPorMetodoPago(Venta venta, String metodoPago) {
        return metodoPago == null || metodoPago.isEmpty() || metodoPago.equals(venta.getMetodoPagoAsString());
    }

    private boolean filtrarPorVendedor(Venta venta, String vendedor) {
        return vendedor == null || vendedor.isEmpty() ||
                (venta.getVendedor() != null && vendedor.equals(venta.getVendedor().getNombreCompleto()));
    }
}