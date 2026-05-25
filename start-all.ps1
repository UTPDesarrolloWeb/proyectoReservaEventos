$folders = @(
    "api-gateway",
    "auth-service",
    "evento-service",
    "notificacion-service",
    "pago-service",
    "reserva-service",
    "admin-service"
)

Write-Host "Iniciando compilacion de microservicios..." -ForegroundColor Cyan

foreach ($folder in $folders) {
    Write-Host "Iniciando $folder..." -ForegroundColor Green
    
    # Inicia una nueva ventana de PowerShell para cada microservicio
    Start-Process powershell -ArgumentList "-NoExit -Command `"cd 'microservicios\$folder'; .\mvnw spring-boot:run`""
    
    # Espera un par de segundos entre cada inicio para no sobrecargar
    Start-Sleep -Seconds 3
}

Write-Host "Todos los microservicios han sido iniciados en ventanas separadas." -ForegroundColor Cyan
Write-Host "Por favor, espera unos segundos a que todos los servicios arranquen completamente." -ForegroundColor Yellow
