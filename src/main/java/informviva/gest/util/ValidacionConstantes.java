package informviva.gest.util;

/**
 * @author Roberto Rivas
 * @version 2.0
 */
 * Constantes para las validaciones del sistema
 *
public final class ValidacionConstantes {
    // Mensajes de validación de RUT
    public static final String RUT_VALIDO = "RUT válido";
    public static final String RUT_INVALIDO = "RUT inválido";
    public static final String RUT_FORMATO_INCORRECTO = "El formato del RUT es incorrecto";
    public static final String RUT_REQUERIDO = "El RUT es obligatorio";
    // Mensajes de validación de email
    public static final String EMAIL_VALIDO = "Email válido";
    public static final String EMAIL_INVALIDO = "Formato de email inválido";
    public static final String EMAIL_YA_EXISTE = "El email ya está registrado";
    public static final String EMAIL_DISPONIBLE = "Email disponible";
    public static final String EMAIL_REQUERIDO = "El email es obligatorio";
    // Mensajes de validación de código de producto
    public static final String CODIGO_DISPONIBLE = "Código disponible";
    public static final String CODIGO_YA_EXISTE = "El código ya está en uso";
    public static final String CODIGO_REQUERIDO = "El código es obligatorio";
    // Mensajes de validación de username
    public static final String USERNAME_DISPONIBLE = "Username disponible";
    public static final String USERNAME_YA_EXISTE = "El username ya está en uso";
    public static final String USERNAME_REQUERIDO = "El username es obligatorio";
    // Mensajes de validación de stock
    public static final String STOCK_SUFICIENTE = "Stock suficiente";
    public static final String STOCK_INSUFICIENTE = "Stock insuficiente";
    public static final String STOCK_NO_DISPONIBLE = "Producto no disponible";
    // Mensajes de validación de eliminación
    public static final String PUEDE_ELIMINAR = "El registro puede ser eliminado";
    public static final String NO_PUEDE_ELIMINAR_CLIENTE = "No se puede eliminar el cliente porque tiene ventas registradas";
    public static final String NO_PUEDE_ELIMINAR_PRODUCTO = "No se puede eliminar el producto porque tiene ventas registradas";
    // Mensajes de validación de fechas
    public static final String RANGO_FECHAS_VALIDO = "Rango de fechas válido";
    public static final String RANGO_FECHAS_INVALIDO = "La fecha de inicio debe ser anterior a la fecha fin";
    public static final String FECHA_INVALIDA = "Formato de fecha inválido";
    public static final String FECHA_FUERA_RANGO = "La fecha está fuera del rango permitido";
    // Mensajes de validación de contraseña
    public static final String PASSWORD_VALIDA = "Contraseña válida";
    public static final String PASSWORD_DEBIL = "La contraseña debe tener al menos 8 caracteres, incluir mayúsculas, minúsculas, números y símbolos";
    public static final String PASSWORD_REQUERIDA = "La contraseña es obligatoria";
    // Mensajes de validación de teléfono
    public static final String TELEFONO_VALIDO = "Número de teléfono válido";
    public static final String TELEFONO_INVALIDO = "Formato de teléfono inválido. Use formato chileno (+56XXXXXXXXX)";
    // Mensajes de validación de precio
    public static final String PRECIO_VALIDO = "Precio válido";
    public static final String PRECIO_INVALIDO = "El precio debe ser mayor que cero y menor que 10.000.000";
    // Mensajes de validación de venta
    public static final String VENTA_VALIDA = "Datos de venta válidos";
    public static final String VENTA_INVALIDA = "Se encontraron errores en los datos de venta";
    public static final String CLIENTE_REQUERIDO = "Cliente es obligatorio";
    public static final String VENDEDOR_REQUERIDO = "Vendedor es obligatorio";
    public static final String PRODUCTOS_REQUERIDOS = "Debe incluir al menos un producto";
    public static final String CANTIDAD_INVALIDA = "La cantidad debe ser mayor que cero";
    // Mensajes generales
    public static final String VALIDACION_EXITOSA = "Validación exitosa";
    public static final String ERROR_VALIDACION = "Error en la validación";
    public static final String DATOS_INVALIDOS = "Los datos proporcionados son inválidos";
    public static final String CAMPO_REQUERIDO = "Este campo es obligatorio";
    // Límites de validación
    public static final int MIN_LENGTH_PASSWORD = 8;
    public static final int MAX_LENGTH_PASSWORD = 50;
    public static final int MAX_LENGTH_EMAIL = 100;
    public static final int MAX_LENGTH_CODIGO_PRODUCTO = 20;
    public static final int MAX_LENGTH_USERNAME = 30;
    public static final int MAX_LENGTH_TELEFONO = 15;
    public static final double MIN_PRECIO = 0.01;
    public static final double MAX_PRECIO = 10_000_000.0;
    public static final int MIN_STOCK = 0;
    public static final int MAX_STOCK = 999_999;
    public static final int MIN_CANTIDAD_VENTA = 1;
    public static final int MAX_CANTIDAD_VENTA = 1_000;
    // Patrones regex como constantes
    public static final String REGEX_EMAIL = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public static final String REGEX_TELEFONO_CHILENO = "^(\\+56|56)?[2-9]\\d{8}$|^(\\+56|56)?9\\d{8}$";
    public static final String REGEX_PASSWORD_FUERTE = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    public static final String REGEX_CODIGO_PRODUCTO = "^[A-Z0-9-]{3,20}$";
    public static final String REGEX_USERNAME = "^[a-zA-Z0-9._-]{3,30}$";
    // Configuración de fechas de negocio
    public static final int MAX_AÑOS_ATRAS = 5;
    public static final int MAX_DIAS_FUTURO = 1;
    // Evitar instanciación
    private ValidacionConstantes() {
        throw new UnsupportedOperationException("Esta es una clase de constantes y no debe ser instanciada");
    }
    // Mensajes de validación de archivos de importación
    public static final String ARCHIVO_VALIDO = "Archivo válido para importación";
    public static final String ARCHIVO_INVALIDO = "Archivo no válido para importación";
    public static final String ARCHIVO_VACIO = "El archivo está vacío";
    public static final String ARCHIVO_CORRUPTO = "El archivo está corrupto o dañado";
    public static final String ARCHIVO_MUY_GRANDE = "El archivo excede el tamaño máximo";
    // Mensajes de validación de formato
    public static final String FORMATO_VALIDO = "Formato de archivo soportado";
    public static final String FORMATO_INVALIDO = "Formato de archivo no soportado";
    public static final String FORMATO_CSV_VALIDO = "Archivo CSV válido";
    public static final String FORMATO_EXCEL_VALIDO = "Archivo Excel válido";
    // Mensajes de validación de columnas
    public static final String COLUMNAS_COMPLETAS = "Todas las columnas requeridas están presentes";
    public static final String COLUMNAS_FALTANTES = "Faltan columnas requeridas en el archivo";
    public static final String ENCABEZADOS_VALIDOS = "Encabezados del archivo son válidos";
    public static final String ENCABEZADOS_INVALIDOS = "Encabezados del archivo no son válidos";
    // Mensajes de validación de datos
    public static final String DATOS_VALIDOS = "Datos del archivo son válidos";
    public static final String DATOS_INVALIDOS = "Se encontraron datos inválidos";
    public static final String FILA_VALIDA = "Fila procesada correctamente";
    public static final String FILA_INVALIDA = "Error en la fila: ";
    // Mensajes de validación específicos por entidad
    public static final String CLIENTE_VALIDO = "Datos de cliente válidos";
    public static final String CLIENTE_INVALIDO = "Datos de cliente inválidos";
    public static final String PRODUCTO_VALIDO = "Datos de producto válidos";
    public static final String PRODUCTO_INVALIDO = "Datos de producto inválidos";
    public static final String USUARIO_VALIDO = "Datos de usuario válidos";
    public static final String USUARIO_INVALIDO = "Datos de usuario inválidos";
    // Patrones de validación para importación
    public static final String PATRON_PRECIO = "^[0-9]+(\\.[0-9]{1,2})?$";
    public static final String PATRON_STOCK = "^[0-9]+$";
    public static final String PATRON_CODIGO_PRODUCTO = "^[A-Z0-9]{3,20}$";
}
