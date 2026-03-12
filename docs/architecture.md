# Arquitectura - SISK13 / KHIPUS

## 1. Resumen

SISK13 (KHIPUS) es un Sistema Integrado Contable construido sobre Java EE con el framework JBoss Seam 2.2. Cubre contabilidad, presupuestos, tesoreria, produccion (acopio de leche), compras, almacenes, activos fijos, recursos humanos y mas.

Version actual: **6.0.31**

## 2. Stack Tecnologico

| Capa | Tecnologia |
|------|-----------|
| Frontend | JSF 2.0 + RichFaces (XHTML Facelets) |
| Framework | JBoss Seam 2.2 |
| Servidor | JBoss AS 5.x (5.1.0.GA / 6.1.0) |
| Persistencia | Hibernate 3.3 + JPA 1.0 + Hibernate Envers (auditoria) |
| Base de datos | MySQL |
| Build | Apache Ant |
| Reportes | JasperReports 3.7.4, iText (PDF), Apache POI (Excel) |
| Testing | TestNG + JUnit |

## 3. Estructura del Proyecto

```
sisk13/
├── src/
│   ├── main/com/encens/khipus/    # Codigo fuente Java
│   └── test/com/encens/khipus/    # Tests
├── view/                           # Vistas XHTML (JSF/Facelets)
├── resources/                      # Configuracion (persistence.xml, datasources, pages.xml)
├── lib/                            # Dependencias JAR (65+)
├── query/                          # Scripts SQL de migracion (v4.0.0 a v6.0.x)
├── model/                          # Diseno de BD (PowerDesigner)
├── docs/                           # Documentacion
├── build/                          # Artefactos de compilacion
└── dist/                           # Distribuibles (EAR)
```

## 4. Arquitectura por Capas

```
┌─────────────────────────────────────────────┐
│  Vista (view/)                              │
│  XHTML + RichFaces + Facelets               │
├─────────────────────────────────────────────┤
│  Action (action/)                           │
│  Seam conversation-scoped beans             │
│  Base: GenericAction<T>                     │
├─────────────────────────────────────────────┤
│  Service (service/)                         │
│  Stateless EJBs + JTA transactions          │
│  Base: GenericService / GenericServiceBean   │
│        ExtendedGenericServiceBean           │
├─────────────────────────────────────────────┤
│  Model (model/)                             │
│  Entidades JPA (implementan BaseModel)      │
│  Hibernate Envers para auditoria            │
├─────────────────────────────────────────────┤
│  MySQL                                      │
└─────────────────────────────────────────────┘
```

### Paquetes principales (`src/main/com/encens/khipus/`)

| Paquete | Descripcion |
|---------|-------------|
| `model/` | Entidades JPA con `@Entity`, implementan `BaseModel` |
| `service/` | Interfaces `@Local` + EJBs `@Stateless` con `@PersistenceContext` |
| `action/` | Beans `@Scope(ScopeType.CONVERSATION)` que manejan la logica de UI |
| `framework/` | Clases base genericas (`GenericAction`, `GenericService`, `GenericServiceBean`, `ExtendedGenericServiceBean`) |
| `reports/` | Integracion con JasperReports |
| `dashboard/` | Componentes de dashboard y DTOs |
| `util/` | Utilidades (`Constants.java`, helpers) |
| `exception/` | Excepciones de negocio |
| `converter/` | Conversores JSF |
| `validator/` | Validadores de negocio |
| `interceptor/` | Interceptores de request |
| `applet/` | Applets Java (legacy) |
| `dataintegration/` | Integracion de datos |
| `initialize/` | Inicializacion del sistema |
| `listener/` | Listeners JPA/JSF |
| `tag/` | Tags JSF custom |

## 5. Modulos de Negocio

Cada modulo tiene su propio sub-paquete en `model/`, `service/` y `action/`:

| # | Modulo | Descripcion |
|---|--------|-------------|
| 1 | `accounting` | Contabilidad general, plan de cuentas, comprobantes |
| 2 | `admin` | Administracion del sistema, usuarios, roles |
| 3 | `budget` | Presupuestos |
| 4 | `cashbox` | Caja chica |
| 5 | `common` | Entidades compartidas (empresa, ciudad, moneda) |
| 6 | `contacts` | Contactos |
| 7 | `customers` | Clientes |
| 8 | `dashboard` | Dashboard de indicadores |
| 9 | `employees` | Empleados, planillas de sueldos, RRHH |
| 10 | `finances` | Finanzas |
| 11 | `fixedassets` | Activos fijos |
| 12 | `academics` | Modulo academico |
| 13 | `production` | Produccion (acopio de leche, planillas de pago) |
| 14 | `products` | Productos |
| 15 | `purchases` | Compras, ordenes de compra |
| 16 | `treasury` | Tesoreria |
| 17 | `usertype` | Tipos de usuario |
| 18 | `warehouse` | Almacenes, inventarios |
| 19 | `rest` | Servicios REST |
| 20 | `xproduction` | Produccion extendida |

## 6. Persistencia

### EntityManagers

El sistema usa multiples `persistence-unit` definidos en `resources/META-INF/persistence-*.xml`:
- **khaborez** - Unidad principal
- Configuraciones separadas para dev, prod y test

### Filtros Multi-tenancy

Hibernate filters aplicados globalmente:
- `companyFilter` - Filtra por empresa (`idcompania`)
- `businessUnitFilter` - Filtra por unidad de negocio

### Auditoria

- Hibernate Envers via `RevisionEntityInfo` y `RevisionEntityListener`
- Tracking automatico de cambios en entidades auditadas

### Entidades

- Todas implementan la interfaz `BaseModel`
- Listeners JPA: `CompanyListener`, `CompanyNumberListener`, `UpperCaseStringListener`

## 7. Configuracion

| Archivo | Proposito |
|---------|-----------|
| `resources/khipus-dev-ds.xml` | Datasource MySQL (desarrollo) |
| `resources/khipus-prod-ds.xml` | Datasource MySQL (produccion) |
| `resources/META-INF/persistence-dev.xml` | JPA config desarrollo |
| `resources/META-INF/persistence-prod.xml` | JPA config produccion |
| `resources/WEB-INF/components.xml` | Configuracion de componentes Seam |
| `resources/WEB-INF/[modulo]/pages.xml` | Reglas de navegacion por modulo |
| `build.properties` | Configuracion principal de build |
| `build-dev.properties` | Configuracion de desarrollo |
| `build-prod.properties` | Configuracion de produccion |

## 8. Patrones Clave

### CRUD Generico

`GenericAction<T>` provee operaciones CRUD estandar con manejo de conversaciones Seam (BEGIN/END). Los actions concretos extienden esta clase y agregan logica de negocio especifica.

### Servicios Stateless

`GenericServiceBean` y `ExtendedGenericServiceBean` proveen operaciones base de persistencia. Los servicios concretos inyectan `EntityManager` via `@PersistenceContext`.

### Excepciones de Negocio

| Excepcion | Uso |
|-----------|-----|
| `EntryDuplicatedException` | Registro duplicado |
| `EntryNotFoundException` | Registro no encontrado |
| `ConcurrencyException` | Conflicto de concurrencia (version) |
| `ReferentialIntegrityException` | Violacion de integridad referencial |

### Generacion de Comprobantes

Patron comun para generar comprobantes contables desde operaciones de negocio (planillas, compras, etc.), integrando el modulo de origen con `accounting`.

## 9. Constantes del Sistema

Definidas en `src/main/com/encens/khipus/util/Constants.java`:

| Constante | Valor | Descripcion |
|-----------|-------|-------------|
| `PRICE_UNIT_MILK` | 4.50 | Precio por litro de leche (Bs) |
| `DISCOUNT_GA` | 0.0 | Descuento GA (deshabilitado) |
| `IT` | 0.3 | Impuesto a las Transacciones (%) |
| `IUE` | 0.5 | Impuesto sobre Utilidades (%) |

## 10. Build y Deploy

```bash
# Build y deploy a JBoss (desarrollo)
ant explode

# Generar archivo EAR
ant archive

# Limpiar artefactos
ant clean

# Build de produccion
ant -f build-prod.xml archive

# Ejecutar tests
ant test
```

JBoss home por defecto: `D:/appserver/jboss-5.1.0.GA` (configurable en `build.properties`).
