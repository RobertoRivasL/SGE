package informviva.gest.dto;

/**
 * DTO para exportación de análisis de clientes
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
class AnalisisClientesDTO {
    private Long clienteId;
    private String rut;
    private String nombreCompleto;
    private String email;
    private LocalDateTime fechaRegistro;
    private LocalDateTime primeraCompra;
    private LocalDateTime ultimaCompra;
    private Long totalCompras;
    private Double montoTotalGastado;
    private Double promedioCompra;
    private Integer diasSinComprar;
    private String categoriaCliente; // "NUEVO", "FRECUENTE", "VIP", "INACTIVO"
    private Double frecuenciaCompra; // compras por mes
}

