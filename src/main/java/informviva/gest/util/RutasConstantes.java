package informviva.gest.util;

/**
 * Constantes para las rutas de la aplicación
 * Centraliza las rutas públicas, estáticas, de API y administración.
 *
 * @author Roberto
 * @version 2.1
 */
public final class RutasConstantes {

    // Rutas públicas
    public static final String[] RUTAS_PUBLICAS = {
            "/", "/inicio", "/contacto", "/login", "/error"
    };

    // Rutas de recursos estáticos
    public static final String[] RUTAS_ESTATICAS = {
            "/css/**", "/js/**", "/static/images/**", "/webjars/**", "/favicon.ico"
    };

    // Rutas de API
    public static final String API_AUTH = "/api/auth/**";
    public static final String API_PRODUCTOS = "/api/productos/**";
    public static final String API_VENTAS = "/api/ventas/**";
    public static final String API_CLIENTES = "/api/clientes/**";
    public static final String API_REPORTES = "/api/reportes/**";

    // Rutas de administración
    public static final String PANEL = "/panel/**";
    public static final String USUARIOS = "/usuarios/**";
    public static final String ADMIN = "/admin/**";

    // Rutas funcionales por módulo
    public static final String PRODUCTOS = "/productos/**";
    public static final String VENTAS = "/ventas/**";
    public static final String CLIENTES = "/clientes/**";
    public static final String REPORTES = "/reportes/**";

    // Rutas específicas de clientes
    public static final String RUTA_CLIENTES = "/clientes";
    public static final String RUTA_NUEVO = "/nuevo";
    public static final String RUTA_GUARDAR = "/guardar";
    public static final String RUTA_EDITAR = "/editar";
    public static final String RUTA_ELIMINAR = "/eliminar";

    // Vistas
    public static final String VISTA_LISTA_CLIENTES = "clientes/lista_clientes";
    public static final String VISTA_FORMULARIO_CLIENTE = "clientes/formulario";

    // Redirecciones
    public static final String REDIRECT_USUARIOS = "redirect:/admin/usuarios";
    public static final String REDIRECT_PASSWORD = "redirect:/admin/usuarios/password/";

    // Constantes de atributos del modelo
    public static final String VENTAS_HOY = "ventasHoy";
    public static final String INGRESOS_MES = "ingresosMes";
    public static final String PRODUCTOS_BAJO_STOCK = "productosConBajoStock";
    public static final String PRODUCTOS_BAJOS = "productosBajos";
    public static final String VENTAS_RECIENTES = "ventasRecientes";
    public static final String VENTA_SUMMARY = "ventaSummary";

    // Atributos del modelo para usuarios
    public static final String ATTR_USUARIO = "usuario";
    public static final String ATTR_ROLES_DISPONIBLES = "rolesDisponibles";
    public static final String ATTR_ES_NUEVO = "esNuevo";
    public static final String ATTR_MENSAJE_EXITO = "mensajeExito";
    public static final String ATTR_MENSAJE_ERROR = "mensajeError";
    public static final String ATTR_ULTIMAS_VISTAS = "ultimasVistas";
    public static final String ATTR_USUARIOS_PAGE = "usuariosPage";
    public static final String ATTR_SEARCH = "search";

    // Vistas para usuarios
    public static final String VISTA_USUARIOS = "admin/usuarios";
    public static final String VISTA_USUARIO_FORM = "admin/usuario-form";
    public static final String VISTA_USUARIO_ROLES = "admin/usuario-roles";
    public static final String VISTA_USUARIO_PASSWORD = "admin/usuario-password";
    public static final String VISTA_ULTIMAS_VISTAS = "admin/usuarios-ultimas-vistas";

    // Vistas para ventas
    public static final String VISTA_NUEVA_VENTA = "ventas/nueva";
    public static final String VISTA_LISTA_VENTAS = "ventas/lista";
    public static final String VISTA_DETALLE_VENTA = "ventas/detalle";
    public static final String VISTA_EDITAR_VENTA = "ventas/editar";

    public static final String RESPALDOS = "/admin/respaldos/**";
    public static final String API_RESPALDOS = "/admin/respaldos/api/**";

    // Evitar instanciación
    private RutasConstantes() {
    }
}