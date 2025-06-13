package informviva.gest.validador;


import informviva.gest.config.ImportacionConfig;
import informviva.gest.dto.ValidacionResultadoDTO;
import informviva.gest.validador.ValidadorRutUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Servicio especializado en validaciones para importación
 *
 * @author Roberto Rivas
 * @version 2.1
 */
@Service
public class ImportacionValidador {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern CODIGO_PRODUCTO_PATTERN = Pattern.compile(
            "^[A-Z0-9]{3,20}$");

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._-]{3,20}$");

    @Autowired
    private ImportacionConfig configuracion;

    /**
     * Valida archivo antes de procesamiento
     */
    public ValidacionResultadoDTO validarArchivo(MultipartFile archivo, String tipoEntidad) {
        ValidacionResultadoDTO resultado = new ValidacionResultadoDTO();
        resultado.setNombreArchivo(archivo.getOriginalFilename());
        resultado.setTamahoArchivo(archivo.getSize());
        resultado.setTipoEntidad(tipoEntidad);

        // Validaciones básicas del archivo
        validarArchivoBasico(archivo, resultado);

        // Validaciones específicas por tipo
        if (resultado.isValido()) {
            validarConfiguracionEntidad(tipoEntidad, resultado);
        }

        return resultado;
    }

    /**
     * Valida los datos de una fila específica
     */
    public ValidacionResultadoDTO validarFila(Map<String, Object> fila, String tipoEntidad, int numeroFila) {
        ValidacionResultadoDTO resultado = new ValidacionResultadoDTO();
        resultado.setNumeroFila(numeroFila);

        switch (tipoEntidad.toLowerCase()) {
            case "cliente":
                validarFilaCliente(fila, resultado);
                break;
            case "producto":
                validarFilaProducto(fila, resultado);
                break;
            case "usuario":
                validarFilaUsuario(fila, resultado);
                break;
            default:
                resultado.agregarError("Tipo de entidad no reconocido: " + tipoEntidad);
        }

        return resultado;
    }

    /**
     * Valida estructura de columnas
     */
    public ValidacionResultadoDTO validarEstructura(Set<String> columnasArchivo, String tipoEntidad) {
        ValidacionResultadoDTO resultado = new ValidacionResultadoDTO();

        List<String> columnasRequeridas = configuracion.getConfiguracionEntidad(tipoEntidad)
                .getCamposObligatorios();

        List<String> columnasFaltantes = new ArrayList<>();
        for (String columnaRequerida : columnasRequeridas) {
            if (!columnasArchivo.contains(columnaRequerida)) {
                columnasFaltantes.add(columnaRequerida);
            }
        }

        if (!columnasFaltantes.isEmpty()) {
            resultado.agregarError("Faltan las siguientes columnas obligatorias: " +
                    String.join(", ", columnasFaltantes));
        }

        return resultado;
    }

    // Métodos privados de validación

    private void validarArchivoBasico(MultipartFile archivo, ValidacionResultadoDTO resultado) {
        String nombreArchivo = archivo.getOriginalFilename();

        // Validar nombre
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            resultado.agregarError("Nombre de archivo inválido");
            return;
        }

        // Validar extensión
        String extension = obtenerExtension(nombreArchivo);
        if (!configuracion.getFormatosSoportados().contains(extension.toLowerCase())) {
            resultado.agregarError("Formato de archivo no soportado: " + extension +
                    ". Formatos permitidos: " + String.join(", ", configuracion.getFormatosSoportados()));
        }

        // Validar tamaño
        if (archivo.getSize() == 0) {
            resultado.agregarError("El archivo está vacío");
        } else if (archivo.getSize() > configuracion.getTamahoMaximoArchivo()) {
            resultado.agregarError("El archivo excede el tamaño máximo permitido: " +
                    formatearTamaño(configuracion.getTamahoMaximoArchivo()));
        }
    }

    private void validarConfiguracionEntidad(String tipoEntidad, ValidacionResultadoDTO resultado) {
        ImportacionConfig.EntidadConfig entidadConfig = configuracion.getConfiguracionEntidad(tipoEntidad);

        if (entidadConfig.getCamposObligatorios().isEmpty()) {
            resultado.agregarAdvertencia("No hay configuración específica para " + tipoEntidad);
        }
    }

    private void validarFilaCliente(Map<String, Object> fila, ValidacionResultadoDTO resultado) {
        // Validar campos obligatorios
        validarCampoObligatorio(fila, "nombre", resultado);
        validarCampoObligatorio(fila, "apellido", resultado);
        validarCampoObligatorio(fila, "email", resultado);
        validarCampoObligatorio(fila, "rut", resultado);

        // Validaciones específicas
        String email = obtenerValorString(fila, "email");
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            resultado.agregarError("Email inválido: " + email);
        }

        String rut = obtenerValorString(fila, "rut");
        if (!rut.isEmpty() && !ValidadorRutUtil.validar(rut)) {
            resultado.agregarError("RUT inválido: " + rut);
        }

        // Validar longitudes
        if (obtenerValorString(fila, "nombre").length() > 50) {
            resultado.agregarError("Nombre demasiado largo (máximo 50 caracteres)");
        }
        if (obtenerValorString(fila, "apellido").length() > 50) {
            resultado.agregarError("Apellido demasiado largo (máximo 50 caracteres)");
        }
    }

    private void validarFilaProducto(Map<String, Object> fila, ValidacionResultadoDTO resultado) {
        // Validar campos obligatorios
        validarCampoObligatorio(fila, "codigo", resultado);
        validarCampoObligatorio(fila, "nombre", resultado);
        validarCampoObligatorio(fila, "precio", resultado);

        // Validaciones específicas
        String codigo = obtenerValorString(fila, "codigo").toUpperCase();
        if (!codigo.isEmpty() && !CODIGO_PRODUCTO_PATTERN.matcher(codigo).matches()) {
            resultado.agregarError("Código de producto inválido: " + codigo +
                    ". Debe contener solo letras mayúsculas y números (3-20 caracteres)");
        }

        String precioStr = obtenerValorString(fila, "precio");
        if (!precioStr.isEmpty()) {
            try {
                double precio = Double.parseDouble(precioStr);
                if (precio <= 0) {
                    resultado.agregarError("El precio debe ser mayor que cero");
                }
                if (precio > 999999.99) {
                    resultado.agregarError("El precio excede el máximo permitido");
                }
            } catch (NumberFormatException e) {
                resultado.agregarError("Precio inválido: " + precioStr);
            }
        }

        String stockStr = obtenerValorString(fila, "stock");
        if (!stockStr.isEmpty()) {
            try {
                int stock = Integer.parseInt(stockStr);
                if (stock < 0) {
                    resultado.agregarError("El stock no puede ser negativo");
                }
                if (stock > 999999) {
                    resultado.agregarError("El stock excede el máximo permitido");
                }
            } catch (NumberFormatException e) {
                resultado.agregarError("Stock inválido: " + stockStr);
            }
        }
    }

    private void validarFilaUsuario(Map<String, Object> fila, ValidacionResultadoDTO resultado) {
        // Validar campos obligatorios
        validarCampoObligatorio(fila, "username", resultado);
        validarCampoObligatorio(fila, "password", resultado);
        validarCampoObligatorio(fila, "nombre", resultado);
        validarCampoObligatorio(fila, "apellido", resultado);
        validarCampoObligatorio(fila, "email", resultado);

        // Validaciones específicas
        String username = obtenerValorString(fila, "username");
        if (!username.isEmpty() && !USERNAME_PATTERN.matcher(username).matches()) {
            resultado.agregarError("Username inválido: " + username +
                    ". Debe contener solo letras, números, puntos, guiones y guiones bajos (3-20 caracteres)");
        }

        String email = obtenerValorString(fila, "email");
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            resultado.agregarError("Email inválido: " + email);
        }

        String password = obtenerValorString(fila, "password");
        if (!password.isEmpty()) {
            if (password.length() < 6) {
                resultado.agregarError("La contraseña debe tener al menos 6 caracteres");
            }
            if (password.length() > 100) {
                resultado.agregarError("La contraseña es demasiado larga (máximo 100 caracteres)");
            }
        }

        // Validar roles si están presentes
        String roles = obtenerValorString(fila, "roles");
        if (!roles.isEmpty()) {
            List<String> rolesPermitidos = List.of("ADMIN", "VENTAS", "PRODUCTOS", "GERENTE", "USER");
            String[] rolesArray = roles.split(";");
            for (String rol : rolesArray) {
                rol = rol.trim().toUpperCase();
                if (!rolesPermitidos.contains(rol)) {
                    resultado.agregarAdvertencia("Rol desconocido: " + rol +
                            ". Se asignará rol USER por defecto");
                }
            }
        }
    }

    private void validarCampoObligatorio(Map<String, Object> fila, String campo, ValidacionResultadoDTO resultado) {
        String valor = obtenerValorString(fila, campo);
        if (valor.trim().isEmpty()) {
            resultado.agregarError("Campo obligatorio faltante: " + campo);
        }
    }

    private String obtenerValorString(Map<String, Object> fila, String campo) {
        Object valor = fila.get(campo);
        return valor != null ? valor.toString().trim() : "";
    }

    private String obtenerExtension(String nombreArchivo) {
        int ultimoPunto = nombreArchivo.lastIndexOf('.');
        return ultimoPunto > 0 ? nombreArchivo.substring(ultimoPunto + 1) : "";
    }

    private String formatearTamaño(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        return (bytes / (1024 * 1024)) + " MB";
    }
}