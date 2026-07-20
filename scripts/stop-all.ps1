#Requires -Version 5.1
# ============================================================
#  Teleco CRM Microservices - Full Stack Stop Script
#  Windows PowerShell
#  Kullanim: .\stop-all.ps1
#  Secenekler:
#    --docker    Docker container'larini da durdurur
#    --all       Tum seyleri durdurur (Java + Docker + Minikube)
#    --minikube  Minikube'u da durdurur
# ============================================================
param(
    [switch]$Docker,
    [switch]$Minikube,
    [switch]$All
)

$ErrorActionPreference = "Continue"
$ROOT = (Get-Item $PSScriptRoot).Parent.FullName

# --all flag'i digerleri de kapsar
if ($All) { $Docker = $true; $Minikube = $true }

# ─── Colour helpers ───────────────────────────────────────────────────────────
function Log-Info  { param($msg) Write-Host "  [INFO]  $msg" -ForegroundColor Cyan   }
function Log-OK    { param($msg) Write-Host "  [ OK ]  $msg" -ForegroundColor Green  }
function Log-Warn  { param($msg) Write-Host "  [WARN]  $msg" -ForegroundColor Yellow }
function Log-Error { param($msg) Write-Host "  [ERR ]  $msg" -ForegroundColor Red    }
function Log-Step  {
    param($msg)
    Write-Host "`n══════════════════════════════════════════════════" -ForegroundColor DarkGray
    Write-Host "  $msg" -ForegroundColor White
    Write-Host "══════════════════════════════════════════════════" -ForegroundColor DarkGray
}

Clear-Host
Write-Host @"

  ████████╗███████╗██╗     ███████╗ ██████╗ ██████╗     ██████╗██████╗ ███╗   ███╗
     ██╔══╝██╔════╝██║     ██╔════╝██╔════╝██╔═══██╗   ██╔════╝██╔══██╗████╗ ████║
     ██║   █████╗  ██║     █████╗  ██║     ██║   ██║   ██║     ██████╔╝██╔████╔██║
     ██║   ██╔══╝  ██║     ██╔══╝  ██║     ██║   ██║   ██║     ██╔══██╗██║╚██╔╝██║
     ██║   ███████╗███████╗███████╗╚██████╗╚██████╔╝   ╚██████╗██║  ██║██║ ╚═╝ ██║
     ╚═╝   ╚══════╝╚══════╝╚══════╝ ╚═════╝ ╚═════╝    ╚═════╝╚═╝  ╚═╝╚═╝     ╚═╝
                         Mikroservis Full-Stack Durdurma Scripti
"@ -ForegroundColor Magenta

# ─────────────────────────────────────────────────────────────────────────────
# ADIM 1 — Angular Frontend (port 4200)
# ─────────────────────────────────────────────────────────────────────────────
Log-Step "1/5  Angular Frontend durduruluyor (port 4200)"
try {
    $conn = Get-NetTCPConnection -LocalPort 4200 -State Listen -ErrorAction SilentlyContinue
    if ($conn) {
        $ownerPid = ($conn | Select-Object -First 1).OwningProcess
        if ($ownerPid -and $ownerPid -ne 4) {
            Stop-Process -Id $ownerPid -Force -ErrorAction SilentlyContinue
            Log-OK "Angular durduruldu (PID: $ownerPid)"
        }
    } else {
        Log-Warn "Angular zaten calismiyor (port 4200 kapali)"
    }
} catch { Log-Warn "Angular durdurulurken hata: $_" }

# ─────────────────────────────────────────────────────────────────────────────
# ADIM 2 — Java / Maven Servisleri (bilinen portlar)
# ─────────────────────────────────────────────────────────────────────────────
Log-Step "2/5  Java/Maven servisleri durduruluyor"

$servicePorts = [ordered]@{
    9090 = "BFF Server"
    9001 = "Identity Service"
    8088 = "Notification Service"
    8090 = "Ticket Service"
    8089 = "Payment Service"
    8087 = "Usage Service"
    8086 = "Subscription Service"
    8085 = "Order Service"
    8083 = "Product Catalog Service"
    9000 = "API Gateway"
    8888 = "Config Server"
    8761 = "Eureka Server"
}

foreach ($port in $servicePorts.Keys) {
    $name = $servicePorts[$port]
    try {
        $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
        if ($conn) {
            $ownerPid = ($conn | Select-Object -First 1).OwningProcess
            if ($ownerPid -and $ownerPid -ne 4) {
                Stop-Process -Id $ownerPid -Force -ErrorAction SilentlyContinue
                Log-OK "$name durduruldu (port $port, PID: $ownerPid)"
            }
        } else {
            Log-Warn "$name zaten kapali (port $port)"
        }
    } catch { Log-Warn "$name durdurulurken hata: $_" }
}

# Kalan mvn / java processlerini temizle
Log-Info "Kalan mvn/java processleri temizleniyor..."
$killed = 0
Get-Process -Name "java","mvn" -ErrorAction SilentlyContinue | ForEach-Object {
    try {
        Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
        $killed++
    } catch {}
}
# cmd.exe uzerinden calistirilan mvn processlerinin parent cmd'lerini de kapat
Get-Process -Name "cmd" -ErrorAction SilentlyContinue | Where-Object {
    $_.MainWindowTitle -eq "" -and $_.StartTime -gt (Get-Date).AddHours(-12)
} | ForEach-Object {
    try { Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue; $killed++ } catch {}
}
if ($killed -gt 0) { Log-OK "$killed ek java/mvn/cmd process durduruldu" }

# ─────────────────────────────────────────────────────────────────────────────
# ADIM 3 — kubectl port-forward (port 8081)
# ─────────────────────────────────────────────────────────────────────────────
Log-Step "3/5  kubectl port-forward durduruluyor (port 8081)"
try {
    $conn = Get-NetTCPConnection -LocalPort 8081 -State Listen -ErrorAction SilentlyContinue
    if ($conn) {
        $ownerPid = ($conn | Select-Object -First 1).OwningProcess
        if ($ownerPid -and $ownerPid -ne 4) {
            Stop-Process -Id $ownerPid -Force -ErrorAction SilentlyContinue
            Log-OK "port-forward durduruldu (port 8081, PID: $ownerPid)"
        }
    } else {
        Log-Warn "port-forward zaten kapali (port 8081)"
    }
    # PowerShell background job'larini da temizle
    Get-Job | Where-Object { $_.State -eq "Running" } | ForEach-Object {
        Stop-Job  $_ -ErrorAction SilentlyContinue
        Remove-Job $_ -ErrorAction SilentlyContinue
        Log-OK "Background job durduruldu: $($_.Name)"
    }
} catch { Log-Warn "port-forward durdurulurken hata: $_" }

# ─────────────────────────────────────────────────────────────────────────────
# ADIM 4 — Docker Container'lar (opsiyonel: --docker veya --all)
# ─────────────────────────────────────────────────────────────────────────────
Log-Step "4/5  Docker container'lari"
if ($Docker) {
    $DOCKER_DIR = Join-Path $ROOT "docker-telcom"
    if (Get-Command docker -ErrorAction SilentlyContinue) {
        Log-Info "docker-compose down calistiriliyor..."
        Push-Location $DOCKER_DIR
        docker-compose down
        Pop-Location
        Log-OK "Docker container'lari durduruldu"
    } else {
        Log-Warn "Docker bulunamadi, atlaniyor"
    }
} else {
    Log-Warn "Docker container'lari calistirilmaya devam ediyor. Durdurmak icin: .\stop-all.ps1 --docker"
}

# ─────────────────────────────────────────────────────────────────────────────
# ADIM 5 — Minikube (opsiyonel: --minikube veya --all)
# ─────────────────────────────────────────────────────────────────────────────
Log-Step "5/5  Minikube"
if ($Minikube) {
    if (Get-Command minikube -ErrorAction SilentlyContinue) {
        Log-Info "minikube stop calistiriliyor..."
        minikube stop 2>&1 | ForEach-Object { Log-Info $_ }
        Log-OK "Minikube durduruldu"
    } else {
        Log-Warn "minikube bulunamadi, atlaniyor"
    }
} else {
    Log-Warn "Minikube calistirilmaya devam ediyor. Durdurmak icin: .\stop-all.ps1 --minikube"
}

# ─────────────────────────────────────────────────────────────────────────────
# Ozet
# ─────────────────────────────────────────────────────────────────────────────
Write-Host ""
Write-Host "══════════════════════════════════════════════════" -ForegroundColor DarkGray
Write-Host "  Durdurma tamamlandi!" -ForegroundColor Green
Write-Host ""
Write-Host "  Yeniden baslatmak icin  : .\start-all.ps1" -ForegroundColor Cyan
Write-Host "  Docker'i da durdurmak   : .\stop-all.ps1 --docker" -ForegroundColor Cyan
Write-Host "  Minikube'u da durdurmak : .\stop-all.ps1 --minikube" -ForegroundColor Cyan
Write-Host "  Her seyi durdurmak      : .\stop-all.ps1 --all" -ForegroundColor Cyan
Write-Host "══════════════════════════════════════════════════" -ForegroundColor DarkGray
Write-Host ""
