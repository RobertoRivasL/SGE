package informviva.gest.dto;

public class VentaPorVendedorDTO {
    private String vendedor;
    private double total;

    public VentaPorVendedorDTO(String vendedor, double total) {
        this.vendedor = vendedor;
        this.total = total;
    }

    // Getters y setters
    public String getVendedor() {
        return vendedor;
    }

    public void setVendedor(String vendedor) {
        this.vendedor = vendedor;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}