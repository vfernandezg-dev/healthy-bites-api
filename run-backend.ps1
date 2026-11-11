# Arranca el backend de Healthy Bites en http://localhost:8090/api/v1
#
# - Usa JDK 21 solo para este proceso (Spring Boot 3.3 / Lombok / Hibernate no soportan JDK 25).
# - Se ejecuta desde la raiz del backend para que "mediafiles/" se resuelva correctamente.
# - Requiere la BD levantada:  docker compose up -d
#
# Variables opcionales: DB_SEED_MODE=never (no inyectar datos), SHOW_SQL=true, SERVER_PORT, DB_URL...

$ErrorActionPreference = "Stop"

$jdk21 = "C:\Program Files\Java\jdk-21.0.10"
if (-not (Test-Path "$jdk21\bin\java.exe")) {
    $candidato = Get-ChildItem "C:\Program Files\Java", "C:\Program Files\Microsoft", "C:\Program Files\Eclipse Adoptium" -Directory -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -match '^jdk-21' } | Select-Object -First 1
    if ($null -eq $candidato) { throw "No se encontro un JDK 21. Instala uno o edita la ruta en run-backend.ps1" }
    $jdk21 = $candidato.FullName
}

$env:JAVA_HOME = $jdk21
$env:Path = "$jdk21\bin;$env:Path"
Write-Host "Usando JAVA_HOME=$env:JAVA_HOME" -ForegroundColor Cyan

Set-Location -Path $PSScriptRoot
& .\mvnw.cmd spring-boot:run
