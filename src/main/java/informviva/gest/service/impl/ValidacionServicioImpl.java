package informviva.gest.service.impl;



/**
 * @author Roberto Rivas
 * @version 2.0
 */

import informviva.gest.model.Producto;
import informviva.gest.repository.ClienteRepositorio;
import informviva.gest.repository.RepositorioUsuario;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.ValidacionServicio;
import informviva.gest.service.VentaServicio;
import informviva.gest.validador.ValidadorRutUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Implementación del servicio de validaciones del sistema
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Service
public class ValidacionServicioImpl implements ValidacionServicio {

    private static final Logger logger = LoggerFactory.getLogger(ValidacionServicioImpl.class);

    // Patrones de validación
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private static final Pattern TELEFONO_CHILENO_PATTERN = Pattern.compile(
            "^(\\+56|56)?[2-9]\\d{8}$|^(\\+56|56)?9\\d{8}$"
    );

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    // Servicios y repositorios
    private final ProductoServicio productoServicio;
    private final VentaServicio ventaServicio;
    private final ClienteRepositorio clienteRepositorio;
    private final RepositorioUsuario usuarioRepositorio;
    private final org.modelmapper.ModelMapper modelMapper;

    @Autowired
    public ValidacionServicioImpl(ProductoServicio productoServicio,
                                  VentaServicio ventaServicio,
                                  ClienteRepositorio clienteRepositorio,
                                  RepositorioUsuario usuarioRepositorio,
                                  org.modelmapper.ModelMapper modelMapper) {
        this.productoServicio = productoServicio;
        this.ventaServicio = ventaServicio;
        this.clienteRepositorio = clienteRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.modelMapper = modelMapper;
    }

    @Override
    public boolean validarRut(String rut) {
        if (rut == null || rut.trim().isEmpty()) {
            return false;
        }
        try {
            return ValidadorRutUtil.validar(rut);
        } catch (Exception e) {
            logger.error("Error validando RUT {}: {}", rut, e.getMessage());
            return false;
        }
    }

    @Override
    public String formatearRut(String rut) {
        if (rut == null || rut.trim().isEmpty()) {
            return rut;
        }

        try {
            // Limpiar el RUT
            String rutLimpio = rut.replaceAll("[^0-9kK]", "").toUpperCase();

            if (rutLimpio.length() < 2) {
                return rut;
            }

            String cuerpo = rutLimpio.substring(0, rutLimpio.length() - 1);
            String dv = rutLimpio.substring(rutLimpio.length() - 1);

            // Formatear con puntos
            StringBuilder cuerpoFormateado = new StringBuilder();
            int contador = 0;
            for (int i = cuerpo.length() - 1; i >= 0; i--) {
                if (contador > 0 && contador % 3 == 0) {
                    cuerpoFormateado.insert(0, ".");
                }
                cuerpoFormateado.insert(0, cuerpo.charAt(i));
                contador++;
            }

            return cuerpoFormateado + "-" + dv;
        } catch (Exception e) {
            logger.error("Error formateando RUT {}: {}", rut, e.getMessage());
            return rut;
        }
    }

    @Override
    public boolean validarCodigoProductoUnico(String codigo, Long excludeId) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return false;
        }

        try {
            return !productoServicio.existePorCodigo(codigo.trim().toUpperCase(), excludeId);
        } catch (Exception e) {
            logger.error("Error validando código de producto {}: {}", codigo, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validarFormatoEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    @Override
    public boolean validarEmailUnico(String email, Long excludeId) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        try {
            // Buscar en clientes
            boolean existeEnClientes = clienteRepositorio.existsByEmail(email.trim().toLowerCase());

            // Buscar en usuarios
            boolean existeEnUsuarios = usuarioRepositorio.findByEmail(email.trim().toLowerCase()).isPresent();

            if (excludeId != null) {
                // Si estamos editando, verificar que el email no pertenezca a otro registro
                var clienteActual = clienteRepositorio.findById(excludeId);
                var usuarioActual = usuarioRepositorio.findById(excludeId);

                if (clienteActual.isPresent() && email.trim().toLowerCase().equals(clienteActual.get().getEmail())) {
                    existeEnClientes = false;
                }

                if (usuarioActual.isPresent() && email.trim().toLowerCase().equals(usuarioActual.get().getEmail())) {
                    existeEnUsuarios = false;
                }
            }

            return !existeEnClientes && !existeEnUsuarios;
        } catch (Exception e) {
            logger.error("Error validando email único {}: {}", email, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validarUsernameUnico(String username, Long excludeId) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        try {
            var usuario = usuarioRepositorio.findByUsername(username.trim().toLowerCase());

            if (usuario.isEmpty()) {
                return true;
            }

            // Si estamos editando y el username pertenece al mismo usuario, es válido
            return excludeId != null && usuario.get().getId().equals(excludeId);
        } catch (Exception e) {
            logger.error("Error validando username único {}: {}", username, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validarStockSuficiente(Long productoId, Integer cantidadSolicitada) {
        if (productoId == null || cantidadSolicitada == null || cantidadSolicitada <= 0) {
            return false;
        }

        try {
            informviva.gest.dto.ProductoDTO producto = productoServicio.buscarPorId(productoId);
            if (producto == null || !producto.getActivo()) {
                return false;
            }

            Integer stockDisponible = producto.getStock();
            return stockDisponible != null && stockDisponible >= cantidadSolicitada;
        } catch (Exception e) {
            logger.error("Error validando stock para producto {}: {}", productoId, e.getMessage());
            return false;
        }
    }

    @Override
    public Integer obtenerStockDisponible(Long productoId) {
        if (productoId == null) {
            return 0;
        }

        try {
            informviva.gest.dto.ProductoDTO producto = productoServicio.buscarPorId(productoId);
            return producto != null ? producto.getStock() : 0;
        } catch (Exception e) {
            logger.error("Error obteniendo stock de producto {}: {}", productoId, e.getMessage());
            return 0;
        }
    }

    @Override
    public boolean validarEliminacionCliente(Long clienteId) {
        if (clienteId == null) {
            return false;
        }

        try {
            return !ventaServicio.existenVentasPorCliente(clienteId);
        } catch (Exception e) {
            logger.error("Error validando eliminación de cliente {}: {}", clienteId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validarEliminacionProducto(Long productoId) {
        if (productoId == null) {
            return false;
        }

        try {
            return !ventaServicio.existenVentasPorProducto(productoId);
        } catch (Exception e) {
            logger.error("Error validando eliminación de producto {}: {}", productoId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validarRangoFechas(String fechaInicio, String fechaFin) {
        if (fechaInicio == null || fechaFin == null ||
                fechaInicio.trim().isEmpty() || fechaFin.trim().isEmpty()) {
            return false;
        }

        try {
            LocalDate inicio = LocalDate.parse(fechaInicio.trim());
            LocalDate fin = LocalDate.parse(fechaFin.trim());

            return !inicio.isAfter(fin);
        } catch (DateTimeParseException e) {
            logger.error("Error parseando fechas {} - {}: {}", fechaInicio, fechaFin, e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> validarVentaCompleta(Map<String, Object> ventaData) {
        Map<String, Object> resultado = new HashMap<>();
        List<String> errores = new ArrayList<>();
        boolean esValido = true;

        try {
            // Validar cliente
            Object clienteId = ventaData.get("clienteId");
            if (clienteId == null) {
                errores.add("Cliente es obligatorio");
                esValido = false;
            }

            // Validar vendedor
            Object vendedorId = ventaData.get("vendedorId");
            if (vendedorId == null) {
                errores.add("Vendedor es obligatorio");
                esValido = false;
            }

            // Validar detalles de venta
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> detalles = (List<Map<String, Object>>) ventaData.get("detalles");
            if (detalles == null || detalles.isEmpty()) {
                errores.add("Debe incluir al menos un producto");
                esValido = false;
            } else {
                for (int i = 0; i < detalles.size(); i++) {
                    Map<String, Object> detalle = detalles.get(i);

                    Object productoId = detalle.get("productoId");
                    Object cantidad = detalle.get("cantidad");

                    if (productoId == null) {
                        errores.add("Producto es obligatorio en la línea " + (i + 1));
                        esValido = false;
                        continue;
                    }

                    if (cantidad == null) {
                        errores.add("Cantidad es obligatoria en la línea " + (i + 1));
                        esValido = false;
                        continue;
                    }

                    try {
                        Long prodId = Long.valueOf(productoId.toString());
                        Integer cant;
                        if (cantidad instanceof Integer) {
                            cant = (Integer) cantidad;
                        } else {
                            cant = Integer.valueOf(cantidad.toString());
                        }


                        if (cant <= 0) {
                            errores.add("La cantidad debe ser mayor que cero en la línea " + (i + 1));
                            esValido = false;
                        } else if (!validarStockSuficiente(prodId, cant)) {
                            Integer stockDisponible = obtenerStockDisponible(prodId);
                            errores.add("Stock insuficiente en la línea " + (i + 1) +
                                    ". Disponible: " + stockDisponible + ", Solicitado: " + cant);
                            esValido = false;
                        }
                    } catch (NumberFormatException e) {
                        errores.add("Datos inválidos en la línea " + (i + 1));
                        esValido = false;
                    }
                }
            }

            resultado.put("valido", esValido);
            resultado.put("errores", errores);
            resultado.put("mensaje", esValido ? "Validación exitosa" : "Se encontraron errores en la validación");
            resultado.put("timestamp", LocalDateTime.now());

        } catch (Exception e) {
            logger.error("Error en validación completa de venta: {}", e.getMessage());
            resultado.put("valido", false);
            resultado.put("errores", List.of("Error interno en la validación: " + e.getMessage()));
            resultado.put("mensaje", "Error interno en la validación");
            resultado.put("timestamp", LocalDateTime.now());
        }

        return resultado;
    }

    @Override
    public boolean validarFortalezaContrasena(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password.trim()).matches();
    }

    @Override
    public boolean validarTelefonoChileno(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            return true; // Teléfono es opcional en la mayoría de casos
        }

        // Limpiar el teléfono de espacios y caracteres especiales
        String telefonoLimpio = telefono.replaceAll("[\\s\\-\\(\\)]", "");
        return TELEFONO_CHILENO_PATTERN.matcher(telefonoLimpio).matches();
    }

    @Override
    public boolean validarFechaNegocio(String fecha) {
        if (fecha == null || fecha.trim().isEmpty()) {
            return false;
        }

        try {
            LocalDate fechaValidar = LocalDate.parse(fecha.trim());
            LocalDate hoy = LocalDate.now();

            // La fecha no puede ser futura (más de 1 día en el futuro)
            // y no puede ser muy antigua (más de 5 años atrás)
            LocalDate fechaLimiteAnterior = hoy.minusYears(5);
            LocalDate fechaLimiteFutura = hoy.plusDays(1);

            return !fechaValidar.isBefore(fechaLimiteAnterior) &&
                    !fechaValidar.isAfter(fechaLimiteFutura);
        } catch (DateTimeParseException e) {
            logger.error("Error parseando fecha {}: {}", fecha, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validarPrecio(Double precio) {
        if (precio == null) {
            return false;
        }

        // El precio debe ser mayor que 0 y menor que 10,000,000 (10 millones)
        return precio > 0.0 && precio <= 10_000_000.0;
    }
}