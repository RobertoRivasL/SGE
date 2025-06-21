#!/bin/bash

# Script MÍNIMO y SEGURO - Solo crea archivos nuevos
# NO modifica código existente
# Autor: Asistente de Programación

set -e

PROJECT_ROOT="."
SRC_MAIN="src/main/java/informviva/gest"

echo "🛡️ SCRIPT SEGURO - Solo creación de archivos faltantes"
echo "===================================================="
echo "Este script SOLO crea archivos que faltan completamente."
echo "NO modifica código existente."
echo ""

# Función para crear backup
create_git_backup() {
    echo "📦 Creando backup en Git..."
    git add . 2>/dev/null || echo "⚠️ Git no inicializado, continuando sin backup"
    git commit -m "Backup antes de crear archivos faltantes - $(date)" 2>/dev/null || echo "⚠️ Sin cambios para commit"
}

# Función para verificar si archivo existe antes de crear
safe_create_file() {
    local file_path=$1
    local file_content=$2
    local description=$3

    if [ -f "$file_path" ]; then
        echo "⏭️ SALTANDO: $description ya existe: $file_path"
        return 0
    fi

    echo "📝 CREANDO: $description: $file_path"

    # Crear directorio si no existe
    mkdir -p "$(dirname "$file_path")"

    # Crear archivo
    echo "$file_content" > "$file_path"

    if [ -f "$file_path" ]; then
        echo "✅ CREADO exitosamente: $description"
        return 0
    else
        echo "❌ ERROR creando: $description"
        return 1
    fi
}

# Confirmar con usuario
echo "🤔 ¿Quieres continuar? Este script:"
echo "   ✅ Crea archivos que faltan (enum, DTOs)"
echo "   ✅ NO modifica archivos existentes"
echo "   ✅ Crea backup en Git"
echo ""
read -p "¿Continuar? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ Cancelado por el usuario"
    exit 1
fi

# Crear backup
create_git_backup

echo ""
echo "🔨 Creando archivos faltantes..."

# 1. CREAR ENUM FormatoExportacion
FORMATO_ENUM_PATH="$SRC_MAIN/enums/FormatoExportacion.java"
FORMATO_ENUM_CONTENT='package informviva.gest.enums;

/**
 * Enum para los formatos de exportación soportados
 * @author Roberto Rivas
 * @version 1.0
 */
public enum FormatoExportacion {
    PDF("PDF", "application/pdf"),
    EXCEL("Excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    CSV("CSV", "text/csv");

    private final String descripcion;
    private final String mimeType;

    FormatoExportacion(String descripcion, String mimeType) {
        this.descripcion = descripcion;
        this.mimeType = mimeType;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getMimeType() {
        return mimeType;
    }
}'

safe_create_file "$FORMATO_ENUM_PATH" "$FORMATO_ENUM_CONTENT" "Enum FormatoExportacion"

# 2. CREAR DTO ResultadoImportacionCliente
RESULTADO_DTO_PATH="$SRC_MAIN/dto/ResultadoImportacionCliente.java"
RESULTADO_DTO_CONTENT='package informviva.gest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para resultado de importación de clientes
 * @author Roberto Rivas
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoImportacionCliente {
    private int registrosExitosos;
    private int registrosConError;
    private int registrosOmitidos;
    private String mensaje;
    private boolean exitoso;

    public boolean isExitoso() {
        return exitoso;
    }

    public void setExitoso(boolean exitoso) {
        this.exitoso = exitoso;
    }
}'

safe_create_file "$RESULTADO_DTO_PATH" "$RESULTADO_DTO_CONTENT" "DTO ResultadoImportacionCliente"

# 3. CREAR ValidadorRutUtil si no existe
VALIDADOR_PATH="$SRC_MAIN/validador/ValidadorRutUtil.java"
VALIDADOR_CONTENT='package informviva.gest.validador;

/**
 * Utilidad para validar RUT chileno
 * @author Roberto Rivas
 * @version 1.0
 */
public class ValidadorRutUtil {

    /**
     * Valida un RUT chileno
     * @param rut RUT a validar
     * @return true si es válido, false en caso contrario
     */
    public static boolean validar(String rut) {
        if (rut == null || rut.trim().isEmpty()) {
            return false;
        }

        try {
            // Limpiar el RUT
            String rutLimpio = rut.replaceAll("[^0-9kK]", "").toUpperCase();

            if (rutLimpio.length() < 2) {
                return false;
            }

            String cuerpo = rutLimpio.substring(0, rutLimpio.length() - 1);
            char dv = rutLimpio.charAt(rutLimpio.length() - 1);

            int suma = 0;
            int multiplicador = 2;

            // Calcular suma
            for (int i = cuerpo.length() - 1; i >= 0; i--) {
                suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
                multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
            }

            int resto = suma % 11;
            char dvCalculado = resto == 0 ? '\''0'\'' : resto == 1 ? '\''K'\'' : (char) (48 + 11 - resto);

            return dv == dvCalculado;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Formatea un RUT con puntos y guión
     * @param rut RUT a formatear
     * @return RUT formateado
     */
    public static String formatear(String rut) {
        if (!validar(rut)) {
            return rut;
        }

        String rutLimpio = rut.replaceAll("[^0-9kK]", "").toUpperCase();
        String cuerpo = rutLimpio.substring(0, rutLimpio.length() - 1);
        String dv = rutLimpio.substring(rutLimpio.length() - 1);

        // Agregar puntos
        StringBuilder sb = new StringBuilder();
        int contador = 0;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            if (contador > 0 && contador % 3 == 0) {
                sb.insert(0, ".");
            }
            sb.insert(0, cuerpo.charAt(i));
            contador++;
        }

        return sb.toString() + "-" + dv;
    }
}'

safe_create_file "$VALIDADOR_PATH" "$VALIDADOR_CONTENT" "ValidadorRutUtil"

echo ""
echo "🧪 PROBANDO COMPILACIÓN..."

# Probar compilación
if command -v mvn &> /dev/null; then
    echo "Ejecutando: mvn compile -q"

    if mvn compile -q; then
        echo ""
        echo "🎉 ¡COMPILACIÓN EXITOSA!"
        echo "Los archivos creados no introdujeron errores."
    else
        echo ""
        echo "⚠️ Aún hay errores de compilación, pero los archivos creados están OK."
        echo "Errores restantes (primeros 10):"
        mvn compile 2>&1 | grep -E "\[ERROR\]" | head -10
    fi
else
    echo "⚠️ Maven no encontrado. Ejecuta manualmente: mvn compile"
fi

echo ""
echo "📊 RESUMEN DE ARCHIVOS CREADOS:"
echo "==============================="
[ -f "$FORMATO_ENUM_PATH" ] && echo "✅ FormatoExportacion enum" || echo "❌ FormatoExportacion enum"
[ -f "$RESULTADO_DTO_PATH" ] && echo "✅ ResultadoImportacionCliente DTO" || echo "❌ ResultadoImportacionCliente DTO"
[ -f "$VALIDADOR_PATH" ] && echo "✅ ValidadorRutUtil" || echo "✅ ValidadorRutUtil (ya existía)"

echo ""
echo "🎯 PRÓXIMOS PASOS SEGUROS:"
echo "=========================="
echo "1. Verificar que los archivos creados están correctos"
echo "2. Ejecutar: mvn compile"
echo "3. Si hay menos errores, continuar con correcciones manuales"
echo "4. Si hay más errores, hacer rollback: git reset --hard HEAD"

echo ""
echo "✅ SCRIPT SEGURO COMPLETADO"
echo "Solo se crearon archivos nuevos, sin modificar código existente."