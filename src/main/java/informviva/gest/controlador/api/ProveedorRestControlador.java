package informviva.gest.controlador.api;

import informviva.gest.dto.ProveedorDTO;
import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.service.ProveedorServicio;
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
 * API REST para gestión de proveedores
 * Endpoints para integración con aplicaciones externas y funcionalidades AJAX
 *
 * Siguiendo el patrón arquitectónico del ClienteRestControlador
 *
 * @author Sistema de Gestión Empresarial
 * @version 1.0
 * @since FASE 3 - Módulo de Compras
 */
@RestController
@RequestMapping("/api/proveedores")
public class ProveedorRestControlador {

    private static final Logger logger = LoggerFactory.getLogger(ProveedorRestControlador.class);

    private final ProveedorServicio proveedorServicio;

    public ProveedorRestControlador(ProveedorServicio proveedorServicio) {
        this.proveedorServicio = proveedorServicio;
    }

    // ========== ENDPOINTS DE CONSULTA ==========

    /**
     * Obtener todos los proveedores con paginación y filtros
     * GET /api/proveedores
     *
     * @param page Número de página
     * @param size Tamaño de página
     * @param sortBy Campo de ordenamiento
     * @param sortDir Dirección de ordenamiento
     * @param nombre Filtro por nombre
     * @param categoria Filtro por categoría
     * @param ciudad Filtro por ciudad
     * @param soloActivos Filtro solo activos
     * @return Página de proveedores
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COMPRAS')")
    public ResponseEntity<Page<ProveedorDTO>> obtenerProveedores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nombre") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) Boolean soloActivos) {

        try {
            logger.info("API: Obteniendo proveedores - Página: {}, Tamaño: {}", page, size);

            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);

            boolean activos = Boolean.TRUE.equals(soloActivos);
            Page<ProveedorDTO> proveedoresPage = proveedorServicio.buscarConCriterios(
                    nombre, categoria, ciudad, activos, pageable);

            logger.info("API: {} proveedores encontrados", proveedoresPage.getTotalElements());
            return ResponseEntity.ok(proveedoresPage);

        } catch (Exception e) {
            logger.error("Error obteniendo proveedores: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener proveedor por ID
     * GET /api/proveedores/{id}
     *
     * @param id ID del proveedor
     * @return Proveedor encontrado
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COMPRAS')")
    public ResponseEntity<ProveedorDTO> obtenerPorId(@PathVariable Long id) {
        try {
            logger.info("API: Obteniendo proveedor ID: {}", id);

            ProveedorDTO proveedor = proveedorServicio.buscarPorId(id);

            return ResponseEntity.ok(proveedor);

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Proveedor no encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("Error obteniendo proveedor {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener proveedores activos para formularios
     * GET /api/proveedores/activos
     *
     * @return Lista de proveedores activos
     */
    @GetMapping("/activos")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COMPRAS')")
    public ResponseEntity<List<ProveedorDTO>> obtenerActivos() {
        try {
            logger.info("API: Obteniendo proveedores activos");

            List<ProveedorDTO> proveedores = proveedorServicio.buscarActivos();

            logger.info("API: {} proveedores activos encontrados", proveedores.size());
            return ResponseEntity.ok(proveedores);

        } catch (Exception e) {
            logger.error("Error obteniendo proveedores activos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== ENDPOINTS DE MODIFICACIÓN ==========

    /**
     * Crear nuevo proveedor
     * POST /api/proveedores
     *
     * @param proveedorDTO Datos del proveedor
     * @return Proveedor creado
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ProveedorDTO> crear(@Valid @RequestBody ProveedorDTO proveedorDTO) {
        try {
            logger.info("API: Creando nuevo proveedor: {}", proveedorDTO.getNombre());

            // Validar RUT único
            if (proveedorServicio.existeRut(proveedorDTO.getRut())) {
                logger.warn("RUT duplicado: {}", proveedorDTO.getRut());
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            ProveedorDTO proveedorGuardado = proveedorServicio.guardar(proveedorDTO);

            logger.info("API: Proveedor creado exitosamente: {}", proveedorGuardado.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(proveedorGuardado);

        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            logger.error("Error creando proveedor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Actualizar proveedor existente
     * PUT /api/proveedores/{id}
     *
     * @param id ID del proveedor
     * @param proveedorDTO Datos actualizados
     * @return Proveedor actualizado
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ProveedorDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProveedorDTO proveedorDTO) {

        try {
            logger.info("API: Actualizando proveedor ID: {}", id);

            // Verificar existencia
            if (!proveedorServicio.existe(id)) {
                logger.warn("Proveedor no encontrado: {}", id);
                return ResponseEntity.notFound().build();
            }

            // Validar RUT único (excluyendo el mismo proveedor)
            if (proveedorServicio.existeRutExcluyendo(proveedorDTO.getRut(), id)) {
                logger.warn("RUT duplicado: {}", proveedorDTO.getRut());
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            ProveedorDTO proveedorActualizado = proveedorServicio.actualizar(id, proveedorDTO);

            logger.info("API: Proveedor actualizado exitosamente: {}", id);
            return ResponseEntity.ok(proveedorActualizado);

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Proveedor no encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            logger.error("Error actualizando proveedor {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Eliminar proveedor
     * DELETE /api/proveedores/{id}
     *
     * @param id ID del proveedor
     * @return Respuesta con mensaje
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Long id) {
        try {
            logger.info("API: Eliminando proveedor ID: {}", id);

            // Verificar existencia
            if (!proveedorServicio.existe(id)) {
                logger.warn("Proveedor no encontrado: {}", id);
                return ResponseEntity.notFound().build();
            }

            // Verificar si tiene órdenes asociadas
            if (proveedorServicio.tieneOrdenesCompra(id)) {
                logger.warn("No se puede eliminar proveedor con órdenes asociadas: {}", id);
                Map<String, String> respuesta = new HashMap<>();
                respuesta.put("error", "No se puede eliminar el proveedor porque tiene órdenes de compra asociadas");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(respuesta);
            }

            proveedorServicio.eliminar(id);

            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Proveedor eliminado exitosamente");

            logger.info("API: Proveedor eliminado exitosamente: {}", id);
            return ResponseEntity.ok(respuesta);

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Proveedor no encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            logger.warn("No se puede eliminar proveedor: {}", e.getMessage());
            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(respuesta);

        } catch (Exception e) {
            logger.error("Error eliminando proveedor {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== ENDPOINTS DE VALIDACIÓN ==========

    /**
     * Verificar si un RUT ya está registrado
     * GET /api/proveedores/verificar-rut
     *
     * @param rut RUT a verificar
     * @return Resultado de verificación
     */
    @GetMapping("/verificar-rut")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COMPRAS')")
    public ResponseEntity<Map<String, Object>> verificarRut(
            @RequestParam String rut,
            @RequestParam(required = false) Long idExcluir) {

        try {
            logger.info("API: Verificando RUT: {}", rut);

            boolean existe;
            if (idExcluir != null) {
                existe = proveedorServicio.existeRutExcluyendo(rut, idExcluir);
            } else {
                existe = proveedorServicio.existeRut(rut);
            }

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("existe", existe);
            respuesta.put("disponible", !existe);
            respuesta.put("mensaje", existe ? "RUT ya registrado" : "RUT disponible");

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            logger.error("Error verificando RUT: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== ENDPOINTS DE ESTADÍSTICAS ==========

    /**
     * Obtener estadísticas de proveedores
     * GET /api/proveedores/estadisticas
     *
     * @return Estadísticas generales
     */
    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        try {
            logger.info("API: Obteniendo estadísticas de proveedores");

            Map<String, Object> estadisticas = new HashMap<>();

            estadisticas.put("totalProveedores", proveedorServicio.contarTodos());
            estadisticas.put("proveedoresActivos", proveedorServicio.contarActivos());
            estadisticas.put("proveedoresInactivos", proveedorServicio.contarInactivos());
            estadisticas.put("proveedoresConCredito", proveedorServicio.contarProveedoresConCredito());
            estadisticas.put("categorias", proveedorServicio.obtenerCategorias());

            // Top proveedores
            List<ProveedorDTO> topProveedores = proveedorServicio.obtenerTopProveedoresPorOrdenes(5);
            estadisticas.put("topProveedores", topProveedores);

            // Proveedores sin órdenes
            List<ProveedorDTO> sinOrdenes = proveedorServicio.obtenerProveedoresSinOrdenes();
            estadisticas.put("proveedoresSinOrdenes", sinOrdenes.size());

            logger.info("API: Estadísticas de proveedores generadas exitosamente");
            return ResponseEntity.ok(estadisticas);

        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas de proveedores: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener top proveedores por número de órdenes
     * GET /api/proveedores/top-ordenes
     *
     * @param limite Número máximo de proveedores
     * @return Lista de top proveedores
     */
    @GetMapping("/top-ordenes")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<List<ProveedorDTO>> obtenerTopPorOrdenes(
            @RequestParam(defaultValue = "10") int limite) {

        try {
            logger.info("API: Obteniendo top {} proveedores por órdenes", limite);

            List<ProveedorDTO> topProveedores = proveedorServicio.obtenerTopProveedoresPorOrdenes(limite);

            return ResponseEntity.ok(topProveedores);

        } catch (Exception e) {
            logger.error("Error obteniendo top proveedores: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener top proveedores por monto total de compras
     * GET /api/proveedores/top-monto
     *
     * @param limite Número máximo de proveedores
     * @return Lista de top proveedores
     */
    @GetMapping("/top-monto")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<List<ProveedorDTO>> obtenerTopPorMonto(
            @RequestParam(defaultValue = "10") int limite) {

        try {
            logger.info("API: Obteniendo top {} proveedores por monto", limite);

            List<ProveedorDTO> topProveedores = proveedorServicio.obtenerTopProveedoresPorMonto(limite);

            return ResponseEntity.ok(topProveedores);

        } catch (Exception e) {
            logger.error("Error obteniendo top proveedores por monto: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener información detallada de un proveedor con estadísticas
     * GET /api/proveedores/{id}/detalle
     *
     * @param id ID del proveedor
     * @return Información detallada del proveedor
     */
    @GetMapping("/{id}/detalle")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COMPRAS')")
    public ResponseEntity<Map<String, Object>> obtenerDetalle(@PathVariable Long id) {
        try {
            logger.info("API: Obteniendo detalle de proveedor ID: {}", id);

            ProveedorDTO proveedor = proveedorServicio.buscarPorId(id);

            Map<String, Object> detalle = new HashMap<>();
            detalle.put("proveedor", proveedor);
            detalle.put("totalOrdenes", proveedorServicio.contarOrdenesProveedor(id));
            detalle.put("montoTotal", proveedorServicio.calcularMontoTotalProveedor(id));

            return ResponseEntity.ok(detalle);

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Proveedor no encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("Error obteniendo detalle de proveedor {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Activar proveedor
     * POST /api/proveedores/{id}/activar
     *
     * @param id ID del proveedor
     * @return Proveedor activado
     */
    @PostMapping("/{id}/activar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ProveedorDTO> activar(@PathVariable Long id) {
        try {
            logger.info("API: Activando proveedor ID: {}", id);

            ProveedorDTO proveedor = proveedorServicio.activar(id);

            logger.info("API: Proveedor activado exitosamente: {}", id);
            return ResponseEntity.ok(proveedor);

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Proveedor no encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("Error activando proveedor {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Desactivar proveedor
     * POST /api/proveedores/{id}/desactivar
     *
     * @param id ID del proveedor
     * @return Proveedor desactivado
     */
    @PostMapping("/{id}/desactivar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ProveedorDTO> desactivar(@PathVariable Long id) {
        try {
            logger.info("API: Desactivando proveedor ID: {}", id);

            ProveedorDTO proveedor = proveedorServicio.desactivar(id);

            logger.info("API: Proveedor desactivado exitosamente: {}", id);
            return ResponseEntity.ok(proveedor);

        } catch (RecursoNoEncontradoException e) {
            logger.warn("Proveedor no encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("Error desactivando proveedor {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
