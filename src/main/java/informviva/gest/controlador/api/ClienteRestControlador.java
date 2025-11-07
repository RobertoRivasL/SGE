package informviva.gest.controlador.api;



import informviva.gest.dto.ClienteDTO;
import informviva.gest.dto.ResultadoValidacionDTO;
import informviva.gest.service.ClienteServicio;
import informviva.gest.service.VentaServicio;
import informviva.gest.util.MensajesConstantes;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API REST para gestión de clientes
 * Endpoints para integración con aplicaciones externas y funcionalidades AJAX
 *
 * Siguiendo el patrón arquitectónico del VentaRestControlador
 *
 * @author Roberto
 * @version 2.1
 * @since FASE 2 - Estandarización Arquitectónica
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteRestControlador {

    private static final Logger logger = LoggerFactory.getLogger(ClienteRestControlador.class);

    private final ClienteServicio clienteServicio;
    private final VentaServicio ventaServicio;

    public ClienteRestControlador(ClienteServicio clienteServicio, VentaServicio ventaServicio) {
        this.clienteServicio = clienteServicio;
        this.ventaServicio = ventaServicio;
    }

    // ========== ENDPOINTS DE CONSULTA ==========

    /**
     * Obtener todos los clientes con paginación
     * GET /api/clientes
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
    public ResponseEntity<Page<ClienteDTO>> obtenerClientes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nombre") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) Boolean soloActivos) {

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);

            Page<ClienteDTO> clientesPage = aplicarFiltros(search, ciudad, soloActivos, pageable);

            return ResponseEntity.ok(clientesPage);

        } catch (Exception e) {
            logger.error("Error obteniendo clientes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener cliente por ID
     * GET /api/clientes/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
    public ResponseEntity<ClienteDTO> obtenerClientePorId(@PathVariable Long id) {
        try {
            ClienteDTO cliente = clienteServicio.buscarPorId(id);

            if (cliente == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(cliente);

        } catch (Exception e) {
            logger.error("Error obteniendo cliente {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Buscar clientes para autocompletado
     * GET /api/clientes/buscar
     */
    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
    public ResponseEntity<List<ClienteDTO>> buscarClientes(
            @RequestParam String termino,
            @RequestParam(defaultValue = "10") int limite) {

        try {
            List<ClienteDTO> clientes = clienteServicio.buscarPorTermino(termino, limite);

            return ResponseEntity.ok(clientes);

        } catch (Exception e) {
            logger.error("Error buscando clientes con término '{}': {}", termino, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener clientes activos para formularios de venta
     * GET /api/clientes/activos
     */
    @GetMapping("/activos")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
    public ResponseEntity<List<ClienteDTO>> obtenerClientesActivos() {
        try {
            List<ClienteDTO> clientes = clienteServicio.buscarActivos();

            return ResponseEntity.ok(clientes);

        } catch (Exception e) {
            logger.error("Error obteniendo clientes activos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== ENDPOINTS DE MODIFICACIÓN ==========

    /**
     * Crear nuevo cliente
     * POST /api/clientes
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
    public ResponseEntity<ClienteDTO> crearCliente(@Valid @RequestBody ClienteDTO clienteDTO) {
        try {
            // Validar RUT
            if (!clienteServicio.esRutValido(clienteDTO.getRut())) {
                return ResponseEntity.badRequest().build();
            }

            // Verificar email duplicado
            if (clienteServicio.existeEmail(clienteDTO.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            // Las fechas y estado deben ser manejados por el servicio
            ClienteDTO clienteGuardado = clienteServicio.guardar(clienteDTO);

            logger.info("Cliente creado exitosamente: {}", clienteGuardado.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(clienteGuardado);

        } catch (Exception e) {
            logger.error("Error creando cliente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Actualizar cliente existente
     * PUT /api/clientes/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
    public ResponseEntity<ClienteDTO> actualizarCliente(
            @PathVariable Long id,
            @Valid @RequestBody ClienteDTO clienteDTO) {

        try {
            ClienteDTO clienteExistente = clienteServicio.buscarPorId(id);

            if (clienteExistente == null) {
                return ResponseEntity.notFound().build();
            }

            // Validar RUT
            if (!clienteServicio.esRutValido(clienteDTO.getRut())) {
                return ResponseEntity.badRequest().build();
            }

            // Verificar email duplicado en otros clientes
            if (clienteServicio.existeEmailOtroCliente(clienteDTO.getEmail(), id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            // Usar el método actualizar del servicio que maneja fechas internamente
            ClienteDTO clienteActualizado = clienteServicio.actualizar(id, clienteDTO);

            logger.info("Cliente actualizado exitosamente: {}", id);
            return ResponseEntity.ok(clienteActualizado);

        } catch (Exception e) {
            logger.error("Error actualizando cliente {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Eliminar cliente
     * DELETE /api/clientes/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> eliminarCliente(@PathVariable Long id) {
        try {
            ClienteDTO cliente = clienteServicio.buscarPorId(id);

            if (cliente == null) {
                return ResponseEntity.notFound().build();
            }

            // Verificar si tiene ventas asociadas
            if (ventaServicio.existenVentasPorCliente(id)) {
                Map<String, String> respuesta = new HashMap<>();
                respuesta.put("error", MensajesConstantes.ERROR_CLIENTE_CON_VENTAS);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(respuesta);
            }

            clienteServicio.eliminar(id);

            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("mensaje", MensajesConstantes.EXITO_CLIENTE_ELIMINADO);

            logger.info("Cliente eliminado exitosamente: {}", id);
            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            logger.error("Error eliminando cliente {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== ENDPOINTS DE VALIDACIÓN ==========

    /**
     * Validar RUT de cliente
     * POST /api/clientes/validar-rut
     */
    @PostMapping("/validar-rut")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
    public ResponseEntity<ResultadoValidacionDTO> validarRUT(@RequestBody Map<String, String> datos) {
        try {
            String rut = datos.get("rut");
            boolean esValido = clienteServicio.esRutValido(rut);

            ResultadoValidacionDTO resultado = new ResultadoValidacionDTO();
            resultado.setValido(esValido);
            resultado.setMensaje(esValido ? "RUT válido" : "RUT inválido");

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            logger.error("Error validando RUT: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Validar email único
     * POST /api/clientes/validar-email
     */
    @PostMapping("/validar-email")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENTAS')")
    public ResponseEntity<ResultadoValidacionDTO> validarEmail(@RequestBody Map<String, Object> datos) {
        try {
            String email = (String) datos.get("email");
            Long clienteId = datos.get("clienteId") != null ?
                    Long.valueOf(datos.get("clienteId").toString()) : null;

            boolean esUnico = clienteId == null ?
                    !clienteServicio.existeEmail(email) :
                    !clienteServicio.existeEmailOtroCliente(email, clienteId);

            ResultadoValidacionDTO resultado = new ResultadoValidacionDTO();
            resultado.setValido(esUnico);
            resultado.setMensaje(esUnico ? "Email disponible" : "Email ya registrado");

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            logger.error("Error validando email: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== ENDPOINTS DE ESTADÍSTICAS ==========

    /**
     * Obtener estadísticas de clientes
     * GET /api/clientes/estadisticas
     */
    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        try {
            Map<String, Object> estadisticas = new HashMap<>();

            estadisticas.put("totalClientes", clienteServicio.contarTodos());
            estadisticas.put("clientesActivos", clienteServicio.contarActivos());
            estadisticas.put("clientesInactivos", clienteServicio.contarInactivos());
            estadisticas.put("clientesNuevosHoy", clienteServicio.contarNuevosHoy());
            // TODO: Implementar contarNuevosMes() en ClienteServicio
            // estadisticas.put("clientesNuevosMes", clienteServicio.contarNuevosMes());
            // TODO: Implementar contarCiudades() en ClienteServicio
            // estadisticas.put("ciudadesRepresentadas", clienteServicio.contarCiudades());

            return ResponseEntity.ok(estadisticas);

        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas de clientes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Aplica filtros de búsqueda
     */
    private Page<ClienteDTO> aplicarFiltros(String search, String ciudad, Boolean soloActivos, Pageable pageable) {
        boolean haySearch = search != null && !search.trim().isEmpty();
        boolean hayCiudad = ciudad != null && !ciudad.trim().isEmpty();
        boolean activos = Boolean.TRUE.equals(soloActivos);

        if (haySearch && hayCiudad && activos) {
            return clienteServicio.buscarPorTerminoYCiudadYActivos(search.trim(), ciudad.trim(), pageable);
        }
        if (haySearch && hayCiudad) {
            return clienteServicio.buscarPorTerminoYCiudad(search.trim(), ciudad.trim(), pageable);
        }
        if (haySearch && activos) {
            return clienteServicio.buscarPorTerminoYActivos(search.trim(), pageable);
        }
        if (haySearch) {
            return clienteServicio.buscarPorTermino(search.trim(), pageable);
        }
        if (hayCiudad && activos) {
            return clienteServicio.buscarPorCiudadYActivos(ciudad.trim(), pageable);
        }
        if (hayCiudad) {
            return clienteServicio.buscarPorCiudad(ciudad.trim(), pageable);
        }
        if (activos) {
            return clienteServicio.buscarActivos(pageable);
        }
        return clienteServicio.buscarTodos(pageable);
    }
}