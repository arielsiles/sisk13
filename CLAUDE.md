# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SISK13 (KHIPUS) is an Integrated Accounting System built on Java EE/Seam 2.2 framework. Current version: 6.0.27.

## Build Commands

The project uses **Apache Ant** as the build system with JBoss AS 5.x as the target server.

```bash
# Build and deploy (development)
ant explode

# Build EAR archive
ant archive

# Clean build
ant clean

# Production build
ant -f build-prod.xml archive
```

Build profiles are configured in:
- `build.properties` - Primary configuration
- `build-dev.properties` - Development settings
- `build-prod.properties` - Production settings

JBoss home defaults to `D:/appserver/jboss-5.1.0.GA` (configurable in build.properties).

## Testing

```bash
# Run all tests
ant test
```

Tests use TestNG and JUnit. Test sources are in `src/test/com/encens/khipus/`.

## Technology Stack

- **Framework:** JBoss Seam 2.2 with JSF 2.0 and RichFaces
- **Persistence:** Hibernate 3.3 with JPA 1.0, Hibernate Envers for auditing
- **EJB:** Stateless session beans with JTA transactions
- **Database:** MySQL
- **Reporting:** JasperReports 3.7.4, iText for PDF, Apache POI for Excel
- **Server:** JBoss AS 5.x (5.1.0.GA or 6.1.0)

## Architecture

Classic J2EE layered architecture under package `com.encens.khipus`:

```
src/main/com/encens/khipus/
├── model/          # JPA entities (18 domain modules)
├── service/        # Stateless EJB services
├── action/         # Seam conversation-scoped action beans (JSF controllers)
├── framework/      # Generic base classes (GenericAction, GenericService)
├── dashboard/      # Dashboard components and DTOs
├── util/           # Utility classes
├── reports/        # JasperReports integration
├── exception/      # Business exceptions
├── converter/      # JSF converters
├── validator/      # Business validators
└── interceptor/    # Request interceptors
```

### Key Patterns

**Generic Framework Classes** in `framework/`:
- `GenericAction<T>` - Base CRUD action with conversation management (BEGIN/END)
- `GenericService` / `GenericServiceBean` - Base service interface and stateless EJB implementation

**Entity Layer:**
- All entities implement `BaseModel` interface
- Hibernate Envers auditing via `RevisionEntityInfo`
- Multi-tenancy filters: `companyFilter`, `businessUnitFilter`

**Service Layer:**
- Stateless EJBs with `@PersistenceContext` EntityManager injection
- Standard exceptions: `EntryDuplicatedException`, `EntryNotFoundException`, `ConcurrencyException`, `ReferentialIntegrityException`

**Action Layer:**
- `@Scope(ScopeType.CONVERSATION)` for Seam conversation management
- Navigation defined in module-specific `pages.xml` files under `resources/WEB-INF/`

## Key Directories

| Directory | Purpose |
|-----------|---------|
| `src/main/` | Java source code |
| `src/web/` | Web content (XHTML, web.xml) |
| `resources/` | Configuration (persistence.xml, components.xml, datasources) |
| `lib/` | Dependencies (65+ JARs) |
| `query/` | SQL migration scripts (v4.0.0 to v6.0.27) |
| `model/` | Database design files (PowerDesigner) |

## Configuration Files

- `resources/META-INF/persistence-*.xml` - JPA configuration (dev/prod/test)
- `resources/WEB-INF/components.xml` - Seam components configuration
- `resources/khipus-*-ds.xml` - MySQL datasource definitions
- `resources/WEB-INF/[module]/pages.xml` - Navigation rules per module

## Business Modules

The system covers: Accounting, Budget, Cashbox, Customers, Employees (Payroll/HR), Finances, Fixed Assets, Production, Products, Purchases, Treasury, Warehouse, and Admin.
