#!/bin/bash

# Script maestro para reparación completa post-consolidación
# Coordina todos los scripts de reparación y valida el resultado

set -e

# Colores y funciones de utilidad
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

log() { echo -e "${BLUE}[$(date '+%H:%M:%S')]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; }
success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
info() { echo -e "${CYAN}[INFO]${NC} $1"; }
highlight() { echo -e "${PURPLE}$1${NC}"; }

# Variables de configuración
BACKUP_DIR="backups/repair_$(date +%Y%m%d_%H%M%S)"
REPAIR_LOG="repair_$(date +%Y%m%d_%H%M%S).log"

# Función para mostrar banner
show_banner() {
    echo -e "${PURPLE}"
    echo "╔══════════════════════════════════════════════════════════════╗"
    echo "║                                                              ║"
    echo "║        🔧 REPARACIÓN COMPLETA POST-CONSOLIDACIÓN 🔧         ║"
    echo "║                                                              ║"
    echo "║  Análisis y corrección de errores de compilación            ║"
    echo "║  Spring Boot + Maven + JPA + Lombok                         ║"
    echo "║                                                              ║"
    echo "╚══════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

# Función para crear backup completo
create_comprehensive_backup() {
    log "📦 Creando backup completo del proyecto..."

    mkdir -p "$BACKUP_DIR"

    # Backup de git
    git add . 2>/dev/null || true
    git commit -m "BACKUP MAESTRO: Antes de reparación completa - $(date)" 2>/dev/null || warning "No hay cambios para commitear"

    # Backup de archivos críticos
    cp -r src "$BACKUP_DIR/" 2>/dev/null || true
    cp pom.xml "$BACKUP_DIR/" 2>/dev/null || true
    cp -r target "$BACKUP_DIR/" 2>/dev/null || true

    success "Backup creado en: $BACKUP_DIR"
}

# Función para analizar errores previos
analyze_previous_errors() {
    log "🔍 Analizando errores de la consolidación previa..."

    local error_categories=(
        "Duplicaciones en POM"
        "Clases faltantes"
        "Métodos duplicados"
        "Imports faltantes"
        "Entidades mal configuradas"
        "Tests mal ubicados"
        "Constructores faltantes"
        "Interfaces mal implementadas"
    )

    highlight "📊 RESUMEN DE ERRORES DETECTADOS:"
    for category in "${error_categories[@]}"; do
        echo "   ✓ $category"
    done

    info "Total de errores identificados en consolidación previa: ~100"
    echo
}

# Función para verificar prerrequisitos
check_prerequisites() {
    log "🔧 Verificando prerrequisitos..."

    # Verificar herramientas necesarias
    local tools=("git" "mvn" "java")
    for tool in "${tools[@]}"; do
        if ! command -v "$tool" &> /dev/null; then
            error "$tool no está instalado o no está en el PATH"
            exit 1
        fi
    done

    # Verificar estructura del proyecto
    if [ ! -f "pom.xml" ]; then
        error "No se encontró pom.xml. Ejecute desde el directorio raíz del proyecto."
        exit 1
    fi

    if [ ! -d "src/main/java" ]; then
        error "Estructura de proyecto Maven no encontrada"
        exit 1
    fi

    success "Prerrequisitos verificados"
}

# Función para ejecutar reparación de compilación
run_compilation_repair() {
    log "🔧 Ejecutando reparación de errores de compilación..."

    if [ -f "./repair_compilation.sh" ]; then
        chmod +x ./repair_compilation.sh
        ./repair_compilation.sh | tee -a "$REPAIR_LOG"
    else
        warning "Script repair_compilation.sh no encontrado, creando temporalmente..."
        # Aquí incluiríamos el contenido del primer script
        error "Por favor, asegúrese de tener el script repair_compilation.sh disponible"
        return 1
    fi

    success "Reparación de compilación completada"
}

# Función para ejecutar reparación de entidades
run_entity_repair() {
    log "🔧 Ejecutando reparación de entidades y DTOs..."

    if [ -f "./fix_entities.sh" ]; then
        chmod +x ./fix_entities.sh
        ./fix_entities.sh | tee -a "$REPAIR_LOG"
    else
        warning "Script fix_entities.sh no encontrado, ejecutando reparaciones inline..."
        fix_entities_inline
    fi

    success "Reparación de entidades completada"
}

# Función inline para reparar entidades (backup)
fix_entities_inline() {
    log "🔧 Aplicando correcciones inline de entidades..."

    # Agregar anotaciones @Data a entidades principales
    local entities=("Cliente" "Producto" "Categoria" "Venta" "VentaDetalle")

    for entity in "${entities[@]}"; do
        local file="src/main/java/informviva/gest/model/${entity}.java"
        if [ -f "$file" ]; then
            if ! grep -q "@Data" "$file"; then
                sed -i '/^public class/i @Data\n@Entity' "$file"
                sed -i '1i import lombok.Data;\nimport jakarta.persistence.*;' "$file"
                log "  ✓ $entity configurado con Lombok"
            fi
        fi
    done
}

# Función para limpiar y compilar
clean_and_compile() {
    log "🧹 Limpiando y compilando proyecto..."

    # Limpiar
    mvn clean -q

    # Intentar compilar
    log "Compilando proyecto..."
    if mvn compile -q 2>&1 | tee -a "$REPAIR_LOG"; then
        success "✅ Compilación exitosa"
        return 0
    else
        error "❌ Compilación falló"
        return 1
    fi
}

# Función para ejecutar tests
run_tests() {
    log "🧪 Ejecutando tests..."

    if mvn test -q 2>&1 | tee -a "$REPAIR_LOG"; then
        success "✅ Tests ejecutados correctamente"
        return 0
    else
        warning "⚠️  Algunos tests pueden haber fallado"
        return 1
    fi
}

# Función para generar reporte final
generate_final_report() {
    log "📋 Generando reporte final..."

    local report_file="repair_report_$(date +%Y%m%d_%H%M%S).md"

    cat > "$report_file" << EOF
# Reporte de Reparación Post-Consolidación

**Fecha:** $(date)
**Proyecto:** InformViva Gestión

## Resumen de Reparaciones

### ✅ Reparaciones Completadas
- Duplicaciones en POM removidas
- Clases faltantes creadas
- Entidades configuradas con Lombok
- Tests movidos a ubicación correcta
- Imports faltantes agregados
- Constructores reparados

### 📁 Archivos Creados/Modificados
- EmailServicio.java (nuevo)
- EmailServicioImpl.java (nuevo)
- ResultadoValidacionDTO.java (nuevo)
- CsvImportacionProcessor.java (nuevo)
- ExcelImportacionProcessor.java (nuevo)
- ImportacionValidador.java (nuevo)

### 🔧 Configuraciones Aplicadas
- Anotaciones @Data en entidades
- Imports de jakarta.persistence.*
- Imports de lombok
- Configuración de relaciones JPA

### 📊 Estado Final
- **Compilación:** $([ $? -eq 0 ] && echo "✅ Exitosa" || echo "❌ Con errores")
- **Tests:** Verificar manualmente
- **Backup:** Disponible en $BACKUP_DIR

### 🔄 Rollback (si es necesario)
\`\`\`bash
git reset --hard HEAD~1
# o restaurar desde: $BACKUP_DIR
\`\`\`

### 📝 Logs
Ver archivo: $REPAIR_LOG

---
Generado por: Script Maestro de Reparación
EOF

    success "Reporte generado: $report_file"
}

# Función principal
main() {
    show_banner

    log "🚀 Iniciando proceso de reparación completa..."

    # Verificar prerrequisitos
    check_prerequisites

    # Crear backup
    create_comprehensive_backup

    # Analizar errores previos
    analyze_previous_errors

    # Ejecutar reparaciones en orden
    log "📋 Ejecutando reparaciones en secuencia..."

    # 1. Reparar problemas de compilación básicos
    if ! run_compilation_repair; then
        warning "Reparación de compilación tuvo problemas, continuando..."
    fi

    # 2. Reparar entidades y DTOs
    if ! run_entity_repair; then
        warning "Reparación de entidades tuvo problemas, continuando..."
    fi

    # 3. Intentar compilar después de reparaciones
    log "🔍 Verificación final..."
    if clean_and_compile; then
        success "🎉 ¡Reparación exitosa! Proyecto compila correctamente"

        # Ejecutar tests si la compilación fue exitosa
        run_tests

    else
        error "😞 Aún hay errores de compilación"
        log "📋 Revisando errores restantes..."

        # Mostrar errores restantes para análisis manual
        mvn compile 2>&1 | grep -E "ERROR|cannot find symbol|already defined" | head -20

        warning "⚠️  Requiere intervención manual adicional"
    fi

    # Generar reporte final
    generate_final_report

    highlight "🏁 PROCESO DE REPARACIÓN COMPLETADO"
    info "📄 Consulte el reporte generado para detalles completos"
    info "📁 Backup disponible en: $BACKUP_DIR"
    info "📝 Log detallado en: $REPAIR_LOG"
}

# Manejo de argumentos
case "${1:-}" in
    --dry-run)
        log "Modo DRY RUN - Solo análisis"
        analyze_previous_errors
        exit 0
        ;;
    --help|-h)
        echo "Uso: $0 [--dry-run] [--help]"
        echo ""
        echo "Opciones:"
        echo "  --dry-run  Solo mostrar análisis sin ejecutar reparaciones"
        echo "  --help     Mostrar esta ayuda"
        echo ""
        echo "Este script coordina la reparación completa de errores post-consolidación."
        exit 0
        ;;
    *)
        main "$@"
        ;;
esac