# Requerimiento: Modernización de Asientos Contables (Voucher/VoucherDetail)

> **Propósito de este documento:** especificación tipo requerimiento + memoria de contexto de
> todo lo realizado en la migración/mejora de los asientos contables. Sirve para retomar el
> trabajo en otra sesión sin perder el hilo. Al final está **todo lo pendiente** y los
> **pasos de despliegue**.

- **Rama:** `feat/voucher-detail-reorder`
- **Stack:** JBoss 5.1 / Seam 2.1 / Hibernate 3.2.4 / JSF 1.2 + RichFaces 3.3.3 / MySQL
- **Build:** SOLO con **JDK 1.8** (`ant explode`). El terminal por defecto es Java 21.
- **Migración BD asociada:** `query/query_v6.0.114_terdemol.sql` (bloques **A–H**).

---

## 0. Contexto y premisas (IMPORTANTE para continuar)

- **`Voucher` (tabla `sf_tmpenc`) y `VoucherDetail` (tabla `sf_tmpdet`) son las entidades de
  asiento contable PRINCIPALES y más usadas**, aunque su estructura sea legacy — es lo que se
  está modernizando. `AccountingRecord` / `AccountingDocument` (tabla `registrocontable`) es
  **secundario, casi vacío**; NO tomarlo como la referencia "moderna".
- **Compañía real = entidad `Company` (`idcompania`)**, obtenida de la sesión Seam
  `currentCompany` (seteada en login, `AuthenticatorAction`). El campo `no_cia = "01"` es
  **legacy** de una integración vieja; NO usarlo como compañía.
- **`hbm2ddl.auto = validate`**: toda columna nueva mapeada DEBE existir en la BD **antes** del
  deploy, o JBoss no arranca. Hibernate 3.2 valida **existencia Y tipo** de columna
  ("Missing column" / "Wrong column type").
- **Las funciones almacenadas NO se eliminan todavía** (respaldo). Su borrado es el bloque **G**
  de la migración, a ejecutar recién tras validar todo en marcha.
- El usuario **aplica los scripts SQL** (nunca los ejecuta el asistente). Los scripts van **sin
  prefijo de esquema** (producción usa otro esquema; se usa el default de la conexión) y con
  un solo parámetro `@company_id` arriba (ajustar al id real de la compañía).

---

## 1. Objetivos del requerimiento

1. Mejorar la **UX** de creación/edición de asientos (reordenar líneas, calendario, proveedor,
   facturas).
2. Cerrar problemas de **integridad del guardado** (no perder datos, no reportar éxito falso,
   validar cuadre y estado en servidor).
3. Migrar la generación de **ids y correlativos** (id_tmpenc, id_tmpdet, no_trans, no_doc, VALE,
   ventas) de **funciones almacenadas** (con condición de carrera) a **JPA/Hibernate**.
4. Preparar **multicompañía** real (entidad `Company`) y **concurrencia de edición**
   (bloqueo optimista `@Version`).
5. Todo **sin romper producción**.

---

## 2. Requerimientos implementados

### RF-01 — Reordenar líneas del asiento (UX) y persistir el orden
- Flechas ↑/↓ minimalistas por fila para reordenar cuentas (`voucherCreate.xhtml`).
- Métodos `moveVoucherDetailUp/Down`, `isFirst/isLastVoucherDetail` en `VoucherCreateAction`.
- **Persistencia del orden:** nueva columna **`sf_tmpdet.nro_orden`** (`VoucherDetail.orderNumber`,
  `Integer`). Al guardar (`saveVoucher`/`updateVoucher`) se asigna `nro_orden = posición en la
  lista`. La carga (`getVoucherDetailList`) ordena por **`nro_orden, id_tmpdet`**. Los asientos
  viejos (`nro_orden` NULL) conservan su orden por `id` hasta que se vuelvan a guardar.
- **El orden se hace persistente al GUARDAR** (las flechas solo reordenan en memoria; es
  coherente con el resto del formulario).
- **Migración:** bloque **H**.

### RF-02 — Integridad del guardado
- `create()`: valida **antes** de escribir; ante error hace rollback + limpia ids generados y
  devuelve `REDISPLAY` (NO cierra la conversación → no se pierde lo cargado; antes devolvía
  `FAIL` y se perdía todo, dejando facturas huérfanas).
- `update()`/`approveVoucher()`/`annulVoucher()`: validan **cuadre y estado (pendiente)** en el
  servidor; `update()` dejó de reportar éxito cuando en realidad falló.
- **Cuadre a 2 decimales** (`validateVoucherBalance` usa `setScale(2)`): elimina el falso
  "valores incorrectos" por ruido de sub-céntimos cuando debe==haber en pantalla.
- Facturas editadas en la grilla ahora **se persisten** (`purchaseList` se leía como
  `@Transient` y se descartaba → `updateVoucher` ahora las mergea).

### RF-03 — Factura duplicada (bloqueo)
- Criterio de duplicado: **NIT + nº factura + fecha + monto**. Se valida al agregar la factura
  (`addFiscalCreditCashAccount`) y al guardar/actualizar, contra la lista en memoria y contra la
  BD (`existsPurchaseDocument`, ignora anuladas y la propia en edición).
- **Botón de eliminar factura** en la grilla "Documento de compra" (`removePurchaseDocument`
  robusto): si tiene línea de crédito fiscal asociada quita ambas; si está huérfana (duplicado
  bloqueado, sin línea) solo la saca de la lista y borra de BD solo si estaba persistida.
  Antes el link estaba comentado porque `removeDocument` reventaba sobre una factura transitoria.

### RF-04 — id_tmpenc / id_tmpdet por JPA (Etapa A)
- `Voucher` / `VoucherDetail` generan el PK con **`@GeneratedValue(strategy=TABLE)`** (el
  `@TableGenerator` ya declarado sobre la tabla `secuencia`), reemplazando
  `newId_sf_tmpenc()/newId_sf_tmpdet()`.
- **Bug corregido:** `Voucher` tenía `allocationSize=2` (activaba modo hi/lo → ids saltados/
  colisión); se puso **1** (modo simple, verificado a nivel de bytecode del generador).
- Se quitó toda asignación manual de id en `VoucherAccoutingServiceBean` (saveVoucher,
  updateVoucher, createPurchaseDocumentVoucher) y en el path de asiento de
  `WarehouseAccountEntryServiceBean`.
- **Migración:** bloque **A** (ajuste `+1` de `secuencia` por diferencia de semántica: la función
  entregaba `valor+1`, el generador entrega `valor` y luego incrementa).

### RF-05 — no_trans / no_doc por JPA, POR COMPAÑÍA (Etapa B)
- Nueva entidad **`FinancesSequence`** (mapea la tabla existente `_sequence`) con **`@EmbeddedId`
  `FinancesSequenceId` (seq_name + idcompania)** y columna `version`.
  (Se usó `@EmbeddedId`, NO `@IdClass` — la convención del proyecto y `@IdClass` daba problemas.)
- `_sequence` evolucionó a **por-compañía**: columnas `idcompania` (FK a `compania`) + `version`,
  PK compuesta `(seq_name, idcompania)`.
- **Migración:** bloque **B** (agrega columnas, `seq_val` a BIGINT, PK compuesta, FK).

### RF-06 — idcompania en el asiento (entidad `Company` real)
- `Voucher` y `VoucherDetail` con **`@ManyToOne(optional=false) Company company`** sobre
  `idcompania`, estampada por **`CompanyListener`** con la `currentCompany` de la sesión.
  Reemplaza al `no_cia` legacy (que se mantiene por compatibilidad).
- **NO se agregó `@Filter(companyFilter)`** todavía (eso es el pendiente de aislamiento por
  compañía; ver Pendientes).
- **Migración:** bloques **C** (sf_tmpenc) y **D** (sf_tmpdet): `idcompania` NOT NULL + FK.

### RF-07 — @Version (concurrencia de EDICIÓN)
- `Voucher` y `VoucherDetail` con **`@Version private long version`** (columna `version`),
  convención de ~278 entidades del sistema.
- **Migración:** bloques **E** (sf_tmpenc) y **F** (sf_tmpdet): `version BIGINT NOT NULL DEFAULT 0`.

### RF-08 — Migrar VALE/ventas y dejar `getNextSeq` sin uso
- `getNextPK()` (VALE) y `getNextNoTransByDocumentType()` (ventas) migrados al mismo mecanismo
  JPA por compañía (`nextFinancesSequence`). También el caller directo
  `PayableDocumentServiceBean:292` (`getNextSeq('VALE')`).
- **`getNextSeq` quedó SIN llamadores.** Su borrado (junto a `newId_sf_*`, `next_tmpenc`,
  `sigte_trans`) es el bloque **G** (comentado, ejecutar tras validar).

### RF-09 — Concurrencia de edición: detección + recarga
- **Chequeo explícito de versión fresca antes de guardar:** `getPersistedVersion(id)` (query
  escalar, sin caché) vs la versión en memoria (`isVoucherStale()`), en `update/approve/annul`.
- Se **fuerza el avance de la versión del encabezado** en `updateVoucher`
  (`em.lock(voucher, WRITE)`), así un cambio solo en detalles también versiona el asiento.
- **Recarga real con `em.refresh`** (`refreshVoucher`/`refreshVoucherDetailList`) — antes
  `getVoucher(Long)` devolvía la entidad **cacheada** del PC extendido de Seam y no traía datos
  frescos.
- Resultado: el segundo guardado concurrente **avisa y recarga** en vez de pisar en silencio.
- Mensaje: `Voucher.message.concurrency`.

### RF-10 — Fix crítico: generar correlativos SIN flushear el PC (rompía Orden de Compra)
- Síntoma: al finalizar OC → `TransientObjectException` (WarehouseVoucher → Voucher transitorio)
  en `FinancesSequenceServiceBean.nextValue` (el `em.flush()`).
- Causa: el `em.flush()` del enfoque optimista **flusheaba todo el PC compartido** de la
  conversación, arrastrando entidades a medio guardar de otros flujos.
- Solución: `nextValue` reescrito con **UPDATE nativo atómico**
  (`update _sequence set seq_val = seq_val + 1 ... where seq_name and idcompania`), **sin
  `em.flush()`**, con `setFlushMode(COMMIT)`. Atómico (lock de fila → concurrency-safe), por
  compañía, no toca el PC del que llama. Replica el comportamiento seguro de la función vieja.
- **Nota de diseño:** se usa **pesimista/atómico para la secuencia** (contención corta y
  caliente) y **optimista `@Version` para la edición del asiento** (tiempo de pensar del
  usuario). Es la herramienta correcta para cada caso; NO es inconsistencia. La columna
  `version` de `_sequence` sigue y se incrementa, pero la exclusión la da el lock del UPDATE.

### RF-11 — Reporte "Generar documento" consistente
- El reporte carga los detalles por `voucher.voucherDetailList` (sin orden). Se seteó
  `sortProperty = "voucherDetail.orderNumber, voucherDetail.id"` en `VoucherReportAction` →
  el comprobante respeta el mismo orden que la pantalla.
- **"Generar documento" (asiento y listado) solo disponible con el asiento APROBADO**
  (`rendered="#{voucherCreateAction.approved}"` / `#{voucherItem.approved}`): un asiento
  aprobado es inmutable (controles de edición `rendered="pending"`), así **pantalla == BD** y el
  comprobante nunca sale inconsistente con cambios sin guardar.

### RF-12 — Ajustes UX varios
- **Fecha de factura** como `rich:calendar` (popup + tipeo manual `enableManualInput`).
- **Proveedor (R.Social):** patrón "seleccionar y bloquear" — una vez elegido se muestra fijo con
  ícono para re-elegir (`clearPurchaseDocumentProvider`); ya no se pierde la referencia por un
  carácter/espacio de más.
- **Eliminado el input obsoleto del número de documento** (al lado de la fecha) y la lógica
  muerta de override en `approveVoucher`. Se conserva el campo/columna `ndoc`.

### RF-13 — Fix ViewExpiredException (ajax tras reinicio)
- El `a4j:poll` del panel de notificaciones (iframe del template, en toda página) pegaba contra
  una vista expirada tras reiniciar → `ViewExpiredException` apenas se entraba.
- Fix en `web.xml`: **`org.ajax4jsf.handleViewExpiredOnClient = true`** (RichFaces 3.3.3 recarga
  la página en el cliente ante vista expirada en ajax). Requiere reiniciar JBoss.

### RF-14 — Limpieza de código muerto y deprecaciones
- **Eliminadas** 5 vistas inalcanzables: `voucherUpdate.xhtml`, `voucher.xhtml`,
  `accountingPurchaseDocument.xhtml`, `simplePurchaseDocument.xhtml`, `voucherCreate_bkp.xhtml`,
  y sus bloques en `pages.xml`. Verificado que **nada vivo** navega al cluster.
- **`VoucherUpdateAction` (la clase) NO se eliminó**: está inyectada por **22 clases** (reportes
  de balance) — no es código muerto. Solo su vista estaba desconectada.
- **`@Deprecated`**: `BankingMovementSync*` (importación de extractos, sin uso) y
  `VoucherEntriesApproveAction` (aprobación MASIVA por rango, sin uso y **no valida cuadre**).
- Eliminado `query/dump.sql` (archivo viejo; el usuario indicó no usarlo).

---

## 3. Cambios de base de datos — `query/query_v6.0.114_terdemol.sql`

Un solo parámetro arriba: `SET @company_id := 1;` (**ajustar al id real de la compañía**).

| Bloque | Tabla | Cambio | Motivo |
|---|---|---|---|
| **A** | `secuencia` | `UPDATE valor = valor + 1` para `sf_tmpenc`, `sf_tmpdet` | Ajuste por semántica del @TableGenerator |
| **B** | `_sequence` | `seq_val`→BIGINT, + `idcompania` (FK), + `version`, PK `(seq_name, idcompania)` | no_trans/no_doc/VALE/ventas por compañía (JPA) |
| **C** | `sf_tmpenc` | + `idcompania` NOT NULL + FK a `compania` | Voucher @ManyToOne Company |
| **D** | `sf_tmpdet` | + `idcompania` NOT NULL + FK a `compania` | VoucherDetail @ManyToOne Company |
| **E** | `sf_tmpenc` | + `version` BIGINT NOT NULL DEFAULT 0 | @Version (concurrencia de edición) |
| **F** | `sf_tmpdet` | + `version` BIGINT NOT NULL DEFAULT 0 | @Version |
| **H** | `sf_tmpdet` | + `nro_orden` INT NULL | Persistir reordenamiento de líneas |
| **G** | (funciones) | `DROP FUNCTION` (COMENTADO) | Limpieza, **solo tras validar** |

> **Orden de despliegue:** aplicar **A–H** con el sistema detenido → redeploy del EAR →
> reiniciar JBoss. **G** recién cuando la migración esté validada en marcha.
> **NO borrar** `sigte_conci` (conciliación) ni `newId_inv_inventario_detalle` (inventario):
> siguen en uso, son otra etapa.

---

## 3.1 — Estado de las funciones almacenadas MySQL

Verificado contra el código (llamadores vivos), no contra el servidor. Del lado del código
determina qué es seguro borrar. Si hubiera *triggers*/*vistas* en la BD que invoquen alguna, no
se ven desde el código (para funciones de secuencia es muy improbable); se puede cruzar con un
`SELECT` sobre `information_schema.ROUTINES` del lado del servidor.

### A) Funciones YA MIGRADAS (sin uso — borrables en el bloque G, tras validar)

| Función MySQL | Antes generaba | Reemplazo (JPA/Hibernate) | Cómo quedó sin uso |
|---|---|---|---|
| `getNextSeq(nombre)` | `no_trans` (ASIENTO), `no_doc` (tipo doc), `VALE`, ventas | `FinancesSequence` (tabla `_sequence`, por compañía, UPDATE atómico) | `getNextPK`, `getNextNoTransByDocumentType`, `getNextNoTransTmpenc`, `getNextDocumentNumberByType` y `PayableDocumentServiceBean:292` migrados |
| `newId_sf_tmpenc()` | PK `sf_tmpenc.id_tmpenc` | `@GeneratedValue(TABLE)` sobre `secuencia` | Asignación manual eliminada de todos los escritores |
| `newId_sf_tmpdet()` | PK `sf_tmpdet.id_tmpdet` | `@GeneratedValue(TABLE)` sobre `secuencia` | Idem (llamadores restantes están en bloques comentados) |
| `next_tmpenc()` | `no_trans` (vía `getNextTmpenc()`) | — (no se reemplazó; ya no se invocaba) | `getNextTmpenc()` sin llamadores vivos |
| `sigte_trans()` | correlativo de `inv_vales` | — | Nunca referenciada en el código |
| `sp_setSeqVal(...)` (procedure) | fijaba el valor de `VALE` | — | `setNextPK()` sin llamadores vivos |

**Borrado (bloque G de `query_v6.0.114`, COMENTADO — ejecutar solo tras validar en marcha):**
```sql
DROP FUNCTION IF EXISTS getNextSeq;
DROP FUNCTION IF EXISTS newId_sf_tmpenc;
DROP FUNCTION IF EXISTS newId_sf_tmpdet;
DROP FUNCTION IF EXISTS next_tmpenc;
DROP FUNCTION IF EXISTS sigte_trans;
-- sp_setSeqVal: opcional (procedure sin uso)
```

### B) Funciones que SIGUEN EN USO — REQUERIMIENTO de migración (otra etapa)

| Función MySQL | Usada por (archivo:línea) | Genera | Requerimiento de migración |
|---|---|---|---|
| `sigte_conci()` | `service/finances/PayableDocumentServiceBean.java:222` | Número de conciliación de documentos por pagar | **REQ-MIG-01** |
| `newId_inv_inventario_detalle()` | `service/warehouse/ApprovalWarehouseVoucherServiceBean.java:1355` | PK de `inv_inventario_detalle` | **REQ-MIG-02** |

#### REQ-MIG-01 — Migrar `sigte_conci()` (número de conciliación)
- **Estado:** en uso; **su código fuente NO está en el repo** (solo existe en la BD).
- **Riesgo:** misma clase de condición de carrera que las demás funciones (SELECT+UPDATE sin
  bloqueo) si internamente hace `MAX()+1` o lee/escribe sin lock.
- **Pasos sugeridos:**
  1. Extraer la definición de la BD: `SHOW CREATE FUNCTION sigte_conci;` — entender sobre qué
     tabla/columna opera y con qué semántica.
  2. Definir si el correlativo debe ser por compañía (como los de asiento) o global.
  3. Migrar la generación a JPA: reutilizar `FinancesSequence`/`FinancesSequenceService`
     (`nextValue(nombre, companyId)`, UPDATE atómico sin flush) o el mecanismo `Sequence`/
     `gensecuencia` si corresponde a un correlativo de negocio.
  4. Cambiar `PayableDocumentServiceBean:222` para usar el nuevo generador.
  5. Recién entonces `DROP FUNCTION sigte_conci`.
- **Verificar:** que no haya OTROS llamadores (hoy solo `:222`) ni *triggers* en la BD.

#### REQ-MIG-02 — Migrar `newId_inv_inventario_detalle()` (PK de detalle de inventario)
- **Estado:** en uso; opera sobre la tabla `secuencia` (fila `inv_inventario_detalle`), igual que
  `newId_sf_*`.
- **Pasos sugeridos:**
  1. Preferido: mapear `inv_inventario_detalle` como entidad JPA y usar `@GeneratedValue(TABLE)`
     con `@TableGenerator` sobre `secuencia` (`pkColumnValue = "inv_inventario_detalle"`), igual
     que se hizo con `id_tmpenc`/`id_tmpdet` (RF-04). Aplicar el mismo ajuste `+1` si aplica.
  2. Quitar la asignación manual en `ApprovalWarehouseVoucherServiceBean:1355` (es un
     `INSERT` nativo `inv_inventario_detalle`; requiere revisar si ese insert puede pasar a JPA
     o si se mantiene nativo con un contador atómico).
  3. Recién entonces `DROP FUNCTION newId_inv_inventario_detalle`.
- **Ojo:** `ReverseInventoryServiceBean` genera el mismo id con `MAX(id_inv_det)+1` en Java —
  revisar para que no diverja del contador.

---

## 4. Commits de la rama (más reciente primero)

```
d5338852 fix(web): ViewExpiredException de ajax tras reinicio
b15cc782 feat(accounting): orden persistente, reporte ordenado, comprobante solo aprobado
475a6952 fix(finances): correlativos sin flushear el PC (rompia Orden de Compra)
5c875296 chore(accounting): deprecar aprobacion masiva y eliminar vistas muertas
f2f398b3 feat(accounting): secuencias por JPA/compania, @Version y UX asientos
f5db034e feat(accounting): id_tmpenc/id_tmpdet por JPA
e44287ff feat(accounting): reordenar lineas, integridad del guardado y facturas duplicadas
```

---

## 5. Archivos clave (para navegar el código)

- **Entidades:** `model/finances/Voucher.java`, `VoucherDetail.java`, `FinancesSequence.java`,
  `FinancesSequenceId.java`.
- **Acción principal:** `action/accounting/VoucherCreateAction.java`.
- **Servicios:** `service/accouting/VoucherAccoutingServiceBean.java`,
  `service/finances/FinancesPkGeneratorServiceBean.java`,
  `service/finances/FinancesSequenceServiceBean.java`.
- **Vistas:** `view/accounting/voucherCreate.xhtml`, `voucherList.xhtml`.
- **Reporte:** `action/accounting/reports/VoucherReportAction.java`.
- **Config:** `resources/WEB-INF/web.xml`, `resources/WEB-INF/accounting/pages.xml`,
  `model/package-info.java` (`@FilterDef companyFilter` = `idcompania = :currentCompanyId`).

---

## 6. Estado de pruebas

- **Compila** con JDK 1.8.
- **Arneses de regresión** (en el scratchpad de la sesión, no en el repo): totales/cuadre,
  reordenamiento, factura duplicada — en verde. La generación real de secuencias/ids y el
  bloqueo requieren el contenedor.
- **Validado por el usuario en la app:** concurrencia de edición (avisa y recarga), factura
  duplicada + monto, finalización de OC (tras el fix RF-10), orden persistente, reporte ordenado,
  comprobante solo en aprobado, desaparición del ViewExpired.

---

## 7. PENDIENTE (para continuar)

### P-01 — Aislamiento por compañía (`@Filter companyFilter`) — NO arrancado
Objetivo: que las lecturas de asientos devuelvan solo la compañía en sesión. Infra lista
(`@FilterDef` existe, `idcompania` ya está en las tablas). El `@Filter` de Hibernate **solo
aplica a JPQL/HQL, NO a SQL nativo**. Superficie relevada:
- JPQL (~28 en `VoucherAccoutingServiceBean` + ~10 rutas de reportes/datamodels): se
  **auto-filtran** al activar `@Filter`.
- **~11 consultas NATIVAS** sobre `sf_tmpenc`/`sf_tmpdet` requieren condición manual de compañía:
  `VoucherServiceBean` (`getTransactionMajorAccounting`, `getTransactionsByAccountCodes`,
  `getMinMaxNumber`), `VoucherAccoutingServiceBean` (`getValuedInventory`,
  `getInvalidValuedInventoryEntries`, `getWarehouseValuedPhysical`, `getUnitCost_milkProducts`,
  `getNextMaxNumberByDocType`), `InventoryReconciliationServiceBean`,
  `CashAccountServiceBean.findAccountReferences`, **`FinanceDashboardServlet`** (JDBC crudo, sin
  sesión — el más crítico, "SIN RESTRICCIONES").
- **Reportes:** ningún `.jrxml` tiene SQL propio (todos vía Java). 2 con fuga cross-company por
  origen nativo: **majorAccountingReport** y **dailyCollectionReport** (se arreglan al corregir
  sus consultas nativas).
- **Decisión pendiente:** `Voucher2`/`VoucherDetail2` (entidades duplicadas sobre las mismas
  tablas, usadas por `VoucherDataModel2`): consolidar o filtrar también.
- Fases sugeridas: (1) activar `@Filter`, (2) nativas P0 (fugas + numeración), (3) nativas P1
  (inventario/costeo), (4) P2 + Voucher2, (5) testing multicompañía.

### P-02 — Bloque G de la migración (DROP de funciones)
`getNextSeq`, `newId_sf_tmpenc`, `newId_sf_tmpdet`, `next_tmpenc`, `sigte_trans` — ejecutar
**solo tras validar** todo en producción.

### P-03 — Correlativos de otros módulos (otra etapa)
- `sigte_conci` (conciliación, `PayableDocumentServiceBean`): su código **no está en el repo**
  (extraer de la BD antes de migrar/borrar).
- `newId_inv_inventario_detalle` (PK de detalle de inventario): migrar a `@TableGenerator`.

### P-04 — Retiros diferidos
- `BankingMovementSync` (deprecado): borrar paquete + entrada de menú + permiso cuando se confirme.
- Cluster de vistas ya borrado; **`VoucherUpdateAction` la clase queda** (22 reportes la usan).

### P-05 — Aprobación masiva sin validación de cuadre
`VoucherEntriesApproveAction.approvedAllVoucherEntries` (deprecado): si se reactiva, agregar
validación de cuadre por asiento (definir: saltar los descuadrados y avisar, o abortar el lote).

### P-06 — Menores
- El path de almacén (`WarehouseAccountEntryServiceBean`) no setea `nro_orden` en sus detalles
  (quedan por `id`, que para asientos programáticos es correcto). Setearlo si se quiere uniformar.
- Si se necesita **vista previa** del comprobante de un asiento pendiente (hoy solo aprobado):
  imprimir el estado en memoria con leyenda "borrador".

---

## 8. Cómo retomar (checklist)

1. Estar en la rama `feat/voucher-detail-reorder`.
2. Compilar con **JDK 1.8** (`ant explode`).
3. Antes de cualquier deploy nuevo: aplicar los bloques faltantes de `query_v6.0.114` (con
   `@company_id` correcto), redeploy, **reiniciar JBoss**.
4. Para el aislamiento por compañía (P-01): empezar por activar `@Filter` en `Voucher`/
   `VoucherDetail` y luego las nativas P0. Confirmar con el usuario el alcance (toca reportes).
5. Recordar: **el usuario aplica los SQL**, nunca el asistente; y **no commitear sin confirmar**.
