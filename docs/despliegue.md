# Compilación y despliegue (KHIPUS / KHIPUS-PROD)

Guía del flujo de build local y de release a producción. Dos build files de Ant,
uno por entorno:

- **KHIPUS** (`build.xml`, perfil `dev`) → desarrollo diario en tu máquina.
- **KHIPUS-PROD** (`build-prod.xml`, perfil `prod`) → generar el release de producción.

> **Requisito absoluto:** compilar siempre con **JDK 1.8**. En IntelliJ:
> panel Ant → clic derecho en el build file → *Build File Properties → Execution →
> Run under JDK = 1.8*. Desde terminal, exportar `JAVA_HOME` al JDK 1.8 antes de `ant`.

---

## KHIPUS (desarrollo)

Trabajo local contra tu MySQL y tu JBoss local.

| Target | Para qué |
|---|---|
| `explode` | Desplegar/reflejar cambios de **estilos y vistas (.xhtml)** |
| `restart-exploded` | Recompilar **Java** y redesplegar con JBoss corriendo |
| `deploy` | Empaquetar y desplegar el ear de **dev** en el JBoss local (copia también el datasource de dev) |

- El schema de desarrollo sale de `devDbSchema` en `build.properties` (se inyecta en
  `Constants.java` al compilar y el fuente se restaura → git queda limpio).
- El login autocompleta usuario/contraseña/compañía según `build.properties`.
- El footer muestra `Version X - dev Build: #<fecha-hora>`.

---

## KHIPUS-PROD (producción)

> **Regla de oro:** en el panel KHIPUS-PROD usa solo los targets **sin punto**
> (`dist`, `dist-release`, `deploy`). Los que empiezan con punto (`.deploy`,
> `.datasource`…) son los heredados y pueden copiar datasources obsoletos.

| Target | Para qué |
|---|---|
| `dist` | Empaqueta el ear de producción en `dist/khipus.ear` |
| `dist-release` | `dist` + copia el ear a la carpeta de release **y genera el zip** para el servidor |
| `deploy` | Empaqueta y despliega el ear en el JBoss de `jboss.home` (para probar local) |

Salida de `dist-release` (cliente TERDEMOL, versión 6.0.116):
- `D:/appserver/release/TERDEMOL/v6.0.116/khipus.ear`
- `D:/appserver/release/TERDEMOL/v6.0.116.zip`  (contiene `v6.0.116/khipus.ear`)

### Selección de cliente y schema
- El **cliente activo** se define en `build.properties` con `client=<cliente>`
  (override puntual: `ant -f build-prod.xml -Dclient=ilva dist`).
- Cada cliente tiene su `build-prod-<cliente>.properties` con:
  - `dbSchema` → schema de producción (se inyecta en `Constants.java` al compilar).
  - `clientReleaseName` → subcarpeta de release.
  - `clientCompanyLogin` → compañía que se pre-llena en el login.
- Agregar un cliente = copiar `build-prod-client.properties.sample` a
  `build-prod-<cliente>.properties` y completar. Clientes actuales: `terdemol`
  (schema `sic_terdemol`), `ilva` (schema `khipus`).

### Blindajes automáticos de producción
Aplicados por `build-prod.xml` y verificados por `verify-prod-ear` (rompe el build si algo falla):
- Login **sin** autofill de usuario/contraseña (solo la compañía del cliente).
- `debug=false`, sin `jboss-seam-debug.jar`, `/debug.seam` bloqueado.
- `web.xml`: `facelets.DEVELOPMENT=false`, `REFRESH_PERIOD=-1`, `STATE_SAVING=server`.
- Footer con la versión real; `Build: #<fecha-hora>` (tstamp numérico, o `BUILD_NUMBER` si hay CI).
- `Constants.java` se inyecta y se **restaura** siempre (git limpio).

---

## Flujo completo de un release

```bash
# 1) En tu máquina (JDK 1.8): generar el zip del release
ant -f build-prod.xml dist-release        # cliente activo = build.properties (client=)

# 2) Subir el zip al servidor
scp "D:/appserver/release/TERDEMOL/v6.0.116.zip" terdemol@66.228.48.25:~/downloads/

# 3) En el servidor: desplegar (ver scripts/deploy/README.md)
cd ~/deploy && ./khipus-deploy.sh deploy 6.0.116
```

El despliegue en el servidor (systemd, backup de BD, health check, rollback
automático) está documentado en **[`scripts/deploy/README.md`](../scripts/deploy/README.md)**.

---

## Notas
- `build.properties` es **local** (no va a git; tiene rutas y credenciales). Su
  plantilla versionada es `build.properties.sample`.
- El footer necesita build **limpio** para actualizar la versión mostrada
  (`dist`/`dist-release` limpian solos; `war`/`explode` sueltos reutilizan el footer previo).
