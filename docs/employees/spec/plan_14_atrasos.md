# PLAN 14 — Los atrasos: base configurable y política por área

Hoy el descuento por atrasos es **una sola regla escrita en el código**, igual para todos los
clientes y para todas las personas. Este plan la vuelve configurable en dos ejes: **sobre qué
base** se calcula, y **con qué política** se convierte el atraso en días descontados.

## Lo que hay hoy, verificado

**Un solo lugar calcula el importe**: `fillManagersPayroll`
([GeneratedPayrollServiceBean:1249](../../../src/main/com/encens/khipus/service/employees/GeneratedPayrollServiceBean.java)).
De ahí sale `tardinessMinutesDiscount`, y ese número viaja a tres destinos:

- la **planilla de sueldos** (`planillaadministrativos.descuentoporminutosatraso`);
- la **planilla fiscal por categoría**, que lo copia y lo suma a sus descuentos
  ([FiscalPayrollGeneralCalculator:60](../../../src/main/com/encens/khipus/util/employees/payroll/fiscal/FiscalPayrollGeneralCalculator.java));
- un **movimiento de sueldo** de tipo `TARDINESS_MINUTES`
  ([SalaryMovementServiceBean:136](../../../src/main/com/encens/khipus/service/employees/SalaryMovementServiceBean.java)).

Comprobado en la generación 008 de julio: HEREDIA tiene 51,75 en las dos planillas y el mismo
líquido, 2.647,16. **Tocar el cálculo en un solo lugar alcanza**, y hay que verificar que los dos
sigan coincidiendo.

**La regla actual** es una escala por minutos acumulados en el mes, sobre el **total ganado**:

| Atraso del mes | Descuento |
|---|---|
| 0 – 30 | 0 |
| 31 – 60 | ½ día |
| 61 – 90 | 1 día |
| 91 – 120 | 2 días |
| ≥ 121 | 3 días |

El código que calculaba lo mismo sobre el **sueldo básico** quedó comentado al lado, de cuando se
cambió la base. O sea: la discusión de este plan ya ocurrió una vez, y se resolvió por código.

**La planilla de docentes es otra historia**: usa `regladescuento` / `rangoregladescuento`, un
mecanismo **ya configurable** de rangos con importe fijo o porcentaje, con alcance por categoría de
puesto y unidad de negocio ([GeneratedPayrollServiceBean:4363](../../../src/main/com/encens/khipus/service/employees/GeneratedPayrollServiceBean.java)).
Existe desde antes y **la planilla de sueldos no lo usa**.

**El marco legal.** Descontar el tiempo no trabajado no necesita autorización: es pagar lo
trabajado. Todo lo que exceda ese tiempo es una **multa**, y el
[DS 224 de 1943, art. 43](https://www.lexivox.org/norms/BO-DS-224.html) solo la permite si está en
el **reglamento interno aprobado por el Ministerio de Trabajo**. La escala actual —y también la
política de producción de este plan— son multas en ese sentido: descuentan más que el tiempo
perdido. Esto **no bloquea** el desarrollo, pero sí es una condición para usarlo.

## Parte A — La base: total ganado o sueldo básico

### La decisión

Una preferencia **por compañía**, en *Preferencias de compañía → Recursos Humanos*, con dos
valores:

| Valor | El día vale |
|---|---|
| **Total ganado** (actual, por defecto) | `total ganado ÷ 30` |
| **Sueldo básico** | `sueldo básico ÷ 30` |

Por defecto queda en **total ganado**, que es lo que hacen hoy los dos clientes: cambiar el valor
por defecto cambiaría números sin que nadie lo haya pedido.

La diferencia no es teórica. HEREDIA, julio 2026: total ganado 3.105 → el día vale 103,50; sueldo
básico 2.700 → 90,00. El bono de antigüedad es lo que separa los dos números, y con la base en
total ganado **el mismo atraso le cuesta más a quien tiene más años en la empresa**.

### Dónde vive

`configuracion` (esquema de finanzas), la misma tabla que ya guarda las horas del día laboral y las
opciones de aguinaldo. Un campo nuevo, `basedescuentoatraso`, con los valores `TOTAL_INCOME` y
`BASIC_SALARY`.

## Parte B — La política: por área

### El pedido de terdemol, para producción

| Situación | Descuento |
|---|---|
| **4 atrasos de hasta 30 min** acumulados en el mes | 1 día de haber |
| **Un atraso de 31 a 120 min** | ½ día, por cada uno |
| **Un atraso de 121 min o más** | ½ día **+ memorándum**, por cada uno |

Y **no se aplica al personal administrativo**, que sigue con la escala por tramos acumulados.

### Por qué esto no entra en el mecanismo que ya existe

`regladescuento` mapea **minutos acumulados → un importe**. La política de producción necesita otra
cosa: **contar eventos** —cuántos atrasos, y de qué tamaño cada uno—. Un atraso de 45 minutos y
tres de 15 suman los mismos 90 minutos y cuestan distinto. El mecanismo viejo no puede expresarlo,
y forzarlo lo dejaría sirviendo a dos ideas incompatibles.

### La forma

Una **política de atrasos** que recibe la lista de atrasos del mes —un número por día, que el motor
ya calcula— y el valor del día, y devuelve **cuántos días se descuentan** y **qué memorándums hay
que emitir**:

```
atrasos del mes (por día)  +  valor del día  ──►  política  ──►  días descontados
                                                               + memorándums
```

Dos políticas:

- **Por tramos acumulados** (la actual): suma los minutos del mes y busca el tramo.
- **Por evento** (la nueva): clasifica cada atraso en los tres cajones y suma.

La elección se resuelve **por persona**, no por empresa: en terdemol conviven producción y
administración en la misma planilla. El área sale de la **unidad organizacional del puesto**, que
en terdemol ya separa PRODUCCION (168 puestos) de ADMINISTRACION (66), COMERCIAL y GERENCIA.

### Cómo se configura

En *Preferencias de compañía → Recursos Humanos*:

| Preferencia | Para qué |
|---|---|
| Base del descuento por atrasos | total ganado / sueldo básico (Parte A) |
| Política de atrasos por evento | apagada por defecto; encendida, se aplica a las unidades elegidas |
| Unidades organizacionales con política por evento | selección múltiple; en terdemol, PRODUCCION |

Con la política apagada —ILVA y cualquier cliente nuevo— **no cambia absolutamente nada**.

## Las tareas

| # | Tarea | Riesgo |
|---|---|---|
| A1 | Campo `basedescuentoatraso` en `configuracion` + preferencia en la pestaña de RRHH | bajo |
| A2 | El cálculo toma la base de la preferencia, con `TOTAL_INCOME` por defecto | **medio** |
| A3 | Verificar que sueldos y fiscal sigan coincidiendo al centavo | bajo |
| B1 | `LatenessPolicy`: la política por tramos acumulados, tal como está hoy, aislada y con pruebas | **medio** |
| B2 | La política por evento: los tres cajones, con sus reglas de conteo | **alto** |
| B3 | El ámbito: unidades organizacionales con política por evento, en preferencias | bajo |
| B4 | El cálculo elige la política según la unidad del puesto de cada persona | **medio** |
| B5 | Los memorándums: dónde se registran y dónde se ven | **a definir** |
| B6 | El reporte de control muestra en qué cajón cayó cada atraso | bajo |
| B7 | Documentar las dos políticas en `03_calculos_planilla.md` | bajo |

**B2 es la más cara** y es donde están las preguntas: contar eventos obliga a definir qué pasa con
los restos, con los topes y con las combinaciones.

## Las respuestas (2026-09-21)

| # | Pregunta | Respuesta |
|---|---|---|
| 1 | "Sueldo básico", ¿cuál? | **El del contrato**, el mes completo, sin prorratear |
| 2 | Docentes | Queda como está, con su mecanismo de rangos |
| 3 | Qué es "un atraso" | Cualquier día con atraso mayor a cero, ya descontadas tolerancia y permisos |
| 4 | Los chicos | **Equivalencia**: cada 4 son 1 día, 8 son 2. El resto no se arrastra |
| 5 | Los de 31 a 120 | **Medio día por cada uno**, sin tope propio |
| 6 | ¿Se suman las tres? | **Sí** |
| 7 | Tope mensual | No hay uno fijo: el tope son **los días que se pagan** |
| 8 | Memorándum | **Solo reportar**, en la planilla de sueldos. Lo último en hacerse |
| 9 | Base de esta política | **Manda la preferencia** de la Parte A |
| 10 | Desde cuándo | **Fecha de corte**, como el motor de asistencia |
| 11 | Administrativos | Siguen con la escala por minutos acumulados |
| 12 | Salida anticipada | Sigue sin descontarse |

## Preguntas que hay que responder antes de implementar

**Sobre la base (Parte A)**

1. **"Sueldo básico", ¿cuál de los dos?** ¿El sueldo del contrato —2.700 en HEREDIA, el mes
   completo— o el **básico ganado** —2.610, ya proporcional a los días trabajados—? No es lo mismo
   cuando la persona tuvo faltas o entró a mitad de mes.
2. ¿La preferencia alcanza también a la **planilla de docentes**, que hoy usa el mecanismo de
   rangos con importe fijo? Propuesta: dejarla como está y que la preferencia solo toque la planilla
   de sueldos y la fiscal, que comparten el número.

**Sobre la política por evento (Parte B)**

3. **¿Qué cuenta como "un atraso"?** Propuesta: cualquier día cuyo atraso, ya descontada la
   tolerancia de 10 minutos y los permisos por horas, sea mayor a cero. Un día perdonado o una falta
   no cuentan, porque no acumulan atraso.
4. **Los de hasta 30 minutos: ¿cada 4 completos, o uno solo por mes?** Con 8 atrasos chicos,
   ¿son 2 días o 1? Y los que sobran —1, 2 o 3 al cerrar el mes— ¿se pierden o se arrastran al mes
   siguiente? Propuesta: **cada 4 completos, y el resto se pierde**.
5. **Los de 31 a 120 minutos: ½ día por cada uno, ¿sin tope?** Cuatro atrasos de 45 minutos serían
   **2 días**. ¿Es lo que se quiere?
6. **¿Las tres reglas se suman?** Un mes con 4 atrasos chicos y 2 de 45 minutos, ¿son 1 + 1 = 2
   días? Propuesta: sí.
7. **¿Hay tope mensual?** Por ejemplo, que los atrasos nunca descuenten más de N días. Hoy no lo
   hay, y la escala vieja llega a 3 días.
8. **El memorándum: ¿qué es dentro del sistema?** Tres alcances posibles, de menor a mayor:
   (a) una marca en el reporte y una lista de "memorándums a emitir" para RRHH;
   (b) además, un registro con fecha, motivo y quién lo emitió, que queda en el legajo;
   (c) además, un documento imprimible con el texto del memorándum.
   Propuesta: empezar por (a), y (b) si hace falta dejar constancia.
9. **La base de esta política: ¿"1 día de haber" es siempre el sueldo básico**, o sigue la
   preferencia de la Parte A? El pedido dice "haber", que en la norma es el salario.
10. **¿Desde cuándo aplica?** Propuesta: una fecha de corte, como hicimos con el motor de
    asistencia, para que regenerar un mes viejo reproduzca lo que se pagó.

**Sobre el alcance**

11. ¿El **administrativo de terdemol** se queda con la escala por tramos actual, sin cambios?
12. ¿La **salida anticipada** sigue sin descontarse, como hoy? Si entra en alguna de las dos
    políticas, es otra decisión y cambia números.

## Riesgos

**Esto cambia sueldos.** Cualquiera de las dos partes mueve el líquido de personas reales. La
verificación tiene que ser la misma que usamos en el plan 12: generar en paralelo y comparar
persona por persona contra la planilla anterior, explicando cada diferencia.

**La política por evento es más dura que la actual en los casos chicos.** Cuatro atrasos de 10
minutos suman 40 minutos: con la escala actual son ½ día; con la nueva, 1 día. Conviene calcular el
impacto sobre julio antes de decidir la fecha de corte.

**El respaldo legal.** Con la política por evento, un atraso de 31 minutos descuenta 4 horas de
salario. Eso es una multa y necesita el reglamento interno aprobado. Antes de encenderla en
producción hay que confirmar que ese reglamento existe y dice esto.

## Cómo se verifica

1. Con la preferencia en **total ganado** y la política por evento apagada, regenerar julio de
   terdemol: **todos los números tienen que ser idénticos** a la generación 008. Es la prueba de que
   el refactor no cambió nada.
2. Cambiar la base a **sueldo básico** y regenerar: solo debe moverse el descuento por atrasos de
   quienes tienen bono de antigüedad u otros ingresos. HEREDIA: 51,75 → 45,00.
3. Encender la política por evento para PRODUCCION y regenerar: comparar persona por persona,
   con el detalle de en qué cajón cayó cada atraso.
4. Los administrativos no se mueven en el paso 3.
5. Sueldos y fiscal siguen coincidiendo al centavo en los tres pasos.

## Estado

| Etapa SDD | Estado |
|---|---|
| SPEC | Este documento lo incluye |
| PLAN | Aprobado, con las respuestas de arriba |
| **IMPLEMENT** | **A1–A3, B1–B7 hechas** · SQL aplicado en dev y desplegado · falta el SQL en producción y probar |

### Qué quedó hecho

- La preferencia **Base del descuento por atrasos** y la fecha **Atrasos por evento desde**, en la
  pestaña de Recursos Humanos de Preferencias de compañía.
- La marca **Atrasos por evento** en la unidad organizacional, que se hereda a sus sub-áreas.
- [LatenessPolicy](../../../src/main/com/encens/khipus/util/employees/payroll/LatenessPolicy.java):
  las dos políticas, sin nada de la escala escrito en el cálculo de la planilla.
- La columna **MEMORANDUMS** en la planilla de sueldos.
- SQL: sección 1 de `query_v6.1.1_terdemol.sql` — archivo nuevo, porque la 6.1.0 ya está desplegada.

### Lo verificado antes de entregar

- Las dos políticas, ejecutando las clases con los casos definidos: 4 chicos = 1 día, 8 = 2, 9 = 2,
  uno de 31 = medio, cuatro de 45 = 2, uno de 121 = medio + memorándum.
- **La política de siempre reproduce exactamente la generación 008** de julio: los ocho que tienen
  atraso dan el mismo importe al centavo. Con la configuración por defecto no cambia nada.
- **Simulación del impacto** en producción, julio 2026, con los atrasos reales:

| | Hoy | Por evento, total ganado | Por evento, sueldo básico |
|---|---|---|---|
| Total del mes | 601,44 | **507,03** | **545,83** |

  Baja porque casi todos tienen uno, dos o tres atrasos chicos, que por evento **no llegan a los
  cuatro** y no descuentan nada. Sube para quien acumula muchos: BRAÑEZ, con 5 chicos y uno de 41
  minutos, pasa de 275,00 a 137,50 —también baja—. En julio **nadie** llegó a 121 minutos en un
  día, así que no hay memorándums.

### Falta probar

- Aplicar el SQL en producción: sin las columnas nuevas, Hibernate no valida el modelo. En dev ya está.
- Marcar PRODUCCION, poner la fecha, regenerar julio y comparar contra la 008.
- Cambiar la base a sueldo básico y verificar que solo se muevan quienes tienen bono u otros ingresos.
- Que la planilla fiscal siga coincidiendo al centavo con la de sueldos.
