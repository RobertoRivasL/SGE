package informviva.gest.controlador;

/**
 * @author Roberto Rivas
 * @version 2.0
 */


import informviva.gest.controlador.api.VentaRestControlador;
import informviva.gest.dto.VentaDTO;
import informviva.gest.model.Cliente;
import informviva.gest.model.Producto;
import informviva.gest.service.ClienteServicio;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.VentaServicio;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controlador para mantener compatibilidad con las rutas API antiguas
 *
 * @author Roberto Rivas
 * @version 1.0
 * @deprecated Este controlador completo será eliminado después del 01/12/2025.
 * Todas las aplicaciones cliente deben migrar a las nuevas rutas en /api/ventas/.
 * Esta clase existe solo para mantener la compatibilidad con código existente.
 */
@RestController
@RequestMapping("/ventas/api")
@Deprecated(since = "2.2", forRemoval = true)
public class VentaApiCompatibilidadControlador {

    private static final Logger logger = LoggerFactory.getLogger(VentaApiCompatibilidadControlador.class);

    // Constantes para mensajes de log
    private static final String RUTA_DEPRECADA_BASE = "Acceso a ruta deprecada /ventas/api";
    private static final String USAR_NUEVA_RUTA_BASE = ", usar /api/ventas";

    private final VentaRestControlador ventaRestControlador;

    public VentaApiCompatibilidadControlador(VentaServicio ventaServicio,
                                             ProductoServicio productoServicio,
                                             ClienteServicio clienteServicio) {
        // Reutilizamos el controlador REST nuevo a través de delegación
        this.ventaRestControlador = new VentaRestControlador(ventaServicio, productoServicio, clienteServicio);
    }

    /**
     * @deprecated Usar /api/ventas/productos en su lugar.
     */
    @Deprecated(since = "2.2", forRemoval = true)
    @GetMapping("/productos")
    public List<Producto> obtenerProductosConStock() {
        logger.info(RUTA_DEPRECADA_BASE + "/productos" + USAR_NUEVA_RUTA_BASE + "/productos en su lugar");
        return ventaRestControlador.obtenerProductosConStock();
    }

    /**
     * @deprecated Usar /api/ventas/productos/{id} en su lugar.
     */
    @Deprecated(since = "2.2", forRemoval = true)
    @GetMapping("/productos/{id}")
    public ResponseEntity<Producto> obtenerProductoPorId(@PathVariable Long id) {
        logger.info(RUTA_DEPRECADA_BASE + "/productos/{id}" + USAR_NUEVA_RUTA_BASE + "/productos/{id} en su lugar");
        return ventaRestControlador.obtenerProductoPorId(id);
    }

    /**
     * @deprecated Usar /api/ventas/clientes/{id} en su lugar.
     */
    @Deprecated(since = "2.2", forRemoval = true)
    @GetMapping("/clientes/{id}")
    public ResponseEntity<Cliente> obtenerClientePorId(@PathVariable Long id) {
        logger.info(RUTA_DEPRECADA_BASE + "/clientes/{id}" + USAR_NUEVA_RUTA_BASE + "/clientes/{id} en su lugar");
        return ventaRestControlador.obtenerClientePorId(id);
    }

    /**
     * @deprecated Usar /api/ventas en su lugar.
     */
    @Deprecated(since = "2.2", forRemoval = true)
    @PostMapping
    public ResponseEntity<Object> crearVenta(@Valid @RequestBody VentaDTO ventaDTO) {
        logger.info(RUTA_DEPRECADA_BASE + " (POST)" + USAR_NUEVA_RUTA_BASE + " en su lugar");
        return ventaRestControlador.crearVenta(ventaDTO);
    }

    /**
     * @deprecated Usar /api/ventas en su lugar.
     */
    @Deprecated(since = "2.2", forRemoval = true)
    @GetMapping
    public List<VentaDTO> listarVentas() {
        logger.info(RUTA_DEPRECADA_BASE + USAR_NUEVA_RUTA_BASE + " en su lugar");
        LocalDate startDate = LocalDate.now().withDayOfMonth(1); // Primer día del mes actual
        LocalDate endDate = LocalDate.now(); // Fecha actual

        // Llamar al método y extraer el cuerpo de la respuesta
        ResponseEntity<List<Map<String, Object>>> response = ventaRestControlador.listarVentas(startDate, endDate);
        List<Map<String, Object>> ventasSimplificadas = response.getBody();

        // Convertir la lista de mapas a una lista de VentaDTO
        return ventasSimplificadas.stream()
                .map(this::convertirMapaAVentaDTO)
                .toList();
    }

    // Método auxiliar para convertir un mapa a VentaDTO
    private VentaDTO convertirMapaAVentaDTO(Map<String, Object> mapa) {
        VentaDTO ventaDTO = new VentaDTO();
        ventaDTO.setId((Long) mapa.get("id"));
        ventaDTO.setFecha(LocalDateTime.parse((String) mapa.get("fecha"))); // Conversión de String a LocalDateTime
        ventaDTO.setCliente((String) mapa.get("cliente"));
        ventaDTO.setVendedor((String) mapa.get("vendedor"));
        ventaDTO.setProducto((String) mapa.get("producto"));
        ventaDTO.setCantidad((Integer) mapa.get("cantidad"));
        ventaDTO.setPrecioUnitario((Double) mapa.get("precioUnitario"));
        ventaDTO.setTotal((Double) mapa.get("total"));
        ventaDTO.setEstado((String) mapa.get("estado"));
        return ventaDTO;
    }

    /**
     * @deprecated Usar /api/ventas/{id} en su lugar.
     */
    @Deprecated(since = "2.2", forRemoval = true)
    @GetMapping("/{id}")
    public ResponseEntity<Object> obtenerVentaPorId(@PathVariable Long id) {
        logger.info(RUTA_DEPRECADA_BASE + "/{id}" + USAR_NUEVA_RUTA_BASE + "/{id} en su lugar");
        return ventaRestControlador.obtenerVentaPorId(id);
    }

    /**
     * @deprecated Usar /api/ventas/{id} en su lugar.
     */
    @Deprecated(since = "2.2", forRemoval = true)
    @PutMapping("/{id}")
    public ResponseEntity<Object> actualizarVenta(@PathVariable Long id, @Valid @RequestBody VentaDTO ventaDTO) {
        logger.info(RUTA_DEPRECADA_BASE + "/{id} (PUT)" + USAR_NUEVA_RUTA_BASE + "/{id} en su lugar");
        return ventaRestControlador.actualizarVenta(id, ventaDTO);
    }

    /**
     * @deprecated Usar /api/ventas/{id}/anular en su lugar.
     */
    @Deprecated(since = "2.2", forRemoval = true)
    @PutMapping("/{id}/anular")
    public ResponseEntity<Object> anularVenta(@PathVariable Long id) {
        logger.info(RUTA_DEPRECADA_BASE + "/{id}/anular" + USAR_NUEVA_RUTA_BASE + "/{id}/anular en su lugar");
        return ventaRestControlador.anularVenta(id);
    }
}