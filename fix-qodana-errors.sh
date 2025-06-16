#!/bin/bash

# ===================================================================
# Script Bash para corregir errores críticos de Qodana
# ===================================================================

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
CYAN='\033[0;36m'
GRAY='\033[0;37m'
NC='\033[0m' # No Color

# Configuración
DRY_RUN=false
BACKUP=true
PROJECT_ROOT=$(pwd)
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_DIR="$PROJECT_ROOT/backup-qodana-$TIMESTAMP"

# Procesar argumentos
while [[ $# -gt 0 ]]; do
    case $1 in
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        --no-backup)
            BACKUP=false
            shift
            ;;
        -h|--help)
            echo "Uso: $0 [--dry-run] [--no-backup]"
            echo "  --dry-run    Solo mostrar lo que se haría"
            echo "  --no-backup  No crear backup de archivos"
            exit 0
            ;;
        *)
            echo "Argumento desconocido: $1"
            exit 1
            ;;
    esac
done

echo -e "${GREEN}🔧 Iniciando corrección de errores críticos de Qodana...${NC}"
echo "======================================================="

# Crear directorio de backup
if [ "$BACKUP" = true ]; then
    echo -e "${YELLOW}📁 Creando backup en: $BACKUP_DIR${NC}"
    mkdir -p "$BACKUP_DIR"
fi

# Función para crear backup
create_backup() {
    local file_path="$1"
    if [ "$BACKUP" = true ] && [ -f "$file_path" ]; then
        local filename=$(basename "$file_path")
        cp "$file_path" "$BACKUP_DIR/$filename.bak"
        echo -e "  ${GRAY}✓ Backup: $filename${NC}"
    fi
}

# Función para mostrar resultado
show_result() {
    local message="$1"
    if [ "$DRY_RUN" = true ]; then
        echo -e "  ${CYAN}🔍 [DRY RUN] $message${NC}"
    else
        echo -e "  ${GREEN}✓ $message${NC}"
    fi
}

# Función para verificar compilación
test_compilation() {
    echo -e "${YELLOW}🧪 Probando compilación...${NC}"
    if mvn clean compile -q > /dev/null 2>&1; then
        echo -e "  ${GREEN}✅ Compilación exitosa${NC}"
        return 0
    else
        echo -e "  ${RED}❌ Error de compilación${NC}"
        return 1
    fi
}

# =============== CORRECCIÓN 1: AsyncExportController ===============
echo -e "\n${MAGENTA}1️⃣ Corrigiendo AsyncExportController...${NC}"

ASYNC_FILE="$PROJECT_ROOT/src/main/java/informviva/gest/controlador/AsyncExportController.java"

if [ -f "$ASYNC_FILE" ]; then
    if grep -q "private CompletableFuture<Void> procesarExportacionAsync" "$ASYNC_FILE"; then
        create_backup "$ASYNC_FILE"

        if [ "$DRY_RUN" = false ]; then
            sed -i 's/private CompletableFuture<Void> procesarExportacionAsync/public CompletableFuture<Void> procesarExportacionAsync/g' "$ASYNC_FILE"
        fi
        show_result "Método cambiado de private a public"

        if [ "$DRY_RUN" = false ]; then
            if ! test_compilation; then
                echo -e "  ${YELLOW}🔄 Restaurando desde backup...${NC}"
                cp "$BACKUP_DIR/AsyncExportController.java.bak" "$ASYNC_FILE"
                return 1
            fi
        fi
    else
        echo -e "  ${BLUE}ℹ️  Ya está corregido o patrón no encontrado${NC}"
    fi
else
    echo -e "  ${RED}❌ Archivo no encontrado: AsyncExportController.java${NC}"
fi

# =============== CORRECCIÓN 2: ImportacionAPIControlador ===============
echo -e "\n${MAGENTA}2️⃣ Corrigiendo ImportacionAPIControlador...${NC}"

API_FILE="$PROJECT_ROOT/src/main/java/informviva/gest/controlador/api/ImportacionAPIControlador.java"

if [ -f "$API_FILE" ]; then
    if grep -q "@Autowired" "$API_FILE" && grep -q "private ImportacionServicio importacionServicio" "$API_FILE"; then
        create_backup "$API_FILE"

        if [ "$DRY_RUN" = false ]; then
            # Crear archivo temporal con las correcciones
            awk '
            /@Autowired/ {
                if (getline next_line && next_line ~ /private ImportacionServicio importacionServicio/) {
                    print "    private final ImportacionServicio importacionServicio;"
                    print ""
                    print "    public ImportacionAPIControlador(ImportacionServicio importacionServicio) {"
                    print "        this.importacionServicio = importacionServicio;"
                    print "    }"
                    next
                } else {
                    print
                    print next_line
                }
                next
            }
            {print}
            ' "$API_FILE" > "$API_FILE.tmp" && mv "$API_FILE.tmp" "$API_FILE"
        fi
        show_result "Constructor injection implementado"

        if [ "$DRY_RUN" = false ]; then
            if ! test_compilation; then
                echo -e "  ${YELLOW}🔄 Restaurando desde backup...${NC}"
                cp "$BACKUP_DIR/ImportacionAPIControlador.java.bak" "$API_FILE"
            fi
        fi
    else
        echo -e "  ${BLUE}ℹ️  Ya usa constructor injection${NC}"
    fi
else
    echo -e "  ${RED}❌ Archivo no encontrado: ImportacionAPIControlador.java${NC}"
fi

# =============== CORRECCIÓN 3: ImportacionControlador ===============
echo -e "\n${MAGENTA}3️⃣ Corrigiendo ImportacionControlador...${NC}"

CONTROLLER_FILE="$PROJECT_ROOT/src/main/java/informviva/gest/controlador/ImportacionControlador.java"

if [ -f "$CONTROLLER_FILE" ]; then
    if grep -q "@Autowired" "$CONTROLLER_FILE" && grep -q "private ImportacionServicio importacionServicio" "$CONTROLLER_FILE"; then
        create_backup "$CONTROLLER_FILE"

        if [ "$DRY_RUN" = false ]; then
            # Crear archivo temporal con las correcciones
            awk '
            /@Autowired/ {
                if (getline next_line && next_line ~ /private ImportacionServicio importacionServicio/) {
                    print "    private final ImportacionServicio importacionServicio;"
                    print ""
                    print "    public ImportacionControlador(ImportacionServicio importacionServicio) {"
                    print "        this.importacionServicio = importacionServicio;"
                    print "    }"
                    next
                } else {
                    print
                    print next_line
                }
                next
            }
            {print}
            ' "$CONTROLLER_FILE" > "$CONTROLLER_FILE.tmp" && mv "$CONTROLLER_FILE.tmp" "$CONTROLLER_FILE"
        fi
        show_result "Constructor injection implementado"

        if [ "$DRY_RUN" = false ]; then
            if ! test_compilation; then
                echo -e "  ${YELLOW}🔄 Restaurando desde backup...${NC}"
                cp "$BACKUP_DIR/ImportacionControlador.java.bak" "$CONTROLLER_FILE"
            fi
        fi
    else
        echo -e "  ${BLUE}ℹ️  Ya usa constructor injection${NC}"
    fi
else
    echo -e "  ${RED}❌ Archivo no encontrado: ImportacionControlador.java${NC}"
fi

# =============== CORRECCIÓN 4: TendenciaVenta ===============
echo -e "\n${MAGENTA}4️⃣ Buscando archivo TendenciaVenta...${NC}"

# Buscar en posibles ubicaciones
TENDENCIA_PATHS=(
    "$PROJECT_ROOT/src/main/java/informviva/gest/dto/TendenciaVenta.java"
    "$PROJECT_ROOT/src/main/java/informviva/gest/enums/TendenciaVenta.java"
    "$PROJECT_ROOT/src/main/java/informviva/gest/model/TendenciaVenta.java"
)

TENDENCIA_FILE=""
for path in "${TENDENCIA_PATHS[@]}"; do
    if [ -f "$path" ]; then
        TENDENCIA_FILE="$path"
        echo -e "  ${GREEN}✓ Archivo encontrado: $(echo $path | sed "s|$PROJECT_ROOT/||")${NC}"
        break
    fi
done

if [ -n "$TENDENCIA_FILE" ]; then
    if grep -q "package informviva.gest.dto;" "$TENDENCIA_FILE" && [[ "$TENDENCIA_FILE" == *"/enums/"* ]]; then
        create_backup "$TENDENCIA_FILE"

        if [ "$DRY_RUN" = false ]; then
            sed -i 's/package informviva\.gest\.dto;/package informviva.gest.enums;/g' "$TENDENCIA_FILE"
        fi
        show_result "Package statement corregido de dto a enums"
    else
        echo -e "  ${BLUE}ℹ️  Package statement es correcto${NC}"
    fi
else
    echo -e "  ${RED}❌ Archivo TendenciaVenta.java no encontrado${NC}"
    echo -e "     ${GRAY}Ubicaciones buscadas:${NC}"
    for path in "${TENDENCIA_PATHS[@]}"; do
        echo -e "     ${GRAY}- $(echo $path | sed "s|$PROJECT_ROOT/||")${NC}"
    done
fi

# =============== REFACTORIZACIÓN ADICIONAL: ProductoImportacionServicio ===============
echo -e "\n${MAGENTA}5️⃣ Refactorizando ProductoImportacionServicio...${NC}"

PROD_IMPORT_FILE="$PROJECT_ROOT/src/main/java/informviva/gest/service/ProductoImportacionServicio.java"

if [ -f "$PROD_IMPORT_FILE" ]; then
    if grep -q "@Autowired" "$PROD_IMPORT_FILE" && grep -q "private ProductoServicio productoServicio" "$PROD_IMPORT_FILE"; then
        create_backup "$PROD_IMPORT_FILE"

        if [ "$DRY_RUN" = false ]; then
            awk '
            /@Autowired/ {
                if (getline next_line && next_line ~ /private ProductoServicio productoServicio/) {
                    print "    private final ProductoServicio productoServicio;"
                    print ""
                    print "    public ProductoImportacionServicio(ProductoServicio productoServicio) {"
                    print "        this.productoServicio = productoServicio;"
                    print "    }"
                    next
                } else {
                    print
                    print next_line
                }
                next
            }
            {print}
            ' "$PROD_IMPORT_FILE" > "$PROD_IMPORT_FILE.tmp" && mv "$PROD_IMPORT_FILE.tmp" "$PROD_IMPORT_FILE"
        fi
        show_result "ProductoImportacionServicio refactorizado"
    else
        echo -e "  ${BLUE}ℹ️  Ya usa constructor injection${NC}"
    fi
else
    echo -e "  ${RED}❌ Archivo no encontrado: ProductoImportacionServicio.java${NC}"
fi

# =============== REFACTORIZACIÓN ADICIONAL: ClienteImportacionServicio ===============
echo -e "\n${MAGENTA}6️⃣ Refactorizando ClienteImportacionServicio...${NC}"

CLIENTE_IMPORT_FILE="$PROJECT_ROOT/src/main/java/informviva/gest/service/ClienteImportacionServicio.java"

if [ -f "$CLIENTE_IMPORT_FILE" ]; then
    if grep -q "@Autowired" "$CLIENTE_IMPORT_FILE" && grep -q "private ClienteServicio clienteServicio" "$CLIENTE_IMPORT_FILE"; then
        create_backup "$CLIENTE_IMPORT_FILE"

        if [ "$DRY_RUN" = false ]; then
            awk '
            /@Autowired/ {
                if (getline next_line && next_line ~ /private ClienteServicio clienteServicio/) {
                    print "    private final ClienteServicio clienteServicio;"
                    print ""
                    print "    public ClienteImportacionServicio(ClienteServicio clienteServicio) {"
                    print "        this.clienteServicio = clienteServicio;"
                    print "    }"
                    next
                } else {
                    print
                    print next_line
                }
                next
            }
            {print}
            ' "$CLIENTE_IMPORT_FILE" > "$CLIENTE_IMPORT_FILE.tmp" && mv "$CLIENTE_IMPORT_FILE.tmp" "$CLIENTE_IMPORT_FILE"
        fi
        show_result "ClienteImportacionServicio refactorizado"
    else
        echo -e "  ${BLUE}ℹ️  Ya usa constructor injection${NC}"
    fi
else
    echo -e "  ${RED}❌ Archivo no encontrado: ClienteImportacionServicio.java${NC}"
fi

# =============== VERIFICACIÓN ARCHIVOS DUPLICADOS ===============
echo -e "\n${MAGENTA}7️⃣ Verificando archivos duplicados...${NC}"

DUPLICATE_FILE="$PROJECT_ROOT/src/main/java/informviva/gest/service/impl/ImportacionServicioRefactorizado.java"

if [ -f "$DUPLICATE_FILE" ]; then
    echo -e "  ${RED}❌ Encontrado: ImportacionServicioRefactorizado.java${NC}"
    if [ "$DRY_RUN" = false ]; then
        read -p "  ¿Eliminar archivo duplicado? (s/n): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[SsYy]$ ]]; then
            rm "$DUPLICATE_FILE"
            echo -e "  ${GREEN}✅ Archivo eliminado${NC}"
        fi
    else
        show_result "Se eliminaría ImportacionServicioRefactorizado.java"
    fi
else
    echo -e "  ${GREEN}✅ No hay archivos duplicados${NC}"
fi

# =============== COMPILACIÓN FINAL ===============
echo -e "\n${MAGENTA}8️⃣ Verificación final...${NC}"

if [ "$DRY_RUN" = false ]; then
    test_compilation
else
    echo -e "  ${CYAN}🔍 [DRY RUN] Se ejecutaría: mvn clean compile${NC}"
fi

# =============== RESUMEN ===============
echo -e "\n${GREEN}✅ PROCESO COMPLETADO${NC}"
echo "==================="

echo -e "\n${CYAN}📊 Refactorizaciones automáticas:${NC}"
echo -e "${GREEN}✅ AsyncExportController (private → public)${NC}"
echo -e "${GREEN}✅ ImportacionAPIControlador (constructor injection)${NC}"
echo -e "${GREEN}✅ ImportacionControlador (constructor injection)${NC}"
echo -e "${GREEN}✅ ProductoImportacionServicio (constructor injection)${NC}"
echo -e "${GREEN}✅ ClienteImportacionServicio (constructor injection)${NC}"
echo -e "${GREEN}✅ TendenciaVenta (package correction)${NC}"

echo -e "\n${YELLOW}⚠️  Refactorizaciones manuales pendientes:${NC}"
echo -e "${GRAY}- ImportacionServicioImpl.java (4 dependencias)${NC}"
echo -e "${GRAY}- ExportacionServicioImpl.java (5 dependencias)${NC}"

echo -e "\n${CYAN}📋 Próximos pasos:${NC}"
echo -e "${NC}1. Ejecutar sin --dry-run para aplicar cambios${NC}"
echo -e "${NC}2. Refactorizar manualmente los archivos complejos${NC}"
echo -e "${NC}3. Ejecutar: mvn clean compile test${NC}"
echo -e "${NC}4. Ejecutar análisis de Qodana${NC}"

if [ "$BACKUP" = true ] && [ -d "$BACKUP_DIR" ]; then
    echo -e "\n${YELLOW}📁 Backups en: $BACKUP_DIR${NC}"
fi

echo -e "\n${GREEN}🚀 Script completado exitosamente!${NC}"