package informviva.gest.service.impl;


import informviva.gest.dto.ClienteReporteDTO;
import informviva.gest.model.Cliente;
import informviva.gest.service.ClienteServicio;
import informviva.gest.service.ReporteClienteServicio;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para reportes de clientes
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Service
public class ReporteClienteServicioImpl implements ReporteClienteServicio {

    private static final Logger logger = LoggerFactory.getLogger(ReporteClienteServicioImpl.class);

    @Autowired
    private ClienteServicio clienteServicio;

    @Override
    public List<ClienteReporteDTO> generarReporteClientes(LocalDate fechaInicio, LocalDate fechaFin) {
        logger.info("Generando reporte de clientes para el período: {} - {}", fechaInicio, fechaFin);

        try {
            // TODO: Implementar lógica de reporte
            // Este método necesita:
            // 1. Obtener clientes con clienteServicio.buscarTodos()
            // 2. Para cada cliente, consultar sus ventas
            // 3. Calcular estadísticas (comprasRealizadas, totalCompras, etc.)
            // 4. Mapear a ClienteReporteDTO

            logger.warn("Método generarReporteClientes() necesita implementación completa");
            List<ClienteReporteDTO> reporte = new ArrayList<>();

            // Por ahora retorna lista vacía
            // NOTA: Este método estaba llamando a clienteServicio.obtenerClientesConCompras()
            // que no existe. Necesita implementación propia.

            logger.info("Reporte generado con {} clientes", reporte.size());
            return reporte;

        } catch (Exception e) {
            logger.error("Error al generar reporte de clientes: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> obtenerEstadisticasGenerales(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> estadisticas = new HashMap<>();

        try {
            List<ClienteReporteDTO> clientesConCompras = generarReporteClientes(fechaInicio, fechaFin);
            // TODO: Implementar conteo de clientes nuevos
            // Long totalClientes = clienteServicio.contarClientesNuevos(fechaInicio, fechaFin);
            Long totalClientes = 0L; // Temporal hasta implementar

            // Calcular estadísticas básicas
            estadisticas.put("totalClientesRegistrados", clienteServicio.buscarTodos().size());
            estadisticas.put("clientesNuevosEnPeriodo", totalClientes);
            estadisticas.put("clientesConCompras", clientesConCompras.size());
            estadisticas.put("clientesActivos", clienteServicio.contarActivos());

            // Calcular valores monetarios
            BigDecimal ventasTotales = clientesConCompras.stream()
                    .map(ClienteReporteDTO::getTotalCompras)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            estadisticas.put("ventasTotales", ventasTotales);

            // Calcular promedios
            if (!clientesConCompras.isEmpty()) {
                BigDecimal promedioComprasPorCliente = ventasTotales.divide(
                        BigDecimal.valueOf(clientesConCompras.size()), 2, RoundingMode.HALF_UP);
                estadisticas.put("promedioComprasPorCliente", promedioComprasPorCliente);

                int totalTransacciones = clientesConCompras.stream()
                        .mapToInt(ClienteReporteDTO::getComprasRealizadas)
                        .sum();

                BigDecimal ticketPromedio = ventasTotales.divide(
                        BigDecimal.valueOf(totalTransacciones), 2, RoundingMode.HALF_UP);
                estadisticas.put("ticketPromedio", ticketPromedio);

                estadisticas.put("totalTransacciones", totalTransacciones);
            } else {
                estadisticas.put("promedioComprasPorCliente", BigDecimal.ZERO);
                estadisticas.put("ticketPromedio", BigDecimal.ZERO);
                estadisticas.put("totalTransacciones", 0);
            }

        } catch (Exception e) {
            logger.error("Error al calcular estadísticas generales: {}", e.getMessage());
        }

        return estadisticas;
    }

    @Override
    public List<ClienteReporteDTO> obtenerTopClientes(int limite, LocalDate fechaInicio, LocalDate fechaFin) {
        return generarReporteClientes(fechaInicio, fechaFin).stream()
                .limit(limite)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> analizarDistribucionAntiguedad() {
        Map<String, Long> distribucion = new LinkedHashMap<>();
        LocalDate hoy = LocalDate.now();

        List<Cliente> todosLosClientes = clienteServicio.buscarTodos();

        // Inicializar contadores
        distribucion.put("Menos de 3 meses", 0L);
        distribucion.put("3 a 6 meses", 0L);
        distribucion.put("6 a 12 meses", 0L);
        distribucion.put("1 a 2 años", 0L);
        distribucion.put("Más de 2 años", 0L);

        todosLosClientes.forEach(cliente -> {
            if (cliente.getFechaRegistro() != null) {
                long mesesDiferencia = ChronoUnit.MONTHS.between(cliente.getFechaRegistro(), hoy);

                String categoria;
                if (mesesDiferencia < 3) {
                    categoria = "Menos de 3 meses";
                } else if (mesesDiferencia < 6) {
                    categoria = "3 a 6 meses";
                } else if (mesesDiferencia < 12) {
                    categoria = "6 a 12 meses";
                } else if (mesesDiferencia < 24) {
                    categoria = "1 a 2 años";
                } else {
                    categoria = "Más de 2 años";
                }

                distribucion.put(categoria, distribucion.get(categoria) + 1);
            }
        });

        return distribucion;
    }

    @Override
    public List<Cliente> obtenerClientesInactivos(int diasInactividad) {
        LocalDate fechaLimite = LocalDate.now().minusDays(diasInactividad);

        // TODO: Implementar lógica correcta para clientes inactivos
        // Este método necesita:
        // 1. Obtener todos los clientes
        // 2. Para cada cliente, verificar fecha de última compra
        // 3. Filtrar los que no han comprado en X días

        logger.warn("Método obtenerClientesInactivos() necesita implementación completa");

        // Por ahora retorna lista vacía
        // NOTA: Este método estaba llamando a clienteServicio.obtenerClientesConCompras()
        // que no existe. Necesita implementación propia consultando VentaServicio.

        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> calcularMetricasRetencion(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> metricas = new HashMap<>();

        try {
            // Clientes que compraron en el período actual
            List<ClienteReporteDTO> clientesActuales = generarReporteClientes(fechaInicio, fechaFin);

            // Clientes que compraron en el período anterior (mismo rango de tiempo)
            long diasPeriodo = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
            LocalDate inicioAnterior = fechaInicio.minusDays(diasPeriodo);
            LocalDate finAnterior = fechaInicio.minusDays(1);

            List<ClienteReporteDTO> clientesAnteriores = generarReporteClientes(inicioAnterior, finAnterior);

            // Calcular retención
            Set<Long> idsClientesActuales = clientesActuales.stream()
                    .map(ClienteReporteDTO::getId)
                    .collect(Collectors.toSet());

            Set<Long> idsClientesAnteriores = clientesAnteriores.stream()
                    .map(ClienteReporteDTO::getId)
                    .collect(Collectors.toSet());

            // Clientes retenidos (que compraron en ambos períodos)
            long clientesRetenidos = idsClientesActuales.stream()
                    .mapToLong(id -> idsClientesAnteriores.contains(id) ? 1 : 0)
                    .sum();

            // Calcular tasas
            double tasaRetencion = clientesAnteriores.isEmpty() ? 0.0 :
                    (double) clientesRetenidos / clientesAnteriores.size() * 100;

            long clientesNuevos = clientesActuales.size() - clientesRetenidos;
            double tasaCrecimiento = clientesAnteriores.isEmpty() ? 0.0 :
                    (double) clientesNuevos / clientesAnteriores.size() * 100;

            metricas.put("clientesActuales", clientesActuales.size());
            metricas.put("clientesAnteriores", clientesAnteriores.size());
            metricas.put("clientesRetenidos", clientesRetenidos);
            metricas.put("clientesNuevos", clientesNuevos);
            metricas.put("tasaRetencion", Math.round(tasaRetencion * 100.0) / 100.0);
            metricas.put("tasaCrecimiento", Math.round(tasaCrecimiento * 100.0) / 100.0);

        } catch (Exception e) {
            logger.error("Error al calcular métricas de retención: {}", e.getMessage());
        }

        return metricas;
    }

    @Override
    public byte[] exportarReporteClientes(List<ClienteReporteDTO> clientes, String formato) {
        switch (formato.toUpperCase()) {
            case "EXCEL":
                return exportarAExcel(clientes);
            case "CSV":
                return exportarACSV(clientes);
            default:
                throw new IllegalArgumentException("Formato no soportado: " + formato);
        }
    }

    /**
     * Exporta la lista de clientes a formato Excel
     */
    private byte[] exportarAExcel(List<ClienteReporteDTO> clientes) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Reporte de Clientes");

            // Crear estilos
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy"));

            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"RUT", "Nombre Completo", "Email", "Fecha Registro",
                    "Compras Realizadas", "Total Compras", "Promedio por Compra", "Última Compra"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Llenar datos
            int rowIdx = 1;
            for (ClienteReporteDTO cliente : clientes) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(cliente.getRut());
                row.createCell(1).setCellValue(cliente.getNombreCompleto());
                row.createCell(2).setCellValue(cliente.getEmail());

                if (cliente.getFechaRegistro() != null) {
                    Cell dateCell = row.createCell(3);
                    dateCell.setCellValue(java.sql.Date.valueOf(cliente.getFechaRegistro()));
                    dateCell.setCellStyle(dateStyle);
                }

                row.createCell(4).setCellValue(cliente.getComprasRealizadas());

                Cell totalCell = row.createCell(5);
                totalCell.setCellValue(cliente.getTotalCompras().doubleValue());
                totalCell.setCellStyle(currencyStyle);

                Cell promedioCell = row.createCell(6);
                promedioCell.setCellValue(cliente.getPromedioPorCompra().doubleValue());
                promedioCell.setCellStyle(currencyStyle);

                if (cliente.getUltimaCompra() != null) {
                    Cell ultimaCompraCell = row.createCell(7);
                    ultimaCompraCell.setCellValue(java.sql.Date.valueOf(cliente.getUltimaCompra()));
                    ultimaCompraCell.setCellStyle(dateStyle);
                }
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            logger.error("Error al exportar a Excel: {}", e.getMessage());
            return new byte[0];
        }
    }

    /**
     * Exporta la lista de clientes a formato CSV
     */
    private byte[] exportarACSV(List<ClienteReporteDTO> clientes) {
        StringBuilder csv = new StringBuilder();

        // Encabezados
        csv.append("RUT,Nombre Completo,Email,Fecha Registro,Compras Realizadas,Total Compras,Promedio por Compra,Última Compra\n");

        // Datos
        for (ClienteReporteDTO cliente : clientes) {
            csv.append(cliente.getRut()).append(",");
            csv.append("\"").append(cliente.getNombreCompleto()).append("\",");
            csv.append(cliente.getEmail()).append(",");
            csv.append(cliente.getFechaRegistro() != null ? cliente.getFechaRegistro().toString() : "").append(",");
            csv.append(cliente.getComprasRealizadas()).append(",");
            csv.append(cliente.getTotalCompras()).append(",");
            csv.append(cliente.getPromedioPorCompra()).append(",");
            csv.append(cliente.getUltimaCompra() != null ? cliente.getUltimaCompra().toString() : "");
            csv.append("\n");
        }

        return csv.toString().getBytes();
    }
}
