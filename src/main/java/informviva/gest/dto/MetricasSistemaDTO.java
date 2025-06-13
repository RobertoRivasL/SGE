package informviva.gest.dto;

// =================== DTO PARA MÉTRICAS ===================

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MetricasSistemaDTO {
    private Long memoriaUsada;
    private Long memoriaTotal;
    private Double porcentajeMemoria;
    private Long tiempoActividad;
    private Long totalProductos;
    private Long totalClientes;
    private Long productosStockBajo;
    private Double tiempoRespuestaPromedio;
    private LocalDateTime fechaActualizacion;
    private String error;
}
