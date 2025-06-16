package informviva.gest.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Constantes específicas para el sistema de importación.
 * Se integra con la estructura de constantes existente del proyecto.
 *
 * Complementa a:
 * - MensajesConstantes: para mensajes de la aplicación
 * - ValidacionConstantes: para validaciones
 * - RutasConstantes: para rutas y atributos
 *
 * @author Roberto Rivas
 * @version 1.0
 */
public final class ImportacionConstants {

    // Tipos de entidad para importación
    public static final String TIPO_CLIENTE = "cliente";
    public static final String TIPO_PRODUCTO = "producto";
    public static final String TIPO_USUARIO = "usuario";

    public static final List<String> TIPOS_ENTIDAD_VALIDOS = Arrays.asList(
            TIPO_CLIENTE, TIPO_PRODUCTO, TIPO_USUARIO
    );

    // Formatos de archivo soportados
    public static final String FORMATO_CSV = "csv";
    public static final String FORMATO_EXCEL_XLSX = "xlsx";
    public static final String FORMATO_EXCEL_XLS = "xls";

    public static final List<String> FORMATOS_SOPORTADOS = Arrays.asList(
            FORMATO_CSV, FORMATO_EXCEL_XLSX, FORMATO_EXCEL_XLS
    );

    // Configuraciones de archivo
    public static final int TAMAÑO_MAXIMO_ARCHIVO = 10 * 1024 * 1024; // 10 MB
    public static final int LIMITE_VISTA_PREVIA_MINIMO = 1;
    public static final int LIMITE_VISTA_PREVIA_MAXIMO = 100;
    public static final int LIMITE_VISTA_PREVIA_DEFAULT = 10;

    // Índices para procesamiento
    public static final int INDICE_PRIMERA_FILA = 0;
    public static final int INDICE_SEGUNDA_FILA = 1;
    public static final int INDICE_PRIMERA_LETRA = 0;
    public static final int INDICE_SEGUNDA_LETRA = 1;
    public static final int CERO_ERRORES = 0;
    public static final int PRIMERA_HOJA_EXCEL = 0;

    // Configuraciones de procesamiento
    public static final String ENCODING_UTF8 = "UTF-8";
    public static final String SEPARADOR_CSV = ",";
    public static final String SEPARADOR_PUNTO_COMA = ";";
    public static final String SEPARADOR_TABULADOR = "\t";

    // Extensiones de archivo
    public static final String EXTENSION_CSV = ".csv";
    public static final String EXTENSION_XLSX = ".xlsx";
    public static final String EXTENSION_XLS = ".xls";

    // Columnas requeridas por tipo de entidad
    private static final Map<String, List<String>> COLUMNAS_REQUERIDAS = Map.of(
            TIPO_CLIENTE, Arrays.asList("nombre", "apellido", "email", "rut", "telefono", "direccion", "categoria"),
            TIPO_PRODUCTO, Arrays.asList("codigo", "nombre", "descripcion", "precio", "stock", "marca", "modelo"),
            TIPO_USUARIO, Arrays.asList("username", "password", "nombre", "apellido", "email", "roles", "activo")
    );

    // Valores por defecto
    public static final String VALOR_VACIO = "";
    public static final String VALOR_NO_DISPONIBLE = "N/A";
    public static final boolean VALOR_ACTIVO_DEFAULT = true;

    // Constructor privado para prevenir instanciación
    private ImportacionConstants() {
        throw new IllegalStateException("Clase de utilidad - no debe ser instanciada");
    }

    /**
     * Obtiene las columnas requeridas para un tipo de entidad específico.
     *
     * @param tipoEntidad El tipo de entidad
     * @return Lista de columnas requeridas
     */
    public static List<String> obtenerColumnasRequeridas(String tipoEntidad) {
        if (tipoEntidad == null) {
            throw new IllegalArgumentException("El tipo de entidad no puede ser nulo");
        }

        List<String> columnas = COLUMNAS_REQUERIDAS.get(tipoEntidad.toLowerCase());
        if (columnas == null) {
            throw new IllegalArgumentException("Tipo de entidad no válido: " + tipoEntidad);
        }

        return columnas;
    }

    /**
     * Verifica si un tipo de entidad es válido.
     */
    public static boolean esTipoEntidadValido(String tipoEntidad) {
        return tipoEntidad != null && TIPOS_ENTIDAD_VALIDOS.contains(tipoEntidad.toLowerCase());
    }

    /**
     * Verifica si un formato de archivo es soportado.
     */
    public static boolean esFormatoSoportado(String formato) {
        return formato != null && FORMATOS_SOPORTADOS.contains(formato.toLowerCase());
    }

    /**
     * Obtiene la extensión de un nombre de archivo.
     */
    public static String obtenerExtensionArchivo(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isEmpty()) {
            return VALOR_VACIO;
        }

        int ultimoPunto = nombreArchivo.lastIndexOf('.');
        if (ultimoPunto == -1) {
            return VALOR_VACIO;
        }

        return nombreArchivo.substring(ultimoPunto + 1).toLowerCase();
    }

    // Patrones de validación
    public static final String PATRON_EMAIL = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String PATRON_RUT = "^[0-9]{7,8}-[0-9Kk]$";
    public static final String PATRON_TELEFONO = "^[+]?[0-9]{8,15}$";
}
