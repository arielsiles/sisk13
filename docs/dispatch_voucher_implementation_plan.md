# Plan de Implementación — Vale de Egreso por Despacho de Productos Terminados

**Identificador:** DESPACHO-PT
**Rama:** `dev_dispatch_voucher` (a partir de `dev_terdemol`)
**Stack:** JBoss Seam 2.1 + JSF 1.2 + RichFaces + Hibernate 3.x + MySQL — sin cambios al stack.
**Build:** Apache Ant con **JDK 1.8** (ver [feedback_jdk8_build](../../../C%3A/Users/Precision%207710/.claude/projects/d--Intellij-sisk13/memory/feedback_jdk8_build.md)).
**Requerimientos:** [dispatch_voucher_requirements.md](dispatch_voucher_requirements.md).

---

## 0. Decisión arquitectural

**Crear un módulo nuevo de "Despacho" que se apoya en el flujo de vales existente, sin modificar `WarehouseVoucherCreate/Update`.**

### Razones

1. **Aislar el dominio nuevo.** El despacho tiene ~20 campos específicos (conductor, vehículo, balanza, bolsas, lugares, turno, lote-venta) que no encajan en `WarehouseVoucher`.
2. **Evitar regresión.** [warehouseVoucherCreate.xhtml](../view/warehouse/warehouseVoucherCreate.xhtml) ya gobierna 7+ tipos mediante `rendered=` sobre `documentType`; [warehouseVoucherUpdate.xhtml](../view/warehouse/warehouseVoucherUpdate.xhtml) tiene 3 ramas en un `c:choose`. Agregar una cuarta rama de ~500 líneas crearía deuda técnica garantizada.
3. **Reusar el motor.** El descuento de inventario, asiento contable, estados y reversión se delegan al servicio existente (`approvalWarehouseVoucherService`, `ReverseInventoryService`). El despacho **genera un `WarehouseVoucher` de tipo egreso al aprobarse** y lo enlaza vía FK.
4. **Reporte específico.** El "Certificado de Recepción de Carguío" tiene layout propio; tiene su Jasper dedicado.

### Esquema conceptual

```
┌──────────────────────────────┐
│ WarehouseVoucherDispatch     │  ← entidad nueva (cabecera del despacho)
│  - state: BORRADOR/APR/ANL   │
│  - deliveryOrderNumber       │
│  - cliente, transportadora   │
│  - conductor, vehículo       │
│  - pesos, bolsas, lote       │
│  - lugar origen/destino      │
│  - turno (ProductionGroup)   │
│  - warehouse, costCenter     │
│  - warehouseVoucher (FK) ────┼─┐
└──────────────┬───────────────┘ │
               │ 1..N            │
               ▼                 │
┌──────────────────────────────┐ │
│ WarehouseVoucherDispatchDetail│ │
│  - productItem, qty, costo   │ │
└──────────────────────────────┘ │
                                 │
       ┌─────────────────────────┘
       ▼
┌──────────────────────────────┐
│ WarehouseVoucher (existente)  │  ← se crea AL APROBAR el despacho
│  + InventoryMovement          │
│  + MovementDetail[]           │
└──────────────────────────────┘
```

---

## 1. Fases de implementación

### Estado

| Fase | Estado | Commit |
|---|---|---|
| Fase 0 — Schema, sequence, permisos, i18n | ✅ Completa | `cdee03d6` |
| Fase 1 — Modelo (entidades + enums) | ✅ Completa | `32fa760c` |
| Fase 2 — Catálogo DispatchPlace (CRUD) | ✅ Completa | `7ef80aab` |
| Fase 3 — Servicio + Action de creación/edición | ✅ Completa | `fd406275` |
| Fase 4 — Vistas BORRADOR (create + update + list) | ✅ Completa | `462ac27e` |
| Fase 5 — Aprobación con doble confirmación | ✅ Completa | `0cb1a9d8` |
| Fase 6 — Anulación / reversión | ✅ Completa | `1ef2800b` |
| Fase 7 — Reporte Jasper (Certificado) | ✅ Completa | `56a3e197` |
| Fase 8 — Pruebas integradas y QA | Pendiente (manual en dev) | — |

Cada fase produce un commit (o pequeña serie) revisable. Las dependencias son lineales salvo las indicadas.

---

## 2. Fase 0 — Esquema de base de datos, sequence, permisos, i18n

### 2.1 DDL (archivo: `query/dispatch_voucher_v1.sql`)

```sql
-- =========================================================
-- DDL Vale de Despacho de Productos Terminados (v1)
-- =========================================================

CREATE TABLE inv_lugardespacho (
  idlugardespacho     BIGINT       NOT NULL,
  no_cia              SMALLINT     NOT NULL,
  codigo              VARCHAR(20)  NOT NULL,
  descripcion         VARCHAR(150) NOT NULL,
  direccion           VARCHAR(250),
  tipo                VARCHAR(20)  NOT NULL,  -- ORIGEN | DESTINO | AMBOS
  activo              BOOLEAN      NOT NULL DEFAULT TRUE,
  createdby           VARCHAR(40),
  createddate         DATETIME,
  updatedby           VARCHAR(40),
  updateddate         DATETIME,
  version             INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (idlugardespacho, no_cia),
  UNIQUE KEY uq_lugardespacho_cod (no_cia, codigo)
);

CREATE TABLE inv_valedespacho (
  idvaledespacho      BIGINT       NOT NULL,
  no_cia              SMALLINT     NOT NULL,
  estado              VARCHAR(15)  NOT NULL,
  no_orden_entrega    BIGINT,
  fecha_despacho      DATETIME     NOT NULL,
  idvendedor          BIGINT,
  no_cia_vendedor     SMALLINT,
  codigo_lote_venta   VARCHAR(80)  NOT NULL,
  cantidad_bolsas     INT          NOT NULL,
  numero_factura      VARCHAR(50),
  idtransportadora    BIGINT,
  idcliente           BIGINT,
  idturno             BIGINT,
  idlugar_origen      BIGINT,
  idlugar_destino     BIGINT,
  conductor_nombre    VARCHAR(120) NOT NULL,
  conductor_licencia  VARCHAR(30)  NOT NULL,
  conductor_celular   VARCHAR(30),
  vehiculo_placa      VARCHAR(20)  NOT NULL,
  vehiculo_marca      VARCHAR(50),
  vehiculo_color      VARCHAR(30),
  hora_inicio         DATETIME     NOT NULL,
  hora_fin            DATETIME     NOT NULL,
  no_camion           INT          NOT NULL,
  no_boleta_balanza   VARCHAR(30)  NOT NULL,
  peso_tara_kg        DECIMAL(12,3) NOT NULL,
  peso_bruto_kg       DECIMAL(12,3) NOT NULL,
  peso_neto_kg        DECIMAL(12,3) NOT NULL,
  bolsa_desde         INT          NOT NULL,
  bolsa_hasta         INT          NOT NULL,
  cod_alm             VARCHAR(20),
  idresponsable       BIGINT,
  idunidadej          BIGINT,
  idcentrocosto       BIGINT,
  no_trans_vale       BIGINT,         -- FK al WarehouseVoucher generado
  no_cia_vale         SMALLINT,
  observacion         VARCHAR(500),
  createdby           VARCHAR(40),
  createddate         DATETIME,
  updatedby           VARCHAR(40),
  updateddate         DATETIME,
  version             INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (idvaledespacho, no_cia),
  KEY idx_valedespacho_estado (no_cia, estado),
  KEY idx_valedespacho_fecha (no_cia, fecha_despacho),
  KEY idx_valedespacho_orden (no_cia, no_orden_entrega)
);

CREATE TABLE inv_valedespacho_detalle (
  iddetalledespacho   BIGINT       NOT NULL,
  no_cia              SMALLINT     NOT NULL,
  idvaledespacho      BIGINT       NOT NULL,
  no_cia_padre        SMALLINT     NOT NULL,
  cod_art             VARCHAR(20)  NOT NULL,
  cod_alm             VARCHAR(20)  NOT NULL,
  cod_uso             VARCHAR(10)  NOT NULL,
  cantidad            DECIMAL(14,6) NOT NULL,
  costo_unitario      DECIMAL(14,6) NOT NULL DEFAULT 0,
  monto               DECIMAL(14,6) NOT NULL DEFAULT 0,
  cantidad_bolsas     INT,
  observacion         VARCHAR(250),
  PRIMARY KEY (iddetalledespacho, no_cia),
  KEY fk_detalledespacho_valedespacho (idvaledespacho, no_cia_padre),
  CONSTRAINT fk_detalledespacho_valedespacho
    FOREIGN KEY (idvaledespacho, no_cia_padre)
    REFERENCES inv_valedespacho (idvaledespacho, no_cia)
);

-- Sequence dedicado para N° de Orden de Entrega
INSERT INTO gensecuencia (nombre, valor, idcompania)
SELECT 'DISPATCH_ORDER_NUMBER', 0, idcompania FROM admcompania;

-- Permisos
INSERT INTO permiso (codigo, descripcion, idmodulo) VALUES
  ('WAREHOUSEDISPATCH',         'Despacho de Productos Terminados',         /*idmodulo warehouse*/),
  ('WAREHOUSEDISPATCHAPPROVAL', 'Aprobación de Despacho',                   /*idmodulo warehouse*/),
  ('WAREHOUSEDISPATCHREVERSE',  'Anulación / Reversión de Despacho',        /*idmodulo warehouse*/),
  ('WAREHOUSEDISPATCHPLACE',    'Catálogo Lugares de Despacho/Entrega',     /*idmodulo warehouse*/);
```

> **Nota:** los nombres de tabla siguen el prefijo `inv_` usado por el módulo. Confirmar `idmodulo` exacto del módulo Warehouse antes del insert (revisar tabla `modulo`).

### 2.2 Sequence

Reutiliza tabla `gensecuencia` con nombre `DISPATCH_ORDER_NUMBER` por empresa. Se invoca:

```java
long next = sequenceGeneratorService.nextValue("DISPATCH_ORDER_NUMBER");
```

### 2.3 i18n

Añadir las claves descritas en sección 9 del documento de requerimientos a `resources/messages_es.properties` (rama ES). Si existe `messages_en.properties`, agregar también traducciones EN aunque sea provisional.

---

## 3. Fase 1 — Modelo

### 3.1 Estructura

```
src/main/com/encens/khipus/model/warehouse/
├── WarehouseVoucherDispatch.java
├── WarehouseVoucherDispatchPk.java
├── WarehouseVoucherDispatchDetail.java
├── WarehouseVoucherDispatchDetailPk.java
├── DispatchState.java                  (enum)
├── DispatchPlace.java
├── DispatchPlacePk.java
└── DispatchPlaceKind.java              (enum)
```

### 3.2 Convenciones

- PK compuesta `(id, companyNumber)` con `@TableGenerator` apuntando a `gensecuencia` (igual `Sequence.java`).
- `@CompanyListener` para asignar `companyNumber` automáticamente.
- `@Filter(name = COMPANY_FILTER_NAME)` para aislar por empresa.
- Implementar `BaseModel`.
- `@NamedQueries` para las consultas de listado y búsquedas.
- Sin lógica de negocio en el modelo; getters/setters + equals/hashCode + toString.

### 3.3 Entidades resumidas

- **`WarehouseVoucherDispatch`** — ver Sec. 4.1 del documento de requerimientos. Relación `@OneToMany` con `dispatchDetails` (cascade ALL, orphanRemoval=true). FK opcional a `WarehouseVoucher` (vale generado al aprobar).
- **`WarehouseVoucherDispatchDetail`** — ver Sec. 4.2.
- **`DispatchPlace`** — ver Sec. 4.3.

### 3.4 Enums

- `DispatchState { BORRADOR, APROBADO, ANULADO }` con `resourceKey` (mismo patrón que `WarehouseVoucherState`).
- `DispatchPlaceKind { ORIGEN, DESTINO, AMBOS }` con `resourceKey`.

---

## 4. Fase 2 — Catálogo `DispatchPlace` (CRUD)

Mismo patrón que cualquier catálogo del proyecto (referencia: `CostCenter`, `MeasureUnit`).

### 4.1 Archivos

```
src/main/com/encens/khipus/
├── action/warehouse/
│   ├── DispatchPlaceAction.java        (@Name("dispatchPlaceAction"), Scope CONVERSATION)
│   └── DispatchPlaceDataModel.java     (paginado, filtros)
├── service/warehouse/
│   ├── DispatchPlaceService.java
│   └── DispatchPlaceServiceBean.java
└── view/warehouse/
    ├── dispatchPlaceList.xhtml
    ├── dispatchPlaceCreate.xhtml
    └── dispatchPlaceUpdate.xhtml
```

### 4.2 Navegación

Entrada en `resources/WEB-INF/warehouse/pages.xml` siguiendo el patrón existente:

```xml
<page view-id="/warehouse/dispatchPlaceList.xhtml" login-required="true">
  <restrict>#{s:hasPermission('WAREHOUSEDISPATCHPLACE','VIEW')}</restrict>
  ...
</page>
```

Entrada en el menú lateral (`view/layout/menu.xhtml` o donde corresponda) bajo Almacenes → "Lugares de Despacho/Entrega".

---

## 5. Fase 3 — Servicio + Action de creación/edición

### 5.1 Servicios

```
src/main/com/encens/khipus/service/warehouse/
├── DispatchVoucherService.java         (interface)
└── DispatchVoucherServiceBean.java     (@Stateless)
```

**API mínima:**

```java
public interface DispatchVoucherService {
    WarehouseVoucherDispatch save(WarehouseVoucherDispatch dispatch);
    WarehouseVoucherDispatch update(WarehouseVoucherDispatch dispatch);
    void delete(WarehouseVoucherDispatch dispatch);
    WarehouseVoucherDispatch findById(Object pk);
    BigDecimal getAvailableStock(Warehouse warehouse, ProductItem productItem);

    /**
     * Aprueba el despacho: genera el WarehouseVoucher de egreso,
     * lo aprueba (descuento de inventario + asiento contable),
     * asigna número de orden de entrega y vincula el vale.
     * Transaccional REQUIRES_NEW; rollback total ante error.
     */
    WarehouseVoucherDispatch approve(WarehouseVoucherDispatch dispatch)
        throws InventoryException, InsufficientStockException, MonthProcessClosedException;

    /**
     * Revierte un despacho aprobado:
     * delega en el ReverseInventoryService existente sobre el WarehouseVoucher asociado.
     */
    WarehouseVoucherDispatch annul(WarehouseVoucherDispatch dispatch, String reason)
        throws ReversalException;
}
```

### 5.2 Action de cabecera

```
src/main/com/encens/khipus/action/warehouse/
├── DispatchVoucherAction.java          (@Name("dispatchVoucherAction"), Scope CONVERSATION)
└── DispatchVoucherDataModel.java       (listado paginado)
```

**Responsabilidades de `DispatchVoucherAction`:**
- `create()` — guardar en BORRADOR.
- `update()` — actualizar BORRADOR.
- `delete()` — eliminar BORRADOR.
- `addProductItem(ProductItem)` / `removeDetail(WarehouseVoucherDispatchDetail)` — gestión de líneas.
- `recalculateNetWeight()` — `peso_bruto - peso_tara`.
- `recalculateBagCount()` — `bolsa_hasta - bolsa_desde + 1`.
- `autoFillResponsibleFromWarehouse()` — al asignar warehouse asigna responsible y executorUnit (mismo patrón que vales).
- `prepareApprove()` — dispara el modal de doble confirmación (panel 1).
- `confirmStep1()` — pasa al panel 2 (calcula impacto de stock).
- `confirmStep2()` — llama a `dispatchVoucherService.approve(...)`.
- `prepareAnnul(reason)` / `confirmAnnul()` — invoca `annul`.

### 5.3 Validaciones

Las validaciones V01-V08 viven en `DispatchVoucherAction.validate()`. V09-V12 se re-ejecutan dentro de `DispatchVoucherServiceBean.approve()` para defensa en profundidad (no confiar solo en el cliente).

---

## 6. Fase 4 — Vistas BORRADOR (create, update, list)

### 6.1 Archivos

```
view/warehouse/
├── dispatchVoucherList.xhtml
├── dispatchVoucherCreate.xhtml
├── dispatchVoucherUpdate.xhtml
└── productItemsByDispatchWarehouseListModalPanel.xhtml   (si requiere variante específica)
```

### 6.2 Patrones obligatorios

- Usar `app:quickSearch` y `app:selectPopUp` igual que [warehouseVoucherCreate.xhtml](../view/warehouse/warehouseVoucherCreate.xhtml).
- `s:decorate template="/include/inputField.xhtml"` para cada campo.
- `a4j:support event="onblur"` para recalcular peso neto y cantidad de bolsas en tiempo real.
- Pestaña / modal `productItemsByWarehouseListModalPanel` para agregar líneas, filtrando por almacén y productos terminados.
- Botón principal "Guardar Borrador" y "Aprobar Despacho" (solo si BORRADOR y permiso).

### 6.3 Reglas de render

- En BORRADOR: todos los campos editables.
- En APROBADO/ANULADO: todos los campos read-only (mismo patrón `disabled="#{readOnly}"`).
- Botón "Imprimir Certificado" visible solo cuando estado = APROBADO o ANULADO (en ANULADO debe imprimirse con la marca "ANULADO").
- Botón "Anular" visible solo cuando estado = APROBADO y `s:hasPermission('WAREHOUSEDISPATCHREVERSE','VIEW')`.

---

## 7. Fase 5 — Aprobación con doble confirmación

### 7.1 UI (modales)

Dos `rich:modalPanel` en `dispatchVoucherCreate.xhtml`/`dispatchVoucherUpdate.xhtml`:

1. **`approveConfirmStep1ModalPanel`** — Resumen de cabecera (cliente, transportista, conductor, vehículo, pesos, lote, turno, lugares). Texto explicativo. Botones `[Cancelar]` `[Continuar]`.
2. **`approveConfirmStep2ModalPanel`** — Tabla de productos con columnas (Item, Cantidad, Saldo actual, Saldo resultante, Costo Unit, Monto). Texto de advertencia "esta acción solo puede revertirse mediante anulación con permiso especial". Botones `[Volver]` `[Confirmar aprobación definitivamente]`.

El paso entre paneles es vía `a4j:commandButton` con `oncomplete="Richfaces.showModalPanel('...')"` y `reRender` apropiado.

### 7.2 Flujo en el Action

```java
public String prepareApprove() {
    validate();                    // V01-V08
    if (hasErrors()) return REDISPLAY;
    showStep1 = true;              // dispara modal 1
    return null;
}

public String confirmStep1() {
    impactPreview = dispatchVoucherService
        .calculateInventoryImpact(dispatch);   // saldo actual vs resultante
    showStep1 = false;
    showStep2 = true;
    return null;
}

public String confirmStep2() {
    try {
        dispatch = dispatchVoucherService.approve(dispatch);
        facesMessages.add(INFO, "WarehouseDispatch.approve.success",
                          dispatch.getDeliveryOrderNumber());
        return SUCCESS;
    } catch (InsufficientStockException e) {
        addStockMessages(e);
        return REDISPLAY;
    } catch (Exception e) {
        log.error("Error aprobando despacho", e);
        facesMessages.add(ERROR, "WarehouseDispatch.approve.error");
        return FAIL;
    } finally {
        showStep2 = false;
    }
}
```

### 7.3 Generación del WarehouseVoucher al aprobar (`DispatchVoucherServiceBean.approve`)

```java
@TransactionAttribute(REQUIRES_NEW)
public WarehouseVoucherDispatch approve(WarehouseVoucherDispatch d) {
    // 1. Re-validar V09-V12
    validateForApproval(d);

    // 2. Asignar número de orden de entrega
    if (d.getDeliveryOrderNumber() == null) {
        d.setDeliveryOrderNumber(sequenceGeneratorService.nextValue("DISPATCH_ORDER_NUMBER"));
    }

    // 3. Construir WarehouseVoucher de egreso
    WarehouseVoucher vale = new WarehouseVoucher();
    vale.setDate(d.getDispatchDate());
    vale.setExecutorUnit(d.getExecutorUnit());
    vale.setCostCenter(d.getCostCenter());
    vale.setWarehouse(d.getWarehouse());
    vale.setResponsible(d.getResponsible());
    vale.setState(WarehouseVoucherState.PEN);
    vale.setDocumentType(warehouseService.findWarehouseDocumentType(WarehouseVoucherType.C));  // o tipo dedicado DESP
    vale.setOperation(VoucherOperation.DESP);  // nuevo enum, ver §11

    InventoryMovement inv = new InventoryMovement();
    inv.setCreationDate(new Date());
    inv.setMovementDate(d.getDispatchDate());
    inv.setDescription("Despacho N° " + d.getDeliveryOrderNumber()
                       + " - Cliente: " + d.getClient().getFullName());

    List<MovementDetail> details = new ArrayList<>();
    for (WarehouseVoucherDispatchDetail line : d.getDispatchDetails()) {
        MovementDetail md = new MovementDetail();
        md.setProductItem(line.getProductItem());
        md.setProductItemCode(line.getProductItem().getProductItemCode());
        md.setMeasureUnit(line.getMeasureUnit());
        md.setMeasureCode(line.getMeasureUnit().getCode());
        md.setQuantity(line.getQuantity());
        md.setUnitCost(line.getProductItem().getUnitCost());     // snapshot al aprobar
        md.setAmount(BigDecimalUtil.multiply(line.getQuantity(),
                                             line.getProductItem().getUnitCost(), 6));
        md.setMovementType(MovementDetailType.S);
        md.setWarehouse(d.getWarehouse());
        md.setCashAccount(line.getProductItem().getCashAccount());
        details.add(md);

        // snapshot de costo/monto en la línea del despacho
        line.setUnitCost(md.getUnitCost());
        line.setAmount(md.getAmount());
    }

    // 4. Guardar y aprobar el vale (descuento de stock + asiento)
    warehouseService.saveWarehouseVoucher(vale, inv, details,
        new HashMap<>(), new HashMap<>(), new ArrayList<>());
    approvalWarehouseVoucherService.approveWarehouseVoucher(vale.getId(),
        inv.getDescription(),
        new HashMap<>(), new HashMap<>(), new ArrayList<>());

    // 5. Enlazar vale al despacho y persistir
    d.setWarehouseVoucher(vale);
    d.setState(DispatchState.APROBADO);
    return em.merge(d);
}
```

> **Punto crítico**: `approveWarehouseVoucher(...)` ya hace el descuento de inventario y el asiento contable. No reimplementar nada.

---

## 8. Fase 6 — Anulación / Reversión

Delega en el servicio existente de reversión de vales documentado en [warehouse_reversal_implementation_plan.md](warehouse_reversal_implementation_plan.md).

```java
@TransactionAttribute(REQUIRES_NEW)
public WarehouseVoucherDispatch annul(WarehouseVoucherDispatch d, String reason) {
    if (d.getState() != DispatchState.APROBADO) {
        throw new IllegalStateException("Solo despachos APROBADOS pueden anularse");
    }
    if (d.getWarehouseVoucher() == null) {
        throw new IllegalStateException("Despacho sin vale asociado");
    }

    // Reusa el servicio de anulación existente sobre el WarehouseVoucher
    reverseInventoryService.annulVoucher(d.getWarehouseVoucher().getId(), reason);

    d.setState(DispatchState.ANULADO);
    return em.merge(d);
}
```

**UI:** botón "Anular" en `dispatchVoucherUpdate.xhtml`, modal con campo de motivo obligatorio, llamada a `confirmAnnul`. Mismo patrón que la anulación de vales existente.

---

## 9. Fase 7 — Reporte Jasper (Certificado de Recepción de Carguío)

### 9.1 Archivos

```
src/main/com/encens/khipus/reports/warehouse/
├── dispatchCertificate.jrxml
├── dispatchCertificateDetail.jrxml         (subreport para la tabla de líneas)
└── (compilados .jasper se generan en build)

src/main/com/encens/khipus/action/warehouse/
└── DispatchCertificateReportAction.java
```

### 9.2 Layout

Replicar el lado izquierdo del PDF de referencia con bloques:
1. Logo + datos de empresa (header con `imageExpression` + texto fijo).
2. Bloque "Datos de Vendedor de Entrega" (4 líneas).
3. Tabla resumen (Planta de Despacho / N° de Orden / Producto / Fecha / Destino / Hora entrega / Cliente / N° Lote / Conductor / Placa / CI / Color / Transporte / Marca).
4. Tabla principal de productos (subreport) con: CANTIDAD, PRODUCTO/SERVICIO, DESCRIPCIÓN DETALLADA, NUMERACIÓN DE BOLSAS, TOTAL ENTREGADO.
5. Bloque MEDIDOR DE PESO (Tara, Bruto, Neto, Peso Neto Carga = neto en toneladas, Pesado En, N° Boleta).
6. Bloque "Datos del Recepcionista de la Orden" (firma izquierda: Entregue Conforme - Responsable Almacén; firma derecha: Recibí Conforme - Conductor).
7. Disclaimer fijo del artículo 956.
8. Si estado = ANULADO, agregar marca de agua "ANULADO" diagonal.

### 9.3 Action

```java
@Name("dispatchCertificateReportAction")
public class DispatchCertificateReportAction extends ReportActionBase {

    public String generateCertificate(WarehouseVoucherDispatch dispatch) {
        Map<String, Object> params = new HashMap<>();
        params.put("DISPATCH_ID", dispatch.getId());
        params.put("DISPATCH_COMPANY", dispatch.getCompanyNumber());
        params.put("LOGO", contextPath + "/img/logo.png");
        params.put("REVERSED", dispatch.getState() == DispatchState.ANULADO);
        return generateReport("/reports/warehouse/dispatchCertificate.jasper", params);
    }
}
```

---

## 10. Fase 8 — Pruebas integradas y QA

Test plan manual sobre dev (terdemol):

| Escenario | Pasos | Resultado esperado |
|---|---|---|
| T01 — Crear borrador con todos los campos válidos | Llenar formulario, agregar 1 detalle, guardar | Registro en BORRADOR, sin N° de orden |
| T02 — Editar borrador | Cambiar fecha, cantidad, guardar | Cambios persistidos |
| T03 — Eliminar borrador | Botón eliminar | Registro y detalles eliminados |
| T04 — Validar peso neto en tiempo real | Cambiar bruto y tara | Neto se recalcula al `onblur` |
| T05 — Validar bolsas en tiempo real | Cambiar desde/hasta | `cantidad_bolsas` se recalcula |
| T06 — Aprobar con stock suficiente | Click Aprobar → confirmar paso 1 → confirmar paso 2 | Estado APROBADO, N° de orden asignado, vale generado, stock descontado, asiento contable creado |
| T07 — Aprobar con stock insuficiente | Mismo flujo con cantidad > saldo | Falla en paso 2 con mensaje detallado, despacho queda en BORRADOR |
| T08 — Aprobar con mes contable cerrado | Cerrar mes, intentar aprobar | Mensaje de error, despacho en BORRADOR |
| T09 — Anular despacho aprobado | Botón Anular con motivo | Estado ANULADO, stock revertido, contra-asiento generado |
| T10 — Anular despacho con mes cerrado | Cerrar mes, intentar anular | Mensaje de error, sin cambios |
| T11 — Imprimir certificado APROBADO | Botón Imprimir | PDF Jasper con datos correctos |
| T12 — Imprimir certificado ANULADO | Botón Imprimir | PDF con marca de agua "ANULADO" |
| T13 — Listado con filtros | Filtrar por cliente, estado, rango fecha | Resultados correctos |
| T14 — Usuario sin permiso | Login con rol limitado | No ve menú / botones según permisos |
| T15 — Multi-company | Crear despachos en dos empresas | Numeración independiente |
| T16 — CRUD DispatchPlace | Crear, editar, desactivar lugares | Filtro `kind` respetado en combos |
| T17 — Build limpio | `ant clean explode` con JDK 1.8 | Sin errores ni warnings nuevos |

---

## 11. Decisiones técnicas y open items

| # | Tema | Decisión | Justificación |
|---|---|---|---|
| 1 | Crear nuevo `WarehouseVoucherType` (p.ej. `DESP`) o reusar `S` (Salida)? | **Crear `DESP`** | Permite filtrar despachos del resto de salidas en listados/reportes |
| 2 | Crear nuevo `VoucherOperation` (`DESP`)? | **Sí** | Igual razón; útil para identificar vales generados por despacho |
| 3 | Crear `DocumentType` específico para despacho? | **Sí**, semilla en migración SQL | Mantiene el modelo polimórfico actual coherente |
| 4 | Permitir múltiples productos por despacho? | **Sí** (sec. 6.2 requerimientos) | Operador selecciona almacén y agrega productos terminados |
| 5 | Cómo identificar "productos terminados"? | **No filtrar por tipo de producto**; el almacén seleccionado define qué hay disponible | Confirmado: usuario selecciona almacén → se listan items con stock |
| 6 | `dispatchDate` editable por el usuario o = `new Date()`? | **Editable** dentro del mes contable abierto | Permite registrar despachos del día anterior si es necesario |
| 7 | Bloquear edición durante BORRADOR si otro usuario lo abrió? | **No (v1)** | Mismo comportamiento que vales existentes (último que guarda gana) |
| 8 | Permitir despachos en negativo (stock 0)? | **No** | Validación V10 obliga stock ≥ cantidad |
| 9 | Reusar `WAREHOUSEVOUCHERAPPROVAL` para aprobar despachos? | **No, permiso dedicado** | Granularidad: jefatura de logística puede aprobar despachos sin acceso al resto |
| 10 | Idioma del reporte | **Solo ES** en v1 | Confirmado con el cliente |

### Items pendientes de confirmar antes de codear

- **OP01.** Confirmar `idmodulo` del módulo Warehouse para el insert de permisos.
- **OP02.** Confirmar la convención de naming de tablas: ¿usar prefijo `inv_` o algo distinto para vale de despacho?
- **OP03.** Confirmar si `ProductionGroup` tiene scope por unidad ejecutora o es global; afecta el combo del turno.
- **OP04.** Verificar si `Provider` ya tiene un flag o categoría para "transportadora" o si debemos listar todos los proveedores.
- **OP05.** Confirmar si el documento `WarehouseVoucherType.DESP` debe afectar a algún lugar más (reportes existentes, filtros, etc.).
- **OP06.** Confirmar permisos exactos por rol (qué grupo tendrá cada uno de los 4 permisos nuevos).

---

## 12. Estimación de esfuerzo

| Fase | Estimación |
|---|---|
| Fase 0 — Schema/permisos/i18n | 0.5 día |
| Fase 1 — Modelo | 0.5 día |
| Fase 2 — Catálogo DispatchPlace | 1 día |
| Fase 3 — Servicio + Action | 1.5 días |
| Fase 4 — Vistas BORRADOR | 1.5 días |
| Fase 5 — Aprobación doble confirmación | 1 día |
| Fase 6 — Anulación | 0.5 día |
| Fase 7 — Reporte Jasper | 1.5 días |
| Fase 8 — Pruebas integradas y QA | 1.5 días |
| **Total** | **≈ 9.5 días** |

---

## 13. Mejores prácticas observadas

1. **Build con JDK 1.8** — verificar con `ant clean explode` después de cada fase. La terminal local puede tener Java 21 por default; usar JAVA_HOME apuntando a JDK 8 ([feedback_jdk8_build](../../../C%3A/Users/Precision%207710/.claude/projects/d--Intellij-sisk13/memory/feedback_jdk8_build.md)).
2. **Cambios mínimos al código existente** — solo se agregan archivos nuevos. Las modificaciones a `WarehouseVoucherType`, `VoucherOperation` y `DocumentType` son aditivas (nuevos valores).
3. **Transacciones REQUIRES_NEW** en los puntos de aprobación y anulación, para no contaminar transacciones del caller y permitir rollback aislado.
4. **Defensa en profundidad**: validaciones tanto en Action (V01-V08) como en Service (V09-V12).
5. **Snapshot de costo** en la línea del despacho al aprobar — el costo promedio puede cambiar después; el despacho debe reflejar el valor del momento.
6. **Sin secretos en repo**: no incluir credenciales en ningún script SQL.
7. **Idempotencia** del sequence: el `nextValue` está implementado con retry sobre `OptimisticLockException` (ver `SequenceGeneratorServiceBean`).
8. **i18n completa**: cualquier texto visible va por bundle, nunca hard-coded en xhtml ni Java.
9. **Commits pequeños y revisables** — un commit por fase como mínimo, ideal por sub-fase.
10. **Documentación viva**: actualizar este plan a medida que avance la implementación; marcar fases completadas con el hash del commit (mismo patrón que [warehouse_reversal_implementation_plan.md](warehouse_reversal_implementation_plan.md)).

---

## 14. Referencias

- Requerimientos detallados: [dispatch_voucher_requirements.md](dispatch_voucher_requirements.md).
- Flujo actual de vales: [warehouse_order_voucher_flow.md](warehouse_order_voucher_flow.md).
- Reversión de vales (base reutilizable): [warehouse_reversal_implementation_plan.md](warehouse_reversal_implementation_plan.md).
- PDF de especificación: `REQ DESPACHO.pdf`.
- Patrón de generación de secuencia: [Sequence.java](../src/main/com/encens/khipus/model/common/Sequence.java), [SequenceGeneratorServiceBean.java](../src/main/com/encens/khipus/service/common/SequenceGeneratorServiceBean.java).
- Patrón de vale con post-aprobación automática: [WarehouseVoucherCreateAction.java#createTransfer](../src/main/com/encens/khipus/action/warehouse/WarehouseVoucherCreateAction.java#L134) — usar como referencia el orden `save → approve`.
