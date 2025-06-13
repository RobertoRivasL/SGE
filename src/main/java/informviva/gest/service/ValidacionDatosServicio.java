package informviva.gest.service;

import informviva.gest.model.*;
import informviva.gest.validador.ValidadorRutUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Servicio de validación integral de datos del sistema
 */
@Service
public class ValidacionDatosServicio {

    private static final Logger logger = LoggerFactory.getLogger(ValidacionDatosServicio.class);

    // Patrones de validación
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern TELEFONO_PATTERN = Pattern.compile("^[+]?[0-9]{8,15}$");
    private static final Pattern CODIGO_PRODUCTO_PATTERN = Pattern.compile("^[A-Z0-9]{3,20}$");

    @Autowired
    private ClienteServicio clienteServicio;

    @Autowired
    private ProductoServicio productoServicio;

    @Autowired
    private VentaServicio ventaServicio;

    @Autowired
    private UsuarioServicio usuarioServicio;

    /**
     * Validación completa del sistema
     */
    public ResultadoValidacionSistema validarSistemaCompleto() {
        logger.info("Iniciando validación completa del sistema");

        ResultadoValidacionSistema resultado = new ResultadoValidacionSistema();
        resultado.setFechaValidacion(LocalDateTime.now());

        try {
            resultado.setValidacionClientes(validarModuloClientes());
            resultado.setValidacionProductos(validarModuloProductos());
            resultado.setValidacionVentas(validarModuloVentas());
            resultado.setValidacionUsuarios(validarModuloUsuarios());
            resultado.setValidacionIntegridad(validarIntegridadReferencial());

            calcularResumenValidacion(resultado);

            logger.info("Validación del sistema completada. Errores críticos: {}, Advertencias: {}",
                    resultado.getTotalErroresCriticos(), resultado.getTotalAdvertencias());

        } catch (Exception e) {
            logger.error("Error durante validación del sistema: {}", e.getMessage());
            resultado.setErrorGeneral("Error durante validación: " + e.getMessage());
        }

        return resultado;
    }

    /**
     * Valida módulo de clientes
     */
    public ResultadoValidacionModulo validarModuloClientes() {
        return validarModulo("Clientes", clienteServicio.obtenerTodos(), this::validarCliente);
    }

    /**
     * Valida módulo de productos
     */
    public ResultadoValidacionModulo validarModuloProductos() {
        return validarModulo("Productos", productoServicio.listar(), this::validarProducto);
    }

    /**
     * Valida módulo de ventas
     */
    public ResultadoValidacionModulo validarModuloVentas() {
        return validarModulo("Ventas", ventaServicio.listarTodas(), this::validarVenta);
    }

    /**
     * Valida módulo de usuarios
     */
    public ResultadoValidacionModulo validarModuloUsuarios() {
        return validarModulo("Usuarios", usuarioServicio.listarTodos(), this::validarUsuario);
    }

    /**
     * Valida integridad referencial
     */
    public ResultadoValidacionModulo validarIntegridadReferencial() {
        ResultadoValidacionModulo resultado = new ResultadoValidacionModulo("Integridad Referencial");

        try {
            List<Venta> ventas = ventaServicio.listarTodas();

            for (Venta venta : ventas) {
                List<ErrorValidacion> errores = validarReferenciasVenta(venta);
                if (!errores.isEmpty()) {
                    resultado.getRegistrosConErrores().put(venta.getId().toString(), errores);
                }
            }

            resultado.setTotalRegistros(ventas.size());
            resultado.calcularEstadisticas();

        } catch (Exception e) {
            logger.error("Error validando integridad referencial: {}", e.getMessage());
            resultado.getRegistrosConErrores().put("error_general",
                    List.of(new ErrorValidacion("sistema", "Error de validación", NivelSeveridad.CRITICO, e.getMessage())));
        }

        return resultado;
    }

    /**
     * Valida un módulo genérico
     */
    private <T> ResultadoValidacionModulo validarModulo(String nombreModulo, List<T> registros, Validador<T> validador) {
        ResultadoValidacionModulo resultado = new ResultadoValidacionModulo(nombreModulo);

        for (T registro : registros) {
            List<ErrorValidacion> errores = validador.validar(registro);
            if (!errores.isEmpty()) {
                resultado.getRegistrosConErrores().put(registro.toString(), errores);
            }
        }

        resultado.setTotalRegistros(registros.size());
        resultado.calcularEstadisticas();

        logger.debug("Validación {}: {} registros, {} con errores", nombreModulo, registros.size(), resultado.getRegistrosConErrores().size());

        return resultado;
    }

    /**
     * Valida referencias de una venta
     */
    private List<ErrorValidacion> validarReferenciasVenta(Venta venta) {
        List<ErrorValidacion> errores = new ArrayList<>();

        if (venta.getCliente() == null || clienteServicio.buscarPorId(venta.getCliente().getId()) == null) {
            errores.add(new ErrorValidacion("cliente", "Cliente inexistente", NivelSeveridad.CRITICO, "El cliente asociado no existe"));
        }

        if (venta.getVendedor() == null || usuarioServicio.buscarPorId(venta.getVendedor().getId()) == null) {
            errores.add(new ErrorValidacion("vendedor", "Vendedor inexistente", NivelSeveridad.CRITICO, "El vendedor asociado no existe"));
        }

        if (venta.getProducto() != null && productoServicio.buscarPorId(venta.getProducto().getId()) == null) {
            errores.add(new ErrorValidacion("producto", "Producto inexistente", NivelSeveridad.CRITICO, "El producto asociado no existe"));
        }

        return errores;
    }

    /**
     * Valida un cliente
     */
    private List<ErrorValidacion> validarCliente(Cliente cliente) {
        List<ErrorValidacion> errores = new ArrayList<>();

        validarCampoObligatorio(cliente.getRut(), "rut", "RUT vacío", errores, NivelSeveridad.CRITICO);
        if (cliente.getRut() != null && !ValidadorRutUtil.validar(cliente.getRut())) {
            errores.add(new ErrorValidacion("rut", "RUT inválido", NivelSeveridad.CRITICO, "El RUT no tiene un formato válido"));
        }

        validarCampoObligatorio(cliente.getEmail(), "email", "Email vacío", errores, NivelSeveridad.CRITICO);
        validarPatron(cliente.getEmail(), EMAIL_PATTERN, "email", "Email inválido", errores, NivelSeveridad.ADVERTENCIA);

        validarPatron(cliente.getTelefono(), TELEFONO_PATTERN, "telefono", "Teléfono inválido", errores, NivelSeveridad.ADVERTENCIA);

        if (cliente.getFechaRegistro() != null && cliente.getFechaRegistro().isAfter(LocalDate.now())) {
            errores.add(new ErrorValidacion("fechaRegistro", "Fecha futura", NivelSeveridad.ADVERTENCIA, "La fecha de registro es futura"));
        }

        return errores;
    }

    /**
     * Valida un producto
     */
    private List<ErrorValidacion> validarProducto(Producto producto) {
        List<ErrorValidacion> errores = new ArrayList<>();

        validarCampoObligatorio(producto.getCodigo(), "codigo", "Código vacío", errores, NivelSeveridad.CRITICO);
        validarPatron(producto.getCodigo(), CODIGO_PRODUCTO_PATTERN, "codigo", "Código inválido", errores, NivelSeveridad.ADVERTENCIA);

        if (producto.getPrecio() == null || producto.getPrecio() <= 0) {
            errores.add(new ErrorValidacion("precio", "Precio inválido", NivelSeveridad.CRITICO, "El precio debe ser mayor que cero"));
        }

        if (producto.getStock() == null || producto.getStock() < 0) {
            errores.add(new ErrorValidacion("stock", "Stock negativo", NivelSeveridad.CRITICO, "El stock no puede ser negativo"));
        }

        return errores;
    }

    /**
     * Valida un campo obligatorio
     */
    private void validarCampoObligatorio(String valor, String campo, String mensaje, List<ErrorValidacion> errores, NivelSeveridad severidad) {
        if (valor == null || valor.trim().isEmpty()) {
            errores.add(new ErrorValidacion(campo, mensaje, severidad, "El campo " + campo + " es obligatorio"));
        }
    }

    /**
     * Valida un patrón
     */
    private void validarPatron(String valor, Pattern patron, String campo, String mensaje, List<ErrorValidacion> errores, NivelSeveridad severidad) {
        if (valor != null && !patron.matcher(valor).matches()) {
            errores.add(new ErrorValidacion(campo, mensaje, severidad, "El valor no cumple con el patrón esperado"));
        }
    }

    /**
     * Calcula el resumen de validación
     */
    private void calcularResumenValidacion(ResultadoValidacionSistema resultado) {
        int totalErroresCriticos = 0;
        int totalAdvertencias = 0;
        int totalRegistrosConErrores = 0;

        for (ResultadoValidacionModulo modulo : Arrays.asList(
                resultado.getValidacionClientes(),
                resultado.getValidacionProductos(),
                resultado.getValidacionVentas(),
                resultado.getValidacionUsuarios(),
                resultado.getValidacionIntegridad())) {

            totalErroresCriticos += modulo.getErroresCriticos();
            totalAdvertencias += modulo.getAdvertencias();
            totalRegistrosConErrores += modulo.getRegistrosConErrores().size();
        }

        resultado.setTotalErroresCriticos(totalErroresCriticos);
        resultado.setTotalAdvertencias(totalAdvertencias);
        resultado.setTotalRegistrosConErrores(totalRegistrosConErrores);
        resultado.setSistemaConsistente(totalErroresCriticos == 0);
    }

    @FunctionalInterface
    private interface Validador<T> {
        List<ErrorValidacion> validar(T objeto);
    }
}