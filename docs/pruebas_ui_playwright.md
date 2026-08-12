# Pruebas de UI automatizadas (Playwright)

Cómo se prueba la interfaz de KHIPUS sin hacer clic a mano. Se usó por primera vez para
validar "Revertir acopio de materia prima".

## MCP registrado

El servidor MCP de Playwright quedó registrado en **scope local** (solo este proyecto, no se
comparte por el repositorio):

```
claude mcp add playwright --scope local -- npx -y @playwright/mcp@latest --browser chrome
```

Queda anotado en `C:\Users\<usuario>\.claude.json`, bajo el proyecto `D:\Intellij\sisk13`.
Verificar con `claude mcp list` (debe decir `✓ Connected`).

> **Las herramientas del MCP se cargan al iniciar la sesión.** Si se registra el servidor con
> una sesión ya abierta, hay que reiniciarla para que aparezcan.

Usa el Chrome ya instalado (`--browser chrome`), así que no descarga navegadores.

## Alternativa sin MCP: script directo

Sirve en la misma sesión en que se registra el MCP, y para correr las pruebas desde una
terminal. Requiere solo `playwright-core` (sin descarga de navegador, usa el Chrome del
sistema):

```
npm i playwright-core
```

Datos de entrada de la aplicación en desarrollo:

| Dato | Valor |
|---|---|
| URL | `http://127.0.0.1:8081/khipus` |
| Login | `/login.jsf` (el patrón del servlet JSF es `*.jsf`) |
| Compañía | el `codigo` de la tabla `compania` (en terdemol: `TERDEMOL`) |

Esqueleto mínimo:

```js
const { chromium } = require('playwright-core');
const BASE = 'http://127.0.0.1:8081/khipus';

const browser = await chromium.launch({ channel: 'chrome', headless: true });
const page = await browser.newPage({ viewport: { width: 1500, height: 950 } });

await page.goto(BASE + '/login.jsf');
await page.fill('input[id$=":username"]', USUARIO);
await page.fill('input[id$=":password"]', CLAVE);
await page.fill('input[id$=":companyLogin"]', 'TERDEMOL');
await page.click('input[type="submit"]');
```

## Trampas de JSF/RichFaces al escribir selectores

Aprendidas a los golpes; ahorran mucho tiempo.

- **Los ids llevan el prefijo del contenedor.** `s:decorate` es un NamingContainer: un campo
  declarado como `id="username"` se renderiza como `loginForm:usernameField:username`, y un
  `rich:modalPanel` con `id="revertConfirmation"` termina en `j_id1148:revertConfirmation`.
  Por eso **siempre usar selectores por sufijo**: `[id$=":username"]`,
  `[id$="revertConfirmationContentDiv"]`.
- **El contenido visible del modal** está en `[id$="revertConfirmationContentDiv"]`, no en el
  panel raíz (que queda oculto).
- **Los iconos de acción** se ubican bien por su imagen: `a:has(img[src*="edit-line"])`.
- **`h:panelGroup` sin `styleClass` ni `id` no emite ningún `<span>`.** Un `rich:toolTip`
  anidado se queda sin elemento al cual engancharse y nunca se muestra.
- **`f:param` es un `UIParameter`, no un `ValueHolder`**: no admite `f:convertDateTime` ni
  `f:convertNumber` anidados. Si hay que formatear un parámetro de un mensaje, armar el texto
  ya interpolado en la acción (`MessageFormat` + `Messages.instance().get(clave)`).
- **`f:convertDateTime` sin `timeZone`** muestra la hora corrida. Pasar siempre
  `timeZone="#{sessionUser.timeZone}"`.
- **La banda global de mensajes usa `globalOnly="true"`** (`view/include/messages.xhtml`), así
  que los mensajes de validación atados a un componente (`required="true"`) **no se ven**. Para
  que el usuario reciba el aviso, validar en la acción y emitir el mensaje con
  `facesMessages.addFromResourceBundle(...)`, que es global.

## Ciclo de trabajo

JBoss no toma los cambios con solo copiar los archivos; hay que forzar el redespliegue:

```
ant explode
touch <jboss>/server/default/deploy/khipus.ear/META-INF/application.xml
```

El redespliegue tarda ~60 s. Está listo cuando `curl -o /dev/null -w "%{http_code}"
http://127.0.0.1:8081/khipus/login.jsf` responde `200`. Conviene revisar
`server/default/log/server.log` en busca de errores de validación de esquema de Hibernate.

## Datos de prueba

La base local es una copia de producción, así que **las pruebas corren contra datos reales**.
Los cambios de datos que una prueba necesite (abrir la ventana de reversión, otorgar un
permiso, cambiar una contraseña) van en un script aparte, **nunca mezclados con los
`query_v6.0.x.sql` que se ejecutan en producción**, y con su script de reversión al lado.
