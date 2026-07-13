#!/bin/bash
services=(
    "api-gateway"
    "auth-service"
    "evento-service"
    "notificacion-service"
    "pago-service"
    "reserva-service"
    "admin-service"
    "two-factor-service"
)

echo "Iniciando microservicios desde Git Bash..."

for service in "${services[@]}"; do
    echo "Iniciando $service..."
    # Inicia el microservicio en una nueva ventana de consola de Git Bash
    start bash -c "cd microservicios/$service && ./mvnw spring-boot:run; exec bash"
    sleep 3
done

echo "Todos los microservicios han sido iniciados en ventanas separadas."
