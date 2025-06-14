package informviva.gest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * Configuración externa para el módulo de importación
 *
 * @author Roberto Rivas
 * @version 2.1
 */
@Configuration
@ConfigurationProperties(prefix = "app.importacion")
public class ImportacionConfig {

    private long tamahoMaximoArchivo = 10 * 1024 * 1024; // 10MB por defecto
    private int loteMaximo = 1000; // Registros por lote
    private int vistaPreviaMaxima = 10; // Filas en vista previa
    private List<String> formatosSoportados = List.of("csv", "xlsx", "xls");
    private boolean procesamientoAsincrono = true;
    private int timeoutProcesamiento = 300; // 5 minutos en segundos

    // Configuración específica por tipo de entidad
    private Map<String, EntidadConfig> entidades = Map.of(
            "cliente", new EntidadConfig(5000, List.of("nombre", "apellido", "email", "rut")),
            "producto", new EntidadConfig(10000, List.of("codigo", "nombre", "precio")),
            "usuario", new EntidadConfig(1000, List.of("username", "password", "nombre", "apellido", "email"))
    );

    // Getters y Setters
    public long getTamahoMaximoArchivo() {
        return tamahoMaximoArchivo;
    }

    public void setTamahoMaximoArchivo(long tamahoMaximoArchivo) {
        this.tamahoMaximoArchivo = tamahoMaximoArchivo;
    }

    public int getLoteMaximo() {
        return loteMaximo;
    }

    public void setLoteMaximo(int loteMaximo) {
        this.loteMaximo = loteMaximo;
    }

    public int getVistaPreviaMaxima() {
        return vistaPreviaMaxima;
    }

    public void setVistaPreviaMaxima(int vistaPreviaMaxima) {
        this.vistaPreviaMaxima = vistaPreviaMaxima;
    }

    public List<String> getFormatosSoportados() {
        return formatosSoportados;
    }

    public void setFormatosSoportados(List<String> formatosSoportados) {
        this.formatosSoportados = formatosSoportados;
    }

    public boolean isProcesamientoAsincrono() {
        return procesamientoAsincrono;
    }

    public void setProcesamientoAsincrono(boolean procesamientoAsincrono) {
        this.procesamientoAsincrono = procesamientoAsincrono;
    }

    public int getTimeoutProcesamiento() {
        return timeoutProcesamiento;
    }

    public void setTimeoutProcesamiento(int timeoutProcesamiento) {
        this.timeoutProcesamiento = timeoutProcesamiento;
    }

    public Map<String, EntidadConfig> getEntidades() {
        return entidades;
    }

    public void setEntidades(Map<String, EntidadConfig> entidades) {
        this.entidades = entidades;
    }

    public EntidadConfig getConfiguracionEntidad(String tipoEntidad) {
        return entidades.getOrDefault(tipoEntidad.toLowerCase(),
                new EntidadConfig(1000, List.of()));
    }

    /**
     * Configuración específica por tipo de entidad
     */
    public static class EntidadConfig {
        private int maxRegistros;
        private List<String> camposObligatorios;

        public EntidadConfig() {}

        public EntidadConfig(int maxRegistros, List<String> camposObligatorios) {
            this.maxRegistros = maxRegistros;
            this.camposObligatorios = camposObligatorios;
        }

        public int getMaxRegistros() {
            return maxRegistros;
        }

        public void setMaxRegistros(int maxRegistros) {
            this.maxRegistros = maxRegistros;
        }

        public List<String> getCamposObligatorios() {
            return camposObligatorios;
        }

        public void setCamposObligatorios(List<String> camposObligatorios) {
            this.camposObligatorios = camposObligatorios;
        }
    }
}