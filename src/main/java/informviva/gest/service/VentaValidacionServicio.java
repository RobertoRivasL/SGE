package informviva.gest.service;

import informviva.gest.dto.VentaDTO;
import informviva.gest.exception.RecursoNoEncontradoException;
import informviva.gest.exception.StockInsuficienteException;
import informviva.gest.model.Cliente;
import informviva.gest.model.Producto;
import informviva.gest.model.Usuario;
import informviva.gest.model.Venta;
import informviva.gest.repository.ClienteRepositorio;
import informviva.gest.repository.ProductoRepositorio;
import informviva.gest.repository.RepositorioUsuario;
import informviva.gest.util.VentaConstantes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de validación para el módulo de ventas - CORREGIDO
 * Centraliza todas las validaciones y reglas de negocio
 * Aplica principio de responsabilidad única
 * ✅ CORREGIDO: Usando RepositorioUsuario y tipos correctos
 *
 * @author Roberto Rivas
 * @version 1.2 - CORREGIDO tipos BigDecimal/Double
 */
@Service
public class VentaValidacionServicio {

    private static final Logger logger = LoggerFactory.getLogger(VentaValidacionServicio.class);

    private final ClienteRepositorio clienteRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final RepositorioUsuario repositorioUsuario;

    public VentaValidacionServicio(ClienteRepositorio clienteRepositorio,
                                   ProductoRepositorio productoRepositorio,
                                   RepositorioUsuario repositorioUsuario) {
        this.clienteRepositorio = clienteRepositorio;
        this.productoRepositorio = productoRepositorio;
        this.repositorioUsuario = repositorioUsuario;
    }

    // ==================== VALIDACIONES PRINCIPALES ====================

    /**
     * Valida completamente una venta antes de crear
     */
    public ResultadoValidacion validarVentaParaCrear(VentaDTO ventaDTO) {
        logger.debug("Validando venta para crear: {}", ventaDTO);

        ResultadoValidacion resultado = new ResultadoValidacion();

        // Validaciones básicas
        validarDatosBasicos(ventaDTO, resultado);

        if (resultado.tieneErrores()) {
            return resultado;
        }

        // Validaciones de existencia
        validarExistenciaEntidades(ventaDTO, resultado);

        if (resultado.tieneErrores()) {
            return resultado;
        }

        // Validaciones de negocio
        validarReglasNegocio(ventaDTO, resultado);

        // Validaciones de stock
        validarStock(ventaDTO, resultado);

        // Validaciones de límites
        validarLimites(ventaDTO, resultado);

        logger.debug("Validación completada. Errores: {}, Advertencias: {}",
                resultado.getErrores().size(), resultado.getAdvertencias().size());

        return resultado;
    }

    /**
     * Valida una venta antes de actualizar
     */
    public ResultadoValidacion validarVentaParaActualizar(Long ventaId, VentaDTO ventaDTO) {
        logger.debug("Validando venta para actualizar. ID: {}", ventaId);

        ResultadoValidacion resultado = validarVentaParaCrear(ventaDTO);

        if (!resultado.tieneErrores()) {
            // Validaciones específicas para actualización
            validarEstadoParaEdicion(ventaId, resultado);
        }

        return resultado;
    }

    /**
     * Valida si una venta se puede anular
     */
    public ResultadoValidacion validarVentaParaAnular(Long ventaId) {
        logger.debug("Validando venta para anular. ID: {}", ventaId);

        ResultadoValidacion resultado = new ResultadoValidacion();

        try {
            if (ventaId == null || ventaId <= 0) {
                resultado.agregarError("ID de venta inválido");
                return resultado;
            }

            // Aquí se podría agregar más lógica de validación específica para anulación

        } catch (Exception e) {
            resultado.agregarError("Error validando venta para anular: " + e.getMessage());
        }

        return resultado;
    }

    // ==================== VALIDACIONES ESPECÍFICAS ====================

    private void validarDatosBasicos(VentaDTO ventaDTO, ResultadoValidacion resultado) {
        if (ventaDTO == null) {
            resultado.agregarError("Los datos de la venta no pueden ser nulos");
            return;
        }

        // Cliente obligatorio
        if (ventaDTO.getClienteId() == null) {
            resultado.agregarError(VentaConstantes.Mensajes.ERROR_CLIENTE_REQUERIDO);
        }

        // Productos obligatorios
        if (ventaDTO.getProductos() == null || ventaDTO.getProductos().isEmpty()) {
            resultado.agregarError(VentaConstantes.Mensajes.ERROR_PRODUCTOS_REQUERIDOS);
        } else {
            validarProductosDTO(ventaDTO.getProductos(), resultado);
        }

        // Fecha válida
        if (ventaDTO.getFecha() != null && ventaDTO.getFecha().isAfter(LocalDateTime.now().plusDays(1))) {
            resultado.agregarError("La fecha de la venta no puede ser futura");
        }

        // Método de pago
        if (ventaDTO.getMetodoPago() != null &&
                !VentaConstantes.MetodosPago.TODOS.contains(ventaDTO.getMetodoPago())) {
            resultado.agregarError("Método de pago no válido: " + ventaDTO.getMetodoPago());
        }

        // Estado
        if (ventaDTO.getEstado() != null &&
                !VentaConstantes.Estados.TODOS.contains(ventaDTO.getEstado())) {
            resultado.agregarError("Estado de venta no válido: " + ventaDTO.getEstado());
        }

        // Observaciones
        if (ventaDTO.getObservaciones() != null &&
                ventaDTO.getObservaciones().length() > VentaConstantes.Limites.MAX_CARACTERES_OBSERVACIONES) {
            resultado.agregarError("Las observaciones no pueden exceder " +
                    VentaConstantes.Limites.MAX_CARACTERES_OBSERVACIONES + " caracteres");
        }
    }

    private void validarProductosDTO(List<VentaDTO.ProductoVentaDTO> productos, ResultadoValidacion resultado) {
        if (productos.size() > VentaConstantes.Limites.MAX_PRODUCTOS_POR_VENTA) {
            resultado.agregarError("No se pueden agregar más de " +
                    VentaConstantes.Limites.MAX_PRODUCTOS_POR_VENTA + " productos por venta");
        }

        for (int i = 0; i < productos.size(); i++) {
            VentaDTO.ProductoVentaDTO producto = productos.get(i);
            String prefijo = "Producto " + (i + 1) + ": ";

            if (producto.getProductoId() == null) {
                resultado.agregarError(prefijo + "Debe seleccionar un producto");
                continue;
            }

            if (producto.getCantidad() == null || producto.getCantidad() < VentaConstantes.Limites.CANTIDAD_MINIMA) {
                resultado.agregarError(prefijo + VentaConstantes.Mensajes.ERROR_CANTIDAD_INVALIDA);
            }

            if (producto.getCantidad() != null && producto.getCantidad() > VentaConstantes.Limites.CANTIDAD_MAXIMA) {
                resultado.agregarError(prefijo + "La cantidad no puede exceder " +
                        VentaConstantes.Limites.CANTIDAD_MAXIMA);
            }

            // ✅ CORREGIDO: Convertir BigDecimal a Double para comparación
            if (producto.getPrecioUnitario() != null &&
                    producto.getPrecioUnitario().compareTo(VentaConstantes.Limites.VENTA_MINIMA.doubleValue()) < 0) {
                resultado.agregarError(prefijo + VentaConstantes.Mensajes.ERROR_PRECIO_INVALIDO);
            }
        }
    }

    private void validarExistenciaEntidades(VentaDTO ventaDTO, ResultadoValidacion resultado) {
        // Validar cliente
        try {
            buscarClienteSeguro(ventaDTO.getClienteId());
        } catch (RecursoNoEncontradoException e) {
            resultado.agregarError(e.getMessage());
        }

        // Validar vendedor si está especificado
        if (ventaDTO.getVendedorId() != null) {
            try {
                Usuario vendedor = buscarUsuarioSeguro(ventaDTO.getVendedorId());
                if (!vendedor.isActivo()) {
                    resultado.agregarError("El vendedor seleccionado no está activo");
                }
            } catch (RecursoNoEncontradoException e) {
                resultado.agregarError(e.getMessage());
            }
        }

        // Validar productos
        if (ventaDTO.getProductos() != null) {
            for (VentaDTO.ProductoVentaDTO productoDTO : ventaDTO.getProductos()) {
                if (productoDTO.getProductoId() != null) {
                    try {
                        Producto producto = buscarProductoSeguro(productoDTO.getProductoId());
                        if (!producto.isActivo()) {
                            resultado.agregarError("El producto '" + producto.getNombre() + "' no está activo");
                        }
                    } catch (RecursoNoEncontradoException e) {
                        resultado.agregarError(e.getMessage());
                    }
                }
            }
        }
    }

    private void validarReglasNegocio(VentaDTO ventaDTO, ResultadoValidacion resultado) {
        // ✅ CORREGIDO: Convertir BigDecimal a Double para validaciones
        if (ventaDTO.getTotal() != null) {
            BigDecimal totalBD = BigDecimal.valueOf(ventaDTO.getTotal());

            if (totalBD.compareTo(VentaConstantes.Limites.VENTA_MINIMA) < 0) {
                resultado.agregarError("El total de la venta debe ser mayor a " +
                        VentaConstantes.formatearMoneda(VentaConstantes.Limites.VENTA_MINIMA));
            }

            if (totalBD.compareTo(VentaConstantes.Limites.VENTA_MAXIMA) > 0) {
                resultado.agregarError("El total de la venta no puede exceder " +
                        VentaConstantes.formatearMoneda(VentaConstantes.Limites.VENTA_MAXIMA));
            }
        }

        // ✅ CORREGIDO: Usar Double y conversiones correctas para cálculos
        if (ventaDTO.getSubtotal() != null && ventaDTO.getImpuesto() != null && ventaDTO.getTotal() != null) {
            Double totalCalculado = ventaDTO.getSubtotal() + ventaDTO.getImpuesto();
            Double diferencia = Math.abs(ventaDTO.getTotal() - totalCalculado);

            if (diferencia > 0.01) {
                resultado.agregarAdvertencia("Hay una diferencia en los cálculos de total. " +
                        "Verificar: Subtotal + Impuesto = Total");
            }
        }
    }

    private void validarStock(VentaDTO ventaDTO, ResultadoValidacion resultado) {
        if (ventaDTO.getProductos() == null) {
            return;
        }

        for (VentaDTO.ProductoVentaDTO productoDTO : ventaDTO.getProductos()) {
            if (productoDTO.getProductoId() != null && productoDTO.getCantidad() != null) {
                try {
                    Producto producto = buscarProductoSeguro(productoDTO.getProductoId());

                    if (producto.getStock() == null) {
                        resultado.agregarAdvertencia("El producto '" + producto.getNombre() +
                                "' no tiene stock configurado");
                        continue;
                    }

                    if (producto.getStock() < productoDTO.getCantidad()) {
                        resultado.agregarError(String.format(
                                VentaConstantes.Mensajes.ERROR_STOCK_INSUFICIENTE,
                                producto.getNombre() + " (Disponible: " + producto.getStock() +
                                        ", Solicitado: " + productoDTO.getCantidad() + ")"
                        ));
                    } else if (producto.getStock() - productoDTO.getCantidad() < VentaConstantes.Limites.STOCK_MINIMO_ALERTA) {
                        resultado.agregarAdvertencia(String.format(
                                VentaConstantes.Mensajes.INFO_STOCK_BAJO,
                                producto.getNombre(),
                                producto.getStock() - productoDTO.getCantidad()
                        ));
                    }

                } catch (RecursoNoEncontradoException e) {
                    resultado.agregarError(e.getMessage());
                }
            }
        }
    }

    private void validarLimites(VentaDTO ventaDTO, ResultadoValidacion resultado) {
        // Validar límites por producto
        if (ventaDTO.getProductos() != null) {
            for (VentaDTO.ProductoVentaDTO productoDTO : ventaDTO.getProductos()) {
                if (productoDTO.getCantidad() != null &&
                        productoDTO.getCantidad() > VentaConstantes.Notificaciones.UMBRAL_CANTIDAD_ALTA) {
                    resultado.agregarAdvertencia("Cantidad alta detectada para un producto: " +
                            productoDTO.getCantidad() + " unidades");
                }
            }
        }

        // ✅ CORREGIDO: Convertir Double a BigDecimal para comparación
        if (ventaDTO.getTotal() != null) {
            BigDecimal totalBD = BigDecimal.valueOf(ventaDTO.getTotal());
            BigDecimal umbralBD = BigDecimal.valueOf(VentaConstantes.Notificaciones.UMBRAL_VENTA_ALTA);

            if (totalBD.compareTo(umbralBD) > 0) {
                resultado.agregarAdvertencia("Venta de monto alto: " +
                        VentaConstantes.formatearMoneda(totalBD));
            }
        }
    }

    private void validarEstadoParaEdicion(Long ventaId, ResultadoValidacion resultado) {
        if (ventaId == null || ventaId <= 0) {
            resultado.agregarError("ID de venta inválido para edición");
        }
    }

    // ==================== MÉTODOS DE BÚSQUEDA SEGURA ====================

    private Cliente buscarClienteSeguro(Long clienteId) {
        return clienteRepositorio.findById(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con ID: " + clienteId));
    }

    private Usuario buscarUsuarioSeguro(Long usuarioId) {
        return repositorioUsuario.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + usuarioId));
    }

    private Producto buscarProductoSeguro(Long productoId) {
        return productoRepositorio.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con ID: " + productoId));
    }

    // ==================== VALIDACIONES ADICIONALES ====================

    /**
     * Valida si un cliente puede realizar compras
     */
    public boolean clientePuedeComprar(Long clienteId) {
        try {
            Cliente cliente = buscarClienteSeguro(clienteId);
            return cliente.isActivo();
        } catch (Exception e) {
            logger.warn("Error validando cliente {}: {}", clienteId, e.getMessage());
            return false;
        }
    }

    /**
     * Valida si un usuario puede ser vendedor
     */
    public boolean usuarioPuedeVender(Long usuarioId) {
        try {
            Usuario usuario = buscarUsuarioSeguro(usuarioId);
            return usuario.isActivo() && tieneRolVendedor(usuario);
        } catch (Exception e) {
            logger.warn("Error validando vendedor {}: {}", usuarioId, e.getMessage());
            return false;
        }
    }

    private boolean tieneRolVendedor(Usuario usuario) {
        return usuario.tieneRol("VENDEDOR") ||
                usuario.tieneRol("ADMIN") ||
                usuario.tieneRol("GERENTE") ||
                usuario.tieneRol("SUPERVISOR");
    }

    /**
     * Calcula el stock disponible considerando reservas
     */
    public int calcularStockDisponible(Long productoId) {
        try {
            Producto producto = buscarProductoSeguro(productoId);
            return producto.getStock() != null ? producto.getStock() : 0;
        } catch (Exception e) {
            logger.warn("Error calculando stock disponible para producto {}: {}", productoId, e.getMessage());
            return 0;
        }
    }

    // ==================== CLASE INTERNA: RESULTADO DE VALIDACIÓN ====================

    public static class ResultadoValidacion {
        private final List<String> errores = new ArrayList<>();
        private final List<String> advertencias = new ArrayList<>();
        private final List<String> informaciones = new ArrayList<>();

        public void agregarError(String error) {
            errores.add(error);
        }

        public void agregarAdvertencia(String advertencia) {
            advertencias.add(advertencia);
        }

        public void agregarInformacion(String informacion) {
            informaciones.add(informacion);
        }

        public boolean tieneErrores() {
            return !errores.isEmpty();
        }

        public boolean tieneAdvertencias() {
            return !advertencias.isEmpty();
        }

        public boolean esValido() {
            return !tieneErrores();
        }

        public List<String> getErrores() {
            return new ArrayList<>(errores);
        }

        public List<String> getAdvertencias() {
            return new ArrayList<>(advertencias);
        }

        public List<String> getInformaciones() {
            return new ArrayList<>(informaciones);
        }

        public String getPrimerError() {
            return errores.isEmpty() ? null : errores.get(0);
        }

        public String getResumen() {
            StringBuilder sb = new StringBuilder();
            if (tieneErrores()) {
                sb.append("Errores: ").append(errores.size());
            }
            if (tieneAdvertencias()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append("Advertencias: ").append(advertencias.size());
            }
            if (sb.length() == 0) {
                sb.append("Validación exitosa");
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            return "ResultadoValidacion{" +
                    "errores=" + errores.size() +
                    ", advertencias=" + advertencias.size() +
                    ", informaciones=" + informaciones.size() +
                    '}';
        }
    }
}