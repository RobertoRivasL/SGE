package informviva.gest.service.asincrono;


import informviva.gest.config.ImportacionConfig;
import informviva.gest.dto.ImportacionResultadoDTO;
import informviva.gest.dto.ValidacionResultadoDTO;
import informviva.gest.service.procesador.ProcesadorEntidad;
import informviva.gest.service.procesador.ProcesadorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servicio para procesamiento asíncrono de importaciones
 *
 * @author Roberto Rivas
 * @version 2.1
 */
@Service
public class ImportacionAsincronaServicio {

    private static final Logger logger = LoggerFactory.getLogger(ImportacionAsincronaServicio.class);

    @Autowired
    private ImportacionConfig configuracion;

    @Autowired
    private ProcesadorFactory procesadorFactory;

    // Cache para seguimiento de procesamientos en curso
    private final Map<String, ProcesoImportacion> procesosEnCurso = new ConcurrentHashMap<>();

    /**
     * Procesa una importación de forma asíncrona
     */
    @Async("importacionTaskExecutor")
    @Transactional
    public CompletableFuture<ImportacionResultadoDTO> procesarImportacionAsincrona(
            String tipoEntidad,
            List<Map<String, Object>> datos,
            String nombreArchivo,
            String idProceso) {

        logger.info("Iniciando procesamiento asíncrono para {}: {} registros", tipoEntidad, datos.size());

        ImportacionResultadoDTO resultado = new ImportacionResultadoDTO();
        resultado.setTipoEntidad(tipoEntidad);
        resultado.setNombreArchivo(nombreArchivo);
        resultado.setFechaImportacion(LocalDateTime.now());

        ProcesoImportacion proceso = new ProcesoImportacion(idProceso, datos.size());
        procesosEnCurso.put(idProceso, proceso);

        try {
            long tiempoInicio = System.currentTimeMillis();

            // Obtener procesador específico para el tipo de entidad
            ProcesadorEntidad<Object> procesador = procesadorFactory.obtenerProcesador(tipoEntidad);

            // Configuración de lotes
            int tamañoLote = configuracion.getLoteMaximo();
            List<List<Map<String, Object>>> lotes = dividirEnLotes(datos, tamañoLote);

            logger.info("Procesando {} lotes de máximo {} registros cada uno", lotes.size(), tamañoLote);

            // Procesar cada lote
            for (int i = 0; i < lotes.size(); i++) {
                List<Map<String, Object>> lote = lotes.get(i);
                ImportacionResultadoDTO resultadoLote = procesarLote(procesador, lote, i * tamañoLote, proceso);

                // Combinar resultados
                combinarResultados(resultado, resultadoLote);

                // Actualizar progreso
                proceso.actualizarProgreso(resultadoLote.getRegistrosProcesados());

                logger.debug("Completado lote {}/{}: {} exitosos, {} errores",
                        i + 1, lotes.size(), resultadoLote.getRegistrosExitosos(), resultadoLote.getRegistrosConError());
            }

            // Calcular tiempo total
            long tiempoFinal = System.currentTimeMillis();
            resultado.setTiempoProcesamientoMs(tiempoFinal - tiempoInicio);
            resultado.calcularTotales();

            proceso.marcarComoCompletado();

            logger.info("Procesamiento asíncrono completado: {} exitosos, {} errores, {} omitidos en {} ms",
                    resultado.getRegistrosExitosos(), resultado.getRegistrosConError(),
                    resultado.getRegistrosOmitidos(), resultado.getTiempoProcesamientoMs());

        } catch (Exception e) {
            logger.error("Error en procesamiento asíncrono: {}", e.getMessage(), e);
            resultado.agregarError("Error general en procesamiento: " + e.getMessage());
            proceso.marcarComoError(e.getMessage());
        } finally {
            // Limpiar proceso después de un tiempo
            CompletableFuture.delayedExecutor(java.util.concurrent.TimeUnit.MINUTES.toSeconds(10),
                            java.util.concurrent.TimeUnit.SECONDS)
                    .execute(() -> procesosEnCurso.remove(idProceso));
        }

        return CompletableFuture.completedFuture(resultado);
    }

    /**
     * Procesa un lote de datos
     */
    private ImportacionResultadoDTO procesarLote(
            ProcesadorEntidad<Object> procesador,
            List<Map<String, Object>> lote,
            int offset,
            ProcesoImportacion proceso) {

        ImportacionResultadoDTO resultadoLote = new ImportacionResultadoDTO();
        AtomicInteger contador = new AtomicInteger(0);

        for (int i = 0; i < lote.size(); i++) {
            Map<String, Object> fila = lote.get(i);
            int numeroFila = offset + i + 2; // +2 porque empezamos en fila 2 (después del header)

            try {
                // Mapear entidad
                Object entidad = procesador.mapearDesdeArchivo(fila, numeroFila);

                // Validar entidad
                ValidacionResultadoDTO validacion = procesador.validarEntidad(entidad, numeroFila);

                if (validacion.isValido()) {
                    // Verificar duplicados
                    if (procesador.existeEntidad(entidad)) {
                        resultadoLote.agregarAdvertencia("Fila " + numeroFila + ": Registro duplicado, se omite");
                        resultadoLote.incrementarOmitidos();
                    } else {
                        // Guardar entidad
                        procesador.guardarEntidad(entidad);
                        resultadoLote.incrementarExitosos();
                    }
                } else {
                    // Agregar errores de validación
                    validacion.getErrores().forEach(error ->
                            resultadoLote.agregarError("Fila " + numeroFila + ": " + error));
                    resultadoLote.incrementarErrores();
                }

                // Agregar advertencias si las hay
                validacion.getAdvertencias().forEach(adv ->
                        resultadoLote.agregarAdvertencia("Fila " + numeroFila + ": " + adv));

            } catch (Exception e) {
                logger.error("Error procesando fila {}: {}", numeroFila, e.getMessage());
                resultadoLote.agregarError("Fila " + numeroFila + ": " + e.getMessage());
                resultadoLote.incrementarErrores();
            }

            // Actualizar progreso individual
            proceso.actualizarProgresoDetallado(contador.incrementAndGet());
        }

        return resultadoLote;
    }

    /**
     * Divide una lista en lotes más pequeños
     */
    private List<List<Map<String, Object>>> dividirEnLotes(List<Map<String, Object>> datos, int tamañoLote) {
        return java.util.stream.IntStream.range(0, (datos.size() + tamañoLote - 1) / tamañoLote)
                .mapToObj(i -> datos.subList(i * tamañoLote, Math.min((i + 1) * tamañoLote, datos.size())))
                .toList();
    }

    /**
     * Combina resultados de diferentes lotes
     */
    private void combinarResultados(ImportacionResultadoDTO principal, ImportacionResultadoDTO lote) {
        principal.setRegistrosExitosos(principal.getRegistrosExitosos() + lote.getRegistrosExitosos());
        principal.setRegistrosConError(principal.getRegistrosConError() + lote.getRegistrosConError());
        principal.setRegistrosOmitidos(principal.getRegistrosOmitidos() + lote.getRegistrosOmitidos());

        principal.getErrores().addAll(lote.getErrores());
        principal.getAdvertencias().addAll(lote.getAdvertencias());
        principal.getMensajesInfo().addAll(lote.getMensajesInfo());
    }

    /**
     * Obtiene el estado de un proceso en curso
     */
    public ProcesoImportacion obtenerEstadoProceso(String idProceso) {
        return procesosEnCurso.get(idProceso);
    }

    /**
     * Obtiene todos los procesos activos
     */
    public Map<String, ProcesoImportacion> obtenerProcesosActivos() {
        return new ConcurrentHashMap<>(procesosEnCurso);
    }

    /**
     * Cancela un proceso en curso
     */
    public boolean cancelarProceso(String idProceso) {
        ProcesoImportacion proceso = procesosEnCurso.get(idProceso);
        if (proceso != null && !proceso.isCompletado()) {
            proceso.marcarComoCancelado();
            procesosEnCurso.remove(idProceso);
            return true;
        }
        return false;
    }

    /**
     * Clase para seguimiento de procesos de importación
     */
    public static class ProcesoImportacion {
        private final String id;
        private final int totalRegistros;
        private final LocalDateTime fechaInicio;
        private final AtomicInteger registrosProcesados = new AtomicInteger(0);
        private final AtomicInteger registrosDetallados = new AtomicInteger(0);

        private volatile EstadoProceso estado = EstadoProceso.EN_PROGRESO;
        private volatile String mensajeError;
        private volatile LocalDateTime fechaFinalizacion;

        public ProcesoImportacion(String id, int totalRegistros) {
            this.id = id;
            this.totalRegistros = totalRegistros;
            this.fechaInicio = LocalDateTime.now();
        }

        public void actualizarProgreso(int procesados) {
            this.registrosProcesados.addAndGet(procesados);
        }

        public void actualizarProgresoDetallado(int procesado) {
            this.registrosDetallados.set(procesado);
        }

        public void marcarComoCompletado() {
            this.estado = EstadoProceso.COMPLETADO;
            this.fechaFinalizacion = LocalDateTime.now();
        }

        public void marcarComoError(String error) {
            this.estado = EstadoProceso.ERROR;
            this.mensajeError = error;
            this.fechaFinalizacion = LocalDateTime.now();
        }

        public void marcarComoCancelado() {
            this.estado = EstadoProceso.CANCELADO;
            this.fechaFinalizacion = LocalDateTime.now();
        }

        public double getPorcentajeProgreso() {
            if (totalRegistros == 0) return 0.0;
            return ((double) registrosProcesados.get() / totalRegistros) * 100.0;
        }

        public boolean isCompletado() {
            return estado == EstadoProceso.COMPLETADO ||
                    estado == EstadoProceso.ERROR ||
                    estado == EstadoProceso.CANCELADO;
        }

        // Getters
        public String getId() { return id; }
        public int getTotalRegistros() { return totalRegistros; }
        public LocalDateTime getFechaInicio() { return fechaInicio; }
        public int getRegistrosProcesados() { return registrosProcesados.get(); }
        public int getRegistrosDetallados() { return registrosDetallados.get(); }
        public EstadoProceso getEstado() { return estado; }
        public String getMensajeError() { return mensajeError; }
        public LocalDateTime getFechaFinalizacion() { return fechaFinalizacion; }

        public enum EstadoProceso {
            EN_PROGRESO("En progreso"),
            COMPLETADO("Completado"),
            ERROR("Error"),
            CANCELADO("Cancelado");

            private final String descripcion;

            EstadoProceso(String descripcion) {
                this.descripcion = descripcion;
            }

            public String getDescripcion() { return descripcion; }
        }
    }
}