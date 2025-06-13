package informviva.gest.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para exportación de clientes
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteExportDTO {
    private Long id;
    private String rut;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private String direccion;
    private LocalDateTime fechaRegistro;
    private String categoria;
    private Long totalCompras;
    private Double montoTotalCompras;
}