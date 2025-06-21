package informviva.gest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad que representa un usuario del sistema.
 * ACTUALIZADA: Ahora usa LocalDateTime consistentemente en lugar de LocalDate
 */
@Entity
@Table(name = "usuarios")
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    @Column(unique = true)
    private String username;

    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @NotBlank(message = "El apellido no puede estar vacío")
    private String apellido;

    @Email(message = "El correo debe tener un formato válido")
    @Column(unique = true)
    private String email;

    private boolean activo = true;

    // CAMBIADO: Ahora usa LocalDateTime para consistencia
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimoAcceso;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "rol")
    private Set<String> roles = new HashSet<>();

    // Constructor simplificado para inicializar username y password.
    public Usuario(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Método de ciclo de vida para asignar fechaCreacion y ultimoAcceso al momento de persistir la entidad.
     */
    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.fechaCreacion = now;
        this.ultimoAcceso = now;
    }

    /**
     * Método de ciclo de vida para actualizar el ultimoAcceso cada vez que la entidad se actualice.
     */
    @PreUpdate
    private void onUpdate() {
        this.ultimoAcceso = LocalDateTime.now();
    }

    /**
     * Obtiene el nombre completo del usuario.
     *
     * @return Nombre y apellido concatenados.
     */
//    @Transient
//    public String getNombreCompleto() {
//        return nombre + " " + apellido;
//    }

    /**
     * Verifica si el usuario tiene un rol específico.
     *
     * @param rol El rol a verificar.
     * @return true si el usuario tiene el rol, false en caso contrario.
     */
    public boolean tieneRol(String rol) {
        return roles.contains(rol);
    }

    // Se sobrescriben equals y hashCode utilizando 'id' y 'username' para evitar problemas en las colecciones.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id) &&
                Objects.equals(username, usuario.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }


    public String getNombreCompleto() {
        // Si tiene nombre y apellido, los concatena
        if (nombre != null && !nombre.trim().isEmpty() &&
                apellido != null && !apellido.trim().isEmpty()) {
            return (nombre.trim() + " " + apellido.trim()).trim();
        }

        // Si solo tiene nombre
        if (nombre != null && !nombre.trim().isEmpty()) {
            return nombre.trim();
        }

        // Si solo tiene apellido
        if (apellido != null && !apellido.trim().isEmpty()) {
            return apellido.trim();
        }

        // Fallback al username
        return username != null ? username : "Usuario sin nombre";
    }

}