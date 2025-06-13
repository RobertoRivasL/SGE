package informviva.gest.util;

/**
 * @author Roberto Rivas
 * @version 2.0
 */


/**
 * Constantes para los roles de la aplicación
 *
 * @author Roberto Rivas
 * @version 1.0
 */
public final class RolesConstantes {
    // Roles sin prefijo (para uso normal)
    public static final String ADMIN = "ADMIN";
    public static final String VENTAS = "VENTAS";
    public static final String PRODUCTOS = "PRODUCTOS";
    public static final String GERENTE = "GERENTE";
    // Prefijo para los roles en Spring Security
    private static final String ROLE_PREFIX = "ROLE_";
    // Roles con prefijo ROLE_ (para uso directo con hasRole/hasAnyRole)
    public static final String ROLE_ADMIN = ROLE_PREFIX + ADMIN;
    public static final String ROLE_VENTAS = ROLE_PREFIX + VENTAS;
    public static final String ROLE_PRODUCTOS = ROLE_PREFIX + PRODUCTOS;
    public static final String ROLE_GERENTE = ROLE_PREFIX + GERENTE;

    public static final String RESPALDO_ADMIN = "RESPALDO_ADMIN";
    public static final String ROLE_RESPALDO_ADMIN = ROLE_PREFIX + RESPALDO_ADMIN;

    // Evitar instanciación
    private RolesConstantes() {
    }
}