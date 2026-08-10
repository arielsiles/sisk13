# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SISK13 (KHIPUS) is an enterprise-level Java EE integrated accounting system built with JBoss/Seam framework. It manages comprehensive business operations including accounting, human resources, inventory, production, sales, and financial management for industrial/agricultural cooperatives.

## Build and Development Commands

### Build System
- **Build**: `ant` (default target: explode)
- **Clean Build**: `ant clean explode`
- **Distribution**: `ant dist`
- **Deploy**: `ant deploy` (deploys to JBoss)

### Profiles
- **Development**: `ant -Dprofile=dev` (default)
- **Production**: `ant -Dprofile=prod`

### Testing
- **Run Tests**: `ant test`
- **Test Reports**: Results in `build/test-build/`

> **Compilación y despliegue (KHIPUS dev / KHIPUS-PROD prod):** el flujo completo
> —targets por entorno, selección de cliente y schema (`build-prod-<cliente>.properties`),
> blindajes de producción, y cómo generar el zip de release— está en
> `docs/despliegue.md`. El despliegue del lado servidor (systemd, backup, rollback)
> está en `scripts/deploy/README.md`. Compilar SIEMPRE con JDK 1.8.

## Architecture

### Technology Stack
- **Framework**: JBoss Seam 2.1 (Java EE)
- **Application Server**: JBoss AS 5.1.0.GA
- **ORM**: Hibernate 3.x with JPA
- **Frontend**: JSF 1.2 + RichFaces + Facelets
- **Database**: MySQL (primary), HSQLDB (testing)
- **Build**: Apache Ant

### Project Structure
```
src/main/com/encens/khipus/
├── action/           # Seam action components (controllers)
├── model/           # JPA entities and business models
├── service/         # Business logic services
├── util/           # Utility classes and helpers
├── validator/      # Custom validators
├── reports/        # JasperReports integration
├── framework/      # Framework extensions
└── interceptor/    # Security and AOP interceptors
```

### Key Modules
- **accounting**: Financial accounting, vouchers, reports
- **admin**: User management, roles, system administration
- **budget**: Budget planning and execution tracking
- **customers**: Customer management, sales, credits
- **employees**: Human resources, payroll, evaluations
- **finances**: Cash management, banking, treasury
- **fixedassets**: Asset management, depreciation
- **production**: Manufacturing process management
- **warehouse**: Inventory management, movements
- **purchases**: Procurement processes

> **XProduction (órdenes de producción):** los cálculos de la orden por tipo de línea
> (ULEXITA, BARITINA, General), templates y costeo están documentados en
> `docs/xproduction_ordenes_calculos.md`. De dónde sale cada número de los **reportes
> diarios** (saldo anterior desde Saldos de Almacén, artículos desde la configuración de
> la línea, fecha por plan de producción, marcadores de vales) está en
> `docs/xproduction_reporte_diario_criterios.md`.

> **Provisión de intereses sobre DPF:** el cálculo mensual (días inclusivos, base 360,
> orden del redondeo), de dónde sale cada cuenta del asiento, el CRUD de tipos de cambio
> de `arcgtc` y la verificación contra el histórico 2025 —incluido el error de fórmula
> de la planilla en mayo/junio— están en `docs/Contabilidad/provision_dpf.md`.

> **Plan de Cuentas (`arcgms`):** estructura del código por niveles, reglas de
> `cta_raiz`/`cta_niv3`, validaciones del alta, renumeración, integridad referencial
> (claves foráneas) y trampas de JSF/RichFaces del módulo están documentados en
> `docs/Contabilidad/plan_de_cuentas.md`. Leerlo antes de tocar cuentas contables.

### Configuration Files
- **Database**: `resources/khipus-{profile}-ds.xml`
- **Persistence**: `resources/META-INF/persistence-{profile}.xml`
- **Seam**: `resources/WEB-INF/components.xml`
- **JSF**: `resources/WEB-INF/faces-config.xml`
- **Navigation**: `resources/WEB-INF/{module}/pages.xml`

### Key Patterns
- **Seam Components**: Action classes annotated with `@Name` for dependency injection
- **Entity Management**: JPA entities with Hibernate envers for auditing
- **Data Models**: Seam `Query` components for list pagination
- **Validation**: JSR-303 Bean Validation + custom validators
- **Security**: Role-based access control via interceptors
- **Reports**: JasperReports with scriptlets for complex logic

### Development Notes
- Pages follow module-based organization in `view/{module}/`
- Seam's conversation scope used extensively for wizard-like flows
- Business unit restrictions applied via custom interceptors
- Multi-company support through company context
- Extensive use of suggestion boxes and modal panels for UX
- Dashboard system with customizable widgets
- Version 6.0.60.1 indicates mature, production system

### Database
- Main schema supports multi-company operations
- Extensive versioning queries in `query/` directory show active development
- Production and development databases configured separately