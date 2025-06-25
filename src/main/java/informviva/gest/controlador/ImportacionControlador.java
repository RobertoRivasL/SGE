package informviva.gest.controlador;

import informviva.gest.dto.ImportacionResultadoDTO;
import informviva.gest.service.ImportacionServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * Controlador para la gestión de importaciones
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Controller
@RequestMapping("/importacion")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class ImportacionControlador {

    private static final Logger logger = LoggerFactory.getLogger(ImportacionControlador.class);

    // 🔧 CORRECCIÓN: Cambiar de @Autowired en campo a inyección por constructor
    private final ImportacionServicio importacionServicio;

    // 🔧 CORRECCIÓN: Constructor para inyección de dependencias
    public ImportacionControlador(ImportacionServicio importacionServicio) {
        this.importacionServicio = importacionServicio;
    }

    /**
     * Muestra la página principal de importación
     */
    @GetMapping
    public String mostrarPaginaImportacion(Model model) {
        model.addAttribute("formatosSoportados", importacionServicio.obtenerFormatosSoportados());
        model.addAttribute("tiposEntidad", obtenerTiposEntidadDisponibles());
        return "importacion/index";
    }

    /**
     * Muestra el formulario de importación para un tipo específico de entidad
     */
    @GetMapping("/{tipoEntidad}")
    public String mostrarFormularioImportacion(@PathVariable String tipoEntidad, Model model) {
        if (!esTipoEntidadValido(tipoEntidad)) {
            return "redirect:/importacion?error=tipo-invalido";
        }

        model.addAttribute("tipoEntidad", tipoEntidad);
        model.addAttribute("tipoEntidadCapitalizado", capitalizarPrimeraLetra(tipoEntidad));
        model.addAttribute("columnasRequeridas", importacionServicio.obtenerColumnasRequeridas(tipoEntidad));
        model.addAttribute("formatosSoportados", importacionServicio.obtenerFormatosSoportados());

        return "importacion/formulario";
    }

    /**
     * Procesa la importación de archivos
     */
    @PostMapping("/{tipoEntidad}/procesar")
    public String procesarImportacion(
            @PathVariable String tipoEntidad,
            @RequestParam("archivo") MultipartFile archivo,
            RedirectAttributes redirectAttributes,
            Model model) {

        try {
            if (archivo.isEmpty()) {
                redirectAttributes.addFlashAttribute("mensajeError", "Debe seleccionar un archivo para importar");
                return "redirect:/importacion/" + tipoEntidad;
            }

            // Validar archivo antes de procesar
            Map<String, Object> validacion = importacionServicio.validarArchivoImportacion(archivo, tipoEntidad);

            if (!(Boolean) validacion.get("valido")) {
                redirectAttributes.addFlashAttribute("mensajeError",
                        "Archivo inválido: " + validacion.get("mensaje"));
                return "redirect:/importacion/" + tipoEntidad;
            }

            // Procesar según tipo de entidad
            ImportacionResultadoDTO resultado = switch (tipoEntidad.toLowerCase()) {
                case "clientes" -> importacionServicio.importarClientes(archivo);
                case "productos" -> importacionServicio.importarProductos(archivo);
                case "usuarios" -> importacionServicio.importarUsuarios(archivo);
                default -> throw new IllegalArgumentException("Tipo de entidad no soportado: " + tipoEntidad);
            };

            // Evaluar resultado
            if (resultado.getRegistrosExitosos() > 0) {
                redirectAttributes.addFlashAttribute("mensajeExito",
                        String.format("Importación completada: %d registros procesados exitosamente",
                                resultado.getRegistrosExitosos()));

                if (resultado.getRegistrosConError() > 0) {
                    redirectAttributes.addFlashAttribute("mensajeAdvertencia",
                            String.format("Se encontraron %d registros con errores",
                                    resultado.getRegistrosConError()));
                }
            } else {
                redirectAttributes.addFlashAttribute("mensajeError",
                        "No se pudo procesar ningún registro. Verifique el formato del archivo");
            }

            // Agregar detalles del resultado
            redirectAttributes.addFlashAttribute("resultadoImportacion", resultado);

        } catch (Exception e) {
            logger.error("Error procesando importación de {}: {}", tipoEntidad, e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Error procesando importación: " + e.getMessage());
        }

        return "redirect:/importacion/" + tipoEntidad;
    }

    /**
     * Descarga plantilla de importación
     */
    @GetMapping("/{tipoEntidad}/plantilla")
    public ResponseEntity<byte[]> descargarPlantilla(@PathVariable String tipoEntidad) {
        try {
            if (!esTipoEntidadValido(tipoEntidad)) {
                return ResponseEntity.badRequest().build();
            }

            byte[] plantilla = importacionServicio.generarPlantillaImportacion(tipoEntidad, "xlsx");
            String nombreArchivo = "plantilla_" + tipoEntidad + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombreArchivo)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(plantilla);

        } catch (Exception e) {
            logger.error("Error generando plantilla para {}: {}", tipoEntidad, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Valida un archivo de importación sin procesarlo
     */
    @PostMapping("/{tipoEntidad}/validar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validarArchivo(
            @PathVariable String tipoEntidad,
            @RequestParam("archivo") MultipartFile archivo) {

        try {
            if (archivo.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("valido", false, "mensaje", "Archivo vacío"));
            }

            Map<String, Object> resultado = importacionServicio.validarArchivoImportacion(archivo, tipoEntidad);
            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            logger.error("Error validando archivo: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("valido", false, "mensaje", "Error validando archivo: " + e.getMessage()));
        }
    }

    // =============== MÉTODOS PRIVADOS AUXILIARES ===============

    private List<String> obtenerTiposEntidadDisponibles() {
        return List.of("clientes", "productos", "usuarios");
    }

    private boolean esTipoEntidadValido(String tipoEntidad) {
        return obtenerTiposEntidadDisponibles().contains(tipoEntidad.toLowerCase());
    }

    private String capitalizarPrimeraLetra(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        return texto.substring(0, 1).toUpperCase() + texto.substring(1).toLowerCase();
    }
}