# docs/acopio — Planillas de Acopio de Leche

Documentación del módulo de generación de planillas de pago a productores de leche.

| Documento | Descripción |
|-----------|-------------|
| [analisis-planilla-acopio-leche-v2.md](analisis-planilla-acopio-leche-v2.md) | **Vigente.** Generación **actual**: filtro de días (sin/solo domingos), pre-cálculos batch `@Claude OPT-1..7`, `calculateLiquidPayable` con `BigDecimal HALF_UP`, exclusión de productores sin acopio, avisos de líquido negativo. Incluye fórmulas verbatim, mapeos de descuentos, esquema de tablas y puntos de riesgo para cambios de cálculo. |
| [analisis-planilla-acopio-leche-v1.md](analisis-planilla-acopio-leche-v1.md) | Histórico. Generación anterior (2026-03-11), antes del filtro de días y de las optimizaciones batch. |
| [requerimiento-excedentes-acopio.md](requerimiento-excedentes-acopio.md) | **Spec de implementación** del pago de excedentes por cupo (Modelo A: acopio real + config por productor; 2 planillas de excedente; ajuste sin excedente). Aprobado para implementar en rama a partir de `dev_ilva`. |

Para hacer **cambios en cálculos o generación**, usar la **v2** como referencia
(ver sección "⚠️ Puntos de riesgo para cambios de cálculo").
