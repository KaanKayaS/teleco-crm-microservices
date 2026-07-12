# Microservices Startup Script
Write-Host "Starting Microservices in Order..." -ForegroundColor Cyan

$microservicesPath = "d:\gygy\teleco-crm-microservices\microservices"

function Start-ServiceInNewWindow {
    param(
        [string]$serviceName,
        [int]$delaySeconds = 0
    )
    Write-Host "Starting $serviceName..." -ForegroundColor Green
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd $microservicesPath\$serviceName; mvn spring-boot:run"
    if ($delaySeconds -gt 0) {
        Write-Host "Waiting $delaySeconds seconds for $serviceName to initialize..." -ForegroundColor Yellow
        Start-Sleep -Seconds $delaySeconds
    }
}

# 1. Core Services
Start-ServiceInNewWindow "config-server" 15
Start-ServiceInNewWindow "eureka-server" 15
Start-ServiceInNewWindow "gateway-server" 15

# 2. Domain Services
Start-ServiceInNewWindow "identity-service" 5
Start-ServiceInNewWindow "customer-service" 5
Start-ServiceInNewWindow "product-catalog-service" 5
Start-ServiceInNewWindow "subscription-service" 5
Start-ServiceInNewWindow "usage-service" 5
Start-ServiceInNewWindow "billing-service" 5
Start-ServiceInNewWindow "order-service" 5
Start-ServiceInNewWindow "payment-service" 5
Start-ServiceInNewWindow "notification-service" 5
Start-ServiceInNewWindow "ticket-service" 5

Write-Host "All backend microservices have been launched in separate windows!" -ForegroundColor Cyan
Write-Host "Make sure your Docker containers (docker-compose up -d) are already running." -ForegroundColor Yellow
