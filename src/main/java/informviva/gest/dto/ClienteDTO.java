package informviva.gest.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Data Transfer Object para Cliente
 * Utilizado en APIs REST y transferencia de datos
 *
 * Siguiendo el patrón de DTOs del proyecto
 *
 * @author Roberto
 * @version 2.1
 * @since FASE 2 - Estandarización Arquitectónica
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClienteDTO {

    // ========== IDENTIFICACIÓN ==========

    /**
     * ID único del cliente
     */
    private Long id;

    /**
     * RUT del cliente (formato chileno con dígito verificador)
     */
    @NotBlank(message = "El RUT es obligatorio")
    @Pattern(regexp = "^[0-9]{7,8}-[0-9Kk]$",
            message = "RUT debe tener formato válido (ej: 12345678-9)")
    private String rut;

    // ========== DATOS PERSONALES ==========

    /**
     * Nombre del cliente
     */
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$",
            message = "El nombre solo puede contener letras y espacios")
    private String nombre;

    /**
     * Apellido del cliente
     */
    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$",
            message = "El apellido solo puede contener letras y espacios")
    private String apellido;

    /**
     * Email del cliente
     */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    /**
     * Teléfono del cliente (formato chileno)
     */
    @Pattern(regexp = "^(\\+56|56)?[2-9]\\d{8}$|^(\\+56|56)?9\\d{8}$",
            message = "Teléfono debe ser válido (formato chileno)")
    private String telefono;

    // ========== UBICACIÓN ==========

    /**
     * Dirección completa del cliente
     */
    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    private String direccion;

    /**
     * Ciudad del cliente
     */
    @Size(max = 50, message = "La ciudad no puede exceder 50 caracteres")
    private String ciudad;

    // ========== ESTADO Y CONTROL ==========

    /**
     * Estado activo/inactivo del cliente
     */
    @NotNull(message = "El estado activo es obligatorio")
    private boolean activo = true;

    // ========== FECHAS DE AUDITORÍA ==========

    /**
     * Fecha de registro del cliente
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaRegistro;

    /**
     * Fecha de última modificación
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaModificacion;

    /**
     * Fecha de última compra
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaUltimaCompra;

    // ========== ESTADÍSTICAS DE COMPRAS ==========

    /**
     * Total gastado por el cliente
     */
    @DecimalMin(value = "0.0", message = "El total de compras no puede ser negativo")
    private Double totalCompras;

    /**
     * Número total de compras realizadas
     */
    @Min(value = 0, message = "El número de compras no puede ser negativo")
    private Integer numeroCompras;

    /**
     * Promedio de gasto por compra
     */
    @DecimalMin(value = "0.0", message = "El promedio de compra no puede ser negativo")
    private Double promedioCompra;

    // ========== CAMPOS CALCULADOS ==========

    /**
     * Nombre completo (nombre + apellido)
     * Se calcula automáticamente, no se persiste
     */
    public String getNombreCompleto() {
        if (nombre != null && apellido != null) {
            return nombre + " " + apellido;
        }
        return nombre != null ? nombre : (apellido != null ? apellido : "");
    }

    /**
     * Categoría del cliente basada en su comportamiento de compra
     * Se calcula dinámicamente
     */
    public String getCategoriaCliente() {
        if (numeroCompras == null || numeroCompras == 0) {
            return "NUEVO";
        } else if (numeroCompras >= 10 && totalCompras != null && totalCompras >= 500000) {
            return "VIP";
        } else if (numeroCompras >= 5) {
            return "FRECUENTE";
        } else if (fechaUltimaCompra == null ||
                fechaUltimaCompra.isBefore(LocalDateTime.now().minusMonths(6))) {
            return "INACTIVO";
        } else {
            return "REGULAR";
        }
    }

    /**
     * Indica si es un cliente reciente (registrado en los últimos 30 días)
     */
    public boolean esClienteReciente() {
        return fechaRegistro != null &&
                fechaRegistro.isAfter(LocalDateTime.now().minusDays(30));
    }

    /**
     * Días desde la última compra
     */
    public Long diasSinComprar() {
        if (fechaUltimaCompra == null) {
            return null;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(
                fechaUltimaCompra.toLocalDate(),
                LocalDateTime.now().toLocalDate()
        );
    }

    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * Validar si el cliente está activo y es válido para ventas
     */
    public boolean puedeRealizarCompras() {
        return activo && rut != null && !rut.trim().isEmpty() &&
                email != null && !email.trim().isEmpty();
    }

    /**
     * Obtener iniciales del cliente
     */
    public String getIniciales() {
        StringBuilder iniciales = new StringBuilder();
        if (nombre != null && !nombre.isEmpty()) {
            iniciales.append(nombre.charAt(0));
        }
        if (apellido != null && !apellido.isEmpty()) {
            iniciales.append(apellido.charAt(0));
        }
        return iniciales.toString().toUpperCase();
    }

    /**
     * Obtener representación textual para logs
     */
    @Override
    public String toString() {
        return String.format("ClienteDTO{id=%s, rut='%s', nombre='%s', apellido='%s', email='%s', activo=%s}",
                id, rut, nombre, apellido, email, activo);
    }

    // ========== CONSTRUCTORES AUXILIARES ==========

    /**
     * Constructor para crear un cliente básico (usado en formularios)
     */
    public ClienteDTO(String nombre, String apellido, String email, String rut) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.rut = rut;
        this.activo = true;
        this.fechaRegistro = LocalDateTime.now();
    }

    /**
     * Constructor para búsquedas/autocompletado (solo datos esenciales)
     */
    public ClienteDTO(Long id, String nombre, String apellido, String email, String rut) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.rut = rut;
        this.activo = true;
    }

    /**
     * Constructor completo para reportes
     */
    public ClienteDTO(Long id, String rut, String nombre, String apellido, String email,
                      String telefono, String direccion, String ciudad, boolean activo,
                      LocalDateTime fechaRegistro, Double totalCompras, Integer numeroCompras) {
        this.id = id;
        this.rut = rut;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.activo = activo;
        this.fechaRegistro = fechaRegistro;
        this.totalCompras = totalCompras;
        this.numeroCompras = numeroCompras;

        // Calcular promedio si hay datos
        if (numeroCompras != null && numeroCompras > 0 && totalCompras != null) {
            this.promedioCompra = totalCompras / numeroCompras;
        }
    }
}