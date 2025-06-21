#!/bin/bash

# Script de ANÁLISIS de errores de compilación (SOLO LECTURA)
# Este script NO modifica archivos, solo reporta qué hay que hacer
# Autor: Asistente de Programación

set -e
PROJECT_ROOT="."
SRC_MAIN="src/main/java/informviva/gest"

echo "🔍 ANÁLISIS DE ERRORES DE COMPILACIÓN (Solo Lectura)"
echo "=================================================="
echo "Este script NO modifica archivos, solo analiza y reporta."
echo ""

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Contadores
MISSING_FILES=0
MISSING_METHODS=0
TYPE_ERRORS=0
INTERFACE_ERRORS=0

echo "📁 Verificando estructura del proyecto..."

# Función para verificar archivo
check_file_exists() {
    local file=$1
    local description=$2
    if [ -f "$file" ]; then
        echo -e "${GREEN}✅${NC} $description: $file"
        return 0
    else
        echo -e "${RED}❌${NC} $description: $file ${RED}[FALTA]${NC}"
        ((MISSING_FILES++))
        return 1
    fi
}

# Función para verificar método en archivo
check_method_in_file() {
    local file=$1
    local method=$2
    local description=$3
    if [ -f "$file" ]; then
        if grep -q "$method" "$file"; then
            echo -e "${GREEN}✅${NC} $description: $method"
            return 0
        else
            echo -e "${YELLOW}⚠️${NC} $description: $method ${YELLOW}[FALTA]${NC}"
            ((MISSING_METHODS++))
            return 1
        fi
    else
        echo -e "${RED}❌${NC} Archivo no existe para verificar: $file"
        return 1
    fi
}

# Función para analizar errores específicos
analyze_compilation_errors() {
    echo ""
    echo "🔬 ANÁLISIS DETALLADO DE ERRORES:"
    echo "================================"

    # 1. Verificar enum FormatoExportacion
    echo ""
    echo "${BLUE}1. ENUM FormatoExportacion${NC}"
    check_file_exists "$SRC_MAIN/enums/FormatoExportacion.java" "Enum FormatoExportacion"

    # 2. Verificar repositorios
    echo ""
    echo "${BLUE}2. REPOSITORIOS${NC}"
    check_file_exists "$SRC_MAIN/repository/ClienteRepositorio.java" "ClienteRepositorio"
    check_file_exists "$SRC_MAIN/repository/VentaRepositorio.java" "VentaRepositorio"

    if [ -f "$SRC_MAIN/repository/ClienteRepositorio.java" ]; then
        check_method_in_file "$SRC_MAIN/repository/ClienteRepositorio.java" "findByTelefono" "ClienteRepositorio.findByTelefono"
        check_method_in_file "$SRC_MAIN/repository/ClienteRepositorio.java" "findByNombreContainingOrEmailContainingIgnoreCase" "ClienteRepositorio.findByNombreContainingOrEmailContainingIgnoreCase"
    fi

    if [ -f "$SRC_MAIN/repository/VentaRepositorio.java" ]; then
        check_method_in_file "$SRC_MAIN/repository/VentaRepositorio.java" "findByClienteId" "VentaRepositorio.findByClienteId"
    fi

    # 3. Verificar servicios
    echo ""
    echo "${BLUE}3. INTERFACES DE SERVICIOS${NC}"
    check_file_exists "$SRC_MAIN/service/ClienteServicio.java" "ClienteServicio"
    check_file_exists "$SRC_MAIN/service/VentaServicio.java" "VentaServicio"
    check_file_exists "$SRC_MAIN/service/ValidacionDatosServicio.java" "ValidacionDatosServicio"
    check_file_exists "$SRC_MAIN/service/ImportacionServicio.java" "ImportacionServicio"
    check_file_exists "$SRC_MAIN/service/ExportacionHistorialServicio.java" "ExportacionHistorialServicio"

    if [ -f "$SRC_MAIN/service/ClienteServicio.java" ]; then
        check_method_in_file "$SRC_MAIN/service/ClienteServicio.java" "List<Cliente> buscarPorTelefono" "ClienteServicio.buscarPorTelefono (tipo List)"
        check_method_in_file "$SRC_MAIN/service/ClienteServicio.java" "List<Cliente> listar" "ClienteServicio.listar"
    fi

    if [ -f "$SRC_MAIN/service/VentaServicio.java" ]; then
        check_method_in_file "$SRC_MAIN/service/VentaServicio.java" "List<Venta> listar" "VentaServicio.listar"
        check_method_in_file "$SRC_MAIN/service/VentaServicio.java" "List<Venta> obtenerTodas" "VentaServicio.obtenerTodas"
    fi

    # 4. Verificar implementaciones
    echo ""
    echo "${BLUE}4. IMPLEMENTACIONES DE SERVICIOS${NC}"
    check_file_exists "$SRC_MAIN/service/impl/ClienteServicioImpl.java" "ClienteServicioImpl"
    check_file_exists "$SRC_MAIN/service/impl/ValidacionDatosServicioImpl.java" "ValidacionDatosServicioImpl"
    check_file_exists "$SRC_MAIN/service/impl/ImportacionServicioImpl.java" "ImportacionServicioImpl"

    # 5. Verificar entidades
    echo ""
    echo "${BLUE}5. ENTIDADES${NC}"
    check_file_exists "$SRC_MAIN/model/Producto.java" "Producto"
    check_file_exists "$SRC_MAIN/model/Usuario.java" "Usuario"

    if [ -f "$SRC_MAIN/model/Producto.java" ]; then
        check_method_in_file "$SRC_MAIN/model/Producto.java" "isActivo" "Producto.isActivo"
    fi

    if [ -f "$SRC_MAIN/model/Usuario.java" ]; then
        check_method_in_file "$SRC_MAIN/model/Usuario.java" "isActivo" "Usuario.isActivo"
    fi

    # 6. Verificar DTOs
    echo ""
    echo "${BLUE}6. DTOs${NC}"
    check_file_exists "$SRC_MAIN/dto/VentaDTO.java" "VentaDTO"
    check_file_exists "$SRC_MAIN/dto/VentaDetalleDTO.java" "VentaDetalleDTO"
}

# Función para analizar tipos incorrectos en archivos
analyze_type_issues() {
    echo ""
    echo "${BLUE}7. ANÁLISIS DE TIPOS INCORRECTOS${NC}"

    # Buscar LocalDate en lugar de LocalDateTime
    echo "Buscando problemas LocalDate vs LocalDateTime..."
    find "$SRC_MAIN" -name "*.java" -type f | while read -r file; do
        if [ -f "$file" ]; then
            # Buscar setFecha con LocalDate
            if grep -n "setFecha.*LocalDate" "$file" 2>/dev/null; then
                echo -e "${YELLOW}⚠️${NC} Posible problema LocalDate en: $file"
                ((TYPE_ERRORS++))
            fi

            # Buscar BigDecimal vs Double
            if grep -n "BigDecimal.*Double\|Double.*BigDecimal" "$file" 2>/dev/null; then
                echo -e "${YELLOW}⚠️${NC} Posible problema BigDecimal/Double en: $file"
                ((TYPE_ERRORS++))
            fi
        fi
    done

    # Buscar Optional mal usado
    echo "Buscando problemas con Optional..."
    find "$SRC_MAIN" -name "*.java" -type f | while read -r file; do
        if [ -f "$file" ]; then
            if grep -n "Optional.*cannot be converted" "$file" 2>/dev/null; then
                echo -e "${YELLOW}⚠️${NC} Posible problema Optional en: $file"
                ((TYPE_ERRORS++))
            fi
        fi
    done
}

# Función para generar reporte de errores prioritarios
generate_priority_report() {
    echo ""
    echo "📊 REPORTE DE PRIORIDADES:"
    echo "========================="

    echo ""
    echo "${RED}🔥 ERRORES CRÍTICOS (Impiden compilación):${NC}"
    echo "1. Crear enum FormatoExportacion"
    echo "2. Agregar métodos faltantes en repositorios"
    echo "3. Corregir tipos de retorno en servicios"

    echo ""
    echo "${YELLOW}⚠️ ERRORES IMPORTANTES:${NC}"
    echo "1. Implementar métodos faltantes en servicios"
    echo "2. Corregir tipos LocalDate vs LocalDateTime"
    echo "3. Agregar métodos isActivo() en entidades"

    echo ""
    echo "${BLUE}📝 SUGERENCIAS:${NC}"
    echo "1. Hacer un commit de Git antes de cualquier cambio"
    echo "2. Corregir errores uno por uno, probando compilación"
    echo "3. Empezar por los archivos más simples"
}

# Función para generar comandos específicos
generate_specific_commands() {
    echo ""
    echo "🛠️ COMANDOS ESPECÍFICOS PARA CORRECCIÓN:"
    echo "======================================="

    echo ""
    echo "${GREEN}PASO 1: Backup y Git${NC}"
    echo "git add ."
    echo "git commit -m 'Backup antes de correcciones de compilación'"

    echo ""
    echo "${GREEN}PASO 2: Crear enum FormatoExportacion${NC}"
    echo "mkdir -p $SRC_MAIN/enums"
    echo "# Crear manualmente el archivo FormatoExportacion.java"

    echo ""
    echo "${GREEN}PASO 3: Probar compilación incremental${NC}"
    echo "mvn compile 2>&1 | head -20  # Ver primeros 20 errores"

    echo ""
    echo "${GREEN}PASO 4: Corregir archivo por archivo${NC}"
    echo "# Empezar por ClienteRepositorio.java"
    echo "# Luego VentaRepositorio.java"
    echo "# Finalmente los servicios"
}

# Ejecutar análisis
echo "🚀 Iniciando análisis..."
analyze_compilation_errors
analyze_type_issues
generate_priority_report
generate_specific_commands

# Resumen final
echo ""
echo "📈 RESUMEN DEL ANÁLISIS:"
echo "======================="
echo -e "Archivos faltantes: ${RED}$MISSING_FILES${NC}"
echo -e "Métodos faltantes: ${YELLOW}$MISSING_METHODS${NC}"
echo -e "Problemas de tipos: ${YELLOW}$TYPE_ERRORS${NC}"

echo ""
echo "✅ ANÁLISIS COMPLETADO SIN MODIFICAR ARCHIVOS"
echo ""
echo "💡 RECOMENDACIÓN:"
echo "1. Revisar el reporte anterior"
echo "2. Hacer backup con Git"
echo "3. Corregir errores manualmente uno por uno"
echo "4. Probar compilación después de cada cambio"
echo ""
echo "⚠️  NO ejecutes scripts de modificación masiva sin probar antes"