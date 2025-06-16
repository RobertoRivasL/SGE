#!/bin/bash

# Script de reparación de errores de compilación después de consolidación
# Fecha: $(date)
# Descripción: Repara errores sistemáticos identificados en la compilación

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para logging
log() {
    echo -e "${BLUE}[$(date '+%H:%M:%S')]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Función para crear backup
create_backup() {
    log "🔄 Creando backup antes de reparación..."
    git add .
    git commit -m "BACKUP: Antes de reparación de errores de compilación - $(date)" || warning "No hay cambios para commitear"
    success "Backup creado"
}

# Función para limpiar duplicaciones en POM
fix_pom_duplications() {
    log "🔧 Reparando duplicaciones en pom.xml..."

    # Backup del pom original
    cp pom.xml pom.xml.backup

    # Remover dependencias duplicadas
    sed -i '/<!-- Dependencia duplicada de poi-ooxml -->/,+4d' pom.xml
    sed -i '/<!-- Dependencia duplicada de jackson-datatype-jsr310 -->/,+4d' pom.xml

    # Remover plugins duplicados del maven-compiler-plugin
    awk '
    BEGIN { found_compiler_plugin = 0 }
    /<groupId>org.apache.maven.plugins<\/groupId>/ && /<artifactId>maven-compiler-plugin<\/artifactId>/ {
        if (found_compiler_plugin == 0) {
            found_compiler_plugin = 1
            print
        } else {
            # Skip this duplicate plugin block
            while (getline && !</plugin>/) { }
            next
        }
    }
    { print }
    ' pom.xml > pom.xml.tmp && mv pom.xml.tmp pom.xml

    success "POM reparado"
}

# Función para mover archivos de test mal ubicados
fix_test_location() {
    log "🔧 Moviendo archivos de test a ubicación correcta..."

    # Crear directorios de test si no existen
    mkdir -p src/test/java/informviva/gest/service/impl

    # Mover archivos de test mal ubicados
    if [ -f "src/main/java/informviva/gest/service/impl/ValidacionDatosServicioTest.java" ]; then
        mv "src/main/java/informviva/gest/service/impl/ValidacionDatosServicioTest.java" \
           "src/test/java/informviva/gest/service/impl/ValidacionDatosServicioTest.java"
        success "Test movido a ubicación correcta"
    fi
}

# Función para crear clases faltantes
create_missing_classes() {
    log "🔧 Creando clases faltantes..."

    # Crear EmailServicio si no existe
    if [ ! -f "src/main/java/informviva/gest/service/EmailServicio.java" ]; then
        cat > "src/main/java/informviva/gest/service/EmailServicio.java" << 'EOF'
package informviva.gest.service;

import org.springframework.stereotype.Service;

/**
 * Servicio para envío de emails
 */
@Service
public interface EmailServicio {
    void enviarEmail(String destinatario, String asunto, String contenido);
    void enviarEmailConfirmacion(String destinatario, String nombreCliente);
}
EOF
        success "EmailServicio creado"
    fi

    # Crear implementación de EmailServicio
    if [ ! -f "src/main/java/informviva/gest/service/impl/EmailServicioImpl.java" ]; then
        cat > "src/main/java/informviva/gest/service/impl/EmailServicioImpl.java" << 'EOF'
package informviva.gest.service.impl;

import informviva.gest.service.EmailServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailServicioImpl implements EmailServicio {

    private static final Logger logger = LoggerFactory.getLogger(EmailServicioImpl.class);

    @Override
    public void enviarEmail(String destinatario, String asunto, String contenido) {
        // Implementación temporal - configurar con JavaMailSender
        logger.info("Enviando email a: {} con asunto: {}", destinatario, asunto);
    }

    @Override
    public void enviarEmailConfirmacion(String destinatario, String nombreCliente) {
        String asunto = "Confirmación de registro";
        String contenido = "Estimado/a " + nombreCliente + ", su registro ha sido confirmado.";
        enviarEmail(destinatario, asunto, contenido);
    }
}
EOF
        success "EmailServicioImpl creado"
    fi

    # Crear ResultadoValidacionDTO
    if [ ! -f "src/main/java/informviva/gest/dto/ResultadoValidacionDTO.java" ]; then
        cat > "src/main/java/informviva/gest/dto/ResultadoValidacionDTO.java" << 'EOF'
package informviva.gest.dto;

import java.util.List;

public class ResultadoValidacionDTO {
    private boolean valido;
    private String mensaje;
    private List<String> errores;

    public ResultadoValidacionDTO() {}

    public ResultadoValidacionDTO(boolean valido, String mensaje) {
        this.valido = valido;
        this.mensaje = mensaje;
    }

    // Getters y setters
    public boolean isValido() { return valido; }
    public void setValido(boolean valido) { this.valido = valido; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public List<String> getErrores() { return errores; }
    public void setErrores(List<String> errores) { this.errores = errores; }
}
EOF
        success "ResultadoValidacionDTO creado"
    fi
}

# Función para crear directorios faltantes
create_missing_directories() {
    log "🔧 Creando directorios faltantes..."

    mkdir -p src/main/java/informviva/gest/service/importacion/impl
    mkdir -p src/main/java/informviva/gest/service/validacion
    mkdir -p src/main/java/informviva/gest/dto/importacion

    success "Directorios creados"
}

# Función para crear processors de importación faltantes
create_importacion_processors() {
    log "🔧 Creando processors de importación faltantes..."

    # CsvImportacionProcessor
    cat > "src/main/java/informviva/gest/service/importacion/impl/CsvImportacionProcessor.java" << 'EOF'
package informviva.gest.service.importacion.impl;

import informviva.gest.service.importacion.ImportacionProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class CsvImportacionProcessor implements ImportacionProcessor {

    @Override
    public boolean puedeProcesar(String tipoArchivo) {
        return "csv".equalsIgnoreCase(tipoArchivo);
    }

    @Override
    public Object procesar(MultipartFile archivo) {
        // Implementación temporal
        return null;
    }
}
EOF

    # ExcelImportacionProcessor
    cat > "src/main/java/informviva/gest/service/importacion/impl/ExcelImportacionProcessor.java" << 'EOF'
package informviva.gest.service.importacion.impl;

import informviva.gest.service.importacion.ImportacionProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ExcelImportacionProcessor implements ImportacionProcessor {

    @Override
    public boolean puedeProcesar(String tipoArchivo) {
        return "xlsx".equalsIgnoreCase(tipoArchivo) || "xls".equalsIgnoreCase(tipoArchivo);
    }

    @Override
    public Object procesar(MultipartFile archivo) {
        // Implementación temporal
        return null;
    }
}
EOF

    # ImportacionProcessor interface
    cat > "src/main/java/informviva/gest/service/importacion/ImportacionProcessor.java" << 'EOF'
package informviva.gest.service.importacion;

import org.springframework.web.multipart.MultipartFile;

public interface ImportacionProcessor {
    boolean puedeProcesar(String tipoArchivo);
    Object procesar(MultipartFile archivo);
}
EOF

    success "Processors de importación creados"
}

# Función para crear validador de importación
create_importacion_validator() {
    log "🔧 Creando validador de importación..."

    cat > "src/main/java/informviva/gest/service/validacion/ImportacionValidador.java" << 'EOF'
package informviva.gest.service.validacion;

import org.springframework.stereotype.Component;

@Component
public class ImportacionValidador {

    public boolean validarDatos(Object datos) {
        // Implementación temporal
        return true;
    }
}
EOF

    success "ImportacionValidador creado"
}

# Función para agregar constantes faltantes
fix_importacion_constants() {
    log "🔧 Agregando constantes faltantes en ImportacionConstants..."

    # Verificar si el archivo existe
    if [ -f "src/main/java/informviva/gest/util/ImportacionConstants.java" ]; then
        # Agregar constantes faltantes si no existen
        if ! grep -q "PATRON_EMAIL" "src/main/java/informviva/gest/util/ImportacionConstants.java"; then
            cat >> "src/main/java/informviva/gest/util/ImportacionConstants.java" << 'EOF'

    // Patrones de validación
    public static final String PATRON_EMAIL = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String PATRON_RUT = "^[0-9]{7,8}-[0-9Kk]$";
    public static final String PATRON_TELEFONO = "^[+]?[0-9]{8,15}$";
EOF
        fi
        success "Constantes agregadas a ImportacionConstants"
    fi
}

# Función para reparar ProductoServicio
fix_producto_servicio() {
    log "🔧 Reparando ProductoServicio..."

    # Remover método duplicado y código de implementación en interface
    sed -i '/method buscarPorNombre.*already defined/,+10d' src/main/java/informviva/gest/service/ProductoServicio.java
    sed -i '/interface abstract methods cannot have body/,+10d' src/main/java/informviva/gest/service/ProductoServicio.java

    success "ProductoServicio reparado"
}

# Función para reparar ValidacionConstantes
fix_validacion_constantes() {
    log "🔧 Reparando ValidacionConstantes..."

    # Remover duplicación de DATOS_INVALIDOS
    awk '!seen[$0]++' src/main/java/informviva/gest/util/ValidacionConstantes.java > temp_file && mv temp_file src/main/java/informviva/gest/util/ValidacionConstantes.java

    success "ValidacionConstantes reparado"
}

# Función para agregar imports faltantes
fix_missing_imports() {
    log "🔧 Agregando imports faltantes..."

    # Agregar import de ResultadoValidacionModulo donde sea necesario
    find src/main/java -name "*.java" -exec grep -l "ResultadoValidacionModulo" {} \; | while read file; do
        if ! grep -q "import.*ResultadoValidacionModulo" "$file"; then
            sed -i '1i import informviva.gest.dto.ResultadoValidacionModulo;' "$file"
        fi
    done

    success "Imports agregados"
}

# Función principal de reparación
main() {
    log "🚀 Iniciando reparación de errores de compilación..."

    # Verificar que estamos en directorio correcto
    if [ ! -f "pom.xml" ]; then
        error "No se encontró pom.xml. Ejecute desde el directorio raíz del proyecto."
        exit 1
    fi

    # Crear backup
    create_backup

    # Ejecutar reparaciones
    fix_pom_duplications
    fix_test_location
    create_missing_directories
    create_missing_classes
    create_importacion_processors
    create_importacion_validator
    fix_importacion_constants
    fix_producto_servicio
    fix_validacion_constantes
    fix_missing_imports

    log "🔍 Verificando compilación después de reparaciones..."

    # Intentar compilar
    if mvn clean compile -q; then
        success "✅ Compilación exitosa después de reparaciones"
        log "🎉 Todas las reparaciones completadas exitosamente"
    else
        error "❌ Aún hay errores de compilación. Revisar manualmente:"
        mvn clean compile
        warning "⚠️  Para hacer rollback: git reset --hard HEAD~1"
    fi
}

# Manejo de argumentos
case "${1:-}" in
    --dry-run)
        log "Modo DRY RUN - Solo análisis, sin cambios"
        exit 0
        ;;
    --help|-h)
        echo "Uso: $0 [--dry-run] [--help]"
        echo "  --dry-run  Solo mostrar lo que se haría sin ejecutar"
        echo "  --help     Mostrar esta ayuda"
        exit 0
        ;;
    *)
        main
        ;;
esac