#!/usr/bin/env bash
# ============================================================
#  Teleco CRM Microservices - Full Stack Startup Script
#  Mac / Linux (bash)
#  Kullanim: bash start-all.sh
#  Ctrl+C   -> tum arka plan Java processleri temizce kapanir
# ============================================================

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.."  && pwd)"
MS_ROOT="${ROOT}/microservices"
DOCKER_DIR="${ROOT}/docker-telcom"
FRONTEND="${ROOT}/frontend"
K8S_DIR="${MS_ROOT}/customer-service/k8s"
LOG_DIR="${ROOT}/logs"

# Array to track background PIDs
JAVA_PIDS=()

# ─── Colour helpers ──────────────────────────────────────────────────────────
RED='\033[0;31m';  GREEN='\033[0;32m';  YELLOW='\033[1;33m'
CYAN='\033[0;36m'; WHITE='\033[1;37m';  MAGENTA='\033[0;35m'
GRAY='\033[0;90m'; NC='\033[0m'

log_info()  { echo -e "${CYAN}"  [INFO]  $1"${NC}"; }
log_ok()    { echo -e "${GREEN}"  [ OK ]  $1"${NC}"; }
log_warn()  { echo -e "${YELLOW}"  [WARN]  $1"${NC}"; }
log_error() { echo -e "${RED}"  [ERR ]  $1"${NC}"; }
log_step()  {
    echo -e "${GRAY}"
    echo '══════════════════════════════════════════════════'
    echo -e "${WHITE}"  ADIM: $1"${NC}"
    echo -e "${GRAY}"\'══════════════════════════════════════════════════\'"${NC}"
}

# ─── Cleanup / Trap ──────────────────────────────────────────────────────────
cleanup() {
    echo -e "\n\n"${MAGENTA}""" Kapatma sinyali alindi. Tum servisler durduruluyor..."${NC}"
    for pid in "${JAVA_PIDS[@]:-}"; do
        if [[ -n "${pid}" ]] && kill -0 "${pid}" 2>/dev/null; then
            log_warn "Process durduruluyor: PID ${pid}"
            kill -TERM "${pid}" 2>/dev/null || true
            sleep 1
            kill -KILL "${pid}" 2>/dev/null || true
        fi
    done

    # Kill any Java/mvn processes listening on known ports
    for port in 8761 8888 8222 8081 8082 8083 8084 8085 8086 8087 8088 8089 8090 4200; do
        pid_on_port=$(lsof -ti tcp:${port} 2>/dev/null || true)
        if [[ -n "${pid_on_port}" ]]; then
            kill -TERM ${pid_on_port} 2>/dev/null || true
            log_warn "Port ${port} serbest birakildi (PID: ${pid_on_port})"
        fi
    done

    log_ok "Temizleme tamamlandi. Gorusuruz!"
    exit 0
}

trap cleanup SIGINT SIGTERM EXIT

# ─── Helper: Wait for TCP port ───────────────────────────────────────────────
wait_for_port() {
    local name="$1"
    local port="$2"
    local timeout="${3:-180}"
    local elapsed=0
    log_info "${name}" port "${port}" acilmayi bekliyor..."
    while (( elapsed < timeout )); do
        if nc -z localhost "${port}" 2>/dev/null; then
            log_ok "${name}" hazir (port "${port}" acik)"
            return 0
        fi
        sleep 3
        (( elapsed += 3 ))
        if (( elapsed % 15 == 0 )); then
            log_warn "${name}" hala bekliyor... ("${elapsed}"/"${timeout}" s)"
        fi
    done
    log_warn "${name}" "${timeout}" s icinde yanit vermedi, devam ediliyor..."
}

# ─── Helper: Docker healthy check ────────────────────────────────────────────
wait_for_docker_healthy() {
    local name="$1"
    local timeout="${2:-120}"
    local elapsed=0
    while (( elapsed < timeout )); do
        status=$(docker inspect --format='{{.State.Health.Status}}' "${name}" 2>/dev/null || echo unknown)
        if [[ "${status}" == "healthy" ]]; then
            log_ok "${name}" saglikli"
            return 0
        fi
        sleep 5
        (( elapsed += 5 ))
        if (( elapsed % 20 == 0 )); then
            log_warn "${name}" bekliyor (durum: "${status}", "${elapsed}"/"${timeout}" s)"
        fi
    done
    log_warn "${name}" "${timeout}" s icinde healthy olmadi, devam ediliyor..."
}

# ─── Helper: Start Maven service in background ───────────────────────────────
start_maven_service() {
    local name="$1"
    local dir="$2"
    mkdir -p "${LOG_DIR}"
    local logfile="${LOG_DIR}/.log"
    log_info "${name}" baslatiliyor..."
    ( cd "${dir}" && mvn spring-boot:run --no-transfer-progress -q > "${logfile}" 2>&1 ) &
    local pid=$!
    JAVA_PIDS+=( "${pid}" )
    log_ok "${name}" arka plana alindi (PID: "${pid}") >> logs/"${name}".log"
}

# ═══════════════════════════════════════════════════════════════════════════════
clear
cat << 'EOF'

  ████████╗███████╗██╗     ███████╗ ██████╗ ██████╗     ██████╗██████╗ ███╗   ███╗
     ██╔══╝██╔════╝██║     ██╔════╝██╔════╝██╔═══██╗   ██╔════╝██╔══██╗████╗ ████║
     ██║   █████╗  ██║     █████╗  ██║     ██║   ██║   ██║     ██████╔╝██╔████╔██║
     ██║   ██╔══╝  ██║     ██╔══╝  ██║     ██║   ██║   ██║     ██╔══██╗██║╚██╔╝██║
     ██║   ███████╗███████╗███████╗╚██████╗╚██████╔╝   ╚██████╗██║  ██║██║ ╚═╝ ██║
     ╚═╝   ╚══════╝╚══════╝╚══════╝ ╚═════╝ ╚═════╝    ╚═════╝╚═╝  ╚═╝╚═╝     ╚═╝
                       Mikroservis Full-Stack Baslatma Scripti (Mac/Linux)
EOF

# ─────────────────────────────────────────────────────────────────────────────
# ADIM 1 — Docker Altyapisi
# ─────────────────────────────────────────────────────────────────────────────
log_step "1/5  Docker Altyapisi (PostgreSQL x9, Kafka, Keycloak, Redis)"

if ! command -v docker &>/dev/null; then
    log_error "Docker bulunamadi! Docker Desktop calistigindan emin olun."
    exit 1
fi

log_info "docker-compose up -d calistiriliyor..."
cd "${DOCKER_DIR}"
docker-compose up -d
cd "${ROOT}"

log_info "Kritik veritabanlari icin healthy durumu bekleniyor..."
for db in customer-db telco-product-db telco-order-db subscription-db usage-db billing-db payment-db notification-db ticket-db; do
    wait_for_docker_healthy "${db}" 120
done

log_info "Kafka broker icin 20 saniye bekleniyor..."
sleep 20
log_ok "Altyapi hazir!"

# ─────────────────────────────────────────────────────────────────────────────
# ADIM 2 — Minikube / Kubernetes
# ─────────────────────────────────────────────────────────────────────────────
log_step "2/5  Minikube + customer-service K8s"

if command -v minikube &>/dev/null; then
    mk_status=$(minikube status --format='{{.Host}}' 2>/dev/null || echo Stopped)
    if [[ "${mk_status}" != "Running" ]]; then
        log_info "Minikube baslatiliyor..."
        minikube start --driver=docker
    else
        log_ok "Minikube zaten calisiyor"
    fi

    log_info "K8s manifest'leri uygulanıyor: ${K8S_DIR}"
    kubectl apply -f "${K8S_DIR}"

    log_info "customer-service pod Ready bekliyor (maks 3 dk)..."
    kubectl wait pod \
        --for=condition=ready \
        --selector=app=customer-service \
        --namespace=default \
        --timeout=180s
    log_ok "customer-service K8s pod hazir"
else
    log_warn "minikube bulunamadi — K8s adimi atlandi"
fi

# ─────────────────────────────────────────────────────────────────────────────
# ADIM 3 — Cekirdek Servisler
# ─────────────────────────────────────────────────────────────────────────────
log_step "3/5  Cekirdek Servisler  (Eureka → Config → Gateway)"

start_maven_service "eureka-server"  "${MS_ROOT}/eureka-server"
wait_for_port       "Eureka Server"  8761  180

start_maven_service "config-server"  "${MS_ROOT}/config-server"
wait_for_port       "Config Server"  8888  180

start_maven_service "gateway-server" "${MS_ROOT}/gateway-server"
wait_for_port       "API Gateway"    8222  180

# ─────────────────────────────────────────────────────────────────────────────
# ADIM 4 — Is Servisleri
# ─────────────────────────────────────────────────────────────────────────────
log_step "4/5  Is Servisleri"

services=(
    "identity-service"
    "product-catalog-service"
    "order-service"
    "subscription-service"
    "usage-service"
    "billing-service"
    "payment-service"
    "notification-service"
    "ticket-service"
    "bff-server"
)

for svc in "${services[@]}"; do
    start_maven_service "${svc}" "${MS_ROOT}/"
    sleep 2
done

log_ok "Tum is servisleri arka planda baslatildi"
echo -e "  Log dosyalari: "${LOG_DIR}"${NC}"

# ─────────────────────────────────────────────────────────────────────────────
# ADIM 5 — Angular Frontend (on planda)
# ─────────────────────────────────────────────────────────────────────────────
log_step "5/5  Angular Frontend  (on planda — Ctrl+C ile her sey kapanir)"

if [[ ! -d "${FRONTEND}" ]]; then
    log_error "Frontend dizini bulunamadi: ${FRONTEND}"
    exit 1
fi

cd "${FRONTEND}"

if [[ ! -d node_modules ]]; then
    log_info "npm install calistiriliyor..."
    npm install
fi

log_ok "Angular http://localhost:4200 adresinde baslatiliyor..."
echo -e "${MAGENTA}"  [ Ctrl+C ile tum sistem durdurulur ]
"${NC}"

npm start
