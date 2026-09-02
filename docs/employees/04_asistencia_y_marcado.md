# Asistencia y marcado

Cómo se convierte el marcado biométrico en atrasos y faltas dentro de la planilla.

## Piezas

```
rh_marcado  ──vista──►  vmarcado  ──►  RHMark        (marcas crudas)
                          cruce por empleado.codigomarcacion = rh_marcado.marperid

Contract ──► JobContract ──► HoraryBandContract ──┬── HoraryBand   (hora inicio/fin, días)
                                                  ├── Tolerance    (minutos perdonados)
                                                  └── Limit        (ventana para asociar marcas)

SpecialDate (feriado / permiso)  a nivel  BusinessUnit | OrganizationalUnit | Employee
```

Un **día** puede tener varias **bandas** (típico: mañana y tarde). Cada banda espera dos
marcas: entrada y salida.

## Tolerancia vs. límite

Los dos son minutos alrededor de la banda, pero hacen cosas distintas:

| | `Tolerance` | `Limit` |
|---|---|---|
| Campos | `beforeInit`, `afterInit`, `beforeEnd`, `afterEnd` | `beforeInit`, `afterEnd` |
| Para qué | decidir si una marca **cuenta como atraso** | decidir si una marca **pertenece a la banda** |
| Efecto | dentro de la tolerancia → 0 minutos de atraso | fuera del límite → la marca se ignora |

## Asociación de marcas a la banda

`findInitEndRHMarks(dateRhMarkList, horaryBandContract, día)` —
[GeneratedPayrollServiceBean:3048](../../src/main/com/encens/khipus/service/employees/GeneratedPayrollServiceBean.java)

```
si hay menos de 2 marcas en el día → devuelve lista vacía  (⇒ falta de banda)

ventanaEntrada = [ horaInicioBanda − limit.beforeInit , horaFinBanda )
ventanaSalida  = ( horaInicioBanda , horaFinBanda + limit.afterEnd ]

marcaEntrada = la marca dentro de ventanaEntrada MÁS CERCANA al inicio de banda
marcaSalida  = la marca dentro de ventanaSalida  MÁS CERCANA al fin de banda,
               distinta de la marca de entrada

devuelve [entrada, salida] sólo si encontró AMBAS; si no, lista vacía
```

Consecuencias prácticas:

- Con **una sola marca en todo el día** no se asocia nada → falta de banda completa, aunque
  la persona haya trabajado.
- Las marcas se comparan **sólo por hora**: se les impone el año/mes/día de la iteración.
  Una banda nocturna que cruza medianoche no se maneja bien.
- La marca de entrada nunca puede ser también la de salida.

## Recorrido del mes

`executeAttendanceControlManagers(...)` —
[GeneratedPayrollServiceBean:1929](../../src/main/com/encens/khipus/service/employees/GeneratedPayrollServiceBean.java)

```
para cada día del rango [initDate, endDate]:
    si es DOMINGO (Calendar.DAY_OF_WEEK == 1) → se salta entero
    hasPermission4Today = el día está en alguna fecha especial CON goce de haber
                          (unidad de negocio | unidad organizacional | empleado)
    bandas = HoraryBandContract vigentes ese día, agrupadas por día de la semana

    para cada banda del día:
        marcas = findInitEndRHMarks(...)
        bandDuration = minutos entre inicio y fin de banda
        hasPermission4BandInterval = permiso por franja horaria que cubre esta banda

        si NO hay permiso para el día:
            si employee.controlFlag y marcas.size() < 2:      → FALTA DE BANDA
                 (salvo permiso de franja) acumula bandDuration como minutos de ausencia
            si employee.controlFlag y marcas.size() >= 1:     → ATRASOS
                 compara entrada vs inicio de banda y salida vs fin de banda
                 fuera de tolerancia → suma la diferencia en minutos
        registra un ControlReport de esa banda/día
```

`employee.controlFlag = false` desactiva por completo faltas y atrasos: el empleado cobra
30 días sin importar sus marcas.

### De faltas de banda a días de ausencia

Con `bandAbsences` = bandas faltantes del día y `bandsNumber` = total de bandas del día, al
procesar la **última** banda:

```
si 0 < bandAbsences < bandsNumber          → dayAbsences += 0,5
si bandAbsences == bandsNumber:
      si bandsNumber == 1:
            varón:  bandDuration ≥ 8 h → +1   ; si no → +0,5
            mujer:  bandDuration ≥ 7 h → +1   ; si no → +0,5
      si bandsNumber > 1                   → dayAbsences += 1
```

Ese `dayAbsences` es el que después se multiplica por 2 en `fillManagersPayroll`
(ver [03_calculos_planilla.md](03_calculos_planilla.md)).

### Atrasos

Para entrada y para salida, por separado:

```
si la marca NO está dentro de [banda − tolerance.before , banda + tolerance.after]:
     diferencia = |marca − horaBanda|  en minutos
     tolerancia aplicable = before si la marca es anterior, after si es posterior
     si diferencia > tolerancia  → suma diferencia a los minutos de atraso del día
```

Se acumulan por día → por contrato → por mes, y el total mensual entra en la tabla de
tramos de descuento por atrasos.

> El código **también acumula atrasos por la marca de salida**, no sólo por llegar tarde.
> Salir fuera de la tolerancia cuenta igual que llegar tarde.

### Rotaciones

Si alguna banda del contrato tiene `TypeHoraryBand` (`idtipobandahoraria` no nulo), se usa
`executeAttendanceControlManagersRotation(...)` en lugar del recorrido normal, y **sólo con
esa banda**: `getHasHorariEspecial()` devuelve la primera que encuentra y las demás del
contrato se ignoran. Es el soporte de turnos rotativos.

## Fechas especiales

[SpecialDate](../../src/main/com/encens/khipus/model/employees/SpecialDate.java) → `fechaespecial`

| Dimensión | Valores |
|---|---|
| Destino (`destino`) | `BUSINESSUNIT` (feriado general), `ORGANIZATIONALUNIT` (área), `EMPLOYEE` (permiso personal) |
| Goce de haber (`gocedehaber`) | `PAID` / `UNPAID` |
| Alcance (`ALLDAY`) | día completo, o franja `horainicio`–`horafin` |

**Todas** las consultas de
[SpecialDateServiceBean](../../src/main/com/encens/khipus/service/employees/SpecialDateServiceBean.java)
filtran `credit = PAID`, salvo `getSpecialDateRangeUnpaid`. Por eso un permiso `UNPAID`
**no exime** de la falta: genera ausencia y luego se compensa restando un día (ver la tabla
del ×2 en [03_calculos_planilla.md](03_calculos_planilla.md)).

Las vacaciones aprobadas crean automáticamente una `SpecialDate` asociada
(`VacationServiceBean.createSpecialDate`): es la vía por la que una vacación llega a la
planilla.

Pantalla: **RRHH → Fechas especiales** ([specialDateList.xhtml](../../view/employees/specialDateList.xhtml)),
permiso `SPECIALDATE`. Reporte: `specialDateReportAction`.

## Reporte de control (`reportecontrol`)

Una fila **por empleado, día y banda** en cada corrida de planilla. Es la tabla más grande
del módulo (897.121 filas en la réplica) y se regenera completa en cada prueba de planilla.

| Columna | Contenido |
|---|---|
| `idbandahorariac`, `idplanillagenerada`, `fecha` | clave del registro |
| `marcinicio` / `marcfin` | marcas asociadas a la banda |
| `marcaciones` | **todas** las marcas del día, en texto `dd/MM/yyyy HH:mm \| …` |
| `mindescuento` | minutos de atraso de esa banda |
| `faltabanda` | minutos de ausencia de banda |
| `numerofaltabandas` | bandas faltantes acumuladas en el día |
| `minutostrabajados` | `bandDuration − minutos de atraso` |
| `sueldoporbanda`, `importedescuento`, `importeminutostrabajo` | importes derivados de `perMinuteSalary` |
| `tipocontrol` | `employee.controlFlag` como 0/1 |

> `perMinuteSalary` se inicializa en `0` y **nunca se asigna** en
> `executeAttendanceControlManagers`. Todos los importes en bolivianos del reporte de
> control salen en cero: la tabla sirve como bitácora de minutos, no de dinero.

Se consulta desde la lista de planillas generadas → *Ver → Reporte de control*
([controlReportList.xhtml](../../view/employees/controlReportList.xhtml)) y se exporta con
`controlReportReportAction`.

## Pantallas relacionadas

| Pantalla | Action | Permiso |
|---|---|---|
| Marcaciones | `RHMarkAction`, `RH_MarkAction`, `RegisterMarkAction` | `RHMARK` |
| Bandas horarias por contrato | `HoraryBandContractAction` | `HORARYBANDCONTRACT` |
| Cambios de banda horaria | `HoraryBandChangesAction` | `HORARYBANDCONTRACT` |
| Tolerancias | `ToleranceAction` | `TOLERANCE` |
| Límites | `LimitAction` | `LIMIT` |
| Reporte de estados de marcado | `MarkStateReportAction` | — |
| Reporte de atrasos / faltas | `LatenessViewAction`, `MissingViewAction` | — |
