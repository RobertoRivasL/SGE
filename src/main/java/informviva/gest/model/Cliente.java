package informviva.gest.model;

import informviva.gest.validador.ValidadorRut;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa un cliente en el sistema
 * ACTUALIZADA: Ahora usa LocalDateTime para consistencia
 */
@Entity
@Table(name = "clientes")
@Data
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @NotBlank(message = "El apellido no puede estar vacío")
    private String apellido;

    @Email(message = "El correo debe tener un formato válido")
    @Column(unique = true)
    private String email;

    private String telefono;

    private String direccion;

    @ValidadorRut(message = "El RUT debe tener un formato válido")
    private String rut;

    // CAMBIADO: Ahora usa LocalDateTime para consistencia
    private LocalDateTime fechaRegistro;

    private String categoria;

    /**
     * Obtiene el nombre completo del cliente
     *
     * @return Nombre y apellido concatenados
     */
    @Transient
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    /**
     * Establece la fecha de registro antes de persistir el cliente
     */
    @PrePersist
    public void establecerFechaRegistro() {
        if (fechaRegistro == null) {
            this.fechaRegistro = LocalDateTime.now(); // Usa LocalDateTime
        }
    }

    /**
     * Método auxiliar para obtener la fecha de registro como LocalDate para compatibilidad
     * con código existente que espera LocalDate
     */
    @Transient
    public java.time.LocalDate getFechaRegistroAsLocalDate() {
        return fechaRegistro != null ? fechaRegistro.toLocalDate() : null;
    }

}