package informviva.gest.controlador.api;

import informviva.gest.dto.ImportacionResultadoDTO;
import informviva.gest.service.ImportacionServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Controlador API REST para operaciones de importación
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@RestController
@RequestMapping("/api/importacion")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class ImportacionAPIControlador {

    private static final Logger logger = LoggerFactory.getLogger(ImportacionAPIControlador.class);

    // 🔧 CORRECCIÓN: Cambiar de @Autowired en campo a inyección por constructor
    private final ImportacionServicio importacionServicio;

    // 🔧 CORRECCIÓN: Constructor para inyección de dependencias
    public ImportacionAPIControlador(@Qualifier("importacionServicioImpl") ImportacionServicio importacionServicio) {
        this.importacionServicio = importacionServicio;
    }

    /**
     * Importa clientes desde archivo
     */
    @PostMapping("/clientes")
    public ResponseEntity<ImportacionResultadoDTO> importarClientes(
            @RequestParam("archivo") MultipartFile archivo) {
        try {
            if (archivo.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            ImportacionResultadoDTO resultado = importacionServicio.importarClientes(archivo);

            if (resultado.tieneErrores() && resultado.getRegistrosExitosos() == 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultado);
            }

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            logger.error("Error en importación de clientes vía API: {}", e.getMessage());

            ImportacionResultadoDTO errorResultado = new ImportacionResultadoDTO();
            errorResultado.setTipoEntidad("Cliente");
            errorResultado.setNombreArchivo(archivo.getOriginalFilename());
            errorResultado.agregarError("Error interno: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResultado);
        }
    }

    /**
     * Importa productos desde archivo
     */
    @PostMapping("/productos")
    public ResponseEntity<ImportacionResultadoDTO> importarProductos(
            @RequestParam("archivo") MultipartFile archivo) {
        try {
            if (archivo.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            ImportacionResultadoDTO resultado = importacionServicio.importarProductos(archivo);

            if (resultado.tieneErrores() && resultado.getRegistrosExitosos() == 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultado);
            }

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            logger.error("Error en importación de productos vía API: {}", e.getMessage());

            ImportacionResultadoDTO errorResultado = new ImportacionResultadoDTO();
            errorResultado.setTipoEntidad("Producto");
            errorResultado.setNombreArchivo(archivo.getOriginalFilename());
            errorResultado.agregarError("Error interno: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResultado);
        }
    }

    /**
     * Importa usuarios desde archivo
     */
    @PostMapping("/usuarios")
    public ResponseEntity<ImportacionResultadoDTO> importarUsuarios(
            @RequestParam("archivo") MultipartFile archivo) {
        try {
            if (archivo.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            ImportacionResultadoDTO resultado = importacionServicio.importarUsuarios(archivo);

            if (resultado.tieneErrores() && resultado.getRegistrosExitosos() == 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultado);
            }

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            logger.error("Error en importación de usuarios vía API: {}", e.getMessage());

            ImportacionResultadoDTO errorResultado = new ImportacionResultadoDTO();
            errorResultado.setTipoEntidad("Usuario");
            errorResultado.setNombreArchivo(archivo.getOriginalFilename());
            errorResultado.agregarError("Error interno: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResultado);
        }
    }

    /**
     * Obtiene el estado de una importación específica
     */
    @GetMapping("/estado/{id}")
    public ResponseEntity<ImportacionResultadoDTO> obtenerEstadoImportacion(@PathVariable Long id) {
        try {
            // Implementar lógica para obtener estado de importación por ID
            // Por ahora retorna not found
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("Error obteniendo estado de importación: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene los formatos soportados para importación
     */
    @GetMapping("/formatos-soportados")
    public ResponseEntity<?> obtenerFormatosSoportados() {
        try {
            return ResponseEntity.ok(importacionServicio.obtenerFormatosSoportados());
        } catch (Exception e) {
            logger.error("Error obteniendo formatos soportados: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}