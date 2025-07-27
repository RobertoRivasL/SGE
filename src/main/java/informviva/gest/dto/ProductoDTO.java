package informviva.gest.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) para la entidad Producto
 *
 * Aplicación de principios:
 * - Encapsulación: Datos del producto encapsulados en un objeto
 * - Separación de Responsabilidades: DTO para transferencia, Entidad para persistencia
 * - Abstracción: Representa el producto desde la perspectiva del cliente
 *
 * @author Tu nombre
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {

    /**
     * Identificador único del producto
     */
    private Long id;

    /**
     * Nombre del producto
     * Obligatorio, longitud entre 2 y 100 caracteres
     */
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    /**
     * Descripción detallada del producto
     * Opcional, máximo 500 caracteres
     */
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    /**
     * Precio del producto
     * Obligatorio, debe ser positivo, máximo 2 decimales
     */
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
    @DecimalMax(value = "99999.99", message = "El precio no puede exceder 99,999.99")
    @Digits(integer = 5, fraction = 2, message = "El precio debe tener máximo 5 dígitos enteros y 2 decimales")
    private BigDecimal precio;

    /**
     * Cantidad en stock
     * Obligatorio, no puede ser negativo
     */
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Max(value = 999999, message = "El stock no puede exceder 999,999 unidades")
    private Integer stock;

    /**
     * Categoría del producto
     * Obligatorio, longitud entre 2 y 50 caracteres
     */
    @NotBlank(message = "La categoría es obligatoria")
    @Size(min = 2, max = 50, message = "La categoría debe tener entre 2 y 50 caracteres")
    private String categoria;

    /**
     * Estado del producto (activo/inactivo)
     * Por defecto es true
     */
    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo = true;

    /**
     * Fecha y hora de creación del producto
     * Solo lectura
     */
    private LocalDateTime fechaCreacion;

    /**
     * Fecha y hora de última actualización
     * Solo lectura
     */
    private LocalDateTime fechaActualizacion;

    /**
     * Código SKU del producto (Stock Keeping Unit)
     * Opcional, pero único si se proporciona
     */
    @Size(max = 50, message = "El código SKU no puede exceder 50 caracteres")
    private String sku;

    /**
     * Stock mínimo recomendado para alertas
     * Opcional, por defecto 0
     */
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo = 0;

    /**
     * Indica si el producto tiene stock suficiente
     * Campo calculado, no se persiste
     */
    public boolean tieneStockSuficiente() {
        return stock != null && stock > 0;
    }

    /**
     * Indica si el producto tiene stock bajo
     * Campo calculado basado en stockMinimo
     */
    public boolean tieneStockBajo() {
        return stock != null && stockMinimo != null && stock <= stockMinimo;
    }

    /**
     * Calcula el valor total del inventario para este producto
     *
     * @return Precio * Stock
     */
    public BigDecimal calcularValorInventario() {
        if (precio != null && stock != null) {
            return precio.multiply(BigDecimal.valueOf(stock));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Constructor para crear DTO con datos básicos
     *
     * @param nombre Nombre del producto
     * @param precio Precio del producto
     * @param stock Stock inicial
     * @param categoria Categoría del producto
     */
    public ProductoDTO(String nombre, BigDecimal precio, Integer stock, String categoria) {
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
        this.activo = true;
        this.stockMinimo = 0;
    }

    /**
     * Método para validar si el DTO tiene los datos mínimos requeridos
     *
     * @return true si tiene los datos básicos válidos
     */
    public boolean esValido() {
        return nombre != null && !nombre.trim().isEmpty() &&
                precio != null && precio.compareTo(BigDecimal.ZERO) > 0 &&
                stock != null && stock >= 0 &&
                categoria != null && !categoria.trim().isEmpty();
    }

    /**
     * Genera una representación compacta del producto
     *
     * @return String con formato "nombre - categoria - $precio"
     */
    public String toStringCompacto() {
        return String.format("%s - %s - $%s",
                nombre != null ? nombre : "Sin nombre",
                categoria != null ? categoria : "Sin categoría",
                precio != null ? precio.toString() : "0.00");
    }
}