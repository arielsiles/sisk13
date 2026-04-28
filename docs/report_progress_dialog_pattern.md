# Patrón reusable: progress modal para reportes con descarga de archivo

Componente reutilizable que muestra un modal de "Generando..." mientras un reporte produce un PDF/Excel/CSV, y lo cierra automáticamente cuando termina la descarga.

Ya implementado y probado en el [Reporte de Inventario Extendido](../view/warehouse/extendedInventoryReport.xhtml). Este documento explica cómo aplicarlo a cualquier otro reporte.

---

## Por qué un patrón especial

Los reportes que escriben directo a `ServletOutputStream` y llaman `responseComplete()` **no recargan la página**. Si sólo hacemos `onclick="Richfaces.showModalPanel(...)"`, el modal queda abierto para siempre porque nunca llega un JSF response que lo cierre.

Este patrón usa el truco "**cookie ready**":

1. Cliente genera un `token` único y lo manda en el form (hidden input).
2. Cliente muestra el modal y empieza a sondear cookie `reportReady_<token>` cada 500 ms.
3. Servidor escribe la cookie justo antes de flushear el archivo.
4. Browser entrega el archivo + setea la cookie.
5. Cliente detecta la cookie → cierra el modal.
6. Timeout de seguridad (10 min) cierra el modal igual si algo falla.

---

## Componentes reusables

### 1. [`GenericReportAction.markReportReady()`](../src/main/com/encens/khipus/action/reports/GenericReportAction.java)

Helper en la clase padre de todos los reportes. Setea la cookie con el token recibido del form.

```java
private String reportToken;
public String getReportToken() { return reportToken; }
public void setReportToken(String reportToken) { this.reportToken = reportToken; }

protected void markReportReady() { ... }
```

### 2. [`view/include/reportProgressDialog.xhtml`](../view/include/reportProgressDialog.xhtml)

Include que entrega:
- el `<rich:modalPanel>` (vía `progressDialog.xhtml` ya existente), y
- la función JS `startReportProgress(formClientId, modalId)` que se llama en el `onclick` del botón.

---

## Cómo aplicarlo a un reporte nuevo

### Paso 1 — El action ya extiende `GenericReportAction`

La mayoría ya lo hacen. Si el tuyo no, hacelo extender. Eso te da `reportToken` + `markReportReady()` gratis.

### Paso 2 — Llamar `markReportReady()` justo antes del flush

En **cada método de export** (PDF / Excel / CSV), antes de escribir el archivo al stream:

```java
HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance()
        .getExternalContext().getResponse();
response.addHeader("Content-disposition", "attachment; filename=mireporte.pdf");
markReportReady();                              // ← UNA LINEA
ServletOutputStream stream = response.getOutputStream();
... // escribir
stream.flush();
stream.close();
FacesContext.getCurrentInstance().responseComplete();
```

Llamarlo **después** de los `addHeader` y **antes** del `getOutputStream()`. Si lo llamás después del `getOutputStream()`, los headers ya están comprometidos y la cookie no llega.

### Paso 3 — En el XHTML del reporte

Dentro del `<h:form>` agregar el hidden token:

```xhtml
<h:inputHidden id="reportToken" value="#{miReporteAction.reportToken}"/>
```

> El `id` **debe ser exactamente** `reportToken` — el JS busca por ese ID convencional dentro del form.

En el botón "Generar" agregar el `onclick`:

```xhtml
<h:commandButton action="#{miReporteAction.generateReport}"
                 styleClass="button"
                 value="#{messages['Common.generate']}"
                 onclick="return startReportProgress(this.form.id, 'progressModalPanel');">
    <s:defaultAction/>
</h:commandButton>
```

Fuera del form, al final del body, incluir el modal:

```xhtml
<ui:include src="/include/reportProgressDialog.xhtml">
    <ui:param name="dialogId" value="progressModalPanel"/>
    <ui:param name="progressTitle" value="#{messages['Mi.reporte.titulo']}"/>
</ui:include>
```

Parámetros opcionales del include (todos con defaults sensatos):

| Param | Default |
|---|---|
| `dialogId` | `progressModalPanel` |
| `dialogTitle` | `messages['Common.processing']` |
| `progressTitle` | `messages['Common.processing']` |
| `dialogContent` | `messages['ProductInventory.processMessage']` |

### Paso 4 — Probar

1. `ant explode`
2. Reinicio completo de JBoss (no hot-redeploy — el action tiene `@Name`/`@Scope`).
3. Generar el reporte. El modal aparece al click y desaparece cuando se dispara la descarga.
4. Verificar que el archivo descarga bien y que el modal se cierra solo (no queda colgado).

---

## Ejemplo de referencia

[`ExtendedInventoryReportAction`](../src/main/com/encens/khipus/action/warehouse/reports/ExtendedInventoryReportAction.java):

```java
// Llamado en exportarPDF (linea ~501) y en exportarExcel (linea ~692)
markReportReady();
ServletOutputStream stream = response.getOutputStream();
...
```

[`view/warehouse/extendedInventoryReport.xhtml`](../view/warehouse/extendedInventoryReport.xhtml):

```xhtml
<h:inputHidden id="reportToken" value="#{extendedInventoryReportAction.reportToken}"/>
...
<h:commandButton ...
    onclick="return startReportProgress(this.form.id, 'progressModalPanel');">
...
<ui:include src="/include/reportProgressDialog.xhtml">
    <ui:param name="dialogId" value="progressModalPanel"/>
    <ui:param name="progressTitle" value="#{messages['Reports.kardex.extendedInventoryReport.titleReport']}"/>
</ui:include>
```

---

## Comportamiento por escenario

| Caso | Resultado |
|---|---|
| Reporte termina OK → cookie llega | Modal cierra automáticamente (~500 ms después del flush) |
| Usuario cancela la descarga | Cookie ya llegó antes del cancel; modal igual cierra |
| Excepción server-side (cookie nunca se setea) | Modal cierra por timeout de seguridad (10 min) |
| Usuario cierra pestaña antes de terminar | Irrelevante — la página ya no está |
| Multi-pestaña | Cada pestaña usa su propio token → no hay cruces |
| El action falla validación JSF (required, etc.) | Submit no llega al servidor; el modal igual cierra a los 10 min — TODO mejorable (ver "limitaciones") |

---

## Limitaciones conocidas

1. **Errores de validación dejan el modal abierto** hasta el timeout.
   Mitigación: el timeout de 10 min es suficiente para que el usuario perciba un fallo y cierre la pestaña, pero no es ideal. Un fix futuro podría ser un `<a4j:support>` que detecte errores JSF y dispare el cierre.

2. **Si la generación tarda más de 10 minutos**, el modal cierra antes que llegue el archivo.
   En este caso ajustar la constante `deadline` en [`reportProgressDialog.xhtml`](../view/include/reportProgressDialog.xhtml) o evaluar si el reporte realmente debería tardar tanto (ver [Docs/extended_inventory_report_optimization.md](extended_inventory_report_optimization.md) para optimizaciones aplicables).

3. **No muestra progreso real**, sólo una barra animada indeterminada. Implementar progreso real requiere thread asíncrono + polling, mucho más complejo. Probablemente innecesario.

4. **Cookie path = `/`** — funciona para toda la app. Si en algún momento se segmenta la app por contexto, ajustar.

---

## Cómo extender el patrón

### Variantes posibles
- **Reporte CSV**: aplica igual, llamar `markReportReady()` antes del flush.
- **Reporte que genera múltiples archivos en zip**: aplica igual, una sola cookie al final.
- **Reportes con vista previa en pantalla** (no descarga): no necesitan este patrón — usan la navegación JSF normal y el `onclick` con showModalPanel sin polling.

### Si necesitás mostrar diferente progressTitle según el formato (PDF vs Excel)
Pasar el parámetro dinámicamente desde el form, por ejemplo:

```xhtml
<ui:param name="progressTitle"
          value="#{miAction.reportFormat eq 'XLS' ? 'Generando Excel...' : 'Generando PDF...'}"/>
```

---

## Archivos del patrón

- [`GenericReportAction.java`](../src/main/com/encens/khipus/action/reports/GenericReportAction.java) — campo `reportToken` + método `markReportReady()`.
- [`view/include/reportProgressDialog.xhtml`](../view/include/reportProgressDialog.xhtml) — include reutilizable (modal + JS).
- [`view/include/progressDialog.xhtml`](../view/include/progressDialog.xhtml) — modal genérico ya existente, reusado por el include.
