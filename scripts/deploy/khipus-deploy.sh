#!/usr/bin/env bash
# ============================================================================
# khipus-deploy.sh  -  Instalacion (systemd) y despliegue de KHIPUS (TERDEMOL)
# Script UNICO y autocontenido. Se ejecuta EN EL SERVIDOR (usuario terdemol).
#
#   ./khipus-deploy.sh install            Instala/actualiza el servicio systemd
#                                          (detecta y pregunta JAVA_HOME, MySQL...)
#   ./khipus-deploy.sh deploy <version>   Despliega un release ya subido por scp
#                                          Ej:  ./khipus-deploy.sh deploy 6.0.116
#   ./khipus-deploy.sh rollback [ear]     Vuelve al ear anterior (ultimo backup
#                                          o el .ear indicado)
#   ./khipus-deploy.sh rollback --list    Lista los backups disponibles
#   ./khipus-deploy.sh status             Estado del servicio + health check
#
# Flujo tipico la primera vez:
#   1) ./khipus-deploy.sh install
#   2) scp v6.0.116.zip terdemol@servidor:~/downloads/   (desde tu maquina)
#   3) ./khipus-deploy.sh deploy 6.0.116
#
# Pide la clave de sudo cuando la necesita (la cachea una vez). No modifica sudoers.
# Conserva TODOS los backups (ears y dumps); la limpieza es manual.
# ============================================================================
set -euo pipefail

# ============================================================================
# CONFIG  -  ajusta si algo difiere en el servidor
# ============================================================================
JBOSS_HOME="/usr/local/jboss"
JBOSS_DEPLOY_DIR="${JBOSS_HOME}/server/default/deploy"
JBOSS_BIN_DIR="${JBOSS_HOME}/bin"
JBOSS_USER="jboss"                 # usuario propietario del ear / que corre JBoss
EAR_NAME="khipus.ear"
SERVICE_NAME="khipus-jboss"        # nombre del servicio systemd
JBOSS_BIND_ADDR="0.0.0.0"          # run.sh -b <esto>

# Valores confirmados de ESTE servidor (defaults del 'install'; puedes cambiarlos al ejecutar)
JAVA_HOME_DEFAULT="/opt/jdk"       # JDK 1.8.0_321 (el que usa el usuario jboss)
MYSQL_SERVICE_DEFAULT="mysql"      # servicio systemd de la BD (mysql.service)

DOWNLOADS_DIR="${HOME}/downloads"          # donde llega el zip por scp
BACKUP_EAR_DIR="${HOME}/backups/ears"      # respaldos del ear anterior (TODOS)
LOG_DIR="${HOME}/backups/deploy-logs"      # logs de cada operacion
DB_DUMP_SCRIPT="${HOME}/terdemol/dump_terdemol.sh"   # backup de BD antes del deploy

HEALTH_URL="http://terdemol.net:8421/khipus/"
HEALTH_TIMEOUT_SECONDS=300
HEALTH_INTERVAL_SECONDS=10

UNIT_FILE="/etc/systemd/system/${SERVICE_NAME}.service"

# ============================================================================
# Utilidades
# ============================================================================
log()  { echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"; }
err()  { echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: $*" >&2; }
die()  { err "$*"; exit 1; }

# Cachea la credencial de sudo una vez y la mantiene viva mientras corre el script.
sudo_prime() {
    log "Solicitando credencial sudo (una vez)..."
    sudo -v || die "sudo requerido"
    ( while true; do sudo -n true; sleep 50; kill -0 "$$" 2>/dev/null || exit; done ) 2>/dev/null &
}

# Pregunta con valor por defecto:  ask VAR "Pregunta" "default"
ask() {
    local __var="$1" __msg="$2" __def="${3:-}" __ans
    read -r -p "${__msg} [${__def}]: " __ans
    printf -v "$__var" '%s' "${__ans:-$__def}"
}

# ============================================================================
# Control de JBoss (via systemd)
# ============================================================================
service_installed() { [ -f "$UNIT_FILE" ]; }

jboss_running() { systemctl is-active --quiet "$SERVICE_NAME"; }

jboss_stop() {
    if ! jboss_running; then
        log "Servicio ${SERVICE_NAME} no esta activo; nada que detener."
        return 0
    fi
    log "Deteniendo ${SERVICE_NAME} (systemctl stop, apagado limpio)..."
    sudo systemctl stop "$SERVICE_NAME" \
        || die "systemctl stop ${SERVICE_NAME} fallo; revisar 'systemctl status ${SERVICE_NAME}'"
    jboss_running && die "El servicio no se detuvo. Revisar 'systemctl status ${SERVICE_NAME}'."
    log "JBoss detenido."
}

jboss_start() {
    log "Arrancando ${SERVICE_NAME} (systemctl start)..."
    sudo systemctl start "$SERVICE_NAME" \
        || die "systemctl start ${SERVICE_NAME} fallo; revisar 'systemctl status ${SERVICE_NAME}'"
    jboss_running || die "El servicio no quedo activo tras start. Revisar 'systemctl status ${SERVICE_NAME}'."
    log "Servicio activo; verificando disponibilidad de la app..."
}

health_check() {
    local waited=0 code
    while [ "$waited" -lt "$HEALTH_TIMEOUT_SECONDS" ]; do
        code=$(curl -s -o /dev/null -w '%{http_code}' --max-time 10 "$HEALTH_URL" 2>/dev/null || echo 000)
        case "$code" in
            200|301|302|401|403) log "Health OK (HTTP $code) en ${HEALTH_URL}"; return 0 ;;
        esac
        sleep "$HEALTH_INTERVAL_SECONDS"; waited=$((waited + HEALTH_INTERVAL_SECONDS))
        log "  esperando app... (${waited}s, ultimo HTTP=${code})"
    done
    err "La app no respondio en ${HEALTH_TIMEOUT_SECONDS}s (ultimo HTTP=${code})"
    return 1
}

install_ear() {
    local src="$1"
    [ -f "$src" ] || die "No existe el ear a instalar: $src"
    log "Instalando ear: $src -> ${JBOSS_DEPLOY_DIR}/${EAR_NAME}"
    sudo cp "$src" "${JBOSS_DEPLOY_DIR}/${EAR_NAME}"
    sudo chown "${JBOSS_USER}:${JBOSS_USER}" "${JBOSS_DEPLOY_DIR}/${EAR_NAME}"
    sudo chmod 755 "${JBOSS_DEPLOY_DIR}/${EAR_NAME}"
}

backup_current_ear() {
    mkdir -p "$BACKUP_EAR_DIR"
    local current="${JBOSS_DEPLOY_DIR}/${EAR_NAME}"
    if [ ! -f "$current" ]; then
        log "No hay ear desplegado previo; sin respaldo." >&2
        return 0
    fi
    local dest="${BACKUP_EAR_DIR}/khipus_$(date '+%Y%m%d_%H%M%S').ear"
    sudo cp "$current" "$dest"
    sudo chown "$(id -un):$(id -gn)" "$dest" 2>/dev/null || true
    log "Ear actual respaldado en: $dest" >&2
    echo "$dest"
}

# ============================================================================
# Subcomando: install  (solo instala/actualiza el servicio systemd)
# ============================================================================
cmd_install() {
    log "==================== INSTALACION DEL SERVICIO systemd ===================="

    # --- valores por defecto: los confirmados de este servidor (CONFIG),
    #     con autodeteccion como respaldo si algun dia cambian ---
    local java_guess="${JAVA_HOME_DEFAULT}" mysql_guess="${MYSQL_SERVICE_DEFAULT}"
    if [ ! -x "${java_guess}/bin/java" ]; then
        java_guess="$(dirname "$(dirname "$(readlink -f "$(command -v java 2>/dev/null)" 2>/dev/null)")" 2>/dev/null || true)"
        [ -z "$java_guess" ] && java_guess="/usr/lib/jvm/java-8-openjdk"
    fi
    if ! systemctl list-unit-files 2>/dev/null | grep -q "^${mysql_guess}\.service"; then
        for s in mysqld mysql mariadb; do
            if systemctl list-unit-files 2>/dev/null | grep -q "^${s}\.service"; then mysql_guess="$s"; break; fi
        done
    fi

    # --- confirmar/editar con el usuario ---
    log "Confirma los datos del servicio (Enter = valor por defecto):"
    local JAVA_HOME_VAL JBOSS_HOME_VAL JBOSS_USER_VAL MYSQL_SVC
    ask JAVA_HOME_VAL  "JAVA_HOME del servidor" "$java_guess"
    ask JBOSS_HOME_VAL "JBOSS_HOME"             "$JBOSS_HOME"
    ask JBOSS_USER_VAL "Usuario que corre JBoss" "$JBOSS_USER"
    ask MYSQL_SVC      "Servicio systemd de MySQL (para orden de arranque)" "$mysql_guess"

    [ -x "${JAVA_HOME_VAL}/bin/java" ] || err "Aviso: ${JAVA_HOME_VAL}/bin/java no es ejecutable. Verifica JAVA_HOME."
    [ -x "${JBOSS_HOME_VAL}/bin/run.sh" ] || die "No existe ${JBOSS_HOME_VAL}/bin/run.sh; JBOSS_HOME incorrecto."

    echo
    log "Se instalara ${UNIT_FILE} con:"
    log "  JAVA_HOME = ${JAVA_HOME_VAL}"
    log "  JBOSS_HOME= ${JBOSS_HOME_VAL}   usuario=${JBOSS_USER_VAL}   MySQL=${MYSQL_SVC}.service"
    local ok; ask ok "Continuar?" "s"
    [ "$ok" = "s" ] || [ "$ok" = "S" ] || die "Cancelado."

    sudo_prime

    # --- si hay un JBoss viejo corriendo (screen), detenerlo antes de migrar ---
    if ! jboss_running && pgrep -f "org.jboss.Main" >/dev/null 2>&1; then
        log "Detectado un JBoss en ejecucion fuera de systemd (screen). Se detendra limpio antes de instalar."
        sudo -u "$JBOSS_USER_VAL" bash -c "cd '${JBOSS_HOME_VAL}/bin' && ./shutdown.sh -S" || true
        local w=0
        while pgrep -f "org.jboss.Main" >/dev/null 2>&1; do
            [ "$w" -ge 120 ] && die "El JBoss anterior no se detuvo; deten manualmente y reintenta."
            sleep 5; w=$((w+5)); log "  esperando cierre del JBoss anterior... (${w}s)"
        done
        screen -S "sic_terdemol" -X quit >/dev/null 2>&1 || true
        log "JBoss anterior detenido."
    fi

    # --- generar el unit file ---
    local tmp; tmp="$(mktemp)"
    cat > "$tmp" <<UNIT
[Unit]
Description=JBoss AS 5.1 - KHIPUS (TERDEMOL)
Wants=network-online.target
After=network-online.target ${MYSQL_SVC}.service

[Service]
Type=simple
User=${JBOSS_USER_VAL}
Group=${JBOSS_USER_VAL}
WorkingDirectory=${JBOSS_HOME_VAL}/bin
Environment=JAVA_HOME=${JAVA_HOME_VAL}
Environment=JBOSS_HOME=${JBOSS_HOME_VAL}
Environment=LANG=en_US.UTF-8
ExecStart=${JBOSS_HOME_VAL}/bin/run.sh -b ${JBOSS_BIND_ADDR}
ExecStop=${JBOSS_HOME_VAL}/bin/shutdown.sh -S
Restart=on-failure
RestartSec=10
TimeoutStartSec=300
TimeoutStopSec=180
SuccessExitStatus=0 143
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
UNIT

    log "Instalando unit file..."
    sudo cp "$tmp" "$UNIT_FILE"
    rm -f "$tmp"
    sudo chmod 644 "$UNIT_FILE"
    sudo systemctl daemon-reload
    sudo systemctl enable --now "$SERVICE_NAME"

    log "Verificando..."
    sleep 3
    jboss_running || die "El servicio no quedo activo. Revisar 'systemctl status ${SERVICE_NAME}'."
    if health_check; then
        log "==================== SERVICIO INSTALADO Y ACTIVO ===================="
        log "Control: sudo systemctl start|stop|restart|status ${SERVICE_NAME}"
        log "Siguiente: sube el zip por scp y corre  ./$(basename "$0") deploy <version>"
    else
        die "El servicio arranco pero la app no respondio. Revisar 'journalctl -u ${SERVICE_NAME} -e'."
    fi
}

# ============================================================================
# Subcomando: deploy <version>
# ============================================================================
cmd_deploy() {
    local VERSION="${1:-}"
    [ -n "$VERSION" ] || die "Uso: $(basename "$0") deploy <version>   (ej: deploy 6.0.116)"
    service_installed || die "El servicio no esta instalado. Corre primero: $(basename "$0") install"

    local RELEASE_ZIP="${DOWNLOADS_DIR}/v${VERSION}.zip"
    local UNPACK_DIR="${DOWNLOADS_DIR}/v${VERSION}"
    local NEW_EAR="${UNPACK_DIR}/${EAR_NAME}"

    mkdir -p "$LOG_DIR"
    local LOG_FILE="${LOG_DIR}/deploy_v${VERSION}_$(date '+%Y%m%d_%H%M%S').log"
    exec > >(tee -a "$LOG_FILE") 2>&1

    log "==================== DESPLIEGUE v${VERSION} ===================="
    log "Log: $LOG_FILE"

    [ -f "$RELEASE_ZIP" ] || die "No se encuentra el release: $RELEASE_ZIP (subelo con scp a ${DOWNLOADS_DIR})"
    [ -x "$DB_DUMP_SCRIPT" ] || die "No se encuentra/ejecuta el dump de BD: $DB_DUMP_SCRIPT"
    command -v curl >/dev/null || die "curl no esta instalado (requerido para el health check)"

    sudo_prime

    log ">> [1/6] Backup de base de datos..."
    "$DB_DUMP_SCRIPT" || die "El dump de la BD fallo; se aborta el despliegue"

    log ">> [2/6] Backup del ear desplegado actual..."
    local PREVIOUS_EAR; PREVIOUS_EAR="$(backup_current_ear || true)"

    log ">> [3/6] Apagando JBoss..."
    jboss_stop

    log ">> [4/6] Preparando el nuevo ear..."
    rm -rf "$UNPACK_DIR"
    unzip -o "$RELEASE_ZIP" -d "$DOWNLOADS_DIR" >/dev/null
    [ -f "$NEW_EAR" ] || die "El zip no contiene v${VERSION}/${EAR_NAME} (revisa como se empaqueto)"
    install_ear "$NEW_EAR"

    log ">> [5/6] Arrancando JBoss..."
    jboss_start

    log ">> [6/6] Health check..."
    if health_check; then
        log "==================== DESPLIEGUE v${VERSION} OK ===================="
        log "App viva en ${HEALTH_URL}"
        return 0
    fi

    err "El despliegue de v${VERSION} NO paso el health check. Iniciando ROLLBACK..."
    if [ -z "$PREVIOUS_EAR" ] || [ ! -f "$PREVIOUS_EAR" ]; then
        die "No hay ear anterior para rollback. JBoss quedo con v${VERSION} sin responder. Revisar manualmente."
    fi
    jboss_stop
    install_ear "$PREVIOUS_EAR"
    jboss_start
    if health_check; then
        die "ROLLBACK OK: se restauro el ear anterior ($PREVIOUS_EAR). v${VERSION} NO quedo desplegado."
    else
        die "ROLLBACK arranco pero la app tampoco responde. INTERVENCION MANUAL. Ear anterior: $PREVIOUS_EAR"
    fi
}

# ============================================================================
# Subcomando: rollback [ear|--list]
# ============================================================================
cmd_rollback() {
    if [ "${1:-}" = "--list" ]; then
        echo "Backups en ${BACKUP_EAR_DIR} (mas reciente abajo):"
        ls -1tr "$BACKUP_EAR_DIR"/khipus_*.ear 2>/dev/null || echo "  (no hay backups)"
        return 0
    fi
    service_installed || die "El servicio no esta instalado. Corre primero: $(basename "$0") install"

    local TARGET_EAR="${1:-}"
    if [ -z "$TARGET_EAR" ]; then
        TARGET_EAR="$(ls -1t "$BACKUP_EAR_DIR"/khipus_*.ear 2>/dev/null | head -n1 || true)"
        [ -n "$TARGET_EAR" ] || die "No hay backups en ${BACKUP_EAR_DIR}. Indica un ear: rollback <ruta.ear>"
        log "Backup mas reciente seleccionado: $TARGET_EAR"
    fi
    [ -f "$TARGET_EAR" ] || die "No existe el ear indicado: $TARGET_EAR"

    local ans; ask ans "Restaurar '${TARGET_EAR}' y reiniciar JBoss?" "N"
    [ "$ans" = "s" ] || [ "$ans" = "S" ] || die "Cancelado."

    mkdir -p "$LOG_DIR"
    local LOG_FILE="${LOG_DIR}/rollback_$(date '+%Y%m%d_%H%M%S').log"
    exec > >(tee -a "$LOG_FILE") 2>&1

    log "==================== ROLLBACK ===================="
    log "Restaurando: $TARGET_EAR"
    sudo_prime
    backup_current_ear >/dev/null || true
    jboss_stop
    install_ear "$TARGET_EAR"
    jboss_start
    if health_check; then
        log "==================== ROLLBACK OK ===================="
        log "App viva en ${HEALTH_URL} con $TARGET_EAR"
    else
        die "El rollback arranco pero la app no responde. INTERVENCION MANUAL requerida."
    fi
}

# ============================================================================
# Subcomando: status
# ============================================================================
cmd_status() {
    service_installed || die "El servicio no esta instalado."
    systemctl status "$SERVICE_NAME" --no-pager || true
    echo
    if health_check; then log "App respondiendo."; else err "App NO responde."; fi
}

usage() {
    sed -n '2,30p' "$0" | sed 's/^# \{0,1\}//'
}

# ============================================================================
# Dispatcher
# ============================================================================
case "${1:-}" in
    install)  cmd_install ;;
    deploy)   shift; cmd_deploy "${1:-}" ;;
    rollback) shift; cmd_rollback "${1:-}" ;;
    status)   cmd_status ;;
    -h|--help|help|"") usage ;;
    *) err "Comando desconocido: $1"; echo; usage; exit 1 ;;
esac
