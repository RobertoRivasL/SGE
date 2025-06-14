import informviva.gest.dto.ResultadoValidacionModulo;
package informviva.gest.service;

import informviva.gest.dto.ResultadoValidacionSistema;

/**
 * Interfaz para el servicio de validación integral de datos del sistema
 * Define los contratos para validación completa del sistema y sus módulos
 *
 * @author Roberto Rivas
 * @version 2.1
 */
public interface ValidacionDatosServicio {

    /**
     * Ejecuta una validación completa de todo el sistema
     * Incluye validación de todos los módulos e integridad referencial
     *
     * @return ResultadoValidacionSistema con el resultado completo de la validación
     */
    ResultadoValidacionSistema validarSistemaCompleto();

    /**
     * Valida específicamente el módulo de clientes
     * Incluye validaciones de datos, formato y reglas de negocio
     *
     * @return ResultadoValidacionModulo con los resultados de validación de clientes
     */
    ResultadoValidacionModulo validarModuloClientes();

    /**
     * Valida específicamente el módulo de productos
     * Incluye validaciones de inventario, precios y códigos únicos
     *
     * @return ResultadoValidacionModulo con los resultados de validación de productos
     */
    ResultadoValidacionModulo validarModuloProductos();

    /**
     * Valida específicamente el módulo de ventas
     * Incluye validaciones de transacciones, montos y relaciones
     *
     * @return ResultadoValidacionModulo con los resultados de validación de ventas
     */
    ResultadoValidacionModulo validarModuloVentas();

    /**
     * Valida específicamente el módulo de usuarios
     * Incluye validaciones de credenciales, roles y permisos
     *
     * @return ResultadoValidacionModulo con los resultados de validación de usuarios
     */
    ResultadoValidacionModulo validarModuloUsuarios();

    /**
     * Valida la integridad referencial entre todos los módulos
     * Verifica que las relaciones entre entidades sean consistentes
     *
     * @return ResultadoValidacionModulo con los resultados de validación de integridad
     */
    ResultadoValidacionModulo validarIntegridadReferencial();

    /**
     * Ejecuta una validación rápida del sistema (solo errores críticos)
     * Útil para verificaciones frecuentes sin impacto en rendimiento
     *
     * @return ResultadoValidacionSistema con validación básica
     */
    ResultadoValidacionSistema validacionRapida();

    /**
     * Valida un módulo específico por nombre
     * Permite validación dinámica según el módulo requerido
     *
     * @param nombreModulo Nombre del módulo a validar ("clientes", "productos", "ventas", "usuarios")
     * @return ResultadoValidacionModulo con los resultados específicos del módulo
     * @throws IllegalArgumentException si el nombre del módulo no es válido
     */
    ResultadoValidacionModulo validarModulo(String nombreModulo);

    /**
     * Verifica si el sistema está en estado consistente
     * Ejecuta validaciones críticas básicas sin procesamiento completo
     *
     * @return true si el sistema está consistente, false si hay problemas críticos
     */
    boolean esSistemaConsistente();

    /**
     * Obtiene las estadísticas de la última validación ejecutada
     * Útil para monitoreo y dashboard sin re-ejecutar validaciones
     *
     * @return Map con estadísticas básicas (errores, advertencias, tiempo, etc.)
     */
    java.util.Map<String, Object> obtenerEstadisticasUltimaValidacion();

    /**
     * Programa una validación automática del sistema
     * Configura ejecución periódica según el intervalo especificado
     *
     * @param intervalHoras Intervalo en horas para validaciones automáticas
     * @param notificarErrores true si debe notificar errores por email/sistema
     * @return true si la programación fue exitosa
     */
    boolean programarValidacionAutomatica(int intervalHoras, boolean notificarErrores);

    /**
     * Cancela las validaciones automáticas programadas
     *
     * @return true si la cancelación fue exitosa
     */
    boolean cancelarValidacionAutomatica();
}