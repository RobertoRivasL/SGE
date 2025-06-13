package informviva.gest.service;

import informviva.gest.model.Venta;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public interface ExportacionServicio {
    byte[] exportarClientes(String formato, LocalDateTime fechaInicio, LocalDateTime fechaFin, Map<String, Object> filtros);

    byte[] exportarProductos(String formato, Map<String, Object> filtros);

    byte[] exportarVentas(String formato, LocalDateTime fechaInicio, LocalDateTime fechaFin, Map<String, Object> filtros);

    byte[] exportarUsuarios(String formato, Map<String, Object> filtros);

    byte[] exportarReporteVentas(String formato, LocalDateTime fechaInicio, LocalDateTime fechaFin, String tipoReporte);

    byte[] exportarInventario(String formato, boolean incluirBajoStock, Integer umbralStock);

    byte[] exportarReporteFinanciero(String formato, LocalDateTime fechaInicio, LocalDateTime fechaFin, boolean incluirComparativo);

    List<String> getFormatosSoportados();

    boolean isFormatoSoportado(String formato);

    String getMimeType(String formato);

    String getExtensionArchivo(String formato);

    byte[] exportarVentasExcel(List<Venta> ventas, LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
