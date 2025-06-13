package informviva.gest;

import informviva.gest.model.Producto;
import informviva.gest.model.Venta;
import informviva.gest.model.VentaDetalle;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Venta_03 {

    @Test
    void testSeleccionDeProductos() {
        // Crear productos
        Producto producto1 = new Producto(1L, "Producto A", true);
        Producto producto2 = new Producto(2L, "Producto B", true);

        // Crear detalles de venta
        VentaDetalle detalle1 = new VentaDetalle();
        detalle1.setProducto(producto1);
        detalle1.setCantidad(2);

        VentaDetalle detalle2 = new VentaDetalle();
        detalle2.setProducto(producto2);
        detalle2.setCantidad(3);

        // Asociar detalles a la venta
        List<VentaDetalle> detalles = new ArrayList<>();
        detalles.add(detalle1);
        detalles.add(detalle2);

        Venta venta = new Venta();
        venta.setDetalles(detalles);

        // Verificar que los productos se asociaron correctamente
        assertNotNull(venta.getDetalles());
        assertEquals(2, venta.getDetalles().size());
        assertEquals("Producto A", venta.getDetalles().get(0).getProducto().getNombre());
        assertEquals("Producto B", venta.getDetalles().get(1).getProducto().getNombre());
    }
}