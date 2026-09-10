# Sistema de iconos

> Antes de dibujar un icono nuevo, buscá acá. La mitad del desorden en iconografía sale de que
> nadie sabe qué existe.

## La regla

> **Un icono es una forma, no un color.**

El color lo decide el **contexto** —normal, hover, acción destructiva, deshabilitado—, nunca el
archivo. Eso es lo que permite que el mismo tacho de basura se vea gris en reposo y rojo cuando
el mouse está encima, sin duplicar el archivo.

## Cómo se usa

```xhtml
<a4j:commandLink styleClass="app-iconbtn" title="#{messages['Common.edit']}"
                 action="#{miAction.editar(item)}">
    <span class="app-ico app-ico--edit"/>
</a4j:commandLink>
```

Dos clases y nada más:

| Clase | Qué hace |
|---|---|
| `app-iconbtn` | el botón: recuadro, hover, tamaño. Va en el enlace |
| `app-ico app-ico--<nombre>` | el icono. Va en un `<span>` vacío adentro |

Para una acción destructiva o de cierre, sumá `app-iconbtn--danger`: el icono se tiñe de rojo
**al pasar el mouse**, no en reposo. Un icono rojo permanente hace que cada fila de la tabla se
lea como un problema.

**El `title` es obligatorio.** Un icono sin tooltip es una adivinanza.

## Por qué máscara y no `<img>`

Un `<img>` **no se puede recolorear desde CSS**: el color queda quemado dentro del `.svg`. Ese
era el problema real —había **seis colores distintos** entre dieciséis iconos, decididos archivo
por archivo—, y por eso el tacho se veía rojo hasta cuando no había nada que borrar.

Usados como máscara, del archivo solo importa la **forma**: el color lo pone
`background-color: currentColor`, que hereda del botón.

Los `.svg` **no cambiaron**. Los mismos archivos sirven para las dos formas de uso, así que las
pantallas viejas que todavía usan `<h:graphicImage>` siguen funcionando igual.

## El catálogo

| Clase | Significado | Dónde se usa hoy |
|---|---|---|
| `app-ico--edit` | **Editar / abrir** el registro | Turnos, Grupos, Excepciones |
| `app-ico--trash` | **Eliminar** definitivamente | Quitar del grupo |
| `app-ico--userMinus` | **Dar de baja / sacar** a una persona | Condición de contratos, Grupos |
| `app-ico--exchange` | **Cambiar** de estado o condición | Condición de contratos |
| `app-ico--check` | **Elegir** de una lista | Buscador del préstamo |
| `app-ico--search` | **Buscar** | filtros |
| `app-ico--filterOff` | **Limpiar** el filtro | filtros |
| `app-ico--close` | **Cerrar / cancelar** | — |
| `app-ico--eye` | **Ver** sin editar | — |
| `app-ico--user` | Persona | Acopios |
| `app-ico--userSearch` | Buscar persona | — |
| `app-ico--excel` | Exportar a Excel | listados |
| `app-ico--pdf` | Exportar a PDF | Órdenes de compra |
| `app-ico--journal` | Comprobante contable | Órdenes de compra |
| `app-ico--levels` | Análisis por niveles | Cuentas de caja |
| `app-ico--equals` | Conciliación / igualdad | — |

**Un significado, un icono.** Si *editar* es el lápiz en Turnos, es el lápiz en todas partes. Lo
que se elige es el **significado**, no el dibujo.

## Cómo agregar uno

1. **Buscá primero en la tabla de arriba.** La mayoría de las necesidades ya están cubiertas: lo
   que suele faltar es el significado, no el dibujo.
2. Sacalo de **[Lucide](https://lucide.dev)** (licencia ISC, libre para uso comercial). Es la
   fuente que corresponde a la geometría que ya tiene el sistema. **No dibujar a mano.**
3. Guardalo en `view/img/<nombre>-line.svg` respetando el contrato de abajo.
4. Agregá la clase en `theme.css`, junto a las demás `.app-ico--*`. **La ruta va relativa a
   la hoja de estilos** (`../img/...`), no absoluta: en CSS no hay quien le anteponga el
   contexto de la aplicación, como sí hace `h:graphicImage`.
5. Sumá la fila a la tabla de este documento.

### El contrato geométrico

Todos los iconos comparten exactamente esto, y es lo que hace que se vean como un conjunto:

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
     fill="none" stroke="#34617f" stroke-width="2"
     stroke-linecap="round" stroke-linejoin="round">
```

| Propiedad | Valor | Por qué |
|---|---|---|
| `viewBox` | `0 0 24 24` | la grilla de Lucide y Feather |
| `width` / `height` | `16` | el tamaño al que se renderiza |
| `stroke-width` | `2` | uniforme; un `2.4` suelto se nota |
| `linecap` / `linejoin` | `round` | el rasgo del set |
| `fill` | `none` | son de trazo, no macizos |

El `stroke` del archivo **es irrelevante cuando se usa como máscara**. Está solo para que las
pantallas viejas con `<h:graphicImage>` sigan viéndose bien.

## Pendiente

Quedan **catorce pantallas** fuera del módulo de RRHH que usan los iconos con `<h:graphicImage>`
—contabilidad, almacenes, finanzas, producción—. Funcionan, pero no pueden cambiar de color
según el estado.

Migrarlas es mecánico: reemplazar la etiqueta por el `<span>`. No se hizo acá porque son
pantallas fuera del alcance de este trabajo y no se pudieron probar.
