package informviva.gest.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) para la entidad Proveedor
 *
 * Aplicación de principios SOLID:
 * - Encapsulación: Datos del proveedor encapsulados en un objeto
 * - Separación de Responsabilidades: DTO para transferencia, Entidad para persistencia
 * - Abstracción: Representa el proveedor desde la perspectiva del cliente
 *
 * @author Roberto Rivas
 * @version 1.0
 * @since FASE 3 - Módulo de Compras
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorDTO {

    /**
     * Identificador único del proveedor
     */
    private Long id;

    /**
     * RUT del proveedor (Chile)
     * Obligatorio, formato: 12345678-9
     */
    @NotBlank(message = "El RUT es obligatorio")
    @Pattern(regexp = "^[0-9]+-[0-9Kk]$", message = "Formato de RUT inválido (ej: 12345678-9)")
    private String rut;

    /**
     * Razón social o nombre comercial del proveedor
     * Obligatorio, longitud entre 2 y 200 caracteres
     */
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 200, message = "El nombre debe tener entre 2 y 200 caracteres")
    private String nombre;

    /**
     * Nombre de fantasía o nombre comercial
     * Opcional, máximo 200 caracteres
     */
    @Size(max = 200, message = "El nombre de fantasía no puede exceder 200 caracteres")
    private String nombreFantasia;

    /**
     * Giro del proveedor
     * Opcional, máximo 200 caracteres
     */
    @Size(max = 200, message = "El giro no puede exceder 200 caracteres")
    private String giro;

    /**
     * Dirección del proveedor
     * Opcional, máximo 500 caracteres
     */
    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    private String direccion;

    /**
     * Ciudad
     * Opcional, máximo 100 caracteres
     */
    @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
    private String ciudad;

    /**
     * Región
     * Opcional, máximo 100 caracteres
     */
    @Size(max = 100, message = "La región no puede exceder 100 caracteres")
    private String region;

    /**
     * País
     * Opcional, máximo 100 caracteres
     */
    @Size(max = 100, message = "El país no puede exceder 100 caracteres")
    private String pais;

    /**
     * Código postal
     * Opcional, máximo 20 caracteres
     */
    @Size(max = 20, message = "El código postal no puede exceder 20 caracteres")
    private String codigoPostal;

    /**
     * Teléfono principal
     * Opcional, máximo 20 caracteres
     */
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    /**
     * Teléfono alternativo
     * Opcional, máximo 20 caracteres
     */
    @Size(max = 20, message = "El teléfono alternativo no puede exceder 20 caracteres")
    private String telefonoAlternativo;

    /**
     * Email principal
     * Opcional, debe ser válido si se proporciona
     */
    @Email(message = "Email inválido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    private String email;

    /**
     * Sitio web
     * Opcional, máximo 200 caracteres
     */
    @Size(max = 200, message = "El sitio web no puede exceder 200 caracteres")
    private String sitioWeb;

    /**
     * Nombre del contacto principal
     * Opcional, máximo 150 caracteres
     */
    @Size(max = 150, message = "El nombre del contacto no puede exceder 150 caracteres")
    private String contactoNombre;

    /**
     * Cargo del contacto principal
     * Opcional, máximo 100 caracteres
     */
    @Size(max = 100, message = "El cargo del contacto no puede exceder 100 caracteres")
    private String contactoCargo;

    /**
     * Teléfono del contacto principal
     * Opcional, máximo 20 caracteres
     */
    @Size(max = 20, message = "El teléfono del contacto no puede exceder 20 caracteres")
    private String contactoTelefono;

    /**
     * Email del contacto principal
     * Opcional, debe ser válido si se proporciona
     */
    @Email(message = "Email del contacto inválido")
    @Size(max = 150, message = "El email del contacto no puede exceder 150 caracteres")
    private String contactoEmail;

    /**
     * Condiciones de pago (ej: "30 días", "Al contado", etc.)
     * Opcional, máximo 100 caracteres
     */
    @Size(max = 100, message = "Las condiciones de pago no pueden exceder 100 caracteres")
    private String condicionesPago;

    /**
     * Días de crédito otorgados
     * Opcional, no puede ser negativo
     */
    @Min(value = 0, message = "Los días de crédito no pueden ser negativos")
    private Integer diasCredito;

    /**
     * Observaciones o notas sobre el proveedor
     * Opcional, máximo 1000 caracteres
     */
    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;

    /**
     * Indica si el proveedor está activo
     */
    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo = true;

    /**
     * Fecha de creación del registro
     * Solo lectura
     */
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de última actualización
     * Solo lectura
     */
    private LocalDateTime fechaActualizacion;

    /**
     * ID del usuario que creó el registro
     * Solo para referencia, no se persiste directamente
     */
    private Long usuarioCreacionId;

    /**
     * Nombre del usuario que creó el registro
     * Solo para visualización
     */
    private String usuarioCreacionNombre;

    /**
     * ID del usuario que actualizó el registro
     * Solo para referencia
     */
    private Long usuarioActualizacionId;

    /**
     * Nombre del usuario que actualizó el registro
     * Solo para visualización
     */
    private String usuarioActualizacionNombre;

    /**
     * Calificación del proveedor (1-5 estrellas)
     * Opcional, entre 1 y 5
     */
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    private Integer calificacion;

    /**
     * Categoría o tipo de proveedor
     * Opcional, máximo 100 caracteres
     */
    @Size(max = 100, message = "La categoría no puede exceder 100 caracteres")
    private String categoria;

    /**
     * Constructor para crear DTO con datos básicos
     *
     * @param rut RUT del proveedor
     * @param nombre Nombre del proveedor
     */
    public ProveedorDTO(String rut, String nombre) {
        this.rut = rut;
        this.nombre = nombre;
        this.activo = true;
    }

    /**
     * Obtiene el nombre completo (nombre o nombre fantasía si existe)
     *
     * @return Nombre completo del proveedor
     */
    public String getNombreCompleto() {
        if (nombreFantasia != null && !nombreFantasia.isEmpty()) {
            return nombreFantasia + " (" + nombre + ")";
        }
        return nombre;
    }

    /**
     * Verifica si el proveedor tiene contacto completo
     *
     * @return true si tiene datos de contacto
     */
    public boolean tieneContactoCompleto() {
        return contactoNombre != null && !contactoNombre.isEmpty()
                && (contactoTelefono != null || contactoEmail != null);
    }

    /**
     * Verifica si el proveedor ofrece crédito
     *
     * @return true si tiene días de crédito configurados
     */
    public boolean ofreceCredito() {
        return diasCredito != null && diasCredito > 0;
    }

    /**
     * Método para validar si el DTO tiene los datos mínimos requeridos
     *
     * @return true si tiene los datos básicos válidos
     */
    public boolean esValido() {
        return rut != null && !rut.trim().isEmpty()
                && nombre != null && !nombre.trim().isEmpty();
    }

    /**
     * Genera una representación compacta del proveedor
     *
     * @return String con formato "Nombre (RUT)"
     */
    public String toStringCompacto() {
        return String.format("%s (%s)",
                nombre != null ? nombre : "Sin nombre",
                rut != null ? rut : "Sin RUT");
    }

    /**
     * Obtiene la información de contacto principal
     *
     * @return String con el contacto principal
     */
    public String getContactoPrincipal() {
        if (contactoNombre != null && !contactoNombre.isEmpty()) {
            StringBuilder contacto = new StringBuilder(contactoNombre);
            if (contactoCargo != null && !contactoCargo.isEmpty()) {
                contacto.append(" - ").append(contactoCargo);
            }
            return contacto.toString();
        }
        return "Sin contacto";
    }
}
