package informviva.gest.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import informviva.gest.dto.ImportacionResultadoDTO;
import informviva.gest.model.ImportacionHistorial;
import informviva.gest.model.Usuario;
import informviva.gest.repository.ImportacionHistorialRepositorio;
import informviva.gest.service.ServicioImportacionHistorial;
import informviva.gest.service.UsuarioServicio;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Implementación del servicio de historial de importaciones
 *
 * @author Roberto Rivas
 * @version 2.0
 */
@Slf4j
@Service
@Transactional
public class ServicioImportacionHistorialImpl implements ServicioImportacionHistorial {

    private final ImportacionHistorialRepositorio historialRepositorio;
    private final UsuarioServicio usuarioServicio;
    private final ObjectMapper objectMapper;

    @Autowired
    public ServicioImportacionHistorialImpl(ImportacionHistorialRepositorio historialRepositorio,
                                            UsuarioServicio usuarioServicio,
                                            ObjectMapper objectMapper) {
        this.historialRepositorio = historialRepositorio;
        this.usuarioServicio = usuarioServicio;
        this.objectMapper = objectMapper;
    }

    @Override
    public ImportacionHistorial registrarImportacion(ImportacionResultadoDTO resultado) {
        try {
            Usuario usuario = usuarioServicio.buscarPorId(resultado.getUsuarioId());

            ImportacionHistorial historial = ImportacionHistorial.builder()
                    .tipoImportacion(resultado.getTipoImportacion())
                    .nombreArchivo(resultado.getNombreArchivo())
                    .fechaImportacion(resultado.getFechaImportacion())
                    .usuario(usuario)
                    .totalRegistros(resultado.getTotalRegistros())
                    .registrosExitosos(resultado.getRegistrosExitosos())
                    .registrosConError(resultado.getRegistrosConError())
                    .tiempoProcesamientoMs(resultado.getTiempoProcesamientoMs())
                    .exitoso(resultado.isExitoso())
                    .resumen(resultado.getResumen())
                    .build();

            // Convertir errores y advertencias a JSON
            if (resultado.getErrores() != null && !resultado.getErrores().isEmpty()) {
                try {
                    historial.setErrores(objectMapper.writeValueAsString(resultado.getErrores()));
                } catch (JsonProcessingException e) {
                    log.warn("Error serializando errores de importación: {}", e.getMessage());
                    historial.setErrores(resultado.getErrores().toString());
                }
            }

            if (resultado.getAdvertencias() != null && !resultado.getAdvertencias().isEmpty()) {
                try {
                    historial.setAdvertencias(objectMapper.writeValueAsString(resultado.getAdvertencias()));
                } catch (JsonProcessingException e) {
                    log.warn("Error serializando advertencias de importación: {}", e.getMessage());
                    historial.setAdvertencias(resultado.getAdvertencias().toString());
                }
            }

            return historialRepositorio.save(historial);

        } catch (Exception e) {
            log.error("Error registrando importación en historial: {}", e.getMessage(), e);
            throw new RuntimeException("Error registrando importación en historial", e);
        }
    }

    @Override
    public Page<ImportacionHistorial> obtenerHistorial(Pageable pageable) {
        return historialRepositorio.findAllByOrderByFechaImportacionDesc(pageable);
    }

    @Override
    public Page<ImportacionHistorial> obtenerHistorialPorTipo(String tipo, Pageable pageable) {
        return historialRepositorio.findByTipoImportacionOrderByFechaImportacionDesc(tipo, pageable);
    }

    @Override
    public List<ImportacionHistorial> obtenerHistorialPorUsuario(Long usuarioId) {
        return historialRepositorio.findByUsuarioIdOrderByFechaImportacionDesc(usuarioId);
    }

    @Override
    public List<ImportacionHistorial> obtenerHistorialPorFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return historialRepositorio.findByFechaImportacionBetweenOrderByFechaImportacionDesc(fechaInicio, fechaFin);
    }

    @Override
    public List<ImportacionHistorial> obtenerUltimasImportaciones() {
        return historialRepositorio.findTop10ByOrderByFechaImportacionDesc();
    }

    @Override
    public Map<String, Object> obtenerEstadisticasPorTipo(LocalDateTime fechaInicio) {
        List<Object[]> estadisticas = historialRepositorio.obtenerEstadisticasPorTipo(fechaInicio);

        Map<String, Object> resultado = new HashMap<>();
        List<Map<String, Object>> estadisticasPorTipo = new ArrayList<>();

        long totalImportaciones = 0;
        long totalExitosas = 0;
        long totalConError = 0;
        long totalRegistrosProcesados = 0;
        long totalRegistrosExitosos = 0;
        long totalRegistrosConError = 0;

        for (Object[] stat : estadisticas) {
            String tipo = (String) stat[0];
            Long totalCount = (Long) stat[1];
            Long exitosasCount = (Long) stat[2];
            Long erroresCount = (Long) stat[3];
            Long registrosTotales = (Long) stat[4];
            Long registrosOk = (Long) stat[5];
            Long registrosError = (Long) stat[6];

            Map<String, Object> tipoStats = new HashMap<>();
            tipoStats.put("tipo", tipo);
            tipoStats.put("totalImportaciones", totalCount);
            tipoStats.put("importacionesExitosas", exitosasCount);
            tipoStats.put("importacionesConError", erroresCount);
            tipoStats.put("totalRegistros", registrosTotales);
            tipoStats.put("registrosExitosos", registrosOk);
            tipoStats.put("registrosConError", registrosError);
            tipoStats.put("porcentajeExito",
                    registrosTotales != null && registrosTotales > 0 ?
                            (registrosOk.doubleValue() / registrosTotales.doubleValue()) * 100.0 : 0.0);

            estadisticasPorTipo.add(tipoStats);

            totalImportaciones += totalCount;
            totalExitosas += exitosasCount;
            totalConError += erroresCount;
            totalRegistrosProcesados += registrosTotales != null ? registrosTotales : 0;
            totalRegistrosExitosos += registrosOk != null ? registrosOk : 0;
            totalRegistrosConError += registrosError != null ? registrosError : 0;
        }

        resultado.put("estadisticasPorTipo", estadisticasPorTipo);
        resultado.put("resumenGeneral", Map.of(
                "totalImportaciones", totalImportaciones,
                "totalExitosas", totalExitosas,
                "totalConError", totalConError,
                "totalRegistrosProcesados", totalRegistrosProcesados,
                "totalRegistrosExitosos", totalRegistrosExitosos,
                "totalRegistrosConError", totalRegistrosConError,
                "porcentajeExitoGeneral",
                totalRegistrosProcesados > 0 ?
                        (totalRegistrosExitosos / (double) totalRegistrosProcesados) * 100.0 : 0.0
        ));

        return resultado;
    }

    @Override
    public Map<String, Object> obtenerEstadisticasGenerales() {
        LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);
        return obtenerEstadisticasPorTipo(hace30Dias);
    }

    @Override
    public List<ImportacionHistorial> buscarPorNombreArchivo(String nombreArchivo) {
        return historialRepositorio.findByNombreArchivoContainingIgnoreCaseOrderByFechaImportacionDesc(nombreArchivo);
    }

    @Override
    @Transactional
    public int limpiarHistorialAntiguo(int diasAntiguos) {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(diasAntiguos);
        List<ImportacionHistorial> registrosAntiguos = historialRepositorio
                .findByFechaImportacionBetweenOrderByFechaImportacionDesc(
                        LocalDateTime.of(2000, 1, 1, 0, 0), fechaLimite);

        historialRepositorio.deleteAll(registrosAntiguos);
        log.info("Eliminados {} registros de historial de importaciones anteriores a {}",
                registrosAntiguos.size(), fechaLimite);

        return registrosAntiguos.size();
    }

    @Override
    public byte[] exportarHistorialAExcel(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Historial de Importaciones");

            // Crear estilos
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));

            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "ID", "Tipo", "Nombre Archivo", "Fecha", "Usuario",
                    "Total Registros", "Exitosos", "Con Error", "% Éxito",
                    "Tiempo (seg)", "Estado", "Resumen"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Obtener datos
            List<ImportacionHistorial> historial = fechaInicio != null && fechaFin != null ?
                    obtenerHistorialPorFechas(fechaInicio, fechaFin) :
                    historialRepositorio.findAllByOrderByFechaImportacionDesc();

            // Llenar datos
            int rowIdx = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (ImportacionHistorial registro : historial) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(registro.getId());
                row.createCell(1).setCellValue(registro.getTipoImportacion());
                row.createCell(2).setCellValue(registro.getNombreArchivo());

                if (registro.getFechaImportacion() != null) {
                    Cell dateCell = row.createCell(3);
                    dateCell.setCellValue(registro.getFechaImportacion().format(formatter));
                }

                row.createCell(4).setCellValue(registro.getUsuario() != null ?
                        registro.getUsuario().getNombreCompleto() : "");

                row.createCell(5).setCellValue(registro.getTotalRegistros() != null ?
                        registro.getTotalRegistros() : 0);
                row.createCell(6).setCellValue(registro.getRegistrosExitosos() != null ?
                        registro.getRegistrosExitosos() : 0);
                row.createCell(7).setCellValue(registro.getRegistrosConError() != null ?
                        registro.getRegistrosConError() : 0);

                row.createCell(8).setCellValue(registro.getPorcentajeExito());
                row.createCell(9).setCellValue(registro.getTiempoProcesamientoSegundos());
                row.createCell(10).setCellValue(registro.getExitoso() != null && registro.getExitoso() ?
                        "EXITOSO" : "CON ERRORES");
                row.createCell(11).setCellValue(registro.getResumen() != null ? registro.getResumen() : "");
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error exportando historial a Excel: {}", e.getMessage(), e);
            return new byte[0];
        }
    }
}