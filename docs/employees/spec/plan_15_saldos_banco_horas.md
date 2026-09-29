# PLAN 15 — Los saldos del banco de horas: un saldo que no dependa del filtro

La pestaña **Saldos** del banco de horas contesta hoy una pregunta distinta de la que anuncia.
Dice *saldo* y calcula el **neto del período**, así que el mismo trabajador tiene un saldo
distinto según las fechas que se escriban arriba.

## El problema, visto en pantalla

JHON BRAYAN ACHOCALLA, la misma persona, el mismo día, sin registrar nada entre una consulta y la
otra:

| Rango consultado | ACUMULADO | USADO | PAGADO | "SALDO EN HORAS" |
|---|---|---|---|---|
| 01/07/2026 – 31/07/2026 | 4,00 | 24,00 | 24,00 | **−44,00** |
| 01/01/2025 – 31/07/2026 | 44,00 | 24,00 | 24,00 | **−4,00** |

Debe 4 horas. Quien abra la pantalla con el mes en curso va a leer que debe 44.

Y hay un segundo efecto, peor porque es silencioso: la lista solo trae a **quien tuvo movimientos
dentro del rango**. Alguien con horas a favor del año pasado y ningún movimiento este mes
**no aparece**. El saldo existe, la empresa lo debe, y la pantalla no lo muestra.

La causa está en `HourBankBalance.getBalance()`, que suma solo lo que la consulta trajo:

```java
return accrued.subtract(used).subtract(paid);
```

Eso es correcto como **flujo del período**. No es un saldo.

## La decisión

Un saldo es acumulado desde siempre: **no puede depender del filtro**. El filtro decide qué
movimiento se muestra en detalle, no cuánto tiene la persona.

- **Se va el campo Hasta.** Queda un solo campo, **Desde**, que responde "muéstrame el movimiento
  a partir de acá".
- **Entra una columna SALDO ANTERIOR**: todo lo que pasó antes de esa fecha, comprimido en un
  número.
- **SALDO EN HORAS pasa a ser el saldo real**, el de todos los movimientos, y no se mueve aunque
  se cambie la fecha.

La fila cuadra sola, que es lo que corta las preguntas:

```
SALDO ANTERIOR + ACUMULADO − USADO − PAGADO = SALDO
```

### Cómo queda

```
Desde [01/01/2026]  [Buscar]

Persona                 SALDO ANTERIOR   ACUMULADO   USADO   PAGADO   SALDO EN HORAS   SALDO EN DÍAS
JHON BRAYAN ACHOCALLA            16,00        4,00   24,00    24,00           -4,00           -0,50
MARIA QUISPE                     12,00        0,00    0,00     0,00           12,00            1,50
                                                                        -------------
                                                                TOTAL            8,00
```

Y lo mismo consultado desde el 01/01/2025:

```
Persona                 SALDO ANTERIOR   ACUMULADO   USADO   PAGADO   SALDO EN HORAS   SALDO EN DÍAS
JHON BRAYAN ACHOCALLA             0,00       44,00   24,00    24,00           -4,00           -0,50
```

Cambian las columnas del medio, que es lo que se preguntó. **El saldo no.**

### Las reglas que quedan fijadas

1. **Quién aparece**: quien tenga movimientos desde la fecha **o** saldo anterior distinto de
   cero. El segundo caso es el que hoy se pierde: se muestra con su SALDO ANTERIOR, el resto de
   las columnas en cero y el saldo real que le corresponde.
2. **El rojo** lo decide el **saldo real**, no el neto del período. Hoy ACHOCALLA se pinta en rojo
   con un rango y en rojo con el otro por razones distintas; con esto, se pinta cuando debe horas.
3. **La fecha inicial** de la pantalla sigue siendo el **1.º de enero del año en curso**, como
   hoy. Ya no hace falta que abarque la historia: lo anterior está en SALDO ANTERIOR.
4. **El total al pie** suma los saldos: cuántas horas debe la empresa en total. Va **solo en
   horas**. En días no, a propósito: cada persona podría tener una jornada distinta y sumar días
   de jornadas distintas da un número que no significa nada. El saldo en días sigue existiendo
   por fila, con la regla de 8 h que ya se decidió.
5. **Los movimientos con fecha futura entran en el saldo.** Si alguien carga un permiso del mes
   que viene, el saldo lo refleja. Prefiero que se vea a que se esconda.

## Las tareas

### S1 — Las consultas

`HourBankMovement.balancesInRange` pide `from` y `to`. Se reemplaza por dos:

```java
@NamedQuery(name = "HourBankMovement.balancesFrom",
        query = "select o.contract, o.type, sum(o.hours) from HourBankMovement o"
                + " where o.date >= :from group by o.contract, o.type"),
@NamedQuery(name = "HourBankMovement.balanceBefore",
        query = "select o.contract, sum(o.hours) from HourBankMovement o"
                + " where o.date < :from group by o.contract")
```

La segunda es la que trae el saldo anterior **de todos a la vez**: preguntarlo persona por persona
con `balanceAtDate` sería una consulta por fila.

`sumByTypeInRange` queda sin usar desde que existe esta pantalla —no lo llama nadie— y se
elimina en el mismo movimiento.

### S2 — El DTO

`HourBankBalance` gana `previous` y `getBalance()` pasa a incluirlo:

```java
return previous.add(accrued).subtract(used).subtract(paid);
```

`isNegative()` y `getBalanceInDays()` cuelgan de `getBalance()`, así que el rojo y los días quedan
sobre el saldo real sin tocarlos.

### S3 — El servicio

`balancesBetween(from, to)` pasa a `balancesFrom(from)`: ejecuta las dos consultas y arma **una
sola fila por contrato**, con `previous` viniendo de la segunda. Un contrato que aparece solo en
la de saldo anterior también genera fila, con el período en cero — es la regla 1.

### S4 — El action

- Se van el campo `to`, su getter y su setter.
- `init()` deja `from` en el 1.º de enero y ya no calcula el 31 de diciembre.
- `getBalances()` llama al método nuevo.
- Entra `getTotalBalance()`, la suma de los saldos de la lista.

### S5 — La pantalla

`view/employees/hourBank.xhtml`, pestaña de saldos:

- se quita el campo **Hasta** y su calendario;
- entra la columna **SALDO ANTERIOR** entre Persona y ACUMULADO;
- entra la fila de total al pie de la tabla.

### S6 — Los textos

Dos claves nuevas en `messages_app.properties`, con el criterio de esta pantalla —la ayuda corta
es el nombre de la columna y la explicación va en el `title`—:

```
HourBank.previousBalance = SALDO ANTERIOR
HourBank.total = TOTAL
```

### S7 — La verificación

Con los datos que ya están cargados en dev, ACHOCALLA (contrato 246), **comprobado en la base**:

| Desde | ANTERIOR | ACUM | USADO | PAGADO | SALDO |
|---|---|---|---|---|---|
| 01/01/2025 | 0,00 | 44,00 | 24,00 | 0,00 | **20,00** |
| 01/01/2026 | 16,00 | 28,00 | 24,00 | 0,00 | **20,00** |
| 01/07/2026 | 40,00 | 4,00 | 24,00 | 0,00 | **20,00** |
| 01/09/2026 | 20,00 | 0,00 | 0,00 | 0,00 | **20,00** |

Las cuatro cuadran y **el saldo no se mueve**, que es todo el punto. La última fila es el caso que
hoy desaparece de la lista: sin movimientos desde septiembre, la persona debe seguir estando, con
su saldo. Y contra la base:

```sql
SELECT SUM(horas) FROM movimientobancohoras WHERE idcontrato = 246;   -- 20.00
```

## Riesgos

**El rendimiento**: las dos consultas agrupan **todos** los movimientos, sin tope por arriba ni
por abajo. Es el precio de que el saldo sea real. El banco tiene unas pocas filas por persona y
por año, así que no es un problema hoy; si algún día lo fuera, el saldo anterior se resuelve con
una tabla de cortes, no volviendo a atar el saldo al filtro.

**El campo Hasta desaparece.** Quien quiera ver un mes cerrado ya no puede acotar por arriba. Es
deliberado: para eso está el detalle por persona, que muestra cada movimiento con su fecha. Si más
adelante hace falta, vuelve como filtro del detalle y no del saldo.

## Lo que este plan no toca

La equivalencia en días sigue saliendo de la **jornada semanal** (`jornadasemanal`, 8,00 h en
terdemol), y no del turno real del día. Un permiso de día completo en un turno de 12 h se sigue
mostrando como 1,50 d. Se discutió al probar el saldo negativo el 2026-09-23 y **se decidió
dejarlo así**: el turno cambia día a día —4, 7, 8, 12 h— y no existe un divisor único con el que
expresar un saldo global en días. La regla de 8 h es estable y es la de la norma.

## Estado

| Etapa SDD | Estado |
|---|---|
| SPEC | Este documento lo incluye |
| PLAN | Aprobado el 2026-09-23 |
| **IMPLEMENT** | **S1–S6 hechas y probadas en pantalla** el 2026-09-23 |

Probado con las fechas de S7: el saldo da **20,00 con las tres**, la fila cuadra en las tres, y con
*Desde 01/09/2026* —sin ningún movimiento en septiembre— ACHOCALLA **sigue apareciendo** con su
saldo anterior. El TOTAL al pie da 20,00.

Una nota para quien despliegue: la primera compilación salió en formato Java 21 porque `JAVA_HOME`
apuntaba a un JDK que no existe en la máquina, y JBoss rechazó las clases con
`UnsupportedClassVersionError`. Compilar con **JDK 1.8** no es una recomendación: es la condición
para que el servidor las cargue. Vale la pena verificar la versión del compilador *antes* de
confiar en un `BUILD SUCCESSFUL`.
