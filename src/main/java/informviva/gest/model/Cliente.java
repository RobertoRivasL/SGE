package informviva.gest.model;

import informviva.gest.validador.ValidadorRut;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "clientes")
@Data
public class Cliente {

    private String usuarioCreacion;
    private String usuarioModificacion;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 50, message = "El nombre no puede tener más de 50 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(max = 50, message = "El apellido no puede tener más de 50 caracteres")
    private String apellido;

    @Email(message = "El correo debe tener un formato válido")
    @NotBlank(message = "El correo no puede estar vacío")
    @Column(unique = true)
    private String email;

    @Size(max = 15, message = "El teléfono no puede tener más de 15 caracteres")
    private String telefono;

    @Size(max = 100, message = "La dirección no puede tener más de 100 caracteres")
    private String direccion;

    @ValidadorRut(message = "El RUT debe tener un formato válido")
    @NotBlank(message = "El RUT no puede estar vacío")
    private String rut;

    @NotNull(message = "La fecha de registro no puede ser nula")
    private LocalDateTime fechaRegistro;

    @Size(max = 30, message = "La categoría no puede tener más de 30 caracteres")
    private String categoria;

    private boolean activo;

    @Size(max = 50, message = "La ciudad no puede tener más de 50 caracteres")
    private String ciudad;

    private LocalDateTime fechaModificacion;

    private LocalDateTime fechaUltimaCompra;

    private LocalDate fechaNacimiento;

    @DecimalMin(value = "0.0", message = "El total de compras no puede ser negativo")
    private BigDecimal totalCompras;

    @DecimalMin(value = "0", message = "El número de compras no puede ser negativo")
    private int numeroCompras;

    public boolean isActivo() {
        return this.activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getCiudad() {
        return this.ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public LocalDateTime getFechaModificacion() {
        return this.fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public LocalDateTime getFechaUltimaCompra() {
        return this.fechaUltimaCompra;
    }

    public BigDecimal getTotalCompras() {
        return this.totalCompras;
    }

    public void setTotalCompras(BigDecimal totalCompras) {
        if (totalCompras.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El total de compras no puede ser negativo");
        }
        this.totalCompras = totalCompras;
    }

    public int getNumeroCompras() {
        return this.numeroCompras;
    }

    public void setNumeroCompras(int numeroCompras) {
        if (numeroCompras < 0) {
            throw new IllegalArgumentException("El número de compras no puede ser negativo");
        }
        this.numeroCompras = numeroCompras;
    }

    public String getNombreCompleto() {
        if (nombre == null && apellido == null) {
            return "";
        }
        if (nombre == null) {
            return apellido.trim();
        }
        if (apellido == null) {
            return nombre.trim();
        }
        return (nombre.trim() + " " + apellido.trim()).trim();
    }

    public void actualizarCiudad(String nuevaCiudad) {
        if (nuevaCiudad == null || nuevaCiudad.trim().isEmpty()) {
            throw new IllegalArgumentException("La ciudad no puede estar vacía");
        }
        this.ciudad = nuevaCiudad.trim();
        this.fechaModificacion = LocalDateTime.now();
    }

    public void registrarCompra(BigDecimal montoCompra) {
        if (montoCompra == null || montoCompra.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto de la compra debe ser positivo");
        }
        if (this.totalCompras == null) {
            this.totalCompras = BigDecimal.ZERO;
        }
        this.totalCompras = this.totalCompras.add(montoCompra);
        this.numeroCompras++;
        this.fechaUltimaCompra = LocalDateTime.now();
    }

    public void activarCliente() {
        this.activo = true;
        this.fechaModificacion = LocalDateTime.now();
    }

    public void desactivarCliente() {
        this.activo = false;
        this.fechaModificacion = LocalDateTime.now();
    }

    public LocalDate getFechaRegistroAsLocalDate() {
        return fechaRegistro != null ? fechaRegistro.toLocalDate() : null;
    }

    public void setUsuarioModificacion(String usuarioModificacion) {
        this.usuarioModificacion = usuarioModificacion;
    }

    public String getUsuarioCreacion() {
        return usuarioCreacion;
    }

    public String getUsuarioModificacion() {
        return usuarioModificacion;
    }

    @OneToMany(mappedBy = "cliente")
    private List<Venta> ventas;
}