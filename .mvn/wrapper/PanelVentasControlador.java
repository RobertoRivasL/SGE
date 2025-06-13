package informviva.gest.controlador;

//* Controlador para el Panel de Ventas
// * Este controlador maneja la lógica y la vista del panel de ventas
// *
// * @author Roberto Rivas
// * @version 1.0
// */

import informviva.gest.dto.VentaResumenDTO;
import informviva.gest.service.ReporteServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

/**
 * Controlador dedicado para el Panel de Ventas
 * Maneja la vista detallada de análisis de ventas
 *
 * @author Roberto Rivas
 * @version 1.0
 */
@Controller
@RequestMapping("/panel-ventas")
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_VENTAS', 'ROLE_GERENTE')")
public class PanelVentasControlador {

    private static final Logger logger = LoggerFactory.getLogger(PanelVentasControlador.class);

    private final ReporteServicio reporteServicio;

    @Autowired
    public PanelVentasControlador(ReporteServicio reporteServicio) {
        this.reporteServicio = reporteServicio;
    }

    /**
     * Muestra el panel de ventas con filtros de fecha
     * Esta es la vista principal que el usuario trabajó mucho
     */
    @GetMapping
    public String mostrarPanelVentas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        try {
            // Establecer fechas por defecto si no se proporcionan
            LocalDate hoy = LocalDate.now();
            LocalDate inicioMes = hoy.withDayOfMonth(1);

            // Procesar fechas del filtro
            LocalDate fechaInicio = (startDate != null) ? startDate : inicioMes;
            LocalDate fechaFin = (endDate != null) ? endDate : hoy;

            // Validar que la fecha de inicio no sea posterior a la fecha fin
            if (fechaInicio.isAfter(fechaFin)) {
                fechaInicio = inicioMes;
                fechaFin = hoy;
                model.addAttribute("mensajeAdvertencia", "Las fechas fueron corregidas: la fecha de inicio no puede ser posterior a la fecha fin.");
            }

            // Agregar fechas al modelo para los inputs
            model.addAttribute("startDate", fechaInicio);
            model.addAttribute("endDate", fechaFin);

            // Generar el resumen de ventas
            VentaResumenDTO resumenVentas = reporteServicio.generarResumenVentas(fechaInicio, fechaFin);

            // Asegurar que no haya valores nulos
            validarResumenVentas(resumenVentas);

            // Agregar el resumen al modelo
            model.addAttribute("ventaSummary", resumenVentas);

            // Agregar información adicional para el panel
            agregarInformacionAdicional(model, fechaInicio, fechaFin);

            logger.info("Panel de ventas cargado exitosamente para el período: {} - {}", fechaInicio, fechaFin);

        } catch (Exception e) {
            logger.error("Error cargando panel de ventas: {}", e.getMessage(), e);
            cargarPanelVacio(model);
            model.addAttribute("mensajeError", "Error al cargar los datos. Por favor, intente nuevamente.");
        }

        return "panel-ventas";
    }

    /**
     * Endpoint para actualizar datos via AJAX
     */
    @GetMapping("/actualizar")
    public String actualizarDatos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        try {
            VentaResumenDTO resumenVentas = reporteServicio.generarResumenVentas(startDate, endDate);
            validarResumenVentas(resumenVentas);

            model.addAttribute("ventaSummary", resumenVentas);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);

            // Retornar solo el fragmento de datos actualizados
            return "panel-ventas :: datos-panel";

        } catch (Exception e) {
            logger.error("Error actualizando datos del panel: {}", e.getMessage());
            model.addAttribute("error", true);
            return "panel-ventas :: error-panel";
        }
    }

    /**
     * Valida y completa el resumen de ventas para evitar valores nulos
     */
    private void validarResumenVentas(VentaResumenDTO resumen) {
        if (resumen.getVentasPorVendedor() == null) {
            resumen.setVentasPorVendedor(Collections.emptyList());
        }
        if (resumen.getVentasPorPeriodo() == null) {
            resumen.setVentasPorPeriodo(Collections.emptyList());
        }
        if (resumen.getVentasPorCategoria() == null) {
            resumen.setVentasPorCategoria(Collections.emptyList());
        }
        if (resumen.getProductosMasVendidos() == null) {
            resumen.setProductosMasVendidos(Collections.emptyList());
        }

        // Asegurar que los valores numéricos no sean nulos
        if (resumen.getTotalVentas() == null) {
            resumen.setTotalVentas(BigDecimal.ZERO);
        }
        if (resumen.getTotalTransacciones() == null) {
            resumen.setTotalTransacciones(0L);
        }
        if (resumen.getTicketPromedio() == null) {
            resumen.setTicketPromedio(BigDecimal.ZERO);
        }
        if (resumen.getClientesNuevos() == null) {
            resumen.setClientesNuevos(0L);
        }
    }

    /**
     * Agrega información adicional específica para el panel
     */
    private void agregarInformacionAdicional(Model model, LocalDate fechaInicio, LocalDate fechaFin) {
        // Calcular días del período
        long diasPeriodo = java.time.temporal.ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1;
        model.addAttribute("diasPeriodo", diasPeriodo);

        // Información del período
        model.addAttribute("nombrePeriodo", generarNombrePeriodo(fechaInicio, fechaFin));

        // Agregar flags para mostrar/ocultar secciones
        model.addAttribute("mostrarGraficos", true);
        model.addAttribute("mostrarTablas", true);
    }

    /**
     * Genera un nombre descriptivo para el período seleccionado
     */
    private String generarNombrePeriodo(LocalDate inicio, LocalDate fin) {
        if (inicio.equals(fin)) {
            return "Día: " + inicio;
        } else if (inicio.getMonth().equals(fin.getMonth()) && inicio.getYear() == fin.getYear()) {
            return "Mes: " + inicio.getMonth().name() + " " + inicio.getYear();
        } else {
            return "Período: " + inicio + " al " + fin;
        }
    }

    /**
     * Carga un panel vacío en caso de error
     */
    private void cargarPanelVacio(Model model) {
        VentaResumenDTO dto = new VentaResumenDTO();
        dto.setTotalVentas(BigDecimal.ZERO);
        dto.setTotalTransacciones(0L);
        dto.setTicketPromedio(BigDecimal.ZERO);
        dto.setClientesNuevos(0L);
        dto.setTotalArticulosVendidos(0L);
        dto.setProductosMasVendidos(Collections.emptyList());
        dto.setVentasPorPeriodo(Collections.emptyList());
        dto.setVentasPorCategoria(Collections.emptyList());
        dto.setVentasPorVendedor(Collections.emptyList());

        model.addAttribute("ventaSummary", dto);

        // Fechas por defecto
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        model.addAttribute("startDate", inicioMes);
        model.addAttribute("endDate", hoy);
    }
}