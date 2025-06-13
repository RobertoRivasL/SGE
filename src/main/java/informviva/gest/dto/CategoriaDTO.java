package informviva.gest.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para transferencia de datos de categoría
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Data
@NoArgsConstructor
public class CategoriaDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcion;

    private Boolean activa;

    // Campos adicionales para reportes y estadísticas
    private Long cantidadProductos;
    private Double ventasTotales;
    private Integer productosActivos;

    /**
     * Constructor simplificado para casos básicos
     */
    public CategoriaDTO(Long id, String nombre, String descripcion, Boolean activa) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activa = activa;
    }

    /**
     * Constructor para reportes con estadísticas
     */
    public CategoriaDTO(Long id, String nombre, String descripcion, Boolean activa,
                        Long cantidadProductos, Double ventasTotales, Integer productosActivos) {
        this(id, nombre, descripcion, activa);
        this.cantidadProductos = cantidadProductos;
        this.ventasTotales = ventasTotales;
        this.productosActivos = productosActivos;
    }

    /**
     * Método de utilidad para verificar si la categoría está activa
     */
    public boolean isActiva() {
        return activa != null && activa;
    }

    /**
     * Método de utilidad para obtener un resumen de la categoría
     */
    public String getResumen() {
        StringBuilder resumen = new StringBuilder(nombre);
        if (cantidadProductos != null && cantidadProductos > 0) {
            resumen.append(" (").append(cantidadProductos).append(" productos)");
        }
        return resumen.toString();
    }
}