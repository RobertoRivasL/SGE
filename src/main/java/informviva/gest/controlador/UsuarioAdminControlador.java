package informviva.gest.controlador;

import informviva.gest.model.RolVista;
import informviva.gest.model.Usuario;
import informviva.gest.service.PdfGenerationService;
import informviva.gest.service.RolVistaServicio;
import informviva.gest.service.UsuarioServicio;
import informviva.gest.util.MensajesConstantes;
import informviva.gest.util.RolesConstantes;
import informviva.gest.util.RutasConstantes;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
// -------------------------------------------------------------------------------------------

/**
 * Controlador para la administración de usuarios
 */
@Controller
@RequestMapping("/admin/usuarios")
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_GERENTE_GENERAL')")
public class UsuarioAdminControlador {

    private final UsuarioServicio usuarioServicio;
    private final RolVistaServicio rolVistaServicio;

    // --- Inyección del servicio de generación de PDF ---
    private final PdfGenerationService pdfGenerationService;
    // ---------------------------------------------------

    @Autowired
    public UsuarioAdminControlador(UsuarioServicio usuarioServicio,
                                   RolVistaServicio rolVistaServicio,
                                   PdfGenerationService pdfGenerationService) { // Inyectar el nuevo servicio aquí
        this.usuarioServicio = usuarioServicio;
        this.rolVistaServicio = rolVistaServicio;
        this.pdfGenerationService = pdfGenerationService; // Asignar el servicio inyectado
    }

    /**
     * Muestra la lista de usuarios con paginación y búsqueda
     */
    @GetMapping
    public String listarUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Model model) {

        // Configurar paginación con ordenamiento por ID descendente
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // Obtener y filtrar usuarios
        List<Usuario> usuarios;
        if (search != null && !search.trim().isEmpty()) {
            // NOTA: Si quieres que la exportación a PDF respete el filtro de búsqueda,
            // deberías pasar el parámetro 'search' también al método de exportación,
            // y que tu servicio de usuario tenga un método para buscar por filtro sin paginación.
            List<Usuario> todosUsuarios = usuarioServicio.listarTodos(); // Obtiene todos para filtrar en memoria
            usuarios = todosUsuarios.stream()
                    .filter(u -> u.getNombre().toLowerCase().contains(search.toLowerCase()) ||
                            u.getApellido().toLowerCase().contains(search.toLowerCase()) ||
                            u.getUsername().toLowerCase().contains(search.toLowerCase()))
                    .toList();
        } else {
            usuarios = usuarioServicio.listarTodos(); // Obtener todos los usuarios para la paginación
        }

        // Crear la página
        Page<Usuario> usuariosPage = PageWrapper.createPageFromList(usuarios, pageable);
        model.addAttribute(RutasConstantes.ATTR_USUARIOS_PAGE, usuariosPage);
        model.addAttribute(RutasConstantes.ATTR_SEARCH, search);

        return RutasConstantes.VISTA_USUARIOS;
    }

    /**
     * Muestra las últimas vistas de roles
     */
    @GetMapping("/ultimas-vistas")
    public String mostrarUltimasVistasRoles(Model model) {
        List<RolVista> ultimasVistas = rolVistaServicio.obtenerUltimasVistas();
        model.addAttribute(RutasConstantes.ATTR_ULTIMAS_VISTAS, ultimasVistas);
        return RutasConstantes.VISTA_ULTIMAS_VISTAS;
    }

    /**
     * Muestra el formulario para un nuevo usuario
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoUsuario(Model model) {
        model.addAttribute(RutasConstantes.ATTR_USUARIO, new Usuario());
        model.addAttribute(RutasConstantes.ATTR_ROLES_DISPONIBLES, obtenerTodosLosRoles());
        model.addAttribute(RutasConstantes.ATTR_ES_NUEVO, true);

        return RutasConstantes.VISTA_USUARIO_FORM;
    }

    /**
     * Procesa la creación de un nuevo usuario
     */
    @PostMapping("/guardar")
    public String guardarUsuario(
            @Valid @ModelAttribute(RutasConstantes.ATTR_USUARIO) Usuario usuario,
            BindingResult result,
            @RequestParam(required = false) List<String> roles,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Verificar si hay errores de validación
        if (result.hasErrors()) {
            model.addAttribute(RutasConstantes.ATTR_ROLES_DISPONIBLES, obtenerTodosLosRoles());
            model.addAttribute(RutasConstantes.ATTR_ES_NUEVO, usuario.getId() == null);
            return RutasConstantes.VISTA_USUARIO_FORM;
        }

        try {
            // Verificar si el usuario ya existe
            if (usuario.getId() == null && usuarioServicio.buscarPorUsername(usuario.getUsername()) != null) {
                result.rejectValue("username", "error.usuario", MensajesConstantes.ERROR_USERNAME_USADO);
                model.addAttribute(RutasConstantes.ATTR_ROLES_DISPONIBLES, obtenerTodosLosRoles());
                model.addAttribute(RutasConstantes.ATTR_ES_NUEVO, true);
                return RutasConstantes.VISTA_USUARIO_FORM;
            }

            // Si es un usuario nuevo, asegurar que esté activo por defecto
            if (usuario.getId() == null) {
                usuario.setActivo(true);
            }

            // Asignar roles seleccionados
            if (roles != null && !roles.isEmpty()) {
                Set<String> rolesSet = new HashSet<>(roles);
                usuario.setRoles(rolesSet);
            } else {
                // Si no se selecciona ningún rol, asignar USER por defecto
                Set<String> rolesSet = new HashSet<>();
                rolesSet.add("USER");
                usuario.setRoles(rolesSet);
            }

            // Guardar el usuario
            usuarioServicio.guardar(usuario);

            String mensaje = usuario.getId() == null ?
                    MensajesConstantes.USUARIO_CREADO :
                    MensajesConstantes.USUARIO_ACTUALIZADO;
            redirectAttributes.addFlashAttribute(RutasConstantes.ATTR_MENSAJE_EXITO, mensaje);

            return RutasConstantes.REDIRECT_USUARIOS;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(RutasConstantes.ATTR_MENSAJE_ERROR,
                    MensajesConstantes.ERROR_GUARDAR_USUARIO + e.getMessage());
            return RutasConstantes.REDIRECT_USUARIOS;
        }
    }

    /**
     * Muestra el formulario para editar un usuario existente
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarUsuario(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioServicio.buscarPorId(id);

        if (usuario == null) {
            redirectAttributes.addFlashAttribute(RutasConstantes.ATTR_MENSAJE_ERROR, MensajesConstantes.USUARIO_NO_ENCONTRADO);
            return RutasConstantes.REDIRECT_USUARIOS;
        }

        model.addAttribute(RutasConstantes.ATTR_USUARIO, usuario);
        model.addAttribute(RutasConstantes.ATTR_ROLES_DISPONIBLES, obtenerTodosLosRoles());
        model.addAttribute(RutasConstantes.ATTR_ES_NUEVO, false);

        return RutasConstantes.VISTA_USUARIO_FORM;
    }

    /**
     * Cambia el estado (activo/inactivo) de un usuario
     */
    @PostMapping("/estado/{id}")
    public String cambiarEstadoUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioServicio.buscarPorId(id);

        if (usuario == null) {
            redirectAttributes.addFlashAttribute(RutasConstantes.ATTR_MENSAJE_ERROR, MensajesConstantes.USUARIO_NO_ENCONTRADO);
            return RutasConstantes.REDIRECT_USUARIOS;
        }

        boolean nuevoEstado = !usuario.isActivo();

        if (usuarioServicio.cambiarEstado(id, nuevoEstado)) {
            String mensaje = nuevoEstado ?
                    MensajesConstantes.USUARIO_ACTIVADO :
                    MensajesConstantes.USUARIO_DESACTIVADO;
            redirectAttributes.addFlashAttribute(RutasConstantes.ATTR_MENSAJE_EXITO, mensaje);
        } else {
            redirectAttributes.addFlashAttribute(RutasConstantes.ATTR_MENSAJE_ERROR,
                    MensajesConstantes.ERROR_CAMBIAR_ESTADO);
        }

        return RutasConstantes.REDIRECT_USUARIOS;
    }

    /**
     * Muestra el formulario para gestionar los roles de un usuario
     */
    @GetMapping("/roles/{id}")
    public String mostrarFormularioRoles(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioServicio.buscarPorId(id);

        if (usuario == null) {
            redirectAttributes.addFlashAttribute(RutasConstantes.ATTR_MENSAJE_ERROR, MensajesConstantes.USUARIO_NO_ENCONTRADO);
            return RutasConstantes.REDIRECT_USUARIOS;
        }

        // Registrar vista de roles para este usuario específico
        if (usuario.getRoles() != null && !usuario.getRoles().isEmpty()) {
            for (String rol : usuario.getRoles()) {
                rolVistaServicio.registrarVistaRol(rol);
            }
        }

        model.addAttribute(RutasConstantes.ATTR_USUARIO, usuario);
        model.addAttribute(RutasConstantes.ATTR_ROLES_DISPONIBLES, obtenerTodosLosRoles());

        return RutasConstantes.VISTA_USUARIO_ROLES;
    }

    /**
     * Procesa la actualización de roles de un usuario
     */
    @PostMapping("/roles/{id}")
    public String actualizarRolesUsuario(
            @PathVariable Long id,
            @RequestParam(required = false) List<String> roles,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = usuarioServicio.buscarPorId(id);

        if (usuario == null) {
            redirectAttributes.addFlashAttribute(RutasConstantes.ATTR_MENSAJE_ERROR, MensajesConstantes.USUARIO_NO_ENCONTRADO);
            return RutasConstantes.REDIRECT_USUARIOS;
        }

        try {
            // Limpiar roles actuales
            Set<String> rolesActuales = usuario.getRoles();

            // Para cada rol disponible, comprobar si está seleccionado
            for (String rol : obtenerTodosLosRoles()) {
                if (roles != null && roles.contains(rol)) {
                    // Si está seleccionado y no lo tiene, añadirlo
                    if (!rolesActuales.contains(rol)) {
                        usuarioServicio.agregarRol(id, rol);
                    }
                } else {
                    // Si no está seleccionado y lo tiene, quitarlo
                    if (rolesActuales.contains(rol)) {
                        usuarioServicio.quitarRol(id, rol);
                    }
                }
            }

            redirectAttributes.addFlashAttribute(RutasConstantes.ATTR_MENSAJE_EXITO,
                    MensajesConstantes.ROLES_ACTUALIZADOS);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(RutasConstantes.ATTR_MENSAJE_ERROR,
                    MensajesConstantes.ERROR_ACTUALIZAR_ROLES + e.getMessage());
        }

        return RutasConstantes.REDIRECT_USUARIOS;
    }

    /**
     * Muestra el formulario para cambiar la contraseña de un usuario
     */
    @GetMapping("/password/{id}")
    public String mostrarFormularioCambioPassword(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioServicio.buscarPorId(id);

        if (usuario == null) {
            redirectAttributes.addFlashAttribute(RutasConstantes.ATTR_MENSAJE_ERROR, MensajesConstantes.USUARIO_NO_ENCONTRADO);
            return RutasConstantes.REDIRECT_USUARIOS;
        }

        model.addAttribute(RutasConstantes.ATTR_USUARIO, usuario);

        return RutasConstantes.VISTA_USUARIO_PASSWORD;
    }


    /**
     * Procesa el cambio de contraseña de un usuario
     */
    @PostMapping("/password/{id}")
    public String cambiarPasswordUsuario(
            @PathVariable Long id,
            @RequestParam String nuevoPassword,
            @RequestParam String confirmarPassword,
            RedirectAttributes redirectAttributes) {

        if (!nuevoPassword.equals(confirmarPassword)) {
            redirectAttributes.addFlashAttribute(RutasConstantes.ATTR_MENSAJE_ERROR,
                    MensajesConstantes.ERROR_CONTRASEÑAS);
            return RutasConstantes.REDIRECT_PASSWORD + id;
        }

        try {
            if (usuarioServicio.cambiarPassword(id, nuevoPassword)) {
                redirectAttributes.addFlashAttribute(RutasConstantes.ATTR_MENSAJE_EXITO,
                        MensajesConstantes.CONTRASEÑA_CAMBIADA);
            } else {
                redirectAttributes.addFlashAttribute(RutasConstantes.ATTR_MENSAJE_ERROR,
                        MensajesConstantes.ERROR_CAMBIAR_CONTRASEÑA);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(RutasConstantes.ATTR_MENSAJE_ERROR,
                    "Error: " + e.getMessage());
        }

        return RutasConstantes.REDIRECT_USUARIOS;
    }

    /**
     * Obtiene la lista de todos los roles disponibles en el sistema
     */
    private List<String> obtenerTodosLosRoles() {
        // Registrar vista
        rolVistaServicio.registrarVistaRol("TODOS");

        // Esta lista debe coincidir con los roles definidos en RolesConstantes
        return List.of(
                RolesConstantes.ADMIN,
                RolesConstantes.VENTAS,
                RolesConstantes.PRODUCTOS,
                RolesConstantes.GERENTE
        );
    }

    // --- Nuevo método para exportar a PDF ---

    /**
     * Maneja la solicitud para exportar la lista de usuarios a un archivo PDF.
     * Requiere el servicio PdfGenerationService para generar el contenido.
     *
     * @return ResponseEntity con el archivo PDF en el cuerpo de la respuesta.
     */
    @GetMapping("/export/pdf") // Mapea este método a la URL /admin/usuarios/export/pdf
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_GERENTE_GENERAL')")
    // Puedes ajustar los roles permitidos si es necesario
    public ResponseEntity<byte[]> exportUsersToPdf() {

        // 1. Obtener los datos de los usuarios que quieres exportar.
        // Aquí obtienes TODOS los usuarios llamando a tu servicio.
        // Si necesitas exportar solo los resultados de una búsqueda específica,
        // deberías modificar este método para recibir parámetros de búsqueda
        // y llamar a un método en tu UsuarioService que aplique esos filtros sin paginación.
        List<Usuario> usuarios = usuarioServicio.listarTodos(); // Usando el método que ya tienes

        // 2. Generar el contenido del PDF llamando al servicio.
        // Envía la lista de usuarios al servicio para que genere el PDF.
        // Asegúrate de que el método generateUsersPdf en tu servicio maneje posibles excepciones
        // y retorne un arreglo de bytes (quizás vacío en caso de error).
        byte[] pdfBytes = pdfGenerationService.generateUsersPdf(usuarios);

        // 3. Configurar los encabezados de la respuesta HTTP para la descarga del archivo.
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF); // Indica que la respuesta es un PDF
        // Configura el nombre del archivo que el navegador descargará
        headers.setContentDispositionFormData("attachment", "lista_usuarios.pdf");
        // Configura las cabeceras de caché (opcional pero recomendado)
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        // 4. Retornar la respuesta con los encabezados y el contenido del PDF.
        return ResponseEntity.ok()
                .headers(headers) // Adjunta los encabezados configurados
                .body(pdfBytes); // Adjunta el arreglo de bytes del PDF
    }
    // ---------------------------------------


    /**
     * Clase auxiliar para simular paginación en memoria
     */
    private static class PageWrapper<T> implements Page<T> {
        private final List<T> content;
        private final Pageable pageable;
        private final long total;

        private PageWrapper(List<T> content, Pageable pageable, long total) {
            this.content = content;
            this.pageable = pageable;
            this.total = total;
        }

        public static <T> Page<T> createPageFromList(List<T> list, Pageable pageable) {
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), list.size());

            if (start > list.size()) {
                return new PageWrapper<>(List.of(), pageable, list.size());
            }

            return new PageWrapper<>(list.subList(start, end), pageable, list.size());
        }

        @Override
        public int getTotalPages() {
            return (int) Math.ceil((double) total / pageable.getPageSize());
        }

        @Override
        public long getTotalElements() {
            return total;
        }

        @Override
        public int getNumber() {
            return pageable.getPageNumber();
        }

        @Override
        public int getSize() {
            return pageable.getPageSize();
        }

        @Override
        public int getNumberOfElements() {
            return content.size();
        }

        @Override
        public List<T> getContent() {
            return content;
        }

        @Override
        public boolean hasContent() {
            return !content.isEmpty();
        }

        @Override
        public Sort getSort() {
            return pageable.getSort();
        }

        @Override
        public boolean isFirst() {
            return pageable.getPageNumber() == 0;
        }

        @Override
        public boolean isLast() {
            return pageable.getPageNumber() == getTotalPages() - 1;
        }

        @Override
        public boolean hasNext() {
            return pageable.getPageNumber() < getTotalPages() - 1;
        }

        @Override
        public boolean hasPrevious() {
            return pageable.getPageNumber() > 0;
        }

        @Override
        public Pageable nextPageable() {
            return hasNext() ? pageable.next() : Pageable.unpaged();
        }

        @Override
        public Pageable previousPageable() {
            return hasPrevious() ? pageable.previousOrFirst() : Pageable.unpaged();
        }

        @Override
        public <U> Page<U> map(java.util.function.Function<? super T, ? extends U> converter) {
            return new PageWrapper<>(
                    getContent().stream().map(converter).collect(java.util.stream.Collectors.toList()),
                    pageable,
                    total);
        }

        @Override
        public Iterator<T> iterator() {
            return content.iterator();
        }
    }
}