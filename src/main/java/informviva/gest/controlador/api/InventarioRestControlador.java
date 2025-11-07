package informviva.gest.controlador.api;

import informviva.gest.dto.AjusteInventarioRequestDTO;
import informviva.gest.dto.EstadisticasInventarioDTO;
import informviva.gest.dto.MovimientoInventarioDTO;
import informviva.gest.dto.ProductoDTO;
import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.model.MovimientoInventario.TipoMovimiento;
import informviva.gest.model.Usuario;
import informviva.gest.service.InventarioServicio;
import informviva.gest.service.ProductoServicio;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API REST para gestión de inventario
 * Proporciona endpoints para consultas y operaciones de inventario
 *
 * Características:
 * - Retorna ResponseEntity con datos JSON
 * - Logging de todas las operaciones
 * - Manejo robusto de excepciones
 * - Control de acceso por roles
 *
 * @author Sistema de Gestión Empresarial
 * @version 1.0
 * @since FASE 2 - Módulo de Inventario
 */
@RestController
@RequestMapping("/api/inventario")
public class InventarioRestControlador {

    private static final Logger logger = LoggerFactory.getLogger(InventarioRestControlador.class);

    private final InventarioServicio inventarioServicio;
    private final ProductoServicio productoServicio;
    private final UsuarioServicio usuarioServicio;

    public InventarioRestControlador(InventarioServicio inventarioServicio,
                                     ProductoServicio productoServicio,
                                     UsuarioServicio usuarioServicio) {
        this.inventarioServicio = inventarioServicio;
        this.productoServicio = productoServicio;
        this.usuarioServicio = usuarioServicio;
    }

    // ==================== ENDPOINTS DE ESTADÍSTICAS ====================

    /**
     * Obtener estadísticas completas del inventario
     * GET /api/inventario/estadisticas
     */
    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<EstadisticasInventarioDTO> obtenerEstadisticas() {
        try {
            logger.info("Solicitando estadísticas de inventario");

            EstadisticasInventarioDTO estadisticas = inventarioServicio.obtenerEstadisticas();

            logger.info("Estadísticas de inventario generadas exitosamente");
            return ResponseEntity.ok(estadisticas);

        } catch (Exception e) {
            logger.error("Error al obtener estadísticas de inventario: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== ENDPOINTS DE MOVIMIENTOS ====================

    /**
     * Obtener movimientos con filtros y paginación
     * GET /api/inventario/movimientos
     */
    @GetMapping("/movimientos")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Page<MovimientoInventarioDTO>> obtenerMovimientos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fecha") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Long usuarioId) {

        try {
            logger.info("Solicitando movimientos de inventario - page: {}, size: {}, productoId: {}, tipo: {}",
                    page, size, productoId, tipo);

            // Configurar paginación
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            // Convertir tipo String a TipoMovimiento
            TipoMovimiento tipoMovimiento = null;
            if (tipo != null && !tipo.trim().isEmpty()) {
                try {
                    tipoMovimiento = TipoMovimiento.valueOf(tipo.toUpperCase());
                } catch (IllegalArgumentException e) {
                    logger.warn("Tipo de movimiento inválido: {}", tipo);
                }
            }

            // Convertir fechas a LocalDateTime
            LocalDateTime fechaInicioDateTime = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
            LocalDateTime fechaFinDateTime = fechaFin != null ? fechaFin.atTime(23, 59, 59) : null;

            // Obtener movimientos con criterios
            Page<MovimientoInventarioDTO> movimientos = inventarioServicio.buscarConCriterios(
                    productoId, tipoMovimiento, fechaInicioDateTime, fechaFinDateTime, usuarioId, pageable);

            logger.info("Movimientos obtenidos exitosamente: {} resultados", movimientos.getTotalElements());
            return ResponseEntity.ok(movimientos);

        } catch (Exception e) {
            logger.error("Error al obtener movimientos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener movimiento por ID
     * GET /api/inventario/movimientos/{id}
     */
    @GetMapping("/movimientos/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<MovimientoInventarioDTO> obtenerMovimientoPorId(@PathVariable Long id) {
        try {
            logger.info("Solicitando movimiento con ID: {}", id);

            MovimientoInventarioDTO movimiento = inventarioServicio.buscarPorId(id);

            if (movimiento == null) {
                logger.warn("Movimiento no encontrado con ID: {}", id);
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(movimiento);

        } catch (Exception e) {
            logger.error("Error al obtener movimiento {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener movimientos de un producto específico
     * GET /api/inventario/productos/{id}/movimientos
     */
    @GetMapping("/productos/{id}/movimientos")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Page<MovimientoInventarioDTO>> obtenerMovimientosProducto(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            logger.info("Solicitando movimientos del producto ID: {}", id);

            // Verificar que el producto existe
            ProductoDTO producto = productoServicio.buscarPorId(id);
            if (producto == null) {
                logger.warn("Producto no encontrado con ID: {}", id);
                return ResponseEntity.notFound().build();
            }

            // Configurar paginación
            Pageable pageable = PageRequest.of(page, size, Sort.by("fecha").descending());

            // Obtener movimientos
            Page<MovimientoInventarioDTO> movimientos = inventarioServicio.buscarPorProducto(id, pageable);

            logger.info("Movimientos del producto {} obtenidos exitosamente: {} resultados",
                    id, movimientos.getTotalElements());
            return ResponseEntity.ok(movimientos);

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Producto no encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("Error al obtener movimientos del producto {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener últimos movimientos de un producto
     * GET /api/inventario/productos/{id}/movimientos/ultimos
     */
    @GetMapping("/productos/{id}/movimientos/ultimos")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
    public ResponseEntity<List<MovimientoInventarioDTO>> obtenerUltimosMovimientos(
            @PathVariable Long id,
            @RequestParam(defaultValue = "10") int limite) {

        try {
            logger.info("Solicitando últimos {} movimientos del producto ID: {}", limite, id);

            // Verificar que el producto existe
            ProductoDTO producto = productoServicio.buscarPorId(id);
            if (producto == null) {
                logger.warn("Producto no encontrado con ID: {}", id);
                return ResponseEntity.notFound().build();
            }

            // Obtener últimos movimientos
            List<MovimientoInventarioDTO> movimientos = inventarioServicio.obtenerUltimosMovimientos(id, limite);

            logger.info("Últimos movimientos del producto {} obtenidos exitosamente: {} resultados",
                    id, movimientos.size());
            return ResponseEntity.ok(movimientos);

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Producto no encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("Error al obtener últimos movimientos del producto {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== ENDPOINTS DE AJUSTES ====================

    /**
     * Registrar un ajuste de inventario
     * POST /api/inventario/ajustes
     */
    @PostMapping("/ajustes")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Object> registrarAjuste(@Valid @RequestBody AjusteInventarioRequestDTO ajusteRequest) {
        try {
            logger.info("Registrando ajuste de inventario - Producto ID: {}, Cantidad: {}",
                    ajusteRequest.getProductoId(), ajusteRequest.getCantidad());

            // Obtener usuario actual
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuarioActual = usuarioServicio.buscarPorUsername(auth.getName());

            if (usuarioActual == null) {
                logger.error("No se pudo identificar el usuario actual");
                Map<String, String> error = new HashMap<>();
                error.put("error", "No se pudo identificar el usuario actual");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // Verificar que el producto existe
            ProductoDTO producto = productoServicio.buscarPorId(ajusteRequest.getProductoId());
            if (producto == null) {
                logger.warn("Producto no encontrado con ID: {}", ajusteRequest.getProductoId());
                Map<String, String> error = new HashMap<>();
                error.put("error", "Producto no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // Validar stock disponible para ajustes negativos
            if (ajusteRequest.getCantidad() < 0) {
                Integer stockActual = inventarioServicio.obtenerStockActual(ajusteRequest.getProductoId());
                if (stockActual + ajusteRequest.getCantidad() < 0) {
                    logger.warn("Stock insuficiente para ajuste - Stock actual: {}, Intento de restar: {}",
                            stockActual, Math.abs(ajusteRequest.getCantidad()));
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "Stock insuficiente");
                    error.put("stockActual", stockActual);
                    error.put("cantidadSolicitada", Math.abs(ajusteRequest.getCantidad()));
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }

            // Registrar el ajuste
            MovimientoInventarioDTO movimiento = inventarioServicio.registrarAjuste(
                    ajusteRequest.getProductoId(),
                    ajusteRequest.getCantidad(),
                    ajusteRequest.getMotivo(),
                    usuarioActual.getId()
            );

            logger.info("Ajuste registrado exitosamente - Movimiento ID: {}, Stock nuevo: {}",
                    movimiento.getId(), movimiento.getStockNuevo());

            return ResponseEntity.status(HttpStatus.CREATED).body(movimiento);

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Recurso no encontrado al registrar ajuste: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

        } catch (IllegalStateException e) {
            logger.error("Error de estado al registrar ajuste: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        } catch (Exception e) {
            logger.error("Error al registrar ajuste: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al registrar el ajuste: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ==================== ENDPOINTS DE ALERTAS ====================

    /**
     * Obtener productos con stock bajo
     * GET /api/inventario/alertas/stock-bajo
     */
    @GetMapping("/alertas/stock-bajo")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
    public ResponseEntity<List<EstadisticasInventarioDTO.ProductoStockDTO>> obtenerProductosStockBajo() {
        try {
            logger.info("Solicitando productos con stock bajo");

            List<EstadisticasInventarioDTO.ProductoStockDTO> productos =
                    inventarioServicio.obtenerProductosStockBajo();

            logger.info("Productos con stock bajo obtenidos: {} productos", productos.size());
            return ResponseEntity.ok(productos);

        } catch (Exception e) {
            logger.error("Error al obtener productos con stock bajo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener productos sin stock
     * GET /api/inventario/alertas/sin-stock
     */
    @GetMapping("/alertas/sin-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
    public ResponseEntity<List<EstadisticasInventarioDTO.ProductoStockDTO>> obtenerProductosSinStock() {
        try {
            logger.info("Solicitando productos sin stock");

            List<EstadisticasInventarioDTO.ProductoStockDTO> productos =
                    inventarioServicio.obtenerProductosSinStock();

            logger.info("Productos sin stock obtenidos: {} productos", productos.size());
            return ResponseEntity.ok(productos);

        } catch (Exception e) {
            logger.error("Error al obtener productos sin stock: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== ENDPOINTS DE VERIFICACIÓN ====================

    /**
     * Verificar disponibilidad de stock
     * GET /api/inventario/productos/{id}/stock/verificar
     */
    @GetMapping("/productos/{id}/stock/verificar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
    public ResponseEntity<Map<String, Object>> verificarStockDisponible(
            @PathVariable Long id,
            @RequestParam Integer cantidad) {

        try {
            logger.info("Verificando stock disponible - Producto ID: {}, Cantidad solicitada: {}", id, cantidad);

            // Verificar que el producto existe
            ProductoDTO producto = productoServicio.buscarPorId(id);
            if (producto == null) {
                logger.warn("Producto no encontrado con ID: {}", id);
                return ResponseEntity.notFound().build();
            }

            // Obtener stock actual
            Integer stockActual = inventarioServicio.obtenerStockActual(id);

            // Verificar disponibilidad
            boolean disponible = inventarioServicio.verificarStockDisponible(id, cantidad);

            // Preparar respuesta
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("productoId", id);
            respuesta.put("productoNombre", producto.getNombre());
            respuesta.put("stockActual", stockActual);
            respuesta.put("cantidadSolicitada", cantidad);
            respuesta.put("disponible", disponible);
            respuesta.put("faltante", disponible ? 0 : cantidad - stockActual);

            logger.info("Verificación de stock completada - Producto: {}, Disponible: {}",
                    producto.getNombre(), disponible);

            return ResponseEntity.ok(respuesta);

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Producto no encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("Error al verificar stock: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== ENDPOINTS DE UTILIDAD ====================

    /**
     * Obtener resumen del inventario
     * GET /api/inventario/resumen
     */
    @GetMapping("/resumen")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Map<String, Object>> obtenerResumen() {
        try {
            logger.info("Solicitando resumen del inventario");

            EstadisticasInventarioDTO estadisticas = inventarioServicio.obtenerEstadisticas();

            Map<String, Object> resumen = new HashMap<>();
            resumen.put("totalProductos", estadisticas.getTotalProductos());
            resumen.put("productosActivos", estadisticas.getProductosActivos());
            resumen.put("totalUnidadesStock", estadisticas.getTotalUnidadesStock());
            resumen.put("valorTotalInventario", estadisticas.getValorTotalInventario());
            resumen.put("productosStockBajo", estadisticas.getProductosStockBajo());
            resumen.put("productosSinStock", estadisticas.getProductosSinStock());
            resumen.put("movimientosHoy", estadisticas.getMovimientosHoy());
            resumen.put("movimientosMes", estadisticas.getMovimientosMes());
            resumen.put("fechaGeneracion", estadisticas.getFechaGeneracion());

            logger.info("Resumen del inventario generado exitosamente");
            return ResponseEntity.ok(resumen);

        } catch (Exception e) {
            logger.error("Error al obtener resumen del inventario: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Contar movimientos totales
     * GET /api/inventario/movimientos/contar
     */
    @GetMapping("/movimientos/contar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Map<String, Long>> contarMovimientos(
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        try {
            long total;

            if (productoId != null) {
                // Contar movimientos de un producto específico
                total = inventarioServicio.contarMovimientosProducto(productoId);
            } else if (fechaInicio != null && fechaFin != null) {
                // Contar movimientos en un período
                LocalDateTime fechaInicioDateTime = fechaInicio.atStartOfDay();
                LocalDateTime fechaFinDateTime = fechaFin.atTime(23, 59, 59);
                total = inventarioServicio.contarMovimientosPeriodo(fechaInicioDateTime, fechaFinDateTime);
            } else {
                // Contar todos los movimientos
                total = inventarioServicio.contarMovimientos();
            }

            Map<String, Long> respuesta = new HashMap<>();
            respuesta.put("total", total);

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            logger.error("Error al contar movimientos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
