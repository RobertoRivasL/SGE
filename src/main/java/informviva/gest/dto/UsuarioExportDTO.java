package informviva.gest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para exportación de usuarios
 * Migrado para usar LocalDateTime
 *
 * @author Roberto Rivas
 * @version 3.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioExportDTO {
    private Long id;
    private String username;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private LocalDateTime ultimoAcceso;
    private String roles;

    // Campos adicionales para reportes
    private String departamento;
    private String cargo;
    private String sucursal;
    private Integer intentosFallidos;
    private Boolean bloqueado;
    private LocalDateTime ultimoCambioPassword;
    private String estado; // "ACTIVO", "INACTIVO", "BLOQUEADO", "PENDIENTE"
}