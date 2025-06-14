#!/bin/bash

# Script de reparación específica para archivos corruptos
# Corrige los errores "class, interface, enum, or record expected"

set -e

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log() { echo -e "${BLUE}[$(date '+%H:%M:%S')]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; }
success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }

# Función para reparar archivos específicos corruptos
fix_corrupted_files() {
    log "🔧 Reparando archivos corruptos específicos..."

    # 1. Reparar ResultadoValidacionModulo en dto
    local dto_file="src/main/java/informviva/gest/dto/ResultadoValidacionModulo.java"
    if [ -f "$dto_file" ]; then
        log "Reparando $dto_file..."
        cat > "$dto_file" << 'EOF'
package informviva.gest.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO para resultado de validación de módulos
 */
public class ResultadoValidacionModulo {

    private boolean valido;
    private String mensaje;
    private String nombreModulo;
    private LocalDateTime fechaValidacion;
    private List<String> erroresDetectados;
    private List<String> advertencias;
    private Map<String, Object> metadatos;

    // Constructores
    public ResultadoValidacionModulo() {
        this.erroresDetectados = new ArrayList<>();
        this.advertencias = new ArrayList<>();
        this.metadatos = new HashMap<>();
        this.fechaValidacion = LocalDateTime.now();
    }

    public ResultadoValidacionModulo(boolean valido, String mensaje) {
        this();
        this.valido = valido;
        this.mensaje = mensaje;
    }

    // Getters y Setters
    public boolean isValido() { return valido; }
    public void setValido(boolean valido) { this.valido = valido; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getNombreModulo() { return nombreModulo; }
    public void setNombreModulo(String nombreModulo) { this.nombreModulo = nombreModulo; }

    public LocalDateTime getFechaValidacion() { return fechaValidacion; }
    public void setFechaValidacion(LocalDateTime fechaValidacion) { this.fechaValidacion = fechaValidacion; }

    public List<String> getErroresDetectados() { return erroresDetectados; }
    public void setErroresDetectados(List<String> erroresDetectados) { this.erroresDetectados = erroresDetectados; }

    public List<String> getAdvertencias() { return advertencias; }
    public void setAdvertencias(List<String> advertencias) { this.advertencias = advertencias; }

    public Map<String, Object> getMetadatos() { return metadatos; }
    public void setMetadatos(Map<String, Object> metadatos) { this.metadatos = metadatos; }

    // Métodos de utilidad
    public void agregarError(String error) {
        if (this.erroresDetectados == null) {
            this.erroresDetectados = new ArrayList<>();
        }
        this.erroresDetectados.add(error);
        this.valido = false;
    }

    public void agregarAdvertencia(String advertencia) {
        if (this.advertencias == null) {
            this.advertencias = new ArrayList<>();
        }
        this.advertencias.add(advertencia);
    }

    public boolean tieneErrores() {
        return erroresDetectados != null && !erroresDetectados.isEmpty();
    }
}
EOF
        success "ResultadoValidacionModulo DTO reparado"
    fi

    # 2. Eliminar duplicado en service (debe estar solo en dto)
    local service_file="src/main/java/informviva/gest/service/ResultadoValidacionModulo.java"
    if [ -f "$service_file" ]; then
        log "Eliminando duplicado incorrecto en service..."
        rm "$service_file"
        success "Duplicado eliminado de service"
    fi

    # 3. Reparar ImportacionConstants
    local constants_file="src/main/java/informviva/gest/util/ImportacionConstants.java"
    if [ -f "$constants_file" ]; then
        log "Reparando ImportacionConstants..."
        # Verificar si el archivo está corrupto
        if ! grep -q "^package" "$constants_file"; then
            # Recrear archivo completo
            cat > "$constants_file" << 'EOF'
package informviva.gest.util;

/**
 * Constantes para importación de datos
 */
public class ImportacionConstants {

    // Tipos de archivo soportados
    public static final String TIPO_CSV = "csv";
    public static final String TIPO_EXCEL = "xlsx";
    public static final String TIPO_XLS = "xls";

    // Tamaños máximos
    public static final long TAMAÑO_MAXIMO_ARCHIVO = 10 * 1024 * 1024; // 10MB
    public static final int FILAS_MAXIMAS = 10000;

    // Mensajes
    public static final String MENSAJE_ARCHIVO_VACIO = "El archivo está vacío";
    public static final String MENSAJE_FORMATO_INVALIDO = "Formato de archivo no válido";
    public static final String MENSAJE_TAMAÑO_EXCEDIDO = "El archivo excede el tamaño máximo permitido";

    // Patrones de validación
    public static final String PATRON_EMAIL = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String PATRON_RUT = "^[0-9]{7,8}-[0-9Kk]$";
    public static final String PATRON_TELEFONO = "^[+]?[0-9]{8,15}$";

    // Estados
    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_PROCESANDO = "PROCESANDO";
    public static final String ESTADO_COMPLETADO = "COMPLETADO";
    public static final String ESTADO_ERROR = "ERROR";

    private ImportacionConstants() {
        // Utility class - no instantiation
    }
}
EOF
        else
            # Solo agregar las constantes faltantes al final
            if ! grep -q "PATRON_EMAIL" "$constants_file"; then
                cat >> "$constants_file" << 'EOF'

    // Patrones de validación
    public static final String PATRON_EMAIL = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String PATRON_RUT = "^[0-9]{7,8}-[0-9Kk]$";
    public static final String PATRON_TELEFONO = "^[+]?[0-9]{8,15}$";
}
EOF
            fi
        fi
        success "ImportacionConstants reparado"
    fi

    # 4. Reparar ValidacionConstantes
    local validacion_file="src/main/java/informviva/gest/util/ValidacionConstantes.java"
    if [ -f "$validacion_file" ]; then
        log "Reparando ValidacionConstantes..."

        # Hacer backup
        cp "$validacion_file" "${validacion_file}.backup"

        # Verificar si está corrupto
        if ! grep -q "^package" "$validacion_file"; then
            # Recrear archivo
            cat > "$validacion_file" << 'EOF'
package informviva.gest.util;

/**
 * Constantes para validación de datos
 */
public class ValidacionConstantes {

    // Mensajes de validación
    public static final String CAMPO_REQUERIDO = "Este campo es requerido";
    public static final String FORMATO_INVALIDO = "El formato no es válido";
    public static final String DATOS_INVALIDOS = "Los datos ingresados no son válidos";
    public static final String LONGITUD_INVALIDA = "La longitud no es válida";

    // Validaciones específicas
    public static final String EMAIL_INVALIDO = "El email no tiene un formato válido";
    public static final String RUT_INVALIDO = "El RUT no es válido";
    public static final String TELEFONO_INVALIDO = "El teléfono no es válido";
    public static final String FECHA_INVALIDA = "La fecha no es válida";

    // Límites
    public static final int NOMBRE_MIN_LENGTH = 2;
    public static final int NOMBRE_MAX_LENGTH = 50;
    public static final int EMAIL_MAX_LENGTH = 100;
    public static final int TELEFONO_MAX_LENGTH = 15;

    private ValidacionConstantes() {
        // Utility class - no instantiation
    }
}
EOF
        else
            # Remover líneas duplicadas
            awk '!seen[$0]++' "$validacion_file" > "${validacion_file}.tmp" && mv "${validacion_file}.tmp" "$validacion_file"
        fi
        success "ValidacionConstantes reparado"
    fi

    # 5. Reparar ValidacionDatosServicio
    local service_validacion="src/main/java/informviva/gest/service/ValidacionDatosServicio.java"
    if [ -f "$service_validacion" ]; then
        log "Reparando ValidacionDatosServicio..."

        if ! grep -q "^package" "$service_validacion"; then
            cat > "$service_validacion" << 'EOF'
package informviva.gest.service;

import informviva.gest.dto.ResultadoValidacionModulo;
import informviva.gest.model.*;

import java.util.List;

/**
 * Servicio para validación de datos
 */
public interface ValidacionDatosServicio {

    /**
     * Valida un cliente
     */
    ResultadoValidacionModulo validarCliente(Cliente cliente);

    /**
     * Valida un producto
     */
    ResultadoValidacionModulo validarProducto(Producto producto);

    /**
     * Valida una venta
     */
    ResultadoValidacionModulo validarVenta(Venta venta);

    /**
     * Valida una lista de datos de importación
     */
    ResultadoValidacionModulo validarDatosImportacion(List<Object> datos);

    /**
     * Valida formato de email
     */
    boolean validarEmail(String email);

    /**
     * Valida formato de RUT
     */
    boolean validarRut(String rut);

    /**
     * Valida formato de teléfono
     */
    boolean validarTelefono(String telefono);
}
EOF
        fi
        success "ValidacionDatosServicio reparado"
    fi
}

# Función para limpiar POM manualmente
fix_pom_duplications_manual() {
    log "🔧 Reparando duplicaciones en POM manualmente..."

    # Backup del POM
    cp pom.xml pom.xml.manual_backup

    # Crear versión limpia del POM
    python3 -c "
import xml.etree.ElementTree as ET
import sys

# Leer el POM
try:
    tree = ET.parse('pom.xml')
    root = tree.getroot()

    # Namespace
    ns = {'mvn': 'http://maven.apache.org/POM/4.0.0'}

    # Remover dependencias duplicadas
    dependencies = root.find('.//mvn:dependencies', ns)
    if dependencies is not None:
        seen_deps = set()
        deps_to_remove = []

        for dep in dependencies.findall('mvn:dependency', ns):
            group_id = dep.find('mvn:groupId', ns)
            artifact_id = dep.find('mvn:artifactId', ns)

            if group_id is not None and artifact_id is not None:
                key = f'{group_id.text}:{artifact_id.text}'
                if key in seen_deps:
                    deps_to_remove.append(dep)
                else:
                    seen_deps.add(key)

        for dep in deps_to_remove:
            dependencies.remove(dep)

    # Escribir POM limpio
    tree.write('pom.xml', encoding='utf-8', xml_declaration=True)
    print('POM limpiado exitosamente')

except Exception as e:
    print(f'Error: {e}', file=sys.stderr)
    sys.exit(1)
" 2>/dev/null || {
        # Fallback: usar sed más simple
        warning "Python no disponible, usando sed básico..."

        # Crear una copia temporal
        cp pom.xml pom_temp.xml

        # Remover líneas específicas de duplicados conocidos
        sed -i '/<!-- Segunda declaración de poi-ooxml -->/,+4d' pom.xml 2>/dev/null || true
        sed -i '/<!-- Segunda declaración de jackson-datatype-jsr310 -->/,+4d' pom.xml 2>/dev/null || true

        success "POM reparado con sed"
    }

    success "Duplicaciones del POM reparadas"
}

# Función principal
main() {
    log "🚀 Iniciando reparación específica de archivos corruptos..."

    # Verificar directorio
    if [ ! -f "pom.xml" ]; then
        error "Ejecute desde el directorio raíz del proyecto"
        exit 1
    fi

    # Crear backup adicional
    git add . 2>/dev/null || true
    git commit -m "BACKUP: Antes de reparación específica - $(date)" 2>/dev/null || warning "No hay cambios para commitear"

    # Ejecutar reparaciones específicas
    fix_corrupted_files
    fix_pom_duplications_manual

    log "🔍 Verificando compilación después de reparación específica..."

    # Intentar compilar
    if mvn clean compile -q 2>/dev/null; then
        success "✅ ¡Compilación exitosa después de reparación específica!"
        log "🎉 Todos los archivos corruptos fueron reparados"
    else
        error "❌ Aún hay errores, mostrando detalles:"
        mvn clean compile 2>&1 | head -20
        warning "⚠️  Puede requerir intervención manual adicional"
    fi

    log "📝 Archivos reparados:"
    log "   - ResultadoValidacionModulo.java (DTO)"
    log "   - ImportacionConstants.java"
    log "   - ValidacionConstantes.java"
    log "   - ValidacionDatosServicio.java"
    log "   - pom.xml (duplicaciones)"
}

# Ejecutar
main "$@"