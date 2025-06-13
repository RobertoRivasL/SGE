package informviva.gest.model;

/**
 * @author Roberto Rivas
 * @version 2.0
 */


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    @Column(unique = true)
    private String nombre;

    @Column(length = 200)
    private String descripcion;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "rol_permisos", joinColumns = @JoinColumn(name = "rol_id"))
    @Column(name = "permiso")
    private Set<String> permisos = new HashSet<>();

    // Constructores
    public Rol() {
    }

    public Rol(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Set<String> getPermisos() {
        return permisos;
    }

    public void setPermisos(Set<String> permisos) {
        this.permisos = permisos;
    }

    public void agregarPermiso(String permiso) {
        this.permisos.add(permiso);
    }

    public void quitarPermiso(String permiso) {
        this.permisos.remove(permiso);
    }

    public boolean tienePermiso(String permiso) {
        return this.permisos.contains(permiso);
    }
}