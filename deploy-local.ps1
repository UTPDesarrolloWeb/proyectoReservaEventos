# Script para compilar y subir imágenes a Azure Container Registry localmente

$ACR_NAME = "eventlyregistry24886"
$REGISTRY = "$ACR_NAME.azurecr.io"

# 1. Solicitar la contraseña del ACR
$password = Read-Host -Prompt "Ingresa la contraseña de administrador de tu ACR ($ACR_NAME)"

if ([string]::IsNullOrEmpty($password)) {
    Write-Error "La contraseña no puede estar vacía."
    exit
}

# 2. Iniciar sesión en el registro de Azure
Write-Host "Iniciando sesión en $REGISTRY..."
$loginProcess = echo $password | docker login $REGISTRY -u $ACR_NAME --password-stdin

if ($LASTEXITCODE -ne 0) {
    Write-Error "Fallo al iniciar sesión en Docker. Verifica la contraseña de tu ACR."
    exit
}

# 3. Microservicios a compilar
$services = @(
    "api-gateway",
    "auth-service",
    "evento-service",
    "reserva-service",
    "pago-service",
    "notificacion-service",
    "admin-service",
    "two-factor-service"
)

# 4. Compilar y empujar cada imagen
foreach ($s in $services) {
    Write-Host "`n==========================================================" -ForegroundColor Green
    Write-Host "COMPILANDO LOCALMENTE: $s" -ForegroundColor Green
    Write-Host "==========================================================" -ForegroundColor Green
    
    docker build -t "$REGISTRY/$($s):latest" "./microservicios/$s"
    
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Error compilando la imagen para $s. Abortando."
        exit
    }

    Write-Host "`n==========================================================" -ForegroundColor Cyan
    Write-Host "SUBIENDO A AZURE: $s" -ForegroundColor Cyan
    Write-Host "==========================================================" -ForegroundColor Cyan
    
    docker push "$REGISTRY/$($s):latest"
    
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Error subiendo la imagen de $s a Azure. Abortando."
        exit
    }
}

Write-Host "`n==========================================================" -ForegroundColor Green
Write-Host "¡TODAS LAS IMÁGENES COMPILADAS Y SUBIDAS A AZURE CON ÉXITO!" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
Write-Host "Ahora puedes ir a tu Cloud Shell de Azure y correr el script de actualización."
