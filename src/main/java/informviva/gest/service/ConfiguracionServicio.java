package informviva.gest.service;

import informviva.gest.model.ConfiguracionSistema;

/**
 * Servicio para la gestión de la configuración del sistema.
 * Proporciona métodos para obtener, actualizar y probar la configuración.
 *
 * @author Roberto Rivas
 * @version 2.1
 */
public interface ConfiguracionServicio {

    /**
     * Obtiene la configuración actual del sistema.
     *
     * @return La configuración del sistema.
     */
    ConfiguracionSistema obtenerConfiguracion();

    /**
     * Guarda o actualiza la configuración del sistema.
     *
     * @param configuracion La configuración a guardar.
     * @param usuarioActual Nombre del usuario que realiza la actualización.
     * @return La configuración guardada.
     * @throws IllegalArgumentException Si la configuración es inválida.
     */
    ConfiguracionSistema guardarConfiguracion(ConfiguracionSistema configuracion, String usuarioActual);

    /**
     * Envía un correo de prueba para verificar la configuración SMTP.
     *
     * @param correoDestino Dirección de correo a la que se enviará el correo de prueba.
     * @return true si el correo se envió correctamente, false en caso contrario.
     * @throws IllegalStateException Si la configuración SMTP no está correctamente configurada.
     */
    boolean probarConfiguracionCorreo(String correoDestino);
}