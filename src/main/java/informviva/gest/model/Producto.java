package informviva.gest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    private Integer stockMinimo;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código no puede estar vacío")
    @Column(unique = true)
    private String codigo;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    private String descripcion;

    private boolean activo;

    @NotNull(message = "El precio no puede ser nulo")
    @Positive(message = "El precio debe ser mayor que cero")
    private Double precio;

    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer stock;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    private String marca;

    private String modelo;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;

    public boolean isActivo() {
        return this.activo;
    }

    public Producto(Long id, String nombre, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.activo = activo;
    }

    @Transient
    public String getNombreFormateado() {
        return "[" + codigo + "] - " + nombre;
    }

    @Transient
    public boolean isDisponible() {
        return isActivo() && stock != null && stock > 0;
    }

    public void actualizarStock(int cantidad) {
        if (stock == null) {
            stock = 0;
        }

        int nuevoStock = stock + cantidad;
        if (nuevoStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo. Stock actual: "
                    + stock + ", cantidad a descontar: " + Math.abs(cantidad));
        }

        stock = nuevoStock;
    }

    public Boolean getActivo() {
        return this.activo;
    }
}
