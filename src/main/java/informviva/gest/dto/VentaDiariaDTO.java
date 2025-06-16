package informviva.gest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para ventas diarias
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaDiariaDTO {
    private LocalDate fecha;
    private Double total;
    private Integer cantidadVentas;
    private Double ticketPromedio;
    private String diaSemana;
    private Integer productosVendidos;
    private Integer clientesUnicos;

    /**
     * Obtiene el día de la semana en español
     */
    public String getDiaSemanaEspanol() {
        if (fecha == null) return "";
        return switch (fecha.getDayOfWeek()) {
            case MONDAY -> "Lunes";
            case TUESDAY -> "Martes";
            case WEDNESDAY -> "Miércoles";
            case THURSDAY -> "Jueves";
            case FRIDAY -> "Viernes";
            case SATURDAY -> "Sábado";
            case SUNDAY -> "Domingo";
        };
    }
}