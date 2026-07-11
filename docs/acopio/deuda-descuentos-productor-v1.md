# Diseño: Deuda de descuentos por productor (arrastre / carry-forward)

> Estado: **propuesta para aprobar** (sin implementar aún).
> Contexto del motor de acopio: ver [analisis-planilla-acopio-leche-v3.md](analisis-planilla-acopio-leche-v3.md).
> Fecha: 2026-07-10.

## 1. Problema
Hoy los descuentos (`movimientosalarioproductor` + alcohol GAB + GA) se aplican **completos**
en la quincena de su fecha, aunque superen lo ganado → **líquido negativo** (ej. ELIAS: ganado
4.025, veterinario 4.337 → −312). No hay tope, ni saldo pendiente, ni arrastre, y el `estado` del
movimiento no refleja si se cobró.

## 2. Regla de negocio (confirmada)
- **Sin piso del 2%.** Se cobra **hasta donde alcance**; el **líquido nunca es negativo** (mínimo 0);
  lo no cobrado queda como **deuda que se arrastra** a la(s) siguiente(s) quincena(s).
- **Orden de aplicación y qué arrastra:**
  1. **Retención IT/IUE** — prioridad, **NO arrastra** (por diseño ≤ ganado).
  2. **Comisión Banco** — prioridad, **NO arrastra**.
  3. **GA** — prioridad, **NO arrastra** (hoy constante 0).
  4. **Alcohol** (prorrateo GAB) — prioridad, **NO arrastra** (no es movimiento por productor).
  5. **Movimientos por productor que arrastran (FIFO, deuda más antigua primero):**
     **Veterinario, Crédito, Concentrados, Yogurt, Tachos, Otros Egresos.**
  - **Otros Ingresos**: no es deuda (suma al productor).
  - **Solo los movimientos por productor del punto 5 generan y arrastran deuda.**
- **Commit al Contabilizar**; el paso "Aprobar" se **unifica/obsoleta**.
- Productor con **0 litros** esa quincena: **no aparece** (la deuda espera a que vuelva a entregar).
  Con leche entregada pero descontado todo → **líquido 0, sí aparece** en planilla y boleta.
- Boleta: **solo el líquido** por ahora (sin línea de deuda; se puede agregar después).

## 3. Modelo de datos

### 3.1 `SalaryMovementProducer` (`movimientosalarioproductor`) — agregar
- `saldo` DECIMAL(16,2): saldo pendiente por cobrar. Al crear el movimiento `saldo = valor`.
  **Fuente de verdad de la deuda** de ese movimiento.
- **Estados redefinidos** (el `APPROVED` actual no tiene sentido en este flujo):
  - `PENDIENTE` — `saldo > 0` (queda por cobrar).
  - `PAGADO` — `saldo = 0` (cobrado totalmente).
  - Aplica a los tipos que arrastran. Comisión Banco (no arrastra) se marca `PAGADO` al
    contabilizar aunque no se haya cobrado el 100% (se "perdona" el remanente).

### 3.2 Tabla nueva `aplicacion_descuento_productor` (trazabilidad + reversa)
`(id, idmovimientosalarioproductor, idregistropagomateriaprima, monto_aplicado, fecha)`
- Registra **cuánto se cobró de cada movimiento en cada planilla**. Garantiza:
  no se pierde trazabilidad, no se olvida ningún descuento, y permite **revertir** al anular.

## 4. Algoritmo de cálculo (por productor, en la generación)
```
ganado    = leche*precio + ajuste - reserva
disponible = ganado

# Prioridad, NO arrastran (se aplican hasta donde alcance; su remanente NO se guarda):
aplicar(retención IT/IUE)     ; disponible -= aplicado
aplicar(comisión banco)       ; disponible -= aplicado
aplicar(GA)                   ; disponible -= aplicado
aplicar(alcohol)              ; disponible -= aplicado

# Arrastran (FIFO por fecha, más antiguo primero) — solo movimientos por productor:
para cada movimiento (saldo>0, fecha<=fin, tipo en {VET,CREDITO,CONCENTRADOS,YOGURT,TACHOS,OTROS_EGRESOS})
   aplico = min(saldo, disponible)
   registrar aplicacion_descuento(movimiento, registro, aplico)
   saldo_previsto = saldo - aplico          # se COMMITEA al contabilizar
   disponible -= aplico
   si disponible == 0: break

liquido = disponible          # >= 0
# movimientos con saldo>0 -> quedan de deuda para la proxima quincena
```
- Los montos aplicados por tipo alimentan el registro de la planilla (veterinario, crédito, … =
  lo **efectivamente cobrado**), como hoy, pero **capados**.
- **Ejemplo ELIAS** (ganado 4.025, vet 4.337, sin otros): cobra 4.025 → líquido **0**, deuda **312**.

## 5. Ciclo de vida, estados y botones

Todo el ciclo vive en **`view/production/rawMaterialPayRoll.xhtml`** (nivel periodo/quincena,
todas las zonas ILVA a la vez, como ya hace `deleteAll`). Estados de la planilla
(`RawMaterialPayRoll.state`): `PENDING` → `APPROVED` → `CONTABILIZADO` (nuevo).

| Botón | Visible cuando | Qué hace con la deuda |
|-------|----------------|-----------------------|
| **Generar planilla** | no existe planilla del periodo | Calcula, crea registros + `aplicacion_descuento`. **No** toca `saldo`. |
| **Aprobar planilla** | estado `PENDING` | Revisión/lock: aprueba planilla + sesiones. **No** commitea la deuda. → `APPROVED`. |
| **Contabilizar planilla** | estado `APPROVED` | **Commit**: reduce `saldo` por cada aplicación, marca `PAGADO` (comisión → `PAGADO` siempre), genera el **asiento** (montos cobrados). → `CONTABILIZADO`. |
| **Revertir planilla** | estado `CONTABILIZADO` | `annulVoucher` + **restaura `saldo`** + movimientos a `PENDIENTE` + reabre → estado vuelve a `PENDING`. Luego se puede Borrar o rehacer. |
| **Borrar planilla** | `PENDING` o `APPROVED` (no contabilizado) | Elimina planilla + `aplicacion_descuento`. `saldo` intacto (no se había commiteado). Para regenerar. |

- **La deuda se commitea SOLO al Contabilizar** (el punto donde sale el dinero). "Aprobar" queda
  como gate de control previo (requisito para contabilizar).
- El botón/vista **"Aprobar planillas"** externo (`rawMaterialPayRollList.xhtml` → `startNewApprove`
  y `rawMaterialPayRollApprove.xhtml`) se **eliminan**; la aprobación pasa a ser un botón acá.
- **Contabilizar** hoy vive en el Resumen (`RawMaterialPaySummaryReportAction.accountingPeriod`);
  se expone también como botón acá (y se coordina con el estado + el commit de saldo).

### Regla de orden y quincena sin acopio (tu inquietud 3)
- La generación debe ir **en orden de fecha** y contabilizar N **antes** de generar N+1 (el commit
  de saldo de N debe estar hecho para que N+1 lea el saldo correcto). Guard sugerido: no permitir
  generar N+1 si existe una quincena anterior **con acopio** sin contabilizar.
- **Si una quincena entera NO tuvo acopio**: **no se genera planilla** para ella (no hay nada que
  pagar ni que cobrar). **La deuda NO se pierde**: como los movimientos se leen por
  **`saldo>0 AND fecha ≤ fin`** (deuda acumulada, no quincenas consecutivas), la siguiente quincena
  **con** acopio arrastra toda la deuda pendiente automáticamente. O sea: se **saltea** la quincena
  vacía y se continúa con la próxima que tenga acopio, sin efecto sobre la deuda.

## 6. Impacto en el código existente
- **`prepareDiscountsBatch`**: pasa a leer los movimientos que arrastran por **`saldo>0` y
  `fecha<=fin`** (no por rango exacto de la quincena). Comisión banco: igual por `saldo>0` (se
  paga/forgiveea dentro de su quincena).
- **Motor** (`createMapOfProducers` / `generatePayroll`): aplicar el orden y el tope; registrar
  aplicaciones; el líquido nunca negativo.
- **Boleta de pago**: incluir registros con **líquido 0** (hoy filtra `> 0`).
- **Contabilización** (`accountingPeriod`): usa los montos capados (ya reflejan lo cobrado) y es el
  punto donde se **commitea** el saldo. Debe existir el reverso (anular) coordinado.
- **Aprobar** (`approvedPayRoll` / `approvedDiscounts`): se **unifica/obsoleta**; su lógica de
  estados de movimiento se reemplaza por el commit de saldo al contabilizar.
- **Import** (`salaryMovementProducerImport` / `importFromExcel`): al crear cada movimiento →
  `saldo = valor`, estado `PENDIENTE`.
- **Export** (`exportToExcel`): agregar columnas **`saldo`** y **`estado`**.
- **Lista** (`salaryMovementProducerList`): columna Estado → `PENDIENTE`/`PAGADO`; opcional mostrar
  `saldo`. (Filtro por estado, hoy comentado, se puede reactivar.)

## 7. Migración SQL (en `query/query_v6.0.47.sql`, al final)
- `ALTER TABLE movimientosalarioproductor ADD saldo DECIMAL(16,2)`.
- **Corte (confirmado):** todo lo **hasta la 1ra quincena de Mayo 2026 (`fecha ≤ 2026-05-15`)** →
  `saldo = 0`, estado `PAGADO` (histórico saldado). Lo **posterior** (`fecha > 2026-05-15`) →
  `saldo = valor`, estado `PENDIENTE` (deuda viva).
- `CREATE TABLE aplicacion_descuento_productor (...)` + fila en `secuencia`.
- (Estados nuevos `PENDIENTE`/`PAGADO`: valores string en la columna `estado`.)

## 8. Fases de implementación (sugeridas)
1. **Modelo + migración**: `saldo`, estados, tabla `aplicacion_descuento_productor`, SQL.
2. **Motor**: orden/tope/arrastre en el cálculo por productor + registro de aplicaciones.
3. **Contabilizar/Anular**: commit y reverso del saldo coordinados con el asiento (annulVoucher).
   Unificar/obsoletar "Aprobar".
4. **Import/Export/Lista**: saldo/estado.
5. **Boleta**: incluir líquido 0.

## 8b. Estado de implementación (COMPLETO, compila JDK 1.8, sin commit)

**Todas las fases implementadas y compilando.** Resumen de lo entregado:
- **Modelo/SQL**: `saldo` + enum `SalaryMovementProducerState {PENDIENTE,PAGADO}`;
  `StatePayRoll.CONTABILIZADO`; `DiscountApplication` (`aplicacion_descuento_productor`);
  `RawMaterialPayRoll.accountingVoucherId` (col `idcomprobante`). Migración en `query_v6.0.47.sql`
  (corte ≤ 2026-05-15 → PAGADO; tabla aplicaciones; columna idcomprobante).
- **Motor**: `applyCappedDiscounts` (orden + tope + arrastre) + `preloadCarryMovements` + aplicaciones.
- **Contabilización**: servicio `RawMaterialAccountingService.contabilizar(...)` (movido desde el
  Resumen) devuelve el Voucher. Servicio: `commitDiscountDebts`, `revertDiscountDebts`,
  `setPayRollsState`, `setPayRollsVoucherId`, `findAccountingVoucherId`, `hasPriorUncontabilized`.
- **Acción** `RawMaterialPayRollAction`: `aprobar()` (sesiones/reserva/GAB + APPROVED), `contabilizar()`
  (voucher + commit + guardar id + CONTABILIZADO), `revertir()` (annulVoucher + revert + limpiar id +
  PENDING). `deleteAll` solo bloquea si CONTABILIZADO. Guard de orden en `generate()`. Botones por
  estado (`getPeriodState`, `pendingState`/`approvedState`/`accountedState`/`generated`).
- **Vistas**: 5 botones en `rawMaterialPayRoll.xhtml` (Generar/Aprobar/Contabilizar/Revertir/Borrar,
  con confirmación en Contabilizar/Revertir). Eliminados `rawMaterialPayRollApprove.xhtml` + botón del
  list + su navegación. Quitado Contabilizar del Resumen. Lista de descuentos: columna Saldo + estado
  PENDIENTE/PAGADO. Boleta incluye líquido 0. Import setea `saldo=valor`.

**Pendiente de validación en runtime** (yo no despliego): correr `query_v6.0.47.sql`, `ant clean explode`,
y probar el flujo completo con una quincena real (generar → aprobar → contabilizar → revertir).
Nota: `accountingPeriod` quedó como método muerto en el action del Resumen (ya no se llama); se puede
borrar luego.

## 8c. Estado inicial (histórico de la propuesta)

**Hecho y COMPILA (JDK 1.8):**
- Fase 1: `SalaryMovementProducer.saldo` + enum `SalaryMovementProducerState {PENDIENTE,PAGADO}`;
  `StatePayRoll.CONTABILIZADO`; entidad `DiscountApplication` (`aplicacion_descuento_productor`);
  migración en `query_v6.0.47.sql` (corte ≤ 2026-05-15 → PAGADO). Ajustados los `setState`/creaciones.
- Fase 2 (motor): `applyCappedDiscounts` (orden impuestos→comisión→GA→alcohol→movimientos FIFO,
  líquido ≥ 0, arrastre solo movimientos por productor) + `preloadCarryMovements` (saldo>0, fecha≤fin)
  + aplicaciones (cascade en `RawMaterialPayRecord.discountApplications`). Sin `alcoholDiff`.
- Fase 4 (core, servicio): `commitDiscountDebts` (reduce saldo/PAGADO), `revertDiscountDebts`
  (restaura saldo/PENDIENTE), `setPayRollsState(...)`.
- Boleta: `liquidPayable >= 0` (incluye líquido 0).

**Falta (orquestación money-critical + UI):**
- Referencia al asiento: **guardar el id del comprobante (`sf_tmpenc`)** en `planillapagomateriaprima`
  (nueva columna, ej. `idcomprobante`) — set al Contabilizar, se lee/limpia al Revertir.
- **Mover `accountingPeriod`** (armado del Voucher, hoy en `RawMaterialPaySummaryReportAction`) a un
  **servicio** reutilizable (`contabilizar(periodo)` → Voucher; `anular(voucherId)`).
- Acción de generación: `aprobar()` (→APPROVED), `contabilizar()` (voucher + `commitDiscountDebts` +
  guardar id + →CONTABILIZADO), `revertir()` (annulVoucher + `revertDiscountDebts` + limpiar id +
  →PENDING), y `borrar` (ya existe deleteAll; borra aplicaciones por cascade).
- Vista `rawMaterialPayRoll.xhtml`: 5 botones por estado. Quitar `rawMaterialPayRollApprove.xhtml`
  + botón externo. Quitar Contabilizar del Resumen.
- Fase 5: import/export/lista (saldo/estado), guard secuencial (no generar N+1 si N con acopio sin
  contabilizar).

## 9. Decisiones cerradas
1. **Corte de migración**: `fecha ≤ 2026-05-15` → `PAGADO`; posterior → `PENDIENTE` (§7).
2. **Alcohol**: NO arrastra (no es movimiento por productor). Prioridad, se aplica hasta donde
   alcance. Solo movimientos por productor (vet/crédito/concentrados/yogurt/tachos/otros egresos) arrastran.
3. **Guard sequential**: se implementa (no generar N+1 si hay una quincena previa con acopio sin
   contabilizar). Quincena sin acopio se saltea (§5).
4. **Revertir**: nueva acción que orquesta `annulVoucher` (existe) + restaura saldo + reabre la
   planilla (`CONTABILIZADO` → `PENDING`). Botón en la vista de generación.
5. **Aprobar**: se conserva como gate (estado `APPROVED`) previo a Contabilizar; el commit de la
   deuda es al Contabilizar. Se elimina el flujo/vista de aprobación externo.
