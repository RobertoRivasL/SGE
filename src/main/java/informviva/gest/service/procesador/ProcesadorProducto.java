package informviva.gest.service.procesador;


import informviva.gest.dto.ValidacionResultadoDTO;
import informviva.gest.model.Producto;
import informviva.gest.service.ProductoServicio;
import informviva.gest.service.importacion.ImportacionValidador;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.Map;


/**
 * Procesador especializado para productos
 */
@Component
public class ProcesadorProducto implements ProcesadorEntidad<Producto> {

    private static final Logger logger = LoggerFactory.getLogger(ProcesadorProducto.class);

    @Autowired
    private ProductoServicio productoServicio;

    @Autowired
    private ImportacionValidador validador;

    @Override
    public String getTipoEntidad() {
        return "producto";
    }

    @Override
    @Transactional
    public Producto mapearDesdeArchivo(Map<String, Object> fila, int numeroFila) {
        try {
            Producto producto = new Producto();

            producto.setCodigo(obtenerValorString(fila, "codigo").toUpperCase());
            producto.setNombre(obtenerValorString(fila, "nombre"));
            producto.setDescripcion(obtenerValorString(fila, "descripcion"));
            producto.setMarca(obtenerValorString(fila, "marca"));
            producto.setModelo(obtenerValorString(fila, "modelo"));

            // Precio
            String precioStr = obtenerValorString(fila, "precio");
            if (!precioStr.isEmpty()) {
                producto.setPrecio(Double.parseDouble(precioStr));
            }

            // Stock
            String stockStr = obtenerValorString(fila, "stock");
            if (!stockStr.isEmpty()) {
                producto.setStock(Integer.parseInt(stockStr));
            } else {
                producto.setStock(0);
            }

            producto.setActivo(true);
            producto.setFechaCreacion(LocalDateTime.now());
            producto.setFechaActualizacion(LocalDateTime.now());

            return producto;
        } catch (Exception e) {
            logger.error("Error mapeando producto en fila {}: {}", numeroFila, e.getMessage());
            throw new RuntimeException("Error en fila " + numeroFila + ": " + e.getMessage());
        }
    }

    @Override
    public boolean existeEntidad(Producto producto) {
        return productoServicio.existePorCodigo(producto.getCodigo());
    }

    @Override
    @Transactional
    public void guardarEntidad(Producto producto) {
        productoServicio.guardar(producto);
    }

    @Override
    public ValidacionResultadoDTO validarEntidad(Producto producto, int numeroFila) {
        Map<String, Object> fila = Map.of(
                "codigo", producto.getCodigo(),
                "nombre", producto.getNombre(),
                "descripcion", producto.getDescripcion() != null ? producto.getDescripcion() : "",
                "precio", producto.getPrecio() != null ? producto.getPrecio().toString() : "",
                "stock", producto.getStock() != null ? producto.getStock().toString() : "0",
                "marca", producto.getMarca() != null ? producto.getMarca() : "",
                "modelo", producto.getModelo() != null ? producto.getModelo() : ""
        );

        return validador.validarFila(fila, getTipoEntidad(), numeroFila);
    }

    private String obtenerValorString(Map<String, Object> fila, String campo) {
        Object valor = fila.get(campo);
        return valor != null ? valor.toString().trim() : "";
    }
}