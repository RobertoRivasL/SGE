package informviva.gest.controlador.api;

import informviva.gest.dto.ClienteDTO;
import informviva.gest.dto.ProductoDTO;
import informviva.gest.dto.VentaDTO;
import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.exception.StockInsuficienteException;
import informviva.gest.service.ClienteServicio;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.VentaServicio;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador REST para la API de ventas
 *
 * REFACTORIZADO: Ahora trabaja exclusivamente con DTOs
 * Los servicios devuelven DTOs, no entidades JPA
 *
 * @author Sistema de Gestión Empresarial
 * @version 2.0 - Refactorizado Fase 1
 */
@RestController
@RequestMapping("/api/ventas")
public class VentaRestControlador {

    private static final String ERROR_CREAR = "Error al crear la venta: ";
    private static final String ERROR_ACTUALIZAR = "Error al actualizar la venta: ";
    private static final String ERROR_ANULAR = "Error al anular la venta: ";

    private final VentaServicio ventaServicio;
    private final ProductoServicio productoServicio;
    private final ClienteServicio clienteServicio;

    public VentaRestControlador(VentaServicio ventaServicio,
                                ProductoServicio productoServicio,
                                ClienteServicio clienteServicio) {
        this.ventaServicio = ventaServicio;
        this.productoServicio = productoServicio;
        this.clienteServicio = clienteServicio;
    }

    /**
     * Obtiene el resumen de ventas (summary)
     *
     * TODO: Este método requiere implementar los siguientes métodos en VentaServicio:
     * - calcularTotalVentas(LocalDateTime inicio, LocalDateTime fin)
     * - contarArticulosVendidos(LocalDateTime inicio, LocalDateTime fin)
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> obtenerResumenVentas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Si no se proporcionan fechas, usar el mes actual
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        // Calcular métricas
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // TODO: Implementar estos métodos en VentaServicio
        Long totalTransactions = ventaServicio.contarVentasPorPeriodo(startDateTime, endDateTime);

        // Temporalmente devolvemos 0 para las métricas que faltan
        Double totalAmount = 0.0; // TODO: ventaServicio.calcularTotalVentas(startDateTime, endDateTime);
        Long totalQuantity = 0L; // TODO: ventaServicio.contarArticulosVendidos(startDateTime, endDateTime);

        // Crear respuesta
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAmount", totalAmount);
        summary.put("totalTransactions", totalTransactions);
        summary.put("totalQuantity", totalQuantity);
        summary.put("startDate", startDate);
        summary.put("endDate", endDate);

        return ResponseEntity.ok(summary);
    }

    /**
     * Busca productos con stock disponible
     */
    @GetMapping("/productos")
    public List<ProductoDTO> obtenerProductosConStock() {
        return productoServicio.buscarTodos().stream()
                .filter(producto -> producto.getStock() != null && producto.getStock() > 0)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene datos de un producto por ID
     */
    @GetMapping("/productos/{id}")
    public ResponseEntity<ProductoDTO> obtenerProductoPorId(@PathVariable Long id) {
        try {
            ProductoDTO producto = productoServicio.buscarPorId(id);
            return ResponseEntity.ok(producto);
        } catch (RecursoNoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene datos de un cliente por ID
     */
    @GetMapping("/clientes/{id}")
    public ResponseEntity<ClienteDTO> obtenerClientePorId(@PathVariable Long id) {
        try {
            ClienteDTO cliente = clienteServicio.buscarPorId(id);
            return ResponseEntity.ok(cliente);
        } catch (RecursoNoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Crea una nueva venta
     */
    @PostMapping
    public ResponseEntity<Object> crearVenta(@Valid @RequestBody VentaDTO ventaDTO) {
        try {
            VentaDTO ventaCreada = ventaServicio.guardar(ventaDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(ventaCreada);
        } catch (StockInsuficienteException | RecursoNoEncontradoException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_CREAR + e.getMessage());
        }
    }

    /**
     * Obtiene todas las ventas con filtros opcionales
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listarVentas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<VentaDTO> ventas;

        // Si se proporcionan fechas, filtrar
        if (startDate != null && endDate != null) {
            ventas = ventaServicio.buscarPorRangoFechas(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        } else {
            ventas = ventaServicio.buscarTodos();
        }

        // Convertir a formato simplificado para el JavaScript
        List<Map<String, Object>> ventasSimplificadas = ventas.stream()
                .map(venta -> {
                    Map<String, Object> ventaMap = new HashMap<>();
                    ventaMap.put("id", venta.getId());
                    ventaMap.put("fecha", venta.getFecha() != null ? venta.getFecha().toLocalDate().toString() : "");
                    ventaMap.put("cliente", venta.getClienteNombre() != null ? venta.getClienteNombre() : "");
                    ventaMap.put("vendedor", venta.getVendedorNombre() != null ? venta.getVendedorNombre() : "");

                    // TODO: Los detalles de venta necesitan ser incluidos en VentaDTO
                    // Por ahora, dejamos valores por defecto
                    ventaMap.put("producto", "Ver detalle");
                    ventaMap.put("cantidad", 0);
                    ventaMap.put("precioUnitario", BigDecimal.ZERO);

                    ventaMap.put("total", venta.getTotal());
                    ventaMap.put("estado", venta.getEstado());
                    return ventaMap;
                })
                .filter(v -> !"ANULADA".equals(v.get("estado")))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ventasSimplificadas);
    }

    /**
     * Obtiene una venta por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<VentaDTO> obtenerVentaPorId(@PathVariable Long id) {
        try {
            VentaDTO venta = ventaServicio.buscarPorId(id);
            return ResponseEntity.ok(venta);
        } catch (RecursoNoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Actualiza una venta
     */
    @PutMapping("/{id}")
    public ResponseEntity<Object> actualizarVenta(@PathVariable Long id, @Valid @RequestBody VentaDTO ventaDTO) {
        try {
            VentaDTO ventaActualizada = ventaServicio.actualizar(id, ventaDTO);
            return ResponseEntity.ok(ventaActualizada);
        } catch (StockInsuficienteException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RecursoNoEncontradoException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_ACTUALIZAR + e.getMessage());
        }
    }

    /**
     * Anula una venta
     */
    @PutMapping("/{id}/anular")
    public ResponseEntity<Object> anularVenta(@PathVariable Long id,
                                              @RequestParam(required = false, defaultValue = "Anulada desde API") String motivo) {
        try {
            ventaServicio.anularVenta(id, motivo);
            VentaDTO ventaAnulada = ventaServicio.buscarPorId(id);
            return ResponseEntity.ok(ventaAnulada);
        } catch (RecursoNoEncontradoException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_ANULAR + e.getMessage());
        }
    }

    /**
     * Filtra ventas por rango de fechas
     */
    @GetMapping("/filtrar")
    public ResponseEntity<Map<String, Object>> filtrarVentas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        List<VentaDTO> ventasDTO = ventaServicio.buscarPorRangoFechas(
                fechaInicio.atStartOfDay(),
                fechaFin.atTime(23, 59, 59));

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("ventas", ventasDTO);
        respuesta.put("total", ventasDTO.size());
        respuesta.put("fechaInicio", fechaInicio);
        respuesta.put("fechaFin", fechaFin);

        return ResponseEntity.ok(respuesta);
    }
}
