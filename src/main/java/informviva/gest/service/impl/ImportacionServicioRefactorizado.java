package informviva.gest.service.impl;


import informviva.gest.config.ImportacionConfig;
import informviva.gest.dto.ImportacionResultadoDTO;
import informviva.gest.dto.ValidacionResultadoDTO;
import informviva.gest.service.ImportacionServicio;
import informviva.gest.service.asincrono.ImportacionAsincronaServicio;
import informviva.gest.service.archivo.ProcesadorArchivo;
import informviva.gest.service.plantilla.GeneradorPlantilla;
import informviva.gest.service.procesador.ProcesadorFactory;
import informviva.gest.service.validacion.ImportacionValidador;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Implementación refactorizada del servicio de importación
 * Separación de responsabilidades y mejores prácticas
 *
 * @author Roberto Rivas
 * @version 2.1
 */
@Service
public class ImportacionServicioRefactorizado implements ImportacionServicio {

    private static final Logger logger = LoggerFactory.getLogger(ImportacionServicioRefactorizado.class);

    @Autowired
    private ImportacionConfig configuracion;

    @Autowired
    private ImportacionValidador validador;

    @Autowired
    private ProcesadorArchivo procesadorArchivo;

    @Autowired
    private GeneradorPlantilla generadorPlantilla;

    @Autowired
    private ProcesadorFactory procesadorFactory;

    @Autowired
    private ImportacionAsincronaServicio importacionAsincrona;

    @Override
    public ImportacionResultadoDTO importarClientes(MultipartFile archivo) {
        return procesarImportacion(archivo, "cliente");
    }

    @Override
    public ImportacionResultadoDTO importarProductos(MultipartFile archivo) {
        return procesarImportacion(archivo, "producto");
    }

    @Override
    public ImportacionResultadoDTO importarUsuarios(MultipartFile archivo) {
        return procesarImportacion(archivo, "usuario");
    }

    /**
     * Método unificado para procesar cualquier tipo de importación
     */
    private ImportacionResultadoDTO procesarImportacion(MultipartFile archivo, String tipoEntidad) {
        logger.info("Iniciando importación de {} desde archivo: {}", tipoEntidad, archivo.getOriginalFilename());

        ImportacionResultadoDTO resultado = new ImportacionResultadoDTO();
        resultado.setTipoEntidad(capitalizarPrimeraLetra(tipoEntidad));
        resultado.setNombreArchivo(archivo.getOriginalFilename());
        resultado.setFechaImportacion(LocalDateTime.now());

        try {
            // 1. Validación inicial del archivo
            ValidacionResultadoDTO validacionArchivo = validador.validarArchivo(archivo, tipoEntidad);
            if (!validacionArchivo.isValido()) {
                validacionArchivo.getErrores().forEach(resultado::agregarError);
                validacionArchivo.getAdvertencias().forEach(resultado::agregarAdvertencia);
                return resultado;
            }

            // 2. Procesar datos del archivo
            List<Map<String, Object>> datos = procesadorArchivo.procesarArchivo(archivo);
            if (datos.isEmpty()) {
                resultado.agregarError("El archivo no contiene datos válidos");
                return resultado;
            }

            // 3. Validar estructura de columnas
            Set<String> columnasArchivo = datos.get(0).keySet();
            ValidacionResultadoDTO validacionEstructura = validador.validarEstructura(columnasArchivo, tipoEntidad);
            if (!validacionEstructura.isValido()) {
                validacionEstructura.getErrores().forEach(resultado::agregarError);
                return resultado;
            }

            // 4. Decidir entre procesamiento síncrono o asíncrono
            if (configuracion.isProcesamientoAsincrono() &&
                    datos.size() > configuracion.getLoteMaximo()) {

                logger.info("Archivo grande detectado ({}), usando procesamiento asíncrono", datos.size());
                return procesarDeFormaAsincrona(tipoEntidad, datos, archivo.getOriginalFilename());

            } else {
                // 5. Procesamiento síncrono para archivos pequeños
                return procesarDeFormaSincrona(tipoEntidad, datos, resultado);
            }

        } catch (Exception e) {
            logger.error("Error durante la importación de {}: {}", tipoEntidad, e.getMessage(), e);
            resultado.agregarError("Error general: " + e.getMessage());
            return resultado;
        }
    }

    /**
     * Procesamiento síncrono para archivos pequeños
     */
    private ImportacionResultadoDTO procesarDeFormaSincrona(
            String tipoEntidad,
            List<Map<String, Object>> datos,
            ImportacionResultadoDTO resultado) {

        long tiempoInicio = System.currentTimeMillis();

        try {
            var procesador = procesadorFactory.obtenerProcesador(tipoEntidad);

            for (int i = 0; i < datos.size(); i++) {
                Map<String, Object> fila = datos.get(i);
                int numeroFila = i + 2; // +2 porque empezamos en fila 2

                try {
                    // Mapear entidad
                    Object entidad = procesador.mapearDesdeArchivo(fila, numeroFila);

                    // Validar entidad
                    ValidacionResultadoDTO validacion = procesador.validarEntidad(entidad, numeroFila);

                    if (validacion.isValido()) {
                        // Verificar duplicados
                        if (procesador.existeEntidad(entidad)) {
                            resultado.agregarAdvertencia("Fila " + numeroFila + ": Registro duplicado, se omite");
                            resultado.incrementarOmitidos();
                        } else {
                            // Guardar entidad
                            procesador.guardarEntidad(entidad);
                            resultado.incrementarExitosos();
                        }
                    } else {
                        // Agregar errores de validación
                        validacion.getErrores().forEach(error ->
                                resultado.agregarError("Fila " + numeroFila + ": " + error));
                        resultado.incrementarErrores();
                    }

                    // Agregar advertencias si las hay
                    validacion.getAdvertencias().forEach(adv ->
                            resultado.agregarAdvertencia("Fila " + numeroFila + ": " + adv));

                } catch (Exception e) {
                    logger.error("Error procesando fila {}: {}", numeroFila, e.getMessage());
                    resultado.agregarError("Fila " + numeroFila + ": " + e.getMessage());
                    resultado.incrementarErrores();
                }
            }

        } catch (Exception e) {
            logger.error("Error en procesamiento síncrono: {}", e.getMessage());
            resultado.agregarError("Error en procesamiento: " + e.getMessage());
        }

        // Calcular tiempo y totales
        resultado.setTiempoProcesamientoMs(System.currentTimeMillis() - tiempoInicio);
        resultado.calcularTotales();

        logger.info("Importación síncrona completada: {} exitosos, {} errores, {} omitidos en {} ms",
                resultado.getRegistrosExitosos(), resultado.getRegistrosConError(),
                resultado.getRegistrosOmitidos(), resultado.getTiempoProcesamientoMs());

        return resultado;
    }

    /**
     * Procesamiento asíncrono para archivos grandes
     */
    private ImportacionResultadoDTO procesarDeFormaAsincrona(
            String tipoEntidad,
            List<Map<String, Object>> datos,
            String nombreArchivo) {

        String idProceso = UUID.randomUUID().toString();

        // Iniciar procesamiento asíncrono
        CompletableFuture<ImportacionResultadoDTO> futuro = importacionAsincrona
                .procesarImportacionAsincrona(tipoEntidad, datos, nombreArchivo, idProceso);

        // Crear resultado inmediato con información del proceso
        ImportacionResultadoDTO resultado = new ImportacionResultadoDTO();
        resultado.setTipoEntidad(capitalizarPrimeraLetra(tipoEntidad));
        resultado.setNombreArchivo(nombreArchivo);
        resultado.setFechaImportacion(LocalDateTime.now());
        resultado.agregarMensajeInfo("Procesamiento iniciado de forma asíncrona con ID: " + idProceso);
        resultado.agregarMensajeInfo("Puede consultar el progreso en /importacion/progreso/" + idProceso);

        return resultado;
    }

    @Override
    public Map<String, Object> validarArchivoImportacion(MultipartFile archivo, String tipoEntidad) {
        Map<String, Object> resultado = new HashMap<>();

        try {
            // Validar archivo
            ValidacionResultadoDTO validacion = validador.validarArchivo(archivo, tipoEntidad);

            resultado.put("valido", validacion.isValido());
            resultado.put("errores", validacion.getErrores());
            resultado.put("advertencias", validacion.getAdvertencias());
            resultado.put("informacion", validacion.getInformacion());

            // Si es válido, procesar muestra
            if (validacion.isValido()) {
                try {
                    List<Map<String, Object>> muestra = obtenerVistaPreviaArchivo(
                            archivo, configuracion.getVistaPreviaMaxima());

                    resultado.put("vistaPrevia", muestra);
                    resultado.put("totalFilas", muestra.size());

                    if (!muestra.isEmpty()) {
                        resultado.put("columnas", new ArrayList<>(muestra.get(0).keySet()));

                        // Validar estructura
                        ValidacionResultadoDTO validacionEstructura = validador
                                .validarEstructura(muestra.get(0).keySet(), tipoEntidad);

                        if (!validacionEstructura.isValido()) {
                            resultado.put("valido", false);
                            validacionEstructura.getErrores().forEach(error ->
                                    ((List<String>) resultado.get("errores")).add(error));
                        }
                    }

                } catch (Exception e) {
                    logger.error("Error obteniendo vista previa: {}", e.getMessage());
                    resultado.put("advertencias",
                            Arrays.asList("No se pudo generar vista previa: " + e.getMessage()));
                }
            }

        } catch (Exception e) {
            logger.error("Error validando archivo: {}", e.getMessage());
            resultado.put("valido", false);
            resultado.put("errores", Arrays.asList("Error validando archivo: " + e.getMessage()));
        }

        return resultado;
    }

    @Override
    public byte[] generarPlantillaImportacion(String tipoEntidad, String formato) {
        try {
            return generadorPlantilla.generarPlantilla(tipoEntidad, formato);
        } catch (Exception e) {
            logger.error("Error generando plantilla para {}: {}", tipoEntidad, e.getMessage());
            return new byte[0];
        }
    }

    @Override
    public List<Map<String, Object>> obtenerVistaPreviaArchivo(MultipartFile archivo, int limite) {
        try {
            List<Map<String, Object>> todosDatos = procesadorArchivo.procesarArchivo(archivo);
            return todosDatos.stream()
                    .limit(limite)
                    .toList();
        } catch (Exception e) {
            logger.error("Error obteniendo vista previa: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> obtenerFormatosSoportados() {
        return new ArrayList<>(configuracion.getFormatosSoportados());
    }

    @Override
    public List<String> obtenerColumnasRequeridas(String tipoEntidad) {
        ImportacionConfig.EntidadConfig config = configuracion.getConfiguracionEntidad(tipoEntidad);
        return new ArrayList<>(config.getCamposObligatorios());
    }

    /**
     * Obtiene el estado de un proceso asíncrono
     */
    public ImportacionAsincronaServicio.ProcesoImportacion obtenerEstadoProceso(String idProceso) {
        return importacionAsincrona.obtenerEstadoProceso(idProceso);
    }

    /**
     * Cancela un proceso asíncrono
     */
    public boolean cancelarProceso(String idProceso) {
        return importacionAsincrona.cancelarProceso(idProceso);
    }

    /**
     * Obtiene estadísticas de uso del módulo
     */
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();

        // Procesos activos
        Map<String, ImportacionAsincronaServicio.ProcesoImportacion> procesosActivos =
                importacionAsincrona.obtenerProcesosActivos();

        estadisticas.put("procesosActivos", procesosActivos.size());
        estadisticas.put("procesosPorEstado",
                procesosActivos.values().stream()
                        .collect(java.util.stream.Collectors.groupingBy(
                                p -> p.getEstado().name(),
                                java.util.stream.Collectors.counting())));

        // Configuración actual
        estadisticas.put("configuracion", Map.of(
                "tamahoMaximoArchivo", configuracion.getTamahoMaximoArchivo(),
                "loteMaximo", configuracion.getLoteMaximo(),
                "procesamientoAsincrono", configuracion.isProcesamientoAsincrono(),
                "formatosSoportados", configuracion.getFormatosSoportados(),
                "tiposEntidadDisponibles", procesadorFactory.obtenerTiposDisponibles()
        ));

        return estadisticas;
    }

    private String capitalizarPrimeraLetra(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        return texto.substring(0, 1).toUpperCase() + texto.substring(1).toLowerCase();
    }
}