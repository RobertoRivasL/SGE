package informviva.gest.service;

import informviva.gest.dto.VentaPorCategoriaDTO;
import informviva.gest.dto.VentaResumenDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para generar resúmenes de ventas
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Service
public class ResumenVentasServicio {

    /**
     * Inicializa un mapa a partir de una lista de ventas por categoría
     * CORRECCIÓN: Cambiar signatura para recibir List en lugar de Map
     *
     * @param ventasPorCategoria Lista de ventas agrupadas por categoría
     * @return Mapa con categoría como clave y total como valor
     */
    public Map<String, Object> inicializarMapa(List<VentaPorCategoriaDTO> ventasPorCategoria) {
        return ventasPorCategoria.stream()
                .collect(Collectors.toMap(
                        VentaPorCategoriaDTO::getCategoria,
                        dto -> (Object) dto.getTotal()
                ));
    }

    /**
     * Convierte lista a mapa de String a Double (método auxiliar)
     *
     * @param ventasPorCategoria Lista de ventas por categoría
     * @return Mapa con categoría como clave y total como valor Double
     */
    public Map<String, Double> convertirListaAMapa(List<VentaPorCategoriaDTO> ventasPorCategoria) {
        if (ventasPorCategoria == null || ventasPorCategoria.isEmpty()) {
            return Map.of(); // Mapa vacío
        }

        return ventasPorCategoria.stream()
                .collect(Collectors.toMap(
                        VentaPorCategoriaDTO::getCategoria,
                        VentaPorCategoriaDTO::getTotal
                ));
    }

    /**
     * Método sobrecargado para compatibilidad con código existente
     * que espera Map como parámetro
     *
     * @param ventasPorCategoriaMap Mapa de ventas por categoría
     * @return El mismo mapa envuelto en Map<String, Object>
     */
    public Map<String, Object> inicializarMapa(Map<String, Double> ventasPorCategoriaMap) {
        if (ventasPorCategoriaMap == null || ventasPorCategoriaMap.isEmpty()) {
            return Map.of();
        }

        return ventasPorCategoriaMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (Object) entry.getValue()
                ));
    }

    /**
     * Crea un resumen de ventas vacío
     *
     * @return Un objeto VentaResumenDTO con listas vacías
     */

    public VentaResumenDTO crearResumenVentasVacio() {
        VentaResumenDTO resumen = new VentaResumenDTO();
        resumen.setVentasPorVendedor(new ArrayList<>());
        resumen.setVentasPorPeriodo(new ArrayList<>());
        resumen.setVentasPorCategoria(new ArrayList<>());
        resumen.setProductosMasVendidos(new ArrayList<>());
        return resumen;
    }
}