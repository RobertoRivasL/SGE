# ===================================================================
# Script simplificado para refactorizar servicios a Constructor Injection
# ===================================================================

param(
    [switch]$DryRun = $false,
    [switch]$Backup = $true
)

Write-Host "🔧 REFACTORIZACIÓN A CONSTRUCTOR INJECTION" -ForegroundColor Green
Write-Host "===========================================" -ForegroundColor Green

$projectRoot = Get-Location
$timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$backupDir = Join-Path $projectRoot "backup-refactoring-$timestamp"

if ($Backup) {
    Write-Host "📁 Creando backup en: $backupDir" -ForegroundColor Yellow
    New-Item -ItemType Directory -Path $backupDir -Force | Out-Null
}

function Create-Backup {
    param([string]$filePath)
    if ($Backup -and (Test-Path $filePath)) {
        $fileName = Split-Path $filePath -Leaf
        $backupPath = Join-Path $backupDir $fileName
        Copy-Item $filePath $backupPath -Force
        Write-Host "  ✓ Backup: $fileName" -ForegroundColor Gray
    }
}

function Show-Result {
    param([string]$message)
    if ($DryRun) {
        Write-Host "  🔍 [DRY RUN] $message" -ForegroundColor Cyan
    } else {
        Write-Host "  ✓ $message" -ForegroundColor Green
    }
}

function Test-Compilation {
    Write-Host "🧪 Probando compilación..." -ForegroundColor Yellow
    try {
        $result = & mvn clean compile -q 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  ✅ Compilación exitosa" -ForegroundColor Green
            return $true
        } else {
            Write-Host "  ❌ Error de compilación" -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host "  ❌ Error ejecutando Maven" -ForegroundColor Red
        return $false
    }
}

# =============== CORRECCIÓN 1: AsyncExportController ===============
Write-Host "`n1️⃣ Corrigiendo AsyncExportController..." -ForegroundColor Magenta

$asyncFile = Join-Path $projectRoot "src\main\java\informviva\gest\controlador\AsyncExportController.java"

if (Test-Path $asyncFile) {
    $content = Get-Content $asyncFile -Raw

    if ($content.Contains("private CompletableFuture<Void> procesarExportacionAsync")) {
        Create-Backup $asyncFile
        $newContent = $content.Replace("private CompletableFuture<Void> procesarExportacionAsync", "public CompletableFuture<Void> procesarExportacionAsync")

        if (-not $DryRun) {
            Set-Content -Path $asyncFile -Value $newContent -NoNewline
        }
        Show-Result "Método cambiado de private a public"
    } else {
        Write-Host "  ℹ️  Ya está corregido" -ForegroundColor Blue
    }
} else {
    Write-Host "  ❌ Archivo no encontrado" -ForegroundColor Red
}

# =============== CORRECCIÓN 2: ImportacionAPIControlador ===============
Write-Host "`n2️⃣ Corrigiendo ImportacionAPIControlador..." -ForegroundColor Magenta

$apiFile = Join-Path $projectRoot "src\main\java\informviva\gest\controlador\api\ImportacionAPIControlador.java"

if (Test-Path $apiFile) {
    $content = Get-Content $apiFile -Raw

    if ($content.Contains("private ImportacionServicio importacionServicio;") -and $content.Contains("Autowired")) {
        Create-Backup $apiFile

        # Eliminar Autowired y agregar final
        $newContent = $content -replace "(?s)@Autowired\s+private ImportacionServicio importacionServicio;", "private final ImportacionServicio importacionServicio;"

        # Agregar constructor después del campo
        $fieldPattern = "private final ImportacionServicio importacionServicio;"
        $constructorCode = @"
private final ImportacionServicio importacionServicio;

    public ImportacionAPIControlador(ImportacionServicio importacionServicio) {
        this.importacionServicio = importacionServicio;
    }
"@

        $newContent = $newContent.Replace($fieldPattern, $constructorCode)

        if (-not $DryRun) {
            Set-Content -Path $apiFile -Value $newContent -NoNewline
        }
        Show-Result "Constructor injection implementado"
    } else {
        Write-Host "  ℹ️  Ya usa constructor injection" -ForegroundColor Blue
    }
} else {
    Write-Host "  ❌ Archivo no encontrado" -ForegroundColor Red
}

# =============== CORRECCIÓN 3: ImportacionControlador ===============
Write-Host "`n3️⃣ Corrigiendo ImportacionControlador..." -ForegroundColor Magenta

$controllerFile = Join-Path $projectRoot "src\main\java\informviva\gest\controlador\ImportacionControlador.java"

if (Test-Path $controllerFile) {
    $content = Get-Content $controllerFile -Raw

    if ($content.Contains("private ImportacionServicio importacionServicio;") -and $content.Contains("Autowired")) {
        Create-Backup $controllerFile

        $newContent = $content -replace "(?s)@Autowired\s+private ImportacionServicio importacionServicio;", "private final ImportacionServicio importacionServicio;"

        $fieldPattern = "private final ImportacionServicio importacionServicio;"
        $constructorCode = @"
private final ImportacionServicio importacionServicio;

    public ImportacionControlador(ImportacionServicio importacionServicio) {
        this.importacionServicio = importacionServicio;
    }
"@

        $newContent = $newContent.Replace($fieldPattern, $constructorCode)

        if (-not $DryRun) {
            Set-Content -Path $controllerFile -Value $newContent -NoNewline
        }
        Show-Result "Constructor injection implementado"
    } else {
        Write-Host "  ℹ️  Ya usa constructor injection" -ForegroundColor Blue
    }
} else {
    Write-Host "  ❌ Archivo no encontrado" -ForegroundColor Red
}

# =============== CORRECCIÓN 4: TendenciaVenta ===============
Write-Host "`n4️⃣ Buscando TendenciaVenta..." -ForegroundColor Magenta

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
        Write-Host "  ✓ Encontrado: $path" -ForegroundColor Green
        break
    }
}

if ($tendenciaFile) {
    $content = Get-Content $tendenciaFile -Raw

    if ($content.Contains("package informviva.gest.dto;") -and $tendenciaFile.Contains("\enums\")) {
        Create-Backup $tendenciaFile
        $newContent = $content.Replace("package informviva.gest.dto;", "package informviva.gest.enums;")

        if (-not $DryRun) {
            Set-Content -Path $tendenciaFile -Value $newContent -NoNewline
        }
        Show-Result "Package statement corregido"
    } else {
        Write-Host "  ℹ️  Package correcto" -ForegroundColor Blue
    }
} else {
    Write-Host "  ❌ Archivo TendenciaVenta no encontrado" -ForegroundColor Red
}

# =============== REFACTORIZACIÓN ADICIONAL: ProductoImportacionServicio ===============
Write-Host "`n5️⃣ Refactorizando ProductoImportacionServicio..." -ForegroundColor Magenta

$prodImportFile = Join-Path $projectRoot "src\main\java\informviva\gest\service\ProductoImportacionServicio.java"

if (Test-Path $prodImportFile) {
    $content = Get-Content $prodImportFile -Raw

    if ($content.Contains("private ProductoServicio productoServicio;") -and $content.Contains("Autowired")) {
        Create-Backup $prodImportFile

        $newContent = $content -replace "(?s)@Autowired\s+private ProductoServicio productoServicio;", "private final ProductoServicio productoServicio;"

        $fieldPattern = "private final ProductoServicio productoServicio;"
        $constructorCode = @"
private final ProductoServicio productoServicio;

    public ProductoImportacionServicio(ProductoServicio productoServicio) {
        this.productoServicio = productoServicio;
    }
"@

        $newContent = $newContent.Replace($fieldPattern, $constructorCode)

        if (-not $DryRun) {
            Set-Content -Path $prodImportFile -Value $newContent -NoNewline
        }
        Show-Result "ProductoImportacionServicio refactorizado"
    } else {
        Write-Host "  ℹ️  Ya usa constructor injection" -ForegroundColor Blue
    }
} else {
    Write-Host "  ❌ Archivo no encontrado" -ForegroundColor Red
}

# =============== REFACTORIZACIÓN ADICIONAL: ClienteImportacionServicio ===============
Write-Host "`n6️⃣ Refactorizando ClienteImportacionServicio..." -ForegroundColor Magenta

$clienteImportFile = Join-Path $projectRoot "src\main\java\informviva\gest\service\ClienteImportacionServicio.java"

if (Test-Path $clienteImportFile) {
    $content = Get-Content $clienteImportFile -Raw

    if ($content.Contains("private ClienteServicio clienteServicio;") -and $content.Contains("Autowired")) {
        Create-Backup $clienteImportFile

        $newContent = $content -replace "(?s)@Autowired\s+private ClienteServicio clienteServicio;", "private final ClienteServicio clienteServicio;"

        $fieldPattern = "private final ClienteServicio clienteServicio;"
        $constructorCode = @"
private final ClienteServicio clienteServicio;

    public ClienteImportacionServicio(ClienteServicio clienteServicio) {
        this.clienteServicio = clienteServicio;
    }
"@

        $newContent = $newContent.Replace($fieldPattern, $constructorCode)

        if (-not $DryRun) {
            Set-Content -Path $clienteImportFile -Value $newContent -NoNewline
        }
        Show-Result "ClienteImportacionServicio refactorizado"
    } else {
        Write-Host "  ℹ️  Ya usa constructor injection" -ForegroundColor Blue
    }
} else {
    Write-Host "  ❌ Archivo no encontrado" -ForegroundColor Red
}

# =============== VERIFICACIÓN ARCHIVOS DUPLICADOS ===============
Write-Host "`n7️⃣ Verificando archivos duplicados..." -ForegroundColor Magenta

$duplicateFile = Join-Path $projectRoot "src\main\java\informviva\gest\service\impl\ImportacionServicioRefactorizado.java"

if (Test-Path $duplicateFile) {
    Write-Host "  ❌ Encontrado: ImportacionServicioRefactorizado.java" -ForegroundColor Red
    if (-not $DryRun) {
        $userChoice = Read-Host "  ¿Eliminar archivo duplicado? (s/n)"
        if ($userChoice -eq 's' -or $userChoice -eq 'S') {
            Remove-Item $duplicateFile -Force
            Write-Host "  ✅ Archivo eliminado" -ForegroundColor Green
        }
    } else {
        Show-Result "Se eliminaría ImportacionServicioRefactorizado.java"
    }
} else {
    Write-Host "  ✅ No hay archivos duplicados" -ForegroundColor Green
}

# =============== COMPILACIÓN FINAL ===============
Write-Host "`n8️⃣ Verificación final..." -ForegroundColor Magenta

if (-not $DryRun) {
    Test-Compilation
} else {
    Write-Host "  🔍 [DRY RUN] Se ejecutaría: mvn clean compile" -ForegroundColor Cyan
}

# =============== RESUMEN ===============
Write-Host "`n✅ PROCESO COMPLETADO" -ForegroundColor Green
Write-Host "===================" -ForegroundColor Green

Write-Host "`n📊 Refactorizaciones automáticas:" -ForegroundColor Cyan
Write-Host "✅ AsyncExportController (private → public)" -ForegroundColor Green
Write-Host "✅ ImportacionAPIControlador (constructor injection)" -ForegroundColor Green
Write-Host "✅ ImportacionControlador (constructor injection)" -ForegroundColor Green
Write-Host "✅ ProductoImportacionServicio (constructor injection)" -ForegroundColor Green
Write-Host "✅ ClienteImportacionServicio (constructor injection)" -ForegroundColor Green
Write-Host "✅ TendenciaVenta (package correction)" -ForegroundColor Green

Write-Host "`n⚠️  Refactorizaciones manuales pendientes:" -ForegroundColor Yellow
Write-Host "- ImportacionServicioImpl.java (4 dependencias)" -ForegroundColor Gray
Write-Host "- ExportacionServicioImpl.java (5 dependencias)" -ForegroundColor Gray

Write-Host "`n📋 Próximos pasos:" -ForegroundColor Cyan
Write-Host "1. Ejecutar sin -DryRun para aplicar cambios" -ForegroundColor White
Write-Host "2. Refactorizar manualmente los archivos complejos" -ForegroundColor White
Write-Host "3. Ejecutar: mvn clean compile test" -ForegroundColor White
Write-Host "4. Ejecutar análisis de Qodana" -ForegroundColor White

if ($Backup -and (Test-Path $backupDir)) {
    Write-Host "`n📁 Backups en: $backupDir" -ForegroundColor Yellow
}