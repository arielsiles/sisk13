# PLAN 06 — Movimientos entre grupos

> Nace de dos casos reales que el modelo soportaba pero la pantalla no dejaba hacer.

## Los dos casos

**1 · Préstamo por emergencia.** Producción mueve gente de un grupo a otro por unos días y
después vuelve.

**2 · Reorganización.** Se rearman los grupos y varias personas cambian de grupo de forma
permanente.

## Qué había antes

La tabla `grupotrabajomiembro` ya guardaba `fecha inicio` y `fecha fin`, así que **una estadía
acotada era representable**. Lo que no dejaba era la pantalla:

| | Qué pasaba |
|---|---|
| La validación | `findOverlapping` rechazaba **cualquier** solapamiento: no se podía tener base en un grupo y préstamo en otro |
| *Sacar del grupo* | ponía la fecha de **hoy**, fija. No se podía cerrar en una fecha pasada ni futura |
| *Agregar varios* | mostraba solo a quien **no estaba en ningún grupo**, así que una reorganización exigía sacar a cada uno a mano primero |

Y `findMembership` buscaba "la pertenencia que cubre esa fecha" **sin ningún orden**: si algún
día hubiera dos, devolvía cualquiera.

## Cómo se resolvió

### El préstamo no reescribe la asignación base

Es lo que hacen los sistemas de workforce management —*labor transfer* en UKG y Kronos,
asignación organizativa temporal en SAP—: la pertenencia base queda intacta y encima se pone un
préstamo que la pisa solo en su rango.

`grupotrabajomiembro` gana `tipo` (`BASE` / `LOAN`) y `motivo`. Las reglas:

- **Un préstamo exige fecha de fin.** Sin fecha de fin no es un préstamo: es un cambio de grupo.
- La validación de solapamiento **solo compara pertenencias del mismo tipo**: un préstamo puede
  pisar a la base —es su razón de ser— pero dos préstamos no pueden pisarse entre sí.
- `findMembership` **prefiere el préstamo**, y lo hace con dos consultas explícitas en lugar de
  confiar en el orden en que la base devuelva las filas.

La ventaja sobre mover la pertenencia es que **se vence solo**: el día siguiente al plazo la
persona vuelve a su grupo sin que nadie tenga que acordarse de devolverla.

El resolutor **no ganó una capa**: sigue teniendo cuatro. Lo único que cambió es cómo se responde
"¿en qué grupo estaba esta persona ese día?", que es donde el problema vivía.

### Sacar del grupo con la fecha que corresponda

*Sacar del grupo* abre un modal y pide el último día. Y si el corte cae en la fecha de inicio o
antes, **borra en lugar de cerrar**: cerrar dejaría una vigencia que termina antes de empezar,
que estorba y no se puede sacar. Cerrar es para historia real; deshacer un alta recién hecha no.

### Mover varios de una vez

*Agregar varios* ahora lista a **todos** los que tenían contrato vigente en la fecha, con una
columna **Grupo actual**. Al confirmar, a quien venga de otro grupo se le cierra esa pertenencia
el día anterior y se le abre la nueva.

Una reorganización de treinta personas pasa de sesenta clics a uno, y como cada movimiento queda
con su fecha, la historia se reconstruye sola.

### El período cerrado bloquea

Prestar, sacar o mover con una fecha dentro de un período ya cerrado por planilla oficial se
rechaza: cambiaría cómo se calculó algo ya pagado.

### La verificación dice de qué grupo

La columna *Sale de* muestra el nombre del grupo, resaltado cuando ese día la persona estaba
prestada. Sin eso, un día prestado se vería idéntico a uno normal y no habría forma de explicarle
a alguien por qué se lo evaluó contra otro horario.

## Lo que este plan NO hace

- No calcula nada distinto por estar prestado: el pago sigue siendo el del cronograma que le tocó.
- No avisa cuando un préstamo está por vencer.

## Estado

| Etapa SDD | Estado |
|---|---|
| **PLAN** | Aprobado |
| IMPLEMENT | M1 a M5 implementados; sección 19 del SQL aplicada. **Falta probar** |
