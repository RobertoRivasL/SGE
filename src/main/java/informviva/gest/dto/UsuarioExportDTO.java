package informviva.gest.dto;


/**
 * DTO para exportación de usuarios
 *
 * @author Roberto Rivas
 * @version 2.0
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioExportDTO {
    private Long id;
    private String username;
    private String nombreCompleto;
    private String email;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimoAcceso;
    private String roles;
}
