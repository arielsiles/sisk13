# Deuda técnica y trampas conocidas

Lo que hay que saber **antes** de tocar el módulo. Nada de esto está roto al punto de
impedir operar: son cosas que sorprenden, que hacen que un cambio "obvio" cambie importes,
o que hay que arreglar con confirmación del cliente porque tocan plata.

## 🔴 Cédulas de identidad hardcodeadas en el cálculo de AFP

[RetentionAFPCalculator](../../src/main/com/encens/khipus/util/employees/payroll/tributary/RetentionAFPCalculator.java)
y [PatronalAFPRetentionCalculator](../../src/main/com/encens/khipus/util/employees/payroll/tributary/PatronalAFPRetentionCalculator.java)
contienen:

```java
/** todo AFP **/
if (empleado.getIdNumber().equals("815059"))  { setLaborCommonRiskAFP(ZERO); }
if (empleado.getIdNumber().equals("2862262")) { /* Juana Pozo   */ setLaborIndividualAFP(ZERO); setLaborCommonRiskAFP(ZERO); }
if (empleado.getIdNumber().equals("2868139")) { /* Eliseo Camacho */ setLaborIndividualAFP(ZERO); setLaborCommonRiskAFP(ZERO); }
if (empleado.getIdNumber().equals("921886"))  { setLaborCommonRiskAFP(ZERO); }
```

Cuatro personas concretas quedan exentas de AFP individual y/o riesgo común. Vino del
commit `b4eb44d1` *"cambios para ILVA - Juana AFP"* (07/05/2024).

**Consecuencias:** el cálculo depende del cliente y del CI; si esas personas se retiran o
si el mismo CI aparece en otra instalación, el resultado es incorrecto. En
`PatronalAFPRetentionCalculator` el bloque además es **inútil**: pisa campos *laborales*
que la calculadora patronal no vuelve a usar y que `RetentionAFPCalculator` ya dejó en cero.

**Qué debería hacerse:** una bandera por empleado o contrato (tipo `flagafp`, que ya
existe) o un tipo de exención en `Contract`. Requiere confirmar con el cliente el criterio
real (¿jubilados? ¿consultores?) antes de migrar los datos.

## 🔴 El módulo tributario de RC-IVA está puenteado

Con `tasaiva = 0` y `formfactura` vacía, toda la cadena RC-IVA da cero y lo que se retiene
es el movimiento de sueldo tipo `RCIVA` cargado a mano. El código lo admite:

```java
/** Revisar, nuevo provisional hasta corregir modulo Tributario RCIVA **/
```

Detalle en [03_calculos_planilla.md](03_calculos_planilla.md). Reactivar el cálculo
automático implica: cargar la tasa IVA real, habilitar la captura de formularios 110, y
decidir qué pasa con los RC-IVA históricos cargados a mano.

## 🟠 `otherIncomes` se suma dos veces

[OtherIncomesCalculator](../../src/main/com/encens/khipus/util/employees/payroll/tributary/OtherIncomesCalculator.java)
incluye `instance.getOtherIncomes()` dos veces en el mismo `sum(...)` — el propio código lo
marca con `//???`. Hoy no se nota porque cuando `activeForTaxPayrollGeneration = true` los
movimientos `OTHER_INCOME` no se leen y el valor es 0. **Si alguna vez se habilita esa
lectura, los ingresos se duplican.**

## 🟠 Divergencia silenciosa entre planilla de sueldos y planilla fiscal

`fillManagersPayroll` reconcilia los dos líquidos sólo si difieren en ≤ 5 centavos:

```java
if (Math.abs(managersPayroll.getLiquid() - categoryFiscalPayroll.getLiquidPayment()) <= 0.05)
    managersPayroll.setLiquid(categoryFiscalPayroll.getLiquidPayment());
```

Una diferencia mayor **no se corrige ni se avisa**. Los dos documentos quedan
descuadrados. Vale la pena agregar una advertencia visible.

## 🟠 El descuento por ausencias del reporte de control siempre sale en cero

`perMinuteSalary` se declara `double perMinuteSalary = 0;` en
`executeAttendanceControlManagers` y **nunca se asigna**. Todos los importes en bolivianos
de `reportecontrol` (`sueldoporbanda`, `importedescuento`, `importeminutostrabajo`) salen
en cero. Los minutos sí son correctos.

Lo mismo con `perMinuteDiscount` en `fillManagersPayroll`:

```java
Double pricePerPeriod = 0.0;               // nunca se asigna
perMinuteDiscount += ((minutes - performanceMinutes) * pricePerPeriod);   // siempre 0
```

y `pricePerMinute` se calcula y no se usa. El descuento por rendimiento por minuto está
efectivamente desactivado.

## 🟠 `verifyDocumentAmountValue` devuelve siempre `true`

[RetentionValidatorServiceBean](../../src/main/com/encens/khipus/service/employees/RetentionValidatorServiceBean.java):

```java
private boolean verifyDocumentAmountValue(List<DischargeDocument> documents, BigDecimal amount) {
    for (DischargeDocument document : documents) {
        if (document.getAmount().compareTo(amount) == 0) return true;
    }
    return true;      // ← el else también devuelve true
}
```

La comparación de montos no hace nada: basta con que exista un documento de descargo para
que se desactive la retención del 15,5 %.

## 🟠 Un empleado sin banda horaria tumba la planilla entera

`fillManagersPayroll` hace `return PayrollGenerationResult.WITHOUT_BANDS` al primer
empleado sin `HoraryBandContract` vigente, y `fillPayroll` borra la corrida completa. Igual
con `WITHOUT_CONTRACTS`. El mensaje nombra al empleado, pero hay que corregirlo y volver a
generar desde cero. Es la causa más frecuente de "no me genera la planilla".

Una mejora natural: acumular todos los empleados problemáticos y reportarlos juntos.

## 🟠 La resta de días sin goce de haber puede dejar ausencias negativas

```java
dayAbsences = dayAbsences * 2;
dayAbsences = dayAbsences - specialDateUnpaidList.size();
```

Se resta 1 por cada día `UNPAID` del rango **sin verificar** que el control de asistencia
haya generado esa ausencia. Si el empleado marcó normalmente un día declarado sin goce de
haber, `dayAbsences` queda negativo y se le pagan más de 30 días. Ver
[03_calculos_planilla.md](03_calculos_planilla.md).

## 🟡 `matchGeneratedSalaryMovement` comentado

En `GeneratedPayrollServiceBean.update()`, al pasar a OFICIAL:

```java
/** 23/08/2018 Planilla OFICIAL, comentado para obviar error **/
//salaryMovementService.matchGeneratedSalaryMovement(generatedPayroll4Operations, genericPayrollList);
```

Los movimientos de sueldo no se marcan como consumidos al oficializar. En la práctica no
molesta porque están atados a una `GestionPayroll` concreta, pero el rastro de "ya se
aplicó" no existe.

## 🟡 Nivel organizacional buscado por nombre literal

```java
OrganizationalLevel areaLevel = findOrganizationalLevelByName("AREA");
```

Si el nivel no se llama exactamente `AREA`, la columna *Área* queda vacía y los reportes
"por área" no agrupan. Debería ser una bandera en `OrganizationalLevel` o un parámetro de
configuración.

## 🟡 Fechas con la API obsoleta de `java.util.Date`

`findInitEndRHMarks` y el control de atrasos usan `Date.getHours()`, `getMinutes()`,
`getYear()`, `new Date(year-1900, month, day, h, m, s)`. Las marcas se comparan **sólo por
hora**, imponiéndoles el día de la iteración: una banda que cruza medianoche no se maneja
bien. Al migrar a `Calendar`/`joda` hay que revalidar contra planillas históricas.

## 🟡 `System.out.println` en producción

80 llamadas en `service/employees` + `action/employees` + `util/employees`, 15 sólo en
`GeneratedPayrollServiceBean` (imprime nombres de empleados e importes en cada generación).
Van al log del servidor sin nivel ni filtro. Deberían ser `log.debug`.

## 🟡 `catch (Exception e) { }` vacíos

`EmployeeServiceBean` (10 ocurrencias: líneas 118, 157, 168, 184, 219, 248, 262, 298, 344, 364) y
`SpecialDateServiceBean` (todas sus consultas) tragan cualquier
excepción y devuelven lista vacía. Un error de consulta se vuelve indistinguible de "no hay
datos" — y en la generación de planilla, "no hay empleados" o "no hay fechas especiales"
cambia importes en silencio.

## 🟡 Transacciones largas y timeouts calculados

`fillPayroll` pagina de 50 en 50 y fija `userTransaction.setTransactionTimeout(120 * pageSize)`
= 6.000 s por página. `fillChristmasPayroll` usa `120 * cantidadEmpleados` en **una sola
transacción**. Con un padrón grande esto se acerca a los límites del JBoss.

En `fillChristmasPayroll` hay además un `em.merge(gestionPayroll)` + `em.refresh(generatedPayroll)`
**dentro del bucle de empleados** — un ida y vuelta a la base por empleado.

## 🟡 Clases y métodos muertos

| Elemento | Estado |
|---|---|
| `PersonalIdentifierCalculator` (fiscal) | `execute()` vacío, no se registra en el generador |
| `OtherDiscountsCalculator` | versión real comentada; hoy sólo copia un campo |
| `TotalDiscountCalculator` | versión original comentada arriba de la vigente |
| `cleanDuplicate(...)` | usa una clase anónima que sobrescribe `contains` para deduplicar — frágil |
| `AFPRateType.LABOR_CONTRIBUTION` (12,71 %) | se carga en el ciclo pero ya **no se usa** para calcular la retención |
| `fillProffesorsPayroll`, `fillFiscalProfessorPayroll`, `executeAttendanceControl(Proffesors\|FiscalProffesors)` | ~1.200 líneas de código académico sin uso |

## 🟡 Duplicación `RHMark` / `RH_Mark`

Dos entidades para lo mismo: `RHMark` mapea la **vista** `vmarcado` (lectura) y `RH_Mark` la
tabla `rh_marcado` (escritura). `RHMark` conserva comentado el `@Table` a `rhmarcado`. Hay
tres actions (`RHMarkAction`, `RH_MarkAction`, `RegisterMarkAction`) con responsabilidades
solapadas.

## Código académico: cuánto pesa

| Área | Clases aprox. | Filas en la réplica |
|---|---|---|
| Postulantes | 6 | 0 |
| Encuestas y evaluaciones | ~25 | 0 |
| Planillas docentes (`GeneralPayroll`, `FiscalProfessorPayroll`) | ~15 + ~1.200 líneas en el service | 0 |
| Estructura académica (carrera, facultad, asignatura) | ~12 | — |

**No borrarlo a la ligera:** `GeneratedPayroll` tiene `cascade = ALL` hacia
`GeneralPayroll` y `FiscalProfessorPayroll`, el menú y `pages.xml` los referencian, y hay
permisos sembrados. Una limpieza tiene que ser un trabajo propio, verificado cliente por
cliente.

## Checklist antes de tocar la generación de planillas

1. **Regenerar una planilla histórica antes y después** del cambio y comparar importe por
   importe. Es la única red de seguridad: no hay tests del módulo.
2. Verificar el efecto en **los dos** documentos (`ManagersPayroll` y `CategoryFiscalPayroll`):
   usan criterios distintos para las ausencias.
3. Si se agrega una columna al `PayrollGenerator`, cuidar **el orden**: cada calculadora lee
   lo que dejaron las anteriores.
4. Si se toca el merge, recordar que `sum`/`avg`/`max` se resuelven por **reflexión sobre el
   nombre de la propiedad**: renombrar un campo de la entidad rompe el merge en silencio.
5. Compilar con **JDK 1.8** (`ant compile`). No desplegar sin autorización: `ant explode` /
   `deploy` reinicia el JBoss del usuario.
6. SQL nuevo: acumular en el `query/*.sql` de la versión en curso, una línea de comentario.
