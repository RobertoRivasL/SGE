package informviva.gest.model;

/**
 * @author Roberto Rivas
 * @version 2.0
 */


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "configuracion_sistema")
public class ConfiguracionSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombreEmpresa;

    @Size(max = 200, message = "La dirección no puede superar los 200 caracteres")
    private String direccionEmpresa;

    @Size(max = 50, message = "El teléfono no puede superar los 50 caracteres")
    private String telefonoEmpresa;

    @Email(message = "El correo debe tener un formato válido")
    @Size(max = 100, message = "El correo no puede superar los 100 caracteres")
    private String emailContacto;

    @Size(max = 200, message = "El logo URL no puede superar los 200 caracteres")
    private String logoUrl;

    @Size(max = 50, message = "El color primario no puede superar los 50 caracteres")
    private String colorPrimario = "#0d6efd"; // Color Bootstrap por defecto

    @Column(name = "smtp_host")
    private String smtpHost;

    @Column(name = "smtp_port")
    private Integer smtpPort;

    @Column(name = "smtp_usuario")
    private String smtpUsuario;

    @Column(name = "smtp_password")
    private String smtpPassword;

    @Column(name = "smtp_ssl_habilitado")
    private Boolean smtpSslHabilitado = true;

    @Column(name = "dias_inactividad_alerta")
    private Integer diasInactividadAlerta = 30;

    @Column(name = "habilitar_notificaciones")
    private Boolean habilitarNotificaciones = true;

    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;

    @Column(name = "usuario_actualizacion")
    private String usuarioActualizacion;

    @PrePersist
    @PreUpdate
    private void onUpdate() {
        this.ultimaActualizacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getDireccionEmpresa() {
        return direccionEmpresa;
    }

    public void setDireccionEmpresa(String direccionEmpresa) {
        this.direccionEmpresa = direccionEmpresa;
    }

    public String getTelefonoEmpresa() {
        return telefonoEmpresa;
    }

    public void setTelefonoEmpresa(String telefonoEmpresa) {
        this.telefonoEmpresa = telefonoEmpresa;
    }

    public String getEmailContacto() {
        return emailContacto;
    }

    public void setEmailContacto(String emailContacto) {
        this.emailContacto = emailContacto;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getColorPrimario() {
        return colorPrimario;
    }

    public void setColorPrimario(String colorPrimario) {
        this.colorPrimario = colorPrimario;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public Integer getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(Integer smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getSmtpUsuario() {
        return smtpUsuario;
    }

    public void setSmtpUsuario(String smtpUsuario) {
        this.smtpUsuario = smtpUsuario;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
    }

    public Boolean getSmtpSslHabilitado() {
        return smtpSslHabilitado;
    }

    public void setSmtpSslHabilitado(Boolean smtpSslHabilitado) {
        this.smtpSslHabilitado = smtpSslHabilitado;
    }

    public Integer getDiasInactividadAlerta() {
        return diasInactividadAlerta;
    }

    public void setDiasInactividadAlerta(Integer diasInactividadAlerta) {
        this.diasInactividadAlerta = diasInactividadAlerta;
    }

    public Boolean getHabilitarNotificaciones() {
        return habilitarNotificaciones;
    }

    // Añadir AQUÍ el método setter que falta
    public void setHabilitarNotificaciones(Boolean habilitarNotificaciones) {
        this.habilitarNotificaciones = habilitarNotificaciones;
    }

    public void setUsuarioActualizacion(String usuarioActualizacion) {
        this.usuarioActualizacion = usuarioActualizacion;
    }
} // Esta llave de cierre faltaba al final del archivo
