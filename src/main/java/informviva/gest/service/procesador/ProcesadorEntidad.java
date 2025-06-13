package informviva.gest.service.procesador;


import informviva.gest.dto.ValidacionResultadoDTO;
import informviva.gest.model.Cliente;
import informviva.gest.model.Producto;
import informviva.gest.model.Usuario;
import informviva.gest.service.ClienteServicio;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.UsuarioServicio;
import informviva.gest.validador.ImportacionValidador;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interfaz común para procesadores de entidades
 */
public interface ProcesadorEntida {
    String getTipoEntidad();
    T mapearDesdeArchivo(Map<String, Object> fila, int numeroFila);
    boolean existeEntidad(T entidad);
    void guardarEntidad(T entidad);
    ValidacionResultadoDTO validarEntidad(T entidad, int numeroFila);
}

/**
 * Procesador especializado para clientes
 */
@Component
public class ProcesadorCliente implements ProcesadorEntidadT<Cliente> {

    private static final Logger logger = LoggerFactory.getLogger(ProcesadorCliente.class);

    @Autowired
    private ClienteServicio clienteServicio;

    @Autowired
    private ImportacionValidador validador;

    @Override
    public String getTipoEntidad() {
        return "cliente";
    }

    @Override
    @Transactional
    public Cliente mapearDesdeArchivo(Map<String, Object> fila, int numeroFila) {
        try {
            Cliente cliente = new Cliente();

            cliente.setNombre(obtenerValorString(fila, "nombre"));
            cliente.setApellido(obtenerValorString(fila, "apellido"));
            cliente.setEmail(obtenerValorString(fila, "email"));
            cliente.setRut(obtenerValorString(fila, "rut"));
            cliente.setTelefono(obtenerValorString(fila, "telefono"));
            cliente.setDireccion(obtenerValorString(fila, "direccion"));
            cliente.setCategoria(obtenerValorString(fila, "categoria"));
            cliente.setFechaRegistro(LocalDate.now());

            return cliente;
        } catch (Exception e) {
            logger.error("Error mapeando cliente en fila {}: {}", numeroFila, e.getMessage());
            throw new RuntimeException("Error en fila " + numeroFila + ": " + e.getMessage());
        }
    }

    @Override
    public boolean existeEntidad(Cliente cliente) {
        return clienteServicio.existeClienteConRut(cliente.getRut());
    }

    @Override
    @Transactional
    public void guardarEntidad(Cliente cliente) {
        clienteServicio.guardar(cliente);
    }

    @Override
    public ValidacionResultadoDTO validarEntidad(Cliente cliente, int numeroFila) {
        // Crear mapa temporal para usar el validador existente
        Map<String, Object> fila = Map.of(
                "nombre", cliente.getNombre(),
                "apellido", cliente.getApellido(),
                "email", cliente.getEmail(),
                "rut", cliente.getRut(),
                "telefono", cliente.getTelefono() != null ? cliente.getTelefono() : "",
                "direccion", cliente.getDireccion() != null ? cliente.getDireccion() : "",
                "categoria", cliente.getCategoria() != null ? cliente.getCategoria() : ""
        );

        return validador.validarFila(fila, getTipoEntidad(), numeroFila);
    }

    private String obtenerValorString(Map<String, Object> fila, String campo) {
        Object valor = fila.get(campo);
        return valor != null ? valor.toString().trim() : "";
    }
}

/**
 * Procesador especializado para productos
 */
@Component
public class ProcesadorProducto implements ProcesadorEntidadT<Producto> {

    private static final Logger logger = LoggerFactory.getLogger(ProcesadorProducto.class);

    @Autowired
    private ProductoServicio productoServicio;

    @Autowired
    private ImportacionValidador validador;

    @Override
    public String getTipoEntidad() {
        return "producto";
    }

    @Override
    @Transactional
    public Producto mapearDesdeArchivo(Map<String, Object> fila, int numeroFila) {
        try {
            Producto producto = new Producto();

            producto.setCodigo(obtenerValorString(fila, "codigo").toUpperCase());
            producto.setNombre(obtenerValorString(fila, "nombre"));
            producto.setDescripcion(obtenerValorString(fila, "descripcion"));
            producto.setMarca(obtenerValorString(fila, "marca"));
            producto.setModelo(obtenerValorString(fila, "modelo"));

            // Precio
            String precioStr = obtenerValorString(fila, "precio");
            if (!precioStr.isEmpty()) {
                producto.setPrecio(Double.parseDouble(precioStr));
            }

            // Stock
            String stockStr = obtenerValorString(fila, "stock");
            if (!stockStr.isEmpty()) {
                producto.setStock(Integer.parseInt(stockStr));
            } else {
                producto.setStock(0);
            }

            producto.setActivo(true);
            producto.setFechaCreacion(LocalDateTime.now());
            producto.setFechaActualizacion(LocalDateTime.now());

            return producto;
        } catch (Exception e) {
            logger.error("Error mapeando producto en fila {}: {}", numeroFila, e.getMessage());
            throw new RuntimeException("Error en fila " + numeroFila + ": " + e.getMessage());
        }
    }

    @Override
    public boolean existeEntidad(Producto producto) {
        return productoServicio.existePorCodigo(producto.getCodigo());
    }

    @Override
    @Transactional
    public void guardarEntidad(Producto producto) {
        productoServicio.guardar(producto);
    }

    @Override
    public ValidacionResultadoDTO validarEntidad(Producto producto, int numeroFila) {
        Map<String, Object> fila = Map.of(
                "codigo", producto.getCodigo(),
                "nombre", producto.getNombre(),
                "descripcion", producto.getDescripcion() != null ? producto.getDescripcion() : "",
                "precio", producto.getPrecio() != null ? producto.getPrecio().toString() : "",
                "stock", producto.getStock() != null ? producto.getStock().toString() : "0",
                "marca", producto.getMarca() != null ? producto.getMarca() : "",
                "modelo", producto.getModelo() != null ? producto.getModelo() : ""
        );

        return validador.validarFila(fila, getTipoEntidad(), numeroFila);
    }

    private String obtenerValorString(Map<String, Object> fila, String campo) {
        Object valor = fila.get(campo);
        return valor != null ? valor.toString().trim() : "";
    }
}

/**
 * Procesador especializado para usuarios
 */
@Component
public class ProcesadorUsuario implements ProcesadorEntidadT<Usuario> {

    private static final Logger logger = LoggerFactory.getLogger(ProcesadorUsuario.class);

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private ImportacionValidador validador;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public String getTipoEntidad() {
        return "usuario";
    }

    @Override
    @Transactional
    public Usuario mapearDesdeArchivo(Map<String, Object> fila, int numeroFila) {
        try {
            Usuario usuario = new Usuario();

            usuario.setUsername(obtenerValorString(fila, "username"));
            usuario.setPassword(passwordEncoder.encode(obtenerValorString(fila, "password")));
            usuario.setNombre(obtenerValorString(fila, "nombre"));
            usuario.setApellido(obtenerValorString(fila, "apellido"));
            usuario.setEmail(obtenerValorString(fila, "email"));

            // Procesar roles
            String rolesStr = obtenerValorString(fila, "roles");
            Set<String> roles = new HashSet<>();
            if (!rolesStr.isEmpty()) {
                String[] rolesArray = rolesStr.split(";");
                for (String rol : rolesArray) {
                    roles.add(rol.trim().toUpperCase());
                }
            } else {
                roles.add("USER");
            }
            usuario.setRoles(roles);

            // Procesar estado activo
            String activoStr = obtenerValorString(fila, "activo");
            boolean activo = activoStr.isEmpty() ||
                    "true".equalsIgnoreCase(activoStr) ||
                    "1".equals(activoStr) ||
                    "sí".equalsIgnoreCase(activoStr) ||
                    "si".equalsIgnoreCase(activoStr);
            usuario.setActivo(activo);

            usuario.setFechaCreacion(LocalDate.now());
            usuario.setUltimoAcceso(LocalDate.now());

            return usuario;
        } catch (Exception e) {
            logger.error("Error mapeando usuario en fila {}: {}", numeroFila, e.getMessage());
            throw new RuntimeException("Error en fila " + numeroFila + ": " + e.getMessage());
        }
    }

    @Override
    public boolean existeEntidad(Usuario usuario) {
        return usuarioServicio.buscarPorUsername(usuario.getUsername()) != null;
    }

    @Override
    @Transactional
    public void guardarEntidad(Usuario usuario) {
        usuarioServicio.guardar(usuario);
    }

    @Override
    public ValidacionResultadoDTO validarEntidad(Usuario usuario, int numeroFila) {
        Map<String, Object> fila = Map.of(
                "username", usuario.getUsername(),
                "password", "********", // No exponer la contraseña
                "nombre", usuario.getNombre(),
                "apellido", usuario.getApellido(),
                "email", usuario.getEmail(),
                "roles", String.join(";", usuario.getRoles()),
                "activo", String.valueOf(usuario.isActivo())
        );

        return validador.validarFila(fila, getTipoEntidad(), numeroFila);
    }

    private String obtenerValorString(Map<String, Object> fila, String campo) {
        Object valor = fila.get(campo);
        return valor != null ? valor.toString().trim() : "";
    }
}

/**
 * Factory para obtener el procesador adecuado según el tipo de entidad
 */
@Component
public class ProcesadorFactory {

    @Autowired
    private List<ProcesadorEntidadT<?>> procesadores;

    @SuppressWarnings("unchecked")
    public <T> ProcesadorEntidadT<T> obtenerProcesador(String tipoEntidad) {
        return (ProcesadorEntidadT<T>) procesadores.stream()
                .filter(procesador -> procesador.getTipoEntidad().equalsIgnoreCase(tipoEntidad))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No hay procesador para: " + tipoEntidad));
    }

    public List<String> obtenerTiposDisponibles() {
        return procesadores.stream()
                .map(ProcesadorEntidadT::getTipoEntidad)
                .toList();
    }
}