# Arranca el backend de Healthy Bites en http://localhost:8090/api/v1
#
# - Necesita un JDK 25: usa JAVA_HOME si ya apunta a uno; si no, busca uno instalado.
# - Se ejecuta desde la raiz del backend para que la carpeta de subidas se resuelva correctamente.
# - Requiere la BD levantada:  docker compose up -d
#
# Variables opcionales: DB_SEED_MODE=never (no inyectar datos), SHOW_SQL=true, PORT, DB_URL...
# Secretos (MAIL_PASSWORD, etc.): ponlos en un archivo .env (ver .env.example); se carga aqui.

$ErrorActionPreference = "Stop"

# Carga .env (KEY=VALUE por linea; ignora comentarios). Las variables ya definidas tienen prioridad.
$envFile = Join-Path $PSScriptRoot ".env"
if (Test-Path $envFile) {
    foreach ($line in Get-Content $envFile) {
        if ($line -match '^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.*)\s*$') {
            $name = $Matches[1]
            $value = $Matches[2].Trim('"', "'")
            if (-not [Environment]::GetEnvironmentVariable($name, "Process")) {
                [Environment]::SetEnvironmentVariable($name, $value, "Process")
            }
        }
    }
    Write-Host "Variables cargadas desde .env" -ForegroundColor Cyan
}

# Version mayor de un JDK segun su fichero "release" (JAVA_VERSION="25.0.1" -> 25).
function Get-JavaMajor([string]$javaHome) {
    $release = Join-Path $javaHome "release"
    if (-not $javaHome -or -not (Test-Path $release)) { return 0 }
    $match = Select-String -Path $release -Pattern '^JAVA_VERSION="(\d+)' | Select-Object -First 1
    if ($null -eq $match) { return 0 }
    return [int]$match.Matches[0].Groups[1].Value
}

$jdk = $env:JAVA_HOME
if ((Get-JavaMajor $jdk) -lt 25) {
    $candidato = Get-ChildItem "C:\Program Files\Java", "C:\Program Files\Microsoft", "C:\Program Files\Eclipse Adoptium" -Directory -ErrorAction SilentlyContinue |
        Where-Object { (Get-JavaMajor $_.FullName) -ge 25 } | Sort-Object Name -Descending | Select-Object -First 1
    if ($null -eq $candidato) { throw "No se encontro un JDK 25. Instala Temurin 25 o define JAVA_HOME." }
    $jdk = $candidato.FullName
}

$env:JAVA_HOME = $jdk
$env:Path = "$jdk\bin;$env:Path"
Write-Host "Usando JAVA_HOME=$env:JAVA_HOME" -ForegroundColor Cyan

Set-Location -Path $PSScriptRoot
& .\mvnw.cmd spring-boot:run
