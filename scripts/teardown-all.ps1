#Requires -Version 5.1
# ============================================================
#  Teleco CRM Microservices - Full Stack Teardown Script
#  Windows PowerShell
#  Kullanim: .\teardown-all.ps1
#  Bu script sistemi tamamen sifirlar.
# ============================================================

$ErrorActionPreference = "Continue"
$ROOT = (Get-Item $PSScriptRoot).Parent.FullName

Clear-Host
Write-Host @"

  ████████╗███████╗ █████╗ ██████╗ ██████╗  ██████╗ ██╗    ██╗███╗   ██╗
  ╚══██╔══╝██╔════╝██╔══██╗██╔══██╗██╔══██╗██╔═══██╗██║    ██║████╗  ██║
     ██║   █████╗  ███████║██████╔╝██║  ██║██║   ██║██║ █╗ ██║██╔██╗ ██║
     ██║   ██╔══╝  ██╔══██║██╔══██╗██║  ██║██║   ██║██║███╗██║██║╚██╗██║
     ██║   ███████╗██║  ██║██║  ██║██████╔╝╚██████╔╝╚███╔███╔╝██║ ╚████║
     ╚═╝   ╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝╚═════╝  ╚═════╝  ╚══╝╚══╝ ╚═╝  ╚═══╝
                         Sistemi Tamamen Sifirlama
"@ -ForegroundColor Red

Write-Host "`nDIKKAT: Bu islem tum veritabanlarini, Docker hacimlerini (volumes) ve Minikube cluster'ini silecektir!" -ForegroundColor Yellow
Write-Host "Devam etmek istediginize emin misiniz? (E/H)" -ForegroundColor Cyan
$response = Read-Host
if ($response -notmatch "^[eE]$") {
    Write-Host "Islem iptal edildi." -ForegroundColor Green
    exit 0
}

# 1. Stop Everything
Write-Host "`n[1/4] Calisan servisler durduruluyor..." -ForegroundColor Cyan
& "$PSScriptRoot\stop-all.ps1" -All

# 2. Docker Teardown
Write-Host "`n[2/4] Docker container'lari ve veritabani hacimleri (volumes) siliniyor..." -ForegroundColor Cyan
$DOCKER_DIR = Join-Path $ROOT "docker-telcom"
if (Get-Command docker -ErrorAction SilentlyContinue) {
    Push-Location $DOCKER_DIR
    docker-compose down -v
    Pop-Location
    Write-Host "  [ OK ] Docker volumes silindi." -ForegroundColor Green
} else {
    Write-Host "  [WARN] Docker bulunamadi, atlaniyor." -ForegroundColor Yellow
}

# 3. Minikube Teardown
Write-Host "`n[3/4] Minikube cluster siliniyor..." -ForegroundColor Cyan
if (Get-Command minikube -ErrorAction SilentlyContinue) {
    minikube delete
    Write-Host "  [ OK ] Minikube cluster silindi." -ForegroundColor Green
} else {
    Write-Host "  [WARN] Minikube bulunamadi, atlaniyor." -ForegroundColor Yellow
}

# 4. Clean Logs
Write-Host "`n[4/4] Log dosyalari temizleniyor..." -ForegroundColor Cyan
$LOG_DIR = Join-Path $ROOT "logs"
if (Test-Path $LOG_DIR) {
    Remove-Item -Path "$LOG_DIR\*" -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "  [ OK ] Loglar temizlendi." -ForegroundColor Green
}

Write-Host "`n============================================================" -ForegroundColor DarkGray
Write-Host "  TEARDOWN TAMAMLANDI. Sistem sifirlandi." -ForegroundColor Green
Write-Host "============================================================`n" -ForegroundColor DarkGray
