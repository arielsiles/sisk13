# Módulo de Recursos Humanos (`employees`)

Documentación de referencia del módulo de RRHH de SISK13/KHIPUS: qué hace, cómo está
armado, de dónde sale cada número de la planilla y qué partes están vivas o muertas.

Escrita como **memoria de trabajo** para futuras implementaciones: antes de tocar el
módulo, leer el archivo correspondiente.

## Índice

| Archivo | Contenido |
|---|---|
| [01_modelo_de_datos.md](01_modelo_de_datos.md) | Entidades ↔ tablas, relaciones, enums, dónde vive cada cosa |
| [02_generacion_de_planillas.md](02_generacion_de_planillas.md) | El flujo completo: ciclo → gestión planilla → planilla generada → oficial → merge |
| [03_calculos_planilla.md](03_calculos_planilla.md) | Todas las fórmulas: haber básico, bonos, AFP, RC‑IVA, líquido, aguinaldo |
| [04_asistencia_y_marcado.md](04_asistencia_y_marcado.md) | Bandas horarias, marcado, atrasos, faltas, fechas especiales, reporte de control |
| [05_contabilizacion.md](05_contabilizacion.md) | Registro contable de la planilla: qué comprobantes genera y con qué cuentas |
| [06_reportes.md](06_reportes.md) | Catálogo de los ~40 reportes del módulo y de dónde saca los datos cada uno |
| [07_otros_submodulos.md](07_otros_submodulos.md) | Vacaciones, retiros/finiquitos, documentos de descargo, contratos, postulantes |
| [08_deuda_tecnica.md](08_deuda_tecnica.md) | Trampas conocidas, código muerto y cosas que rompen |
| [spec/](spec/README.md) | **SPEC y planes**: lo que se está construyendo, con su estado |

## Panorama

El módulo cubre el ciclo completo de personal: maestro de empleados, contratos y puestos,
bandas horarias y marcado biométrico, movimientos de sueldo, generación mensual de
planillas (sueldos, tributaria, fiscal, aguinaldo), contabilización y la batería de
reportes legales bolivianos (Ministerio de Trabajo, AFP/CNS, boletas, archivos de banco).

### Origen y consecuencias

KHIPUS nació como sistema **universitario**. El módulo de RRHH todavía arrastra
todo el aparato académico —docentes por hora, carreras, facultades, asignaturas,
encuestas de evaluación docente, postulantes— que en las instalaciones actuales
(industriales/cooperativas) **no se usa**. Ver [08_deuda_tecnica.md](08_deuda_tecnica.md).

Medido sobre la réplica local `khipus` (FCISC/ILVA, migrada en el merge v6.0.125):

| Tabla | Filas | Estado |
|---|---:|---|
| `planillaadministrativos` (ManagersPayroll) | 31.363 | **camino vivo** |
| `planillatributariaporcategoria` | 31.343 | **vivo** |
| `planillafiscalporcategoria` | 31.343 | **vivo** |
| `planillatributaria` / `planillafiscal` (consolidadas por ciclo) | 4.156 | **vivo** |
| `reportecontrol` | 897.121 | **vivo** (una fila por banda/día/empleado) |
| `planillaaguinaldo` | 96 | vivo, uso puntual (diciembre) |
| `planillageneral` (docentes por hora) | 0 | **muerto** |
| `planilladocentelaboral` (docentes fiscal) | 0 | **muerto** |
| `vacacion`, `planvacacion` | 0 | **sin uso** |
| `retiro`, `documentodescargo` | 0 | **sin uso** |
| `postulante`, `formularioencuesta`, `evaluaciondocente` | 0 | **sin uso** |
| `formfactura` (form. 110 RC‑IVA) | 0 | **sin uso** → crédito fiscal siempre 0 |

Categorías de puesto configuradas en esa réplica — todas `GENERATION_BY_SALARY`:

```
FINANCIERO CISC   CISC           ADM        GENERATION_BY_SALARY
LACTEOS ILVA      ILVA-CISC      ADM        GENERATION_BY_SALARY
LACTEOS ILVA      ILVA-EVENTUAL  ILVA-EVE   GENERATION_BY_SALARY
```

> Los conteos son de la réplica FCISC/ILVA, no de terdemol. El perfil de uso es el mismo
> (planilla de sueldos mensual), pero antes de asumir que algo "no se usa" en un cliente
> concreto, verificarlo contra su base.

## Capas y ubicación del código

```
src/main/com/encens/khipus/
├── action/employees/             173 clases  Seam actions (@Name, scope CONVERSATION) + *DataModel
│   └── reports/                   68 clases  GenericReportAction por reporte + scriptlets Jasper
├── model/employees/              140 clases  Entidades JPA + enums
├── service/employees/            162 clases  Interfaz + *ServiceBean (@Stateless)
└── util/employees/payroll/                   Motor de cálculo de planillas (patrón Calculator)
    ├── structure/                            PayrollGenerator, PayrollColumn, Calculator, MergeProcessor
    ├── tributary/                            29 clases: generador, merge y 27 calculadoras (RC-IVA)
    └── fiscal/                               15 clases: generador, merge y las calculadoras del Min. Trabajo
view/employees/                   255 archivos  Facelets + 67 .jrxml de reportes
resources/WEB-INF/employees/pages.xml          Navegación Seam (1.455 líneas)
```

Modelos que el módulo usa pero viven en **`model/finances`**: `Contract`, `Job`,
`JobContract`, `Salary`, `KindOfSalary`, `SalaryHistory`, `AccountingRecord`. No es un
error: son compartidos con finanzas.

## Menú

Pestaña **Recursos Humanos** ([menu.xhtml:1322‑1900](../../view/layout/menu.xhtml)),
clave i18n `menu.rrhh`. Accesos directos:

- **Empleados** (`EMPLOYEE`) · **Contratos puestos** (`JOBCONTRACT`) · **Gestión de planillas** (`GESTIONPAYROLL`)
- **Ciclo de generación de planillas** (`PAYROLLGENERATIONCYCLE`) ← *punto de entrada real del proceso mensual*
- **Plan de vacaciones** (`VACATIONPLANNING`) · **Movimientos de sueldo** (`SALARYMOVEMENT`)
- **Fechas especiales** (`SPECIALDATE`) · **Bandas horarias** (`HORARYBANDCONTRACT`) · **Cuentas bancarias** (`BANKACCOUNT`)
- Submenús: *Reportes*, *Desarrollo Humano* (académico, muerto), *Gestión de personal* (retiros),
  *Generación de planillas*, *Configuración* (gestiones, ciclos, tipos de movimiento, unidades
  organizacionales, tolerancias, límites, sectores, categorías, tasas AFP/CNS/IVA/SMN, bonos,
  reglas de vacación y de descuento).

El menú se dibuja sólo si el usuario tiene alguno de los ~50 permisos listados en el
`rendered` de la pestaña. Sobre por qué una opción "no aparece", ver la memoria
*Permisos: por qué no aparece una opción* — `hasPermission` falla en silencio.

## Convenciones del módulo

- **Actions** `@Name("xxxAction") @Scope(CONVERSATION)`, extienden `GenericAction<T>`;
  el patrón de alta/edición es el descrito en la memoria *Seam catalog edit pattern*.
- **DataModels** extienden `QueryDataModel<Long, T>`; ojo con el caché de paginación
  (memoria *QueryDataModel pagination cache*: `search()` no limpia `rowCount`, hay que llamar `update()`).
- **Servicios** `@Stateless @Name @AutoCreate`. Los que manejan transacción a mano usan
  `@TransactionManagement(BEAN)` + `UserTransaction` (el caso de `GeneratedPayrollServiceBean`).
- **Textos**: nunca literales en el `.xhtml` — siempre clave en `messages_app.properties`.
- **Reportes**: `GenericReportAction` + `.jrxml` en `view/employees/reports/`; la consulta
  se declara sobrescribiendo `getEjbql()` y `init()` con `restrictions`.
