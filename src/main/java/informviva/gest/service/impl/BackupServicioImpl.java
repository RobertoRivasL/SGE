package informviva.gest.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import informviva.gest.dto.ExportConfigDTO;
import informviva.gest.service.BackupServicio;
import informviva.gest.service.ExportacionServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Implementación del respaldo manual usando los exportadores existentes.
 *
 * El respaldo contiene CSV de los módulos principales y un manifiesto JSON.
 * No incluye credenciales ni secretos de configuración.
 */
@Service
public class BackupServicioImpl implements BackupServicio {

    private static final Logger logger = LoggerFactory.getLogger(BackupServicioImpl.class);
    private static final DateTimeFormatter FILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final ExportacionServicio exportacionServicio;
    private final ObjectMapper objectMapper;
    private final Path backupDirectory;

    public BackupServicioImpl(
            ExportacionServicio exportacionServicio,
            @Value("${app.backup.directory:./backups}") String backupDirectory) {
        this.exportacionServicio = exportacionServicio;
        this.backupDirectory = Paths.get(backupDirectory).toAbsolutePath().normalize();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void realizarBackupManual() {
        crearBackup();
    }

    @Override
    public Path crearBackup() {
        LocalDateTime fechaCreacion = LocalDateTime.now();
        Path archivoBackup = backupDirectory.resolve(
                "sge_backup_" + FILE_DATE_FORMAT.format(fechaCreacion) + ".zip");

        try {
            Files.createDirectories(backupDirectory);
            ExportConfigDTO config = new ExportConfigDTO();
            config.setFormato("CSV");

            try (OutputStream output = Files.newOutputStream(archivoBackup);
                 ZipOutputStream zip = new ZipOutputStream(output)) {
                agregarEntrada(zip, "clientes.csv",
                        exportacionServicio.exportarClientes(config.paraClientes("CSV", null, null)));
                agregarEntrada(zip, "productos.csv",
                        exportacionServicio.exportarProductos(config.paraProductos("CSV")));
                agregarEntrada(zip, "usuarios.csv",
                        exportacionServicio.exportarUsuarios(config.paraUsuarios("CSV")));
                agregarEntrada(zip, "ventas.csv",
                        exportacionServicio.exportarVentas(config.paraVentas("CSV", null, null)));
                agregarManifiesto(zip, fechaCreacion);
            }

            logger.info("Respaldo creado: {}", archivoBackup);
            return archivoBackup;
        } catch (IOException | RuntimeException e) {
            logger.error("Error al crear respaldo en {}", archivoBackup, e);
            throw new IllegalStateException("No se pudo crear el respaldo", e);
        }
    }

    private void agregarEntrada(ZipOutputStream zip, String nombre, byte[] contenido)
            throws IOException {
        if (contenido == null) {
            throw new IOException("El exportador devolvió contenido nulo para " + nombre);
        }
        zip.putNextEntry(new ZipEntry(nombre));
        zip.write(contenido);
        zip.closeEntry();
    }

    private void agregarManifiesto(ZipOutputStream zip, LocalDateTime fechaCreacion)
            throws IOException {
        Map<String, Object> manifiesto = new LinkedHashMap<>();
        manifiesto.put("aplicacion", "informviva-gest");
        manifiesto.put("versionFormato", 1);
        manifiesto.put("fechaCreacion", fechaCreacion);
        manifiesto.put("archivos", new String[]{
                "clientes.csv", "productos.csv", "usuarios.csv", "ventas.csv"
        });

        agregarEntrada(zip, "manifiesto.json",
                objectMapper.writeValueAsBytes(manifiesto));
    }
}
