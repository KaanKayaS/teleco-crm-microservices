#Requires -Version 5.1
# ============================================================
#  Teleco CRM Microservices - Full Stack Startup Script
#  Windows PowerShell
#  Kullanim: .\start-all.ps1
#  Ctrl+C   -> tum arka plan Java processleri temiz kapatilir
# ============================================================

$ErrorActionPreference = "Continue"
$ROOT       = (Get-Item $PSScriptRoot).Parent.FullName
$MS_ROOT    = Join-Path $ROOT "microservices"
$DOCKER_DIR = Join-Path $ROOT "docker-telcom"
$FRONTEND   = Join-Path $ROOT "frontend"
$K8S_DIR    = Join-Path $MS_ROOT "customer-service\k8s"
$LOG_DIR    = Join-Path $ROOT "logs"

# Track background Java PIDs for cleanup
$script:javaPids = [System.Collections.Generic.List[int]]::new()

# ─── Colour helpers ───────────────────────────────────────────────────────────
function Log-Info  { param($msg) Write-Host "  [INFO]  $msg" -ForegroundColor Cyan   }
function Log-OK    { param($msg) Write-Host "  [ OK ]  $msg" -ForegroundColor Green  }
function Log-Warn  { param($msg) Write-Host "  [WARN]  $msg" -ForegroundColor Yellow }
function Log-Error { param($msg) Write-Host "  [ERR ]  $msg" -ForegroundColor Red    }
function Log-Step  {
    param($msg)
    Write-Host "`n══════════════════════════════════════════════════" -ForegroundColor DarkGray
    Write-Host "  ADIM: $msg" -ForegroundColor White
    Write-Host "══════════════════════════════════════════════════" -ForegroundColor DarkGray
}

# ─── Cleanup / Trap ───────────────────────────────────────────────────────────
function Invoke-Cleanup {
    Write-Host "`n`n  Kapatma sinyali alindi. Tum servisler durduruluyor..." -ForegroundColor Magenta

    foreach ($javaPid in $script:javaPids) {
        try {
            $proc = Get-Process -Id $javaPid -ErrorAction SilentlyContinue
            if ($proc) {
                Log-Warn "Process durduruluyor: PID $javaPid ($($proc.ProcessName))"
                Stop-Process -Id $javaPid -Force -ErrorAction SilentlyContinue
            }
        } catch {}
    }

    # Kill any leftover mvn/java on known ports
    $knownPorts = @(8761, 8888, 8222, 8081, 8082, 8083, 8084, 8085, 8086, 8087, 8088, 8089, 8090, 4200)
    foreach ($port in $knownPorts) {
        try {
            $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
            if ($conn) {
                $ownerPid = ($conn | Select-Object -First 1).OwningProcess
                if ($ownerPid -and $ownerPid -ne 4) {  # pid 4 = System, skip
                    Stop-Process -Id $ownerPid -Force -ErrorAction SilentlyContinue
                    Log-Warn "Port $port serbest birakildi (PID: $ownerPid)"
                }
            }
        } catch {}
    }

    Log-OK "Temizleme tamamlandi. Gorusuruz!"
}

# Register cleanup on script exit
Register-EngineEvent -SourceIdentifier ([System.Management.Automation.PsEngineEvent]::Exiting) -Action {
    Invoke-Cleanup
} | Out-Null

# ─── Helper: Wait for TCP port ────────────────────────────────────────────────
function Wait-ForPort {
    param(
        [string]$ServiceName,
        [int]$Port,
        [int]$TimeoutSeconds = 180
    )
    Log-Info "$ServiceName port $Port acilmayı bekliyor..."
    $elapsed = 0
    while ($elapsed -lt $TimeoutSeconds) {
        try {
            $tcp = New-Object System.Net.Sockets.TcpClient
            $ar  = $tcp.BeginConnect("localhost", $Port, $null, $null)
            $ok  = $ar.AsyncWaitHandle.WaitOne(1000, $false)
            if ($ok -and $tcp.Connected) {
                $tcp.Close()
                Log-OK "$ServiceName hazir (port $Port acik)"
                return
            }
            $tcp.Close()
        } catch {}
        Start-Sleep -Seconds 3
        $elapsed += 3
        if ($elapsed % 15 -eq 0) {
            Log-Warn "$ServiceName hala bekliyor... ($elapsed / $TimeoutSeconds s)"
        }
    }
    Log-Warn "$ServiceName $TimeoutSeconds s icinde yanit vermedi, devam ediliyor..."
}

# ─── Helper: Docker container healthy check ───────────────────────────────────
function Wait-ForDockerHealthy {
    param([string]$ContainerName, [int]$TimeoutSeconds = 120)
    $elapsed = 0
    while ($elapsed -lt $TimeoutSeconds) {
        $status = (docker inspect --format='{{.State.Health.Status}}' $ContainerName 2>$null)
        if ($status -eq "healthy") { Log-OK "$ContainerName saglıklı"; return }
        Start-Sleep -Seconds 5
        $elapsed += 5
        if ($elapsed % 20 -eq 0) {
            Log-Warn "$ContainerName bekliyor (durum: $status, $elapsed/$TimeoutSeconds s)"
        }
    }
    Log-Warn "$ContainerName $TimeoutSeconds s icinde healthy olmadi, devam ediliyor..."
}

# ─── Helper: Start Maven service in background ────────────────────────────────
function Start-MavenService {
    param([string]$ServiceName, [string]$ServiceDir)
    $null = New-Item -ItemType Directory -Path $LOG_DIR -Force
    $logFile = Join-Path $LOG_DIR "${ServiceName}.log"
    Log-Info "$ServiceName baslatiliyor..."

    $proc = Start-Process `
        -FilePath "cmd.exe" `
        -ArgumentList "/c", "cd /d `"$ServiceDir`" && mvn spring-boot:run --no-transfer-progress -q > `"$logFile`" 2>&1" `
        -WindowStyle Hidden `
        -PassThru

    $script:javaPids.Add($proc.Id)
    Log-OK "$ServiceName arka plana alindi (PID: $($proc.Id)) >> logs\${ServiceName}.log"
    return $proc
}

# ═══════════════════════════════════════════════════════════════════════════════
Clear-Host
Write-Host @"

  ████████╗███████╗██╗     ███████╗ ██████╗ ██████╗     ██████╗██████╗ ███╗   ███╗
     ██╔══╝██╔════╝██║     ██╔════╝██╔════╝██╔═══██╗   ██╔════╝██╔══██╗████╗ ████║
     ██║   █████╗  ██║     █████╗  ██║     ██║   ██║   ██║     ██████╔╝██╔████╔██║
     ██║   ██╔══╝  ██║     ██╔══╝  ██║     ██║   ██║   ██║     ██╔══██╗██║╚██╔╝██║
     ██║   ███████╗███████╗███████╗╚██████╗╚██████╔╝   ╚██████╗██║  ██║██║ ╚═╝ ██║
     ╚═╝   ╚══════╝╚══════╝╚══════╝ ╚═════╝ ╚═════╝    ╚═════╝╚═╝  ╚═╝╚═╝     ╚═╝
                         Mikroservis Full-Stack Baslatma Scripti
"@ -ForegroundColor Cyan

# ─────────────────────────────────────────────────────────────────────────────
# ADIM 1 — Docker Altyapisi
# ─────────────────────────────────────────────────────────────────────────────
Log-Step "1/5  Docker Altyapisi (PostgreSQL x9, Kafka, Keycloak, Redis)"

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Log-Error "Docker bulunamadi! Docker Desktop calistigindan emin olun ve tekrar deneyin."
    exit 1
}

Log-Info "docker-compose up -d calistiriliyor..."
Push-Location $DOCKER_DIR
docker-compose up -d
Pop-Location

Log-Info "Kritik veritabanlari icin healthy durumu bekleniyor..."
$dbs = @("customer-db","telco-product-db","telco-order-db","subscription-db","usage-db","billing-db","payment-db","notification-db","ticket-db")
foreach ($db in $dbs) { Wait-ForDockerHealthy -ContainerName $db -TimeoutSeconds 120 }

Log-Info "Kafka broker icin 20 saniye bekleniyor..."
Start-Sleep -Seconds 20
Log-OK "Altyapi hazir!"

# ─────────────────────────────────────────────────────────────────────────────
# ADIM 2 — Minikube / Kubernetes
# ─────────────────────────────────────────────────────────────────────────────
Log-Step "2/5  Minikube + customer-service K8s"

function Start-MinikubeWithTimeout {
    param([int]$TimeoutSeconds = 180)
    # Run minikube start as a background job so we can time-limit it
    $job = Start-Job -ScriptBlock { minikube start --driver=docker 2>&1 }
    $elapsed = 0
    while ($elapsed -lt $TimeoutSeconds) {
        if ($job.State -ne "Running") { break }
        Start-Sleep -Seconds 5
        $elapsed += 5
        if ($elapsed % 30 -eq 0) {
            Log-Warn "minikube start devam ediyor... ($elapsed / $TimeoutSeconds s)"
        }
    }
    if ($job.State -eq "Running") {
        Stop-Job  $job -ErrorAction SilentlyContinue
        Remove-Job $job -ErrorAction SilentlyContinue
        return $false   # timed out
    }
    $output = Receive-Job $job 2>&1
    Remove-Job $job -ErrorAction SilentlyContinue
    # Check for SSH key / recreate errors in output
    if ($output -match "Error writing keys|Unable to write file|StartHost failed") {
        return $false
    }
    return ($LASTEXITCODE -eq 0 -or $output -match "Done!")
}

if (Get-Command minikube -ErrorAction SilentlyContinue) {
    $mkStatus = minikube status --format='{{.Host}}' 2>$null
    $k8sReady = $false

    if ($mkStatus -eq "Running") {
        Log-OK "Minikube zaten calisiyor"
        $k8sReady = $true
    } else {
        Log-Info "Minikube baslatiliyor (maks 3 dakika)..."

        $started = Start-MinikubeWithTimeout -TimeoutSeconds 180

        if (-not $started) {
            Log-Warn "minikube start basarisiz veya takildi. Eski profil temizlenip tekrar deneniyor..."
            try {
                # Clean up broken profile — this fixes the SSH key write error
                $delJob = Start-Job -ScriptBlock { minikube delete 2>&1 }
                Wait-Job $delJob -Timeout 60 | Out-Null
                Remove-Job $delJob -ErrorAction SilentlyContinue
                Log-Info "Eski profil silindi. Yeniden baslatiliyor..."
                $started = Start-MinikubeWithTimeout -TimeoutSeconds 180
            } catch {
                $started = $false
            }
        }

        if ($started) {
            $k8sReady = $true
            Log-OK "Minikube hazir"
        } else {
            Log-Warn "Minikube baslanamadi - K8s adimi atlaniyor, diger servisler devam ediyor."
            Log-Warn "Manuel duzeltme icin: minikube delete ; minikube start --driver=docker"
        }
    }

    if ($k8sReady) {
        # ── Docker image build + minikube yükleme ───────────────────────────
        $customerSvcDir = Join-Path $MS_ROOT "customer-service"
        $imageTag        = "telecom-crm/customer-service:latest"
        $rebuildFlag     = $args -contains "--rebuild"

        # Image minikube içinde mevcut mu kontrol et
        $imageInMk = minikube image ls 2>$null | Select-String $imageTag

        if (-not $imageInMk -or $rebuildFlag) {
            Log-Step "2b/5  customer-service Docker image build + minikube load"

            Log-Info "Docker image build ediliyor: $imageTag  (bu ~3-5 dk surebilir)..."
            docker build -t $imageTag "$customerSvcDir" 2>&1 | ForEach-Object {
                if ($_ -match "error|ERRO" -and $_ -notmatch "WARNING") { Log-Error $_ }
                elseif ($_ -match "Step |\-\-\->|Successfully")          { Log-Info  $_ }
            }
            $buildOk = ($LASTEXITCODE -eq 0)
            if (-not $buildOk) {
                Log-Error "Docker build basarisiz - customer-service K8s adimi atlaniyor."
            } else {
                Log-OK "Docker image hazir: $imageTag"
                Log-Info "Image minikube'a yukleniyor (bu 1-3 dk surebilir)..."
                minikube image load $imageTag 2>&1 | ForEach-Object { Log-Info $_ }
                Log-OK "Image minikube'a yuklendi"
            }
        } else {
            $buildOk = $true
            Log-OK "Image minikube'da mevcut, build atlaniyor. (Yeniden build icin: .\start-all.ps1 --rebuild)"
        }

        if ($buildOk) {
            # ── K8s manifest'lerini uygula ───────────────────────────────────────
            Log-Info "K8s manifest'leri uygulanıyor: $K8S_DIR"
            kubectl apply -f "$K8S_DIR" 2>&1 | ForEach-Object { Log-Info $_ }

            Log-Info "customer-service pod Ready bekliyor (maks 3 dk)..."
            $waitResult = kubectl wait pod `
                --for=condition=ready `
                --selector=app.kubernetes.io/name=customer-service `
                --namespace=telecom-crm `
                --timeout=180s 2>&1
            if ($LASTEXITCODE -eq 0) {
                Log-OK "customer-service K8s pod hazir"
            } else {
                Log-Warn "customer-service pod hazir olmadi (kubectl wait: $waitResult) - devam ediliyor"
            }

            # ── port-forward: localhost:8081 → customer-service:8081 ─────────────
            Log-Info "customer-service port-forward baslatiliyor (localhost:8081)..."
            $pfJob = Start-Job -ScriptBlock {
                while ($true) {
                    kubectl port-forward service/customer-service 8081:8081 -n telecom-crm 2>&1
                    Start-Sleep -Seconds 3   # bağlantı kopunca kısa bekleyip yeniden dene
                }
            }
            $script:javaPids.Add($pfJob.Id)   # Ctrl+C ile cleanup sırasında job da durdurulsun
            Log-OK "customer-service Swagger: http://localhost:8081/swagger-ui/index.html"
        }
    }
} else {
    Log-Warn "minikube bulunamadi - K8s adimi atlandi"
}

# ─────────────────────────────────────────────────────────────────────────────
# ADIM 3 — Cekirdek Servisler (Eureka → Config → Gateway)
# ─────────────────────────────────────────────────────────────────────────────
Log-Step "3/5  Cekirdek Servisler  (Eureka -> Config -> Gateway)"

Start-MavenService "eureka-server"  (Join-Path $MS_ROOT "eureka-server")
Wait-ForPort "Eureka Server"  8761  180

Start-MavenService "config-server"  (Join-Path $MS_ROOT "config-server")
Wait-ForPort "Config Server"  8888  180

Start-MavenService "gateway-server" (Join-Path $MS_ROOT "gateway-server")
Wait-ForPort "API Gateway"    9000  180

# ─────────────────────────────────────────────────────────────────────────────
# ADIM 4 — Is Servisleri (arka planda)
# ─────────────────────────────────────────────────────────────────────────────
Log-Step "4/5  Is Servisleri"

$businessServices = @(
    [ordered]@{ Name = "identity-service";        Dir = "identity-service"        },
    [ordered]@{ Name = "product-catalog-service"; Dir = "product-catalog-service" },
    [ordered]@{ Name = "order-service";           Dir = "order-service"           },
    [ordered]@{ Name = "subscription-service";    Dir = "subscription-service"    },
    [ordered]@{ Name = "usage-service";           Dir = "usage-service"           },
    [ordered]@{ Name = "billing-service";         Dir = "billing-service"         },
    [ordered]@{ Name = "payment-service";         Dir = "payment-service"         },
    [ordered]@{ Name = "notification-service";    Dir = "notification-service"    },
    [ordered]@{ Name = "ticket-service";          Dir = "ticket-service"          },
    [ordered]@{ Name = "bff-server";              Dir = "bff-server"              }
)

foreach ($svc in $businessServices) {
    Start-MavenService $svc.Name (Join-Path $MS_ROOT $svc.Dir)
    Start-Sleep -Seconds 2
}

Log-OK "Tum is servisleri arka planda baslatildi"
Write-Host ""
Write-Host "  Log dosyalari: $LOG_DIR" -ForegroundColor DarkCyan

# ─────────────────────────────────────────────────────────────────────────────
# ADIM 5 — Angular Frontend (on planda)
# ─────────────────────────────────────────────────────────────────────────────
Log-Step "5/5  Angular Frontend  (on planda - Ctrl+C ile her sey kapanir)"

if (-not (Test-Path $FRONTEND)) {
    Log-Error "Frontend dizini bulunamadi: $FRONTEND"
    Invoke-Cleanup; exit 1
}

Push-Location $FRONTEND

if (-not (Test-Path (Join-Path $FRONTEND "node_modules"))) {
    Log-Info "npm install calistiriliyor..."
    npm install
}

Log-OK "Angular http://localhost:4200 adresinde baslatiliyor..."
Write-Host "  [ Ctrl+C ile tum sistem durdurulur ]`n" -ForegroundColor Magenta

try {
    npm start
} finally {
    Pop-Location
    Invoke-Cleanup
}


