package informviva.gest.dto;

import java.sql.Date;

public class VentaPorPeriodoDTO {

    private Date periodo;
    private Double total;

    // Constructor con parámetros
    public VentaPorPeriodoDTO(Date periodo, Double total) {
        this.periodo = periodo;
        this.total = total;
    }

    // Constructor vacío (necesario para frameworks como Hibernate)
    public VentaPorPeriodoDTO() {
    }

    // Getters y setters
    public Date getPeriodo() {
        return periodo;
    }

    public void setPeriodo(Date periodo) {
        this.periodo = periodo;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }
}