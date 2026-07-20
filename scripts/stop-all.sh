#!/usr/bin/env bash
# ============================================================
#  Teleco CRM Microservices - Full Stack Stop Script
#  Mac / Linux (bash)
#  Kullanim: bash stop-all.sh
#  Secenekler:
#    --docker    Docker container'larini da durdurur
#    --all       Tum seyleri durdurur (Java + Docker + Minikube)
#    --minikube  Minikube'u da durdurur
# ============================================================

set -u

DOCKER=false
MINIKUBE=false
ALL=false

for arg in "$@"; do
    case $arg in
        --docker) DOCKER=true ;;
        --minikube) MINIKUBE=true ;;
        --all) ALL=true ;;
    esac
done

if [ "$ALL" = true ]; then
    DOCKER=true
    MINIKUBE=true
fi

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.."  && pwd)"

# ─── Colour helpers ──────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
MAGENTA='\033[0;35m'
GRAY='\033[0;90m'
NC='\033[0m'

log_info()  { echo -e "${CYAN}  [INFO]  $1${NC}"; }
log_ok()    { echo -e "${GREEN}  [ OK ]  $1${NC}"; }
log_warn()  { echo -e "${YELLOW}  [WARN]  $1${NC}"; }
log_error() { echo -e "${RED}  [ERR ]  $1${NC}"; }
log_step()  {
    echo -e "\n${GRAY}══════════════════════════════════════════════════${NC}"
    echo -e "${WHITE}  $1${NC}"
    echo -e "${GRAY}══════════════════════════════════════════════════${NC}"
}

clear
echo -e "${MAGENTA}"
echo "  ████████╗███████╗██╗     ███████╗ ██████╗ ██████╗     ██████╗██████╗ ███╗   ███╗"
echo "     ██╔══╝██╔════╝██║     ██╔════╝██╔════╝██╔═══██╗   ██╔════╝██╔══██╗████╗ ████║"
echo "     ██║   █████╗  ██║     █████╗  ██║     ██║   ██║   ██║     ██████╔╝██╔████╔██║"
echo "     ██║   ██╔══╝  ██║     ██╔══╝  ██║     ██║   ██║   ██║     ██╔══██╗██║╚██╔╝██║"
echo "     ██║   ███████╗███████╗███████╗╚██████╗╚██████╔╝   ╚██████╗██║  ██║██║ ╚═╝ ██║"
echo "     ╚═╝   ╚══════╝╚══════╝╚══════╝ ╚═════╝ ╚═════╝    ╚═════╝╚═╝  ╚═╝╚═╝     ╚═╝"
echo "                         Mikroservis Full-Stack Durdurma Scripti"
echo -e "${NC}"

kill_port() {
    local port=$1
    local name=$2
    local pid=$(lsof -t -i:$port 2>/dev/null || true)
    if [ ! -z "$pid" ]; then
        kill -9 $pid 2>/dev/null || true
        log_ok "$name durduruldu (port $port, PID: $pid)"
    else
        log_warn "$name zaten kapali (port $port)"
    fi
}

# ─────────────────────────────────────────────────────────────────────────────
# ADIM 1 — Angular Frontend (port 4200)
# ─────────────────────────────────────────────────────────────────────────────
log_step "1/5  Angular Frontend durduruluyor (port 4200)"
kill_port 4200 "Angular"

# ─────────────────────────────────────────────────────────────────────────────
# ADIM 2 — Java / Maven Servisleri (bilinen portlar)
# ─────────────────────────────────────────────────────────────────────────────
log_step "2/5  Java/Maven servisleri durduruluyor"

kill_port 9090 "BFF Server"
kill_port 9001 "Identity Service"
kill_port 8088 "Notification Service"
kill_port 9010 "Ticket Service"
kill_port 9008 "Payment Service"
kill_port 9006 "Usage Service"
kill_port 8086 "Subscription Service"
kill_port 8085 "Order Service"
kill_port 8083 "Product Catalog Service"
kill_port 9000 "API Gateway"
kill_port 8888 "Config Server"
kill_port 8761 "Eureka Server"

log_info "Kalan mvn/java processleri temizleniyor..."
pkill -9 -f "mvn|java" 2>/dev/null || true
log_ok "Kalan java/mvn processleri temizlendi"

# ─────────────────────────────────────────────────────────────────────────────
# ADIM 3 — kubectl port-forward (port 8081)
# ─────────────────────────────────────────────────────────────────────────────
log_step "3/5  kubectl port-forward durduruluyor (port 8081)"
kill_port 8081 "port-forward"
pkill -9 -f "kubectl port-forward" 2>/dev/null || true

# ─────────────────────────────────────────────────────────────────────────────
# ADIM 4 — Docker Container'lar
# ─────────────────────────────────────────────────────────────────────────────
log_step "4/5  Docker container'lari"
if [ "$DOCKER" = true ]; then
    if command -v docker >/dev/null 2>&1; then
        log_info "docker-compose down calistiriliyor..."
        cd "${ROOT}/docker-telcom"
        docker-compose down
        cd "${ROOT}"
        log_ok "Docker container'lari durduruldu"
    else
        log_warn "Docker bulunamadi, atlaniyor"
    fi
else
    log_warn "Docker container'lari calistirilmaya devam ediyor. Durdurmak icin: ./stop-all.sh --docker"
fi

# ─────────────────────────────────────────────────────────────────────────────
# ADIM 5 — Minikube
# ─────────────────────────────────────────────────────────────────────────────
log_step "5/5  Minikube"
if [ "$MINIKUBE" = true ]; then
    if command -v minikube >/dev/null 2>&1; then
        log_info "minikube stop calistiriliyor..."
        minikube stop
        log_ok "Minikube durduruldu"
    else
        log_warn "minikube bulunamadi, atlaniyor"
    fi
else
    log_warn "Minikube calistirilmaya devam ediyor. Durdurmak icin: ./stop-all.sh --minikube"
fi

echo ""
echo -e "${GRAY}══════════════════════════════════════════════════${NC}"
echo -e "${GREEN}  Durdurma tamamlandi!${NC}"
echo ""
echo -e "${CYAN}  Yeniden baslatmak icin  : ./start-all.sh${NC}"
echo -e "${CYAN}  Docker'i da durdurmak   : ./stop-all.sh --docker${NC}"
echo -e "${CYAN}  Minikube'u da durdurmak : ./stop-all.sh --minikube${NC}"
echo -e "${CYAN}  Her seyi durdurmak      : ./stop-all.sh --all${NC}"
echo -e "${GRAY}══════════════════════════════════════════════════${NC}"
echo ""
