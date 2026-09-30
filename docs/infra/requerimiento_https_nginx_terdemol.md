# Requerimiento: HTTPS con nginx delante de JBoss (TERDEMOL)

**Servidor:** TERDEMOL producción, `66.228.48.25` (`terdemol.net`, `www.terdemol.net`)
**Origen:** incidente del 2026-09-29 (`incidente_2026-09-29_terdemol_minero.md`)
**Estado:** **ejecutado** en la ventana del 2026-09-29 23:39 al 2026-09-30 02:12 UTC. Queda subir HSTS (sección 10).

---

## 1. Objetivo

- Que los usuarios entren por **`https://terdemol.net/khipus/`**, con el tráfico cifrado.
- Que JBoss **deje de estar expuesto a internet**: solo nginx habla con él, desde dentro del servidor.
- Que nginx deje pasar **solo `/khipus/`**. Cualquier otra ruta de JBoss (consolas, invoker, webshells que
  pudieran aparecer) responde 404 desde afuera.
- Que el enlace que los usuarios tienen guardado, `http://terdemol.net:8421/khipus/`, **siga funcionando**
  y los lleve a HTTPS.

## 2. Lo que NO cambia

HTTPS solo afecta el tramo entre el navegador y nginx. JBoss, KHIPUS (`khipus.ear`), las transacciones,
JPA/EJB, MySQL, `khipus-ds.xml`, los datos, las secuencias, los reportes, las sesiones y las conversaciones
de Seam funcionan igual. No se toca la base de datos.

```
Navegador ──HTTPS :443──> nginx ──HTTP 127.0.0.1:8480──> JBoss (KHIPUS) ──> MySQL
http(s)://…:8421/… ─301─┘
http://…:80/…      ─301─┘
```

## 3. Situación antes de la ventana

- JBoss arrancaba con `run.sh -b 0.0.0.0` (unit `khipus-jboss`, instalada por `scripts/deploy/khipus-deploy.sh install`).
- Conector HTTP en `jbossweb.sar/server.xml`: `port="8421" address="${jboss.bind.address}"`. Es el único lugar
  donde se define el puerto; `bindings-jboss-beans.xml` no lo sobrescribe.
- ufw: abiertos 22 y 8421.
- nginx y certbot no estaban instalados. Ubuntu 24.04.3.
- JBoss Web (2.1, del 2009) **no trae `RemoteIpValve`**, solo `RemoteAddrValve`. Esto importa para el R5.

## 4. Requisitos

| # | Requisito | Por qué | Estado |
|---|---|---|---|
| R1 | **Tiempo de espera de nginx ≥ 600 s** (`proxy_read_timeout`, `proxy_send_timeout`). | El default es 60 s. Una contabilización o un reporte pesado que tarde más **se completa en KHIPUS**, pero el usuario ve un error y puede **repetir la operación (duplicado)**. nginx nunca debe cortar antes que KHIPUS. | ✅ |
| R2 | **`client_max_body_size 20m`**. | El default es 1 MB. KHIPUS acepta hasta 10 MB (`components.xml:136`, `max-request-size="10000000"`). | ✅ |
| R3 | Pasar `Host`, `X-Real-IP`, `X-Forwarded-For` y `X-Forwarded-Proto`. | Para las redirecciones y para la IP real. | ✅ |
| R4 | Conector de JBoss con `proxyName="terdemol.net" proxyPort="443" scheme="https" secure="true"`. | Las redirecciones que arma JBoss (login, post-redirect-get de Seam) deben salir como `https://terdemol.net/...`. `secure="true"` además marca la cookie `JSESSIONID` como Secure. | ✅ |
| R5 | **IP real en la pantalla de usuarios en sesión** (`sessionUserLogList.xhtml`). La IP no se guarda en la BD; vive en memoria (`SessionUserLog`, ámbito de aplicación). | Detrás de nginx, `getRemoteAddr()` sería siempre `127.0.0.1`. Se resolvió en código con `AuthenticatorAction.clientIp()`: usa `X-Real-IP` **solo si** la conexión viene de `127.0.0.1`, para que desde afuera no se pueda falsear. No existe `RemoteIpValve` en JBoss Web 2.1 para resolverlo solo con configuración. | ✅ En v6.1.4 |
| R6 | nginx publica **solo** `location /khipus/`. `/` redirige a `/khipus/` y todo lo demás da 404. | Que no se pueda volver a llegar a las consolas ni a un webshell. | ✅ |
| R7 | JBoss escucha **solo en 127.0.0.1**, con el HTTP en **8480**. El 8421 pasa a nginx. | Si JBoss queda en 127.0.0.1, sus puertos (1098, 1099, 4446, 8009, 8083…) dejan de estar expuestos aunque ufw falle. | ✅ |
| R8 | ufw: abrir **80** y **443**; mantener 8421 (ahora de nginx) y 22. | El 80 lo necesita Let's Encrypt para emitir y renovar el certificado; además redirige a HTTPS. | ✅ |
| R9 | Certificado Let's Encrypt para `terdemol.net` y `www.terdemol.net`, con renovación automática. | Que el certificado no venza. | ✅ Vence el 28-12-2026; se renueva solo |
| R10 | `scripts/deploy/khipus-deploy.sh`: `JBOSS_BIND_ADDR=127.0.0.1` y `HEALTH_URL=http://127.0.0.1:8480/khipus/`. | Con el 8421 en nginx, el health check viejo daba OK aunque JBoss estuviera caído y el deploy **nunca haría rollback** (sección 9). | ✅ |
| R11 | Encabezados de seguridad: `Strict-Transport-Security` (empezar con `max-age` corto) y `X-Content-Type-Options nosniff`. | Buenas prácticas. HSTS corto al principio, por si hay que volver atrás. | ✅ 1 día; subir a 1 año (sección 10) |

## 5. Configuración aplicada

`/etc/nginx/sites-available/khipus` (enlazado en `sites-enabled`; se quitó el `default`):
```nginx
# Puerto 80: validacion de Let's Encrypt y redireccion a HTTPS
server {
    listen 80;
    listen [::]:80;
    server_name terdemol.net www.terdemol.net;

    location /.well-known/acme-challenge/ { root /var/www/letsencrypt; }
    location / { return 301 https://terdemol.net$request_uri; }
}

# Puerto 443: KHIPUS
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name terdemol.net www.terdemol.net;

    ssl_certificate     /etc/letsencrypt/live/terdemol.net/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/terdemol.net/privkey.pem;
    ssl_protocols       TLSv1.2 TLSv1.3;
    ssl_session_cache   shared:SSL:10m;
    ssl_session_timeout 1d;

    access_log /var/log/nginx/khipus_access.log;
    error_log  /var/log/nginx/khipus_error.log;

    client_max_body_size 20m;                                        # R2
    add_header Strict-Transport-Security "max-age=86400" always;     # R11: subir a 31536000 tras una semana estable
    add_header X-Content-Type-Options nosniff always;

    location = / { return 301 /khipus/; }

    location /khipus/ {                                              # R6: solo KHIPUS
        proxy_pass http://127.0.0.1:8480;
        proxy_set_header Host              $host;                    # R3
        proxy_set_header X-Real-IP         $remote_addr;
        proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
        proxy_connect_timeout 30s;
        proxy_send_timeout    600s;                                  # R1
        proxy_read_timeout    600s;                                  # R1
        proxy_redirect http://terdemol.net/ https://terdemol.net/;
    }

    location / { return 404; }                                       # R6
}

# Puerto 8421: enlace anclado de los usuarios -> https://terdemol.net/...
# Acepta TLS (navegadores que ya recibieron HSTS convierten http://:8421 en https://:8421)
# y HTTP plano (nginx lo marca como 497 y tambien lo redirige).
server {
    listen 8421 ssl;
    listen [::]:8421 ssl;
    server_name terdemol.net www.terdemol.net 66.228.48.25 _;

    ssl_certificate     /etc/letsencrypt/live/terdemol.net/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/terdemol.net/privkey.pem;
    ssl_protocols       TLSv1.2 TLSv1.3;

    error_page 497 =301 https://terdemol.net$request_uri;
    return 301 https://terdemol.net$request_uri;
}
```

`/usr/local/jboss/server/default/deploy/jbossweb.sar/server.xml`, conector HTTP (R4, R7):
```xml
<Connector protocol="HTTP/1.1" port="8480" address="${jboss.bind.address}" proxyName="terdemol.net" proxyPort="443" scheme="https" secure="true"
           connectionTimeout="20000" redirectPort="8443" />
```

`/etc/systemd/system/khipus-jboss.service`: `ExecStart=/usr/local/jboss/bin/run.sh -b 127.0.0.1`.

Certificado: `certbot certonly --webroot -w /var/www/letsencrypt -d terdemol.net -d www.terdemol.net`,
avisos a `ariel.siles@gmail.com`. La renovación la hace `certbot.timer` y recarga nginx
(`renew_hook = systemctl reload nginx` en `/etc/letsencrypt/renewal/terdemol.net.conf`).

## 6. Cómo se ejecutó

1. **Respaldos.** BD con `dump_terdemol.sh` (`~/terdemol/db/sic_terdemol_2026-09-29_2339.zip`). Copias de
   `server.xml`, la unit, `khipus-deploy.sh`, el estado de ufw y cada versión intermedia de la configuración de
   nginx en **`~/backups/ventana-https-2026-09-29_2339/`**.
2. `apt-get install nginx certbot`.
3. `ufw allow 80/tcp` y `ufw allow 443/tcp`.
4. nginx mínimo en el 80 (solo la validación de Let's Encrypt), certificado con `certbot certonly --webroot` y
   `certbot renew --dry-run` OK.
5. Configuración de los puertos 80 y 443 (sección 5), todavía sin el 8421, que seguía siendo de JBoss.
6. Con JBoss detenido: `server.xml` (8421 → 8480 y atributos de proxy), unit con `-b 127.0.0.1` y `daemon-reload`.
7. Servidor del 8421 en nginx y arranque de JBoss.
8. Verificación (sección 7). Durante las pruebas apareció el problema del 8421 con HSTS y se corrigió (sección 9).
9. `khipus-deploy.sh` corregido (R10) en el repo y en `~/deploy/`. Probado con `status` y con un
   `deploy 6.1.4` completo.

El acceso a root durante la ventana se dio con un `sudoers.d` temporal (NOPASSWD), que se borró al terminar.

## 7. Verificación

- [x] `https://terdemol.net/khipus/` carga con certificado válido (Let's Encrypt, `CN=terdemol.net`).
- [x] `http://terdemol.net:8421/khipus/`, `https://terdemol.net:8421/khipus/` y `http://terdemol.net/` redirigen a
      `https://terdemol.net/khipus/`, conservando la página pedida.
- [x] Login, logout y vuelta al login al expirar la sesión: todo queda en `https://`. Las redirecciones de JBoss
      salen como `https://terdemol.net/khipus/...`.
- [x] Cookie `JSESSIONID=…; Path=/khipus; Secure`.
- [x] Guardar un registro: la redirección posterior sigue en HTTPS.
- [x] Envío de 8 MB llega a JBoss; envío de 25 MB lo corta nginx con 413.
- [x] Reporte pesado (PDF y Excel).
- [ ] Operación de más de 60 s: no se pudo probar. El timeout de 600 s está en la configuración activa (`nginx -T`).
      Si un usuario reporta un 504, buscar `upstream timed out` en `/var/log/nginx/khipus_error.log`.
- [x] `/jmx-console/`, `/invoker/JMXInvokerServlet`, `/web-console/`, `/admin-console/`, `/status`, `/management/`,
      `/1.jsp` y `/khipus/../jmx-console/` → 404.
- [x] Desde afuera no responden 8480, 1099, 4444, 8009 ni 3306. Los puertos de JBoss escuchan en 127.0.0.1.
- [x] IP real en `/var/log/nginx/khipus_access.log`.
- [ ] La pantalla de usuarios en sesión muestra la IP real (R5). v6.1.4 incluye `clientIp()`; confirmarlo en la pantalla.
- [x] Stop/start del servicio con JBoss en 127.0.0.1: apagado limpio en 5 s, arranque en 36 s.
- [x] `./khipus-deploy.sh status` y `./khipus-deploy.sh deploy 6.1.4` con el `HEALTH_URL` nuevo: el deploy espera
      a JBoss y da `Health OK (HTTP 200)` a los 40 s.

## 8. Vuelta atrás

Con los archivos de `~/backups/ventana-https-2026-09-29_2339/`:

1. `sudo systemctl stop khipus-jboss`.
2. Liberar el 8421: borrar el servidor del 8421 en `/etc/nginx/sites-available/khipus` y recargar nginx (o `systemctl stop nginx`).
3. Restaurar `server.xml` (8421) y `khipus-jboss.service` (`-b 0.0.0.0`); `sudo systemctl daemon-reload`.
4. Restaurar `khipus-deploy.sh` en `~/deploy/` (el `HEALTH_URL` viejo).
5. `sudo systemctl start khipus-jboss`.

Los usuarios que ya recibieron HSTS seguirán forzando HTTPS en `terdemol.net` hasta que venza (1 día con la
configuración actual). Por eso HSTS se sube recién después de una semana estable.

## 9. Lo que se aprendió en la ventana

- **HSTS conserva el puerto.** Un navegador que recibió HSTS convierte `http://terdemol.net:8421` en
  `https://terdemol.net:8421`. Con el 8421 en HTTP plano, el enlace anclado funcionaba la primera vez y después
  quedaba en blanco. Solución: el 8421 escucha con TLS y `error_page 497` redirige también a quien llega en
  HTTP plano. **Cualquier puerto anclado que redirija tiene que aceptar TLS.**
- **El health check por nginx es un falso positivo.** Un `deploy 6.1.4` hecho con el script viejo dio
  `Health OK (HTTP 301)` un segundo después de arrancar, 33 s antes de que JBoss estuviera arriba. Si JBoss no
  hubiera arrancado, no habría hecho rollback. Con R10 el deploy espera la respuesta real de JBoss.
- **`ultimo HTTP=000000` en el log del deploy.** Cuando no conectaba, curl ya escribía `000` y el `|| echo 000`
  agregaba otro. Se corrigió con `|| true`.
- **El apagado de JBoss deja la unit en `failed`.** Pasa desde antes de la ventana y el apagado es limpio
  (`Shutdown complete`). Es cosmético y no afecta el arranque ni el reinicio automático.
- **Puertos que siguen en 0.0.0.0, bloqueados por ufw:** un puerto RMI aleatorio de Java (JMX) y MySQL 3306.
  Como segunda barrera se podría atar MySQL a `127.0.0.1`, lo que implica reiniciarlo. No se hizo.

## 10. Pendiente

- **Subir HSTS a `max-age=31536000`** después de una semana sin problemas (alrededor del 2026-10-07):
  editar la línea `Strict-Transport-Security` en `/etc/nginx/sites-available/khipus`, `nginx -t` y `systemctl reload nginx`.
- Confirmar la IP real en la pantalla de usuarios en sesión.
