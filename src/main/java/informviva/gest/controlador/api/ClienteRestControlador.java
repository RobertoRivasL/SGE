package informviva.gest.controlador.api;



import informviva.gest.dto.ClienteDTO;
import informviva.gest.dto.ResultadoValidacionDTO;
import informviva.gest.model.Cliente;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

            Page<Cliente> clientesPage = aplicarFiltros(search, ciudad, soloActivos, pageable);

            Page<ClienteDTO> clientesDTO = clientesPage.map(this::convertirADTO);

            return ResponseEntity.ok(clientesDTO);

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
            Cliente cliente = clienteServicio.buscarPorId(id);

            if (cliente == null) {
                return ResponseEntity.notFound().build();
            }

            ClienteDTO clienteDTO = convertirADTODetallado(cliente);
            return ResponseEntity.ok(clienteDTO);

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
            List<Cliente> clientes = clienteServicio.buscarPorTermino(termino, limite);

            List<ClienteDTO> clientesDTO = clientes.stream()
                    .map(this::convertirADTOSimple)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(clientesDTO);

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
            List<Cliente> clientes = clienteServicio.obtenerActivos();

            List<ClienteDTO> clientesDTO = clientes.stream()
                    .map(this::convertirADTOSimple)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(clientesDTO);

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
            if (!clienteServicio.rutEsValido(clienteDTO.getRut())) {
                return ResponseEntity.badRequest().build();
            }

            // Verificar email duplicado
            if (clienteServicio.existeEmail(clienteDTO.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            Cliente cliente = convertirDTOAEntidad(clienteDTO);
            cliente.setFechaRegistro(LocalDateTime.now());
            cliente.setActivo(true);

            Cliente clienteGuardado = clienteServicio.guardar(cliente);
            ClienteDTO respuestaDTO = convertirADTODetallado(clienteGuardado);

            logger.info("Cliente creado exitosamente: {}", clienteGuardado.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(respuestaDTO);

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
            Cliente clienteExistente = clienteServicio.buscarPorId(id);

            if (clienteExistente == null) {
                return ResponseEntity.notFound().build();
            }

            // Validar RUT
            if (!clienteServicio.rutEsValido(clienteDTO.getRut())) {
                return ResponseEntity.badRequest().build();
            }

            // Verificar email duplicado en otros clientes
            if (clienteServicio.existeEmailOtroCliente(clienteDTO.getEmail(), id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            // Actualizar campos
            actualizarDatosCliente(clienteExistente, clienteDTO);
            clienteExistente.setFechaModificacion(LocalDateTime.now());

            Cliente clienteActualizado = clienteServicio.guardar(clienteExistente);
            ClienteDTO respuestaDTO = convertirADTODetallado(clienteActualizado);

            logger.info("Cliente actualizado exitosamente: {}", id);
            return ResponseEntity.ok(respuestaDTO);

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
            Cliente cliente = clienteServicio.buscarPorId(id);

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
            boolean esValido = clienteServicio.rutEsValido(rut);

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
            estadisticas.put("clientesNuevosMes", clienteServicio.contarNuevosMes());
            estadisticas.put("ciudadesRepresentadas", clienteServicio.contarCiudades());

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
    private Page<Cliente> aplicarFiltros(String search, String ciudad, Boolean soloActivos, Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            if (ciudad != null && !ciudad.trim().isEmpty()) {
                if (soloActivos != null && soloActivos) {
                    return clienteServicio.buscarPorTerminoYCiudadYActivos(search.trim(), ciudad.trim(), pageable);
                } else {
                    return clienteServicio.buscarPorTerminoYCiudad(search.trim(), ciudad.trim(), pageable);
                }
            } else {
                if (soloActivos != null && soloActivos) {
                    return clienteServicio.buscarPorTerminoYActivos(search.trim(), pageable);
                } else {
                    return clienteServicio.buscarPorTermino(search.trim(), pageable);
                }
            }
        } else if (ciudad != null && !ciudad.trim().isEmpty()) {
            if (soloActivos != null && soloActivos) {
                return clienteServicio.buscarPorCiudadYActivos(ciudad.trim(), pageable);
            } else {
                return clienteServicio.buscarPorCiudad(ciudad.trim(), pageable);
            }
        } else if (soloActivos != null && soloActivos) {
            return clienteServicio.obtenerActivosPaginados(pageable);
        } else {
            return clienteServicio.obtenerTodosPaginados(pageable);
        }
    }

    /**
     * Convierte Cliente a ClienteDTO simple (para listas)
     */
    private ClienteDTO convertirADTOSimple(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getId());
        dto.setNombre(cliente.getNombre());
        dto.setApellido(cliente.getApellido());
        dto.setEmail(cliente.getEmail());
        dto.setRut(cliente.getRut());
        dto.setTelefono(cliente.getTelefono());
        dto.setActivo(cliente.isActivo());
        return dto;
    }

    /**
     * Convierte Cliente a ClienteDTO detallado (para vista individual)
     */
    private ClienteDTO convertirADTODetallado(Cliente cliente) {
        ClienteDTO dto = convertirADTOSimple(cliente);
        dto.setDireccion(cliente.getDireccion());
        dto.setCiudad(cliente.getCiudad());
        dto.setFechaRegistro(cliente.getFechaRegistro());
        dto.setFechaModificacion(cliente.getFechaModificacion());
        dto.setFechaUltimaCompra(cliente.getFechaUltimaCompra());
        dto.setTotalCompras(cliente.getTotalCompras());
        dto.setNumeroCompras(cliente.getNumeroCompras());
        return dto;
    }

    /**
     * Convierte ClienteDTO a Cliente
     */
    private Cliente convertirDTOAEntidad(ClienteDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setId(dto.getId());
        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setEmail(dto.getEmail());
        cliente.setRut(dto.getRut());
        cliente.setTelefono(dto.getTelefono());
        cliente.setDireccion(dto.getDireccion());
        cliente.setCiudad(dto.getCiudad());
        cliente.setActivo(dto.isActivo());
        return cliente;
    }

    /**
     * Actualiza los datos de un cliente existente con los del DTO
     */
    private void actualizarDatosCliente(Cliente cliente, ClienteDTO dto) {
        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setEmail(dto.getEmail());
        cliente.setRut(dto.getRut());
        cliente.setTelefono(dto.getTelefono());
        cliente.setDireccion(dto.getDireccion());
        cliente.setCiudad(dto.getCiudad());
        cliente.setActivo(dto.isActivo());
    }

    /**
     * Convierte Page<Cliente> a ClienteDTO
     */
    private ClienteDTO convertirADTO(Cliente cliente) {
        return convertirADTOSimple(cliente);
    }
}