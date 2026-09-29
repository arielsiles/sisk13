# Incidente TERDEMOL 2026-09-29: minero de criptomonedas en el servidor de producción

**Servidor:** TERDEMOL producción, `66.228.48.25` (`terdemol.net`), Ubuntu 24.04, JBoss 5.1.0.GA
**Fecha de atención:** 2026-09-29
**Estado:** contenido y limpio. Quedan pendientes (ver al final).

> Horas en UTC, salvo que se indique otra cosa. El reloj del servidor está en UTC y los logs
> de JBoss en hora de La Paz (UTC-4), porque arranca con `-Duser.timezone=America/La_Paz`.

---

## 1. Síntoma

El panel de Linode mostraba el CPU **por encima del 200%** desde las ~09:00 del 29-sep. El servidor
tiene 2 núcleos y 4 GB de RAM. Reiniciar no sirvió: el consumo volvía a los pocos minutos.

## 2. Diagnóstico

| Hallazgo | Detalle |
|---|---|
| Proceso | `/home/jboss/.Qs3fA2qFED/kHJpalMaILey -o www.githubtopai.top:80 --tls --randomx-1gb-pages ...`: **XMRig** (minero de Monero, algoritmo RandomX). Corría como usuario `jboss`, con ~170% de CPU y ~1,7 GB de RAM; llenaba el swap. |
| Vigilante | `/home/jboss/.cache/.kwork/kwork`: relanza el minero cada vez que muere. Cada relanzamiento dejaba una carpeta oculta con nombre aleatorio en el home de jboss. |
| Persistencia | crontab de jboss (`@reboot` + `*/30 * * * *` para `kwork`), units de systemd de usuario (`67a685c8c173.service` con `Restart=always`, `Svcice.service`, `MySvcice.service`), **linger** activado para jboss (su systemd de usuario arranca con el servidor), `~/.config/autostart/*.desktop`, y una línea al final de `.bashrc` y `.profile` que lanzaba `kwork`. |
| Webshells | `deploy/ROOT.war/1.jsp`, `deploy/ROOT.war/cmd.jsp`, `deploy/admin-console.war/cmd.jsp`, `deploy/shell3.war`, `deploy/shell6.war`, `deploy/management/shells.war/shells.jsp`. |
| Otros binarios | `/usr/local/jboss/bin/solrca_clean` (ELF estático) y `logtmp.txt` (log del atacante, en chino). |
| Acceso a root | **No se encontraron señales.** `dpkg -V sudo perl perl-base` sale OK, no hay `/etc/ld.so.preload`, root no tiene crontab, no hay usuarios nuevos ni con uid 0, jboss no está en el grupo sudo y `/root/.ssh/authorized_keys` es de la instalación (sep-2025). |
| Aplicación | `khipus.ear` **intacto**: su sha256 (`0cc08deb…80c8`) es igual al de `v6.1.3.zip`. |

### Cronología

| Fecha (UTC) | Evento |
|---|---|
| 2025-04-20 | Se arma la copia de JBoss `D:\appserver\linode\jboss.tar.gz` / `jboss.zip` (dueño `ulysse`) a partir de otro servidor. **Ya traía `ROOT.war/1.jsp` y `management/shells.war/shells.jsp`.** |
| 2025-09-10 23:19 | Se instala JBoss en TERDEMOL desde esa copia. El ctime de `1.jsp` es el mismo que el de `index.html`, y su mtime (abr-2025) viene de la copia. |
| 2026-09-11 15:42–17:13 | Primera persistencia: `Svcice`/`MySvcice` (cargador "qrl-start") e intento de descarga de `q-start.tar.gz`, que falló. |
| 2026-09-12 13:50–13:58 | Aparecen `shell3.war`, `shell6.war` y los `cmd.jsp` en `ROOT.war` y `admin-console.war`. |
| 2026-09-28 04:46 | Aparece `solrca_clean`. |
| 2026-09-29 09:25 (05:25 La Paz) | El log de JBoss registra accesos a `/jmx-console` y `/invoker` por el puerto público **8421** y varios redespliegues. Arranca el minero y el CPU sube a más de 200%. |
| 2026-09-29 17:58 y 19:11 | Reinicios del servidor. El minero vuelve a levantarse solo. |
| 2026-09-29 19:15–19:55 | Diagnóstico, contención, limpieza, rotación de credenciales y verificación (este documento). |

### Causa raíz

1. **Una copia de JBoss contaminada.** `D:\appserver\linode\jboss.tar.gz` y `jboss.zip` traían el
   webshell `1.jsp` (533 bytes, sha256 `940a674cfe8179b2b8964bf408037e0e5a5ab7e47354fe4fa7a9289732e1f1b8`;
   usa BASE64, `ClassLoader` y `defineClass`, así que carga clases enviadas por el atacante) y
   `shells.jsp`. TERDEMOL se instaló desde esa copia, así que llevaba un año con una puerta trasera.
   El `jboss-5.1.0.GA.zip` original de la misma carpeta está limpio.
2. **Consolas de administración expuestas a internet.** `jmx-console`, `http-invoker`, `management`
   y `admin-console` respondían por el 8421, el mismo puerto de KHIPUS, sin autenticación.
   Permiten desplegar código y ejecutar comandos.

No hay access log de JBoss (el `AccessLogValve` está desactivado), así que no se puede saber cuál
de las dos puertas se usó primero. Las dos quedaron cerradas.

## 3. Solución aplicada

**Contención**
1. `systemctl stop khipus-jboss`, `loginctl disable-linger jboss`, `pkill -9 -u jboss`.
2. Respaldo de la evidencia en `/root/incidente-2026-09-29/`: copia del home de jboss (`home-jboss.tgz`) y su crontab.
3. `crontab -r -u jboss`.

**Limpieza.** Lo que estaba en `deploy/` y en `bin/` se **movió** a evidencia, no se borró.
4. Movidos a `/root/incidente-2026-09-29/deploy-quitado/`: `shell3.war`, `shell6.war`, `admin-console.war`,
   `jmx-console.war`, `http-invoker.sar`, `management` (incluye `shells.war`), `jmx-invoker-service.xml`,
   `legacy-invokers-service.xml`, `ROOT.war/cmd.jsp`.
5. Movidos a `.../bin-quitado/`: `solrca_clean`, `logtmp.txt`. Movidos a `.../restos/`: `ROOT.war/1.jsp`,
   `~/.config/autostart`, `~/.config/Svcice.linger` y `/tmp/.c97fc6f8a98eb351`.
6. Vaciados `server/default/work` y `server/default/tmp`, donde quedan los JSP compilados.
7. Borrado el malware del home de jboss (`.cache/.kwork`, las carpetas aleatorias, `.local/libexec`,
   `.q-start*`, `~/.config/systemd/user`) y quitadas las líneas de `kwork` de `.bashrc` y `.profile`.

**Endurecimiento**
8. ufw: se quitó la regla del **3306**. Solo quedan abiertos el **22** y el **8421**. Para conectarse a MySQL
   desde afuera ahora se usa un túnel: `ssh -N -L 3307:127.0.0.1:3306 terdemol@66.228.48.25` → `localhost:3307`.
9. MySQL: se **cambió la clave** de `admin@localhost` con `ALTER USER … REPLACE` (conserva los mismos grants:
   `ALL PRIVILEGES ON sic_terdemol.* WITH GRANT OPTION`) y se actualizó en `khipus-ds.xml`. La versión
   anterior del archivo está en `/root/incidente-2026-09-29/khipus-ds.xml.antes`. La clave nueva **no** se
   documenta aquí. `dump_terdemol.sh` la pide de forma interactiva.
10. MySQL: se eliminó la cuenta `admin@'190.106.249.81'`, que se usaba para la conexión directa al 3306 y
    tenía la clave vieja.
11. PC local: las copias contaminadas se renombraron a `jboss.tar.gz.CONTAMINADO` y `jboss.zip.CONTAMINADO`
    para que nadie vuelva a instalar desde ellas.

**Verificación (19:54 UTC)**
- Carga 0,04. El único proceso de jboss es el java de KHIPUS.
- `/khipus/` → 200. `/jmx-console`, `/invoker/JMXInvokerServlet`, `/management`, `/admin-console`,
  `/web-console` y `/status` → 404.
- Sin crontab, sin linger, sin units de usuario y sin `kwork` en los archivos de arranque de sesión de jboss.

## 4. Pendientes

- [ ] Cambiar la clave de sudo de `terdemol` (`passwd`), porque se compartió durante la atención.
- [ ] Poner nginx con HTTPS delante de JBoss: ver `requerimiento_https_nginx_terdemol.md`.
- [ ] Decidir cuándo borrar la evidencia de `/root/incidente-2026-09-29/`.
- [ ] Al montar cualquier servidor nuevo: usar solo `jboss-5.1.0.GA.zip` (el limpio), sacar las consolas
      de `deploy/` antes de exponerlo y comprobar que `ROOT.war` no traiga `.jsp`.

## 5. Cómo detectar si vuelve

```bash
top -b -n1 -o %CPU | head -12                       # algo distinto de java/mysqld arriba = alerta
ps -u jboss -o pid,args                             # solo debe estar run.sh + java
sudo crontab -l -u jboss                            # debe decir "no crontab for jboss"
ls /var/lib/systemd/linger                          # jboss no debe aparecer
sudo ls -la /home/jboss /home/jboss/.config         # sin carpetas ocultas con nombres aleatorios
ls /usr/local/jboss/server/default/deploy           # sin *.war extraños ni consolas
sudo find /usr/local/jboss -name '*.jsp' -newer /usr/local/jboss/server/default/deploy/khipus.ear ! -path '*/work/*'
```

**Indicadores de compromiso:** dominio `githubtopai.top`; procesos o rutas `kwork`, `Svcice`,
`MySvcice`, `qrl-start`, `solrca_clean`; archivos `cmd.jsp`, `shell*.war`, `ROOT.war/1.jsp`,
`shells.war`; sha256 `940a674c…f1b8` (`1.jsp`).
