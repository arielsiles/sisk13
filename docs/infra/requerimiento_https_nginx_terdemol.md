# Requerimiento: HTTPS con nginx delante de JBoss (TERDEMOL)

**Servidor:** TERDEMOL producción, `66.228.48.25` (`terdemol.net`, `www.terdemol.net`; el DNS ya apunta aquí)
**Origen:** incidente del 2026-09-29 (`incidente_2026-09-29_terdemol_minero.md`)
**Estado:** pendiente. Se ejecuta en una ventana sin usuarios.

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
de Seam funcionan igual que hoy. No se toca la base de datos.

```
Navegador ──HTTPS :443──> nginx ──HTTP 127.0.0.1:8480──> JBoss (KHIPUS) ──> MySQL
http://…:8421/… ─301─┘
```

## 3. Situación actual (verificada el 2026-09-29)

- JBoss arranca con `run.sh -b 0.0.0.0` (unit `khipus-jboss`, instalada por `scripts/deploy/khipus-deploy.sh install`).
- Conector HTTP en `jbossweb.sar/server.xml`: `port="8421" address="${jboss.bind.address}"`.
- ufw: abiertos 22 y 8421.
- nginx y certbot **no** están instalados. Ubuntu 24.04.3.
- JBoss Web (2.1, del 2009) **no trae `RemoteIpValve`**, solo `RemoteAddrValve`. Esto importa para el requisito R5.

## 4. Requisitos

| # | Requisito | Por qué |
|---|---|---|
| R1 | **Tiempo de espera de nginx ≥ 600 s** (`proxy_read_timeout`, `proxy_send_timeout`). | El default es 60 s. Una contabilización o un reporte pesado que tarde más **se completa en KHIPUS**, pero el usuario ve un error y puede **repetir la operación (duplicado)**. nginx nunca debe cortar antes que KHIPUS. |
| R2 | **`client_max_body_size 20m`**. | El default es 1 MB. KHIPUS acepta hasta 10 MB (`components.xml:136`, `max-request-size="10000000"`). |
| R3 | Pasar `Host`, `X-Real-IP`, `X-Forwarded-For` y `X-Forwarded-Proto`. | Para las redirecciones y para la IP real. |
| R4 | Conector de JBoss con `proxyName="terdemol.net" proxyPort="443" scheme="https" secure="true"`. | Las redirecciones que arma JBoss (login, post-redirect-get de Seam) deben salir como `https://terdemol.net/...`. `secure="true"` además marca la cookie `JSESSIONID` como Secure. |
| R5 | **IP real en el registro de ingresos.** `AuthenticatorAction.java:92` guarda `getRemoteAddr()`, y detrás de nginx sería siempre `127.0.0.1`. | **Hay que decidir** entre (a) un cambio mínimo en el código: usar `X-Forwarded-For` **solo si** `getRemoteAddr()` es `127.0.0.1`; o (b) aceptar `127.0.0.1` en KHIPUS y tomar la IP real del access log de nginx. No existe `RemoteIpValve` para resolverlo solo con configuración. |
| R6 | nginx publica **solo** `location /khipus/`. `/` redirige a `/khipus/` y todo lo demás da 404. | Que no se pueda volver a llegar a las consolas ni a un webshell. |
| R7 | JBoss escucha **solo en 127.0.0.1**, con el HTTP en **8480**. El 8421 pasa a nginx. | Si JBoss queda en 127.0.0.1, todos sus puertos (1098, 1099, 4444–4446, 8009, 8083…) dejan de estar expuestos, aunque ufw falle. |
| R8 | ufw: abrir **80** y **443**; mantener 8421 (ahora de nginx) y 22. | El 80 lo necesita Let's Encrypt para emitir y renovar el certificado; además redirige a HTTPS. |
| R9 | Certificado Let's Encrypt para `terdemol.net` y `www.terdemol.net`, con renovación automática (`certbot.timer`), probada con `certbot renew --dry-run`. | Que el certificado no venza. |
| R10 | Ajustar `scripts/deploy/khipus-deploy.sh`: `ExecStart … -b 127.0.0.1` y `HEALTH_URL=http://127.0.0.1:8480/khipus/`. Documentarlo en `scripts/deploy/README.md`. | El health check del `deploy` apunta hoy a `http://terdemol.net:8421/khipus/`, que pasará a ser una redirección 301. |
| R11 | Encabezados de seguridad: `Strict-Transport-Security` (empezar con `max-age` corto), `X-Content-Type-Options nosniff`. | Buenas prácticas. HSTS con `max-age` corto al principio, por si hay que volver atrás. |

## 5. Configuración propuesta (borrador)

`/etc/nginx/sites-available/khipus`:
```nginx
# Enlace anclado de los usuarios: http://terdemol.net:8421/khipus/ -> https
server {
    listen 8421;
    server_name terdemol.net www.terdemol.net 66.228.48.25;
    return 301 https://terdemol.net$request_uri;
}

server {
    listen 80;
    server_name terdemol.net www.terdemol.net;
    location /.well-known/acme-challenge/ { root /var/www/html; }
    location / { return 301 https://terdemol.net$request_uri; }
}

server {
    listen 443 ssl http2;
    server_name terdemol.net www.terdemol.net;
    # ssl_certificate / ssl_certificate_key: los agrega certbot

    client_max_body_size 20m;                                     # R2
    add_header Strict-Transport-Security "max-age=86400" always;  # R11 (subir despues)
    add_header X-Content-Type-Options nosniff always;

    location = / { return 301 /khipus/; }

    location /khipus/ {                                           # R6
        proxy_pass http://127.0.0.1:8480;
        proxy_set_header Host              $host;                 # R3
        proxy_set_header X-Real-IP         $remote_addr;
        proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
        proxy_connect_timeout 30s;
        proxy_send_timeout    600s;                               # R1
        proxy_read_timeout    600s;                               # R1
        proxy_redirect http://terdemol.net/ https://terdemol.net/;
    }

    location / { return 404; }                                    # R6
}
```

`jbossweb.sar/server.xml`, conector HTTP (R4, R7):
```xml
<Connector protocol="HTTP/1.1" port="8480" address="${jboss.bind.address}"
           proxyName="terdemol.net" proxyPort="443" scheme="https" secure="true"
           ... (resto de atributos igual que hoy) />
```

## 6. Plan de ejecución (ventana sin usuarios, ~45 min)

1. **Antes:** avisar a los usuarios. Backup de la BD (`dump_terdemol.sh`) y copia de `server.xml`.
2. `sudo apt install nginx certbot python3-certbot-nginx`.
3. Crear la configuración (sección 5), **todavía sin el server del 8421**, que sigue siendo de JBoss.
   Abrir 80 y 443 en ufw. `nginx -t && systemctl reload nginx`.
4. `sudo certbot --nginx -d terdemol.net -d www.terdemol.net`, y después `certbot renew --dry-run`.
5. `systemctl stop khipus-jboss`. Editar `server.xml` (8421 → 8480 + atributos de proxy). Cambiar la unit a `-b 127.0.0.1`.
6. Activar el server del 8421 en nginx. `nginx -t && systemctl reload nginx`.
7. `systemctl start khipus-jboss`.
8. Verificar (sección 7). Si todo está bien, publicar la nueva versión de `khipus-deploy.sh` (R10).

## 7. Verificación

- [ ] `https://terdemol.net/khipus/` carga con el candado del navegador.
- [ ] `http://terdemol.net:8421/khipus/` y `http://terdemol.net/` redirigen a `https://terdemol.net/khipus/`.
- [ ] Login, logout y la vuelta al login después de que expira la sesión: todo queda en `https://`.
- [ ] Guardar un registro y ver que la redirección posterior sigue en HTTPS.
- [ ] Subir un adjunto de ~8 MB.
- [ ] Generar un reporte pesado (PDF y Excel).
- [ ] Una operación larga (> 60 s): no debe dar 504.
- [ ] `https://terdemol.net/jmx-console/`, `/invoker/`, `/admin-console/` y `/status` → 404.
- [ ] Desde afuera no responden 8480, 1099, 4444 ni 8009: `ss -tlnp` los muestra en 127.0.0.1.
- [ ] IP registrada en el log de ingresos, según la opción elegida en R5.
- [ ] `./khipus-deploy.sh status` OK con el nuevo `HEALTH_URL`.

## 8. Vuelta atrás

1. `systemctl stop khipus-jboss`.
2. Restaurar `server.xml` (8421) y la unit (`-b 0.0.0.0`).
3. Desactivar el server del 8421 en nginx (o `systemctl stop nginx`).
4. `systemctl start khipus-jboss`.

Los usuarios vuelven a `http://terdemol.net:8421/khipus/` sin cambios en los datos.

## 9. Decisiones pendientes

- **R5:** ¿cambio mínimo en el código para registrar la IP real, o aceptar `127.0.0.1` y usar el access log de nginx?
- La fecha y hora de la ventana.
- Cuándo subir HSTS a `max-age=31536000`, por ejemplo después de una semana sin problemas.
