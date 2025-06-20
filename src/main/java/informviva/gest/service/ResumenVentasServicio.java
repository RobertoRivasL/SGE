package informviva.gest.service;

import informviva.gest.dto.VentaResumenDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;



/**
 * @author Roberto Rivas
 * @version 2.0
 *
 */

// Servicio para inicializar datos
@Service
public class ResumenVentasServicio {

    public <T> java.util.List<T> inicializarLista(java.util.List<T> lista) {
        return lista != null ? lista : java.util.Collections.emptyList();
    }

    public <K, V> java.util.Map<K, V> inicializarMapa(java.util.Map<K, V> mapa) {
        return mapa != null ? mapa : java.util.Collections.emptyMap();
    }

    public VentaResumenDTO crearResumenVentasVacio() {
        VentaResumenDTO dto = new VentaResumenDTO();
        dto.setTotalVentas(BigDecimal.ZERO);
        dto.setTotalTransacciones(0L);
        dto.setTicketPromedio(BigDecimal.ZERO);
        dto.setClientesNuevos(0L);
        dto.setProductosMasVendidos(Collections.emptyList());
        dto.setVentasPorPeriodo(Collections.emptyList());
        dto.setVentasPorCategoria(Collections.emptyMap());
        return dto;
    }

    public LocalDate[] validarFechas(String startDate, String endDate) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate fechaInicio = inicioMes;
        LocalDate fechaFin = hoy;

        if (startDate != null && !startDate.isEmpty()) {
            fechaInicio = LocalDate.parse(startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            fechaFin = LocalDate.parse(endDate);
        }
        if (fechaInicio.isAfter(fechaFin)) {
            fechaInicio = inicioMes;
            fechaFin = hoy;
        }
        return new LocalDate[]{fechaInicio, fechaFin};
    }
}