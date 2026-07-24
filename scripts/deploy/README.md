# Despliegue de KHIPUS en el servidor — TERDEMOL

Guía de operación del servidor de producción. Todo el ciclo (instalar el
servicio, desplegar releases, controlar JBoss y hacer rollback) vive en un solo
script autocontenido: **`khipus-deploy.sh`**.

- Se ejecuta **en el servidor**, como usuario `terdemol`.
- Subes solo ese script (a `~/deploy/`) y el zip del release (a `~/downloads/`).
- Pide la clave de `sudo` cuando la necesita (la cachea una vez). No modifica sudoers.
- Conserva **todos** los backups (ears y dumps de BD); la limpieza es manual.

---

## Comandos

| Comando | Qué hace |
|---|---|
| `./khipus-deploy.sh install` | Instala/actualiza el servicio systemd (una vez) |
| `./khipus-deploy.sh deploy <version>` | Despliega un release ya subido por scp |
| `./khipus-deploy.sh rollback` | Vuelve al ear anterior (último backup) |
| `./khipus-deploy.sh rollback --list` | Lista los backups disponibles |
| `./khipus-deploy.sh status` | Estado del servicio + health check |
| `./khipus-deploy.sh --help` | Ayuda |

---

## 1. Copiar el script al servidor (una vez)

Desde tu máquina (Git Bash o PowerShell; pedirá la clave):
```bash
ssh terdemol@66.228.48.25 'mkdir -p ~/deploy'
scp "d:/Intellij/sisk13/scripts/deploy/khipus-deploy.sh" terdemol@66.228.48.25:~/deploy/
```
En el servidor, darle permiso de ejecución:
```bash
cd ~/deploy && chmod +x khipus-deploy.sh
```

---

## 2. Instalar el servicio systemd (una vez)

```bash
cd ~/deploy && ./khipus-deploy.sh install
```
- Los datos vienen con los **defaults de este servidor** (JAVA_HOME `/opt/jdk`,
  JBOSS_HOME `/usr/local/jboss`, usuario `jboss`, MySQL `mysql`) → basta con
  pulsar **Enter** en cada uno y confirmar con `s`.
- Si detecta un JBoss corriendo en `screen`, lo **apaga limpio** y lo reemplaza
  por el servicio systemd. **Hay un breve corte** → hazlo en ventana de baja carga.
- A partir de aquí JBoss **arranca en boot** y **se reinicia solo** ante caídas.

El servicio queda como `khipus-jboss`. El unit incluye el mismo entorno con que
corría antes (JVM opts vía `run.conf`, `JAVA_HOME`, `LANG=en_US.UTF-8`), así que
la app corre **igual** que en el arranque manual.

---

## 3. Desplegar un release

Desde tu máquina, subir el zip generado por `ant -f build-prod.xml dist-release`:
```bash
scp "d:/appserver/release/TERDEMOL/v6.0.116.zip" terdemol@66.228.48.25:~/downloads/
```
En el servidor:
```bash
cd ~/deploy && ./khipus-deploy.sh deploy 6.0.116
```
El `deploy` hace, en orden:
1. **Backup de BD** (`dump_terdemol.sh`) — aborta si falla.
2. **Backup del ear actual** (punto de rollback).
3. **Para JBoss** (`systemctl stop`, apagado limpio).
4. Descomprime y **coloca el nuevo ear** (`chown jboss`, `chmod 755`).
5. **Arranca JBoss** (`systemctl start`) y hace **health check** (hasta 5 min).
6. Si la app **no** levanta → **rollback automático** al ear anterior.

Éxito:
```
==================== DESPLIEGUE v6.0.116 OK ====================
```
Verifica en el navegador: el footer del login dirá `Version: 6.0.116`.

---

## 4. Controlar el servicio (día a día)

```bash
sudo systemctl stop khipus-jboss       # parar (apagado limpio)
sudo systemctl start khipus-jboss      # levantar (~30-60s en estar arriba)
sudo systemctl restart khipus-jboss    # reiniciar
systemctl status khipus-jboss          # estado (sin sudo)
./khipus-deploy.sh status              # estado + health check de la app
```
Logs:
```bash
journalctl -u khipus-jboss -f          # consola en vivo (Ctrl+C para salir)
journalctl -u khipus-jboss -e          # ir al final
# el log propio de JBoss sigue igual:
/usr/local/jboss/server/default/log/server.log
```

---

## 5. Rollback

```bash
./khipus-deploy.sh rollback --list     # ver backups disponibles
./khipus-deploy.sh rollback            # volver al backup más reciente
./khipus-deploy.sh rollback <ruta.ear> # volver a un ear específico
```
Los backups del ear están en `~/backups/ears/` (se conservan todos).

---

## Configuración (valores de este servidor)

Están al inicio de `khipus-deploy.sh` (sección `CONFIG`); ajusta solo si cambia el servidor:

| Variable | Valor |
|---|---|
| `JBOSS_HOME` | `/usr/local/jboss` |
| `JBOSS_DEPLOY_DIR` | `/usr/local/jboss/server/default/deploy` |
| `JBOSS_USER` | `jboss` |
| `SERVICE_NAME` | `khipus-jboss` |
| `JAVA_HOME_DEFAULT` | `/opt/jdk` (JDK 1.8.0_321) |
| `MYSQL_SERVICE_DEFAULT` | `mysql` |
| `DB_DUMP_SCRIPT` | `~/terdemol/dump_terdemol.sh` |
| `DOWNLOADS_DIR` | `~/downloads` |
| `BACKUP_EAR_DIR` | `~/backups/ears` |
| `LOG_DIR` | `~/backups/deploy-logs` |
| `HEALTH_URL` | `http://terdemol.net:8421/khipus/` |

---

## (Opcional) sudo sin clave para el despliegue

Para no teclear la clave en cada `systemctl`, agrega una regla acotada con `sudo visudo`:
```
terdemol ALL=(root) NOPASSWD: /bin/systemctl start khipus-jboss, /bin/systemctl stop khipus-jboss, /bin/systemctl status khipus-jboss
```

---

## Revertir la migración (volver a screen)

```bash
sudo systemctl disable --now khipus-jboss
sudo rm /etc/systemd/system/khipus-jboss.service
sudo systemctl daemon-reload
# arrancar como antes:
cd /usr/local/jboss/bin && screen -dmS sic_terdemol ./run.sh -b 0.0.0.0
```

---

## Notas

- El **zip** se genera con `ant -f build-prod.xml dist-release` (queda en
  `D:/appserver/release/TERDEMOL/v<version>.zip`, con `v<version>/khipus.ear` dentro).
- El **script** tiene finales de línea Unix (LF). Si lo editas en Windows y falla
  con un error tipo `\r`, en el servidor: `sed -i 's/\r$//' khipus-deploy.sh`.
