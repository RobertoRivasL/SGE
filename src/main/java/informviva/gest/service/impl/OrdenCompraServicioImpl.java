package informviva.gest.service.impl;

import informviva.gest.dto.DetalleOrdenCompraDTO;
import informviva.gest.dto.EstadisticasComprasDTO;
import informviva.gest.dto.OrdenCompraDTO;
import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.model.*;
import informviva.gest.model.OrdenCompra.EstadoOrden;
import informviva.gest.repository.*;
import informviva.gest.service.InventarioServicio;
import informviva.gest.service.OrdenCompraServicio;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de órdenes de compra siguiendo principios SOLID
 *
 * - S: Responsabilidad única para gestión de órdenes de compra
 * - O: Abierto para extensión, cerrado para modificación
 * - L: Cumple el contrato de OrdenCompraServicio
 * - I: Implementa interfaz específica de órdenes
 * - D: Depende de abstracciones (repositorios, servicios, mapper)
 *
 * IMPORTANTE:
 * - Todos los métodos públicos trabajan con DTOs
 * - Los métodos privados manejan entidades JPA
 * - Usa ModelMapper para conversiones automáticas
 * - Integra con InventarioServicio para actualizar stock automáticamente
 *
 * @author Roberto Rivas
 * @version 1.0
 * @since FASE 3 - Módulo de Compras
 */
@Slf4j
@Service
@Transactional
public class OrdenCompraServicioImpl 
        implements OrdenCompraServicio {

    private final OrdenCompraRepositorio ordenCompraRepositorio;
    private final DetalleOrdenCompraRepositorio detalleOrdenCompraRepositorio;
    private final ProveedorRepositorio proveedorRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final RepositorioUsuario usuarioRepositorio;
    private final InventarioServicio inventarioServicio;
    private final ModelMapper modelMapper;

    /**
     * Constructor con inyección de dependencias
     */
    public OrdenCompraServicioImpl(OrdenCompraRepositorio ordenCompraRepositorio,
                                   DetalleOrdenCompraRepositorio detalleOrdenCompraRepositorio,
                                   ProveedorRepositorio proveedorRepositorio,
                                   ProductoRepositorio productoRepositorio,
                                   RepositorioUsuario usuarioRepositorio,
                                   InventarioServicio inventarioServicio,
                                   ModelMapper modelMapper) {
        this.ordenCompraRepositorio = ordenCompraRepositorio;
        this.detalleOrdenCompraRepositorio = detalleOrdenCompraRepositorio;
        this.proveedorRepositorio = proveedorRepositorio;
        this.productoRepositorio = productoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.inventarioServicio = inventarioServicio;
        this.modelMapper = modelMapper;
    }

    // ============================================
    // OPERACIONES CRUD PRINCIPALES
    // ============================================

    @Override
    public OrdenCompraDTO crear(OrdenCompraDTO ordenCompraDTO) {
        log.debug("Creando nueva orden de compra para proveedor ID: {}", ordenCompraDTO.getProveedorId());

        validarOrdenCompraDTO(ordenCompraDTO);

        // Obtener entidades relacionadas
        Proveedor proveedor = obtenerProveedorPorId(ordenCompraDTO.getProveedorId());
        Usuario usuarioComprador = obtenerUsuarioPorId(ordenCompraDTO.getUsuarioCompradorId());

        // Crear la orden
        OrdenCompra ordenCompra = new OrdenCompra();
        ordenCompra.setProveedor(proveedor);
        ordenCompra.setUsuarioComprador(usuarioComprador);
        ordenCompra.setFechaOrden(ordenCompraDTO.getFechaOrden() != null ?
                ordenCompraDTO.getFechaOrden() : LocalDate.now());
        ordenCompra.setFechaEntregaEstimada(ordenCompraDTO.getFechaEntregaEstimada());
        ordenCompra.setEstado(EstadoOrden.BORRADOR);
        ordenCompra.setObservaciones(ordenCompraDTO.getObservaciones());
        ordenCompra.setCondicionesPago(ordenCompraDTO.getCondicionesPago());
        ordenCompra.setMetodoPago(ordenCompraDTO.getMetodoPago());
        ordenCompra.setDireccionEntrega(ordenCompraDTO.getDireccionEntrega());
        ordenCompra.setReferenciaProveedor(ordenCompraDTO.getReferenciaProveedor());
        ordenCompra.setPorcentajeImpuesto(ordenCompraDTO.getPorcentajeImpuesto());
        ordenCompra.setDescuento(ordenCompraDTO.getDescuento());
        ordenCompra.setFechaCreacion(LocalDateTime.now());

        // Procesar detalles
        if (ordenCompraDTO.getDetalles() != null && !ordenCompraDTO.getDetalles().isEmpty()) {
            procesarDetallesOrden(ordenCompra, ordenCompraDTO.getDetalles());
        }

        // Calcular totales
        ordenCompra.calcularTotal();

        // Guardar
        OrdenCompra ordenGuardada = ordenCompraRepositorio.save(ordenCompra);

        // Generar número de orden si no se generó automáticamente
        if (ordenGuardada.getNumeroOrden() == null ||
                ordenGuardada.getNumeroOrden().contains("-000000")) {
            String numeroOrden = generarNumeroOrden(ordenGuardada);
            ordenGuardada.setNumeroOrden(numeroOrden);
            ordenGuardada = ordenCompraRepositorio.save(ordenGuardada);
        }

        log.info("Orden de compra creada exitosamente con ID: {} y número: {}",
                ordenGuardada.getId(), ordenGuardada.getNumeroOrden());

        return convertirADTO(ordenGuardada);
    }

    @Override
    public OrdenCompraDTO actualizar(Long id, OrdenCompraDTO ordenCompraDTO) {
        log.debug("Actualizando orden de compra ID: {}", id);

        OrdenCompra ordenExistente = obtenerEntidadPorId(id);

        if (!ordenExistente.esModificable()) {
            throw new IllegalStateException(
                    "La orden en estado " + ordenExistente.getEstado().getDescripcion() +
                            " no puede ser modificada. Solo se pueden modificar órdenes en estado BORRADOR o PENDIENTE.");
        }

        validarOrdenCompraDTO(ordenCompraDTO);

        // Actualizar campos básicos
        actualizarCamposOrden(ordenExistente, ordenCompraDTO);

        // Actualizar detalles si es necesario
        if (ordenCompraDTO.getDetalles() != null) {
            // Eliminar detalles anteriores
            ordenExistente.getDetalles().clear();
            // Agregar nuevos detalles
            procesarDetallesOrden(ordenExistente, ordenCompraDTO.getDetalles());
        }

        // Recalcular totales
        ordenExistente.calcularTotal();
        ordenExistente.setFechaActualizacion(LocalDateTime.now());

        OrdenCompra ordenActualizada = ordenCompraRepositorio.save(ordenExistente);

        log.info("Orden de compra actualizada exitosamente: {}", ordenActualizada.getId());
        return convertirADTO(ordenActualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenCompraDTO buscarPorId(Long id) {
        log.debug("Buscando orden de compra por ID: {}", id);

        OrdenCompra orden = obtenerEntidadPorId(id);
        return convertirADTO(orden);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrdenCompraDTO> buscarTodos(Pageable pageable) {
        log.debug("Buscando órdenes de compra paginadas - Página: {}", pageable.getPageNumber());

        return ordenCompraRepositorio.findAll(pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenCompraDTO> buscarTodos() {
        log.debug("Buscando todas las órdenes de compra");

        return ordenCompraRepositorio.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // ============================================
    // BÚSQUEDAS ESPECÍFICAS
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public Page<OrdenCompraDTO> buscarPorProveedor(Long proveedorId, Pageable pageable) {
        log.debug("Buscando órdenes por proveedor ID: {}", proveedorId);

        return ordenCompraRepositorio.findByProveedorId(proveedorId, pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrdenCompraDTO> buscarPorEstado(EstadoOrden estado, Pageable pageable) {
        log.debug("Buscando órdenes por estado: {}", estado);

        return ordenCompraRepositorio.findByEstado(estado, pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrdenCompraDTO> buscarPorFechas(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable) {
        log.debug("Buscando órdenes por fechas: {} - {}", fechaInicio, fechaFin);

        return ordenCompraRepositorio.findByFechaOrdenBetween(fechaInicio, fechaFin, pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenCompraDTO buscarPorNumeroOrden(String numeroOrden) {
        log.debug("Buscando orden por número: {}", numeroOrden);

        OrdenCompra orden = ordenCompraRepositorio.findByNumeroOrden(numeroOrden)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Orden de compra no encontrada con número: " + numeroOrden));

        return convertirADTO(orden);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrdenCompraDTO> buscarConCriterios(Long proveedorId, EstadoOrden estado,
                                                    LocalDate fechaInicio, LocalDate fechaFin,
                                                    Long usuarioId, Pageable pageable) {
        log.debug("Buscando órdenes con criterios múltiples");

        return ordenCompraRepositorio.buscarConCriterios(
                proveedorId, estado, fechaInicio, fechaFin, usuarioId, pageable)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenCompraDTO> buscarOrdenesProximas() {
        log.debug("Buscando órdenes próximas a entregar");

        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(7);
        List<EstadoOrden> estadosValidos = Arrays.asList(
                EstadoOrden.ENVIADA,
                EstadoOrden.CONFIRMADA,
                EstadoOrden.RECIBIDA_PARCIAL
        );

        return ordenCompraRepositorio.findOrdenesProximas(hoy, limite, estadosValidos)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenCompraDTO> buscarOrdenesAtrasadas() {
        log.debug("Buscando órdenes atrasadas");

        return ordenCompraRepositorio.findOrdenesAtrasadas(LocalDate.now())
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // ============================================
    // GESTIÓN DE DETALLES
    // ============================================

    @Override
    public OrdenCompraDTO agregarDetalle(Long ordenId, DetalleOrdenCompraDTO detalleDTO) {
        log.debug("Agregando detalle a orden ID: {}", ordenId);

        OrdenCompra orden = obtenerEntidadPorId(ordenId);

        if (!orden.esModificable()) {
            throw new IllegalStateException(
                    "No se pueden agregar detalles a una orden en estado: " +
                            orden.getEstado().getDescripcion());
        }

        validarDetalleDTO(detalleDTO);
        Producto producto = obtenerProductoPorId(detalleDTO.getProductoId());

        DetalleOrdenCompra detalle = new DetalleOrdenCompra();
        detalle.setOrdenCompra(orden);
        detalle.setProducto(producto);
        detalle.setCantidad(detalleDTO.getCantidad());
        detalle.setPrecioUnitario(detalleDTO.getPrecioUnitario());
        detalle.setPorcentajeDescuento(detalleDTO.getPorcentajeDescuento());
        detalle.setObservaciones(detalleDTO.getObservaciones());
        detalle.setCodigoProveedor(detalleDTO.getCodigoProveedor());
        detalle.setNombreProveedor(detalleDTO.getNombreProveedor());
        detalle.setNumeroLinea(orden.getDetalles().size() + 1);
        detalle.setCantidadRecibida(0);
        detalle.calcularSubtotal();

        orden.agregarDetalle(detalle);
        orden.calcularTotal();
        orden.setFechaActualizacion(LocalDateTime.now());

        OrdenCompra ordenActualizada = ordenCompraRepositorio.save(orden);

        log.info("Detalle agregado exitosamente a orden: {}", ordenId);
        return convertirADTO(ordenActualizada);
    }

    @Override
    public OrdenCompraDTO actualizarDetalle(Long ordenId, Long detalleId, DetalleOrdenCompraDTO detalleDTO) {
        log.debug("Actualizando detalle {} de orden {}", detalleId, ordenId);

        OrdenCompra orden = obtenerEntidadPorId(ordenId);

        if (!orden.esModificable()) {
            throw new IllegalStateException(
                    "No se pueden modificar detalles de una orden en estado: " +
                            orden.getEstado().getDescripcion());
        }

        DetalleOrdenCompra detalle = orden.getDetalles().stream()
                .filter(d -> d.getId().equals(detalleId))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Detalle no encontrado con ID: " + detalleId));

        validarDetalleDTO(detalleDTO);

        detalle.setCantidad(detalleDTO.getCantidad());
        detalle.setPrecioUnitario(detalleDTO.getPrecioUnitario());
        detalle.setPorcentajeDescuento(detalleDTO.getPorcentajeDescuento());
        detalle.setObservaciones(detalleDTO.getObservaciones());
        detalle.calcularSubtotal();

        orden.calcularTotal();
        orden.setFechaActualizacion(LocalDateTime.now());

        OrdenCompra ordenActualizada = ordenCompraRepositorio.save(orden);

        log.info("Detalle {} actualizado en orden: {}", detalleId, ordenId);
        return convertirADTO(ordenActualizada);
    }

    @Override
    public OrdenCompraDTO eliminarDetalle(Long ordenId, Long detalleId) {
        log.debug("Eliminando detalle {} de orden {}", detalleId, ordenId);

        OrdenCompra orden = obtenerEntidadPorId(ordenId);

        if (!orden.esModificable()) {
            throw new IllegalStateException(
                    "No se pueden eliminar detalles de una orden en estado: " +
                            orden.getEstado().getDescripcion());
        }

        DetalleOrdenCompra detalle = orden.getDetalles().stream()
                .filter(d -> d.getId() != null && d.getId().equals(detalleId))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Detalle no encontrado con ID: " + detalleId));

        orden.eliminarDetalle(detalle);
        orden.calcularTotal();
        orden.setFechaActualizacion(LocalDateTime.now());

        OrdenCompra ordenActualizada = ordenCompraRepositorio.save(orden);

        log.info("Detalle {} eliminado de orden: {}", detalleId, ordenId);
        return convertirADTO(ordenActualizada);
    }

    // ============================================
    // CAMBIOS DE ESTADO
    // ============================================

    @Override
    public OrdenCompraDTO aprobar(Long id, Long usuarioAprobadorId) {
        log.debug("Aprobando orden ID: {}", id);

        OrdenCompra orden = obtenerEntidadPorId(id);

        if (orden.getEstado() != EstadoOrden.BORRADOR) {
            throw new IllegalStateException(
                    "Solo se pueden aprobar órdenes en estado BORRADOR");
        }

        validarOrdenParaAprobacion(orden);

        Usuario usuarioAprobador = obtenerUsuarioPorId(usuarioAprobadorId);
        orden.setUsuarioAprobador(usuarioAprobador);
        orden.setFechaAprobacion(LocalDateTime.now());
        orden.setEstado(EstadoOrden.PENDIENTE);
        orden.setFechaActualizacion(LocalDateTime.now());

        OrdenCompra ordenAprobada = ordenCompraRepositorio.save(orden);

        log.info("Orden {} aprobada por usuario {}", id, usuarioAprobadorId);
        return convertirADTO(ordenAprobada);
    }

    @Override
    public OrdenCompraDTO enviar(Long id) {
        log.debug("Enviando orden ID: {}", id);

        OrdenCompra orden = obtenerEntidadPorId(id);

        if (orden.getEstado() != EstadoOrden.PENDIENTE) {
            throw new IllegalStateException(
                    "Solo se pueden enviar órdenes en estado PENDIENTE");
        }

        orden.setEstado(EstadoOrden.ENVIADA);
        orden.setFechaActualizacion(LocalDateTime.now());

        OrdenCompra ordenEnviada = ordenCompraRepositorio.save(orden);

        log.info("Orden {} enviada", id);
        return convertirADTO(ordenEnviada);
    }

    @Override
    public OrdenCompraDTO confirmar(Long id) {
        log.debug("Confirmando orden ID: {}", id);

        OrdenCompra orden = obtenerEntidadPorId(id);

        if (orden.getEstado() != EstadoOrden.ENVIADA) {
            throw new IllegalStateException(
                    "Solo se pueden confirmar órdenes en estado ENVIADA");
        }

        orden.setEstado(EstadoOrden.CONFIRMADA);
        orden.setFechaActualizacion(LocalDateTime.now());

        OrdenCompra ordenConfirmada = ordenCompraRepositorio.save(orden);

        log.info("Orden {} confirmada", id);
        return convertirADTO(ordenConfirmada);
    }

    @Override
    public OrdenCompraDTO recibirCompleta(Long id, Long usuarioReceptorId) {
        log.debug("Recibiendo orden completa ID: {}", id);

        OrdenCompra orden = obtenerEntidadPorId(id);

        if (!orden.esRecibible()) {
            throw new IllegalStateException(
                    "La orden en estado " + orden.getEstado().getDescripcion() +
                            " no puede recibir mercancía");
        }

        Usuario usuarioReceptor = obtenerUsuarioPorId(usuarioReceptorId);

        // Registrar recepción de cada detalle e inventario
        for (DetalleOrdenCompra detalle : orden.getDetalles()) {
            int cantidadPendiente = detalle.getCantidadPendiente();

            if (cantidadPendiente > 0) {
                // Registrar en inventario
                inventarioServicio.registrarCompra(
                        detalle.getProducto().getId(),
                        cantidadPendiente,
                        orden.getId(),
                        detalle.getPrecioUnitario().doubleValue(),
                        usuarioReceptorId
                );

                // Actualizar detalle
                detalle.registrarRecepcion(cantidadPendiente);
            }
        }

        orden.setUsuarioReceptor(usuarioReceptor);
        orden.setFechaRecepcion(LocalDateTime.now());
        orden.setFechaEntregaReal(LocalDate.now());
        orden.setEstado(EstadoOrden.RECIBIDA_COMPLETA);
        orden.setFechaActualizacion(LocalDateTime.now());

        OrdenCompra ordenRecibida = ordenCompraRepositorio.save(orden);

        log.info("Orden {} recibida completamente por usuario {}", id, usuarioReceptorId);
        return convertirADTO(ordenRecibida);
    }

    @Override
    public OrdenCompraDTO recibirParcial(Long id, Map<Long, Integer> cantidadesPorDetalle,
                                          Long usuarioReceptorId) {
        log.debug("Recibiendo orden parcial ID: {}", id);

        OrdenCompra orden = obtenerEntidadPorId(id);

        if (!orden.esRecibible()) {
            throw new IllegalStateException(
                    "La orden en estado " + orden.getEstado().getDescripcion() +
                            " no puede recibir mercancía");
        }

        Usuario usuarioReceptor = obtenerUsuarioPorId(usuarioReceptorId);
        boolean primerRecepcion = orden.getFechaRecepcion() == null;

        // Procesar cada detalle
        for (Map.Entry<Long, Integer> entry : cantidadesPorDetalle.entrySet()) {
            Long detalleId = entry.getKey();
            Integer cantidadRecibir = entry.getValue();

            if (cantidadRecibir == null || cantidadRecibir <= 0) {
                continue;
            }

            DetalleOrdenCompra detalle = orden.getDetalles().stream()
                    .filter(d -> d.getId().equals(detalleId))
                    .findFirst()
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Detalle no encontrado con ID: " + detalleId));

            // Validar cantidad
            if (cantidadRecibir > detalle.getCantidadPendiente()) {
                throw new IllegalArgumentException(
                        "Cantidad a recibir (" + cantidadRecibir +
                                ") excede la cantidad pendiente (" + detalle.getCantidadPendiente() +
                                ") para el detalle: " + detalleId);
            }

            // Registrar en inventario
            inventarioServicio.registrarCompra(
                    detalle.getProducto().getId(),
                    cantidadRecibir,
                    orden.getId(),
                    detalle.getPrecioUnitario().doubleValue(),
                    usuarioReceptorId
            );

            // Actualizar detalle
            detalle.registrarRecepcion(cantidadRecibir);
        }

        // Actualizar estado de la orden
        if (primerRecepcion) {
            orden.setUsuarioReceptor(usuarioReceptor);
            orden.setFechaRecepcion(LocalDateTime.now());
        }

        boolean todosCompletos = orden.getDetalles().stream()
                .allMatch(DetalleOrdenCompra::estaCompleta);

        if (todosCompletos) {
            orden.setEstado(EstadoOrden.RECIBIDA_COMPLETA);
            orden.setFechaEntregaReal(LocalDate.now());
        } else {
            orden.setEstado(EstadoOrden.RECIBIDA_PARCIAL);
        }

        orden.setFechaActualizacion(LocalDateTime.now());

        OrdenCompra ordenActualizada = ordenCompraRepositorio.save(orden);

        log.info("Orden {} recibida parcialmente por usuario {}", id, usuarioReceptorId);
        return convertirADTO(ordenActualizada);
    }

    @Override
    public OrdenCompraDTO cancelar(Long id, String motivo) {
        log.debug("Cancelando orden ID: {} con motivo: {}", id, motivo);

        OrdenCompra orden = obtenerEntidadPorId(id);

        if (!orden.esCancelable()) {
            throw new IllegalStateException(
                    "La orden en estado " + orden.getEstado().getDescripcion() +
                            " no puede ser cancelada");
        }

        orden.setEstado(EstadoOrden.CANCELADA);
        orden.setMotivoCancelacion(motivo);
        orden.setFechaCancelacion(LocalDateTime.now());
        orden.setFechaActualizacion(LocalDateTime.now());

        OrdenCompra ordenCancelada = ordenCompraRepositorio.save(orden);

        log.info("Orden {} cancelada", id);
        return convertirADTO(ordenCancelada);
    }

    @Override
    public OrdenCompraDTO completar(Long id) {
        log.debug("Completando orden ID: {}", id);

        OrdenCompra orden = obtenerEntidadPorId(id);

        if (orden.getEstado() != EstadoOrden.RECIBIDA_COMPLETA) {
            throw new IllegalStateException(
                    "Solo se pueden completar órdenes en estado RECIBIDA_COMPLETA");
        }

        boolean todosRecibidos = orden.getDetalles().stream()
                .allMatch(DetalleOrdenCompra::estaCompleta);

        if (!todosRecibidos) {
            throw new IllegalStateException(
                    "No se puede completar la orden porque no todos los productos han sido recibidos completamente");
        }

        orden.setEstado(EstadoOrden.COMPLETADA);
        orden.setFechaActualizacion(LocalDateTime.now());

        OrdenCompra ordenCompletada = ordenCompraRepositorio.save(orden);

        log.info("Orden {} completada", id);
        return convertirADTO(ordenCompletada);
    }

    // ============================================
    // CÁLCULOS
    // ============================================

    @Override
    public OrdenCompraDTO calcularTotales(Long id) {
        log.debug("Calculando totales de orden ID: {}", id);

        OrdenCompra orden = obtenerEntidadPorId(id);
        orden.calcularTotal();
        orden.setFechaActualizacion(LocalDateTime.now());

        OrdenCompra ordenActualizada = ordenCompraRepositorio.save(orden);

        return convertirADTO(ordenActualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularMontoTotalPorEstado(EstadoOrden estado) {
        return ordenCompraRepositorio.calcularMontoTotalPorEstado(estado);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularMontoTotalPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        return ordenCompraRepositorio.calcularMontoTotalPeriodo(fechaInicio, fechaFin);
    }

    // ============================================
    // ESTADÍSTICAS
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public EstadisticasComprasDTO obtenerEstadisticas() {
        log.debug("Obteniendo estadísticas de compras");

        EstadisticasComprasDTO estadisticas = new EstadisticasComprasDTO(LocalDateTime.now());

        // Estadísticas básicas
        estadisticas.setTotalOrdenes(ordenCompraRepositorio.count());
        estadisticas.setTotalProveedores(proveedorRepositorio.count());
        estadisticas.setProveedoresActivos(proveedorRepositorio.countByActivoTrue());

        // Estadísticas por estado
        for (EstadoOrden estado : EstadoOrden.values()) {
            long cantidad = ordenCompraRepositorio.countByEstado(estado);
            switch (estado) {
                case BORRADOR -> estadisticas.setOrdenesBorrador(cantidad);
                case PENDIENTE -> estadisticas.setOrdenesPendientes(cantidad);
                case ENVIADA -> estadisticas.setOrdenesEnviadas(cantidad);
                case CONFIRMADA -> estadisticas.setOrdenesConfirmadas(cantidad);
                case COMPLETADA, RECIBIDA_COMPLETA -> estadisticas.setOrdenesCompletadas(
                        estadisticas.getOrdenesCompletadas() != null ?
                                estadisticas.getOrdenesCompletadas() + cantidad : cantidad);
                case CANCELADA -> estadisticas.setOrdenesCanceladas(cantidad);
            }
        }

        // Montos
        LocalDate hoy = LocalDate.now();
        estadisticas.setMontoMes(calcularMontoTotalPeriodo(
                hoy.withDayOfMonth(1), hoy));
        estadisticas.setMontoTotalCompras(calcularMontoTotalPeriodo(
                hoy.withMonth(1).withDayOfMonth(1), hoy));

        // Calcular métricas derivadas
        estadisticas.calcularMetricasDerivadas();

        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public EstadisticasComprasDTO obtenerEstadisticasPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        log.debug("Obteniendo estadísticas de compras para período: {} - {}", fechaInicio, fechaFin);

        EstadisticasComprasDTO estadisticas = new EstadisticasComprasDTO(LocalDateTime.now());
        estadisticas.setPeriodoAnalizado(fechaInicio + " a " + fechaFin);

        estadisticas.setTotalOrdenes(ordenCompraRepositorio.countByFechaOrdenBetween(fechaInicio, fechaFin));
        estadisticas.setMontoMes(calcularMontoTotalPeriodo(fechaInicio, fechaFin));

        estadisticas.calcularMetricasDerivadas();

        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPorEstado(EstadoOrden estado) {
        return ordenCompraRepositorio.countByEstado(estado);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPorProveedor(Long proveedorId) {
        return ordenCompraRepositorio.countByProveedorId(proveedorId);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPorPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        return ordenCompraRepositorio.countByFechaOrdenBetween(fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarTodos() {
        return ordenCompraRepositorio.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Double calcularPromedioTiempoEntrega() {
        return ordenCompraRepositorio.calcularPromedioTiempoEntrega();
    }

    @Override
    @Transactional(readOnly = true)
    public Double calcularTasaCumplimiento() {
        long totalOrdenes = ordenCompraRepositorio.count();
        if (totalOrdenes == 0) return 0.0;

        Long ordenesATiempo = ordenCompraRepositorio.contarOrdenesATiempo();
        return (ordenesATiempo.doubleValue() / totalOrdenes) * 100.0;
    }

    // ============================================
    // VALIDACIONES
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public boolean existe(Long id) {
        return ordenCompraRepositorio.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean esModificable(Long id) {
        OrdenCompra orden = obtenerEntidadPorId(id);
        return orden.esModificable();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean esRecibible(Long id) {
        OrdenCompra orden = obtenerEntidadPorId(id);
        return orden.esRecibible();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean esCancelable(Long id) {
        OrdenCompra orden = obtenerEntidadPorId(id);
        return orden.esCancelable();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean todosLosDetallesRecibidos(Long id) {
        return detalleOrdenCompraRepositorio.todosLosDetallesRecibidos(id);
    }

    // ============================================
    // OPERACIONES DE ELIMINACIÓN
    // ============================================

    @Override
    public void eliminar(Long id) {
        log.debug("Eliminando orden ID: {}", id);

        OrdenCompra orden = obtenerEntidadPorId(id);

        if (orden.getEstado() != EstadoOrden.BORRADOR) {
            throw new IllegalStateException(
                    "Solo se pueden eliminar órdenes en estado BORRADOR. " +
                            "Cancele la orden en su lugar.");
        }

        ordenCompraRepositorio.deleteById(id);
        log.info("Orden eliminada exitosamente: {}", id);
    }

    // ============================================
    // IMPLEMENTACIÓN DE BaseServiceImpl
    // ============================================

    @Override

    // ============================================
    // MÉTODOS PRIVADOS Y HELPER
    // ============================================

    private OrdenCompra obtenerEntidadPorId(Long id) {
        return ordenCompraRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Orden de compra no encontrada con ID: " + id));
    }

    private Proveedor obtenerProveedorPorId(Long id) {
        return proveedorRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Proveedor no encontrado con ID: " + id));
    }

    private Producto obtenerProductoPorId(Long id) {
        return productoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Producto no encontrado con ID: " + id));
    }

    private Usuario obtenerUsuarioPorId(Long id) {
        return usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario no encontrado con ID: " + id));
    }

    private OrdenCompraDTO convertirADTO(OrdenCompra orden) {
        if (orden == null) return null;

        OrdenCompraDTO dto = modelMapper.map(orden, OrdenCompraDTO.class);

        // Mapear campos adicionales
        if (orden.getProveedor() != null) {
            dto.setProveedorNombre(orden.getProveedor().getNombre());
            dto.setProveedorRut(orden.getProveedor().getRut());
        }

        if (orden.getUsuarioComprador() != null) {
            dto.setUsuarioCompradorNombre(orden.getUsuarioComprador().getNombreCompleto());
        }

        if (orden.getUsuarioAprobador() != null) {
            dto.setUsuarioAprobadorNombre(orden.getUsuarioAprobador().getNombreCompleto());
        }

        if (orden.getUsuarioReceptor() != null) {
            dto.setUsuarioReceptorNombre(orden.getUsuarioReceptor().getNombreCompleto());
        }

        // Mapear detalles
        if (orden.getDetalles() != null) {
            List<DetalleOrdenCompraDTO> detallesDTO = orden.getDetalles().stream()
                    .map(this::convertirDetalleADTO)
                    .collect(Collectors.toList());
            dto.setDetalles(detallesDTO);
        }

        return dto;
    }

    private DetalleOrdenCompraDTO convertirDetalleADTO(DetalleOrdenCompra detalle) {
        if (detalle == null) return null;

        DetalleOrdenCompraDTO dto = modelMapper.map(detalle, DetalleOrdenCompraDTO.class);

        if (detalle.getProducto() != null) {
            dto.setProductoNombre(detalle.getProducto().getNombre());
            dto.setProductoCodigo(detalle.getProducto().getSku());
        }

        return dto;
    }

    private void validarOrdenCompraDTO(OrdenCompraDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos de la orden son obligatorios");
        }

        if (dto.getProveedorId() == null) {
            throw new IllegalArgumentException("El proveedor es obligatorio");
        }

        if (dto.getUsuarioCompradorId() == null) {
            throw new IllegalArgumentException("El usuario comprador es obligatorio");
        }

        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La orden debe tener al menos un detalle");
        }

        // Validar cada detalle
        for (DetalleOrdenCompraDTO detalle : dto.getDetalles()) {
            validarDetalleDTO(detalle);
        }
    }

    private void validarDetalleDTO(DetalleOrdenCompraDTO dto) {
        if (dto.getProductoId() == null) {
            throw new IllegalArgumentException("El producto es obligatorio en el detalle");
        }

        if (dto.getCantidad() == null || dto.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }

        if (dto.getPrecioUnitario() == null || dto.getPrecioUnitario().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor que cero");
        }
    }

    private void validarOrdenParaAprobacion(OrdenCompra orden) {
        if (orden.getDetalles() == null || orden.getDetalles().isEmpty()) {
            throw new IllegalStateException("La orden debe tener al menos un detalle para ser aprobada");
        }

        if (orden.getFechaEntregaEstimada() == null) {
            throw new IllegalStateException("La fecha de entrega estimada es obligatoria para aprobar");
        }
    }

    private void procesarDetallesOrden(OrdenCompra orden, List<DetalleOrdenCompraDTO> detallesDTO) {
        for (DetalleOrdenCompraDTO detalleDTO : detallesDTO) {
            Producto producto = obtenerProductoPorId(detalleDTO.getProductoId());

            DetalleOrdenCompra detalle = new DetalleOrdenCompra();
            detalle.setOrdenCompra(orden);
            detalle.setProducto(producto);
            detalle.setCantidad(detalleDTO.getCantidad());
            detalle.setPrecioUnitario(detalleDTO.getPrecioUnitario());
            detalle.setPorcentajeDescuento(detalleDTO.getPorcentajeDescuento() != null ?
                    detalleDTO.getPorcentajeDescuento() : BigDecimal.ZERO);
            detalle.setObservaciones(detalleDTO.getObservaciones());
            detalle.setCodigoProveedor(detalleDTO.getCodigoProveedor());
            detalle.setNombreProveedor(detalleDTO.getNombreProveedor());
            detalle.setNumeroLinea(orden.getDetalles().size() + 1);
            detalle.setCantidadRecibida(0);
            detalle.calcularSubtotal();

            orden.agregarDetalle(detalle);
        }
    }

    private void actualizarCamposOrden(OrdenCompra orden, OrdenCompraDTO dto) {
        orden.setFechaEntregaEstimada(dto.getFechaEntregaEstimada());
        orden.setObservaciones(dto.getObservaciones());
        orden.setCondicionesPago(dto.getCondicionesPago());
        orden.setMetodoPago(dto.getMetodoPago());
        orden.setDireccionEntrega(dto.getDireccionEntrega());
        orden.setReferenciaProveedor(dto.getReferenciaProveedor());
        orden.setPorcentajeImpuesto(dto.getPorcentajeImpuesto());
        orden.setDescuento(dto.getDescuento());
    }

    private String generarNumeroOrden(OrdenCompra orden) {
        LocalDateTime ahora = LocalDateTime.now();
        String fecha = String.format("%04d%02d%02d",
                ahora.getYear(), ahora.getMonthValue(), ahora.getDayOfMonth());
        return String.format("OC-%s-%06d", fecha, orden.getId());
    }
}
