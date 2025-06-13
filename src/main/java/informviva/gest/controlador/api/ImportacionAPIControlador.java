package informviva.gest.controlador.api;


import informviva.gest.dto.ImportacionResultadoDTO;
import informviva.gest.service.ImportacionServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private ImportacionServicio importacionServicio;

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
     * Importa usuarios desde archivo (solo para ADMIN)
     */
    @PostMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
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
     * Valida un archivo antes de la importación
     */
    @PostMapping("/validar/{tipoEntidad}")
    public ResponseEntity<Map<String, Object>> validarArchivo(
            @PathVariable String tipoEntidad,
            @RequestParam("archivo") MultipartFile archivo) {
        try {
            Map<String, Object> resultado = importacionServicio.validarArchivoImportacion(archivo, tipoEntidad);
            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            logger.error("Error validando archivo vía API: {}", e.getMessage());

            Map<String, Object> errorResultado = new HashMap<>();
            errorResultado.put("valido", false);
            errorResultado.put("errores", List.of("Error interno: " + e.getMessage()));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResultado);
        }
    }

    /**
     * Obtiene vista previa de los datos del archivo
     */
    @PostMapping("/vista-previa")
    public ResponseEntity<Map<String, Object>> obtenerVistaPrevia(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(defaultValue = "10") int limite) {
        try {
            List<Map<String, Object>> vistaPrevia = importacionServicio.obtenerVistaPreviaArchivo(archivo, limite);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("datos", vistaPrevia);
            respuesta.put("totalFilas", vistaPrevia.size());
            respuesta.put("nombreArchivo", archivo.getOriginalFilename());

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            logger.error("Error obteniendo vista previa vía API: {}", e.getMessage());

            Map<String, Object> errorResultado = new HashMap<>();
            errorResultado.put("error", "Error procesando archivo: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResultado);
        }
    }

    /**
     * Descarga plantilla de importación
     */
    @GetMapping("/plantilla/{tipoEntidad}")
    public ResponseEntity<byte[]> descargarPlantilla(
            @PathVariable String tipoEntidad,
            @RequestParam(defaultValue = "excel") String formato) {
        try {
            byte[] plantilla = importacionServicio.generarPlantillaImportacion(tipoEntidad, formato);

            if (plantilla.length == 0) {
                return ResponseEntity.noContent().build();
            }

            HttpHeaders headers = new HttpHeaders();
            String nombreArchivo = "plantilla_" + tipoEntidad + "_importacion";

            if ("csv".equalsIgnoreCase(formato)) {
                headers.setContentType(MediaType.TEXT_PLAIN);
                nombreArchivo += ".csv";
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                nombreArchivo += ".xlsx";
            }

            headers.setContentDispositionFormData("attachment", nombreArchivo);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(plantilla);

        } catch (Exception e) {
            logger.error("Error generando plantilla vía API: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene información sobre formatos soportados y columnas requeridas
     */
    @GetMapping("/info/{tipoEntidad}")
    public ResponseEntity<Map<String, Object>> obtenerInformacionImportacion(@PathVariable String tipoEntidad) {
        try {
            Map<String, Object> info = new HashMap<>();
            info.put("tipoEntidad", tipoEntidad);
            info.put("formatosSoportados", importacionServicio.obtenerFormatosSoportados());
            info.put("columnasRequeridas", importacionServicio.obtenerColumnasRequeridas(tipoEntidad));

            return ResponseEntity.ok(info);

        } catch (Exception e) {
            logger.error("Error obteniendo información de importación: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene estadísticas generales de formatos soportados
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        try {
            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("formatosSoportados", importacionServicio.obtenerFormatosSoportados());
            estadisticas.put("tiposEntidadSoportados", List.of("cliente", "producto", "usuario"));
            estadisticas.put("tamaximoArchivoMB", 10);
            estadisticas.put("limitesRecomendados", Map.of(
                    "cliente", "Hasta 10,000 registros",
                    "producto", "Hasta 5,000 registros",
                    "usuario", "Hasta 1,000 registros"
            ));

            return ResponseEntity.ok(estadisticas);

        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint de health check para el módulo de importación
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", java.time.LocalDateTime.now());
        health.put("modulo", "Importación");
        health.put("version", "2.0");

        return ResponseEntity.ok(health);
    }
}