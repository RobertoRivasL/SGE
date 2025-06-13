package informviva.gest.service;

import informviva.gest.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Servicio para restauración de datos desde respaldos
 */
@Service
public class RestauracionServicio {

    private static final Logger logger = LoggerFactory.getLogger(RestauracionServicio.class);

    @Autowired
    private ClienteServicio clienteServicio;

    @Autowired
    private ProductoServicio productoServicio;

    @Autowired
    private VentaServicio ventaServicio;

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private ConfiguracionServicio configuracionServicio;

    private final ObjectMapper objectMapper;

    public RestauracionServicio() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    // ** Métodos públicos **

    public InfoRestauracion analizarRespaldo(MultipartFile archivo) {
        InfoRestauracion info = inicializarInfoRestauracion(archivo);

        try {
            if (archivo.getOriginalFilename().endsWith(".zip")) {
                analizarRespaldoZip(archivo, info);
            } else if (archivo.getOriginalFilename().endsWith(".json")) {
                analizarRespaldoConfiguracion(archivo, info);
            } else {
                info.setValido(false);
                info.setMensajeError("Formato de archivo no soportado");
            }
        } catch (Exception e) {
            manejarErrorAnalisis(info, e);
        }

        return info;
    }

    public ResultadoRestauracion restaurarDesdeArchivo(MultipartFile archivo, boolean sobrescribir) {
        ResultadoRestauracion resultado = inicializarResultadoRestauracion();

        try {
            if (archivo.getOriginalFilename().endsWith(".zip")) {
                resultado = restaurarDesdeZip(archivo, sobrescribir);
            } else if (archivo.getOriginalFilename().endsWith(".json")) {
                resultado = restaurarConfiguracion(archivo);
            } else {
                resultado.setExitoso(false);
                resultado.setMensajeError("Formato de archivo no soportado");
            }
        } catch (Exception e) {
            manejarErrorRestauracion(resultado, e);
        }

        return resultado;
    }

    public ResultadoValidacion validarIntegridadRespaldo(MultipartFile archivo) {
        ResultadoValidacion resultado = inicializarResultadoValidacion(archivo);

        try {
            if (archivo.getOriginalFilename().endsWith(".zip")) {
                validarRespaldoZip(archivo, resultado);
            } else if (archivo.getOriginalFilename().endsWith(".json")) {
                validarRespaldoJson(archivo, resultado);
            } else {
                resultado.setValido(false);
                resultado.getErrores().add("Formato de archivo no soportado");
            }
        } catch (Exception e) {
            manejarErrorValidacion(resultado, e);
        }

        return resultado;
    }

    // ** Métodos privados auxiliares **

    private InfoRestauracion inicializarInfoRestauracion(MultipartFile archivo) {
        InfoRestauracion info = new InfoRestauracion();
        info.setNombreArchivo(archivo.getOriginalFilename());
        info.setTamanoArchivo(archivo.getSize());
        info.setFechaAnalisis(LocalDateTime.now());
        return info;
    }

    private ResultadoRestauracion inicializarResultadoRestauracion() {
        ResultadoRestauracion resultado = new ResultadoRestauracion();
        resultado.setFechaInicio(LocalDateTime.now());
        return resultado;
    }

    private ResultadoValidacion inicializarResultadoValidacion(MultipartFile archivo) {
        ResultadoValidacion resultado = new ResultadoValidacion();
        resultado.setNombreArchivo(archivo.getOriginalFilename());
        resultado.setFechaValidacion(LocalDateTime.now());
        return resultado;
    }

    private void manejarErrorAnalisis(InfoRestauracion info, Exception e) {
        logger.error("Error analizando respaldo: {}", e.getMessage());
        info.setValido(false);
        info.setMensajeError("Error analizando archivo: " + e.getMessage());
    }

    private void manejarErrorRestauracion(ResultadoRestauracion resultado, Exception e) {
        logger.error("Error en restauración: {}", e.getMessage());
        resultado.setExitoso(false);
        resultado.setMensajeError("Error durante restauración: " + e.getMessage());
        resultado.setFechaFin(LocalDateTime.now());
    }

    private void manejarErrorValidacion(ResultadoValidacion resultado, Exception e) {
        logger.error("Error validando integridad: {}", e.getMessage());
        resultado.setValido(false);
        resultado.getErrores().add("Error de validación: " + e.getMessage());
    }

    private void analizarRespaldoZip(MultipartFile archivo, InfoRestauracion info) throws IOException {
        Map<String, Integer> contadores = new HashMap<>();
        Set<ModuloRespaldo> modulosEncontrados = new HashSet<>();

        try (ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(archivo.getBytes()))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                procesarArchivoRespaldo(entry, zipIn, contadores, modulosEncontrados, info);
                zipIn.closeEntry();
            }
        }

        info.setModulosDisponibles(modulosEncontrados);
        info.setContadoresRegistros(contadores);
    }

    private void procesarArchivoRespaldo(ZipEntry entry, ZipInputStream zipIn, Map<String, Integer> contadores,
                                         Set<ModuloRespaldo> modulosEncontrados, InfoRestauracion info) throws IOException {
        String nombreArchivo = entry.getName();
        String contenido = leerEntradaZip(zipIn);

        if (nombreArchivo.equals("info_respaldo.json")) {
            InfoRespaldo infoRespaldo = objectMapper.readValue(contenido, InfoRespaldo.class);
            info.setVersionRespaldo(infoRespaldo.getVersion());
            info.setFechaRespaldo(infoRespaldo.getFechaCreacion());
            info.setTipoRespaldo(infoRespaldo.getTipo());
            modulosEncontrados.addAll(infoRespaldo.getModulos());
        } else {
            actualizarContadores(nombreArchivo, contenido, contadores);
        }
    }

    private void actualizarContadores(String nombreArchivo, String contenido, Map<String, Integer> contadores) throws IOException {
        switch (nombreArchivo) {
            case "clientes.json":
                RespaldoClientes respaldoClientes = objectMapper.readValue(contenido, RespaldoClientes.class);
                contadores.put("clientes", respaldoClientes.getTotal());
                break;
            case "productos.json":
                RespaldoProductos respaldoProductos = objectMapper.readValue(contenido, RespaldoProductos.class);
                contadores.put("productos", respaldoProductos.getTotal());
                break;
            case "ventas.json":
                RespaldoVentas respaldoVentas = objectMapper.readValue(contenido, RespaldoVentas.class);
                contadores.put("ventas", respaldoVentas.getTotal());
                break;
            case "usuarios.json":
                RespaldoUsuarios respaldoUsuarios = objectMapper.readValue(contenido, RespaldoUsuarios.class);
                contadores.put("usuarios", respaldoUsuarios.getTotal());
                break;
            case "configuracion.json":
                contadores.put("configuracion", 1);
                break;
        }
    }

    private String leerEntradaZip(ZipInputStream zipIn) throws IOException {
        StringBuilder contenido = new StringBuilder();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = zipIn.read(buffer)) > 0) {
            contenido.append(new String(buffer, 0, len));
        }
        return contenido.toString();
    }
}