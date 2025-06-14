package informviva.gest.dto;

import informviva.gest.service.ResultadoValidacionModulo;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO que encapsula el resultado de validación completa del sistema
 *
 * @author Roberto Rivas
 * @version 2.0
 */
public class ResultadoValidacionSistema {

    private LocalDateTime fechaValidacion;
    private boolean sistemaConsistente;

    // Resultados por módulo
    private ResultadoValidacionModulo validacionClientes;
    private ResultadoValidacionModulo validacionProductos;
    private ResultadoValidacionModulo validacionVentas;
    private ResultadoValidacionModulo validacionUsuarios;
    private ResultadoValidacionModulo validacionIntegridad;

    // Resumen general
    private int totalErroresCriticos;
    private int totalAdvertencias;
    private int totalRegistrosConErrores;

    // Error general si ocurre algún problema
    private String errorGeneral;

    // Metadatos adicionales
    private long tiempoValidacionMs;
    private String version = "2.0";
    private List<String> modulosValidados;

    public ResultadoValidacionSistema() {
        this.fechaValidacion = LocalDateTime.now();
        this.modulosValidados = new ArrayList<>();
        this.sistemaConsistente = true;
    }

    // Métodos de utilidad
    public void agregarModuloValidado(String nombreModulo) {
        if (modulosValidados == null) {
            modulosValidados = new ArrayList<>();
        }
        modulosValidados.add(nombreModulo);
    }

    public boolean tieneErroresCriticos() {
        return totalErroresCriticos > 0;
    }

    public boolean tieneAdvertencias() {
        return totalAdvertencias > 0;
    }

    public int getTotalProblemas() {
        return totalErroresCriticos + totalAdvertencias;
    }

    public String getResumenGeneral() {
        StringBuilder resumen = new StringBuilder();
        resumen.append("Sistema ").append(sistemaConsistente ? "CONSISTENTE" : "INCONSISTENTE");

        if (tieneErroresCriticos()) {
            resumen.append(" | Errores críticos: ").append(totalErroresCriticos);
        }

        if (tieneAdvertencias()) {
            resumen.append(" | Advertencias: ").append(totalAdvertencias);
        }

        if (totalRegistrosConErrores > 0) {
            resumen.append(" | Registros con problemas: ").append(totalRegistrosConErrores);
        }

        return resumen.toString();
    }

    public List<ResultadoValidacionModulo> getTodosLosModulos() {
        List<ResultadoValidacionModulo> modulos = new ArrayList<>();

        if (validacionClientes != null) modulos.add(validacionClientes);
        if (validacionProductos != null) modulos.add(validacionProductos);
        if (validacionVentas != null) modulos.add(validacionVentas);
        if (validacionUsuarios != null) modulos.add(validacionUsuarios);
        if (validacionIntegridad != null) modulos.add(validacionIntegridad);

        return modulos;
    }

    // Getters y Setters
    public LocalDateTime getFechaValidacion() {
        return fechaValidacion;
    }

    public void setFechaValidacion(LocalDateTime fechaValidacion) {
        this.fechaValidacion = fechaValidacion;
    }

    public boolean isSistemaConsistente() {
        return sistemaConsistente;
    }

    public void setSistemaConsistente(boolean sistemaConsistente) {
        this.sistemaConsistente = sistemaConsistente;
    }

    public ResultadoValidacionModulo getValidacionClientes() {
        return validacionClientes;
    }

    public void setValidacionClientes(ResultadoValidacionModulo validacionClientes) {
        this.validacionClientes = validacionClientes;
        if (validacionClientes != null) {
            agregarModuloValidado("Clientes");
        }
    }

    public ResultadoValidacionModulo getValidacionProductos() {
        return validacionProductos;
    }

    public void setValidacionProductos(ResultadoValidacionModulo validacionProductos) {
        this.validacionProductos = validacionProductos;
        if (validacionProductos != null) {
            agregarModuloValidado("Productos");
        }
    }

    public ResultadoValidacionModulo getValidacionVentas() {
        return validacionVentas;
    }

    public void setValidacionVentas(ResultadoValidacionModulo validacionVentas) {
        this.validacionVentas = validacionVentas;
        if (validacionVentas != null) {
            agregarModuloValidado("Ventas");
        }
    }

    public ResultadoValidacionModulo getValidacionUsuarios() {
        return validacionUsuarios;
    }

    public void setValidacionUsuarios(ResultadoValidacionModulo validacionUsuarios) {
        this.validacionUsuarios = validacionUsuarios;
        if (validacionUsuarios != null) {
            agregarModuloValidado("Usuarios");
        }
    }

    public ResultadoValidacionModulo getValidacionIntegridad() {
        return validacionIntegridad;
    }

    public void setValidacionIntegridad(ResultadoValidacionModulo validacionIntegridad) {
        this.validacionIntegridad = validacionIntegridad;
        if (validacionIntegridad != null) {
            agregarModuloValidado("Integridad");
        }
    }

    public int getTotalErroresCriticos() {
        return totalErroresCriticos;
    }

    public void setTotalErroresCriticos(int totalErroresCriticos) {
        this.totalErroresCriticos = totalErroresCriticos;
    }

    public int getTotalAdvertencias() {
        return totalAdvertencias;
    }

    public void setTotalAdvertencias(int totalAdvertencias) {
        this.totalAdvertencias = totalAdvertencias;
    }

    public int getTotalRegistrosConErrores() {
        return totalRegistrosConErrores;
    }

    public void setTotalRegistrosConErrores(int totalRegistrosConErrores) {
        this.totalRegistrosConErrores = totalRegistrosConErrores;
    }

    public String getErrorGeneral() {
        return errorGeneral;
    }

    public void setErrorGeneral(String errorGeneral) {
        this.errorGeneral = errorGeneral;
        if (errorGeneral != null && !errorGeneral.trim().isEmpty()) {
            this.sistemaConsistente = false;
        }
    }

    public long getTiempoValidacionMs() {
        return tiempoValidacionMs;
    }

    public void setTiempoValidacionMs(long tiempoValidacionMs) {
        this.tiempoValidacionMs = tiempoValidacionMs;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getModulosValidados() {
        return modulosValidados;
    }

    public void setModulosValidados(List<String> modulosValidados) {
        this.modulosValidados = modulosValidados;
    }
}