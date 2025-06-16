# ===================================================================
# Script PowerShell SIMPLIFICADO para corregir errores críticos de Qodana
# Para sistemas Windows
# ===================================================================

param(
    [switch]$DryRun = $false,
    [switch]$Backup = $true
)

Write-Host "🔧 Iniciando corrección de errores críticos de Qodana..." -ForegroundColor Green

# Configuración
$projectRoot = Get-Location
$timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$backupDir = Join-Path $projectRoot "backup-qodana-$timestamp"

if ($Backup) {
    Write-Host "📁 Creando directorio de backup: $backupDir" -ForegroundColor Yellow
    New-Item -ItemType Directory -Path $backupDir -Force | Out-Null
}

# Función para crear backup
function Create-Backup {
    param([string]$filePath)

    if ($Backup -and (Test-Path $filePath)) {
        $fileName = Split-Path $filePath -Leaf
        $backupPath = Join-Path $backupDir $fileName
        Copy-Item $filePath $backupPath -Force
        Write-Host "  ✓ Backup creado: $fileName" -ForegroundColor Gray
    }
}

# Función para mostrar resultado
function Show-Result {
    param([string]$message, [string]$color = "Green")

    if ($DryRun) {
        Write-Host "  🔍 [DRY RUN] $message" -ForegroundColor Cyan
    } else {
        Write-Host "  ✓ $message" -ForegroundColor $color
    }
}

# =============== CORRECCIÓN 1: AsyncExportController ===============
Write-Host "`n1️⃣ Corrigiendo AsyncExportController..." -ForegroundColor Magenta

$asyncFile = Join-Path $projectRoot "src\main\java\informviva\gest\controlador\AsyncExportController.java"

if (Test-Path $asyncFile) {
    $content = Get-Content $asyncFile -Raw

    # Buscar y reemplazar el método private por public
    if ($content.Contains("private CompletableFuture<Void> procesarExportacionAsync")) {
        $newContent = $content.Replace("private CompletableFuture<Void> procesarExportacionAsync", "public CompletableFuture<Void> procesarExportacionAsync")

        if (-not $DryRun) {
            Create-Backup $asyncFile
            Set-Content -Path $asyncFile -Value $newContent -NoNewline
        }
        Show-Result "Método @Async cambiado de private a public"
    } else {
        Write-Host "  ℹ️  Método ya es público o patrón no encontrado" -ForegroundColor Blue
    }
} else {
    Write-Host "  ❌ Archivo no encontrado: AsyncExportController.java" -ForegroundColor Red
}

# =============== CORRECCIÓN 2: ImportacionAPIControlador ===============
Write-Host "`n2️⃣ Corrigiendo ImportacionAPIControlador..." -ForegroundColor Magenta

$apiFile = Join-Path $projectRoot "src\main\java\informviva\gest\controlador\api\ImportacionAPIControlador.java"

if (Test-Path $apiFile) {
    $content = Get-Content $apiFile -Raw

    # Verificar si tiene @Autowired en campo
    if ($content.Contains("@Autowired") -and $content.Contains("private ImportacionServicio importacionServicio")) {

        # Paso 1: Eliminar @Autowired y cambiar a final
        $newContent = $content.Replace("@Autowired`r`n    private ImportacionServicio importacionServicio;", "private final ImportacionServicio importacionServicio;")
        $newContent = $newContent.Replace("@Autowired`n    private ImportacionServicio importacionServicio;", "private final ImportacionServicio importacionServicio;")

        # Paso 2: Agregar constructor después del campo
        $fieldDeclaration = "private final ImportacionServicio importacionServicio;"
        $constructor = @"
private final ImportacionServicio importacionServicio;

    public ImportacionAPIControlador(ImportacionServicio importacionServicio) {
        this.importacionServicio = importacionServicio;
    }
"@

        $newContent = $newContent.Replace($fieldDeclaration, $constructor)

        if (-not $DryRun) {
            Create-Backup $apiFile
            Set-Content -Path $apiFile -Value $newContent -NoNewline
        }
        Show-Result "Inyección por constructor implementada en ImportacionAPIControlador"
    } else {
        Write-Host "  ℹ️  Ya usa inyección por constructor o no tiene @Autowired" -ForegroundColor Blue
    }
} else {
    Write-Host "  ❌ Archivo no encontrado: ImportacionAPIControlador.java" -ForegroundColor Red
}

# =============== CORRECCIÓN 3: ImportacionControlador ===============
Write-Host "`n3️⃣ Corrigiendo ImportacionControlador..." -ForegroundColor Magenta

$controllerFile = Join-Path $projectRoot "src\main\java\informviva\gest\controlador\ImportacionControlador.java"

if (Test-Path $controllerFile) {
    $content = Get-Content $controllerFile -Raw

    # Verificar si tiene @Autowired en campo
    if ($content.Contains("@Autowired") -and $content.Contains("private ImportacionServicio importacionServicio")) {

        # Paso 1: Eliminar @Autowired y cambiar a final
        $newContent = $content.Replace("@Autowired`r`n    private ImportacionServicio importacionServicio;", "private final ImportacionServicio importacionServicio;")
        $newContent = $newContent.Replace("@Autowired`n    private ImportacionServicio importacionServicio;", "private final ImportacionServicio importacionServicio;")

        # Paso 2: Agregar constructor
        $fieldDeclaration = "private final ImportacionServicio importacionServicio;"
        $constructor = @"
private final ImportacionServicio importacionServicio;

    public ImportacionControlador(ImportacionServicio importacionServicio) {
        this.importacionServicio = importacionServicio;
    }
"@

        $newContent = $newContent.Replace($fieldDeclaration, $constructor)

        if (-not $DryRun) {
            Create-Backup $controllerFile
            Set-Content -Path $controllerFile -Value $newContent -NoNewline
        }
        Show-Result "Inyección por constructor implementada en ImportacionControlador"
    } else {
        Write-Host "  ℹ️  Ya usa inyección por constructor o no tiene @Autowired" -ForegroundColor Blue
    }
} else {
    Write-Host "  ❌ Archivo no encontrado: ImportacionControlador.java" -ForegroundColor Red
}

# =============== CORRECCIÓN 4: TendenciaVenta ===============
Write-Host "`n4️⃣ Buscando archivo TendenciaVenta..." -ForegroundColor Magenta

# Buscar en posibles ubicaciones
$tendenciaPaths = @(
    "src\main\java\informviva\gest\dto\TendenciaVenta.java",
    "src\main\java\informviva\gest\enums\TendenciaVenta.java",
    "src\main\java\informviva\gest\model\TendenciaVenta.java"
)

$tendenciaFile = $null
foreach ($path in $tendenciaPaths) {
    $fullPath = Join-Path $projectRoot $path
    if (Test-Path $fullPath) {
        $tendenciaFile = $fullPath
        Write-Host "  ✓ Archivo encontrado: $path" -ForegroundColor Green
        break
    }
}

if ($tendenciaFile) {
    $content = Get-Content $tendenciaFile -Raw

    # Verificar package statement incorrecto
    if ($content.Contains("package informviva.gest.dto;") -and $tendenciaFile.Contains("\enums\")) {
        $newContent = $content.Replace("package informviva.gest.dto;", "package informviva.gest.enums;")

        if (-not $DryRun) {
            Create-Backup $tendenciaFile
            Set-Content -Path $tendenciaFile -Value $newContent -NoNewline
        }
        Show-Result "Package statement corregido de dto a enums"
    } else {
        Write-Host "  ℹ️  Package statement es correcto o no necesita cambios" -ForegroundColor Blue
    }
} else {
    Write-Host "  ❌ Archivo TendenciaVenta.java no encontrado" -ForegroundColor Red
    Write-Host "     Ubicaciones buscadas:" -ForegroundColor Gray
    foreach ($path in $tendenciaPaths) {
        Write-Host "     - $path" -ForegroundColor Gray
    }
}

# =============== VERIFICACIÓN MAVEN ===============
Write-Host "`n5️⃣ Verificando Maven..." -ForegroundColor Magenta

try {
    $null = Get-Command mvn -ErrorAction Stop
    Write-Host "  ✓ Maven está disponible" -ForegroundColor Green

    if (-not $DryRun) {
        Write-Host "  🔄 Ejecutando mvn clean compile..." -ForegroundColor Yellow
        $result = & mvn clean compile 2>&1

        if ($LASTEXITCODE -eq 0) {
            Write-Host "  ✓ Compilación exitosa" -ForegroundColor Green
        } else {
            Write-Host "  ❌ Error en compilación:" -ForegroundColor Red
            Write-Host $result -ForegroundColor Red
        }
    } else {
        Write-Host "  🔍 [DRY RUN] Se ejecutaría: mvn clean compile" -ForegroundColor Cyan
    }
} catch {
    Write-Host "  ❌ Maven no está disponible en PATH" -ForegroundColor Red
    Write-Host "     Asegúrate de que Maven esté instalado y en PATH" -ForegroundColor Gray
}

# =============== RESUMEN ===============
Write-Host "`n✅ Proceso completado!" -ForegroundColor Green

if ($Backup -and (Test-Path $backupDir)) {
    $backupFiles = Get-ChildItem $backupDir
    if ($backupFiles.Count -gt 0) {
        Write-Host "📁 Backup creado con $($backupFiles.Count) archivo(s) en: $backupDir" -ForegroundColor Yellow
    }
}

Write-Host "`n📋 Próximos pasos:" -ForegroundColor Cyan
Write-Host "1. Revisar los cambios realizados" -ForegroundColor White
Write-Host "2. Ejecutar: mvn test" -ForegroundColor White
Write-Host "3. Ejecutar análisis de Qodana nuevamente" -ForegroundColor White
Write-Host "4. Hacer commit si todo está correcto" -ForegroundColor White

if ($DryRun) {
    Write-Host "`n🔍 Ejecuta sin -DryRun para aplicar los cambios" -ForegroundColor Cyan
}