package informviva.gest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO para configuración de exportación
 * Migrado para usar LocalDateTime como tipo principal
 *
 * @author Roberto Rivas
 * @version 3.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportConfigDTO {

    // ==================== CAMPOS PRINCIPALES ====================

    /**
     * Tipo de exportación: usuarios, clientes, productos, ventas, reportes, inventario, financiero
     */
    @NotBlank(message = "El tipo de exportación es obligatorio")
    @Size(max = 50, message = "El tipo no puede exceder 50 caracteres")
    private String tipo;

    /**
     * Formato de exportación: PDF, EXCEL, CSV, JSON
     */
    @NotBlank(message = "El formato de exportación es obligatorio")
    @Size(max = 20, message = "El formato no puede exceder 20 caracteres")
    private String formato;

    /**
     * Fecha y hora de inicio del rango de datos (TIPO PRINCIPAL)
     */
    private LocalDateTime fechaInicio;

    /**
     * Fecha y hora de fin del rango de datos (TIPO PRINCIPAL)
     */
    private LocalDateTime fechaFin;

    /**
     * Filtros específicos para la exportación
     */
    private Map<String, Object> filtros = new HashMap<>();

    /**
     * Usuario que solicita la exportación
     */
    @Size(max = 100, message = "El usuario no puede exceder 100 caracteres")
    private String usuarioSolicitante;

    /**
     * Fecha y hora de solicitud de la exportación
     */
    private LocalDateTime fechaSolicitud;

    // ==================== CAMPOS ADICIONALES PARA CONFIGURACIÓN AVANZADA ====================

    /**
     * Columnas específicas a incluir en la exportación
     */
    private List<String> columnasIncluir;

    /**
     * Si incluir totales y resúmenes en la exportación
     */
    private Boolean incluirTotales = false;

    /**
     * Si incluir gráficos en la exportación (solo PDF)
     */
    private Boolean incluirGraficos = false;

    /**
     * Campo por el cual ordenar los datos
     */
    private String ordenarPor;

    /**
     * Dirección del ordenamiento: ASC, DESC
     */
    private String direccionOrden = "ASC";

    /**
     * Límite máximo de registros a exportar
     */
    private Integer limiteRegistros;

    /**
     * Título personalizado para la exportación
     */
    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    private String tituloPersonalizado;

    /**
     * Descripción adicional para incluir en el reporte
     */
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    /**
     * Si incluir encabezados y pies de página
     */
    private Boolean incluirEncabezados = true;

    /**
     * Configuración de idioma para la exportación
     */
    private String idioma = "es";

    /**
     * Zona horaria para formateo de fechas
     */
    private String zonaHoraria = "America/Santiago";

    // ==================== CONSTRUCTORES DE CONVENIENCIA ====================

    /**
     * Constructor para exportaciones básicas con LocalDateTime
     */
    public ExportConfigDTO(String tipo, String formato, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        this.tipo = tipo;
        this.formato = formato;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.fechaSolicitud = LocalDateTime.now();
    }

    /**
     * Constructor para exportaciones básicas con LocalDate (se convierte automáticamente)
     */
    public ExportConfigDTO(String tipo, String formato, LocalDate fechaInicio, LocalDate fechaFin) {
        this.tipo = tipo;
        this.formato = formato;
        this.fechaInicio = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
        this.fechaFin = fechaFin != null ? fechaFin.atTime(23, 59, 59) : null;
        this.fechaSolicitud = LocalDateTime.now();
    }

    /**
     * Constructor para exportaciones sin rango de fechas
     */
    public ExportConfigDTO(String tipo, String formato) {
        this.tipo = tipo;
        this.formato = formato;
        this.fechaSolicitud = LocalDateTime.now();
    }

    // ==================== MÉTODOS DE GESTIÓN DE FILTROS ====================

    /**
     * Agrega un filtro a la configuración
     *
     * @param clave Clave del filtro
     * @param valor Valor del filtro (no se agrega si es null)
     */
    public void addFiltro(String clave, Object valor) {
        if (valor != null) {
            if (filtros == null) {
                filtros = new HashMap<>();
            }
            filtros.put(clave, valor);
        }
    }

    /**
     * Obtiene un filtro específico con tipo seguro
     *
     * @param clave Clave del filtro
     * @param tipo Clase del tipo esperado
     * @return Valor del filtro casteado al tipo, o null si no existe o no es compatible
     */
    @SuppressWarnings("unchecked")
    public <T> T getFiltro(String clave, Class<T> tipo) {
        if (filtros == null) return null;

        Object valor = filtros.get(clave);
        if (valor != null && tipo.isAssignableFrom(valor.getClass())) {
            return (T) valor;
        }
        return null;
    }

    /**
     * Obtiene un filtro como String
     */
    public String getFiltroString(String clave) {
        return getFiltro(clave, String.class);
    }

    /**
     * Obtiene un filtro como Boolean
     */
    public Boolean getFiltroBoolean(String clave) {
        return getFiltro(clave, Boolean.class);
    }

    /**
     * Obtiene un filtro como Integer
     */
    public Integer getFiltroInteger(String clave) {
        return getFiltro(clave, Integer.class);
    }

    /**
     * Verifica si un filtro existe y es verdadero
     *
     * @param clave Clave del filtro
     * @return true si el filtro existe y es verdadero
     */
    public boolean isFiltroActivo(String clave) {
        Object valor = filtros != null ? filtros.get(clave) : null;
        if (valor instanceof Boolean) {
            return (Boolean) valor;
        }
        return false;
    }

    /**
     * Verifica si existe un filtro específico
     */
    public boolean tieneFiltro(String clave) {
        return filtros != null && filtros.containsKey(clave);
    }

    /**
     * Remueve un filtro específico
     */
    public void removerFiltro(String clave) {
        if (filtros != null) {
            filtros.remove(clave);
        }
    }

    /**
     * Limpia todos los filtros
     */
    public void limpiarFiltros() {
        if (filtros != null) {
            filtros.clear();
        }
    }

    // ==================== MÉTODOS DE COMPATIBILIDAD CON LocalDate ====================

    /**
     * Obtiene fecha de inicio como LocalDate (para compatibilidad)
     */
    @JsonIgnore
    public LocalDate getFechaInicioDate() {
        return fechaInicio != null ? fechaInicio.toLocalDate() : null;
    }

    /**
     * Establece fecha de inicio desde LocalDate
     */
    @JsonIgnore
    public void setFechaInicioDate(LocalDate fecha) {
        this.fechaInicio = fecha != null ? fecha.atStartOfDay() : null;
    }

    /**
     * Obtiene fecha de fin como LocalDate (para compatibilidad)
     */
    @JsonIgnore
    public LocalDate getFechaFinDate() {
        return fechaFin != null ? fechaFin.toLocalDate() : null;
    }

    /**
     * Establece fecha de fin desde LocalDate
     */
    @JsonIgnore
    public void setFechaFinDate(LocalDate fecha) {
        this.fechaFin = fecha != null ? fecha.atTime(23, 59, 59) : null;
    }

    // ==================== MÉTODOS DE VALIDACIÓN ====================

    /**
     * Valida que la configuración sea correcta
     *
     * @return true si la configuración es válida
     */
    @JsonIgnore
    public boolean isConfiguracionValida() {
        if (tipo == null || tipo.trim().isEmpty()) return false;
        if (formato == null || formato.trim().isEmpty()) return false;

        // Validar rango de fechas si se proporcionan
        if (fechaInicio != null && fechaFin != null) {
            return !fechaInicio.isAfter(fechaFin);
        }

        return true;
    }

    /**
     * Valida que el rango de fechas sea correcto
     */
    @JsonIgnore
    public boolean isRangoFechasValido() {
        if (fechaInicio == null || fechaFin == null) return true; // Sin rango es válido
        return !fechaInicio.isAfter(fechaFin);
    }

    /**
     * Calcula la duración en días del rango de fechas
     */
    @JsonIgnore
    public long getDuracionEnDias() {
        if (fechaInicio == null || fechaFin == null) return 0;
        return java.time.Duration.between(fechaInicio, fechaFin).toDays();
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Obtiene el nombre de archivo sugerido para la exportación
     */
    @JsonIgnore
    public String getNombreArchivoSugerido() {
        StringBuilder nombre = new StringBuilder();

        if (tituloPersonalizado != null && !tituloPersonalizado.trim().isEmpty()) {
            nombre.append(tituloPersonalizado.replaceAll("[^a-zA-Z0-9_-]", "_"));
        } else {
            nombre.append(tipo);
        }

        if (fechaInicio != null) {
            nombre.append("_").append(fechaInicio.toLocalDate().toString());
        }

        if (fechaFin != null && !fechaFin.toLocalDate().equals(fechaInicio.toLocalDate())) {
            nombre.append("_a_").append(fechaFin.toLocalDate().toString());
        }

        return nombre.toString().toLowerCase();
    }

    /**
     * Obtiene la extensión de archivo basada en el formato
     */
    @JsonIgnore
    public String getExtensionArchivo() {
        return switch (formato != null ? formato.toUpperCase() : "") {
            case "PDF" -> ".pdf";
            case "EXCEL" -> ".xlsx";
            case "CSV" -> ".csv";
            case "JSON" -> ".json";
            default -> ".dat";
        };
    }

    /**
     * Obtiene el tipo MIME basado en el formato
     */
    @JsonIgnore
    public String getTipoMime() {
        return switch (formato != null ? formato.toUpperCase() : "") {
            case "PDF" -> "application/pdf";
            case "EXCEL" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "CSV" -> "text/csv";
            case "JSON" -> "application/json";
            default -> "application/octet-stream";
        };
    }

    /**
     * Crea una copia de la configuración
     */
    public ExportConfigDTO clonar() {
        ExportConfigDTO copia = new ExportConfigDTO();
        copia.tipo = this.tipo;
        copia.formato = this.formato;
        copia.fechaInicio = this.fechaInicio;
        copia.fechaFin = this.fechaFin;
        copia.filtros = this.filtros != null ? new HashMap<>(this.filtros) : new HashMap<>();
        copia.usuarioSolicitante = this.usuarioSolicitante;
        copia.fechaSolicitud = this.fechaSolicitud;
        copia.columnasIncluir = this.columnasIncluir;
        copia.incluirTotales = this.incluirTotales;
        copia.incluirGraficos = this.incluirGraficos;
        copia.ordenarPor = this.ordenarPor;
        copia.direccionOrden = this.direccionOrden;
        copia.limiteRegistros = this.limiteRegistros;
        copia.tituloPersonalizado = this.tituloPersonalizado;
        copia.descripcion = this.descripcion;
        copia.incluirEncabezados = this.incluirEncabezados;
        copia.idioma = this.idioma;
        copia.zonaHoraria = this.zonaHoraria;

        return copia;
    }

    // ==================== MÉTODOS PARA CONFIGURACIONES ESPECÍFICAS ====================

    /**
     * Configura para exportación de usuarios
     */
    @JsonIgnore
    public ExportConfigDTO paraUsuarios(String formato) {
        this.tipo = "usuarios";
        this.formato = formato;
        return this;
    }

    /**
     * Configura para exportación de clientes
     */
    @JsonIgnore
    public ExportConfigDTO paraClientes(String formato, LocalDateTime inicio, LocalDateTime fin) {
        this.tipo = "clientes";
        this.formato = formato;
        this.fechaInicio = inicio;
        this.fechaFin = fin;
        return this;
    }

    /**
     * Configura para exportación de productos
     */
    @JsonIgnore
    public ExportConfigDTO paraProductos(String formato) {
        this.tipo = "productos";
        this.formato = formato;
        return this;
    }

    /**
     * Configura para exportación de ventas
     */
    @JsonIgnore
    public ExportConfigDTO paraVentas(String formato, LocalDateTime inicio, LocalDateTime fin) {
        this.tipo = "ventas";
        this.formato = formato;
        this.fechaInicio = inicio;
        this.fechaFin = fin;
        return this;
    }

    /**
     * Configura filtros comunes para solo registros activos
     */
    @JsonIgnore
    public ExportConfigDTO soloActivos() {
        addFiltro("soloActivos", true);
        return this;
    }

    /**
     * Configura para incluir todos los registros (activos e inactivos)
     */
    @JsonIgnore
    public ExportConfigDTO todosLosRegistros() {
        addFiltro("soloActivos", false);
        return this;
    }

    /**
     * Configura límite de registros
     */
    @JsonIgnore
    public ExportConfigDTO conLimite(int limite) {
        this.limiteRegistros = limite;
        return this;
    }

    // ==================== toString PERSONALIZADO ====================

    @Override
    public String toString() {
        return String.format("ExportConfigDTO{tipo='%s', formato='%s', fechaInicio=%s, fechaFin=%s, filtros=%d, usuario='%s'}",
                tipo, formato, fechaInicio, fechaFin,
                filtros != null ? filtros.size() : 0, usuarioSolicitante);
    }
}