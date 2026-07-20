#!/usr/bin/env bash
# ============================================================
#  Teleco CRM Microservices - Full Stack Teardown Script
#  Mac / Linux (bash)
#  Kullanim: bash teardown-all.sh
#  Bu script sistemi tamamen sifirlar.
# ============================================================

set -u

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

clear
echo -e "${RED}"
echo "  ████████╗███████╗ █████╗ ██████╗ ██████╗  ██████╗ ██╗    ██╗███╗   ██╗"
echo "  ╚══██╔══╝██╔════╝██╔══██╗██╔══██╗██╔══██╗██╔═══██╗██║    ██║████╗  ██║"
echo "     ██║   █████╗  ███████║██████╔╝██║  ██║██║   ██║██║ █╗ ██║██╔██╗ ██║"
echo "     ██║   ██╔══╝  ██╔══██║██╔══██╗██║  ██║██║   ██║██║███╗██║██║╚██╗██║"
echo "     ██║   ███████╗██║  ██║██║  ██║██████╔╝╚██████╔╝╚███╔███╔╝██║ ╚████║"
echo "     ╚═╝   ╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝╚═════╝  ╚═════╝  ╚══╝╚══╝ ╚═╝  ╚═══╝"
echo "                         Sistemi Tamamen Sifirlama"
echo -e "${NC}"

echo -e "${YELLOW}\nDIKKAT: Bu islem tum veritabanlarini, Docker hacimlerini (volumes) ve Minikube cluster'ini silecektir!${NC}"
read -p "$(echo -e ${CYAN}"Devam etmek istediginize emin misiniz? (E/H) "${NC})" -n 1 -r
echo
if [[ ! $REPLY =~ ^[Ee]$ ]]
then
    echo -e "${GREEN}Islem iptal edildi.${NC}"
    exit 0
fi

# 1. Stop Everything
echo -e "${CYAN}\n[1/4] Calisan servisler durduruluyor...${NC}"
bash "$(dirname "${BASH_SOURCE[0]}")/stop-all.sh" --all

# 2. Docker Teardown
echo -e "${CYAN}\n[2/4] Docker container'lari ve veritabani hacimleri (volumes) siliniyor...${NC}"
if command -v docker >/dev/null 2>&1; then
    cd "${ROOT}/docker-telcom"
    docker-compose down -v
    cd "${ROOT}"
    echo -e "${GREEN}  [ OK ] Docker volumes silindi.${NC}"
else
    echo -e "${YELLOW}  [WARN] Docker bulunamadi, atlaniyor.${NC}"
fi

# 3. Minikube Teardown
echo -e "${CYAN}\n[3/4] Minikube cluster siliniyor...${NC}"
if command -v minikube >/dev/null 2>&1; then
    minikube delete
    echo -e "${GREEN}  [ OK ] Minikube cluster silindi.${NC}"
else
    echo -e "${YELLOW}  [WARN] Minikube bulunamadi, atlaniyor.${NC}"
fi

# 4. Clean Logs
echo -e "${CYAN}\n[4/4] Log dosyalari temizleniyor...${NC}"
if [ -d "${ROOT}/logs" ]; then
    rm -rf "${ROOT}/logs/"*
    echo -e "${GREEN}  [ OK ] Loglar temizlendi.${NC}"
fi

echo -e "\n${GRAY}============================================================${NC}"
echo -e "${GREEN}  TEARDOWN TAMAMLANDI. Sistem sifirlandi.${NC}"
echo -e "${GRAY}============================================================\n${NC}"
