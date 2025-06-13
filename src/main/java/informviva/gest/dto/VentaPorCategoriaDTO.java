package informviva.gest.dto;

public class VentaPorCategoriaDTO {
    private String categoria;
    private double total;

    public VentaPorCategoriaDTO(String categoria, double total) {
        this.categoria = categoria;
        this.total = total;
    }

    // Getters y setters
    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}