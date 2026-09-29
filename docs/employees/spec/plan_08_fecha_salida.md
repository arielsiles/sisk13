# PLAN 08 — La fecha de salida y los contratos simultáneos

> Nace de la prueba de baja: *"si doy de baja fija la fecha de salida y luego creo otro contrato,
> esa fecha queda mal"*. Al revisar casos apareció algo más grande: hay empresas cliente con
> **varios contratos vigentes a la vez**, uno principal y el resto secundarios.

## El problema de fondo

`empleado.fechasalida` **no es un dato, es una conclusión**: la persona se fue si no le queda
ningún contrato abierto. Hoy se escribe a mano, una sola vez, al dar de baja, y nadie la vuelve a
mirar.

Un valor derivado que se guarda a mano se desactualiza. Siempre. Y acá se agrava porque **no se
muestra en ninguna pantalla**: cuando se desactualiza no hay forma de notarlo mirando el sistema.
Se nota por el efecto, que es que vacaciones deja de generarle plan a alguien que sí trabaja.

### Los casos que hoy quedan mal

| | Qué pasa |
|---|---|
| **Recontratación** | Se da de baja, después se le crea un contrato nuevo. La persona volvió, la fecha sigue puesta |
| **Contrato secundario** | Se cierra uno de varios. Sigue trabajando por los otros, pero queda marcada como que se fue |
| **Baja corregida** | Se registra la baja dos veces con distinta fecha. El contrato se corrige, la fecha de salida no: solo se escribe cuando está vacía |
| **Reactivación por fuera** | Se reabre un contrato con SQL y nadie limpia la fecha |
| **Carga histórica** | Los 167 cerrados por la sección 5 tienen fecha de relleno |

Los cuatro primeros son la misma falla: **un solo escritor y ningún corrector**.

## Cómo lo resuelven los sistemas profesionales

Tener varios contratos a la vez con uno principal no es una rareza: tiene nombre y está resuelto
hace veinte años.

| Sistema | Cómo se llama | Cómo lo modela |
|---|---|---|
| **SAP HCM** | *Concurrent Employment* | Una **Central Person** agrupa varias *Personnel Assignments*; una es la **principal**, y sobre ella se calculan los agregados legales |
| **Oracle HCM Cloud** | *Work Relationships / Assignments* | Persona → relaciones → asignaciones, con un flag de **primary** en cada nivel |
| **Workday** | *Additional Jobs* | Un **primary job** y los adicionales colgando del mismo *worker* |

Los tres comparten **tres reglas**, y son las que conviene copiar:

1. **La persona es el ancla, no el contrato.** Los datos de la persona —salida, afiliación— se
   derivan del conjunto de sus contratos, nunca de uno.
2. **Nunca más de un principal a la vez.** No es una preferencia: hay cálculos que necesitan un
   único origen, y dos principales los dejan sin respuesta.
3. **La persona se va cuando se cierra el último contrato, no el primero.** Oracle lo separa en
   tres niveles distintos de terminación justamente para no confundirlos.

La regla 3 es, palabra por palabra, la que este plan necesita. Que el modelo de tu cliente y el
de SAP lleguen a la misma conclusión es la mejor señal de que es la correcta.

## La regla de la fecha de salida

```
Si la persona tiene al menos un contrato que NO esté INACTIVO
        →  fecha de salida = vacía

Si todos sus contratos están INACTIVO
        →  fecha de salida = la mayor fecha de fin entre ellos

Si no tiene ningún contrato
        →  no se toca
```

Sirve igual con un contrato que con cinco: pregunta por el **conjunto**, no por el que se acaba
de tocar. Por eso la aparición de los contratos simultáneos no la cambió.

El último caso no es un detalle: hay 7 personas sin contrato, gente cargada pero no contratada.
Inventarles una salida sería peor que dejarlas como están.

Se decide por el **estado** y no por la fecha de fin porque el estado es la decisión explícita de
alguien. Un plazo fijo vencido que nadie cerró sigue ACTIVO: nadie dijo todavía que esa persona
se fue, y el sistema no debe decirlo por su cuenta.

## Parte A — La fecha de salida

Se puede hacer ya. No depende de ninguna decisión pendiente y no toca el modelo de datos.

### A1 · Una sola función que la calcula

`refreshRetireDate(empleado)` en `ContractConditionService`, junto a la baja. Aplica la regla y
escribe el resultado. **Es el único lugar del sistema que toca esa columna.**

Es el corazón del plan: mientras haya un solo escritor y todos los caminos pasen por él, la
columna no puede quedar en un estado que la regla no explique.

### A2 · La baja deja de escribirla a mano

`terminate()` hoy hace *"si está vacía, ponerle la fecha"*. Pasa a cerrar el contrato y llamar a
A1. Con eso quedan resueltos **contrato secundario** y **baja corregida** sin ninguna condición
extra: la regla ya contempla que quede otro contrato abierto, y ya contempla recalcular sobre un
valor existente.

### A3 · El alta, la edición y el borrado de un contrato recalculan

Es el caso de la **recontratación**. Cubre más de lo que parece: la pantalla de contratos permite
cambiar el estado a mano, así que se puede reactivar un contrato sin pasar por *Dar de baja*. Ese
camino también queda corregido.

Corre **dentro de la misma transacción del guardado**. Si falla, falla el guardado: es preferible
a guardar el contrato y dejar la fecha mintiendo.

### A4 · Recálculo masivo, una vez

La sección 5.3 del script se reescribe con la regla completa en lugar del criterio parcial que
tiene, y queda **idempotente**: quien ya corrió la versión anterior puede volver a correrla.

### A5 · Que se pueda ver

La fecha aparece en *Editar empleado*, **solo de lectura**, con una nota de dónde sale. De
lectura a propósito: si se pudiera escribir ahí habría dos lugares para cambiar lo mismo y
volveríamos al problema de origen.

### A6 · El contador del listado

Al dar de baja, la fila desaparece pero el pie sigue diciendo *"Items 1 - 1 de 1"*. Es el mismo
refresco que se agregó recién: se limpian las filas guardadas pero no la cuenta del paginador.

## Parte B — Contratos simultáneos

Esto es modelo nuevo. **En terdemol no existe el caso** —238 personas, un contrato cada una, un
puesto cada contrato, un plan de vacaciones cada uno—, así que se construye limpio y se prueba
con datos armados.

### B1 · La marca de contrato principal

Una columna nueva en `contrato`. Explícita y no calculada: cuál es el principal es una **decisión
de RRHH**, no algo deducible del orden de las fechas.

El invariante es **como máximo uno**, no exactamente uno:

```
Entre los contratos no inactivos de una persona puede haber
   un contrato principal   →  tiene AFP y vacaciones
   o ninguno               →  situacion valida
```

**Cero principales es un estado legítimo**, y era el punto que tenía mal la versión anterior de
este plan. Se da cuando termina la relación de planta y le quedan a la persona uno o más
contratos eventuales corriendo hasta su fecha de fin. No hay nada que corregir ahí: es la
realidad, y el sistema no tiene por qué inventar un principal que RRHH no designó.

Por eso el sistema **no promueve solo ni exige elegir**:

| Momento | Qué hace el sistema |
|---|---|
| Se crea o edita un contrato marcado como principal | le **saca la marca** a los demás abiertos de esa persona |
| Se crea un contrato sin marcar y ya hay un principal | queda **secundario**, y lo dice |
| Se crea un contrato sin marcar y no hay ningún principal | **no designa a nadie**, y **avisa** |
| Se cierra el principal y quedan otros | **no pasa nada automático**. La persona queda sin principal |

La casilla viene marcada al crear un contrato nuevo, para que el caso común no dependa de
acordarse. RRHH la desmarca cuando el contrato es un eventual.

Lo único que sí hace es **no dejar que sea invisible**. Sin principal no hay AFP ni vacaciones, y
esa es exactamente la situación que no puede ocurrir por olvido. Cuando una persona tiene
contratos activos y ninguno principal, se muestra como una **advertencia**, no como un error: en
el listado de contratos y al abrir a esa persona.

Es la diferencia entre un estado válido y un estado accidental. Los dos se ven igual en la base;
la advertencia obliga a que alguien haya pasado por ahí y lo haya visto.

### B2 · Qué le toca al principal y qué al conjunto

Es la parte que hay que dejar escrita, porque es donde se cometen los errores. Y no es "cuál de
los dos elijo": es que **el secundario no participa** de estos cálculos.

| | Principal | Secundario |
|---|---|---|
| **AFP / régimen de aportes** | sí | **no** |
| **Vacaciones** | sí, un plan | **no** |
| **Asistencia y horarios** | sí | **sí**, cada uno con su jornada |
| **Planilla del mes** | entra | entra |

Un contrato secundario es **trabajo eventual acotado, con fecha de inicio y de fin definidas**.
No genera relación de planta, y por eso no arrastra los derechos que sí arrastra el principal.

La planilla **ya funciona bien**: verifiqué que acumula por contrato, prorratea por alta y baja y
emite una sola fila por persona. No hay que tocarla.

Lo que sí está mal hoy es **vacaciones**: el plan se genera por contrato de puesto, así que una
persona con dos contratos tendría dos planes y acumularía el doble. Es el defecto concreto que B2
corrige, y se corrige solo con generar planes **únicamente para el contrato principal**.

> **A confirmar con contabilidad antes de codificar el aporte.** Que el secundario no genere
> aporte a la AFP es lo que corresponde al tipo de vínculo, pero la base del aporte es materia
> legal y equivocarla cuesta plata en las dos direcciones. Hasta que esté confirmado, el plan
> deja escrita la regla y no la aplica al cálculo.

### B3 · Reingreso y contrato adicional son dos cosas distintas

Hoy las dos se hacen igual —crear un contrato— y ninguna avisa nada.

- **Reingreso**: se le crea un contrato a alguien **sin** contratos activos. Volvió a la empresa.
- **Contrato adicional**: se le crea a alguien **con** contrato activo. Suma un puesto.

Los dos merecen un aviso en pantalla al elegir a la persona, y con texto distinto. El aviso
importa más que el registro: es lo que evita que alguien cree un contrato duplicado creyendo que
está dando de alta a un empleado nuevo.

Ambos quedan registrados como movimiento en `movimientocontrato`, que ya existe.

## Lo que se descartó y por qué

**Bloquear el segundo contrato activo.** Era la tarea S7 de la versión anterior de este plan.
Se cae con el caso que apareció: los contratos simultáneos son legítimos.

**Exigir que un contrato empiece después de que terminó el anterior.** Tenía sentido con un solo
contrato a la vez. Con contratos que se solapan por diseño, no hay nada que validar ahí.

**Promover un principal solo, o exigir que se designe uno al cerrar el anterior.** Las dos daban
por sentado que una persona con contratos activos tiene que tener un principal, y no es cierto:
quedarse con solo eventuales corriendo hasta su fecha de fin es una situación normal. Un sistema
que la trata como error obliga a inventar datos para poder seguir. Queda la advertencia, que
informa sin imponer.

**Derivar la fecha de salida en cada consulta y no guardarla.** Más puro, pero obliga a reescribir
las consultas de vacaciones y a confiar en que no haya otras leyendo la columna. Con un solo
escritor el resultado es el mismo con mucho menos riesgo.

**Todo lo que toque la antigüedad.** Se llegó a implementar una fecha de antigüedad reconocida
y se revirtió entera antes de entregar. La antigüedad no es solo días de vacaciones: alimenta el
**bono de antigüedad**, que la planilla ya calcula desde `contrato.fechainicio` y paga todos los
meses. Meterse con ella desde este plan habría dejado dos antigüedades distintas para la misma
persona, una para vacaciones y otra para el bono. Si alguna vez hace falta, es un plan propio que
arranca por la planilla.

**`empleado.fechaingreso`.** Está vacía en toda la base. Queda fuera, por lo mismo.

## Riesgos

**La columna nueva exige SQL antes de desplegar.** El sistema valida el esquema al arrancar: si
la columna no está, no levanta. Es el orden de siempre, pero conviene decirlo porque la parte A
no lo necesita y la B sí.

**Hay que sembrar el invariante.** Los 238 contratos actuales pasan a principal en el mismo
script. Como hay uno por persona, no hay ninguna decisión que tomar.

**Cinco lugares dependen de que el estado se llame `INACTIVO`**: la baja, el filtro del plan 07,
la regla de la fecha, y ahora las validaciones del principal. `estadocontrato` es una tabla por
empresa con el nombre escrito a mano. Al menos una constante única; convertirlo en enumerado
merece su propia evaluación.

**B toca la pantalla de contratos**, que es vieja, muy usada y ajena a este módulo. Conviene
probar un alta común después de aplicarlo.

## Cómo se verifica

**Parte A**, con PABLO GUIZADA, ya dado de baja al 31/12/2024:

1. En *Editar empleado* se ve **31/12/2024**, en gris, no editable.
2. Crearle un contrato nuevo desde hoy → la fecha queda **vacía** sin hacer nada más.
3. Dar de baja ese contrato al 30/09/2026 → la fecha pasa a **30/09/2026**.
4. Volver a darlo de baja al 31/10/2026 → la fecha **se corrige**. Hoy se quedaría en la primera.
5. Con la lista filtrada en un solo resultado, dar de baja: la fila desaparece **y el pie dice 0**.

**Parte B**, con una persona de prueba:

6. Primer contrato → viene **sugerido como principal**, y se puede desmarcar.
7. Segundo contrato → queda **secundario**, y avisa que ya tenía uno activo.
8. Cerrar el secundario → la fecha de salida sigue **vacía**.
9. Cerrar el principal quedando el eventual → **no pasa nada automático**. La persona queda sin
   principal y aparece la advertencia. Nada se bloquea.
10. Cerrar el último → recién ahí aparece la fecha de salida.
11. Vacaciones: **un solo plan**, el del contrato principal. Ninguno para el secundario.

## Lo que queda por confirmar

**La base del aporte a la AFP con un contrato secundario.** Que el eventual no genere aporte es
lo que corresponde al tipo de vínculo, pero es materia legal y equivocarla cuesta plata en las
dos direcciones. El plan deja la regla escrita; el cálculo espera la confirmación de
contabilidad.

No bloquea nada: en terdemol no existe todavía ningún contrato secundario.

## Estado

**A y B implementados y probados.** Compila con JDK 1.8. La parte C se descartó entera.

Probado con PABLO GUIZADA: baja → reingreso → baja → contrato secundario. La fecha de salida
se escribe y se borra sola en cada paso, y el contrato secundario avisa que la persona queda
sin contrato principal sin bloquear el guardado.

**Hay SQL que aplicar antes de desplegar**, porque se agregan dos columnas y el sistema valida
el esquema al arrancar:

| Script | Qué hace |
|---|---|
| `query_v6.1.0_terdemol.sql` seccion **20** | agrega `principal` y la siembra |
| `query_v6.1.0_terdemol_updates.sql` seccion **5.3** | reescrita con la regla completa; idempotente |

### Lo que costó tres vueltas

El recálculo de la fecha de salida falló tres veces seguidas, y las tres por motivos distintos:

1. Se llamaba **en medio del guardado**. Los pasos siguientes —el puesto y el contrato de
   puesto— volvían a arrastrar el empleado que la pantalla tenía en memoria y pisaban el valor.
   Se movió al final de todo.
2. Se escribía sobre la copia de la pantalla **antes** de comparar, así que la comparación daba
   siempre *"no cambió"* y salía sin guardar. Y esta pantalla usa *flush* manual: un cambio en
   memoria que nadie manda a grabar se descarta.
3. La consulta iba a la base y **no veía el contrato recién creado**, porque el alta corre en su
   propia transacción. `find` lo encontraba —resuelve contra el contexto de persistencia— pero
   la consulta no.

El tercero se resolvió sin pelear con las transacciones: **el contrato que dispara el recálculo
se pasa aparte y se suma al conjunto**, lo devuelva la consulta o no. Es correcto por definición
y queda inmune a cuándo se hace visible cada cosa.

Lo que hizo falta para encontrarlos fue **dejar rastro**: el recálculo escribe en el log qué
contratos vio y qué concluyó. Con *"vio 2 contratos"* se pierde una vuelta; con
*"vio [200, 336]"* se ve al instante que falta el tercero.

### Dónde quedó cada aviso

La advertencia de "sin contrato principal" quedó **al guardar el contrato**, que es el momento en
que se decide. En el listado de *Condición de contratos* se ve la etiqueta **Principal** en cada
fila que lo sea, así que la ausencia se nota mirando a la persona.

No se construyó una alerta que recorra a todos los empleados buscando quién quedó sin principal.
Con un contrato por persona no tiene a quién encontrar; cuando aparezca el primer cliente con
contratos simultáneos, se evalúa con casos reales.


