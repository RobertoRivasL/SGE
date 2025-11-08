package informviva.gest.controlador.api;

import informviva.gest.dto.DetalleOrdenCompraDTO;
import informviva.gest.dto.OrdenCompraDTO;
import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.model.OrdenCompra.EstadoOrden;
import informviva.gest.model.Usuario;
import informviva.gest.service.OrdenCompraServicio;
import informviva.gest.service.UsuarioServicio;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API REST para gestión de órdenes de compra
 * Endpoints para integración con aplicaciones externas y funcionalidades AJAX
 *
 * Siguiendo el patrón arquitectónico del ClienteRestControlador
 *
 * @author Sistema de Gestión Empresarial
 * @version 1.0
 * @since FASE 3 - Módulo de Compras
 */
@RestController
@RequestMapping("/api/ordenes-compra")
public class OrdenCompraRestControlador {

    private static final Logger logger = LoggerFactory.getLogger(OrdenCompraRestControlador.class);

    private final OrdenCompraServicio ordenCompraServicio;
    private final UsuarioServicio usuarioServicio;

    public OrdenCompraRestControlador(OrdenCompraServicio ordenCompraServicio,
                                     UsuarioServicio usuarioServicio) {
        this.ordenCompraServicio = ordenCompraServicio;
        this.usuarioServicio = usuarioServicio;
    }

    // ========== ENDPOINTS DE CONSULTA ==========

    /**
     * Obtener todas las órdenes con paginación y filtros
     * GET /api/ordenes-compra
     *
     * @param page Número de página
     * @param size Tamaño de página
     * @param sortBy Campo de ordenamiento
     * @param sortDir Dirección de ordenamiento
     * @param proveedorId Filtro por proveedor
     * @param estado Filtro por estado
     * @param fechaInicio Filtro fecha inicial
     * @param fechaFin Filtro fecha final
     * @return Página de órdenes
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COMPRAS')")
    public ResponseEntity<Page<OrdenCompraDTO>> obtenerOrdenes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaOrden") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long proveedorId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        try {
            logger.info("API: Obteniendo órdenes de compra - Página: {}, Tamaño: {}", page, size);

            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);

            // Convertir estado String a EstadoOrden
            EstadoOrden estadoOrden = null;
            if (estado != null && !estado.trim().isEmpty()) {
                try {
                    estadoOrden = EstadoOrden.valueOf(estado);
                } catch (IllegalArgumentException e) {
                    logger.warn("Estado inválido: {}", estado);
                }
            }

            Page<OrdenCompraDTO> ordenesPage = ordenCompraServicio.buscarConCriterios(
                    proveedorId, estadoOrden, fechaInicio, fechaFin, null, pageable);

            logger.info("API: {} órdenes encontradas", ordenesPage.getTotalElements());
            return ResponseEntity.ok(ordenesPage);

        } catch (Exception e) {
            logger.error("Error obteniendo órdenes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener orden por ID
     * GET /api/ordenes-compra/{id}
     *
     * @param id ID de la orden
     * @return Orden encontrada
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COMPRAS')")
    public ResponseEntity<OrdenCompraDTO> obtenerPorId(@PathVariable Long id) {
        try {
            logger.info("API: Obteniendo orden ID: {}", id);

            OrdenCompraDTO orden = ordenCompraServicio.buscarPorId(id);

            return ResponseEntity.ok(orden);

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Orden no encontrada: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("Error obteniendo orden {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== ENDPOINTS DE MODIFICACIÓN ==========

    /**
     * Crear nueva orden de compra
     * POST /api/ordenes-compra
     *
     * @param ordenCompraDTO Datos de la orden
     * @return Orden creada
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COMPRAS')")
    public ResponseEntity<OrdenCompraDTO> crear(@Valid @RequestBody OrdenCompraDTO ordenCompraDTO) {
        try {
            logger.info("API: Creando nueva orden de compra");

            // Obtener usuario actual
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuarioActual = usuarioServicio.buscarPorUsername(auth.getName());
            ordenCompraDTO.setUsuarioCompradorId(usuarioActual.getId());

            // Validar que tenga al menos un detalle
            if (ordenCompraDTO.getDetalles() == null || ordenCompraDTO.getDetalles().isEmpty()) {
                logger.warn("Intento de crear orden sin detalles");
                return ResponseEntity.badRequest().build();
            }

            OrdenCompraDTO ordenGuardada = ordenCompraServicio.crear(ordenCompraDTO);

            logger.info("API: Orden creada exitosamente: {}", ordenGuardada.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(ordenGuardada);

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Recurso no encontrado: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            logger.error("Error creando orden: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Actualizar orden existente
     * PUT /api/ordenes-compra/{id}
     *
     * @param id ID de la orden
     * @param ordenCompraDTO Datos actualizados
     * @return Orden actualizada
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COMPRAS')")
    public ResponseEntity<OrdenCompraDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody OrdenCompraDTO ordenCompraDTO) {

        try {
            logger.info("API: Actualizando orden ID: {}", id);

            // Verificar existencia
            if (!ordenCompraServicio.existe(id)) {
                logger.warn("Orden no encontrada: {}", id);
                return ResponseEntity.notFound().build();
            }

            // Verificar si es modificable
            if (!ordenCompraServicio.esModificable(id)) {
                logger.warn("Orden no modificable: {}", id);
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            OrdenCompraDTO ordenActualizada = ordenCompraServicio.actualizar(id, ordenCompraDTO);

            logger.info("API: Orden actualizada exitosamente: {}", id);
            return ResponseEntity.ok(ordenActualizada);

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Orden no encontrada: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            logger.warn("No se puede actualizar orden: {}", e.getMessage());
            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);

        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            logger.error("Error actualizando orden {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Eliminar orden
     * DELETE /api/ordenes-compra/{id}
     *
     * @param id ID de la orden
     * @return Respuesta con mensaje
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Long id) {
        try {
            logger.info("API: Eliminando orden ID: {}", id);

            // Verificar existencia
            if (!ordenCompraServicio.existe(id)) {
                logger.warn("Orden no encontrada: {}", id);
                return ResponseEntity.notFound().build();
            }

            ordenCompraServicio.eliminar(id);

            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Orden eliminada exitosamente");

            logger.info("API: Orden eliminada exitosamente: {}", id);
            return ResponseEntity.ok(respuesta);

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Orden no encontrada: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            logger.warn("No se puede eliminar orden: {}", e.getMessage());
            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(respuesta);

        } catch (Exception e) {
            logger.error("Error eliminando orden {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== ENDPOINTS DE CAMBIO DE ESTADO ==========

    /**
     * Aprobar orden
     * POST /api/ordenes-compra/{id}/aprobar
     *
     * @param id ID de la orden
     * @return Orden aprobada
     */
    @PostMapping("/{id}/aprobar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<OrdenCompraDTO> aprobar(@PathVariable Long id) {
        try {
            logger.info("API: Aprobando orden ID: {}", id);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuarioActual = usuarioServicio.buscarPorUsername(auth.getName());

            OrdenCompraDTO orden = ordenCompraServicio.aprobar(id, usuarioActual.getId());

            logger.info("API: Orden aprobada exitosamente: {}", id);
            return ResponseEntity.ok(orden);

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Orden no encontrada: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            logger.warn("No se puede aprobar orden: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        } catch (Exception e) {
            logger.error("Error aprobando orden {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Enviar orden al proveedor
     * POST /api/ordenes-compra/{id}/enviar
     *
     * @param id ID de la orden
     * @return Orden enviada
     */
    @PostMapping("/{id}/enviar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COMPRAS')")
    public ResponseEntity<OrdenCompraDTO> enviar(@PathVariable Long id) {
        try {
            logger.info("API: Enviando orden ID: {}", id);

            OrdenCompraDTO orden = ordenCompraServicio.enviar(id);

            logger.info("API: Orden enviada exitosamente: {}", id);
            return ResponseEntity.ok(orden);

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Orden no encontrada: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            logger.warn("No se puede enviar orden: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        } catch (Exception e) {
            logger.error("Error enviando orden {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Confirmar orden con proveedor
     * POST /api/ordenes-compra/{id}/confirmar
     *
     * @param id ID de la orden
     * @return Orden confirmada
     */
    @PostMapping("/{id}/confirmar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COMPRAS')")
    public ResponseEntity<OrdenCompraDTO> confirmar(@PathVariable Long id) {
        try {
            logger.info("API: Confirmando orden ID: {}", id);

            OrdenCompraDTO orden = ordenCompraServicio.confirmar(id);

            logger.info("API: Orden confirmada exitosamente: {}", id);
            return ResponseEntity.ok(orden);

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Orden no encontrada: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            logger.warn("No se puede confirmar orden: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        } catch (Exception e) {
            logger.error("Error confirmando orden {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Recibir orden completa
     * POST /api/ordenes-compra/{id}/recibir-completa
     *
     * @param id ID de la orden
     * @return Orden recibida
     */
    @PostMapping("/{id}/recibir-completa")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COMPRAS')")
    public ResponseEntity<OrdenCompraDTO> recibirCompleta(@PathVariable Long id) {
        try {
            logger.info("API: Recibiendo orden completa ID: {}", id);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuarioActual = usuarioServicio.buscarPorUsername(auth.getName());

            OrdenCompraDTO orden = ordenCompraServicio.recibirCompleta(id, usuarioActual.getId());

            logger.info("API: Orden recibida completamente: {}", id);
            return ResponseEntity.ok(orden);

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Orden no encontrada: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            logger.warn("No se puede recibir orden: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        } catch (Exception e) {
            logger.error("Error recibiendo orden {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Recibir orden parcialmente
     * POST /api/ordenes-compra/{id}/recibir-parcial
     *
     * @param id ID de la orden
     * @param cantidades Map con cantidades recibidas por detalle
     * @return Orden actualizada
     */
    @PostMapping("/{id}/recibir-parcial")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COMPRAS')")
    public ResponseEntity<OrdenCompraDTO> recibirParcial(
            @PathVariable Long id,
            @RequestBody Map<Long, Integer> cantidades) {

        try {
            logger.info("API: Recibiendo orden parcial ID: {}", id);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuarioActual = usuarioServicio.buscarPorUsername(auth.getName());

            OrdenCompraDTO orden = ordenCompraServicio.recibirParcial(id, cantidades, usuarioActual.getId());

            logger.info("API: Orden recibida parcialmente: {}", id);
            return ResponseEntity.ok(orden);

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Orden no encontrada: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warn("Error en recepción parcial: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        } catch (Exception e) {
            logger.error("Error recibiendo orden parcialmente {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Cancelar orden
     * POST /api/ordenes-compra/{id}/cancelar
     *
     * @param id ID de la orden
     * @param request Map con el motivo de cancelación
     * @return Orden cancelada
     */
    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<OrdenCompraDTO> cancelar(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        try {
            logger.info("API: Cancelando orden ID: {}", id);

            String motivo = request.get("motivo");
            if (motivo == null || motivo.trim().isEmpty()) {
                logger.warn("Motivo de cancelación no proporcionado");
                return ResponseEntity.badRequest().build();
            }

            OrdenCompraDTO orden = ordenCompraServicio.cancelar(id, motivo);

            logger.info("API: Orden cancelada: {}", id);
            return ResponseEntity.ok(orden);

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Orden no encontrada: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            logger.warn("No se puede cancelar orden: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        } catch (Exception e) {
            logger.error("Error cancelando orden {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== ENDPOINTS DE GESTIÓN DE DETALLES ==========

    /**
     * Agregar detalle a orden
     * POST /api/ordenes-compra/{id}/detalles
     *
     * @param id ID de la orden
     * @param detalle Detalle a agregar
     * @return Orden actualizada
     */
    @PostMapping("/{id}/detalles")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COMPRAS')")
    public ResponseEntity<OrdenCompraDTO> agregarDetalle(
            @PathVariable Long id,
            @Valid @RequestBody DetalleOrdenCompraDTO detalle) {

        try {
            logger.info("API: Agregando detalle a orden ID: {}", id);

            OrdenCompraDTO orden = ordenCompraServicio.agregarDetalle(id, detalle);

            logger.info("API: Detalle agregado exitosamente");
            return ResponseEntity.ok(orden);

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Recurso no encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            logger.warn("No se puede agregar detalle: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        } catch (Exception e) {
            logger.error("Error agregando detalle a orden {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Eliminar detalle de orden
     * DELETE /api/ordenes-compra/{id}/detalles/{detalleId}
     *
     * @param id ID de la orden
     * @param detalleId ID del detalle
     * @return Orden actualizada
     */
    @DeleteMapping("/{id}/detalles/{detalleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COMPRAS')")
    public ResponseEntity<OrdenCompraDTO> eliminarDetalle(
            @PathVariable Long id,
            @PathVariable Long detalleId) {

        try {
            logger.info("API: Eliminando detalle {} de orden ID: {}", detalleId, id);

            OrdenCompraDTO orden = ordenCompraServicio.eliminarDetalle(id, detalleId);

            logger.info("API: Detalle eliminado exitosamente");
            return ResponseEntity.ok(orden);

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Recurso no encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            logger.warn("No se puede eliminar detalle: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        } catch (Exception e) {
            logger.error("Error eliminando detalle de orden {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== ENDPOINTS DE CONSULTAS ESPECÍFICAS ==========

    /**
     * Obtener órdenes próximas a entregar
     * GET /api/ordenes-compra/proximas
     *
     * @param limite Número máximo de órdenes
     * @return Lista de órdenes próximas
     */
    @GetMapping("/proximas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COMPRAS')")
    public ResponseEntity<List<OrdenCompraDTO>> obtenerProximas(
            @RequestParam(defaultValue = "10") int limite) {

        try {
            logger.info("API: Obteniendo órdenes próximas");

            List<OrdenCompraDTO> ordenes = ordenCompraServicio.buscarOrdenesProximas();

            // Limitar resultados
            if (ordenes.size() > limite) {
                ordenes = ordenes.subList(0, limite);
            }

            return ResponseEntity.ok(ordenes);

        } catch (Exception e) {
            logger.error("Error obteniendo órdenes próximas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener órdenes atrasadas
     * GET /api/ordenes-compra/atrasadas
     *
     * @return Lista de órdenes atrasadas
     */
    @GetMapping("/atrasadas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COMPRAS')")
    public ResponseEntity<List<OrdenCompraDTO>> obtenerAtrasadas() {
        try {
            logger.info("API: Obteniendo órdenes atrasadas");

            List<OrdenCompraDTO> ordenes = ordenCompraServicio.buscarOrdenesAtrasadas();

            return ResponseEntity.ok(ordenes);

        } catch (Exception e) {
            logger.error("Error obteniendo órdenes atrasadas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== ENDPOINTS DE ESTADÍSTICAS ==========

    /**
     * Obtener estadísticas de órdenes de compra
     * GET /api/ordenes-compra/estadisticas
     *
     * @return Estadísticas generales
     */
    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        try {
            logger.info("API: Obteniendo estadísticas de órdenes de compra");

            Map<String, Object> estadisticas = new HashMap<>();

            // Conteos por estado
            estadisticas.put("totalOrdenes", ordenCompraServicio.contarTodos());
            estadisticas.put("ordenesBorrador", ordenCompraServicio.contarPorEstado(EstadoOrden.BORRADOR));
            estadisticas.put("ordenesPendientes", ordenCompraServicio.contarPorEstado(EstadoOrden.PENDIENTE));
            estadisticas.put("ordenesEnviadas", ordenCompraServicio.contarPorEstado(EstadoOrden.ENVIADA));
            estadisticas.put("ordenesConfirmadas", ordenCompraServicio.contarPorEstado(EstadoOrden.CONFIRMADA));
            estadisticas.put("ordenesEnTransito", ordenCompraServicio.contarPorEstado(EstadoOrden.EN_TRANSITO));
            estadisticas.put("ordenesRecibidas", ordenCompraServicio.contarPorEstado(EstadoOrden.RECIBIDA_PARCIAL));
            estadisticas.put("ordenesCompletadas", ordenCompraServicio.contarPorEstado(EstadoOrden.COMPLETADA));
            estadisticas.put("ordenesCanceladas", ordenCompraServicio.contarPorEstado(EstadoOrden.CANCELADA));

            // Montos totales
            estadisticas.put("montoTotalPendientes",
                    ordenCompraServicio.calcularMontoTotalPorEstado(EstadoOrden.PENDIENTE));
            estadisticas.put("montoTotalCompletadas",
                    ordenCompraServicio.calcularMontoTotalPorEstado(EstadoOrden.COMPLETADA));

            // Métricas de desempeño
            estadisticas.put("promedioTiempoEntrega", ordenCompraServicio.calcularPromedioTiempoEntrega());
            estadisticas.put("tasaCumplimiento", ordenCompraServicio.calcularTasaCumplimiento());

            // Órdenes especiales
            estadisticas.put("ordenesProximas", ordenCompraServicio.buscarOrdenesProximas().size());
            estadisticas.put("ordenesAtrasadas", ordenCompraServicio.buscarOrdenesAtrasadas().size());

            logger.info("API: Estadísticas de órdenes generadas exitosamente");
            return ResponseEntity.ok(estadisticas);

        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas de órdenes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener estadísticas por proveedor
     * GET /api/ordenes-compra/estadisticas/proveedor/{proveedorId}
     *
     * @param proveedorId ID del proveedor
     * @return Estadísticas del proveedor
     */
    @GetMapping("/estadisticas/proveedor/{proveedorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasProveedor(@PathVariable Long proveedorId) {
        try {
            logger.info("API: Obteniendo estadísticas para proveedor ID: {}", proveedorId);

            Map<String, Object> estadisticas = new HashMap<>();

            estadisticas.put("totalOrdenes", ordenCompraServicio.contarPorProveedor(proveedorId));

            // Obtener todas las órdenes del proveedor
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
            Page<OrdenCompraDTO> ordenesPage = ordenCompraServicio.buscarPorProveedor(proveedorId, pageable);

            // Calcular estadísticas específicas
            long completadas = ordenesPage.getContent().stream()
                    .filter(o -> o.getEstado() == EstadoOrden.COMPLETADA)
                    .count();

            long canceladas = ordenesPage.getContent().stream()
                    .filter(o -> o.getEstado() == EstadoOrden.CANCELADA)
                    .count();

            estadisticas.put("ordenesCompletadas", completadas);
            estadisticas.put("ordenesCanceladas", canceladas);

            return ResponseEntity.ok(estadisticas);

        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas de proveedor {}: {}", proveedorId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener información resumida de una orden
     * GET /api/ordenes-compra/{id}/resumen
     *
     * @param id ID de la orden
     * @return Resumen de la orden
     */
    @GetMapping("/{id}/resumen")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COMPRAS')")
    public ResponseEntity<Map<String, Object>> obtenerResumen(@PathVariable Long id) {
        try {
            logger.info("API: Obteniendo resumen de orden ID: {}", id);

            OrdenCompraDTO orden = ordenCompraServicio.buscarPorId(id);

            Map<String, Object> resumen = new HashMap<>();
            resumen.put("numeroOrden", orden.getNumeroOrden());
            resumen.put("proveedor", orden.getProveedorNombre());
            resumen.put("estado", orden.getEstado());
            resumen.put("estadoDescripcion", orden.getEstadoDescripcion());
            resumen.put("fechaOrden", orden.getFechaOrden());
            resumen.put("fechaEntregaEstimada", orden.getFechaEntregaEstimada());
            resumen.put("total", orden.getTotal());
            resumen.put("cantidadLineas", orden.getCantidadLineas());
            resumen.put("totalUnidades", orden.getTotalUnidades());
            resumen.put("totalUnidadesRecibidas", orden.getTotalUnidadesRecibidas());
            resumen.put("porcentajeRecepcion", orden.getPorcentajeRecepcion());
            resumen.put("esModificable", orden.esModificable());
            resumen.put("esRecibible", orden.esRecibible());
            resumen.put("esCancelable", orden.esCancelable());

            return ResponseEntity.ok(resumen);

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Orden no encontrada: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("Error obteniendo resumen de orden {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
