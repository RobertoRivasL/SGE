package informviva.gest.controlador;

/**
 * @author Roberto Rivas
 * @version 2.0
 */

import informviva.gest.dto.ValidacionResponseDTO;
import informviva.gest.service.ValidacionServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controlador REST para validaciones del sistema
 * Centraliza todas las validaciones de negocio
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@RestController
@RequestMapping("/api/validaciones")
public class ValidacionControlador {

    private static final Logger logger = LoggerFactory.getLogger(ValidacionControlador.class);

    private final ValidacionServicio validacionServicio;

    @Autowired
    public ValidacionControlador(ValidacionServicio validacionServicio) {
        this.validacionServicio = validacionServicio;
    }

    /**
     * Valida si un RUT chileno es correcto
     */
    @GetMapping("/rut")
    public ResponseEntity<ValidacionResponseDTO> validarRut(@RequestParam String rut) {
        try {
            boolean esValido = validacionServicio.validarRut(rut);
            String rutFormateado = esValido ? validacionServicio.formatearRut(rut) : rut;

            ValidacionResponseDTO response = new ValidacionResponseDTO(
                    esValido,
                    esValido ? "RUT válido" : "RUT inválido",
                    rutFormateado,
                    LocalDateTime.now()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error validando RUT: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ValidacionResponseDTO(false, "Error al validar RUT: " + e.getMessage(), rut, LocalDateTime.now())
            );
        }
    }

    /**
     * Valida si un código de producto es único
     */
    @GetMapping("/producto/codigo")
    public ResponseEntity<ValidacionResponseDTO> validarCodigoProducto(
            @RequestParam String codigo,
            @RequestParam(required = false) Long excludeId) {
        try {
            boolean esUnico = validacionServicio.validarCodigoProductoUnico(codigo, excludeId);

            ValidacionResponseDTO response = new ValidacionResponseDTO(
                    esUnico,
                    esUnico ? "Código disponible" : "El código ya está en uso",
                    codigo.toUpperCase().trim(),
                    LocalDateTime.now()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error validando código de producto: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ValidacionResponseDTO(false, "Error al validar código", codigo, LocalDateTime.now())
            );
        }
    }

    /**
     * Valida si un email es único
     */
    @GetMapping("/email")
    public ResponseEntity<ValidacionResponseDTO> validarEmail(
            @RequestParam String email,
            @RequestParam(required = false) Long excludeId) {
        try {
            boolean esValido = validacionServicio.validarFormatoEmail(email);
            if (!esValido) {
                return ResponseEntity.ok(
                        new ValidacionResponseDTO(false, "Formato de email inválido", email, LocalDateTime.now())
                );
            }

            boolean esUnico = validacionServicio.validarEmailUnico(email, excludeId);

            ValidacionResponseDTO response = new ValidacionResponseDTO(
                    esUnico,
                    esUnico ? "Email disponible" : "El email ya está registrado",
                    email.toLowerCase().trim(),
                    LocalDateTime.now()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error validando email: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ValidacionResponseDTO(false, "Error al validar email", email, LocalDateTime.now())
            );
        }
    }

    /**
     * Valida si un username es único
     */
    @GetMapping("/usuario/username")
    public ResponseEntity<ValidacionResponseDTO> validarUsername(
            @RequestParam String username,
            @RequestParam(required = false) Long excludeId) {
        try {
            boolean esUnico = validacionServicio.validarUsernameUnico(username, excludeId);

            ValidacionResponseDTO response = new ValidacionResponseDTO(
                    esUnico,
                    esUnico ? "Username disponible" : "El username ya está en uso",
                    username.toLowerCase().trim(),
                    LocalDateTime.now()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error validando username: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ValidacionResponseDTO(false, "Error al validar username", username, LocalDateTime.now())
            );
        }
    }

    /**
     * Valida stock suficiente para una venta
     */
    @PostMapping("/stock")
    public ResponseEntity<ValidacionResponseDTO> validarStock(@RequestBody Map<String, Object> stockData) {
        try {
            Long productoId = Long.valueOf(stockData.get("productoId").toString());
            Integer cantidadSolicitada = Integer.valueOf(stockData.get("cantidad").toString());

            boolean stockSuficiente = validacionServicio.validarStockSuficiente(productoId, cantidadSolicitada);

            String mensaje = stockSuficiente
                    ? "Stock suficiente"
                    : "Stock insuficiente. Disponible: " + validacionServicio.obtenerStockDisponible(productoId);

            ValidacionResponseDTO response = new ValidacionResponseDTO(
                    stockSuficiente,
                    mensaje,
                    stockData.toString(),
                    LocalDateTime.now()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error validando stock: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ValidacionResponseDTO(false, "Error al validar stock", "", LocalDateTime.now())
            );
        }
    }

    /**
     * Valida si se puede eliminar un cliente (no tiene ventas asociadas)
     */
    @GetMapping("/cliente/eliminacion/{id}")
    public ResponseEntity<ValidacionResponseDTO> validarEliminacionCliente(@PathVariable Long id) {
        try {
            boolean puedeEliminar = validacionServicio.validarEliminacionCliente(id);

            String mensaje = puedeEliminar
                    ? "Cliente puede ser eliminado"
                    : "No se puede eliminar el cliente porque tiene ventas registradas";

            ValidacionResponseDTO response = new ValidacionResponseDTO(
                    puedeEliminar,
                    mensaje,
                    id.toString(),
                    LocalDateTime.now()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error validando eliminación de cliente: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ValidacionResponseDTO(false, "Error al validar eliminación", id.toString(), LocalDateTime.now())
            );
        }
    }

    /**
     * Valida si se puede eliminar un producto (no tiene ventas asociadas)
     */
    @GetMapping("/producto/eliminacion/{id}")
    public ResponseEntity<ValidacionResponseDTO> validarEliminacionProducto(@PathVariable Long id) {
        try {
            boolean puedeEliminar = validacionServicio.validarEliminacionProducto(id);

            String mensaje = puedeEliminar
                    ? "Producto puede ser eliminado"
                    : "No se puede eliminar el producto porque tiene ventas registradas";

            ValidacionResponseDTO response = new ValidacionResponseDTO(
                    puedeEliminar,
                    mensaje,
                    id.toString(),
                    LocalDateTime.now()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error validando eliminación de producto: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ValidacionResponseDTO(false, "Error al validar eliminación", id.toString(), LocalDateTime.now())
            );
        }
    }

    /**
     * Valida rangos de fechas para reportes
     */
    @GetMapping("/fechas/rango")
    public ResponseEntity<ValidacionResponseDTO> validarRangoFechas(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        try {
            boolean rangoValido = validacionServicio.validarRangoFechas(fechaInicio, fechaFin);

            String mensaje = rangoValido
                    ? "Rango de fechas válido"
                    : "Rango de fechas inválido. La fecha de inicio debe ser anterior a la fecha fin";

            ValidacionResponseDTO response = new ValidacionResponseDTO(
                    rangoValido,
                    mensaje,
                    fechaInicio + " - " + fechaFin,
                    LocalDateTime.now()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error validando rango de fechas: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ValidacionResponseDTO(false, "Error al validar fechas", fechaInicio + " - " + fechaFin, LocalDateTime.now())
            );
        }
    }

    /**
     * Validación integral de datos de venta antes de procesarla
     */
    @PostMapping("/venta/completa")
    public ResponseEntity<Map<String, Object>> validarVentaCompleta(@RequestBody Map<String, Object> ventaData) {
        try {
            Map<String, Object> resultadoValidacion = validacionServicio.validarVentaCompleta(ventaData);
            return ResponseEntity.ok(resultadoValidacion);
        } catch (Exception e) {
            logger.error("Error en validación completa de venta: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "valido", false,
                            "mensaje", "Error en validación: " + e.getMessage(),
                            "timestamp", LocalDateTime.now()
                    )
            );
        }
    }
}