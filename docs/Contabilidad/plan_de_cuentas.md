# Plan de Cuentas (`arcgms`) — estado, estructura y validaciones

Documento de contexto del plan de cuentas: cómo está modelado, qué reglas lo
gobiernan, qué validaciones existen hoy y qué quedó pendiente. Sirve como punto
de partida para mejoras o implementaciones nuevas sobre cuentas contables.

Relacionado: [`accounting_balance_rules.md`](../accounting_balance_rules.md)
(naturaleza deudora/acreedora y cuentas regularizadoras).

---

## 1. Modelo de datos

| Elemento | Valor |
|---|---|
| Tabla | `arcgms` (esquema `Constants.FINANCES_SCHEMA`) |
| Entidad | [`CashAccount.java`](../../src/main/com/encens/khipus/model/finances/CashAccount.java) |
| PK **en la entidad** | `@EmbeddedId CashAccountPk {no_cia, cuenta}` |
| PK **real en la base** | **solo `cuenta`** — `varchar(20)`, `utf8mb3_bin`, NOT NULL |

> La entidad **sobre-modela la clave**: la BD tiene PK simple sobre `cuenta`.
> Consecuencia práctica: un mismo código **no puede repetirse entre compañías**.
> Por eso las validaciones de duplicado y de uso se hacen **por código, sin
> compañía** (`accountCodeExists`, `findAccountReferences`).

Puntos del mapeo que hay que tener presentes:

- `accountCode` (`cuenta`) y `companyNumber` (`no_cia`) son campos **espejo**
  con `insertable=false, updatable=false`. Lo que se escribe es el `@EmbeddedId`.
  Si un alta "no guarda el código", casi siempre es porque no se copió el valor
  del espejo al `id`.
- `CompanyNumberListener` completa `id.companyNumber` en `@PrePersist` si viene
  null.
- Las asociaciones `rootCashAccount` (`cta_raiz`), `cashAccountLeve3`
  (`cta_niv3`) y `financesCurrency` (`moneda`) son de **solo lectura**
  (`insertable/updatable=false`). Las columnas se escriben por los campos
  escalares `rootAccountCode`, `accountLevel3Code` y `currency`.
- Salvo `cuenta`, la **única columna NOT NULL** es `ind_regulariz`
  (default `'N'`). Todo lo demás es nullable.

---

## 2. Estructura del código de cuenta

Código de **10 dígitos**, partido en 6 niveles de longitud fija:

| Nivel | N1 | N2 | N3 | N4 | N5 | N6 |
|---|---|---|---|---|---|---|
| Longitud | 1 | 1 | 1 | 3 | 2 | 2 |
| Dígitos acumulados | 1 | 2 | 3 | 6 | 8 | 10 |

Ejemplo `1110040103` → `1 │ 1 │ 1 │ 004 │ 01 │ 03` = nivel 6.

**El nivel es el último segmento no-cero.** Enmascarando desde la derecha:

| Código | Nivel |
|---|---|
| `1000000000` | 1 |
| `1100000000` | 2 |
| `1110000000` | 3 |
| `1110040000` | 4 |
| `1110040100` | 5 |
| `1110040103` | 6 |

**Máscara rota:** un segmento con valor después de otro en cero (hueco en el
árbol). Ejemplo `1010000000` (N2=`0` pero N3=`1`). Se considera inválido para
altas nuevas, aunque en los datos históricos existen (ver §8).

La segmentación está **fija en código**: `LEVEL_LENGTHS = {1,1,1,3,2,2}` en
[`CashAccountAction`](../../src/main/com/encens/khipus/action/finances/CashAccountAction.java).
Referencia funcional: `scripts/Configuracion Niveles.xlsx`.

### Cuenta padre por nivel

| Nivel | Padre = prefijo + ceros |
|---|---|
| 1 | (no tiene) |
| 2 | 1 dígito |
| 3 | 2 dígitos |
| 4 | 3 dígitos |
| 5 | 6 dígitos |
| 6 | 8 dígitos |

---

## 3. Columnas de jerarquía

| Columna | Campo | Regla |
|---|---|---|
| `cta_raiz` | `rootAccountCode` | 2 primeros dígitos + `00000000` (cuenta nivel 2) |
| `cta_niv3` | `accountLevel3Code` | Vacío en niveles 1-2; 3 primeros dígitos + `0000000` en niveles 3+ |

Alimentan el Dashboard de Finanzas y los reportes jerárquicos/consolidados.
En el análisis, una `cta_raiz` **vacía se omite** (no se marca como error), por
compatibilidad con los scripts originales `scripts/analyze_arcgms*.py`.

---

## 4. Qué se puede derivar del código (medido sobre 912 cuentas de `terdemol`)

| Dato | ¿Derivable? | Correlación real |
|---|---|---|
| **Tipo** desde el 1er dígito | Sí | **100%** — `1`=A, `2`=P, `3`=C, `4`=I, `5`=E |
| **Tipo** desde la cuenta padre | Sí | **100%** (901/901) |
| **Moneda** desde la cuenta padre | Sí | **99.8%** (899/901) |
| **Nivel** desde la máscara | Sí | **98.1%** (895/912) |
| `cta_raiz` / `cta_niv3` | Sí | 100% (fórmula) |
| **Cta. Movimiento** | **No** | 95% de correlación con "es hoja" |

**Cta. Movimiento** no tiene regla dura. Distribución real:

| Nivel | No movimiento | Sí movimiento |
|---|---|---|
| 1-3 | 76 | 0 |
| 4 | 273 | 1 |
| 5 | 11 | **478** |
| 6 | 0 | **72** |

Decisión vigente: **se marca automáticamente solo en nivel 6**. El nivel 5
también puede serlo, pero queda a criterio del usuario (el campo es editable).

---

## 5. Funcionalidades implementadas

### 5.1 Análisis y corrección de niveles

Pantalla: [`cashAccountList.xhtml`](../../view/finances/cashAccountList.xhtml) →
botón **"Analizar niveles"**.
Lógica: [`CashAccountLevelAnalysisAction`](../../src/main/com/encens/khipus/action/finances/CashAccountLevelAnalysisAction.java).

Revisa `arcgms` en vivo (**todas las compañías**) y muestra en un modal:

1. Cuentas con **longitud ≠ 10** → se informan, **no se procesan**.
2. Errores de `cta_raiz`.
3. Errores de `cta_niv3`.

Botón **"Corregir todo"** (con confirmación). El `UPDATE` usa **self-JOIN**
contra `arcgms`, de modo que es imposible escribir hacia una cuenta destino
inexistente:

```sql
UPDATE arcgms a
  JOIN arcgms p ON p.no_cia = a.no_cia
               AND p.cuenta = CONCAT(SUBSTRING(a.cuenta,1,2),'00000000')
   SET a.cta_raiz = p.cuenta
 WHERE <criterios de error>
```

Las filas cuyo destino no existe se pintan **en rojo**, no se corrigen y quedan
pendientes hasta que se cree la cuenta padre. El contador *"Requiere revisión"*
las separa de las corregibles.

### 5.2 Renumerar una cuenta

El código es la PK, así que **no se puede cambiar con el update normal**
(`merge` lo ignora en silencio). Por eso:

- En **edición** el campo Cuenta es `readonly`.
- Botón **"Renumerar"** → valida formato, duplicado y **que la cuenta no esté
  referenciada**; solo entonces hace un `UPDATE` nativo de la PK.

El chequeo de uso vive en `CashAccountServiceBean.findAccountReferences()` y
recorre el inventario `ACCOUNT_REFERENCES`: **35 tablas / 122 columnas**,
derivadas del mapeo JPA (`@JoinColumn(referencedColumnName="cuenta")`) más
`configuracion` y la jerarquía del propio `arcgms` (excluyendo la cuenta misma).
Las columnas se filtran contra `information_schema`, así que una columna ausente
no rompe la consulta.

> **Limitación conocida:** el inventario cubre lo mapeado como entidad. Una
> tabla legacy sin entidad JPA quedaría fuera. Por eso el criterio es
> conservador: ante la duda, bloquea. Si se agrega una entidad nueva que
> referencie `arcgms.cuenta`, **hay que sumarla a `ACCOUNT_REFERENCES`**.

Renumerar **no recalcula** `cn_nivel` ni la jerarquía: si cambia el prefijo, hay
que correr después "Analizar niveles".

### 5.3 Validación y autocompletado en el alta

`CashAccountAction.analyzeAccountCode()`, disparado al salir del campo Cuenta
(`onblur`, ajax). Valida en orden:

1. Formato: exactamente 10 dígitos.
2. Máscara de niveles (sin huecos).
3. Que el código no exista ya.
4. Que exista la **cuenta padre inmediata** (nombra cuál falta).

Si es válido, completa —**todo editable**—: Nivel, Cuenta activa,
Cta. Movimiento (solo nivel 6), Cuenta Raíz, Cuenta Nivel 3, y **Tipo + Moneda
heredados del padre**. Si no hay padre (nivel 1), Tipo y Moneda quedan vacíos y
**no se bloquea**.

El código se muestra segmentado por niveles (N1…N6) tanto en alta como en
edición.

### 5.4 Validación de cuentas en Preferencias de compañía

`CompanySettingAction.validateAccountCodes()` verifica, antes de guardar, que
los 51 códigos configurados existan en `arcgms`. Los campos se descubren **por
reflexión** sobre las asociaciones `CashAccount`, así que una cuenta nueva queda
cubierta sin tocar el método.

Motivo: con las FK activas, un código inexistente hace fallar el `UPDATE`, y
`GenericServiceBean` traduce **cualquier** `PersistenceException` a
`EntryDuplicatedException` → la pantalla diría *"registro duplicado"*, que no
orienta a nada.

---

## 6. Integridad referencial (claves foráneas)

El sistema **no tenía FKs** hacia `arcgms`: las cuentas se referencian por valor
desde ~122 columnas. Se empezó a construir la integridad **sin cascada**
(`ON DELETE RESTRICT` / `ON UPDATE NO ACTION`).

### Requisito: collation

MySQL exige **mismo charset y collation** en ambos lados de una FK, o falla con
**error 3780**. `arcgms.cuenta` es `utf8mb3_bin`, así que las columnas que la
referencian deben alinearse a `utf8mb3_bin`. Como los códigos son solo dígitos,
el cambio no altera ninguna comparación.

### Estado

| Script | Alcance | Estado |
|---|---|---|
| `query_v6.0.112_terdemol.sql` | `arcgms.cta_raiz`, `arcgms.cta_niv3` | Aplicado |
| `query_v6.0.113_terdemol.sql` | 51 columnas de `configuracion` | Aplicado |

### Pendiente (y por qué)

**a) Huérfanos que abortarían el `ALTER`** (verificado sobre `terdemol`):

| Tabla.columna | Huérfanos |
|---|---|
| `inv_movdet.cuenta_art` | 4025 |
| `pagoordencompra.cuentacaja` | 38 |
| `costosindirectosconf.cuenta` | 23 |
| `af_subgrupos.cta_vo` | 10 |
| `sf_tmpdet.cuenta` | 7 |
| `tipocredito` (6 columnas) | 4 c/u |

**b) Charset mixto.** De las 122 columnas: 52 `utf8mb3_general_ci`, 37
`utf8mb3_bin`, **27 `latin1_bin`**, **4 `latin1_swedish_ci`**, 2
`utf8mb4_0900_ai_ci`. En las de `latin1` no basta cambiar collation: hay
**conversión de charset**.

> Nota: `cg_movdet` (mayor contable) estaba **vacía** en la base de desarrollo.
> Cualquier validación de huérfanos para esa tabla debe hacerse **en producción**.

### Efecto de las FKs sobre la app

- "Corregir todo" no puede escribir un padre inexistente ni por error.
- Renumerar o borrar una cuenta referenciada es rechazado por el motor
  (error 1451), no solo por la validación de la aplicación.

---

## 7. Alta y edición

| Operación | Implementación |
|---|---|
| **Alta** | `em.persist` (JPA). Copia el código del espejo al `@EmbeddedId`, valida duplicado por código y da default a los flags para no dejar `NULL` en `ind_regulariz` |
| **Edición** | `em.merge` (JPA) |
| **Renumerar** | `UPDATE` nativo de la PK — **obligatorio**: JPA no cambia claves primarias |

Antes el alta usaba un `INSERT` nativo que hardcodeaba `activa='S'`,
`exije_cc='N'` y `permite_iva='N'`, e ignoraba varios campos del formulario.

---

## 8. Anomalías conocidas en los datos

- **17 cuentas** con `cn_nivel` que no coincide con la estructura del código.
  De ellas, **5 tienen la máscara realmente rota** (hueco), p. ej. `1260000100`
  (N4=`000` pero N5=`01`), `4120000100`, `4120000200`. Históricamente se
  permitieron; las altas nuevas ya no lo permiten.
- `9999999999` — "UTILIDAD / (PERDIDA) OPERATIVA", tipo `Z`, `cn_nivel` NULL.
  Cuenta especial fuera de la estructura.
- `5340506000` — renumerada desde un código de 9 dígitos; quedó con `cn_nivel=6`
  cuando su código implica **nivel 5**. Corregir el nivel a mano.
- La validación de nivel/máscara aplica **solo a altas nuevas**: no toca las
  cuentas existentes.

---

## 9. Permisos

| Código | Bits | Cubre |
|---|---|---|
| `ACCOUNTINGPLAN` | CREATE / UPDATE | Alta y edición de cuentas |
| `CASHACCOUNTLEVEL` | VIEW / UPDATE | VIEW: "Analizar niveles". UPDATE: "Corregir todo" |
| `CASHACCOUNTRENAME` | UPDATE | Botón "Renumerar" |

`CASHACCOUNTLEVEL` se siembra en `query_v6.0.111_terdemol.sql`;
`CASHACCOUNTRENAME` al final de `query_v6.0.113_terdemol.sql`. Ambos con
`idmodulo = 5` (finances) y **sin** insert a `derechoacceso`: el grant se hace
desde Administración > Roles.

> Recordatorio: si la funcionalidad no está sembrada o no se otorgó el permiso,
> `s:hasPermission` devuelve `false` **en silencio** y el botón simplemente no
> aparece. Tras otorgarlo hay que **cerrar sesión** (el mapa de permisos se arma
> en el login).

---

## 10. Trampas técnicas encontradas (JSF 1.2 / RichFaces 3 / Seam 2)

Valen para cualquier pantalla nueva de este módulo:

- **`a4j:support` necesita `ajaxSingle="true"`.** Sin eso valida todo el
  formulario y los campos `required` aún vacíos impiden ejecutar la acción.
- **`ui:repeat` no soporta `varStatus`** en Facelets 1.x: `st.index` queda nulo.
  Para índices, usar listas paralelas.
- **`.entry .data` es `table-cell` con `float:left`** ([theme.css](../../view/stylesheet/theme.css)):
  su ancho lo dicta el contenido, así que un texto largo dentro de un campo
  desarma la fila del formulario. Acotar con `max-width`.
- **`FacesMessages` se duplica dentro de modales**: lo pinta el panel global de
  la plantilla *y* el `h:messages` del modal. Para mensajes de modal, usar
  estado propio de la acción.
- **`h:messages` con `showSummary` y `showDetail`** repite el texto (summary y
  detail traen el mismo valor).
- **`GenericServiceBean.update()` traduce cualquier `PersistenceException` a
  `EntryDuplicatedException`**: una violación de FK termina mostrando
  "registro duplicado". Validar antes en la acción si se quiere un mensaje útil.
- **`getSingleResult()` lanza `NoResultException`**, no `EntityNotFoundException`
  (era un bug en `findByAccountCode`, corregido).

---

## 11. Próximos pasos sugeridos

1. Limpiar los huérfanos de §6 y extender las FKs a las tablas transaccionales
   (`cg_movdet`, `sf_tmpdet`, `sf_tmpenc`, `registrocontable`), verificando
   **en producción**.
2. Resolver las columnas en `latin1` (conversión de charset) para completar la
   integridad.
3. Corregir `cn_nivel` de `5340506000` y revisar las 5 cuentas con máscara rota.
4. Evaluar recalcular `cn_nivel`, `cta_raiz` y `cta_niv3` automáticamente al
   renumerar, hoy es manual.
5. Si alguna vez se necesita renumerar cuentas **en uso**, la vía es FK con
   `ON UPDATE CASCADE` (hoy descartada a propósito), no un update manual en
   cascada.
