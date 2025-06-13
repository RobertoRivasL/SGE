package informviva.gest.model;

import informviva.gest.validador.ValidadorRut;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entidad que representa un cliente en el sistema
 */
@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @ValidadorRut(message = "El RUT debe tener un formato válido")  // Aplicando la validación del RUT
    private String rut;

    private LocalDate fechaRegistro;

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
            this.fechaRegistro = LocalDate.now(); // Establece la fecha actual si no está definida
        }
    }
}
