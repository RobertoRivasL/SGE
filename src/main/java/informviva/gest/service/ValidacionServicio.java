package informviva.gest.service;


/**
 * @author Roberto Rivas
 * @version 2.0
 */

import java.util.Map;

/**
 * Interfaz para el servicio de validaciones del sistema
 * Centraliza todas las validaciones de negocio
 *
 * @author Roberto Rivas
 * @version 2.0
 */
public interface ValidacionServicio {

    /**
     * Valida el formato y dígito verificador de un RUT chileno
     *
     * @param rut RUT a validar
     * @return true si el RUT es válido, false en caso contrario
     */
    boolean validarRut(String rut);

    /**
     * Formatea un RUT chileno con puntos y guión
     *
     * @param rut RUT a formatear
     * @return RUT formateado (ej: 12.345.678-9)
     */
    String formatearRut(String rut);

    /**
     * Valida si un código de producto es único en el sistema
     *
     * @param codigo Código del producto
     * @param excludeId ID del producto a excluir (para edición)
     * @return true si el código es único, false si ya existe
     */
    boolean validarCodigoProductoUnico(String codigo, Long excludeId);

    /**
     * Valida el formato de un email
     *
     * @param email Email a validar
     * @return true si el formato es válido, false en caso contrario
     */
    boolean validarFormatoEmail(String email);

    /**
     * Valida si un email es único en el sistema
     *
     * @param email Email a validar
     * @param excludeId ID del cliente/usuario a excluir (para edición)
     * @return true si el email es único, false si ya existe
     */
    boolean validarEmailUnico(String email, Long excludeId);

    /**
     * Valida si un username es único en el sistema
     *
     * @param username Username a validar
     * @param excludeId ID del usuario a excluir (para edición)
     * @return true si el username es único, false si ya existe
     */
    boolean validarUsernameUnico(String username, Long excludeId);

    /**
     * Valida si hay stock suficiente para una cantidad solicitada
     *
     * @param productoId ID del producto
     * @param cantidadSolicitada Cantidad solicitada
     * @return true si hay stock suficiente, false en caso contrario
     */
    boolean validarStockSuficiente(Long productoId, Integer cantidadSolicitada);

    /**
     * Obtiene el stock disponible de un producto
     *
     * @param productoId ID del producto
     * @return Stock disponible
     */
    Integer obtenerStockDisponible(Long productoId);

    /**
     * Valida si un cliente puede ser eliminado (no tiene ventas asociadas)
     *
     * @param clienteId ID del cliente
     * @return true si puede ser eliminado, false en caso contrario
     */
    boolean validarEliminacionCliente(Long clienteId);

    /**
     * Valida si un producto puede ser eliminado (no tiene ventas asociadas)
     *
     * @param productoId ID del producto
     * @return true si puede ser eliminado, false en caso contrario
     */
    boolean validarEliminacionProducto(Long productoId);

    /**
     * Valida un rango de fechas (fecha inicio debe ser anterior a fecha fin)
     *
     * @param fechaInicio Fecha de inicio en formato ISO (yyyy-MM-dd)
     * @param fechaFin Fecha de fin en formato ISO (yyyy-MM-dd)
     * @return true si el rango es válido, false en caso contrario
     */
    boolean validarRangoFechas(String fechaInicio, String fechaFin);

    /**
     * Validación integral de los datos de una venta
     *
     * @param ventaData Datos de la venta a validar
     * @return Map con el resultado de la validación y detalles
     */
    Map<String, Object> validarVentaCompleta(Map<String, Object> ventaData);

    /**
     * Valida si una contraseña cumple con los criterios de seguridad
     *
     * @param password Contraseña a validar
     * @return true si cumple los criterios, false en caso contrario
     */
    boolean validarFortalezaContrasena(String password);

    /**
     * Valida si un número de teléfono tiene formato chileno válido
     *
     * @param telefono Número de teléfono a validar
     * @return true si el formato es válido, false en caso contrario
     */
    boolean validarTelefonoChileno(String telefono);

    /**
     * Valida si una fecha está en un rango válido para el negocio
     *
     * @param fecha Fecha a validar
     * @return true si la fecha es válida para el negocio, false en caso contrario
     */
    boolean validarFechaNegocio(String fecha);

    /**
     * Valida si un precio es válido (mayor que 0 y dentro de rangos razonables)
     *
     * @param precio Precio a validar
     * @return true si el precio es válido, false en caso contrario
     */
    boolean validarPrecio(Double precio);
}