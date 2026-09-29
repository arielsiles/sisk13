# PLAN 07 — Búsqueda de contratos

> Nace de probar la pantalla de **Condición de contratos** con los datos reales de julio.
> Los filtros funcionan como están escritos, pero uno promete algo que no cumple, otro esconde
> la mitad de los datos, y no hay forma de buscar un contrato viejo.

## Los tres problemas

### 1 · "Solo los que vencen" trae 49 casos y ninguno es trabajo pendiente

El filtro se queda en una sola condición: **fecha de fin menor o igual a hoy más 30 días**.

El problema es que la columna *fecha de fin* guarda **dos cosas distintas**:

| | Qué significa | Ejemplo real |
|---|---|---|
| Fin de plazo | Hasta cuándo dura el acuerdo. **Hay que decidir algo** | ADRIANA ALBORTA, 29/03/2027, ACTIVO |
| Fecha de salida | El día que la persona se fue. **Ya está decidido** | PABLO GUIZADA, 19/07/2024, INACTIVO |

Como el filtro no las distingue, hoy devuelve **49 contratos, todos INACTIVOS y todos de gente
que ya salió**, algunos de 2024. Ninguno necesita atención. La aclaración dice *"y los ya
vencidos sin resolver"*, pero nada en la consulta mira si está resuelto.

El resultado es peor que no tener el filtro: una lista de 49 avisos falsos entrena a la gente a
ignorarla, y el día que aparezca uno de verdad va a pasar desapercibido.

### 2 · "Entra en planillas" nunca muestra todo

Es una casilla de dos estados para un concepto de tres:

| | Qué trae | Cuántos |
|---|---|---|
| Tildada | Los que **sí** entran a planilla | 121 |
| Destildada | Los que **no** entran a planilla | 124 |
| *(falta)* | **Todos** | — |

Al destildarla no se apaga el filtro: se invierte. Nunca se ven los 245 juntos. Combinada con
*"Solo los que vencen"* el efecto es el que confundió en la prueba: 49 de un lado, 2 del otro, y
en ningún momento los 51.

### 3 · No se puede buscar un contrato de hace años

Caso real: *"esta persona hace tres años, ¿qué contrato tenía?"*. Hoy no hay manera razonable:

- **No hay filtro de Estado.** Los contratos históricos están mezclados con los vigentes y la
  única forma de separarlos es mirando la columna a ojo, página por página.
- **La pantalla arranca condicionada.** *"Entra en planillas"* viene tildado, así que un contrato
  viejo con esa marca apagada **no aparece nunca**, ni buscándolo por nombre.
- **No se puede ordenar por fecha.** Solo *Persona* es ordenable, así que el historial de una
  persona sale desordenado.
- **No se puede preguntar por una fecha.** Que es la pregunta real: no *"dame sus contratos"*
  sino *"¿cuál estaba vigente en marzo de 2023?"*.

## Qué se cambia

### B1 · "Solo los que vencen" pasa a significar trabajo pendiente

Cuando la casilla está tildada, se piden **tres cosas** en lugar de una:

```
Duración = Plazo fijo
Fecha de fin <= hoy + 30 días
Estado distinto de INACTIVO
```

Los indefinidos ya quedaban afuera solos —sin fecha de fin no hay vencimiento—, pero pedirlo de
forma explícita evita que un dato sucio se cuele.

La propiedad que interesa es que **las tres formas de resolver sacan el contrato de la lista**,
sin ningún paso extra:

| Decisión de RRHH | Qué cambia | Por qué sale de la lista |
|---|---|---|
| Ampliar el plazo | Fecha de fin se corre al futuro | ya no entra en los 30 días |
| Pasar a indefinido | Duración cambia y la fecha de fin se borra | ya no es plazo fijo |
| Dar de baja | Estado pasa a INACTIVO | queda excluido |

Con los datos de hoy, la lista pasaría de **49 avisos falsos a ninguno**, y ADRIANA aparecería
sola el 27/02/2027, treinta días antes de su vencimiento.

La aclaración cambia para decir la verdad: *"Plazo fijo vencidos o que vencen en 30 días, sin
renovar ni dar de baja"*.

### B2 · "Entra en planillas" pasa a combo de tres opciones

Igual que el combo *Duración*, con la opción en blanco arriba:

```
(en blanco)  ->  no filtra
Sí           ->  solo los que entran a planilla
No           ->  solo los que no entran
```

### B3 · Filtro nuevo: Estado

Combo alimentado de la tabla de estados de contrato —los mismos valores que ya muestra la
columna—, con la opción en blanco arriba. Es lo que separa *"la gente de hoy"* de *"el archivo"*,
y sin él ninguna búsqueda histórica es usable.

### B4 · Filtro nuevo: "Vigente en"

Una sola fecha. Trae los contratos que **cubrían ese día**:

```
empezó antes o ese mismo día
Y (no tiene fecha de fin  O  terminó ese día o después)
```

Responde directamente la pregunta que se hace en la práctica. Escribiendo `15/03/2023` sale el
contrato que cada persona tenía ese día, con su modalidad y su duración de entonces. Combinado
con el nombre, es un contrato: el de esa persona, ese día.

Se eligió **una fecha** y no un rango *desde/hasta* a propósito: el rango contesta *"contratos
que se cruzan con este período"*, que casi nunca es lo que se quiere y produce listas largas.

### B5 · Ordenar por Fecha de inicio y Fecha de fin

Las dos columnas pasan a ser ordenables, como ya lo es *Persona*. Con eso el historial de una
persona se lee en orden cronológico, que es como se lee un legajo.

### B6 · La pantalla arranca en los contratos activos

Hoy abre con *"Entra en planillas"* tildado. Pasa a abrir con **Estado = ACTIVO** y todo lo
demás en blanco. *Limpiar* deja el mismo estado.

El filtro que se va es el que estorbaba: *"Entra en planillas"* es una marca interna del proceso
de planilla, y esconder contratos por ella —sin que nadie la haya puesto— es lo que hacía que un
contrato viejo no apareciera ni buscándolo por nombre. El de Estado, en cambio, se corresponde
con una columna a la vista y con lo que la gente espera al abrir la pantalla: los contratos
vigentes.

Para ir al archivo se deja Estado en blanco. Es un solo paso, y es un paso que se piensa: quien
lo hace sabe que está saliendo de lo vigente.

El estado por defecto se busca **por nombre** en la tabla de estados. Si esa empresa no tiene una
fila llamada `ACTIVO`, el filtro arranca en blanco y se ve todo: ante la duda, de más y no de
menos.

### Cómo queda el panel

```
Persona              [                ]  Duración            [          v]
Estado               [ ACTIVO       v]   Entra en planillas  [          v]
Solo los que vencen  [ ] aclaración      Vigente en [dd/mm/aaaa] [Buscar] [Limpiar]
```

Seis filtros contra los cuatro de hoy, en la misma cantidad de renglones. La aclaración larga de
*"Solo los que vencen"* se queda donde estaba; la de *"Vigente en"* va como ayuda emergente del
calendario, para no apretar el renglón de los botones.

## Lo que no se cambia

- **Ninguna tabla, ninguna columna, ningún dato.** No hay SQL que aplicar y no hay riesgo de
  validación de esquema al desplegar.
- **Ningún cálculo.** Nada de esto toca planillas, asistencia ni el resolutor de jornadas.
- **Ningún permiso nuevo.**
- **Las acciones de la fila** —Modificar y Dar de baja— quedan igual.
- **La búsqueda por nombre** ya funciona sobre nombre y ambos apellidos; no se toca.

## Alcance

Tres archivos:

| Archivo | Qué recibe |
|---|---|
| `ContractConditionDataModel.java` | las condiciones de B1, los filtros de B3 y B4, los valores iniciales de B6 |
| `contractConditionList.xhtml` | los dos combos nuevos, el calendario, las cabeceras ordenables |
| `messages_app.properties` | las etiquetas nuevas y la aclaración corregida |

## Riesgos

**El estado se compara por nombre.** `estadocontrato` es una tabla por empresa con el nombre
escrito a mano, no un enumerado. B1 excluye la fila llamada `INACTIVO`, que es el mismo criterio
que ya usa *Dar de baja* para marcar el contrato. Si una empresa la llamara distinto, B1 dejaría
de excluir a los cerrados y volveríamos a ver ruido —nunca a ocultar trabajo real—. Es el riesgo
menos malo de los dos.

**El estado se filtra por nombre, no como entidad.** Primero se guardaba la entidad
`ContractState` y el combo se veía **vacío aunque el filtro estuviera aplicado**: el conversor de
entidades empareja el valor elegido contra los ítems usando su propio contexto de persistencia, y
una instancia traída por otro entity manager no coincide con ninguno. Guardando el nombre no hay
conversor de por medio. El costo es tener que vaciar a mano la opción en blanco, porque JSF 1.2
manda cadena vacía —no null— para las propiedades de texto, y una cadena vacía no apaga la
condición: filtraría por un estado inexistente y no devolvería nada.

**Cada condición del filtro necesita su propia expresión.** El buscador apaga una condición
cuando su valor es nulo, y esa lógica es de a una expresión por condición. Por eso *"Vigente en"*
se escribe como **dos condiciones separadas** que comparten la misma fecha, en vez de una sola
con un `O` adentro. Es la misma razón por la que la búsqueda por nombre concatena antes de
comparar.

## Cómo se verifica

1. Abrir la pantalla: *Estado* dice `ACTIVO` y no hay ningún otro filtro puesto.
2. Tildar *"Solo los que vencen"*: **la lista queda vacía**. Los 49 de gente que ya salió no
   están.
3. Modificar a ADRIANA y ponerle fin **dentro de los 30 días**: aparece sola. Ampliarle el plazo:
   desaparece. Volver a acortarlo, y darla de baja: desaparece igual.
4. Vaciar *Estado*: aparecen también los históricos. *Entra en planillas* en blanco → 245;
   en `Sí` → 121; en `No` → 124. Los tres números salen sin tocar nada más.
5. *Estado* en `INACTIVO` → solo el archivo.
6. Escribir `guizada` con *Estado* en blanco: aparece su contrato de 2024. Con la pantalla de
   hoy no aparece ni vaciando lo que se pueda vaciar.
7. *Vigente en* `15/03/2024`, sin nombre: salen los contratos que cubrían ese día. Ninguno con
   inicio posterior ni con fin anterior.
8. Clic en la cabecera *Fecha de inicio*: ordena por fecha, y de nuevo al revés.

## Estado

**B1 a B6 implementados.** Aprobado con un cambio sobre lo propuesto: la pantalla no arranca
neutra del todo, arranca en los contratos **activos**.

Compila con JDK 1.8. **No hay SQL que aplicar.** Falta desplegar y recorrer la verificación.
