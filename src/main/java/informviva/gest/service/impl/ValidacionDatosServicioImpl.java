package informviva.gest.service.impl;

import informviva.gest.service.ValidacionDatosServicio;
import informviva.gest.service.ClienteServicio;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.VentaServicio;
import informviva.gest.service.UsuarioServicio;
import informviva.gest.dto.ResultadoValidacionSistema;
import informviva.gest.dto.ErrorValidacion;
import informviva.gest.enums.NivelSeveridad;
import informviva.gest.dto.ResultadoValidacionModulo;
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

/**
 * Implementación del servicio de validación integral de datos
 * @author Roberto Rivas
 * @version 2.0
 */
@Service
public class ValidacionDatosServicioImpl implements ValidacionDatosServicio {

    private static final Logger logger = LoggerFactory.getLogger(ValidacionDatosServicioImpl.class);

    // Patrones de validación
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern TELEFONO_PATTERN = Pattern.compile("^[+]?[0-9]{8,15}$");
    private static final Pattern CODIGO_PRODUCTO_PATTERN = Pattern.compile("^[A-Z0-9]{3,20}$");
    private static final Pattern RUT_PATTERN = Pattern.compile("^[0-9]{7,8}-[0-9kK]$");

    // Constantes de validación
    private static final String NOMBRE_REQUERIDO = "El nombre es obligatorio";
    private static final String RUT_REQUERIDO = "El RUT es obligatorio";
    private static final String RUT_INVALIDO = "El RUT no tiene un formato válido";
    private static final String EMAIL_REQUERIDO = "El email es obligatorio";
    private static final String EMAIL_INVALIDO = "El email no tiene un formato válido";
    private static final String TELEFONO_INVALIDO = "El teléfono no tiene un formato válido";
    private static final String CODIGO_PRODUCTO_REQUERIDO = "El código del producto es obligatorio";
    private static final String CODIGO_PRODUCTO_INVALIDO = "El código del producto no tiene un formato válido";
    private static final String NOMBRE_PRODUCTO_REQUERIDO = "El nombre del producto es obligatorio";
    private static final String PRECIO_INVALIDO = "El precio no es válido";
    private static final String CLIENTE_REQUERIDO = "El cliente es obligatorio";
    private static final String VENDEDOR_REQUERIDO = "El vendedor es obligatorio";
    private static final String CANTIDAD_INVALIDA = "La cantidad no es válida";

    // Límites de validación
    private static final double MIN_PRECIO = 0.01;
    private static final double MAX_PRECIO = 10_000_000.0;
    private static final int MIN_STOCK = 0;
    private static final int MAX_STOCK = 999_999;
    private static final int MIN_CANTIDAD_VENTA = 1;
    private static final int MAX_CANTIDAD_VENTA = 1_000;
    private static final int MAX_LENGTH_USERNAME = 30;

    @Autowired
    private ClienteServicio clienteServicio;

    @Autowired
    private ProductoServicio productoServicio;

    @Autowired
    private VentaServicio ventaServicio;

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Override
    public ResultadoValidacionSistema validarSistemaCompleto() {
        logger.info("Iniciando validación completa del sistema");
        long startTime = System.currentTimeMillis();

        ResultadoValidacionSistema resultado = new ResultadoValidacionSistema();
        resultado.setFechaValidacion(LocalDateTime.now());

        try {
            resultado.setValidacionClientes(validarModuloClientes());
            resultado.setValidacionProductos(validarModuloProductos());
            resultado.setValidacionVentas(validarModuloVentas());
            resultado.setValidacionUsuarios(validarModuloUsuarios());
            resultado.setValidacionIntegridad(validarIntegridadReferencial());

            calcularResumenValidacion(resultado);

            resultado.setTiempoValidacionMs(System.currentTimeMillis() - startTime);

            logger.info("Validación del sistema completada. Errores críticos: {}, Advertencias: {}",
                    resultado.getTotalErroresCriticos(), resultado.getTotalAdvertencias());

        } catch (Exception e) {
            logger.error("Error durante validación del sistema: {}", e.getMessage(), e);
            resultado.setErrorGeneral("Error durante validación: " + e.getMessage());
            resultado.setSistemaConsistente(false);
        }

        return resultado;
    }

    @Override
    public ResultadoValidacionModulo validarModuloClientes() {
        logger.debug("Iniciando validación de clientes");
        try {
            List<Cliente> clientes = clienteServicio.listar();
            return validarModulo("Clientes", clientes, this::validarCliente);
        } catch (Exception e) {
            logger.error("Error al obtener clientes para validación: {}", e.getMessage());
            return crearResultadoConError("Clientes", "Error al acceder a los datos de clientes");
        }
    }

    @Override
    public ResultadoValidacionModulo validarModuloProductos() {
        logger.debug("Iniciando validación de productos");
        try {
            List<Producto> productos = productoServicio.listar();
            return validarModulo("Productos", productos, this::validarProducto);
        } catch (Exception e) {
            logger.error("Error al obtener productos para validación: {}", e.getMessage());
            return crearResultadoConError("Productos", "Error al acceder a los datos de productos");
        }
    }

    @Override
    public ResultadoValidacionModulo validarModuloVentas() {
        logger.debug("Iniciando validación de ventas");
        try {
            List<Venta> ventas = ventaServicio.listar();
            return validarModulo("Ventas", ventas, this::validarVenta);
        } catch (Exception e) {
            logger.error("Error al obtener ventas para validación: {}", e.getMessage());
            return crearResultadoConError("Ventas", "Error al acceder a los datos de ventas");
        }
    }

    @Override
    public ResultadoValidacionModulo validarModuloUsuarios() {
        logger.debug("Iniciando validación de usuarios");
        try {
            List<Usuario> usuarios = usuarioServicio.listarTodos();
            return validarModulo("Usuarios", usuarios, this::validarUsuario);
        } catch (Exception e) {
            logger.error("Error al obtener usuarios para validación: {}", e.getMessage());
            return crearResultadoConError("Usuarios", "Error al acceder a los datos de usuarios");
        }
    }

    @Override
    public ResultadoValidacionModulo validarIntegridadReferencial() {
        logger.debug("Iniciando validación de integridad referencial");
        ResultadoValidacionModulo resultado = new ResultadoValidacionModulo("Integridad Referencial");

        try {
            List<Venta> ventas = ventaServicio.listar();
            Map<String, List<ErrorValidacion>> registrosConErrores = new HashMap<>();

            for (Venta venta : ventas) {
                List<ErrorValidacion> errores = validarReferenciasVenta(venta);
                if (!errores.isEmpty()) {
                    registrosConErrores.put("venta_" + venta.getId(), errores);
                }
            }

            resultado.setRegistrosConErrores(registrosConErrores);
            resultado.setTotalRegistros(ventas.size());
            resultado.setValido(registrosConErrores.isEmpty());

            if (!registrosConErrores.isEmpty()) {
                resultado.setMensaje("Se encontraron " + registrosConErrores.size() + " ventas con problemas de integridad");
            } else {
                resultado.setMensaje("Integridad referencial correcta");
            }

        } catch (Exception e) {
            logger.error("Error validando integridad referencial: {}", e.getMessage());
            resultado.setValido(false);
            resultado.setMensaje("Error al validar integridad referencial: " + e.getMessage());
        }

        return resultado;
    }

    /**
     * Valida un módulo genérico
     */
    private <T> ResultadoValidacionModulo validarModulo(String nombreModulo, List<T> registros, Validador<T> validador) {
        ResultadoValidacionModulo resultado = new ResultadoValidacionModulo(nombreModulo);
        Map<String, List<ErrorValidacion>> registrosConErrores = new HashMap<>();

        if (registros == null || registros.isEmpty()) {
            resultado.setValido(true);
            resultado.setMensaje("No hay registros para validar en " + nombreModulo);
            resultado.setTotalRegistros(0);
            return resultado;
        }

        for (int i = 0; i < registros.size(); i++) {
            T registro = registros.get(i);
            try {
                List<ErrorValidacion> errores = validador.validar(registro);
                if (!errores.isEmpty()) {
                    registrosConErrores.put(nombreModulo.toLowerCase() + "_" + i, errores);
                }
            } catch (Exception e) {
                logger.warn("Error validando registro {} en módulo {}: {}", i, nombreModulo, e.getMessage());
                registrosConErrores.put(nombreModulo.toLowerCase() + "_" + i,
                        List.of(new ErrorValidacion("sistema", "Error de validación", NivelSeveridad.CRITICO, e.getMessage())));
            }
        }

        resultado.setRegistrosConErrores(registrosConErrores);
        resultado.setTotalRegistros(registros.size());
        resultado.setValido(registrosConErrores.isEmpty());

        if (registrosConErrores.isEmpty()) {
            resultado.setMensaje("Todos los registros de " + nombreModulo + " son válidos");
        } else {
            resultado.setMensaje("Se encontraron " + registrosConErrores.size() + " registros con errores en " + nombreModulo);
        }

        logger.debug("Validación {}: {} registros, {} con errores",
                nombreModulo, registros.size(), registrosConErrores.size());

        return resultado;
    }

    /**
     * Valida un cliente individual
     */
    private List<ErrorValidacion> validarCliente(Cliente cliente) {
        List<ErrorValidacion> errores = new ArrayList<>();

        if (cliente == null) {
            errores.add(new ErrorValidacion("cliente", "Cliente nulo", NivelSeveridad.CRITICO, "El cliente no puede ser nulo"));
            return errores;
        }

        // Validar campos obligatorios
        validarCampoObligatorio(cliente.getNombre(), "nombre", NOMBRE_REQUERIDO, errores, NivelSeveridad.CRITICO);
        validarCampoObligatorio(cliente.getRut(), "rut", RUT_REQUERIDO, errores, NivelSeveridad.CRITICO);

        // Validar formato RUT
        if (cliente.getRut() != null && !cliente.getRut().trim().isEmpty()) {
            if (!ValidadorRutUtil.validarRut(cliente.getRut())) {
                errores.add(new ErrorValidacion("rut", RUT_INVALIDO, NivelSeveridad.CRITICO, "El formato del RUT es incorrecto"));
            }
        }

        // Validar email
        if (cliente.getEmail() != null && !cliente.getEmail().trim().isEmpty()) {
            validarPatron(cliente.getEmail(), EMAIL_PATTERN, "email", EMAIL_INVALIDO, errores, NivelSeveridad.ADVERTENCIA);
        }

        // Validar teléfono
        if (cliente.getTelefono() != null && !cliente.getTelefono().trim().isEmpty()) {
            validarPatron(cliente.getTelefono(), TELEFONO_PATTERN, "telefono", TELEFONO_INVALIDO, errores, NivelSeveridad.ADVERTENCIA);
        }

        return errores;
    }

    /**
     * Valida un producto individual
     */
    private List<ErrorValidacion> validarProducto(Producto producto) {
        List<ErrorValidacion> errores = new ArrayList<>();

        if (producto == null) {
            errores.add(new ErrorValidacion("producto", "Producto nulo", NivelSeveridad.CRITICO, "El producto no puede ser nulo"));
            return errores;
        }

        // Validar campos obligatorios
        validarCampoObligatorio(producto.getNombre(), "nombre", NOMBRE_PRODUCTO_REQUERIDO, errores, NivelSeveridad.CRITICO);
        validarCampoObligatorio(producto.getCodigo(), "codigo", CODIGO_PRODUCTO_REQUERIDO, errores, NivelSeveridad.CRITICO);

        // Validar código de producto
        if (producto.getCodigo() != null && !producto.getCodigo().trim().isEmpty()) {
            validarPatron(producto.getCodigo(), CODIGO_PRODUCTO_PATTERN, "codigo", CODIGO_PRODUCTO_INVALIDO, errores, NivelSeveridad.CRITICO);
        }

        // Validar precio
        if (producto.getPrecio() != null) {
            if (producto.getPrecio().compareTo(BigDecimal.valueOf(MIN_PRECIO)) < 0 ||
                    producto.getPrecio().compareTo(BigDecimal.valueOf(MAX_PRECIO)) > 0) {
                errores.add(new ErrorValidacion("precio", PRECIO_INVALIDO, NivelSeveridad.CRITICO,
                        "El precio debe estar entre " + MIN_PRECIO + " y " + MAX_PRECIO));
            }
        } else {
            errores.add(new ErrorValidacion("precio", "Precio requerido", NivelSeveridad.CRITICO, "El precio es obligatorio"));
        }

        // Validar stock
        if (producto.getStock() != null) {
            if (producto.getStock() < MIN_STOCK || producto.getStock() > MAX_STOCK) {
                errores.add(new ErrorValidacion("stock", "Stock fuera de rango", NivelSeveridad.ADVERTENCIA,
                        "El stock debe estar entre " + MIN_STOCK + " y " + MAX_STOCK));
            }

            if (producto.getStock() < 10) {
                errores.add(new ErrorValidacion("stock", "Stock bajo", NivelSeveridad.ADVERTENCIA, "El producto tiene stock bajo"));
            }
        } else {
            errores.add(new ErrorValidacion("stock", "Stock requerido", NivelSeveridad.ADVERTENCIA, "El stock es recomendable definirlo"));
        }

        return errores;
    }

    /**
     * Valida una venta individual
     */
    private List<ErrorValidacion> validarVenta(Venta venta) {
        List<ErrorValidacion> errores = new ArrayList<>();

        if (venta == null) {
            errores.add(new ErrorValidacion("venta", "Venta nula", NivelSeveridad.CRITICO, "La venta no puede ser nula"));
            return errores;
        }

        // Validar cliente
        if (venta.getCliente() == null) {
            errores.add(new ErrorValidacion("cliente", CLIENTE_REQUERIDO, NivelSeveridad.CRITICO, "La venta debe tener un cliente asociado"));
        }

        // Validar vendedor
        if (venta.getVendedor() == null) {
            errores.add(new ErrorValidacion("vendedor", VENDEDOR_REQUERIDO, NivelSeveridad.CRITICO, "La venta debe tener un vendedor asociado"));
        }

        // Validar fecha
        if (venta.getFecha() == null) {
            errores.add(new ErrorValidacion("fecha", "Fecha requerida", NivelSeveridad.CRITICO, "La venta debe tener una fecha"));
        } else if (venta.getFecha().isAfter(LocalDate.now())) {
            errores.add(new ErrorValidacion("fecha", "Fecha futura", NivelSeveridad.ADVERTENCIA, "La fecha de venta es futura"));
        }

        // Validar total
        if (venta.getTotal() == null || venta.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            errores.add(new ErrorValidacion("total", "Total inválido", NivelSeveridad.CRITICO, "El total de la venta debe ser mayor que cero"));
        }

        // Validar cantidad si existe
        if (venta.getCantidad() != null) {
            if (venta.getCantidad() < MIN_CANTIDAD_VENTA || venta.getCantidad() > MAX_CANTIDAD_VENTA) {
                errores.add(new ErrorValidacion("cantidad", CANTIDAD_INVALIDA, NivelSeveridad.ADVERTENCIA,
                        "La cantidad debe estar entre " + MIN_CANTIDAD_VENTA + " y " + MAX_CANTIDAD_VENTA));
            }
        }

        return errores;
    }

    /**
     * Valida un usuario individual
     */
    private List<ErrorValidacion> validarUsuario(Usuario usuario) {
        List<ErrorValidacion> errores = new ArrayList<>();

        if (usuario == null) {
            errores.add(new ErrorValidacion("usuario", "Usuario nulo", NivelSeveridad.CRITICO, "El usuario no puede ser nulo"));
            return errores;
        }

        // Validar campos obligatorios
        validarCampoObligatorio(usuario.getUsername(), "username", "Username requerido", errores, NivelSeveridad.CRITICO);
        validarCampoObligatorio(usuario.getEmail(), "email", EMAIL_REQUERIDO, errores, NivelSeveridad.CRITICO);

        // Validar formato email
        if (usuario.getEmail() != null && !usuario.getEmail().trim().isEmpty()) {
            validarPatron(usuario.getEmail(), EMAIL_PATTERN, "email", EMAIL_INVALIDO, errores, NivelSeveridad.CRITICO);
        }

        // Validar longitud username
        if (usuario.getUsername() != null &&
                (usuario.getUsername().length() < 3 || usuario.getUsername().length() > MAX_LENGTH_USERNAME)) {
            errores.add(new ErrorValidacion("username", "Username fuera de rango", NivelSeveridad.ADVERTENCIA,
                    "El username debe tener entre 3 y " + MAX_LENGTH_USERNAME + " caracteres"));
        }

        // Validar estado activo - Manejo seguro de Boolean
        Boolean activo = usuario.getActivo();
        if (activo == null || !activo) {
            errores.add(new ErrorValidacion("activo", "Usuario inactivo", NivelSeveridad.ADVERTENCIA,
                    "El usuario está marcado como inactivo"));
        }

        return errores;
    }

    /**
     * Valida referencias de una venta
     */
    private List<ErrorValidacion> validarReferenciasVenta(Venta venta) {
        List<ErrorValidacion> errores = new ArrayList<>();

        if (venta == null) {
            errores.add(new ErrorValidacion("venta", "Venta nula", NivelSeveridad.CRITICO, "La venta no puede ser nula"));
            return errores;
        }

        // Verificar existencia del cliente
        if (venta.getCliente() != null && venta.getCliente().getId() != null) {
            try {
                Cliente cliente = clienteServicio.obtenerPorId(venta.getCliente().getId());
                if (cliente == null) {
                    errores.add(new ErrorValidacion("cliente", "Cliente inexistente", NivelSeveridad.CRITICO,
                            "El cliente asociado no existe en el sistema"));
                }
            } catch (Exception e) {
                errores.add(new ErrorValidacion("cliente", "Error al validar cliente", NivelSeveridad.CRITICO,
                        "Error verificando la existencia del cliente: " + e.getMessage()));
            }
        }

        // Verificar existencia del vendedor
        if (venta.getVendedor() != null && venta.getVendedor().getId() != null) {
            try {
                Usuario vendedor = usuarioServicio.buscarPorId(venta.getVendedor().getId());
                if (vendedor == null) {
                    errores.add(new ErrorValidacion("vendedor", "Vendedor inexistente", NivelSeveridad.CRITICO,
                            "El vendedor asociado no existe en el sistema"));
                }
            } catch (Exception e) {
                errores.add(new ErrorValidacion("vendedor", "Error al validar vendedor", NivelSeveridad.CRITICO,
                        "Error verificando la existencia del vendedor: " + e.getMessage()));
            }
        }

        // Verificar existencia del producto
        if (venta.getProducto() != null && venta.getProducto().getId() != null) {
            try {
                Producto producto = productoServicio.buscarPorId(venta.getProducto().getId());
                if (producto == null) {
                    errores.add(new ErrorValidacion("producto", "Producto inexistente", NivelSeveridad.CRITICO,
                            "El producto asociado no existe en el sistema"));
                } else {
                    // Verificar stock disponible
                    if (venta.getCantidad() != null && producto.getStock() != null && venta.getCantidad() > producto.getStock()) {
                        errores.add(new ErrorValidacion("stock", "Stock insuficiente", NivelSeveridad.ADVERTENCIA,
                                "La cantidad vendida (" + venta.getCantidad() + ") excede el stock disponible (" + producto.getStock() + ")"));
                    }
                }
            } catch (Exception e) {
                errores.add(new ErrorValidacion("producto", "Error al validar producto", NivelSeveridad.CRITICO,
                        "Error verificando la existencia del producto: " + e.getMessage()));
            }
        }

        return errores;
    }

    /**
     * Valida un campo obligatorio
     */
    private void validarCampoObligatorio(String valor, String campo, String mensaje,
                                         List<ErrorValidacion> errores, NivelSeveridad severidad) {
        if (valor == null || valor.trim().isEmpty()) {
            errores.add(new ErrorValidacion(campo, mensaje, severidad, "El campo " + campo + " es obligatorio"));
        }
    }

    /**
     * Valida un patrón
     */
    private void validarPatron(String valor, Pattern patron, String campo, String mensaje,
                               List<ErrorValidacion> errores, NivelSeveridad severidad) {
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

        List<ResultadoValidacionModulo> modulos = Arrays.asList(
                resultado.getValidacionClientes(),
                resultado.getValidacionProductos(),
                resultado.getValidacionVentas(),
                resultado.getValidacionUsuarios(),
                resultado.getValidacionIntegridad());

        for (ResultadoValidacionModulo modulo : modulos) {
            if (modulo != null && modulo.getRegistrosConErrores() != null) {
                totalRegistrosConErrores += modulo.getRegistrosConErrores().size();

                // Contar errores por severidad
                for (List<ErrorValidacion> erroresRegistro : modulo.getRegistrosConErrores().values()) {
                    for (ErrorValidacion error : erroresRegistro) {
                        if (error.getSeveridad() == NivelSeveridad.CRITICO) {
                            totalErroresCriticos++;
                        } else if (error.getSeveridad() == NivelSeveridad.ADVERTENCIA) {
                            totalAdvertencias++;
                        }
                    }
                }
            }
        }

        resultado.setTotalErroresCriticos(totalErroresCriticos);
        resultado.setTotalAdvertencias(totalAdvertencias);
        resultado.setTotalRegistrosConErrores(totalRegistrosConErrores);
        resultado.setSistemaConsistente(totalErroresCriticos == 0);
    }

    /**
     * Crea un resultado con error para casos de excepción
     */
    private ResultadoValidacionModulo crearResultadoConError(String nombreModulo, String mensajeError) {
        ResultadoValidacionModulo resultado = new ResultadoValidacionModulo(nombreModulo);
        resultado.setValido(false);
        resultado.setMensaje(mensajeError);
        resultado.setTotalRegistros(0);
        return resultado;
    }

    /**
     * Interfaz funcional para validadores
     */
    @FunctionalInterface
    private interface Validador<T> {
        List<ErrorValidacion> validar(T objeto);
    }
}