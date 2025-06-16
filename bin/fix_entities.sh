#!/bin/bash

# Script para reparar problemas específicos de entidades y DTOs
# Complementa al script principal de reparación

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

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

# Función para reparar entidad Cliente
fix_cliente_entity() {
    log "🔧 Reparando entidad Cliente..."

    local cliente_file="src/main/java/informviva/gest/model/Cliente.java"

    if [ -f "$cliente_file" ]; then
        # Verificar si tiene las anotaciones de Lombok
        if ! grep -q "@Data\|@Getter\|@Setter" "$cliente_file"; then
            # Agregar anotaciones de Lombok al inicio de la clase
            sed -i '/^public class Cliente/i @Data\n@Entity\n@Table(name = "clientes")' "$cliente_file"

            # Asegurar imports de Lombok
            if ! grep -q "import lombok.Data" "$cliente_file"; then
                sed -i '1i import lombok.Data;' "$cliente_file"
            fi
        fi

        # Verificar campos específicos que causan errores
        if ! grep -q "fechaRegistro" "$cliente_file"; then
            sed -i '/private.*id;/a\\n    @Column(name = "fecha_registro")\n    private LocalDateTime fechaRegistro;' "$cliente_file"
        fi

        success "Cliente reparado"
    else
        warning "Archivo Cliente.java no encontrado"
    fi
}

# Función para reparar entidad Producto
fix_producto_entity() {
    log "🔧 Reparando entidad Producto..."

    local producto_file="src/main/java/informviva/gest/model/Producto.java"

    if [ -f "$producto_file" ]; then
        # Verificar constructor por defecto
        if ! grep -q "public Producto()" "$producto_file"; then
            sed -i '/^public class Producto/a\\n    public Producto() {}\n' "$producto_file"
        fi

        # Verificar anotaciones de Lombok
        if ! grep -q "@Data\|@Getter\|@Setter" "$producto_file"; then
            sed -i '/^public class Producto/i @Data\n@Entity\n@Table(name = "productos")' "$producto_file"
            sed -i '1i import lombok.Data;' "$producto_file"
        fi

        # Verificar campos necesarios
        local campos_necesarios=("id" "nombre" "precio" "stock" "activo" "categoria")
        for campo in "${campos_necesarios[@]}"; do
            if ! grep -q "private.*$campo" "$producto_file"; then
                case $campo in
                    "id")
                        sed -i '/^public class Producto/a\\n    @Id\n    @GeneratedValue(strategy = GenerationType.IDENTITY)\n    private Long id;\n' "$producto_file"
                        ;;
                    "nombre")
                        sed -i '/private.*id;/a\\n    @Column(nullable = false)\n    private String nombre;\n' "$producto_file"
                        ;;
                    "precio")
                        sed -i '/private.*nombre;/a\\n    @Column(nullable = false)\n    private Double precio;\n' "$producto_file"
                        ;;
                    "stock")
                        sed -i '/private.*precio;/a\\n    @Column(nullable = false)\n    private Integer stock;\n' "$producto_file"
                        ;;
                    "activo")
                        sed -i '/private.*stock;/a\\n    @Column(nullable = false)\n    private Boolean activo = true;\n' "$producto_file"
                        ;;
                    "categoria")
                        sed -i '/private.*activo;/a\\n    @ManyToOne\n    @JoinColumn(name = "categoria_id")\n    private Categoria categoria;\n' "$producto_file"
                        ;;
                esac
            fi
        done

        success "Producto reparado"
    else
        warning "Archivo Producto.java no encontrado"
    fi
}

# Función para reparar entidad Categoria
fix_categoria_entity() {
    log "🔧 Reparando entidad Categoria..."

    local categoria_file="src/main/java/informviva/gest/model/Categoria.java"

    if [ -f "$categoria_file" ]; then
        # Verificar si tiene el método getNombre()
        if ! grep -q "getNombre\|@Data\|@Getter" "$categoria_file"; then
            sed -i '/^public class Categoria/i @Data\n@Entity\n@Table(name = "categorias")' "$categoria_file"
            sed -i '1i import lombok.Data;' "$categoria_file"
        fi

        # Verificar campo nombre
        if ! grep -q "private.*nombre" "$categoria_file"; then
            sed -i '/private.*id;/a\\n    @Column(nullable = false)\n    private String nombre;\n' "$categoria_file"
        fi

        success "Categoria reparada"
    else
        warning "Archivo Categoria.java no encontrado"
    fi
}

# Función para reparar entidad Venta
fix_venta_entity() {
    log "🔧 Reparando entidad Venta..."

    local venta_file="src/main/java/informviva/gest/model/Venta.java"

    if [ -f "$venta_file" ]; then
        # Agregar anotaciones de Lombok
        if ! grep -q "@Data\|@Getter\|@Setter" "$venta_file"; then
            sed -i '/^public class Venta/i @Data\n@Entity\n@Table(name = "ventas")' "$venta_file"
            sed -i '1i import lombok.Data;' "$venta_file"
        fi

        # Verificar campos necesarios para Venta
        local campos_venta=("cliente" "fecha" "total" "detalles" "estado")
        for campo in "${campos_venta[@]}"; do
            if ! grep -q "private.*$campo" "$venta_file"; then
                case $campo in
                    "cliente")
                        sed -i '/private.*id;/a\\n    @ManyToOne\n    @JoinColumn(name = "cliente_id")\n    private Cliente cliente;\n' "$venta_file"
                        ;;
                    "fecha")
                        sed -i '/private.*cliente;/a\\n    @Column(name = "fecha_venta")\n    private LocalDateTime fecha;\n' "$venta_file"
                        ;;
                    "total")
                        sed -i '/private.*fecha;/a\\n    @Column(nullable = false)\n    private Double total;\n' "$venta_file"
                        ;;
                    "detalles")
                        sed -i '/private.*total;/a\\n    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL)\n    private List<VentaDetalle> detalles;\n' "$venta_file"
                        ;;
                    "estado")
                        sed -i '/private.*detalles;/a\\n    @Column(nullable = false)\n    private String estado = "PENDIENTE";\n' "$venta_file"
                        ;;
                esac
            fi
        done

        success "Venta reparada"
    else
        warning "Archivo Venta.java no encontrado"
    fi
}

# Función para reparar VentaDetalle
fix_venta_detalle_entity() {
    log "🔧 Reparando entidad VentaDetalle..."

    local detalle_file="src/main/java/informviva/gest/model/VentaDetalle.java"

    if [ -f "$detalle_file" ]; then
        # Agregar anotaciones de Lombok
        if ! grep -q "@Data\|@Getter\|@Setter" "$detalle_file"; then
            sed -i '/^public class VentaDetalle/i @Data\n@Entity\n@Table(name = "venta_detalles")' "$detalle_file"
            sed -i '1i import lombok.Data;' "$detalle_file"
        fi

        # Verificar campos necesarios
        local campos_detalle=("venta" "producto" "cantidad" "precioUnitario")
        for campo in "${campos_detalle[@]}"; do
            if ! grep -q "private.*$campo" "$detalle_file"; then
                case $campo in
                    "venta")
                        sed -i '/private.*id;/a\\n    @ManyToOne\n    @JoinColumn(name = "venta_id")\n    private Venta venta;\n' "$detalle_file"
                        ;;
                    "producto")
                        sed -i '/private.*venta;/a\\n    @ManyToOne\n    @JoinColumn(name = "producto_id")\n    private Producto producto;\n' "$detalle_file"
                        ;;
                    "cantidad")
                        sed -i '/private.*producto;/a\\n    @Column(nullable = false)\n    private Integer cantidad;\n' "$detalle_file"
                        ;;
                    "precioUnitario")
                        sed -i '/private.*cantidad;/a\\n    @Column(name = "precio_unitario", nullable = false)\n    private Double precioUnitario;\n' "$detalle_file"
                        ;;
                esac
            fi
        done

        success "VentaDetalle reparado"
    else
        warning "Archivo VentaDetalle.java no encontrado"
    fi
}

# Función para reparar DTOs problemáticos
fix_dtos() {
    log "🔧 Reparando DTOs problemáticos..."

    # Reparar VentaDTO
    local venta_dto="src/main/java/informviva/gest/dto/VentaDTO.java"
    if [ -f "$venta_dto" ]; then
        # Asegurar que tiene los métodos necesarios
        if ! grep -q "getClienteId\|@Data" "$venta_dto"; then
            sed -i '/^public class VentaDTO/i @Data' "$venta_dto"
            sed -i '1i import lombok.Data;' "$venta_dto"

            # Agregar campo clienteId si no existe
            if ! grep -q "clienteId" "$venta_dto"; then
                sed -i '/private.*id;/a\\n    private Long clienteId;\n' "$venta_dto"
            fi

            # Agregar lista de productos si no existe
            if ! grep -q "productos" "$venta_dto"; then
                sed -i '/private.*clienteId;/a\\n    private List<ProductoVentaDTO> productos;\n' "$venta_dto"
            fi
        fi

        # Crear clase interna ProductoVentaDTO si no existe
        if ! grep -q "ProductoVentaDTO" "$venta_dto"; then
            cat >> "$venta_dto" << 'EOF'

    @Data
    public static class ProductoVentaDTO {
        private Long productoId;
        private Integer cantidad;
        private Double precio;
    }
EOF
        fi

        success "VentaDTO reparado"
    fi

    # Reparar EstadisticasResumen en ImportacionResultadoDTO
    local importacion_dto="src/main/java/informviva/gest/dto/ImportacionResultadoDTO.java"
    if [ -f "$importacion_dto" ]; then
        # Agregar constructor sin parámetros a EstadisticasResumen
        sed -i '/public static class EstadisticasResumen/a\\n        public EstadisticasResumen() {}\n' "$importacion_dto"
        success "ImportacionResultadoDTO reparado"
    fi

    # Reparar ClienteExportDTO
    local cliente_export_dto="src/main/java/informviva/gest/dto/ClienteExportDTO.java"
    if [ -f "$cliente_export_dto" ]; then
        if ! grep -q "setNombreCompleto\|@Data" "$cliente_export_dto"; then
            sed -i '/^public class ClienteExportDTO/i @Data' "$cliente_export_dto"
            sed -i '1i import lombok.Data;' "$cliente_export_dto"
        fi
        success "ClienteExportDTO reparado"
    fi
}

# Función para reparar servicios problemáticos
fix_services() {
    log "🔧 Reparando servicios problemáticos..."

    # Reparar CategoriaServicio
    local categoria_service="src/main/java/informviva/gest/service/CategoriaServicio.java"
    if [ -f "$categoria_service" ]; then
        # Agregar método findAllActivas si no existe
        if ! grep -q "findAllActivas" "$categoria_service"; then
            sed -i '/public interface CategoriaServicio/a\\n    List<Categoria> findAllActivas();\n    Categoria save(Categoria categoria);\n' "$categoria_service"
        fi
        success "CategoriaServicio reparado"
    fi

    # Reparar VentaServicio
    local venta_service="src/main/java/informviva/gest/service/VentaServicio.java"
    if [ -f "$venta_service" ]; then
        # Agregar método buscarPorRangoFechas si no existe
        if ! grep -q "buscarPorRangoFechas" "$venta_service"; then
            sed -i '/public interface VentaServicio/a\\n    List<Venta> buscarPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin);\n' "$venta_service"
        fi
        success "VentaServicio reparado"
    fi
}

# Función para agregar imports necesarios a todas las entidades
fix_imports() {
    log "🔧 Agregando imports necesarios..."

    # Lista de imports comunes para entidades
    local entity_imports=(
        "import jakarta.persistence.*;"
        "import java.time.LocalDateTime;"
        "import java.util.List;"
        "import lombok.Data;"
    )

    # Aplicar imports a todas las entidades
    find src/main/java/informviva/gest/model -name "*.java" | while read file; do
        for import_stmt in "${entity_imports[@]}"; do
            if ! grep -q "${import_stmt}" "$file"; then
                sed -i "1i ${import_stmt}" "$file"
            fi
        done
    done

    success "Imports agregados"
}

# Función principal
main() {
    log "🚀 Iniciando reparación de entidades y DTOs..."

    # Verificar directorio
    if [ ! -f "pom.xml" ]; then
        error "Ejecute desde el directorio raíz del proyecto"
        exit 1
    fi

    # Ejecutar reparaciones
    fix_imports
    fix_cliente_entity
    fix_producto_entity
    fix_categoria_entity
    fix_venta_entity
    fix_venta_detalle_entity
    fix_dtos
    fix_services

    log "🔍 Verificando estructura después de reparaciones..."
    success "✅ Reparación de entidades completada"

    log "💡 Próximos pasos:"
    log "   1. Ejecutar el script principal de reparación"
    log "   2. Compilar: mvn clean compile"
    log "   3. Ejecutar tests: mvn test"
}

# Ejecutar
case "${1:-}" in
    --help|-h)
        echo "Uso: $0"
        echo "Repara problemas específicos de entidades y DTOs"
        exit 0
        ;;
    *)
        main
        ;;
esac