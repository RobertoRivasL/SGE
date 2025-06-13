package informviva.gest.controlador;


import informviva.gest.dto.ImportacionResultadoDTO;
import informviva.gest.service.ImportacionServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ImportacionServicio importacionServicio;

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
                @SuppressWarnings("unchecked")
                List<String> errores = (List<String>) validacion.get("errores");
                redirectAttributes.addFlashAttribute("mensajeError",
                        "Archivo inválido: " + String.join(", ", errores));
                return "redirect:/importacion/" + tipoEntidad;
            }

            // Procesar importación según el tipo de entidad
            ImportacionResultadoDTO resultado = procesarSegunTipo(tipoEntidad, archivo);

            // Agregar resultado al modelo para mostrar en la vista
            model.addAttribute("resultado", resultado);
            model.addAttribute("tipoEntidad", tipoEntidad);
            model.addAttribute("tipoEntidadCapitalizado", capitalizarPrimeraLetra(tipoEntidad));

            return "importacion/resultado";

        } catch (Exception e) {
            logger.error("Error procesando importación de {}: {}", tipoEntidad, e.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Error procesando el archivo: " + e.getMessage());
            return "redirect:/importacion/" + tipoEntidad;
        }
    }

    /**
     * Muestra vista previa del archivo antes de importar
     */
    @PostMapping("/{tipoEntidad}/vista-previa")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerVistaPrevia(
            @PathVariable String tipoEntidad,
            @RequestParam("archivo") MultipartFile archivo) {

        try {
            Map<String, Object> validacion = importacionServicio.validarArchivoImportacion(archivo, tipoEntidad);

            if ((Boolean) validacion.get("valido")) {
                List<Map<String, Object>> vistaPrevia = importacionServicio.obtenerVistaPreviaArchivo(archivo, 10);
                validacion.put("vistaPrevia", vistaPrevia);
            }

            return ResponseEntity.ok(validacion);

        } catch (Exception e) {
            logger.error("Error obteniendo vista previa: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("valido", false, "errores", List.of("Error procesando archivo: " + e.getMessage())));
        }
    }

    /**
     * Descarga plantilla de importación
     */
    @GetMapping("/{tipoEntidad}/plantilla")
    public ResponseEntity<byte[]> descargarPlantilla(
            @PathVariable String tipoEntidad,
            @RequestParam(defaultValue = "excel") String formato) {

        try {
            if (!esTipoEntidadValido(tipoEntidad)) {
                return ResponseEntity.badRequest().build();
            }

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
            logger.error("Error generando plantilla para {}: {}", tipoEntidad, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API REST para validar archivo
     */
    @PostMapping("/api/{tipoEntidad}/validar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validarArchivo(
            @PathVariable String tipoEntidad,
            @RequestParam("archivo") MultipartFile archivo) {

        try {
            Map<String, Object> resultado = importacionServicio.validarArchivoImportacion(archivo, tipoEntidad);
            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            logger.error("Error validando archivo: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("valido", false, "errores", List.of("Error: " + e.getMessage())));
        }
    }

    /**
     * Muestra el historial de importaciones
     */
    @GetMapping("/historial")
    public String mostrarHistorialImportaciones(Model model) {
        // TODO: Implementar historial de importaciones si se requiere persistencia
        model.addAttribute("mensaje", "Funcionalidad de historial en desarrollo");
        return "importacion/historial";
    }

    /**
     * Muestra la ayuda para importaciones
     */
    @GetMapping("/ayuda")
    public String mostrarAyudaImportacion(Model model) {
        model.addAttribute("tiposEntidad", obtenerTiposEntidadDisponibles());
        model.addAttribute("formatosSoportados", importacionServicio.obtenerFormatosSoportados());

        // Agregar información de columnas para cada tipo
        for (String tipo : obtenerTiposEntidadDisponibles()) {
            model.addAttribute("columnas" + capitalizarPrimeraLetra(tipo),
                    importacionServicio.obtenerColumnasRequeridas(tipo));
        }

        return "importacion/ayuda";
    }

    // Métodos auxiliares privados

    private ImportacionResultadoDTO procesarSegunTipo(String tipoEntidad, MultipartFile archivo) {
        long inicioTiempo = System.currentTimeMillis();
        ImportacionResultadoDTO resultado;

        switch (tipoEntidad.toLowerCase()) {
            case "cliente":
                resultado = importacionServicio.importarClientes(archivo);
                break;
            case "producto":
                resultado = importacionServicio.importarProductos(archivo);
                break;
            case "usuario":
                resultado = importacionServicio.importarUsuarios(archivo);
                break;
            default:
                throw new IllegalArgumentException("Tipo de entidad no soportado: " + tipoEntidad);
        }

        long tiempoTranscurrido = System.currentTimeMillis() - inicioTiempo;
        resultado.setTiempoProcesamientoMs(tiempoTranscurrido);

        return resultado;
    }

    private boolean esTipoEntidadValido(String tipoEntidad) {
        return obtenerTiposEntidadDisponibles().contains(tipoEntidad.toLowerCase());
    }

    private List<String> obtenerTiposEntidadDisponibles() {
        return List.of("cliente", "producto", "usuario");
    }

    private String capitalizarPrimeraLetra(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        return texto.substring(0, 1).toUpperCase() + texto.substring(1).toLowerCase();
    }
}
