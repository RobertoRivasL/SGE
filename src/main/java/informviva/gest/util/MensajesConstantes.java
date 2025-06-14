package informviva.gest.util;

/**
 * Constantes para los mensajes de la aplicación
 */
public final class MensajesConstantes {
    // Mensajes de éxito
    public static final String VENTA_CREADA = "Venta creada exitosamente con ID: ";
    public static final String VENTA_ACTUALIZADA = "Venta actualizada exitosamente";
    public static final String VENTA_ANULADA = "Venta anulada exitosamente";

    // Mensajes de usuarios
    public static final String USUARIO_NO_ENCONTRADO = "Usuario no encontrado";
    public static final String USUARIO_CREADO = "Usuario creado correctamente";
    public static final String USUARIO_ACTUALIZADO = "Usuario actualizado correctamente";
    public static final String ERROR_CONTRASEÑAS = "Las contraseñas no coinciden";
    public static final String CONTRASEÑA_CAMBIADA = "Contraseña cambiada correctamente";
    public static final String ERROR_CAMBIAR_CONTRASEÑA = "Error al cambiar la contraseña";
    public static final String ROLES_ACTUALIZADOS = "Roles actualizados correctamente";
    public static final String ERROR_ACTUALIZAR_ROLES = "Error al actualizar roles: ";
    public static final String USUARIO_ACTIVADO = "Usuario activado correctamente";
    public static final String USUARIO_DESACTIVADO = "Usuario desactivado correctamente";
    public static final String ERROR_CAMBIAR_ESTADO = "Error al cambiar el estado del usuario";
    public static final String ERROR_GUARDAR_USUARIO = "Error al guardar el usuario: ";
    public static final String ERROR_USERNAME_USADO = "El nombre de usuario ya está en uso";

    // Mensajes de error
    public static final String ERROR_CREAR_VENTA = "Error al crear la venta: ";
    public static final String ERROR_ACTUALIZAR_VENTA = "Error al actualizar la venta: ";
    public static final String ERROR_ANULAR_VENTA = "Error al anular la venta: ";
    public static final String ERROR_RUT_INVALIDO = "El RUT ingresado no es válido.";
    public static final String EXITO_CLIENTE_GUARDADO = "Cliente guardado correctamente.";
    public static final String ERROR_CLIENTE_CON_VENTAS = "No se puede eliminar el cliente porque tiene ventas registradas.";
    public static final String EXITO_CLIENTE_ELIMINADO = "Cliente eliminado correctamente.";

    // Otros mensajes
    public static final String MESSA = "mensaje";
    public static final String UNK = "unknow";

    // Evitar instanciación
    private MensajesConstantes() {
    }

    // Mensajes de importación - éxito
    public static final String IMPORTACION_EXITOSA = "Importación completada exitosamente";
    public static final String IMPORTACION_PARCIAL = "Importación completada con algunos errores";
    public static final String PLANTILLA_GENERADA = "Plantilla generada correctamente";

    // Mensajes de importación - error
    public static final String ERROR_IMPORTACION = "Error durante la importación: ";
    public static final String ERROR_ARCHIVO_VACIO = "El archivo está vacío o no contiene datos válidos";
    public static final String ERROR_FORMATO_NO_SOPORTADO = "Formato de archivo no soportado";
    public static final String ERROR_ARCHIVO_CORRUPTO = "El archivo parece estar corrupto o dañado";
    public static final String ERROR_COLUMNAS_FALTANTES = "El archivo no contiene todas las columnas requeridas";
    public static final String ERROR_VISTA_PREVIA = "Error procesando vista previa del archivo";
    public static final String ERROR_GENERAR_PLANTILLA = "Error generando plantilla de importación";
    public static final String ERROR_ARCHIVO_MUY_GRANDE = "El archivo excede el tamaño máximo permitido";
    public static final String ERROR_TIPO_ENTIDAD_INVALIDO = "Tipo de entidad no válido para importación";

    // Mensajes informativos de importación
    public static final String INFO_PROCESANDO_BASE = "Procesando archivo: ";
    public static final String INFO_FILAS_PROCESADAS = " filas procesadas";
    public static final String INFO_REGISTROS_EXITOSOS = " registros importados exitosamente";
    public static final String INFO_REGISTROS_CON_ERROR = " registros con errores";
}