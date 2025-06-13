package informviva.gest.model;

/**
 * @author Roberto Rivas
 * @version 2.0
 */


import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "rol_vistas")
public class RolVista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rol_nombre")
    private String rolNombre;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "username")
    private String username;

    @Column(name = "fecha_vista")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaVista;

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRolNombre() {
        return rolNombre;
    }

    public void setRolNombre(String rolNombre) {
        this.rolNombre = rolNombre;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getFechaVista() {
        return fechaVista;
    }

    public void setFechaVista(Date fechaVista) {
        this.fechaVista = fechaVista;
    }
}
