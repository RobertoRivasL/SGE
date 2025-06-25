package informviva.gest.util;


import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Constantes para el módulo de ventas
 * Centraliza todas las constantes utilizadas en el sistema de ventas
 *
 * @author Roberto Rivas
 * @version 1.0
 */
public final class VentaConstantes {

    // ==================== CONSTRUCTOR PRIVADO ====================
    private VentaConstantes() {
        throw new IllegalStateException("Clase de utilidades - no debe ser instanciada");
    }

    // ==================== ESTADOS DE VENTA ====================
    public static final class Estados {
        public static final String PENDIENTE = "PENDIENTE";
        public static final String EN_PROCESO = "EN_PROCESO";
        public static final String COMPLETADA = "COMPLETADA";
        public static final String ANULADA = "ANULADA";
        public static final String PARCIALMENTE_PAGADA = "PARCIALMENTE_PAGADA";

        public static final List<String> TODOS = Arrays.asList(
                PENDIENTE, EN_PROCESO, COMPLETADA, ANULADA, PARCIALMENTE_PAGADA
        );

        public static final List<String> EDITABLES = Arrays.asList(
                PENDIENTE, EN_PROCESO, PARCIALMENTE_PAGADA
        );

        public static final List<String> FINALIZADOS = Arrays.asList(
                COMPLETADA, ANULADA
        );
    }

    // ==================== MÉTODOS DE PAGO ====================
    public static final class MetodosPago {
        public static final String EFECTIVO = "EFECTIVO";
        public static final String TARJETA_CREDITO = "TARJETA_CREDITO";
        public static final String TARJETA_DEBITO = "TARJETA_DEBITO";
        public static final String TRANSFERENCIA = "TRANSFERENCIA";
        public static final String CHEQUE = "CHEQUE";
        public static final String CREDITO = "CREDITO";

        public static final List<String> TODOS = Arrays.asList(
                EFECTIVO, TARJETA_CREDITO, TARJETA_DEBITO, TRANSFERENCIA, CHEQUE, CREDITO
        );

        public static final List<String> ELECTRONICOS = Arrays.asList(
                TARJETA_CREDITO, TARJETA_DEBITO, TRANSFERENCIA
        );

        public static final List<String> INMEDIATOS = Arrays.asList(
                EFECTIVO, TARJETA_CREDITO, TARJETA_DEBITO, TRANSFERENCIA
        );
    }

    // ==================== CONFIGURACIÓN DE IMPUESTOS ====================
    public static final class Impuestos {
        public static final BigDecimal IVA_PORCENTAJE = new BigDecimal("0.19"); // 19%
        public static final BigDecimal IVA_PORCENTAJE_DISPLAY = new BigDecimal("19"); // Para mostrar
        public static final String IVA_NOMBRE = "IVA";

        // Otros impuestos posibles
        public static final BigDecimal IMPUESTO_LUJO = new BigDecimal("0.08"); // 8%
        public static final BigDecimal IMPUESTO_VERDE = new BigDecimal("0.05"); // 5%
    }

    // ==================== LÍMITES Y VALIDACIONES ====================
    public static final class Limites {
        public static final BigDecimal VENTA_MINIMA = new BigDecimal("0.01");
        public static final BigDecimal VENTA_MAXIMA = new BigDecimal("999999.99");
        public static final int CANTIDAD_MINIMA = 1;
        public static final int CANTIDAD_MAXIMA = 99999;
        public static final int MAX_PRODUCTOS_POR_VENTA = 100;
        public static final int MAX_CARACTERES_OBSERVACIONES = 500;
        public static final int STOCK_MINIMO_ALERTA = 5;
    }

    // ==================== FORMATOS ====================
    public static final class Formatos {
        public static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        public static final DateTimeFormatter FECHA_SIMPLE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        public static final DateTimeFormatter HORA_SIMPLE = DateTimeFormatter.ofPattern("HH:mm");
        public static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

        // Formatos de número
        public static final String FORMATO_MONEDA = "$%,.2f";
        public static final String FORMATO_PORCENTAJE = "%.2f%%";
        public static final String FORMATO_NUMERO = "%,.0f";
    }

    // ==================== MENSAJES DE VALIDACIÓN ====================
    public static final class Mensajes {
        // Errores de validación
        public static final String ERROR_CLIENTE_REQUERIDO = "Debe seleccionar un cliente para la venta";
        public static final String ERROR_PRODUCTOS_REQUERIDOS = "Debe agregar al menos un producto a la venta";
        public static final String ERROR_CANTIDAD_INVALIDA = "La cantidad debe ser mayor a cero";
        public static final String ERROR_PRECIO_INVALIDO = "El precio debe ser mayor a cero";
        public static final String ERROR_STOCK_INSUFICIENTE = "Stock insuficiente para el producto: %s";
        public static final String ERROR_VENTA_NO_ENCONTRADA = "Venta no encontrada con ID: %s";
        public static final String ERROR_VENTA_NO_EDITABLE = "La venta en estado %s no se puede editar";

        // Mensajes de éxito
        public static final String EXITO_VENTA_CREADA = "Venta creada exitosamente con ID: %s";
        public static final String EXITO_VENTA_ACTUALIZADA = "Venta actualizada exitosamente";
        public static final String EXITO_VENTA_ANULADA = "Venta anulada exitosamente";
        public static final String EXITO_VENTA_DUPLICADA = "Venta duplicada exitosamente con ID: %s";

        // Mensajes informativos
        public static final String INFO_STOCK_BAJO = "Advertencia: Stock bajo para el producto %s (Disponible: %d)";
        public static final String INFO_DESCUENTO_APLICADO = "Descuento aplicado: %s";
        public static final String INFO_IMPUESTO_CALCULADO = "Impuesto calculado automáticamente";
    }

    // ==================== CONFIGURACIÓN DE REPORTES ====================
    public static final class Reportes {
        public static final int MAX_PRODUCTOS_TOP = 10;
        public static final int MAX_CLIENTES_TOP = 20;
        public static final int MAX_VENDEDORES_TOP = 10;
        public static final int DIAS_PERIODO_DEFECTO = 30;

        // Nombres de columnas para exportación
        public static final String[] COLUMNAS_EXCEL_VENTA = {
                "ID", "Fecha", "Cliente", "Vendedor", "Subtotal", "Impuesto", "Total", "Método Pago", "Estado"
        };

        public static final String[] COLUMNAS_CSV_VENTA = {
                "ID", "Fecha", "Cliente_RUT", "Cliente_Nombre", "Vendedor",
                "Subtotal", "Impuesto", "Total", "Método_Pago", "Estado", "Observaciones"
        };
    }

    // ==================== CONFIGURACIÓN DE PAGINACIÓN ====================
    public static final class Paginacion {
        public static final int TAMAÑO_PAGINA_DEFECTO = 20;
        public static final int TAMAÑO_MAXIMO_PAGINA = 100;
        public static final String CAMPO_ORDENAMIENTO_DEFECTO = "fecha";
        public static final String DIRECCION_ORDENAMIENTO_DEFECTO = "desc";
    }

    // ==================== CONFIGURACIÓN DE CACHE ====================
    public static final class Cache {
        public static final String CACHE_VENTAS = "ventas";
        public static final String CACHE_ESTADISTICAS = "estadisticas-ventas";
        public static final String CACHE_PRODUCTOS_VENDIDOS = "productos-vendidos";
        public static final int TTL_CACHE_MINUTOS = 15;
    }

    // ==================== RUTAS DE CONTROLADOR ====================
    public static final class Rutas {
        public static final String BASE = "/ventas";
        public static final String LISTA = BASE + "/lista";
        public static final String NUEVA = BASE + "/nueva";
        public static final String DETALLE = BASE + "/detalle";
        public static final String EDITAR = BASE + "/editar";
        public static final String GUARDAR = BASE + "/guardar";
        public static final String ACTUALIZAR = BASE + "/actualizar";
        public static final String ELIMINAR = BASE + "/eliminar";
        public static final String DUPLICAR = BASE + "/duplicar";
        public static final String EXPORTAR = BASE + "/exportar";
    }

    // ==================== VISTAS ====================
    public static final class Vistas {
        public static final String LISTA = "ventas/lista";
        public static final String NUEVA = "ventas/nueva";
        public static final String DETALLE = "ventas/detalle";
        public static final String EDITAR = "ventas/editar";
        public static final String ERROR = "error/general";
    }

    // ==================== ATRIBUTOS DE MODELO ====================
    public static final class Atributos {
        public static final String VENTA = "venta";
        public static final String VENTAS = "ventas";
        public static final String CLIENTES = "clientes";
        public static final String PRODUCTOS = "productos";
        public static final String VENDEDORES = "vendedores";
        public static final String ERROR = "error";
        public static final String MENSAJE_EXITO = "mensajeExito";
        public static final String MENSAJE_ADVERTENCIA = "mensajeAdvertencia";
    }

    // ==================== CONFIGURACIÓN DE EXPORTACIÓN ====================
    public static final class Exportacion {
        public static final String FORMATO_EXCEL = "xlsx";
        public static final String FORMATO_PDF = "pdf";
        public static final String FORMATO_CSV = "csv";

        public static final String CONTENT_TYPE_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        public static final String CONTENT_TYPE_PDF = "application/pdf";
        public static final String CONTENT_TYPE_CSV = "text/csv";

        public static final String PREFIJO_ARCHIVO = "ventas_";
        public static final String PREFIJO_RESUMEN = "resumen_ventas_";
    }

    // ==================== CONFIGURACIÓN DE SEGURIDAD ====================
    public static final class Seguridad {
        public static final String ROL_VENDEDOR = "VENDEDOR";
        public static final String ROL_SUPERVISOR = "SUPERVISOR";
        public static final String ROL_GERENTE = "GERENTE";
        public static final String ROL_ADMIN = "ADMIN";

        public static final List<String> ROLES_PERMITIDOS_CREAR = Arrays.asList(
                ROL_VENDEDOR, ROL_SUPERVISOR, ROL_GERENTE, ROL_ADMIN
        );

        public static final List<String> ROLES_PERMITIDOS_ANULAR = Arrays.asList(
                ROL_SUPERVISOR, ROL_GERENTE, ROL_ADMIN
        );

        public static final List<String> ROLES_PERMITIDOS_EXPORTAR = Arrays.asList(
                ROL_GERENTE, ROL_ADMIN
        );
    }

    // ==================== TIPOS DE REPORTE ====================
    public static final class TiposReporte {
        public static final String DIARIO = "DIARIO";
        public static final String SEMANAL = "SEMANAL";
        public static final String MENSUAL = "MENSUAL";
        public static final String ANUAL = "ANUAL";
        public static final String PERSONALIZADO = "PERSONALIZADO";

        public static final List<String> TODOS = Arrays.asList(
                DIARIO, SEMANAL, MENSUAL, ANUAL, PERSONALIZADO
        );
    }

    // ==================== CONFIGURACIÓN DE NOTIFICACIONES ====================
    public static final class Notificaciones {
        public static final String TIPO_VENTA_CREADA = "VENTA_CREADA";
        public static final String TIPO_VENTA_ANULADA = "VENTA_ANULADA";
        public static final String TIPO_STOCK_BAJO = "STOCK_BAJO";
        public static final String TIPO_META_ALCANZADA = "META_ALCANZADA";

        public static final int UMBRAL_VENTA_ALTA = 1000; // Monto en moneda local
        public static final int UMBRAL_CANTIDAD_ALTA = 50; // Cantidad de productos
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Verifica si un estado es editable
     */
    public static boolean esEstadoEditable(String estado) {
        return Estados.EDITABLES.contains(estado);
    }

    /**
     * Verifica si un estado está finalizado
     */
    public static boolean esEstadoFinalizado(String estado) {
        return Estados.FINALIZADOS.contains(estado);
    }

    /**
     * Verifica si un método de pago es electrónico
     */
    public static boolean esMetodoPagoElectronico(String metodoPago) {
        return MetodosPago.ELECTRONICOS.contains(metodoPago);
    }

    /**
     * Verifica si un método de pago es inmediato
     */
    public static boolean esMetodoPagoInmediato(String metodoPago) {
        return MetodosPago.INMEDIATOS.contains(metodoPago);
    }

    /**
     * Formatea un monto como moneda
     */
    public static String formatearMoneda(BigDecimal monto) {
        return String.format(Formatos.FORMATO_MONEDA, monto != null ? monto : BigDecimal.ZERO);
    }

    /**
     * Formatea un porcentaje
     */
    public static String formatearPorcentaje(Double porcentaje) {
        return String.format(Formatos.FORMATO_PORCENTAJE, porcentaje != null ? porcentaje : 0.0);
    }
}