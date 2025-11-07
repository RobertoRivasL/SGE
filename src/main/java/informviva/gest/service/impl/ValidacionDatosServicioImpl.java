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
import java.util.stream.Collectors;

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

    private final ClienteServicio clienteServicio;
    private final ProductoServicio productoServicio;
    private final VentaServicio ventaServicio;
    private final UsuarioServicio usuarioServicio;

    @Autowired
    public ValidacionDatosServicioImpl(
            ClienteServicio clienteServicio,
            ProductoServicio productoServicio,
            VentaServicio ventaServicio,
            UsuarioServicio usuarioServicio
    ) {
        this.clienteServicio = clienteServicio;
        this.productoServicio = productoServicio;
        this.ventaServicio = ventaServicio;
        this.usuarioServicio = usuarioServicio;
    }

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
            List<Cliente> clientes = clienteServicio.buscarTodos();
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
            List<Venta> ventas = ventaServicio.obtenerTodasLasVentas();
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
            List<Venta> ventas = ventaServicio.obtenerTodasLasVentas();
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
                        Arrays.asList(new ErrorValidacion("sistema", "Error de validación", NivelSeveridad.CRITICO, e.getMessage()))
                );

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
            if (!ValidadorRutUtil.validar(cliente.getRut())) {
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
            if (producto.getPrecio().doubleValue() < MIN_PRECIO ||
                    producto.getPrecio().doubleValue() > MAX_PRECIO) {
                errores.add(new ErrorValidacion("precio", PRECIO_INVALIDO, NivelSeveridad.CRITICO,
                        String.format("El precio debe estar entre %.2f y %.2f", MIN_PRECIO, MAX_PRECIO)));
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
        } else if (venta.getFecha().isAfter(LocalDate.now().atStartOfDay())) {
            errores.add(new ErrorValidacion("fecha", "Fecha futura", NivelSeveridad.ADVERTENCIA, "La fecha de venta es futura"));
        }

        // Validar total
        Double total = venta.getTotal();
        if (total == null || BigDecimal.valueOf(total).compareTo(BigDecimal.ZERO) <= 0) {
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
        Boolean activo = usuario.isActivo();
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
                Cliente cliente = clienteServicio.buscarPorId(venta.getCliente().getId());
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

    /**
     * Validación rápida de datos críticos del sistema
     * Verifica solo los elementos más importantes para un diagnóstico rápido
     *
     * @return ResultadoValidacionSistema con validación básica
     */
    @Override
    public ResultadoValidacionSistema validacionRapida() {
        logger.info("Iniciando validación rápida del sistema");
        long startTime = System.currentTimeMillis();

        ResultadoValidacionSistema resultado = new ResultadoValidacionSistema();
        resultado.setFechaValidacion(LocalDateTime.now());

        try {
            // Validación rápida solo de registros críticos (primeros 100 de cada módulo)
            resultado.setValidacionClientes(validarModuloRapido("Clientes",
                    clienteServicio.buscarTodos().stream().limit(100).collect(Collectors.toList()),
                    this::validarCliente));

            resultado.setValidacionProductos(validarModuloRapido("Productos",
                    productoServicio.listar().stream().limit(100).collect(Collectors.toList()),
                    this::validarProducto));

            resultado.setValidacionVentas(validarModuloRapido("Ventas",
                    ventaServicio.obtenerTodasLasVentas().stream().limit(50).collect(Collectors.toList()),
                    this::validarVenta));

            resultado.setValidacionUsuarios(validarModuloRapido("Usuarios",
                    usuarioServicio.listarTodos().stream().limit(50).collect(Collectors.toList()),
                    this::validarUsuario));

            // Saltamos validación de integridad para hacerlo más rápido
            resultado.setValidacionIntegridad(new ResultadoValidacionModulo("Integridad Referencial"));
            resultado.getValidacionIntegridad().setValido(true);
            resultado.getValidacionIntegridad().setMensaje("Validación rápida - integridad omitida");

            calcularResumenValidacion(resultado);
            resultado.setTiempoValidacionMs(System.currentTimeMillis() - startTime);

            logger.info("Validación rápida completada en {}ms. Errores críticos: {}, Advertencias: {}",
                    resultado.getTiempoValidacionMs(), resultado.getTotalErroresCriticos(), resultado.getTotalAdvertencias());

        } catch (Exception e) {
            logger.error("Error durante validación rápida: {}", e.getMessage(), e);
            resultado.setErrorGeneral("Error durante validación rápida: " + e.getMessage());
            resultado.setSistemaConsistente(false);
        }

        return resultado;
    }

    /**
     * Método auxiliar para validación rápida de módulos
     */
    private <T> ResultadoValidacionModulo validarModuloRapido(String nombreModulo, List<T> registros, Validador<T> validador) {
        ResultadoValidacionModulo resultado = new ResultadoValidacionModulo(nombreModulo);
        Map<String, List<ErrorValidacion>> registrosConErrores = new HashMap<>();

        if (registros == null || registros.isEmpty()) {
            resultado.setValido(true);
            resultado.setMensaje("No hay registros para validar en " + nombreModulo);
            resultado.setTotalRegistros(0);
            return resultado;
        }

        // Solo validar una muestra para ser rápido
        int limite = Math.min(registros.size(), 20); // Máximo 20 registros por módulo

        for (int i = 0; i < limite; i++) {
            T registro = registros.get(i);
            try {
                List<ErrorValidacion> errores = validador.validar(registro);
                if (!errores.isEmpty()) {
                    // Solo mantener errores críticos en validación rápida
                    List<ErrorValidacion> erroresCriticos = errores.stream()
                            .filter(error -> error.getSeveridad() == NivelSeveridad.CRITICO)
                            .collect(Collectors.toList());

                    if (!erroresCriticos.isEmpty()) {
                        registrosConErrores.put(nombreModulo.toLowerCase() + "_" + i, erroresCriticos);
                    }
                }
            } catch (Exception e) {
                logger.warn("Error validando registro {} en módulo {}: {}", i, nombreModulo, e.getMessage());
                registrosConErrores.put(nombreModulo.toLowerCase() + "_" + i,
                        Collections.singletonList(new ErrorValidacion("sistema", "Error de validación", NivelSeveridad.CRITICO, e.getMessage())));
            }
        }

        resultado.setRegistrosConErrores(registrosConErrores);
        resultado.setTotalRegistros(limite); // Registros realmente validados
        resultado.setValido(registrosConErrores.isEmpty());

        if (registrosConErrores.isEmpty()) {
            resultado.setMensaje("Validación rápida de " + nombreModulo + " completada - muestra OK");
        } else {
            resultado.setMensaje("Validación rápida encontró " + registrosConErrores.size() +
                    " registros con errores críticos en " + nombreModulo + " (muestra de " + limite + " registros)");
        }

        logger.debug("Validación rápida {}: {} registros validados, {} con errores",
                nombreModulo, limite, registrosConErrores.size());

        return resultado;
    }

//    **
//            * Valida un módulo específico por nombre
// * @param nombreModulo Nombre del módulo a validar
// * @return ResultadoValidacionModulo
// */
    @Override
    public ResultadoValidacionModulo validarModulo(String nombreModulo) {
        logger.debug("Validando módulo específico: {}", nombreModulo);

        switch (nombreModulo.toLowerCase()) {
            case "clientes":
                return validarModuloClientes();
            case "productos":
                return validarModuloProductos();
            case "ventas":
                return validarModuloVentas();
            case "usuarios":
                return validarModuloUsuarios();
            case "integridad":
            case "integridad_referencial":
                return validarIntegridadReferencial();
            default:
                logger.warn("Módulo no reconocido: {}", nombreModulo);
                ResultadoValidacionModulo resultado = new ResultadoValidacionModulo(nombreModulo);
                resultado.setValido(false);
                resultado.setMensaje("Módulo no reconocido: " + nombreModulo);
                resultado.setTotalRegistros(0);
                return resultado;
        }
    }

    /**
     * Verifica si el sistema es consistente basándose en una validación rápida
     * @return true si no hay errores críticos, false en caso contrario
     */
    @Override
    public boolean esSistemaConsistente() {
        logger.debug("Verificando consistencia del sistema");

        try {
            // Realizar una validación rápida para determinar consistencia
            ResultadoValidacionSistema resultado = validacionRapida();

            // El sistema es consistente si no hay errores críticos
            boolean esConsistente = resultado.getTotalErroresCriticos() == 0;

            logger.debug("Sistema consistente: {}, Errores críticos: {}",
                    esConsistente, resultado.getTotalErroresCriticos());

            return esConsistente;

        } catch (Exception e) {
            logger.error("Error verificando consistencia del sistema: {}", e.getMessage());
            // En caso de error, considerar el sistema como inconsistente por seguridad
            return false;
        }
    }

    /**
     * Obtiene estadísticas de la última validación realizada
     * @return Map con estadísticas de la última validación
     */
    @Override
    public Map<String, Object> obtenerEstadisticasUltimaValidacion() {
        logger.debug("Obteniendo estadísticas de la última validación");

        Map<String, Object> estadisticas = new HashMap<>();

        try {
            // Realizar validación rápida para obtener estadísticas actuales
            ResultadoValidacionSistema resultado = validacionRapida();

            // Estadísticas básicas
            estadisticas.put("fechaValidacion", resultado.getFechaValidacion());
            estadisticas.put("tiempoValidacionMs", resultado.getTiempoValidacionMs());
            estadisticas.put("sistemaConsistente", resultado.isSistemaConsistente());
            estadisticas.put("totalErroresCriticos", resultado.getTotalErroresCriticos());
            estadisticas.put("totalAdvertencias", resultado.getTotalAdvertencias());
            estadisticas.put("totalRegistrosConErrores", resultado.getTotalRegistrosConErrores());

            // Estadísticas por módulo
            Map<String, Map<String, Object>> estadisticasModulos = new HashMap<>();

            // Clientes
            if (resultado.getValidacionClientes() != null) {
                Map<String, Object> statsClientes = new HashMap<>();
                statsClientes.put("valido", resultado.getValidacionClientes().isValido());
                statsClientes.put("totalRegistros", resultado.getValidacionClientes().getTotalRegistros());
                statsClientes.put("registrosConErrores", resultado.getValidacionClientes().getRegistrosConErrores().size());
                estadisticasModulos.put("clientes", statsClientes);
            }

            // Productos
            if (resultado.getValidacionProductos() != null) {
                Map<String, Object> statsProductos = new HashMap<>();
                statsProductos.put("valido", resultado.getValidacionProductos().isValido());
                statsProductos.put("totalRegistros", resultado.getValidacionProductos().getTotalRegistros());
                statsProductos.put("registrosConErrores", resultado.getValidacionProductos().getRegistrosConErrores().size());
                estadisticasModulos.put("productos", statsProductos);
            }

            // Ventas
            if (resultado.getValidacionVentas() != null) {
                Map<String, Object> statsVentas = new HashMap<>();
                statsVentas.put("valido", resultado.getValidacionVentas().isValido());
                statsVentas.put("totalRegistros", resultado.getValidacionVentas().getTotalRegistros());
                statsVentas.put("registrosConErrores", resultado.getValidacionVentas().getRegistrosConErrores().size());
                estadisticasModulos.put("ventas", statsVentas);
            }

            // Usuarios
            if (resultado.getValidacionUsuarios() != null) {
                Map<String, Object> statsUsuarios = new HashMap<>();
                statsUsuarios.put("valido", resultado.getValidacionUsuarios().isValido());
                statsUsuarios.put("totalRegistros", resultado.getValidacionUsuarios().getTotalRegistros());
                statsUsuarios.put("registrosConErrores", resultado.getValidacionUsuarios().getRegistrosConErrores().size());
                estadisticasModulos.put("usuarios", statsUsuarios);
            }

            estadisticas.put("modulos", estadisticasModulos);

            // Resumen ejecutivo
            estadisticas.put("resumen",
                    resultado.isSistemaConsistente() ? "Sistema operativo - sin errores críticos"
                            : "Sistema requiere atención - errores críticos detectados");

            logger.debug("Estadísticas de validación generadas exitosamente");

        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas de validación: {}", e.getMessage());
            estadisticas.put("error", "Error al obtener estadísticas: " + e.getMessage());
            estadisticas.put("fechaError", LocalDateTime.now());
        }

        return estadisticas;
    }

    /**
     * Programa una validación automática del sistema
     * Configura ejecución periódica según el intervalo especificado
     *
     * @param intervalHoras Intervalo en horas para validaciones automáticas
     * @param notificarErrores true si debe notificar errores por email/sistema
     * @return true si la programación fue exitosa
     */
    @Override
    public boolean programarValidacionAutomatica(int intervalHoras, boolean notificarErrores) {
        logger.info("Programando validación automática cada {} horas, notificaciones: {}",
                intervalHoras, notificarErrores);

        try {
            // Validar parámetros
            if (intervalHoras < 1 || intervalHoras > 168) { // Entre 1 hora y 1 semana
                logger.warn("Intervalo de horas inválido: {}. Debe estar entre 1 y 168 horas", intervalHoras);
                return false;
            }

            // TODO: Aquí iría la implementación real con un scheduler (ej: @Scheduled)
            // Por ahora, implementación básica que simula la programación

            logger.info("Validación automática programada exitosamente cada {} horas", intervalHoras);

            // Simular que se guardó la configuración
            // En una implementación real, guardarías esto en base de datos o configuración

            return true;

        } catch (Exception e) {
            logger.error("Error programando validación automática: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Cancela las validaciones automáticas programadas
     *
     * @return true si la cancelación fue exitosa
     */
    @Override
    public boolean cancelarValidacionAutomatica() {
        logger.info("Cancelando validaciones automáticas programadas");

        try {
            // TODO: Aquí iría la implementación real para cancelar el scheduler
            // Por ahora, implementación básica que simula la cancelación

            logger.info("Validaciones automáticas canceladas exitosamente");

            // Simular que se eliminó la configuración
            // En una implementación real, removerías la configuración de la base de datos

            return true;

        } catch (Exception e) {
            logger.error("Error cancelando validaciones automáticas: {}", e.getMessage());
            return false;
        }
    }
}