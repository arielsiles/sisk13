# Reporte Diario de Producción — criterios de datos (ULEXITA y BARITINA)

> Complementa [`xproduction_ulexita_daily_report_spec.md`](xproduction_ulexita_daily_report_spec.md)
> (que describe el diseño funcional de ULEXITA) y
> [`xproduction_ordenes_calculos.md`](xproduction_ordenes_calculos.md) (fórmulas de la orden).
> Este documento fija **de dónde sale cada número** de los reportes diarios y qué queda fuera.

La pantalla `view/xproduction/dailyProductionReport.xhtml` es un dispatcher:
`DailyProductionReportAction` enruta según `ProductionLine.reportTemplateCode` a
`UlexitaDailyReportAction` o `BaritinaDailyReportAction`. Cada uno mantiene su propio layout.

## 1. Artículos: siempre desde la configuración de la línea

Ambos reportes resuelven los `cod_art` desde `xpr_linea`, y solo caen a derivarlos de las
órdenes si la línea no está configurada:

| Template | Materia prima | Producto terminado |
|----------|---------------|--------------------|
| ULEXITA | `cod_art_mp_principal` | `cod_art_pt_a`, `cod_art_pt_b` |
| BARITINA | `cod_art_mp_principal` | `cod_art_pt_principal` |

**El template describe la forma del proceso, no el producto.** Varias líneas pueden compartir el
mismo `report_template_code` y diferenciarse solo por configuración: molienda de baritina y
chancado de baritina usan las dos el template BARITINA, con distinto PT y distinto factor. Agregar
una línea nueva de la misma forma —otro chancado, otra materia prima— es una fila de configuración
y **no cuesta código**. Por eso los títulos de columna del Excel se arman con el nombre del
artículo configurado y no con literales.

Los límites del template BARITINA: **una** MP y **un** PT por línea (si una línea produjera dos
productos con rendimientos distintos haría falta el patrón PT A/B de ULEXITA), y captura uso MP,
PT, turnos, observación y distribución por zonas productivas.

Ambos campos se configuran en `view/xproduction/productionLine.xhtml` (bloques condicionales por
template, reutilizando el mismo modal `mpPrincipalListModalPanel`).

**Por qué importa**: derivar los artículos desde las órdenes deja el código en `null` cuando el
período no tiene órdenes, y con código nulo *todas* las consultas devuelven vacío — el reporte
entero sale en cero, incluido el saldo anterior. Fue el bug de abril 2026 en BARITINA.

**Solo se reportan los artículos configurados.** La columna de saldo arrastra el saldo de un
único artículo tomado de Saldos de Almacén; mezclar otro artículo la vuelve incomparable contra
cualquier pantalla del sistema. Lo no configurado no se pierde: ver §4.

## 2. Fuente de cada columna

| Concepto | Fuente | Filtro |
|----------|--------|--------|
| SALDO ANTERIOR (MP y PT) | `XProductionBalanceService.computeBalances` — la misma pantalla "Saldos de Almacén" | corte = último día del mes anterior, fin de día |
| INGRESO materia prima | `CollectMaterial` / `acopiomp`, campo `pesobal` | estados `APR`, `CONTA` |
| USO / CONSUMO de MP | `XSupply` / `xpr_insumo` del artículo MP configurado (ULEXITA usa el consumo teórico `consumoMpCalc`) | — |
| USO OTRAS LINEAS (solo BARITINA) | `XSupply` del mismo artículo MP, en órdenes de otras líneas | ver §2.1 |
| PRODUCTO TERMINADO | `XProductionProduct` / `xpr_producto` de los PT configurados | — |
| DESPACHO | `WarehouseVoucherDispatchDetail` / `inv_valedespacho_det` | estados `APROBADO`, `FINALIZADO` |

El saldo anterior **debe** salir del balance y no de las órdenes: el saldo real incluye cargas y
ajustes por vale que la producción no ve. Ejemplo real: el PT `2021 BARITINA MOLIDA` tenía
140.000 KG al 31/03/2026 provenientes de un vale de entrada, con cero órdenes de producción.

### 2.1 Materia prima compartida entre líneas

Dos líneas pueden consumir el **mismo** artículo de materia prima (caso real: molienda y chancado
de baritina, ambas sobre `4 BARITINA`). El saldo de un artículo es uno solo, así que si cada
reporte descontara únicamente su propio consumo, los dos mostrarían saldo de más y ninguno
cuadraría contra Saldos de Almacén.

Por eso el reporte del template BARITINA descuenta también el consumo ajeno, y lo hace visible en
una columna **USO OTRAS LINEAS**:

```
SALDO MP = saldo_ant + INGRESO − USO (esta línea) − USO OTRAS LINEAS
```

Con ese término el saldo de MP de **todas** las líneas que comparten el artículo coincide, día por
día, con Saldos de Almacén. Que el saldo anterior y el ingreso por acopio salgan iguales en los dos
reportes es correcto: es la misma pila física entrando al mismo almacén.

La columna se muestra cuando otra línea está **configurada** con la misma
`cod_art_mp_principal`, o cuando de hecho hubo consumo ajeno en el período. Las líneas con materia
prima exclusiva no la ven nunca, y el término que se resta es cero.

## 3. Fecha con la que se ubica una orden en el día

Se usa **`productionPlan.date`**, con `initDate` solo como respaldo si la orden no tiene plan.
Es el mismo criterio del Kardex de Artículos y de `XProductionBalanceService`.

Motivo: un turno de noche que arranca pasada la medianoche tiene `initDate` en el día calendario
siguiente al de su plan. Usando `initDate` la fila cae un día corrida y, en el borde del mes, la
orden puede quedar del lado equivocado del corte — con lo que el saldo anterior (criterio plan) y
el arrastre diario (criterio initDate) dejan de cuadrar entre sí.

## 4. Marcadores en OBSERVACIONES

Los datos que no entran en ninguna columna se anotan en la columna OBSERVACIONES para que no
queden perdidos. Claves en `messages_app.properties`, prefijo `DailyProductionReport.obs.*`:

| Marcador | Cuándo | Reporte |
|----------|--------|---------|
| `adjustment` | movimiento de inventario por vale que **no** proviene de un despacho | ULEXITA y BARITINA |
| `unconfiguredProduct` | la orden produjo un PT que no está configurado en la línea | BARITINA |
| `unconfiguredSupply` | la orden consumió otra **materia prima** distinta de la MP configurada | BARITINA |

`unconfiguredSupply` se limita a los insumos del **mismo almacén que la MP configurada**. El
marcador existe para detectar materia prima que se consume y no está entrando en la columna USO;
envases, agua, aglutinantes y demás consumibles viven en otros almacenes y no forman parte de esa
historia, así que no lo disparan. Sin ese recorte el marcador saldría todos los días: la línea
ULEXITA, por ejemplo, consume seis artículos además de su MP, desde tres almacenes distintos.

Los vales de despacho se excluyen del marcador porque ya tienen su propia columna. La separación
es exacta y no depende de texto: `inv_valedespacho.no_trans_vale` + `no_cia_vale` enlazan el
despacho con su vale (`BaritinaDailyReportService.adjustmentRows`).

## 5. Limitación conocida: los vales no mueven el saldo del reporte

Los reportes **no arrastran** los ajustes por vale en sus saldos: sus columnas de flujo son
acopio, producción y despacho. Un vale de ajuste a mitad de mes queda visible en OBSERVACIONES,
pero el saldo del reporte se separa del de Saldos de Almacén desde ese día hasta fin de mes, y el
saldo anterior del mes siguiente (que sí se recalcula del balance) "salta".

Decisión explícita: se prefirió la traza en observaciones antes que agregar una columna AJUSTES.
Si en el futuro los ajustes dejan de ser excepcionales, la solución es esa columna (neto E−S por
día de `adjustmentRows`), con lo cual el saldo de cada día pasa a ser idéntico al de Saldos de
Almacén por construcción — son las mismas fuentes.

## 6. Lo anulado no se cuenta nunca

Nada en estado anulado entra al reporte: no se suma, no se resta y no genera marcador. Cada
fuente lo excluye por lista blanca de estados, así que basta con no agregar el estado anulado:

| Fuente | Filtro | Estado anulado excluido |
|--------|--------|-------------------------|
| Órdenes de producción (`xpr_produccion`) | `state <> ANL` | `ANL` |
| Acopio (`acopiomp`) | `state in (APR, CONTA)` | `ANL` |
| Despachos (`inv_valedespacho`) | `state in (APROBADO, FINALIZADO)` | `ANULADO` |
| Vales / movimientos (`inv_movdet`) | `state = APR` | `ANL` y `PEN` |

El filtro de órdenes vive en `BaritinaDailyReportService.findProductions`, que usan **los dos**
reportes, y replica el criterio de `XProductionBalanceService` para que el reporte y "Saldos de
Almacén" no discrepen.
