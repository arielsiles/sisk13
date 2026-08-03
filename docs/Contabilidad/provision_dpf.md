# Provisión mensual de intereses por pagar sobre DPF

Automatiza el asiento que hasta la versión 6.0.120 se armaba a mano copiando la planilla
`ESTADO DE DPF AL 31-12-2025.xlsx` (una pestaña por mes) al comprobante contable.

- **Pantalla:** Atención al cliente → Procesos → *Provisión Intereses x Pagar DPFs*
  ([provisionInterestPayable.xhtml](../../view/customers/provisionInterestPayable.xhtml))
- **Lógica:** [ProvisionInterestPayableAction](../../src/main/com/encens/khipus/action/customers/ProvisionInterestPayableAction.java)
- **Permiso:** `PROVISIONDPF` (VIEW = calcular/verificar, CREATE = generar el asiento)

## Cálculo

Para cada DPF con días devengados dentro del mes `[inicioMes, finMes]`:

```
desde     = max(inicioMes, fechaApertura)
hasta     = min(finMes,    fechaVencimiento)
días      = hasta - desde + 1            ← inclusivo en AMBOS extremos
provisión = capital * tasa% * días / 36000
```

- **Tasa:** `tipocuenta.inta`. `intb` (tasa "con crédito") no se usa nunca en DPF.
- **Base:** 360 días.
- **Capital:** `cuenta.capital`, en la moneda de la cuenta.
- Los días se cuentan inclusive en los dos extremos. Cuando un DPF se renueva, el día del
  vencimiento queda contado tanto en el certificado viejo como en el nuevo. **Es
  intencional**: así lo hacía la planilla y así están los asientos históricos.

### Selección de cuentas: manda `fechavence`, no el estado

```sql
tipocuenta.tipo = 'DPF'
AND cuenta.fechaapertura <= :finMes
AND cuenta.fechavence    >= :inicioMes
AND cuenta.capital > 0
```

Deliberadamente **no** se filtra por `cuenta.estado`. Al renovar un DPF, `createDpfRenewal`
deja la cuenta anterior en `INACTIVE`; esa cuenta igual devengó intereses hasta su
vencimiento dentro del mes. Filtrar por estado activo perdería esos días, y al regenerar
meses viejos los perdería en casi todas las cuentas.

### Redondeo: el orden importa

```
totalME_$us = round( Σ provisiones_ME_sin_redondear , 2 )
totalME_Bs  = round( totalME_$us * TC , 2 )      ← se redondea el $us PRIMERO
totalMN_Bs  = round( Σ provisiones_MN , 2 )
```

Redondear recién al final da diferencias de centavos: diciembre 2025 daría 5.464,76 en vez
de 5.464,78, que es lo que está contabilizado.

### Tipo de cambio

Se toma de `arcgtc` para la clase de cambio del dólar (resuelta vía `cg_moneda`), en la
**fecha exacta del último día del mes** que se está generando. Si no existe esa fila, el
proceso corta con mensaje y no deja ni verificar ni generar — no hay fallback al último
tipo de cambio cargado. Se cargan desde Finanzas → Configuración → *Tipos de cambio
(contabilidad)*.

UFV no se procesa. Un DPF en moneda distinta de P o D corta el cálculo con error.

## Asiento generado

Tipo **CB**, fecha = último día del mes, glosa
`PROVISION DE INTERESES POR PAGAR SOBRE AHORROS DPF AL dd/MM/yyyy`.

| Cuenta | Debe | Haber | Debe $us | Haber $us |
|---|---|---|---|---|
| Gasto intereses DPF **ME** | totalME_Bs | | totalME_$us | |
| Cargos financieros x pagar DPF **ME** | | totalME_Bs | | totalME_$us |
| Gasto intereses DPF **MN** | totalMN_Bs | | 0 | |
| Cargos financieros x pagar DPF **MN** | | totalMN_Bs | | 0 |

**Sólo se emite el bloque de la moneda cuyo total sea mayor a cero.** Septiembre y octubre
de 2025 no tienen DPF en bolivianos y su asiento lleva únicamente las dos líneas de ME.

### De dónde sale cada cuenta

- **Gasto (debe):** Preferencias de compañía, apartado DPF.
  Columnas `i_ppag_dpf_mn` / `i_ppag_dpf_me`.
- **Pasivo (haber):** `tipocuenta.CTACF_MN` / `CTACF_ME` del tipo de cuenta de cada DPF.
  Son las mismas que ya usa la renovación de DPF, por eso no se duplican en configuración.

El pasivo se agrupa **por código de cuenta**, no por moneda: si dos tipos de DPF apuntaran
a cuentas de cargos financieros distintas, salen líneas separadas en vez de mezclarse. Con
la configuración actual los tres tipos apuntan a la misma cuenta y sale una sola línea.

Las líneas en ME llevan `exchangeAmount` = tipo de cambio; las de MN llevan 1 y los importes
en $us en cero, igual que el resto del sistema.

## Flujo de uso

1. Elegir mes y gestión (por defecto, el último mes cerrado).
2. **Calcular** → grilla con el detalle por certificado (equivalente a una pestaña del
   Excel) más los totales ME/MN y el tipo de cambio aplicado.
3. **Generar asiento** → modal con las líneas exactas del comprobante y la advertencia.
   **Aceptar** registra; **Cancelar** no graba nada.

**No hay control de duplicados.** Generar dos veces el mismo mes crea dos asientos. La
advertencia del modal lo dice explícitamente. Para *verificar* un mes ya contabilizado hay
que quedarse en el paso 2, o abrir el modal y cancelar.

## Verificación contra el histórico 2025

El algoritmo reproduce al centavo **10 de los 12 meses** de la planilla:

| Mes | Sistema $us | Excel $us | | Mes | Sistema $us | Excel $us | |
|---|---|---|---|---|---|---|---|
| Ene | 360,37 | 360,37 | ✔ | Jul | 436,62 | 436,62 | ✔ |
| Feb | 379,54 | 379,54 | ✔ | Ago | 421,78 | 421,78 | ✔ |
| Mar | 448,00 | 448,00 | ✔ | Sep | 406,59 | 406,59 | ✔ |
| Abr | 406,15 | 406,15 | ✔ | Oct | 461,16 | 461,16 | ✔ |
| **May** | **436,62** | **453,01** | ✘ | Nov | 741,93 | 741,93 | ✔ |
| **Jun** | **422,54** | **472,80** | ✘ | Dic | 785,17 | 785,17 | ✔ |

Los totales en bolivianos coinciden en los 12 meses.

### Por qué difieren mayo y junio: error de fórmula en la planilla

El certificado **ME00155** (apertura 16/04/2025, capital 9.834 $us, 4%) tiene mal la
columna *Días* en esas dos pestañas: la fórmula quedó anclada a la **fecha de apertura**
(`D`) en vez de al **inicio del mes** (`$K$3`), así que acumula días ya provisionados.

| Pestaña | Fórmula de días | Días | Correcto | Provisión Excel | Correcta |
|---|---|---|---|---|---|
| Prov.Abr | `DAYS(K15,D15)+1` | 15 | 15 ✔ | 16,39 | 16,39 |
| Prov.May | `DAYS(K14,D14)+1` | 46 | 31 ✘ | 50,26 | 33,87 |
| Prov.Jun | `DAYS(K14,D14)+1` | 76 | 30 ✘ | 83,04 | 32,78 |
| Prov.Jul | `DAYS(K14,$K$3)+1` | 31 | 31 ✔ | 33,87 | 33,87 |

En abril la fórmula da lo correcto por casualidad, porque la apertura cae dentro del mes.
En julio ya está corregida a `$K$3`.

Resultado: entre mayo y junio se provisionaron **61 días de más** sobre ese certificado,
unos **66,65 $us** (≈ Bs 463,90 al 6,96) de sobre-provisión que quedó en la cuenta de
cargos financieros por pagar.

El sistema **no replica este error**: calcula el devengue del mes. Al regenerar mayo y
junio de 2025 los importes van a dar menores que los asientos existentes, y esa diferencia
es la correcta. Es distinto del doble conteo del día de renovación, que sí se replica a
propósito porque es una regla del cálculo y no un error de celda.

## Trampas

- **La pantalla necesita conversación de larga duración.** El resultado de *Calcular* tiene
  que sobrevivir hasta confirmar el modal, por eso
  [customers/pages.xml](../../resources/WEB-INF/customers/pages.xml) declara
  `<begin-conversation join="true" flush-mode="MANUAL"/>` para esta vista. Con
  `propagation="none"` desde el menú y sin esa declaración, el detalle se perdía entre un
  request y el otro.
- **`flush-mode="MANUAL"` no es decorativo:** el cálculo carga entidades `Account` en el
  contexto de persistencia de la conversación. Con flush automático, cualquier cambio
  incidental se escribiría. `saveVoucher` hace su `flush()` explícito.
- **El modal vive en su propio `h:form`** porque RichFaces reubica el `modalPanel` en el
  body. Los `reRender` que cruzan de un formulario al otro usan id absoluto
  (`:provisionConfirmationForm:provisionVoucherPreviewPanel`).
- **`getFullCashAccount()` agrega solo el sufijo `(785.17 $us)`** cuando la cuenta es moneda
  D. No hay que armarlo a mano: por eso el asiento se ve igual que los que se cargaban
  manualmente.

## Tipos de cambio (`arcgtc`)

Hay **dos** tablas de tipo de cambio en el sistema y no son la misma:

| | `tipocambio` | `arcgtc` |
|---|---|---|
| Entidad | `ExchangeRate` | `FinancesExchangeRate` |
| PK | `idtipocambio` autoincremental | compuesta (`clase_cambio`, `fecha`) |
| Campos | compra / venta / tasa | `tipo_cambio` decimal(10,6) |
| Pantalla | RRHH (existía) | Finanzas → Configuración (nueva) |
| La usa | planillas | **contabilidad: provisión DPF, comprobantes en ME** |

El ABM nuevo es sobre `arcgtc`. Su catálogo de clases es `arcgcc` (**D** = dólar,
**U** = UFV) y `cg_moneda` mapea moneda → clase.

Como `clase_cambio` y `fecha` son la clave primaria y son inmutables, la edición sólo
permite corregir el valor: para cambiar clase o fecha hay que eliminar y volver a crear.
