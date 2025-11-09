package informviva.gest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa un proveedor en el sistema
 * Gestiona información de proveedores para el módulo de compras
 *
 * @author Roberto Rivas
 * @version 1.0
 * @since FASE 3 - Módulo de Compras
 */
@Entity
@Table(name = "proveedores", indexes = {
    @Index(name = "idx_rut_proveedor", columnList = "rut"),
    @Index(name = "idx_nombre_proveedor", columnList = "nombre"),
    @Index(name = "idx_activo_proveedor", columnList = "activo")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * RUT del proveedor (Chile)
     */
    @NotBlank(message = "El RUT es obligatorio")
    @Pattern(regexp = "^[0-9]+-[0-9Kk]$", message = "Formato de RUT inválido (ej: 12345678-9)")
    @Column(unique = true, length = 12)
    private String rut;

    /**
     * Razón social o nombre comercial del proveedor
     */
    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false, length = 200)
    private String nombre;

    /**
     * Nombre de fantasía o nombre comercial
     */
    @Column(length = 200)
    private String nombreFantasia;

    /**
     * Giro del proveedor
     */
    @Column(length = 200)
    private String giro;

    /**
     * Dirección del proveedor
     */
    @Column(length = 500)
    private String direccion;

    /**
     * Ciudad
     */
    @Column(length = 100)
    private String ciudad;

    /**
     * Región
     */
    @Column(length = 100)
    private String region;

    /**
     * País
     */
    @Column(length = 100)
    private String pais;

    /**
     * Código postal
     */
    @Column(length = 20)
    private String codigoPostal;

    /**
     * Teléfono principal
     */
    @Column(length = 20)
    private String telefono;

    /**
     * Teléfono alternativo
     */
    @Column(length = 20)
    private String telefonoAlternativo;

    /**
     * Email principal
     */
    @Email(message = "Email inválido")
    @Column(length = 150)
    private String email;

    /**
     * Sitio web
     */
    @Column(length = 200)
    private String sitioWeb;

    /**
     * Nombre del contacto principal
     */
    @Column(length = 150)
    private String contactoNombre;

    /**
     * Cargo del contacto principal
     */
    @Column(length = 100)
    private String contactoCargo;

    /**
     * Teléfono del contacto principal
     */
    @Column(length = 20)
    private String contactoTelefono;

    /**
     * Email del contacto principal
     */
    @Email(message = "Email del contacto inválido")
    @Column(length = 150)
    private String contactoEmail;

    /**
     * Condiciones de pago (ej: "30 días", "Al contado", etc.)
     */
    @Column(length = 100)
    private String condicionesPago;

    /**
     * Días de crédito otorgados
     */
    private Integer diasCredito;

    /**
     * Observaciones o notas sobre el proveedor
     */
    @Column(length = 1000)
    private String observaciones;

    /**
     * Indica si el proveedor está activo
     */
    @Column(nullable = false)
    private boolean activo = true;

    /**
     * Fecha de creación del registro
     */
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de última actualización
     */
    private LocalDateTime fechaActualizacion;

    /**
     * Usuario que creó el registro
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_creacion_id")
    private Usuario usuarioCreacion;

    /**
     * Usuario que actualizó el registro
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_actualizacion_id")
    private Usuario usuarioActualizacion;

    /**
     * Calificación del proveedor (1-5 estrellas)
     */
    private Integer calificacion;

    /**
     * Categoría o tipo de proveedor
     */
    @Column(length = 100)
    private String categoria;

    /**
     * Establece fecha de creación si no está definida
     */
    @PrePersist
    public void establecerFechaCreacion() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }

    /**
     * Actualiza fecha de modificación
     */
    @PreUpdate
    public void actualizarFechaModificacion() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Obtiene el nombre completo (nombre o nombre fantasía si existe)
     */
    @Transient
    public String getNombreCompleto() {
        return nombreFantasia != null && !nombreFantasia.isEmpty()
                ? nombreFantasia + " (" + nombre + ")"
                : nombre;
    }

    /**
     * Verifica si el proveedor tiene contacto completo
     */
    @Transient
    public boolean tieneContactoCompleto() {
        return contactoNombre != null && !contactoNombre.isEmpty()
                && (contactoTelefono != null || contactoEmail != null);
    }

    /**
     * Verifica si el proveedor ofrece crédito
     */
    @Transient
    public boolean ofreceCredito() {
        return diasCredito != null && diasCredito > 0;
    }
}
