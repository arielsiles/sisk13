# Vale de Despacho — Fase 10 (Tipos de Bolsa, Nota de Remisión, refinamientos)

**Rama:** `dev_dispatch_voucher`
**Stack:** JBoss Seam 2.1 + JSF 1.2 + RichFaces + Hibernate 3.x + MySQL.
**Build:** Apache Ant con **JDK 1.8** (`JAVA_HOME="C:/Program Files/Java/jdk1.8.0_311" ant clean explode`).
**SQL deltas:** apilados en [`query/query_v6.0.82_terdemol.sql`](../query/query_v6.0.82_terdemol.sql) (secciones 1-9).
**Plan original:** [dispatch_voucher_implementation_plan.md](dispatch_voucher_implementation_plan.md). Este doc cubre cambios posteriores que NO están en ese plan.

---

## 0. Mapa del módulo (qué es qué)

| Componente | Archivo | Función |
|---|---|---|
| Entidad cabecera | [WarehouseVoucherDispatch](../src/main/com/encens/khipus/model/warehouse/WarehouseVoucherDispatch.java) | Vale de despacho. Tabla `inv_valedespacho`. |
| Entidad detalle | [WarehouseVoucherDispatchDetail](../src/main/com/encens/khipus/model/warehouse/WarehouseVoucherDispatchDetail.java) | Línea por producto. Tabla `inv_valedespacho_det`. |
| Catálogo Tipo de Bolsa | [InventoryPackaging](../src/main/com/encens/khipus/model/warehouse/InventoryPackaging.java) | Tipo de envase con capacidad neta y peso bruto promedio. Tabla `inv_tipo_envase`. |
| Action despacho | [DispatchVoucherAction](../src/main/com/encens/khipus/action/warehouse/DispatchVoucherAction.java) | CRUD + asignaciones + validaciones del despacho. |
| Action catálogo envase | [InventoryPackagingAction](../src/main/com/encens/khipus/action/warehouse/InventoryPackagingAction.java) | CRUD del catálogo. |
| Reporte Certificado | [DispatchCertificateReportAction](../src/main/com/encens/khipus/action/warehouse/reports/DispatchCertificateReportAction.java) + [dispatchCertificateReport.jrxml](../view/warehouse/reports/dispatchCertificateReport.jrxml) | "Certificado de Recepción de Carguío". |
| Reporte Nota Remisión | [DispatchRemisionNoteReportAction](../src/main/com/encens/khipus/action/warehouse/reports/DispatchRemisionNoteReportAction.java) + [dispatchRemisionNoteReport.jrxml](../view/warehouse/reports/dispatchRemisionNoteReport.jrxml) | "Detalle de Transbordo". Comparte subreporte de productos con el Certificado, tiene subreporte propio para DETALLE EN PESO. |

---

## 1. Catálogo `InventoryPackaging` (Tipo de Bolsa/Envase)

**Tabla:** `inv_tipo_envase`. Multi-empresa por `idcompania`. Estado VIG/ANL (borrado lógico).

| Campo Java | Columna | Tipo | Significado |
|---|---|---|---|
| `name` | `nombre` | VARCHAR(80) NOT NULL | Tipo, ej. "Big Bag". |
| `capacityLabel` | `etiqueta_capacidad` | VARCHAR(60) NOT NULL | Texto mostrado tras "de", ej. "1 tonelada". |
| `unitCapacityKg` | `capacidad_kg` | DECIMAL(12,3) NULL | **Capacidad NETA** por bolsa (lo que entra dentro). Para validación suave. |
| `averageGrossWeightKg` | `peso_bruto_promedio_kg` | DECIMAL(12,3) NULL | **Peso BRUTO** real promedio (contenido + bolsa). Usado en OBSERVACION de Nota de Remisión. |
| `state` | `estado` | VARCHAR(3) NOT NULL DEFAULT 'VIG' | VIG / ANL. |

> **Importante:** `unitCapacityKg` y `averageGrossWeightKg` son CONCEPTOS DISTINTOS. No mezclarlos. Big Bag 1 t típicamente tiene `unitCapacityKg=1000` y `averageGrossWeightKg=1001` (bolsa vacía pesa ~1 kg).

### Unicidad
`UNIQUE KEY uq_tipoenvase_nombre (idcompania, nombre, etiqueta_capacidad)`. Permite "Big Bag" + "1 tonelada" y "Big Bag" + "1/2 tonelada" coexistiendo.

### Cómo se usa
- **Detalle de despacho** ([WarehouseVoucherDispatchDetail.packaging](../src/main/com/encens/khipus/model/warehouse/WarehouseVoucherDispatchDetail.java)) tiene FK `idtipoenvase` por línea.
- **Form del despacho** muestra combo "Tipo de Bolsa" por línea de producto.
- **Reporte Certificado**, columna TOTAL ENTREGADO genera: `"{bagsCount} bolsas {packaging.name} de {packaging.capacityLabel}"` (ver `buildTotalDelivered` en [DispatchCertificateReportAction.java](../src/main/com/encens/khipus/action/warehouse/reports/DispatchCertificateReportAction.java)).
- **Reporte Nota de Remisión**, columna OBSERVACION del DETALLE EN PESO usa `averageGrossWeightKg` para componer: `"Cada Bolsa {name} tiene un Peso Bruto Promedio de {peso} Kg."`. Si `averageGrossWeightKg` está NULL → celda vacía.

### Validación suave
En [DispatchVoucherAction.warnPackagingConsistency](../src/main/com/encens/khipus/action/warehouse/DispatchVoucherAction.java): si `bagsCount × unitCapacityKg` difiere del peso de línea por más que una bolsa, agrega WARN (no bloqueante) en `validateDraft`.

---

## 2. Cliente/Transbordo en el despacho (cambio de UX importante)

Versión anterior tenía DOS campos:
1. **Cliente** (selectPopUp regular mostrando nombre completo del cliente).
2. **Cliente/Transbordo** (input texto libre, columna `inv_valedespacho.codigo_transbordo`).

Versión actual: **solo un selectPopUp llamado "Cliente/Transbordo"** que apunta al cliente (`dispatchVoucher.client`, FK `idcliente`). Muestra **únicamente `client.codigo`** (no expone nombre). El popup ([dispatchClientListModalPanel.xhtml](../view/customers/dispatchClientListModalPanel.xhtml)) solo lista `codPrefijo + codigo` y filtra por esos dos campos. **Razón:** seguridad de información — los choferes/operadores no deben ver nombres de clientes.

### Componentes
- [`DispatchClientDataModel`](../src/main/com/encens/khipus/action/customers/DispatchClientDataModel.java): Query con `WHERE client.codigo IS NOT NULL` en `getEjbql()` (NO como restriction — Seam exige que cada restriction tenga ≥1 binding `#{...}`). Restrictions solo por `codPrefijo` y `codigo`.
- [`dispatchClientListModalPanel.xhtml`](../view/customers/dispatchClientListModalPanel.xhtml): modal de 2 columnas y 2 filtros.
- Columna `codigo_transbordo` de `inv_valedespacho` **eliminada** (SQL sección 2 del 6.0.82, DROP idempotente).
- Reporte Certificado: la celda "CLIENTE / TRANSBORDO" ahora imprime `dispatch.client.codigo`.

---

## 3. Campos del cliente con permiso propio

Para ocultar selectivamente campos sensibles del form del cliente, se agregaron **6 permisos VIEW-only** (bitmask=1) en módulo `idmodulo=1`:

| Permiso | Campo del form | Decisión |
|---|---|---|
| `CLIENTTYPE` (462) | Tipo Cliente | `idtipocliente` es **NOT NULL en BD**: si el rol no tiene este permiso y crea nuevo cliente, falla. Solo asignar a roles que pueden crear. |
| `CLIENTTERRITORY` (463) | Territorio | Nullable en BD. |
| `CLIENTCATEGORY` (464) | Categoría Cliente | Nullable en BD. |
| `CLIENTDISCOUNT` (465) | % Desc. Producto + % Desc. Adicional | Ambos con un único permiso (decisión del usuario). |
| `CLIENTPAYMENT` (466) | Método de Pago | Nullable. |
| `CLIENTCASHACCOUNT` (467) | Cuenta Diferida | Nullable. |

**Patrón:** `rendered="#{s:hasPermission('CLIENTXYZ','VIEW')}"` en el `s:decorate` del campo. Si no tiene permiso, el campo **no se renderiza** (no aparece). Los permisos están en SQL sección 8 del 6.0.82, sin asignaciones automáticas — hay que asignarlos manualmente al rol.

### Campos nuevos en `personacliente`
- `codprefijo VARCHAR(10) NULL` — código de prefijo del cliente. Java: `Client.codPrefijo`.
- `codigocliente` ampliado de VARCHAR(10) → VARCHAR(100). Java: `Client.codigo`.

Ambos opcionales. SQL sección 7.

---

## 4. Reporte Certificado — peculiaridades

Página Carta 612×792, márgenes 40px (~1.4cm), `columnWidth=532`.

### Estructura
- **Title band (h=120):** logo + REGISTRO/CERTIFICADO + TERDEMOL S.R.L./ORDEN DE ENTREGA N° + datos empresa + DATOS DE VENDEDOR DE ENTREGA (4 filas).
- **pageFooter (h=14):** Estado + N° página.
- **Summary band (h=461):** tabla DATOS DEL DESPACHO (7 filas), subreporte productos, MEDIDOR DE PESO (vertical), DATOS DEL RECEPCIONISTA, firmas, IMPORTANTE.

### Tabla DATOS DEL DESPACHO — fila PRODUCTO con productos múltiples
- El valor `productListDescription` se arma en Java con **separador `", "`** (no `"\n"`) para que quede en una línea.
- La celda valor tiene `isStretchWithOverflow="false"` para que NUNCA estire y nunca desalinee las filas siguientes. Si el texto excede el ancho, se recorta visualmente. El detalle completo está en el subreporte de productos abajo.

### TOTAL ENTREGADO (columna 5 del subreporte)
Generado en `buildTotalDelivered`:
```
{bagsCount} bolsa(s) {packaging.name} de {packaging.capacityLabel}
```
Si la línea no tiene packaging o bagsCount → celda vacía (mantiene formato).

### EMPRESA DE TRANSPORTE
**Usar `getAcronym()`, NO `getFullName()`** — `FinancesEntity.getFullName()` concatena `nitNumber + " " + acronym` y produce salida indeseada tipo "0 ASOCIACION...".

### Lecciones aprendidas con JR
- **`isStretchWithOverflow="true"` es necesario** en textFields con `<box padding>` y altura ajustada — sin esto, el contenido se recorta y la celda sale vacía (no estira para acomodar).
- **`stretchType="RelativeToTallestObject"` aplicado masivamente rompe el layout**: JR agrupa elementos por overlap vertical y todas las filas crecen al máximo. Usar **solo** cuando es estrictamente necesario y verificar visualmente.
- **Expresiones JR son Java puro**, no JPQL. Usar `&&` (codificado como `&amp;&amp;` en XML) no `and`. Si la expresión es compleja, **construirla en Java** y pasarla como un solo parámetro.
- **`positionType="Float"`** mueve un elemento hacia abajo cuando elementos arriba **estiran**, pero solo si **se solapan horizontalmente** con el que estiró. Por eso un value que estira en x=138 no empuja un label en x=0.

---

## 5. Reporte Nota de Remisión

Similar dimensiones al Certificado.

### Estructura
- **Title band (h=56):** logo + REGISTRO-NOTA DE REMISION/DETALLE DE TRANSBORDO + TERDEMOL S.R.L./Terminal de Molienda + N° orden grande.
- **pageFooter (h=14):** Estado + N° página. **OJO:** el action DEBE setear el parámetro `state`, si no sale en blanco.
- **Summary band (h=430):** fila LOTE/FECHA/CAMION (sin bordes), tabla Conductor/Vehículo (labels sin borde, valores con borde — grid 130/150/80/70/50/52), subreporte productos (compartido con Certificado), DETALLE EN PESO (título + subreporte propio), firmas, RECIBI CONFORME.

### Bordes en la sección Conductor/Vehículo
Patrón en JR para celda sin borde manteniendo padding:
```xml
<box padding="2"/>      <!-- sin borde -->
<box padding="2"><pen lineWidth="0.25"/></box>  <!-- con borde fino -->
```

### Subreporte DETALLE EN PESO ([dispatchRemisionNoteWeightSubReport.jrxml](../view/warehouse/reports/dispatchRemisionNoteWeightSubReport.jrxml))
Una fila por línea de detalle del despacho:
| Columna | Datos |
|---|---|
| CANTIDAD (izq) | `bagsCount + " Bolsas " + packaging.name` |
| CANTIDAD (der) | `quantity` formateado es-ES (`28.000,00`) + " Kg. En Peso Neto" |
| PRODUCTO | `productItem.name` |
| OBSERVACION | `"Cada Bolsa {name} tiene un Peso Bruto Promedio de {averageGrossWeightKg} Kg."` o vacío si el envase no tiene `averageGrossWeightKg` configurado. |

### Bloque DESPACHADO POR — patrón "single styled textField"
Antes era 5 textFields apilados (nombre/CI/rol/celular/empresa). Si el celular era nulo, dejaba una línea en blanco visible. **Solución:** un único textField con `markup="styled"` que renderiza un string multi-línea con `<b>...</b>` para las líneas en negrilla. El Java (`buildSignatureBlock`) omite las líneas sin dato — sin gaps. Patrón aplicable a cualquier bloque tipo "ficha" con campos opcionales.

```java
// resumen del patrón
StringBuilder sig = new StringBuilder();
if (name.length() > 0) sig.append(name);
if (ci.length() > 0) { if (sig.length() > 0) sig.append("\n"); sig.append("C.I.: ").append(ci); }
sig.append("\n<b>RESP. DE ALMACEN Y COMPRAS</b>");
if (cellphone.length() > 0) sig.append("\n").append(cellphone);
if (companyName.length() > 0) sig.append("\n<b>").append(companyName).append("</b>");
```

```xml
<textField isStretchWithOverflow="true" isBlankWhenNull="true">
    <reportElement ... />
    <textElement textAlignment="Center" markup="styled"><font fontName="Arial" size="9"/></textElement>
    <textFieldExpression class="java.lang.String"><![CDATA[$P{warehouseSignatureBlock}]]></textFieldExpression>
</textField>
```

---

## 6. Botones de impresión en el form

Ambos reportes tienen botón en la barra **superior e inferior** del form Update ([dispatchVoucherUpdate.xhtml](../view/warehouse/dispatchVoucherUpdate.xhtml)):
- **Imprimir Certificado** → `dispatchCertificateReportAction.generateCertificate(dispatchVoucher)`.
- **Imprimir Nota de Remisión** → `dispatchRemisionNoteReportAction.generateRemisionNote(dispatchVoucher)`.

Visibles en BORRADOR / APROBADO / ANULADO con permiso `WAREHOUSEDISPATCH,VIEW`. **Pendiente:** crear permisos propios y restringir (estos botones quedan accesibles para todo el que vea despachos por ahora).

---

## 7. SQL idempotente — patrón para DROP COLUMN

Para que el script `query_v6.0.82_terdemol.sql` pueda re-ejecutarse sin fallar:

```sql
SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                    AND TABLE_NAME = 'inv_valedespacho'
                    AND COLUMN_NAME = 'codigo_transbordo');
SET @sql := IF(@col_exists > 0,
               'ALTER TABLE inv_valedespacho DROP COLUMN codigo_transbordo',
               'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
```

Usar este patrón para cualquier DROP que pueda re-ejecutarse en BDs ya migradas.

---

## 8. Empleado: campo `cellphone`

Person ya tenía `cellphone` (columna `telcelular`) pero no estaba expuesto en el form de empleado. Agregado el input en [employee.xhtml](../view/employees/employee.xhtml) debajo de Domicilio. **Importante:** el reporte Certificado lo usa para llenar la celda "Celular:" de DATOS DE VENDEDOR DE ENTREGA.

---

## 9. Validar JRXML contra schema antes de desplegar

```bash
python - <<'PY'
import zipfile
from lxml import etree
with zipfile.ZipFile("lib/jasperreports-3.7.4.jar") as z:
    schema = etree.XMLSchema(etree.fromstring(z.read("net/sf/jasperreports/engine/dtds/jasperreport.xsd")))
doc = etree.parse("view/warehouse/reports/dispatchCertificateReport.jrxml")
print("valid:", schema.validate(doc))
PY
```

Útil para detectar problemas (attribute no permitido, `printWhenExpression` mal posicionada, band order incorrecta) **antes** de compilar y desplegar. JR 3.7.4 es estricto con la posición de los bands (title→pageHeader→columnHeader→detail→columnFooter→pageFooter→lastPageFooter→summary→noData).

---

## 10.bis Fase 11 — Detalle de Envases Carguio + permisos Finalizar/Desfinalizar + listado

### A. Detalle de Envases Carguio (reporte nuevo)

Reporte por bolsa fisica del despacho. Disponible **solo en estados APROBADO y FINALIZADO**.

#### Modelo nuevo
- [`WarehouseVoucherDispatchEnvelope`](../src/main/com/encens/khipus/model/warehouse/WarehouseVoucherDispatchEnvelope.java) — tabla `inv_valedespacho_envase`. Una fila por bolsa.
  - `numero_correlativo INT NOT NULL` — correlativo unico por despacho.
  - `codigo_identificacion VARCHAR(120) NOT NULL` — codigo impreso/visible en la bolsa.
  - `detalle_envase VARCHAR(255)` — editable por el operador (texto libre).
  - `peso_neto_aprox_kg DECIMAL(12,3)` — editable por el operador.
  - FK `idvaledespacho` + FK `iddetalledespacho` (a que linea de detalle pertenece).
- `WarehouseVoucherDispatchDetail.envelopes` — `@OneToMany`, `@OrderBy("correlativeNumber ASC")` para iterar en orden estable.

#### Generacion automatica al aprobar
Al APROBAR el despacho, [`DispatchVoucherServiceBean.generateEnvelopes`](../src/main/com/encens/khipus/service/warehouse/DispatchVoucherServiceBean.java) recorre los detalles y, por cada linea con `bagsCount > 0` y `bagsFromNumber`, crea `bagsCount` envases con correlativos consecutivos `[bagsFromNumber .. bagsFromNumber + bagsCount - 1]`. El `identificationCode` se inicializa con el correlativo formateado por defecto; el operador puede editarlo despues.

#### Edicion (estado APROBADO unicamente)
- Panel "Detalle de Envases Carguio" en [`dispatchVoucherUpdate.xhtml`](../view/warehouse/dispatchVoucherUpdate.xhtml) editable cuando `isApproved && !finalized`.
- `DispatchVoucherAction.saveEnvelopes()` llama a `dispatchVoucherService.updateEnvelopes(...)`. El service esta marcado `@TransactionAttribute(REQUIRES_NEW)` — REQUIRED no propagaba bien tras el approve y `em.flush()` tiraba `TransactionRequiredException`.

#### Finalizado
- Estado nuevo `DispatchState.FINALIZADO` agregado al enum, posterior a APROBADO.
- `finalizeDispatch` / `unfinalizeDispatch` en el service, tambien `REQUIRES_NEW`.
- En FINALIZADO los envases son inmutables: el form los muestra read-only y los botones de edicion desaparecen.

#### Reporte
- [`DispatchEnvelopeReportAction`](../src/main/com/encens/khipus/action/warehouse/reports/DispatchEnvelopeReportAction.java) genera PDF carta vertical.
- Subreporte tabular [`dispatchEnvelopeTableSubReport.jrxml`](../view/warehouse/reports/dispatchEnvelopeTableSubReport.jrxml) — columnas: N° / Codigo Identificacion / Peso Neto Aprox. (Center) / Detalle. Datasource = lista plana de envases (orden por detalle, luego por correlativo).
- Reporte principal [`dispatchEnvelopeReport.jrxml`](../view/warehouse/reports/dispatchEnvelopeReport.jrxml):
  - **Title band (h=108)**: logo + bloque derecho con TERDEMOL + Terminal + celda con N° de orden de entrega centrada (sin label "ORDEN DE ENTREGA N°", sin zero-padding). Luego 3 filas alineadas verticalmente: LOTE/FECHA/CAMION (sin bordes), CONDUCTOR/LICENCIA/CELULAR (con bordes en valores), PLACA/MARCA/COLOR (con bordes). Anchos por columna fijos: col1 100+150, col2 70+75, col3 65+72 (suma 532).
  - **Summary band (h=270)**: subreporte de envases (y=10) + linea de firma + bloque firma como single styled textField con `markup="styled"` (patron Nota de Remision).
- Lecciones JR consolidadas:
  - `markup="styled"` va en `<textElement>`, no en `<textField>`.
  - `positionType="Float"` va en `<reportElement>`, no en `<line>` directamente.
  - Multi-line textField atomico no se parte entre paginas — usar single styled textField con `\n` interno para el bloque de firmas, asi no quedan paginas en blanco.
  - Orden de bands en XSD: `title → pageHeader → columnHeader → detail → columnFooter → pageFooter → lastPageFooter → summary`. Un `pageFooter` despues de `summary` rompe la validacion.

### B. Permisos propios Finalizar / Desfinalizar

Antes ambas operaciones colgaban del permiso generico `WAREHOUSEDISPATCH`. Ahora:

| Permiso | Id func. | Para que rol |
|---|---|---|
| `WAREHOUSEDISPATCHFINALIZE` (468) | 5/1 | Operador que cierra el despacho. |
| `WAREHOUSEDISPATCHUNFINALIZE` (469) | 5/1 | **Supervisor** — revierte FINALIZADO → APROBADO. NO darlo a operadores. |

SQL en seccion 14 de [`query_v6.0.82_terdemol.sql`](../query/query_v6.0.82_terdemol.sql). Etiquetas i18n: `menu.warehouse.dispatch.finalize` = "Finalizar Despacho" y `menu.warehouse.dispatch.unfinalize` = "Desfinalizar Despacho".

Las asignaciones `derechoacceso` se hacen manualmente — son decision del usuario por rol.

### C. Layout de botones (convencion sistema)

Convencion confirmada por el usuario y que aplica a TODO el sistema, no solo a despachos:

> **Las barras superior e inferior del form deben ser ESPEJOS IDENTICOS en cualquier estado.** Los botones "intermedios" (especificos de una seccion, ej. Guardar Envases / Imprimir Detalle de Envases) van **solo arriba de esa seccion**, no abajo.

Aplicado en [`dispatchVoucherUpdate.xhtml`](../view/warehouse/dispatchVoucherUpdate.xhtml):
- TOP y BOTTOM: Save Draft / Aprobar / Anular / Finalizar / Desfinalizar / Imprimir Certificado / Imprimir Nota / Eliminar / Cancelar. Mismos `rendered` por estado.
- Intermedia (solo cuando `hasEnvelopes`): Guardar Envases / Imprimir Detalle de Envases / Cancelar — **solo encima del panel**, no debajo.
- Botones de impresion visibles en BORRADOR / APROBADO / FINALIZADO / ANULADO con `s:hasPermission('WAREHOUSEDISPATCH','VIEW')`.

### D. Listado de despachos — filtro por estado por defecto

[`DispatchVoucherDataModel`](../src/main/com/encens/khipus/action/warehouse/DispatchVoucherDataModel.java) tenia comportamiento de "filtrar siempre por BORRADOR" porque `WarehouseVoucherDispatch.state` tiene field initializer `= DispatchState.BORRADOR` y `QueryDataModel.initEntityQuery()` recrea el `criteria` via `getEntityClass().newInstance()`. Setear `criteria.state = null` en `@Create init()` no funciona — `initEntityQuery` lo sobreescribe despues.

**Fix:** override `createInstance()` para limpiar el `state` en cada creacion de instancia:

```java
@Override
public WarehouseVoucherDispatch createInstance() {
    WarehouseVoucherDispatch instance = super.createInstance();
    if (instance != null) {
        instance.setState(null);
    }
    return instance;
}
```

Patron reusable para cualquier QueryDataModel cuya entidad tenga un enum/state con default no deseado en el filtro.

---

## 10.ter Fase 12 — Hoja de Ruta (catalogos + reporte)

Reporte nuevo con catalogos propios: rutas reusables (paradas + mapa) y descripciones tecnicas reusables por producto. Ambos catalogos tienen ciclo de aprobacion con **estados inmutables** post-aprobacion para no alterar despachos historicos.

### A. Estados de los catalogos (politica)

Nuevo enum [`CatalogApprovalState`](../src/main/com/encens/khipus/model/warehouse/CatalogApprovalState.java): `BORRADOR -> APROBADO -> INACTIVO`. **Sin vuelta atras.**

- **BORRADOR**: editable, NO aparece en selectPopUps del despacho.
- **APROBADO**: inmutable, aparece en selectPopUps.
- **INACTIVO**: inmutable, no aparece (terminal).

Politica unica: si una entrada APROBADA necesita cambio, se inactiva y se crea nueva. No hay edicion bajo demanda. Esto preserva la inmutabilidad de los despachos historicos que referencian la entrada.

Patron en los Actions: `update()`, `delete()` y los inputs del form validan `isEditable()`; `approve()` exige `BORRADOR`, `inactivate()` exige `APROBADO`. El form muestra read-only en APROBADO/INACTIVO. Botones cambian segun estado: BORRADOR → Guardar/Aprobar/Eliminar; APROBADO → Inactivar; INACTIVO → nada.

### B. Catalogo `ProductDescription`

Tabla [`inv_descripcion_producto`](../query/query_v6.0.82_terdemol.sql) (seccion 15.a). Entidad [`ProductDescription`](../src/main/com/encens/khipus/model/warehouse/ProductDescription.java) con composite FK al producto via (`no_cia_art`, `cod_art`). Un producto puede tener N descripciones, varias APROBADAS simultaneamente.

- CRUD: [`ProductDescriptionAction`](../src/main/com/encens/khipus/action/warehouse/ProductDescriptionAction.java) + [`ProductDescriptionDataModel`](../src/main/com/encens/khipus/action/warehouse/ProductDescriptionDataModel.java) + [productDescriptionList.xhtml](../view/warehouse/productDescriptionList.xhtml) + [productDescription.xhtml](../view/warehouse/productDescription.xhtml).
- Permiso `PRODUCTDESCRIPTION` (id 470, CRUD bitmask=15).
- NamedQuery `ProductDescription.findApprovedByProduct(:companyNumber, :productItemCode)` para alimentar el dropdown del detalle del despacho.
- En el form del despacho: nueva columna en la grilla de detalle, dropdown filtrado por producto via `dispatchVoucherAction.getApprovedDescriptions(detail)`.

### C. Catalogo `DispatchRoute`

Tabla [`inv_ruta_despacho`](../query/query_v6.0.82_terdemol.sql) (seccion 15.b). Entidad [`DispatchRoute`](../src/main/com/encens/khipus/model/warehouse/DispatchRoute.java).

- **Paradas (`waypoints`)**: campo TEXT con separador ` - ` (decision de diseño - alternativa "entidad hija de stops" descartada por overhead innecesario).
- **Imagen del mapa**: LONGBLOB en el catalogo, NUNCA replicada al despacho. Una imagen por ruta, reutilizada por N despachos → la BD NO crece a medida de los despachos.
- **Resize+compresion al guardar**: [`ImageUtils.resizeAndCompress(bytes, 1024)`](../src/main/com/encens/khipus/util/ImageUtils.java) re-codifica a JPEG ~85% calidad max 1024px de ancho. Resultado tipico: 50-150 KB por mapa. Limite duro de entrada: 5 MB.
- Upload via `<s:fileUpload data="#{dispatchRouteAction.uploadedMapBytes}">`. El action procesa en `create()`/`update()`/`approve()` antes de persistir.
- Unique key `(idcompania, nombre)` evita duplicados.
- CRUD: [`DispatchRouteAction`](../src/main/com/encens/khipus/action/warehouse/DispatchRouteAction.java) + [`DispatchRouteDataModel`](../src/main/com/encens/khipus/action/warehouse/DispatchRouteDataModel.java) + [dispatchRouteList.xhtml](../view/warehouse/dispatchRouteList.xhtml) + [dispatchRoute.xhtml](../view/warehouse/dispatchRoute.xhtml).
- Permiso `WAREHOUSEDISPATCHROUTE` (id 471, CRUD bitmask=15).
- NamedQuery `DispatchRoute.findApproved` alimenta el dropdown del despacho.

### D. Cambios en el despacho

- `WarehouseVoucherDispatch`: nuevos campos `route` (FK `idruta`) + `validityDays` (`vigencia_dias`). Editables solo en BORRADOR.
- `WarehouseVoucherDispatchDetail`: nuevo campo `productDescription` (FK `iddescripcion_producto`). Selector por linea en la grilla, filtrado por el producto de la linea.
- Form: nueva seccion ITINERARIO ya tenia origen/destino - se agrego Ruta + Vigencia. Nueva columna "Descripcion (Hoja de Ruta)" en la grilla de detalle.

### E. Reporte "Hoja de Ruta"

[`DispatchRouteSheetReportAction`](../src/main/com/encens/khipus/action/warehouse/reports/DispatchRouteSheetReportAction.java) + [dispatchRouteSheetReport.jrxml](../view/warehouse/reports/dispatchRouteSheetReport.jrxml). Pagina Carta vertical, una sola pagina (title h=60 + summary h=630 + pageFooter h=14 = 704 ≤ 712 utiles).

**Estructura del summary band** (en orden vertical):
1. OPERADOR DE ORIGEN: empresa de transporte, conductor + licencia.
2. DATOS DE VEHICULO: color, marca, placa.
3. DATOS DE LA CARGA: descripcion (concatena `productDescription.description` de cada linea, con fallback a `productItem.name`) + cantidad total (suma de `details[].quantity`) + unidad (compartida; "Varios" si no coinciden).
4. ITINERARIO: lugar despacho/entrega (del catalogo `DispatchPlace`), paradas (del catalogo `DispatchRoute`), imagen del mapa.
5. Vigencia en dias.
6. Bloque firmas (Resp. Almacen + Conductor) usando el patron single styled textField (seccion 5).

**Imagen del mapa en JR**: parametro `routeMapImage` tipo `java.io.InputStream`, el action provee un `ByteArrayInputStream` sobre `route.mapImage`. Atributo `isUsingCache="false"` en el `<image>` para evitar reutilizacion del InputStream consumido.

### F. Boton "Imprimir Hoja de Ruta"

En barras top/bottom del form del despacho (mismos botones, convencion del sistema). Visible **solo en APROBADO + FINALIZADO** (no en BORRADOR ni ANULADO - decision del usuario porque la hoja solo cobra sentido con datos consolidados). Restringido por permiso `WAREHOUSEDISPATCHROUTESHEET` (id 472, VIEW bitmask=1).

### G. Decisiones tomadas (por si despues hay que recordar)

- **No hay snapshot del nombre/paradas de la ruta en el despacho**: la inmutabilidad del catalogo (APROBADO no se edita) ya garantiza que despachos historicos imprimen los datos originales. Si la ruta cambia, se INACTIVA y se crea nueva.
- **Sin asignaciones automaticas de permisos**: las 3 funcionalidades nuevas (470/471/472) se agregan a `funcionalidad` pero no a `derechoacceso`. El usuario asigna por rol manualmente.
- **No-edit-when-unused**: aunque tecnicamente una entrada APROBADA sin referencias podria editarse sin riesgo, la politica es uniforme (APROBADO = inmutable) para simplificar el modelo mental.

## 11. Pendientes conocidos

- **Asiento contable al aprobar despacho:** opciones A/B/C en plan original sección 15, sin decidir.
- **Permisos propios para botones de impresión (Certificado / Nota Remisión / Detalle Envases):** actualmente usan `WAREHOUSEDISPATCH,VIEW` genérico.
- **Reflejo del prefijo+código de cliente en el form de despacho:** el cliente seleccionado en Cliente/Transbordo muestra solo `codigo`. Si se quiere mostrar `prefijo + codigo` en el form, agregar a `textValue` del selectPopUp.
- **InventoryPackaging.averageGrossWeightKg en form catálogo:** se agregó input pero no aparece en la lista (puede agregarse columna si se requiere).
- **Mensaje viejo `NotaRemision.warehouseEmail`** quedó en `messages_app.properties` sin uso — se puede borrar si no se piensa reactivar el email en el bloque de firma.
