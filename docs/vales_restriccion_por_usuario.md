# Restricción de Vales de Almacén por Usuario

Funcionalidad que permite limitar, **por usuario**, qué **Tipos de Documento**,
**Almacenes** y **Artículos** están disponibles al **crear o editar un Vale de
Almacén** (`view/warehouse/warehouseVoucherCreate.xhtml` y la edición vía
`warehouseVoucherUpdate.xhtml` → `movementDetail.xhtml`).

Pensada para habilitar a ciertos operadores el módulo de Vales de forma acotada
(p. ej. solo EGRESO, 1 almacén, un conjunto fijo de artículos) **sin afectar** a
los demás usuarios ni a otras pantallas.

---

## 1. Principios de diseño

- **Opt-in:** un usuario queda restringido **solo si** tiene una fila *activa*
  en `inv_vale_restriccion`. Sin fila (o `activo = 0`) el comportamiento es
  idéntico al actual → **cero impacto** para los usuarios en producción.
- **Null-skip:** el resolver devuelve `null` cuando no hay restricción o la lista
  de esa dimensión está vacía. Una restricción EL de Seam con parámetro `null`
  **se omite**, por lo que el usuario ve "todo" en esa dimensión
  (*Interpretación A*: lista vacía = sin restricción en esa dimensión).
- **Fail-open:** ante cualquier error (config no cargable, etc.) el usuario queda
  **sin** restricción, para no romper la operativa.
- **Aislamiento por pantalla (viewId):** la restricción solo filtra en las
  pantallas de **vale**; despacho, producción y reportes (que comparten los
  mismos DataModel) quedan intactos. Ver §5.
- **Refuerzo de servidor:** el guardado del vale valida en el servidor, de modo
  que el filtro de UI no se puede saltar (typeahead o petición manipulada).

---

## 2. Modelo de datos

SQL: `query/query_v6.0.89_terdemol.sql` (crea las 4 tablas + la funcionalidad de
permiso). Entidades JPA en `src/main/com/encens/khipus/model/warehouse/`.

| Tabla | Entidad | Contenido |
|---|---|---|
| `inv_vale_restriccion` | `RestrictedWarehouseVoucherConfig` | Cabecera: 1 por usuario (`idusuario`, `activo`, `idcompania`) |
| `inv_vale_restriccion_tipodoc` | `RestrictedConfigDocumentType` | Tipos de documento permitidos (FK a `WarehouseDocumentType`) |
| `inv_vale_restriccion_almacen` | `RestrictedConfigWarehouse` | Almacenes permitidos (FK a `Warehouse`) |
| `inv_vale_restriccion_articulo` | `RestrictedConfigProductItem` | Artículos permitidos (FK a `ProductItem`) |

- Cabecera con `UNIQUE (idcompania, idusuario)` → una configuración por usuario.
- Detalles con `ON DELETE CASCADE` y orphan-removal (`@Cascade(DELETE_ORPHAN)`).
- Las FK hacia los catálogos legacy (`inv_almacenes`, `inv_articulos`,
  `inv_tipodocs`) **no** se declaran a nivel BD (mismo criterio que
  `inv_valedespacho_det`); la integridad se valida vía JPA. Solo se indexan.
- Compañía autocompletada por `CompanyListener` (`@PrePersist`).

---

## 3. Componentes

### Resolver (corazón de la lógica)
`action/warehouse/WarehouseVoucherRestrictionResolver.java`
- `@Name("warehouseVoucherRestrictionResolver")`, `@Scope(EVENT)`, `@AutoCreate`.
- Carga una vez por request la config activa del usuario logueado
  (`sessionUser.userId`) usando el `listEntityManager` (EVENT, con filtro de
  compañía).
- Expone:
  - Getters EL **gated por pantalla**: `getAllowedDocumentCodes()`,
    `getAllowedWarehouses()`, `getAllowedProductItems()`.
  - Listas privadas **raw** (sin gate): `rawAllowed*()`.
  - Validaciones de servidor: `isDocumentTypeAllowed`, `isWarehouseAllowed`,
    `isProductItemAllowed` (usan las raw).
  - `isRestricted()`.

### Pantalla de administración (CRUD)
- Action: `action/warehouse/RestrictedVoucherConfigAction.java`
  (`GenericAction`) + `RestrictedVoucherConfigDataModel.java`.
- Vistas: `view/warehouse/restrictedVoucherConfigList.xhtml` y
  `restrictedVoucherConfig.xhtml`.
- Navegación: `resources/WEB-INF/warehouse/pages.xml`; menú en
  `view/layout/menu.xhtml`; textos en `resources/messages_app.properties`.
- Protegida por el permiso **`WAREHOUSEVOUCHERRESTRICTION`** (funcionalidad
  id 477, módulo 5).

---

## 4. Puntos de filtrado (dónde se aplica la restricción)

La restricción tiene **3 dimensiones**. Cada una consume un getter del resolver:

| Dimensión | Dónde se aplica | Getter consumido |
|---|---|---|
| **Tipo de documento** | Factory `warehouseRestrictedDocumentTypeList` en `action/warehouse/components.xml` + rama del `selectList` y `app:restriction` del quickSearch en `warehouseVoucherCreate.xhtml` | `allowedDocumentCodes` |
| **Almacén** | Restricción `warehouse in (#{...allowedWarehouses})` en `WarehouseSearchDataModel.java` (DataModel COMPARTIDO) + quickSearch inline en `warehouseVoucherCreate.xhtml` | `allowedWarehouses` |
| **Artículo** | Restricción `inventory.productItem in (#{...allowedProductItems})` en `ProductItemByWarehouseDataModel.java` (DataModel COMPARTIDO) | `allowedProductItems` |

> Nota: los DataModel de almacén y artículo son **compartidos** con otras
> pantallas (despacho, producción, reportes, transferencias, OC...). Por eso el
> aislamiento por viewId (§5) es imprescindible.

---

## 5. Aislamiento por pantalla (viewId) — clave de la solución

Como `WarehouseSearchDataModel` y `ProductItemByWarehouseDataModel` son
compartidos, sin aislamiento un usuario restringido que **también** tenga acceso
a despacho/producción vería esas pantallas recortadas (caso real detectado con
el usuario `aflores`, que hace vales restringidos **y** despachos).

La solución centraliza la decisión en **un único punto: el resolver**. Los
getters EL devuelven la lista **solo si la pantalla activa es de vale**:

```java
// WarehouseVoucherRestrictionResolver.java
private boolean isVoucherScreen() {
    String viewId = FacesContext.getCurrentInstance().getViewRoot().getViewId();
    return viewId.contains("warehouseVoucherCreate")   // crear vale
        || viewId.contains("warehouseVoucherUpdate")   // editar vale
        || viewId.contains("movementDetail");          // añadir detalle (edición)
}

public List<Warehouse>   getAllowedWarehouses()    { return isVoucherScreen() ? rawAllowedWarehouses()    : null; }
public List<ProductItem> getAllowedProductItems()  { return isVoucherScreen() ? rawAllowedProductItems()  : null; }
public List<String>      getAllowedDocumentCodes() { return isVoucherScreen() ? rawAllowedDocumentCodes() : null; }
```

**Efecto:**
- En pantallas de vale → el getter devuelve la lista → el modelo compartido
  filtra.
- En despacho / producción / reportes → `viewId` no es de vale → getter `null` →
  la restricción EL **se omite** → esas pantallas ven todo (intactas).

**Pantallas de vale incluidas en el whitelist** (las únicas donde se consulta el
modelo de artículos en contexto de vale):
- `warehouseVoucherCreate.xhtml` — crear vale (modal "Añadir artículos").
- `warehouseVoucherUpdate.xhtml` — editar vale (picker de almacén).
- `movementDetail.xhtml` — "Adicionar detalle" desde la edición del vale.
  *(Verificado: producción `rawMaterialPaymentRequestUpdate.xhtml` no usa esta
  página para elegir artículos — solo la nombra en un tab/línea comentada — por
  lo que el whitelist es seguro.)*

> Si en el futuro se agrega otra pantalla de vale con distinto nombre de archivo,
> hay que añadirla al whitelist de `isVoucherScreen()`.

### Por qué viewId y NO duplicar DataModel/modales
Se intentó aislar duplicando DataModel (subclases) + modales dedicados y **se
revirtió**: el picker de artículos de la edición vive en `movementDetail.xhtml`
y hay varios modales compartidos → se volvía *whack-a-mole* entre páginas. El
gate por viewId resuelve todos los flujos de vale en un solo lugar, sin duplicar
archivos.

---

## 6. Refuerzo de servidor (anti-bypass)

`WarehouseVoucherGeneralAction.validateUserRestriction(details)` valida que el
tipo de documento, el almacén y cada artículo del vale estén permitidos. Se
invoca desde:
- `WarehouseVoucherCreateAction.create()`
- `WarehouseVoucherUpdateAction.update()`

Usa las listas **raw** del resolver (sin gate de viewId): durante el guardado el
viewId siempre es la pantalla de vale, pero al desacoplarlo se garantiza el
enforce aunque cambiara el contexto. Si algo no está permitido, agrega un
`facesMessages` de error y aborta el guardado.

---

## 7. Permisos

### Para el administrador (configura las restricciones)
- **`WAREHOUSEVOUCHERRESTRICTION`** → Ver/Crear/Editar/Borrar (CRUD de la pantalla
  de administración). Asignar al rol correspondiente en el panel de permisos.

### Para el usuario restringido (p. ej. solo Vales EGRESO)
- **`WAREHOUSEVOUCHER`** → Ver + Crear (+ Editar si debe modificar vales).
- **Unidad(es) de Negocio** del almacén configurado, asignadas en
  *Administración → Usuarios → "Asignar Unidades de Negocio"* (sin esto, el combo
  "Unidad ejecutora" y el almacén salen vacíos por `businessUnitFilter`).
- **NO** necesita `WAREHOUSESPECIALOPERATIONS` (el factory restringido muestra el
  tipo configurado aunque sea E/S) ni `WAREHOUSEVOUCHERRESTRICTION`.

### Si el mismo usuario también hace Despachos
- Necesita **`WAREHOUSEPROVIDERMAN` → Ver** (la *Transportadora* del despacho es
  un proveedor; sin este permiso la pantalla de despacho lanza
  `AuthorizationException` al instanciar `ProviderDataModel`). Esto es un
  requisito propio del despacho, no de esta funcionalidad.

> Los permisos se cargan al **iniciar sesión**: tras cambiarlos, el usuario debe
> cerrar sesión y volver a entrar.

---

## 8. Despliegue

1. **Ejecutar el SQL primero:** `query/query_v6.0.89_terdemol.sql` en la BD.
   ⚠️ `hibernate.hbm2ddl.auto=validate` valida **todas** las entidades al
   arrancar; si despliegas sin crear las tablas `inv_vale_restriccion*`, **el
   sistema entero no arranca**.
2. **Compilar con JDK 1.8** y `ant explode` (o `ant clean explode`).
3. Asignar el permiso `WAREHOUSEVOUCHERRESTRICTION` al rol de administración.
4. Crear la configuración del usuario en la pantalla nueva.

---

## 9. Cómo configurar un usuario restringido

1. Menú *Gestión de almacenes → Restricción de Vales por Usuario → Nuevo*.
2. Seleccionar el **usuario**, marcar **Activo**.
3. Añadir los **Tipos de Documento** permitidos (combo + "Añadir").
4. Añadir los **Almacenes** permitidos (combo + "Añadir").
5. Añadir los **Artículos** permitidos (modal de búsqueda multi-selección).
6. Guardar.
7. Asignar al usuario `WAREHOUSEVOUCHER` (Ver+Crear) y su Unidad de Negocio.

Dejar una dimensión **vacía** = sin restricción en esa dimensión.

---

## 10. Checklist de pruebas

- Usuario **sin** config → vales y todo lo demás idéntico a hoy.
- Usuario **con** config → en *crear vale* y *editar vale → Adicionar detalle*
  solo aparecen su tipo doc / almacén / artículos.
- El typeahead tampoco permite elegir fuera de lo configurado.
- `create()` / `update()` rechazan un POST forzado fuera de lo permitido.
- Una **lista vacía** (p. ej. tipos doc) → esa dimensión sin restricción.
- Config **inactiva** → sin restricción.
- El mismo usuario en **Despacho** → ve **todos** los almacenes/artículos.
- Usuarios **normales** → sin cambios en ninguna pantalla.

---

## 11. Archivos principales

**Nuevos:** `RestrictedWarehouseVoucherConfig` + 3 detalles,
`WarehouseVoucherRestrictionResolver`, `RestrictedVoucherConfigAction` /
`...DataModel`, `restrictedVoucherConfig(List).xhtml`,
`query/query_v6.0.89_terdemol.sql`.

**Modificados:** `WarehouseSearchDataModel`, `ProductItemByWarehouseDataModel`
(+1 restricción c/u), `components.xml` (factory), `warehouseVoucherCreate.xhtml`
(combo + quickSearch), `WarehouseVoucherGeneralAction` (`validateUserRestriction`)
+ llamadas en create/update, `pages.xml`, `menu.xhtml`, `messages_app.properties`.
Fix colateral: guard `c:if` del filtro de proveedor en `warehouseVoucherList.xhtml`
(evita `AuthorizationException` para usuarios sin `WAREHOUSEPROVIDERMAN`).

**Commits:** `9fb59e1e` (funcionalidad), `e19c3da2` (aislamiento por viewId).
