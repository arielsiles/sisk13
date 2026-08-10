# Provisión mensual de intereses por pagar sobre DPF

Un **DPF** es un Depósito a Plazo Fijo: un producto de **ahorro**, no un préstamo. El socio
deposita un capital a un plazo y una tasa pactados, y la cooperativa le **debe** el interés
—de ahí que la provisión sea de intereses **por pagar** y la contrapartida un pasivo.

> No confundir con la *Provisión de intereses por cobrar sobre cartera vigente*
> ([ProvisionInterestReceivableAction](../../src/main/com/encens/khipus/action/customers/ProvisionInterestReceivableAction.java)),
> que es la de los préstamos y no tiene nada que ver con esto.
>
> Ojo también con la palabra **crédito** al leer este documento: cuando aparece referida a un
> movimiento contable significa el **haber** del asiento (el importe acreditado a la cuenta),
> no un préstamo. Por eso acá se usa "abono" o "importe acreditado".

Los DPF viven en `cuenta` junto con las cuentas de ahorro, distinguidos por
`tipocuenta.tipo = 'DPF'` (los otros valores son `CAJ` y `SOC`).

Esta pantalla automatiza el asiento que hasta la versión 6.0.120 se armaba a mano copiando la
planilla `ESTADO DE DPF AL 31-12-2025.xlsx` (una pestaña por mes) al comprobante contable.

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

- **Tasa:** `tipocuenta.inta`. `intb`, que es la tasa para socios con un préstamo vigente,
  no se usa nunca en DPF: aplica sólo a las cuentas de ahorro.
- **Base:** 360 días.
- Los días se cuentan inclusive en los dos extremos. Cuando un DPF se renueva, el día del
  vencimiento queda contado tanto en el certificado viejo como en el nuevo. **Es
  intencional**: así lo hacía la planilla y así están los asientos históricos.

### Capital por tramos

Un DPF no recibe dinero a mitad de plazo: para aumentar el capital se cierra el certificado
y se abre uno nuevo. Pero **excepcionalmente pasa**, y cuando pasa un solo importe para todo
el mes es incorrecto: el interés se devenga sobre el capital **vigente cada día**.

Por eso el capital del devengue sale de la **línea de tiempo del mayor**, no de
`cuenta.capital` — un campo no puede valer dos cosas. El mes se parte en tramos, uno por
cada aumento, y el día del aumento ya devenga con el capital nuevo:

```
ME00156, marzo 2026 (aumento de 8.000 el 26/03):
  01/03 a 25/03   25 días   27.141,49    75,39
  26/03 a 31/03    6 días   35.141,49    23,43
                                        ------
                                          98,82
```

`cuenta.capital` sigue siendo la referencia del contrato y la base del candado, pero no es
de donde se lee el importe día a día.

**Los tramos se cortan solo por CRÉDITOS, nunca por débitos.** El único movimiento deudor de
un certificado es su cancelación, y viene fechada el día del vencimiento: un DPF devenga
interés hasta su vencimiento **inclusive**, así que el asiento que lo liquida ese día no
reduce la base de ese día. Restarlo costaba un día de interés en el último mes de cada
certificado — se detectó porque diciembre 2025 daba 782,53 en vez de 785,17, exactamente un
día de ME00151 (2,64).

La grilla muestra **una fila por tramo**, con el período devengado y el capital de cada uno,
marcadas con `*` las que nacen de un aumento. Y arriba aparece un aviso nombrando el
certificado, la fecha y el capital nuevo, para que el contador lo vea en vez de descubrirlo
cuadrando el importe.

En los 12 meses de 2025 **ningún certificado tiene más de un tramo**: el mecanismo está
inerte en todo el histórico y sólo se activa en el caso excepcional.

### El retiro anticipado deja de devengar

Un DPF retirado **antes de su vencimiento** no devenga en ningún período cuya fecha de corte
sea igual o posterior a la del retiro, aunque su `fechavence` sea mucho más adelante: el
dinero ya se devolvió.

Tampoco devenga **nada en el mes del retiro**, porque el asiento de cierre liquida ese tramo.
Si igual se provisionara, quedaría un pasivo posterior a la liquidación que nadie reversa.

**Anticipado, no simplemente "cerrado".** Ésta es la distinción que importa y es fácil
equivocarla: una **renovación también deja el saldo en cero** —el capital pasa al
certificado nuevo—, pero ese certificado ganó el plazo completo y **tiene que devengar hasta
su `fechavence`**. Y hay una segunda razón, más grave: el asiento de renovación debita del
pasivo el interés de **todo el plazo**, así que saltear su último mes deja un faltante
permanente en `CTACF_*`.

El caso que lo destapó: **ME00148** vence el 21/08/2025 y se renovó el 26/08/2025 —cinco
días después—. Cortando por "saldo cero" quedaba fuera de la provisión de agosto y se
perdían sus 21 días devengados. **ME00149** es idéntico salvo que se renovó el 01/09, así
que se salvaba por casualidad de fechas.

Sobre los datos reales: de los 148 certificados que alguna vez quedaron en cero, **138 son
renovaciones o retiros al vencimiento** y sólo **10 son retiros anticipados**.

La fecha se saca del **mayor** —el saldo llegando a cero— y se compara contra `fechavence`.
Nunca del estado: un DPF renovado también queda `INACTIVE`, y un retiro total cargado por
comprobante no toca el estado y deja la cuenta en `ACTIVE` con saldo cero.

Y el corte es por **fecha**, nunca por "¿está cerrado hoy?": la provisión de agosto se
genera en septiembre con fecha 31/08, y un retiro del 2 de septiembre no debe tocar agosto.

**Esto no era así hasta la versión 6.0.122.** `capitalAt()` ignoraba los débitos partiendo
de que "el único movimiento deudor de un certificado es su cancelación, y viene fechada el
día del vencimiento". Esa premisa era falsa: hay cierres anticipados, y esos certificados
seguían provisionando todos los meses hasta su `fechavence` sobre plata ya devuelta.

Al detectarlo había 10 DPF cerrados antes de vencer, todos en dólares y todos con saldo
cero. Nueve vencieron entre 2019 y 2024, así que ninguno estaba vivo durante 2025 y la
planilla de ese año no quedó contaminada. El único que cruza períodos aún sin generar es
ME00165 (cerrado el 22/07/2026, vence el 28/01/2027).

### Selección de cuentas: manda `fechavence`, no el estado

```sql
tipocuenta.tipo = 'DPF'
AND cuenta.accountState NOT IN ('ANNULLED', 'PENDING')
AND cuenta.fechaapertura <= :finMes
AND cuenta.fechavence    >= :inicioMes
```

Deliberadamente **no** se filtra por activo/inactivo. Al renovar un DPF,
`createDpfRenewal` deja la cuenta anterior en `INACTIVE`; esa cuenta igual devengó
intereses hasta su vencimiento dentro del mes. Filtrar por estado activo perdería esos
días, y al regenerar meses viejos los perdería en casi todas las cuentas.

Tampoco se filtra por `capital > 0`: un capital en cero es un dato a corregir y tiene que
salir a la luz, no desaparecer de la grilla sin dejar rastro. Lo atrapa el candado.

Sí quedan fuera las cuentas **anuladas**, que nunca fueron una operación real, y las
**pendientes de aprobación**, cuya apertura todavía no se contabilizó.

### El candado: `capital` contra el mayor

`cuenta.capital` es un dato de captura. Antes de calcular nada, la pantalla contrasta cada
certificado del período contra el capital que dice su contabilidad —la **suma de los
importes acreditados** al certificado—. **Si alguno diverge, no se calcula ni se genera
nada** y se listan todos los certificados con problema, no solo el primero.

Sin este control la provisión salía corta o larga en silencio: el campo se carga a mano y
no tiene forma de enterarse de lo que pasa por contabilidad. Cuando se implementó, 54 de
164 DPF (33%) estaban desalineados.

Se compara contra el **total acreditado** y no contra el saldo a una fecha: una variación
del capital dentro del mes es legítima y la resuelve el cálculo por tramos. Lo que este
candado atrapa es el dato genuinamente mal cargado, que es otra cosa.

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
  Se cargan desde el CRUD de tipos de cuenta (ver *Tipos de cuenta*).

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

## Por qué el capital se desalineaba

El campo `cuenta.capital` es una foto que se escribe una sola vez, al nacer el certificado.
Cuando se auditó había **54 de 164 DPF (33%)** donde el campo y la contabilidad decían
cosas distintas: 46 en cero y 8 con diferencias.

Los tres agujeros, y cómo quedaron cerrados:

**1. Aumentos cargados por contabilidad.** Un depósito adicional al DPF se cargaba desde la
pantalla genérica de comprobantes, que no pasa por el módulo, así que el campo no se
enteraba. Así se desincronizaron ME00158 (+2.000), ME00159 (+2.000), ME00162 (+1.000) y
ME00156 (+8.000).

Un DPF **no recibe dinero a mitad de plazo**: si el socio quiere aumentar el capital, el
certificado se cierra y se abre uno nuevo. Ahora
[VoucherAccoutingServiceBean.validateFixedTermDepositCredits()](../../src/main/com/encens/khipus/service/accouting/VoucherAccoutingServiceBean.java)
**corta** cualquier comprobante que acredite a un DPF con fecha posterior a su apertura, y
lanza [FixedTermDepositCapitalException](../../src/main/com/encens/khipus/exception/finances/FixedTermDepositCapitalException.java).

El control es por fecha y no por un marcador: la apertura de un certificado puede venir en
dos líneas del mismo comprobante y eso es legítimo — pasa en 21 de los 24 casos históricos.
Lo que no lo es, es un abono fechado después de la apertura.

**Esto no contradice el cálculo por tramos.** Son dos cosas complementarias: el corte impide
que se generen aumentos nuevos por esa vía, y los tramos calculan correctamente los que
**ya existen** en la base. De los 24 aumentos históricos, sólo 3 son posteriores a la
apertura: ME00156 (26/03/2026, vigente), ME00155 y ME00115 (cerrados). Sin los tramos, esos
tres se provisionarían mal para siempre.

**2. El campo editable de la ficha.** Sigue siendo el único lugar por donde se puede
desalinear a mano. Cerrarlo está pendiente.

**3. Fallas del flujo de renovación.** Ver la sección siguiente.

### Cómo detectar un desalineado

No hay pantalla dedicada, a propósito: el candado de la provisión ya reporta cualquier
diferencia nombrando los certificados, y lo hace en el momento en que importa. Para un
control fuera de ese circuito está la consulta de la sección 1 de
[query_v6.0.121_update_DPF.sql](../../query/query_v6.0.121_update_DPF.sql).

El capital contable de un certificado es la **suma de los importes acreditados**: dentro de
un mismo certificado el único movimiento deudor es la cancelación.

### Sobre auditar los cambios del campo (descartado)

Al aparecer ME00148 con capital en cero no hubo forma de saber quién lo había puesto así:
`cuenta` no tiene traza de cambios. Se evaluó auditarla con envers —la infraestructura está
cableada desde hace años y sin usar: los listeners en `persistence.xml`, `RevisionEntityInfo`,
`RevisionEntityListener` y la tabla `revisionentidad`, con 0 filas porque ninguna entidad
tiene `@Audited`.

**Se descartó**, y conviene que quede escrito el por qué para no reabrirlo sin motivo:

- El agujero se cierra por prevención, no por forensia. Con el corte en `saveVoucher` y el
  campo de la ficha en solo lectura, el capital solo puede cambiar por la renovación, que
  valida contra el asiento.
- Sería la primera entidad auditada del sistema. Si una anotación de relación está mal, la
  aplicación **no arranca**; si falta la tabla de auditoría, **no se puede dar de alta ni
  modificar ninguna cuenta**.
- `RevisionEntityListener` toma el usuario de la sesión web, así que una escritura de cuenta
  sin usuario logueado fallaría.
- Nadie en el equipo usó envers nunca.

El volumen no era el problema: son 168 cuentas y ~13 altas por año, o sea unas 26 filas de
auditoría anuales. Si alguna vez se quiere el rastro, son cuatro anotaciones y una tabla.

**No confundir con `transaccioncuenta`**, que es otra cosa: una tabla de *movimientos*
(`importe`, `glosa`, `fechatransaccion`, `tipotrans`, `idcuenta`) que está vacía porque los
movimientos de la cuenta ya viven en la contabilidad (`sf_tmpdet` ligado por `idcuenta`).
Llenarla crearía una segunda fuente de verdad para el mismo hecho, que es exactamente el
problema que este trabajo vino a resolver. Su cableado muerto —un campo nunca poblado en
`AccountAction` y un DataModel referenciado solo desde una línea comentada— se eliminó,
porque fue lo que hizo perder tiempo en el diagnóstico.

## Alta y aprobación de un DPF

Un DPF nuevo nace en **`PENDING`**: la ficha está cargada pero el dinero todavía no entró.
No tiene un solo asiento, así que no devenga, no se puede renovar ni cerrar, y no se imprime
su certificado. Queda fuera de la provisión y de los destinos de transferencia por
`AccountServiceBean`, que excluye `PENDING` junto con `ANNULLED`.

El botón **Aprobar** de la ficha lo pasa a `ACTIVE` y genera el comprobante de ingreso de la
apertura, todo en la misma operación:

```
Debe   caja general              capital        cajagral1me / cajagral1mn
Haber  capital del DPF           capital        tipocuenta.CTAP_ME / CTAP_MN, con el certificado enlazado
```

El comprobante se fecha **el día de la apertura**, no el día en que se aprueba: es la fecha
desde la que devenga. En ME se valúa al tipo de cambio de esa fecha; si no está cargado, no
aprueba. El tipo de comprobante sale de `configuracion.tipo_doc_dpf` (**CI**) y no de
`tipocuenta`, porque no depende del plazo ni de la moneda: toda apertura es un ingreso.

Antes de contabilizar, el modal muestra exactamente las líneas que se van a grabar —son la
misma lista de `VoucherDetail` que después recibe `saveVoucher`, no una copia—, con la glosa
editable.

El comprobante nace en estado `PEN`: aprobar el DPF no aprueba el asiento, eso lo sigue
haciendo Contabilidad. Por eso el ícono de PDF de la pestaña Transacciones **no** exige que
el asiento esté aprobado, a diferencia del listado de comprobantes: desde la ficha se imprime
el comprobante tal como quedó.

### El código lo genera el sistema

`cuenta.codigo` es la clave con la que el certificado aparece en la contabilidad y en los
reportes, y escribirlo a mano es de donde salieron los **5 códigos repetidos** del histórico.
En un DPF ya no se escribe: al guardar se toma de `gensecuencia`, prefijo por moneda más
cinco dígitos (`ME00304`, `MN00003`).

Hay **una secuencia por moneda** —`ACCOUNT_DPF_CODE_ME` y `ACCOUNT_DPF_CODE_MN`— porque las
dos series son independientes: ME va por 303 y MN por 2, y un solo contador haría que el
próximo DPF en bolivianos saliera MN00304, heredando el número de la serie de dólares.

La de ME es la vieja `ACCOUNT_DPF_CODE`, renombrada. Existía desde antes —resto de un intento
de numeración automática en la renovación que quedó comentado y nunca corrió— y significaba
ME sin decirlo, porque en la práctica todos los DPF son en dólares (157 contra 2). El nombre
sin sufijo al lado de uno con `_MN` se leía como si fueran "el código" y "el código en
bolivianos", que no es lo que son.

Ojo con la semántica de esta tabla: `valor` es el **último entregado** y `nextValue()`
devuelve `valor + 1`, al revés que `secuencia` (ver *El id: `secuencia` no significa lo mismo
para todos*).

Si el código que entrega la secuencia ya existe se pide el siguiente, hasta diez veces: así
una base con la secuencia atrasada —una copia de producción restaurada sobre desarrollo— se
acomoda sola en vez de trabar el alta.

El incremento **no** pasa por `SequenceGeneratorService`. Ese generador lee, suma y graba
desde Java, y confía en un reintento por `OptimisticLockException` que acá no puede
dispararse: ni `gensecuencia` ni la entidad `Sequence` tienen columna de versión, así que dos
altas simultáneas podrían leer el mismo valor. En su lugar,
`AccountServiceBean.nextAccountCodeNumber()` hace `valor = valor + 1` en el motor: InnoDB
toma el candado de fila en el `UPDATE` y lo suelta al confirmar la transacción del alta, con
lo que la segunda espera y se lleva el número siguiente. Si el alta falla, el incremento se
deshace con ella y no queda un hueco.

Tampoco se arregla el generador compartido: lo usan compras, almacenes, activos fijos y
producción, y este código no necesita meterse en ese camino. La garantía definitiva sería un
índice único sobre `cuenta.codigo`, pero los 5 pares duplicados del histórico lo impiden.

La **renovación** usa la misma secuencia. Hasta la 6.0.123 pedía el código a mano y era la
última vía por la que uno podía repetirse; ahora lo genera antes de tocar nada, así un fallo
no deja el certificado viejo marcado como inactivo a medio camino.

`codigo` es de los DPF. Una cuenta de ahorro se identifica por `nocuenta` y va con el código
en null; la entidad lo declaraba `nullable = false` —y esa anotación no es decorativa,
Hibernate valida la propiedad antes del insert—, así que hubo que aflojarla para que
coincidiera con la columna, que siempre aceptó nulos.

La duplicidad se valida además al guardar, pero **sólo si el código cambió**. Validarlo
siempre dejaría esos 10 certificados históricos imposibles de guardar: chocarían contra su
propio gemelo en cada intento, sin que nadie hubiera tocado el código.

### El estado no es un campo libre

El estado dejó de ser un combo: es la consecuencia de una operación. Un DPF nace `PENDING`,
lo pasa a `ACTIVE` el botón **Aprobar** —lo único que genera el asiento—, y a `INACTIVE` la
renovación o el cierre. Elegirlo a mano dejaba un certificado devengando sobre un capital que
nunca entró, o permitía contabilizar la apertura dos veces volviéndolo a `PENDING`.
`AccountAction.validateStateTransition()` sigue como red por si alguien fuerza el submit.

Para marcar la cuenta que nunca existió está el botón **Anular**, con permiso propio
(`ACCOUNTANNUL`) y modal de confirmación. Exige que no tenga **ningún** asiento: un
certificado ya contabilizado se cierra, no se anula.

### Una vez aprobado, los datos se bloquean

Apertura, vencimiento, capital y tipo de cuenta quedan deshabilitados en la ficha: cambiar
cualquiera de ellos la desincroniza del asiento que ya está en el mayor y del interés que se
viene devengando. Alcanza también a los certificados anteriores a esta pantalla, que
nacieron `ACTIVE` sin pasar por `PENDING` —correcto, porque todos ellos ya están
contabilizados.

### Guardar ya no vacía el formulario

`create()` y `update()` terminaban la conversación, y la regla de navegación de `update`
además abría una **conversación nueva**: el action volvía vacío y la ficha se veía como un
alta, sin vencimiento ni capital, aunque en la base los datos estuvieran bien. Ahora
`create()` devuelve `SUCCESS` con `op=UPDATE` y pages.xml redirige a la misma pantalla
propagando la conversación; `update()` se queda sin navegar. El redirect del alta no es
cosmético: da un límite de transacción limpio antes de que se pueda aprobar el certificado
en esa misma conversación. De paso, un segundo clic en Guardar ya no da de alta la cuenta
dos veces.

## Estado ANULADO

`AccountState.ANNULLED` distingue el certificado fantasma —se dio de alta y el desembolso
nunca se contabilizó, o se cargó dos veces y esta es la copia— del `INACTIVE`, que es un
certificado real ya cerrado y que sí devengó intereses.

Las anuladas quedan fuera de toda operación (provisión, renovación, transferencias) pero se
siguen listando. Solo se puede anular una cuenta **sin ningún movimiento contable**: si
tiene asientos corresponde `Inactivo`, y así nadie le borra el devengue al histórico.

Al implementarlo había exactamente 2 cuentas en esa situación en todo el sistema, ambas DPF
y ambas el hermano huérfano de un código duplicado: `idcuenta` 112 (ME00122) y 45 (ME0062).

**Ojo con los códigos duplicados.** Hay 5 códigos de DPF repetidos en dos filas distintas.
En 3 de esos pares **ambas filas son certificados legítimos de socios distintos** que
comparten código por un error de captura. La clave real es `idcuenta` / `nocuenta`:
cualquier consulta o corrección por `codigo` toca dos certificados.

## Renovación de un DPF

En cada renovación **nace un registro nuevo** en `cuenta` y el anterior pasa a inactivo; no
se reutiliza el mismo. El asiento hace las dos puntas: debita el pasivo del certificado
viejo y acredita el del nuevo, cada línea ligada a su `idcuenta`.

Pantalla: [renewalDeposit.xhtml](../../view/customers/renewalDeposit.xhtml), a la que se
entra con el botón **Renovación** de la ficha de la cuenta.

### Cuándo se puede renovar

`AccountAction.isRenewable()` exige tres cosas, y cada una falla con su propio mensaje
(`getRenewalBlockedReason()`, así el usuario lee el motivo real y no un texto genérico):

| Condición | Si no se cumple |
|---|---|
| Que el certificado **no esté terminado** (ver abajo) | *Ya fue renovado o cerrado. Su capital pasó al certificado nuevo.* |
| Que tenga `fechavence` cargada | *No tiene fecha de vencimiento, no se puede calcular el interés ni el plazo.* |
| Que el plazo esté **cumplido**: `hoy >= fechavence` | *El plazo vence el {fecha}.* |

Nunca se pagan intereses por días que no transcurrieron. El día del vencimiento cuenta como
cumplido, porque el certificado devenga hasta esa fecha inclusive.

#### Terminado: hay que mirar el estado **y** el mayor

`isClosed()` no alcanza con el estado, porque se llega por dos caminos distintos:

- **Renovado desde el sistema** → `createDpfRenewal()` deja el viejo en `INACTIVE`.
- **Anulado** → `ANNULLED`.
- **Retiro total por comprobante** → **el estado no se toca**: la cuenta queda `ACTIVE` con
  saldo cero. Mirando sólo el estado, este caso se escapaba y el certificado se podía
  renovar de nuevo, duplicando el pasivo.

El saldo cero **sólo cuenta si hay movimientos**. Un certificado sin ningún asiento también
da cero y no está cerrado: está sin contabilizar (es el caso de ME00148, ver *Por qué el
capital se desalineaba*). Por eso `calculateTotalAmounts()` guarda además la cantidad de
asientos.

#### Dónde bloquea cada caso

La distinción importa para la UX:

- **Terminado** → el botón **Renovación** de la ficha sale deshabilitado y `renewalDPF()`
  corta antes de abrir. No hay nada que calcular ni que renovar.
- **Plazo vigente** → la pantalla **se abre igual** y muestra todos los cálculos (capital,
  interés por tramos, retención, total). Lo que se deshabilita es el botón **Renovar** de
  esa pantalla, así se puede consultar sin poder ejecutar.

En los dos casos `validateRenewal()` vuelve a validar antes de grabar: el bloqueo no vive
sólo en la vista.

**Fecha de inicio del nuevo certificado** = `max(fechavence + 1, hoy)`. Renovando el día del
vencimiento arranca al día siguiente; si el vencimiento pasó hace tiempo, arranca hoy. Así no
queda un tramo sin devengar entre los dos certificados ni se arranca antes de que el anterior
termine. Sin `fechavence` cae a hoy en vez de reventar: `DateUtils.addDay(null, 1)` tiraba
`NullPointerException` al **entrar** a la pantalla, en DPF viejos sin vencimiento cargado.

### Interés ganado por tramos

El interés se paga por el **plazo completo contratado** (`tipocuenta.dias`). Si durante ese
plazo el certificado recibió un aumento de capital, se parte en tramos: los días anteriores
al aumento devengan sobre el capital que tenía antes.

```
ME00156 (apertura 26/08/2025, 4%, aumento de 8.000 el 26/03/2026):
  26/08/2025 a 25/03/2026   212 días   27.141,49    639,33
  26/03/2026 a 20/08/2026   148 días   35.141,49    577,88
                            ────────              ────────
                            360 días              1.217,22
```

Antes tomaba el capital final (35.141,49) para todo el plazo y daba 1.405,66 — se pagaban
212 días sobre un capital que todavía no existía. La diferencia, 188,44, es exactamente
`8.000 × 4% × 212/360`, y coincide con lo que la provisión mensual venía sobreprovisionando
por el mismo motivo.

Los días del último tramo salen **por diferencia contra el plazo contratado**, así la suma de
los tramos siempre da el plazo exacto y no depende de cómo caigan las fechas.

Criterios acordados con Contabilidad:

- **Retención (RC-IVA):** sobre el interés total, no por tramo. Una sola línea.
- **Capital:** un solo importe, el final. Los capitales por tramo se ven en el desglose del
  interés.
- **Asiento:** una sola línea de interés con el total, misma cuenta y misma moneda.

Con un solo tramo —el caso normal— la pantalla se ve igual que siempre: un único importe.

### Fallas corregidas

| Falla | Corrección |
|---|---|
| El interés se calculaba sobre el capital final para todo el plazo | Por tramos, según el capital vigente en cada uno |
| Se podía renovar antes del vencimiento, pagando días no transcurridos | Bloqueado en tres capas |
| `calculateTotalAmounts` acumulaba sin resetear | Arranca en cero. Era la peor: `renewalDPF()` toma de ahí el capital base, así que entrar dos veces a una cuenta en la misma conversación podía duplicarlo. |
| El panel del certificado viejo era editable | A solo lectura. El submit de Renovar lo escribía sobre la cuenta vieja y `updateAccount()` lo persistía, alterando fechas y códigos de un certificado ya vencido. |
| Renovación parcial podía grabar capital 0 | Valida `> 0` y `<=` el total disponible |
| "Capital parcial" conservaba el valor anterior | Se limpia al tildar y al destildar el check; el campo arranca en `null` para verse vacío y no con un `0,00` |
| Socio, tipo de cuenta y vencimiento del nuevo venían en blanco | Precargados con los del certificado que se renueva, editables |
| Nada verificaba el capital contra el asiento | Si el capital de la ficha no es igual a lo acreditado, no registra nada |
| Tipo de comprobante opcional | Requerido |
| Tipo de cambio del último cargado | El de la fecha del asiento |
| El comprobante no fijaba su fecha | Usa `startDateDPF`. `Voucher.date` default es `new Date()`, así que una renovación registrada días después se contabilizaba con la fecha de hoy; coincidía solo porque siempre se hacen el mismo día. |
| El retiro parcial en cuentas MN se armaba como ME | Corregido a MN. Convertía el importe por el tipo de cambio. |
| Un DPF ya renovado o cerrado se podía renovar otra vez | `isClosed()` mira estado **y** saldo del mayor; bloquea el botón de la ficha, la entrada y el guardado |
| `NullPointerException` al entrar con `fechavence` en NULL | La fecha de inicio cae a hoy; el botón Renovar queda deshabilitado con su motivo |

`createDpfRenewal` usa `@End(beforeRedirect = true, ifOutcome = Outcome.SUCCESS)`: sin el
`ifOutcome`, una validación fallida cerraba igual la conversación y la pantalla quedaba
vacía.

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

## Qué no se puede hacer con un DPF desde un comprobante suelto

Un DPF no recibe dinero a mitad de plazo ni admite retiros parciales, y su cierre va por la
pantalla **Cerrar DPF** —la única que además reversa la provisión acumulada y da de baja el
certificado—. Cargarlo como asiento suelto es lo que dejaba la contabilidad diciendo un
capital y la ficha otro (ver *Por qué el capital se desalineaba*).

El control **es una consulta, no una excepción**: `findFixedTermDepositConflict(fecha,
detalles)` devuelve el conflicto o `null`. Lo llama `VoucherCreateAction` en `create()` y
`update()`, junto a la validación de balance, **antes** de entrar al EJB. Si hay conflicto
muestra el mensaje y redisplaya, con el asiento cargado intacto.

### Nivel 1 — bloquea, por el certificado enlazado

Identifica el DPF por el **enlace de la línea** (`sf_tmpdet.idcuenta` → `cuenta` →
`tipocuenta.tipo = 'DPF'`), **nunca por el código de cuenta contable**. Eso importa: hay una
cuenta distinta por plazo y moneda —hoy siete— y las que se agreguen desde el ABM de tipos de
cuenta quedan cubiertas sin tocar código.

| Caso | Mensaje |
|---|---|
| **Abono** posterior a la apertura | Aumento de capital: hay que renovar |
| **Débito** que deja saldo | El retiro parcial no existe |
| **Débito** que cierra el certificado | Va por *Cerrar DPF* |

Un abono del **mismo día** de la apertura es legítimo: puede venir en dos líneas del mismo
comprobante, y pasa en 21 de 24 casos históricos.

No afecta a la renovación ni al cierre: los dos llaman a `saveVoucher()` directo desde
`AccountAction`, sin pasar por la pantalla de comprobantes, así que el control no puede
bloquearse a sí mismo.

### Nivel 2 — sólo advierte, por la cuenta contable

Cubre el hueco del nivel 1: una línea que toca una cuenta de capital de DPF **sin indicar el
certificado**. Ahí no se puede bloquear —sin saber cuál es el certificado no hay contra qué
comparar, y un asiento de reclasificación sobre esas cuentas es legítimo—, así que sólo avisa.

La lista de cuentas **se calcula, no se hardcodea**: sale de `tipocuenta` y se queda con las
que usan tipos DPF **y sólo tipos DPF**. La exclusividad no es un detalle: `2120130200 —
Cuenta Ahorros Socios MV` está configurada en los 7 tipos de DPF **y también en un tipo de
ahorros**, así que queda afuera; advertir sobre ella sería ruido sobre movimientos legítimos.
Hoy la consulta devuelve 7 cuentas, todas `2130xxxxxx`.

**Por qué no va dentro de `saveVoucher()`.** Se intentó así primero y estuvo mal. `saveVoucher`
es el chokepoint de **61 llamadas en 20 archivos** de todos los módulos —almacén, producción,
activos fijos, ventas, créditos—, y `VoucherAccoutingServiceBean` es un `@Stateless`: una
`RuntimeException` sin `@ApplicationException` hace que el contenedor **marque la transacción
para rollback y la envuelva en `EJBException`**. Consecuencias:

- Cualquier módulo podía terminar con la transacción muerta por una validación de DPF.
- El `catch` específico de `create()` nunca matcheaba, por el wrapping: el mensaje era código
  muerto.
- El redisplay reventaba con `LazyInitializationException` al renderizar
  `partnerAccount.fullAccountName`, porque `Account.partner` es `@ManyToOne(LAZY)` y la
  transacción ya estaba marcada para rollback.

Una validación de negocio no va en un chokepoint transaccional compartido.

**Y el texto de la cuenta de ahorro se materializa en el setter**, no en la vista:
`VoucherCreateAction.setPartnerAccount()` guarda `partnerAccountLabel` mientras el contexto de
persistencia está vivo, y la vista muestra ese String. Así **ningún** camino de error puede
volver a reventar la pantalla por un proxy sin sesión — tampoco el `catch (Exception e)`
genérico, que tenía el mismo problema desde antes.

## Cierre de un DPF

Botón **"Cerrar DPF"** en la ficha de la cuenta, junto a *Renovación*, con permiso
`DPFCLOSE`. Es para cuando el socio se lleva el dinero y no renueva.

Reemplaza al procedimiento manual —cargar el comprobante por Contabilidad y después
acordarse de inactivar el certificado—, cuyo segundo paso se olvidaba. Ahora el asiento y
la baja del certificado van en **la misma transacción**: o pasan las dos cosas o no pasa
ninguna.

### Cuánto interés se paga

| Fecha de cierre | Interés |
|---|---|
| **Hasta el vencimiento inclusive** | **0** — retiro anticipado, el socio resigna lo devengado |
| **Después del vencimiento** | **completo**, con el mismo cálculo por tramos de la renovación |

La retención RC-IVA del 13% sólo aplica cuando hay interés, y sólo si el socio la tiene
marcada (`cuenta.ret`).

### El asiento

```
Debe   CTAP_MN / CTAP_ME          capital
Debe   CTACF_MN / CTACF_ME        provisión ya contabilizada
Debe   gasto i_ppag_dpf_*         interés neto − provisión acumulada
Haber  cajagral1mn / cajagral1me  capital + interés neto
```

**Es una sola fórmula para los dos casos.** Cuando el interés es 0 —retiro anticipado— la
tercera línea da negativo y se invierte a HABER: eso es la reversa de todo lo provisionado.

Es exactamente la estructura que el contador ya venía armando a mano: se debita el pasivo
con lo que hay acumulado y el resto va a gasto.

### El RC-IVA no se contabiliza

**Por decisión expresa**, y siguiendo el criterio que se viene usando desde 2019: la
retención se descuenta del importe que se le entrega al socio y se le informa —en pantalla
y en el certificado impreso—, pero **no se acredita en ninguna cuenta**.

Verificado contra la base: `2420310100` (*RC IVA retenido a Clientes*) y las otras cuentas
de retención **no tienen un solo movimiento** entre 2019 y 2026, ni en renovaciones de DPF
ni en capitalización de ahorros. Las 25 renovaciones más recientes debitan `CTACF_*` con el
interés **neto** y no llevan línea de retención — comprobado al centavo en ME00160 (79,00
contra 78,84 de neto sobre 90,62 brutos) y ME00161 (764,00 contra 763,76).

Por eso el ajuste al gasto se calcula contra el **interés neto** y no contra el bruto: sin
línea de retención, compararlo contra el bruto dejaría el asiento descuadrado justo por ese
importe. Así la retención no queda colgada en ningún lado, simplemente baja el costo
financiero del certificado.

Dimensión de lo que esto implica: entre 2019 y 2026 las renovaciones debitaron unos
**65.700 $us netos**, lo que supone una retención implícita del orden de **9.800 $us** nunca
registrada como deuda fiscal. 149 de los 167 DPF tienen la marca `ret` activa. Queda
anotado; es una definición del contador, no del sistema.

### Por qué hay que debitar el pasivo COMPLETO

La provisión mensual fue acreditando `CTACF_*` con el interés **bruto**. Si el cierre
debita menos que eso, la diferencia **queda en la cuenta para siempre**: un pasivo de
intereses por pagar que no le corresponde a ningún DPF vivo, que se acumula con cada
cierre y que después nadie puede rastrear.

Ejemplo real (ME00170, 6.120 $us al 3%, 08/06 → 05/12): la provisión acumula 89,76 entre
junio y noviembre y el interés neto del plazo es 79,87. El asiento debita los **89,76
completos** y los 9,89 de diferencia se reversan contra el gasto:

```
Debe   2130420000  capital                        6.120,00
Debe   2180320000  provisión acumulada               89,76
Haber  4110320000  ajuste al gasto                    9,89
Haber  1110220000  caja ME                        6.199,87
                                        ─────────────────────
                                          6.209,76 = 6.209,76
```

Debitando sólo el neto —como hace la renovación— quedarían **9,89 colgados** en el pasivo.

**La renovación tiene este mismo faltante**: debita el neto e ignora lo acumulado. No se
corrigió acá por decisión expresa, para no cambiarle el criterio a un flujo en uso. Queda
anotado.

### La provisión acumulada se recalcula, no se lee

El asiento mensual de la provisión **agrupa por cuenta contable**: una sola línea para
todos los DPF juntos, sin rastro de cuánto le tocó a cada certificado. Así que el monto a
debitar hay que recalcularlo.

Por eso el cálculo vive en
[FixedTermDepositAccrual](../../src/main/com/encens/khipus/util/FixedTermDepositAccrual.java),
compartido con la provisión: **un lado acredita y el otro debita, y si cada uno calculara
por su cuenta cualquier diferencia de redondeo quedaría colgada en el pasivo.**

Condición para que el recálculo coincida con los libros: **la provisión tiene que estar al
día**. Si falta generar algún mes, el cierre debitaría un pasivo que nunca se acreditó.

## Tipos de cuenta (`tipocuenta`)

CRUD en **Atención al cliente → Configuración → Tipo de Cuenta**, permiso `ACCOUNTTYPE`.
Es de donde salen las cuentas de los asientos de este módulo, así que un error acá se
arrastra a todos los certificados de ese tipo.

**Las cinco cuentas contables eran de sólo lectura.** Estaban mapeadas
`insertable = false, updatable = false`: JPA no las escribía **ni al crear**, se cargaban
por SQL. Un tipo dado de alta desde una pantalla habría nacido con `CTAP_*` y `CTACF_*` en
NULL, y la provisión y la renovación no habrían podido armar el asiento. Se abrieron para
escritura al hacer el CRUD.

`CTAP_MV` se dejó **EAGER** como estaba: la leen `VoucherCreateAction`, `AccountServiceBean`
y `CreditTransactionAction`. Las otras cuatro ya eran LAZY.

**Un tipo DPF no se guarda sin `CTACF_MN` y `CTACF_ME`.** Son la contrapartida del asiento
de provisión: sin ellas el error aparecería recién a fin de mes, ya arrastrado por todos los
certificados de ese tipo. Se corta en el alta.

El **tipo de ahorro** (`tipo`: SOC / CAJ / DPF) se fija en el alta y no se edita: la columna
está mapeada `updatable = false`, y cambiarlo movería de listado a certificados ya emitidos.

### Inactivar en vez de borrar

`cuenta.idtipocuenta` es FK no nula (`cuenta_ibfk_3` → `tipocuenta`), así que **un tipo con
cuentas asociadas no se puede borrar**: el `flush()` de `GenericServiceBean.delete()` rebota
contra MySQL, se convierte en `ReferentialIntegrityException` y sale la advertencia. El
`@TransactionAttribute(REQUIRES_NEW)` mantiene ese rollback aislado de la conversación.

Lo que corresponde es **desactivar** (`activo`). El factory `accountTypeList` —el que
alimenta los combos de abrir y renovar cuenta— filtra `active = true`, así que un tipo
inactivo deja de ofrecerse. El listado del CRUD sigue trayendo todos, con filtro
Todos / Sí / No; si no, no habría manera de reactivarlos.

**Ojo:** si un tipo con DPF vivos se inactiva, al abrir esas cuentas el combo no contendrá
su tipo. No se corrompe nada —el `required` hace fallar el guardado—, pero no se podrán
guardar cambios en esa cuenta sin elegir un tipo activo.

### El id: `secuencia` no significa lo mismo para todos

El alta usa un `@TableGenerator` sobre `secuencia`, y ahí **`valor` es el próximo id a
entregar**, no el último entregado: `MultipleHiLoPerTableGenerator` devuelve lo que leyó y
recién después incrementa.

**No es la misma semántica que la fila de `funcionalidad`**, que va por función almacenada,
donde `nextValue` devuelve `valor + 1` y por eso se siembra con `MAX(...)`. Las dos
conviven en la misma tabla: normalizarlas a un criterio único rompe una de las dos.

No había fila para `tipocuenta` (el máximo era 9). Sin fila, Hibernate la crea en 0,
descarta el 0 y **el primer alta intenta el id 1**, que ya existe → clave duplicada. Por eso
`query_v6.0.122_terdemol.sql` la siembra en `MAX + 1`.

## Tipos de cambio (`arcgtc`)

Hay **dos** tablas de tipo de cambio en el sistema y no son la misma:

| | `tipocambio` | `arcgtc` |
|---|---|---|
| Entidad | `ExchangeRate` | `FinancesExchangeRate` |
| PK | `idtipocambio` autoincremental | compuesta (`clase_cambio`, `fecha`) |
| Campos | compra / venta / tasa | `tipo_cambio` decimal(10,6) |
| Pantalla | RRHH (existía) | Finanzas → Configuración (nueva) |
| La usa | planillas | **contabilidad: provisión DPF, comprobantes en ME** |

El CRUD nuevo es sobre `arcgtc`. Su catálogo de clases es `arcgcc` (**D** = dólar,
**U** = UFV) y `cg_moneda` mapea moneda → clase.

Como `clase_cambio` y `fecha` son la clave primaria y son inmutables, la edición sólo
permite corregir el valor: para cambiar clase o fecha hay que eliminar y volver a crear.
