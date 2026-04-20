# MIGRATION.md — Plan de Migración a Sophia 7.0

> **Producto:** Sophia 7.0 (anteriormente SISK13 / KHIPUS)
> **Paquete Java:** `net.encens.sophia`
> **Proyecto:** `sophia`
> **Documento generado:** 2026-03-24
> **Versión actual (legacy):** 6.0.41
> **En producción desde:** 2010
> **Stack destino:** Spring Boot 3.4 + Angular 19 + PrimeNG 18
> **Base de datos:** MySQL 8.4 LTS (migración a PostgreSQL planificada post go-live)
> **Herramienta de migración:** Claude Code

---

## TABLA DE CONTENIDOS

1. [Diagnóstico del Sistema Actual](#1-diagnóstico-del-sistema-actual)
2. [Inventario Técnico Detallado](#2-inventario-técnico-detallado)
3. [Riesgos del Stack Actual](#3-riesgos-del-stack-actual)
4. [Arquitectura Destino: Spring Boot + Angular](#4-arquitectura-destino-spring-boot--angular)
5. [Mapeo Detallado de Tecnologías](#5-mapeo-detallado-de-tecnologías)
6. [Migración del Backend: Seam/EJB → Spring Boot](#6-migración-del-backend-seamejb--spring-boot)
7. [Migración del Frontend: JSF/RichFaces → Angular + PrimeNG](#7-migración-del-frontend-jsfrichfaces--angular--primeng)
8. [Migración de Seguridad](#8-migración-de-seguridad)
9. [Plan de Migración de Base de Datos](#9-plan-de-migración-de-base-de-datos)
10. [Migración de Reportes](#10-migración-de-reportes)
11. [Estrategia de Migración Incremental (Strangler Fig)](#11-estrategia-de-migración-incremental-strangler-fig)
12. [Uso de Claude Code en la Migración](#12-uso-de-claude-code-en-la-migración)
13. [Cronograma y Fases](#13-cronograma-y-fases)
14. [Riesgos y Mitigación](#14-riesgos-y-mitigación)

---

## 1. DIAGNÓSTICO DEL SISTEMA ACTUAL

### 1.1 Resumen Ejecutivo

SISK13 (KHIPUS) es un **Sistema Integrado de Contabilidad** empresarial construido sobre Java EE/JBoss Seam 2.2, en producción continua desde 2010 para múltiples empresas. Es un sistema maduro con 16 años de lógica de negocio acumulada.

### 1.2 Métricas del Código

| Métrica | Valor |
|---------|-------|
| Archivos Java fuente | **2,534** |
| Páginas XHTML (vistas) | **832** |
| Líneas de código XHTML | **77,125** |
| Reportes JasperReports (.jrxml) | **266** |
| Archivos de migración SQL | **80** |
| Dependencias JAR | **111** |
| Módulos de negocio | **21** |
| Entidades JPA (modelo) | **~671 clases** |
| Servicios EJB | **~492 clases** |
| Acciones/Controladores JSF | **~778 clases** |
| Clases QueryDataModel | **~283** |
| Componentes custom Facelets | **22** |
| Archivos pages.xml (navegación) | **18** |
| Clases de test | **10** |

### 1.3 Módulos de Negocio

| Módulo | Entidades | Servicios | Acciones | Vistas | Reportes |
|--------|-----------|-----------|----------|--------|----------|
| **Employees (RRHH/Planilla)** | 140 | 162 | 241 | 187 | 67 |
| **Finances** | 139 | 62 | 95 | 78 | 25 |
| **Production** | 83 | 60 | 99 | 102 | 33 |
| **Warehouse** | 51 | 46 | 96 | 83 | 39 |
| **Customers** | 71 | 48 | 69 | 76 | 23 |
| **Fixed Assets** | 35 | 39 | 73 | 67 | 36 |
| **Contacts** | 22 | — | 28 | 27 | — |
| **Admin** | 20 | 16 | 21 | 22 | 1 |
| **Academics** | 18 | 12 | 2 | — | — |
| **Purchases** | 17 | 10 | 6 | 7 | 1 |
| **XProduction** | 15 | 8 | 11 | 20 | — |
| **Budget** | 12 | 8 | 28 | 19 | — |
| **Accounting** | 2 | 2 | 31 | 33 | 28 |
| **Cashbox** | 4 | — | 25 | 9 | 11 |
| **Products** | 4 | 4 | 9 | 9 | 2 |
| **Dashboard** | 6 | 4 | 19 | 49 | — |
| **Treasury** | 1 | 2 | 1 | 3 | — |
| **REST API** | 43 | 1 | 7 | — | — |
| **Common** | 5 | 8 | 6 | 1 | — |

### 1.4 Stack Tecnológico Actual

```
┌─────────────────────────────────────────────────────┐
│                    PRESENTACIÓN                      │
│  JSF 2.0 + RichFaces 3.x + Facelets                │
│  832 páginas XHTML, skin glassX                     │
│  jQuery (limitado), 22 componentes custom           │
├─────────────────────────────────────────────────────┤
│                    CONTROLADORES                     │
│  JBoss Seam 2.2 (Conversaciones)                    │
│  778 Action beans (@Scope CONVERSATION)             │
│  @Begin/@End, @In/@Out, @Factory                    │
│  pages.xml para navegación                          │
├─────────────────────────────────────────────────────┤
│                    SERVICIOS                         │
│  EJB 3.0 Stateless Session Beans                    │
│  492 servicios, GenericServiceBean base             │
│  @TransactionAttribute(REQUIRES_NEW)                │
│  Quartz para jobs asíncronos                        │
├─────────────────────────────────────────────────────┤
│                    PERSISTENCIA                      │
│  Hibernate 3.3 + JPA 1.0                            │
│  Hibernate Envers (auditoría)                       │
│  Multi-tenancy via @Filter (company, businessUnit)  │
│  3 EntityManagers (mutación, lectura, BU-lectura)   │
├─────────────────────────────────────────────────────┤
│                    BASE DE DATOS                     │
│  MySQL (esquema: khipus)                            │
│  Tablas de auditoría: *_AUD                         │
│  Secuencias: seqKhipus (TableGenerator)             │
├─────────────────────────────────────────────────────┤
│                    SERVIDOR                          │
│  JBoss AS 5.x (5.1.0.GA / 6.1.0)                  │
│  Deploy: EAR (EJB JAR + WAR)                       │
│  Build: Apache Ant                                  │
└─────────────────────────────────────────────────────┘
```

### 1.5 Integraciones Externas

| Integración | Tecnología | Ubicación |
|-------------|-----------|-----------|
| Email (SMTP) | JavaMail API, TLS | `util/EmailSender.java` |
| REST API | Jersey 2.10.2 + Jackson 2.9.x | `action/restful/`, `service/rest/` |
| HTTP Client | HttpURLConnection | `util/RestHttpClient.java` |
| Facturación electrónica | HTTP POST/GET | `action/billing/BillControllerAction.java` |
| Sincronización WISE | Quartz + Bean-managed TX | `dataintegration/wise/` |
| Códigos QR/Barcode | ZXing | `util/Barcode*.java` |
| Reportes PDF/Excel | JasperReports 3.7.4 + iText + POI | `reports/`, `view/*/reports/` |

### 1.6 Patrones Arquitectónicos Identificados

1. **Template Method**: `GenericAction`/`GenericServiceBean` definen plantillas CRUD
2. **DAO implícito**: Capa de servicio encapsula acceso a entidades
3. **Factory**: `EntityQuery`, Seam component factory (`@Factory`)
4. **Observer**: Seam Events para comunicación desacoplada
5. **Decorator**: Filtros Hibernate para multi-tenancy
6. **Strategy**: Calculadoras de planilla (40+ clases tributarias/fiscales)
7. **Builder**: `QueryBuilder` para consultas dinámicas EJBQL
8. **Singleton**: `KhipusCacheManager`, `CustomQuartzProcessorSync`

### 1.7 Seguridad

- **Autenticación**: Custom sobre Seam Security + `AppIdentity`
- **Hash de passwords**: `Hash.instance().hash()` (hash propio)
- **Autorización**: Basada en permisos (`identity.setPermissions()`)
- **Multi-tenancy**: Filtros a nivel Hibernate por Company y BusinessUnit
- **Auditoría de sesión**: `SessionUserLog` con IP, usuario, timestamp
- **Cifrado URL**: `URLCipher` custom

### 1.8 Internacionalización

- **Locale**: Español (`es`) hardcoded como default
- **Timezone**: GMT-4 (Bolivia)
- **Bundles**: `messages_app.properties` (528 KB), `messages_jsf.properties` (6.5 KB)
- **Acceso**: `@In Map<String, String> messages` + `FacesMessages`

---

## 2. INVENTARIO TÉCNICO DETALLADO

### 2.1 Dependencias Críticas (111 JARs)

**Framework Core:**
- JBoss Seam 2.2 (jboss-seam.jar, jboss-seam-ui.jar, jboss-seam-pdf.jar)
- JSF 2.0 (jsf-api.jar, jsf-impl.jar, jsf-facelets.jar)
- RichFaces 3.x (richfaces-api.jar, richfaces-impl.jar, richfaces-ui.jar)

**Persistencia:**
- Hibernate 3.3 (hibernate-core, hibernate-annotations, hibernate-entitymanager)
- Hibernate Envers (hibernate-envers.jar)
- MySQL Connector 5.0.8 (mysql-connector-java-5.0.8-bin.jar)
- Oracle JDBC (ojdbc14.jar) — soporte dual DB

**Reportería:**
- JasperReports 3.7.4 (jasperreports-3.7.4.jar)
- iText 2.1.7 (itext-2.1.7.jar)
- Apache POI 3.5-FINAL (poi-3.5-FINAL.jar)
- JFreeChart 1.0.12 (jfreechart-1.0.12.jar)

**Utilidades:**
- Jackson 2.9.9 (jackson-core, jackson-databind, jackson-annotations)
- Jersey 2.10.2 (jersey-client, jersey-common)
- Quartz Scheduler
- Joda-Time 1.6
- ZXing (barcode/QR)
- exp4j (expression evaluator)
- Javassist 3.20

### 2.2 Configuración de Persistencia

```
Persistence Unit: khipus
├── Provider: org.hibernate.ejb.HibernatePersistence
├── Datasource: java:/khipusDatasource (JTA)
├── Dialect: org.hibernate.dialect.MySQLDialect
├── DDL: hibernate.hbm2ddl.auto = validate
├── Cache: Query cache disabled
├── Envers: Audit tables *_AUD
│   ├── Revision field: idreventidad
│   ├── Revision type: tiporevision
│   └── Events: post-insert, post-update, post-delete
└── Multi-tenancy Filters:
    ├── companyFilter → #{currentCompany.id}
    └── businessUnitFilter → #{currentUser}
```

### 2.3 Componentes Custom Facelets (22 tags)

| Tag | Tipo | Uso en páginas |
|-----|------|----------------|
| `app:commandSortHeader` | Tag | 2,282 |
| `app:realNumberConverter` | Converter | 983 |
| `app:selectPopUp` | Tag | 468 |
| `app:fieldset` | Tag | 352 |
| `app:dataScroller` | Tag | 311 |
| `app:quickSearch` | Tag | 303 |
| `app:naturalNumberConverter` | Converter | 183 |
| `app:reportDefaultOptions` | Tag | 116 |
| `app:dataTable` | Tag | — |
| `app:suggestionBox` | Tag | — |
| `app:inputBarcode` | Tag | — |
| `app:customInput` | Tag | — |

### 2.4 Estructura de Navegación

- **18 archivos pages.xml** (6,819 líneas totales)
- Modelo: `login-required="true"` por módulo
- Conversaciones: `<begin-conversation>` / `<end-conversation>`
- Navegación basada en outcomes: `Success`, `Fail`, `Cancel`
- Redirects entre módulos

---

## 3. RIESGOS DEL STACK ACTUAL

### 3.1 Componentes en End-of-Life (EOL)

| Componente | Estado | Riesgo |
|------------|--------|--------|
| **JBoss Seam 2.2** | EOL desde 2012 | **CRÍTICO** — Sin parches de seguridad |
| **JBoss AS 5.x** | EOL desde 2013 | **CRÍTICO** — Vulnerabilidades conocidas |
| **RichFaces 3.x** | EOL desde 2016 | **ALTO** — Sin soporte, CVEs abiertos |
| **Hibernate 3.3** | EOL desde 2012 | **ALTO** — 12 versiones major atrás |
| **JSF 1.2/2.0 legacy** | Superseded | **MEDIO** — Jakarta Faces 4.x es el estándar |
| **MySQL Connector 5.0.8** | EOL | **ALTO** — Vulnerabilidades de seguridad |
| **iText 2.1.7** | EOL | **MEDIO** — Licenciamiento cambió a AGPL |
| **JasperReports 3.7.4** | EOL | **MEDIO** — 20+ versiones atrás |
| **Jackson 2.9.x** | EOL | **ALTO** — CVEs conocidos (deserialización) |
| **Java EE** | Migrado a Jakarta EE | **MEDIO** — Namespace `javax.*` → `jakarta.*` |

### 3.2 Deuda Técnica

1. **Cobertura de tests**: Solo 10 clases de test para 2,534 archivos fuente (~0.4%)
2. **Build system**: Ant es legacy; no hay gestión de dependencias (JARs en `lib/`)
3. **Sin CI/CD**: No hay pipeline de integración/deploy continuo visible
4. **Seguridad**: Hash de passwords custom (no bcrypt/argon2), sin CSRF protection moderna
5. **Frontend monolítico**: 832 XHTML pages acopladas al servidor, sin API REST separada
6. **Sin containerización**: Deploy manual a JBoss AS

### 3.3 Impacto en el Negocio

- **Vulnerabilidades sin parche**: JBoss AS 5.x y Seam 2.2 tienen CVEs sin resolver
- **Dificultad de contratación**: Desarrolladores Seam 2.2/RichFaces son prácticamente inexistentes
- **Escalabilidad limitada**: JBoss AS 5.x no soporta clustering moderno
- **Compliance**: Drivers y librerías con vulnerabilidades conocidas pueden no pasar auditorías

---

## 4. ARQUITECTURA DESTINO: SPRING BOOT + ANGULAR

### 4.1 Diagrama de Arquitectura

```
┌─────────────────────────────────────────────────────────────────┐
│                     FRONTEND (Angular 19)                       │
│                                                                 │
│  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐      │
│  │  PrimeNG  │ │  Angular  │ │  NgRx /   │ │  Angular  │      │
│  │  UI Comp. │ │  Router   │ │  Signals  │ │  i18n     │      │
│  └───────────┘ └───────────┘ └───────────┘ └───────────┘      │
│  ┌──────────────────────────────────────────────────────┐      │
│  │  Módulos: Admin | Accounting | Employees | ...       │      │
│  │  Componentes: Tablas, Forms, Modales, Reportes       │      │
│  │  Servicios HTTP: Interceptors, Auth Guards           │      │
│  └──────────────────────────────────────────────────────┘      │
│  Build: Angular CLI + Node 22 | Deploy: Nginx / CDN           │
├─────────────────────────────────────────────────────────────────┤
│                     COMUNICACIÓN                                │
│  REST API (JSON) + JWT Bearer Token                            │
│  OpenAPI 3.1 (Swagger) — Contrato entre frontend y backend     │
│  CORS configurado para dominio frontend                        │
├─────────────────────────────────────────────────────────────────┤
│                     BACKEND (Spring Boot 3.4)                   │
│                                                                 │
│  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐      │
│  │  Spring   │ │  Spring   │ │  Spring   │ │  Spring   │      │
│  │  MVC REST │ │  Security │ │  Data JPA │ │  Mail     │      │
│  └───────────┘ └───────────┘ └───────────┘ └───────────┘      │
│  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐      │
│  │  Spring   │ │  Flyway   │ │  MapStruct│ │ Jasper    │      │
│  │  Scheduler│ │  Migrat.  │ │  Mappers  │ │ Reports 7 │      │
│  └───────────┘ └───────────┘ └───────────┘ └───────────┘      │
│  ┌──────────────────────────────────────────────────────┐      │
│  │  Capas: Controller → Service → Repository → Entity   │      │
│  │  Multi-tenancy: Hibernate @Filter (company, BU)      │      │
│  │  Auditoría: Hibernate Envers 6.x (tablas _AUD)       │      │
│  └──────────────────────────────────────────────────────┘      │
│  Runtime: Java 21 (LTS) | Build: Maven | Server: Tomcat emb.  │
├─────────────────────────────────────────────────────────────────┤
│                     PERSISTENCIA                                │
│  Hibernate 6.6 + JPA 3.1 (Jakarta Persistence)                │
│  Spring Data JPA Repositories                                  │
│  Hibernate Envers 6.6 (compatibilidad con tablas _AUD)         │
├─────────────────────────────────────────────────────────────────┤
│                     BASE DE DATOS                               │
│  MySQL 8.4 LTS (esquema existente preservado)                  │
│  Flyway para versionamiento de migraciones                     │
│  Redis 7.x (cache de sesiones JWT + cache de consultas)        │
├─────────────────────────────────────────────────────────────────┤
│                     INFRAESTRUCTURA                              │
│  Docker + Docker Compose (dev) / Kubernetes (prod opcional)    │
│  CI/CD: GitHub Actions (build → test → deploy)                 │
│  Monitoring: Spring Actuator + Micrometer + Prometheus         │
│  Logs: SLF4J + Logback → ELK o Loki (opcional)                │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 Justificación Técnica

| Criterio | Valor para Sophia |
|----------|-------------------|
| **Ecosistema** | Spring Boot es el framework Java #1 mundial, con soporte comercial de VMware/Broadcom |
| **Talento** | Los desarrolladores Spring + Angular son los más abundantes en el mercado |
| **API-first** | API REST abre la puerta a mobile apps, integraciones con terceros, microservicios |
| **Stateless** | Backend stateless = escalabilidad horizontal sin problemas de sesión |
| **Cloud-ready** | Spring Boot + Docker = deploy en AWS, Azure, GCP, o on-premise |
| **Testing** | Spring Test + Mockito + TestContainers = testing de primer nivel |
| **Claude Code** | Soporte excepcional — Spring Boot y Angular son los frameworks con mayor cobertura |
| **Longevidad** | Spring lleva 20+ años evolucionando, Angular es respaldado por Google |
| **Open source** | 100% open source, sin costos de licenciamiento |

### 4.3 Versiones Específicas del Stack Destino

| Tecnología | Versión | Soporte LTS |
|------------|---------|-------------|
| **Java** | 21 (LTS) | Hasta 2031 |
| **Spring Boot** | 3.4.x | Soporte comercial hasta 2027+ |
| **Spring Security** | 6.4.x | Incluido en Spring Boot |
| **Hibernate ORM** | 6.6.x | Incluido via Spring Data JPA |
| **Hibernate Envers** | 6.6.x | Compatible con tablas _AUD existentes |
| **Angular** | 19.x | Soporte Google, releases semestrales |
| **PrimeNG** | 18.x | Componentes UI ricos, open source |
| **MySQL** | 8.4 LTS | Soporte Oracle hasta 2032 |
| **Node.js** | 22 LTS | Para build de Angular |
| **Docker** | 27.x | Containerización |
| **Maven** | 3.9.x | Build backend |
| **Flyway** | 10.x | Migraciones DB |
| **MapStruct** | 1.6.x | Entity ↔ DTO mappers |
| **JasperReports** | 7.x | Reportería |
| **OpenPDF** | 2.x | Reemplazo de iText (open source) |
| **Jackson** | 2.18.x | JSON (incluido en Spring Boot) |
| **Redis** | 7.x | Cache + sesiones |

---

## 5. MAPEO DETALLADO DE TECNOLOGÍAS

### 5.1 Mapeo General

| Actual (Legacy) | Nuevo (Spring Boot + Angular) | Notas |
|-----------------|-------------------------------|-------|
| JBoss Seam 2.2 | Spring Boot 3.4 | Framework empresarial #1 |
| JSF 2.0 + RichFaces 3.x | Angular 19 + PrimeNG 18 | SPA moderna, responsive |
| EJB 3.0 Stateless | Spring `@Service` + `@Transactional` | Menos boilerplate |
| Seam Conversations | Estado en Angular (NgRx/Signals) | Stateless en backend |
| `@In`/`@Out` (Seam DI) | Constructor injection (Spring) | Inyección estándar |
| Hibernate 3.3 | Hibernate 6.6 via Spring Data JPA | APIs modernizadas |
| Hibernate Envers 3.x | Hibernate Envers 6.6 | Compatible con _AUD |
| JPA 1.0 (`javax.persistence`) | JPA 3.1 (`jakarta.persistence`) | Rename de namespace |
| pages.xml (navegación) | Angular Router | Navegación client-side |
| Quartz async | Spring `@Scheduled` / `@Async` | Integración nativa |
| JasperReports 3.7 | JasperReports 7.x | Preservar JRXML |
| iText 2.1.7 | OpenPDF 2.x | Fork open source |
| Apache POI 3.5 | Apache POI 5.x | API compatible |
| Apache Ant | Maven 3.9 | Gestión de deps moderna |
| JBoss AS 5.x | Embedded Tomcat (Spring Boot) | Sin deploy manual |
| `@Filter` multi-tenancy | Hibernate `@FilterDef` + `@Filter` | Patrón idéntico |
| Seam Security | Spring Security + JWT | Estándar industria |
| JavaMail API | Spring Mail | Simplificado |
| Jersey 2.x REST | Spring MVC `@RestController` | Incluido en Spring |
| Jackson 2.9 | Jackson 2.18 (Spring Boot managed) | Auto-configurado |
| Joda-Time 1.6 | `java.time` (Java 21) | API nativa de Java |
| `Hash.instance()` | BCryptPasswordEncoder | Seguridad moderna |
| `URLCipher` | No necesario (JWT + HTTPS) | JWT es stateless |
| jQuery | No necesario (Angular) | SPA nativa |
| HSQLDB (tests) | TestContainers + MySQL | Tests contra DB real |

### 5.2 Mapeo de Componentes RichFaces → PrimeNG

| RichFaces (Legacy) | PrimeNG (Angular) | Uso en proyecto |
|--------------------|--------------------|-----------------|
| `rich:dataTable` | `p-table` (con lazy loading, sort, filter) | 809 instancias |
| `rich:column` | Columnas de `p-table` con `pSortableColumn` | 10,906 instancias |
| `rich:columnGroup` | `p-columnGroup` | 790 instancias |
| `rich:calendar` | `p-calendar` (DatePicker) | 810 instancias |
| `rich:panel` | `p-panel` o `p-card` | 841 instancias |
| `rich:simpleTogglePanel` | `p-accordion` o `p-panel` (toggleable) | 583 instancias |
| `rich:modalPanel` | `p-dialog` | 204 instancias |
| `rich:tabPanel` / `rich:tab` | `p-tabView` / `p-tabPanel` | 118/256 instancias |
| `rich:menuItem` | `p-menuItem` / `p-menu` | 639 instancias |
| `rich:jQuery` | No necesario (Angular nativo) | 551 instancias |
| `rich:componentControl` | Angular ViewChild + template refs | 499 instancias |
| `rich:spacer` | CSS margin/padding | 164 instancias |
| `a4j:support` | Angular event binding + HttpClient | 645 instancias |
| `a4j:commandButton` | `p-button` con (click) handler | — |
| `a4j:commandLink` | `routerLink` o (click) | — |
| `a4j:region` | No necesario (component-level updates) | — |
| `a4j:poll` | `interval()` RxJS + HttpClient | Dashboard |
| `a4j:status` | Angular HTTP Interceptor + loading spinner | — |

### 5.3 Mapeo de Custom Facelets → Angular Components

| Custom Tag (Legacy) | Angular Equivalente | Estrategia |
|---------------------|---------------------|------------|
| `app:commandSortHeader` (2,282 usos) | `pSortableColumn` de PrimeNG Table | Componente nativo PrimeNG |
| `app:realNumberConverter` (983 usos) | `p-inputNumber` con locale `es-BO` | PrimeNG InputNumber |
| `app:selectPopUp` (468 usos) | `p-dialog` + `p-table` con selección | Componente shared Angular |
| `app:fieldset` (352 usos) | `p-fieldset` de PrimeNG | Componente nativo PrimeNG |
| `app:dataScroller` (311 usos) | `p-paginator` de PrimeNG Table | Componente nativo PrimeNG |
| `app:quickSearch` (303 usos) | `p-autoComplete` o filtro global en `p-table` | Componente shared Angular |
| `app:naturalNumberConverter` (183 usos) | `p-inputNumber` con `[useGrouping]="false"` | PrimeNG InputNumber |
| `app:reportDefaultOptions` (116 usos) | Componente shared `ReportOptionsComponent` | Componente custom Angular |
| `app:dataTable` | `p-table` wrapper customizado | Componente shared Angular |
| `app:suggestionBox` | `p-autoComplete` | PrimeNG AutoComplete |
| `app:inputBarcode` | Componente custom con ZXing-js | Componente custom Angular |

---

## 6. MIGRACIÓN DEL BACKEND: SEAM/EJB → SPRING BOOT

### 6.1 Mapeo de Clases Framework Base

#### GenericAction → REST Controller

```java
// ═══════════════════════════════════════════════════
// ANTES: Seam Action (GenericAction)
// ═══════════════════════════════════════════════════
@Name("companyAction")
@Scope(ScopeType.CONVERSATION)
public class CompanyAction extends GenericAction<Company> {

    @In private CompanyService companyService;
    @In protected Map<String, String> messages;
    @In protected FacesMessages facesMessages;

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(Company company) {
        setInstance(company);
        return Outcome.SUCCESS;
    }

    @End
    public String create() {
        try {
            getService().create(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.FAIL;
        }
    }

    @End
    public String delete() {
        try {
            getService().delete(getInstance());
            addDeletedMessage();
            return Outcome.SUCCESS;
        } catch (ReferentialIntegrityException e) {
            addReferentialIntegrityMessage();
            return Outcome.FAIL;
        }
    }
}

// ═══════════════════════════════════════════════════
// DESPUÉS: Spring Boot REST Controller
// ═══════════════════════════════════════════════════
@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Tag(name = "Companies", description = "Gestión de empresas")
public class CompanyController {

    private final CompanyService companyService;
    private final CompanyMapper companyMapper;

    @GetMapping("/{id}")
    @Operation(summary = "Obtener empresa por ID")
    public ResponseEntity<CompanyDTO> getById(@PathVariable Long id) {
        Company company = companyService.findById(id);
        return ResponseEntity.ok(companyMapper.toDTO(company));
    }

    @GetMapping
    @Operation(summary = "Listar empresas con paginación")
    public ResponseEntity<Page<CompanyDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        Page<Company> result = companyService.findAll(page, size, search);
        return ResponseEntity.ok(result.map(companyMapper::toDTO));
    }

    @PostMapping
    @Operation(summary = "Crear empresa")
    public ResponseEntity<CompanyDTO> create(@Valid @RequestBody CreateCompanyRequest request) {
        Company company = companyService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(companyMapper.toDTO(company));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar empresa")
    public ResponseEntity<CompanyDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCompanyRequest request) {
        Company company = companyService.update(id, request);
        return ResponseEntity.ok(companyMapper.toDTO(company));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar empresa")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        companyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

#### GenericServiceBean → Spring Service

```java
// ═══════════════════════════════════════════════════
// ANTES: EJB Stateless Session Bean
// ═══════════════════════════════════════════════════
@Stateless
@Name("companyService")
@AutoCreate
public class CompanyServiceBean extends GenericServiceBean implements CompanyService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @Logger
    protected Log log;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void create(Company company) throws EntryDuplicatedException {
        try {
            em.persist(company);
            em.flush();
        } catch (PersistenceException e) {
            log.debug("Duplicate entry", e);
            throw new EntryDuplicatedException();
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void delete(Company company)
            throws ReferentialIntegrityException, ConcurrencyException {
        try {
            company = em.merge(company);
            em.remove(company);
            em.flush();
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException(e);
        } catch (PersistenceException e) {
            throw new ReferentialIntegrityException();
        }
    }
}

// ═══════════════════════════════════════════════════
// DESPUÉS: Spring Service
// ═══════════════════════════════════════════════════
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    @Override
    public Company findById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company", id));
    }

    @Override
    public Page<Company> findAll(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        if (search != null && !search.isBlank()) {
            return companyRepository.findByNameContainingIgnoreCase(search, pageable);
        }
        return companyRepository.findAll(pageable);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Company create(CreateCompanyRequest request) {
        if (companyRepository.existsByLogin(request.login())) {
            throw new DuplicateEntryException("Company", "login", request.login());
        }
        Company company = companyMapper.fromCreateRequest(request);
        return companyRepository.save(company);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void delete(Long id) {
        Company company = findById(id);
        try {
            companyRepository.delete(company);
            companyRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ReferentialIntegrityException("Company", id);
        }
    }
}
```

#### QueryDataModel → Spring Data JPA Repository

```java
// ═══════════════════════════════════════════════════
// ANTES: QueryDataModel + EntityQuery (RichFaces DataModel)
// ═══════════════════════════════════════════════════
@Name("companyDataModel")
public class CompanyDataModel extends QueryDataModel<Long, Company> {
    private static final String[] RESTRICTIONS = {
        "lower(company.name) like concat('%', concat(lower(#{companyDataModel.criteria.name}), '%'))",
        "company.login = #{companyDataModel.criteria.login}"
    };

    @Override
    public String getEjbql() {
        return "select company from Company company";
    }

    @Override
    public String[] getRestrictions() {
        return RESTRICTIONS;
    }
}

// ═══════════════════════════════════════════════════
// DESPUÉS: Spring Data JPA Repository
// ═══════════════════════════════════════════════════
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long>,
                                           JpaSpecificationExecutor<Company> {

    Optional<Company> findByLogin(String login);

    boolean existsByLogin(String login);

    Page<Company> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT c FROM Company c WHERE c.active = true ORDER BY c.name")
    List<Company> findAllActive();
}

// Para queries dinámicas complejas (reemplazo de RESTRICTIONS):
public class CompanySpecifications {

    public static Specification<Company> withFilters(CompanyFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.name() != null) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                    "%" + filter.name().toLowerCase() + "%"));
            }
            if (filter.login() != null) {
                predicates.add(cb.equal(root.get("login"), filter.login()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

### 6.2 Mapeo de Excepciones

```java
// ═══════════════════════════════════════════════════
// ANTES: Excepciones Seam (@ApplicationException)
// ═══════════════════════════════════════════════════
@ApplicationException(rollback = true)
public class EntryDuplicatedException extends Exception { }

@ApplicationException(rollback = true)
public class ConcurrencyException extends Exception { }

@ApplicationException(rollback = true)
public class ReferentialIntegrityException extends Exception { }

// ═══════════════════════════════════════════════════
// DESPUÉS: Spring — RuntimeExceptions + @ControllerAdvice
// ═══════════════════════════════════════════════════
// Excepciones personalizadas
public class DuplicateEntryException extends RuntimeException {
    private final String entity;
    private final String field;
    private final Object value;
    // constructor...
}

public class EntityNotFoundException extends RuntimeException {
    private final String entity;
    private final Object id;
    // constructor...
}

public class ReferentialIntegrityException extends RuntimeException {
    private final String entity;
    private final Object id;
    // constructor...
}

public class ConcurrencyException extends RuntimeException { }

// Global Exception Handler
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(DuplicateEntryException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateEntryException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("DUPLICATE", e.getMessage()));
    }

    @ExceptionHandler(ReferentialIntegrityException.class)
    public ResponseEntity<ErrorResponse> handleReferential(ReferentialIntegrityException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("REFERENTIAL_INTEGRITY", e.getMessage()));
    }

    @ExceptionHandler(ConcurrencyException.class)
    public ResponseEntity<ErrorResponse> handleConcurrency(ConcurrencyException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CONCURRENCY", "El registro fue modificado por otro usuario"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALIDATION", "Errores de validación", errors));
    }
}

public record ErrorResponse(String code, String message, Object details) {
    public ErrorResponse(String code, String message) {
        this(code, message, null);
    }
}
```

### 6.3 Mapeo de Inyección de Dependencias

| Seam (Legacy) | Spring (Nuevo) | Notas |
|---------------|----------------|-------|
| `@In private ServiceX serviceX;` | Constructor injection (Lombok `@RequiredArgsConstructor`) | Preferido por inmutabilidad |
| `@In(required = false)` | `@Autowired(required = false)` o `Optional<T>` | Inyección opcional |
| `@In(create = true)` | `@Autowired` (Spring crea automáticamente) | Auto-create es default |
| `@In(value = "#{entityManager}")` | `@PersistenceContext EntityManager em;` o vía Repository | Spring Data JPA preferido |
| `@Out(scope = SESSION)` | Spring Security `SecurityContextHolder` o `@SessionScope` bean | Para datos de sesión |
| `@In protected Map<String, String> messages;` | `MessageSource` + `@Autowired` | i18n de Spring |
| `@In protected FacesMessages facesMessages;` | No necesario (errores en ResponseEntity) | API REST no tiene FacesMessages |
| `@Logger protected Log log;` | `@Slf4j` (Lombok) | SLF4J + Logback |
| `@Name("companyService")` | `@Service` (auto-detectado por Spring) | Component scanning |
| `@AutoCreate` | No necesario (Spring crea beans automáticamente) | Default en Spring |

### 6.4 Mapeo de Utilidades

| Utilidad Legacy | Reemplazo Spring/Java 21 |
|----------------|--------------------------|
| `BigDecimalUtil` | Mantener (lógica financiera crítica) — migrar como `@Component` |
| `DateUtils` | `java.time.LocalDate`, `LocalDateTime`, `ZonedDateTime` |
| `FormatUtils` | `java.text.NumberFormat`, `DateTimeFormatter` |
| `QueryUtils` / `QueryBuilder` | `JpaSpecificationExecutor`, Spring Data `@Query` |
| `ELEvaluator` | SpEL (`@Value`, `ExpressionParser`) si necesario |
| `KhipusCacheManager` | Spring Cache (`@Cacheable`) + Redis |
| `RestHttpClient` | Spring `RestClient` (Spring 6.1+) o `WebClient` |
| `EmailSender` | Spring `JavaMailSender` |
| `Hash.instance()` | `BCryptPasswordEncoder` |
| `FileCacheLoader` | Spring Resource + `@Cacheable` |
| `JSFUtil` | No necesario (eliminado con JSF) |
| `SessionUserUtil` | `SecurityContextHolder.getContext().getAuthentication()` |

### 6.5 Configuración Spring Boot

```yaml
# application.yml
spring:
  application:
    name: sophia-api

  datasource:
    url: jdbc:mysql://localhost:3306/khipus?useSSL=false&allowPublicKeyRetrieval=true
    username: ${DB_USERNAME:khipus}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      minimum-idle: 5
      maximum-pool-size: 50
      idle-timeout: 1800000  # 30 min
      connection-timeout: 30000

  jpa:
    hibernate:
      ddl-auto: validate  # NUNCA auto-create en producción
    open-in-view: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: false
        # Envers config (compatibilidad con tablas _AUD existentes)
        org.hibernate.envers:
          audit_table_suffix: AUD
          revision_field_name: idreventidad
          revision_type_field_name: tiporevision
        # Multi-tenancy filters
        '[hibernate.session_factory.session_scoped_interceptor]': net.encens.sophia.config.TenantInterceptor

  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration

  mail:
    host: mail.ilvabolivia.com
    port: 587
    properties:
      mail.smtp.starttls.enable: true

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: 6379

  jackson:
    default-property-inclusion: non_null
    serialization:
      write-dates-as-timestamps: false
    time-zone: GMT-4

server:
  port: 8080
  servlet:
    context-path: /api

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui

---
# application-dev.yml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
logging:
  level:
    net.encens.sophia: DEBUG
    org.hibernate.SQL: DEBUG

---
# application-prod.yml
spring:
  jpa:
    show-sql: false
server:
  port: ${PORT:8080}
logging:
  level:
    root: WARN
    net.encens.sophia: INFO
```

### 6.6 Multi-tenancy Configuration

```java
// Reemplazo de los Seam filters en components.xml
@Configuration
public class MultiTenancyConfig {

    @Bean
    public FilterRegistrationBean<TenantFilter> tenantFilter() {
        // HTTP filter que extrae tenant del JWT
        FilterRegistrationBean<TenantFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TenantFilter());
        registration.addUrlPatterns("/api/*");
        return registration;
    }
}

@Component
public class TenantContextHolder {
    private static final ThreadLocal<Long> currentCompanyId = new ThreadLocal<>();
    private static final ThreadLocal<List<Long>> currentBusinessUnitIds = new ThreadLocal<>();

    public static void setCompanyId(Long companyId) { currentCompanyId.set(companyId); }
    public static Long getCompanyId() { return currentCompanyId.get(); }
    public static void setBusinessUnitIds(List<Long> ids) { currentBusinessUnitIds.set(ids); }
    public static List<Long> getBusinessUnitIds() { return currentBusinessUnitIds.get(); }
    public static void clear() { currentCompanyId.remove(); currentBusinessUnitIds.remove(); }
}

// Hibernate filter activation (equivalente a components.xml filters)
@Component
public class HibernateFilterActivator {

    @PersistenceContext
    private EntityManager entityManager;

    public void activateFilters() {
        Session session = entityManager.unwrap(Session.class);

        Long companyId = TenantContextHolder.getCompanyId();
        if (companyId != null) {
            session.enableFilter("companyFilter")
                   .setParameter("currentCompanyId", companyId);
        }

        List<Long> buIds = TenantContextHolder.getBusinessUnitIds();
        if (buIds != null && !buIds.isEmpty()) {
            session.enableFilter("businessUnitFilter")
                   .setParameterList("businessUnitIds", buIds);
        }
    }
}
```

---

## 7. MIGRACIÓN DEL FRONTEND: JSF/RICHFACES → ANGULAR + PRIMENG

### 7.1 Estructura del Proyecto Angular

```
sophia-web/
├── angular.json
├── package.json
├── tsconfig.json
├── src/
│   ├── main.ts
│   ├── index.html
│   ├── styles.scss                    # Tema PrimeNG + estilos globales
│   ├── environments/
│   │   ├── environment.ts             # API URL dev
│   │   └── environment.prod.ts        # API URL prod
│   │
│   └── app/
│       ├── app.component.ts
│       ├── app.routes.ts              # Lazy-loaded module routes
│       │
│       ├── core/                      # Singleton services
│       │   ├── auth/
│       │   │   ├── auth.service.ts           # Login, JWT management
│       │   │   ├── auth.guard.ts             # Route protection
│       │   │   ├── auth.interceptor.ts       # JWT Bearer header
│       │   │   └── token.service.ts          # JWT decode, refresh
│       │   ├── services/
│       │   │   ├── api.service.ts            # Base HTTP service
│       │   │   ├── notification.service.ts   # Toast messages (reemplazo FacesMessages)
│       │   │   └── tenant.service.ts         # Company/BU context
│       │   ├── interceptors/
│       │   │   ├── error.interceptor.ts      # Global error handling
│       │   │   └── loading.interceptor.ts    # Loading spinner (reemplazo a4j:status)
│       │   └── models/
│       │       ├── page.model.ts             # Paginación genérica
│       │       └── error-response.model.ts
│       │
│       ├── shared/                    # Reusable components
│       │   ├── components/
│       │   │   ├── data-table/               # Wrapper p-table (reemplazo app:dataTable)
│       │   │   ├── select-popup/             # Dialog + table (reemplazo app:selectPopUp)
│       │   │   ├── quick-search/             # AutoComplete (reemplazo app:quickSearch)
│       │   │   ├── report-options/           # Report params (reemplazo app:reportDefaultOptions)
│       │   │   ├── fieldset/                 # Wrapper p-fieldset
│       │   │   ├── barcode-input/            # ZXing-js (reemplazo app:inputBarcode)
│       │   │   └── confirmation-dialog/      # Delete confirmations
│       │   ├── directives/
│       │   │   ├── has-permission.directive.ts  # Reemplazo s:hasPermission
│       │   │   └── number-only.directive.ts
│       │   ├── pipes/
│       │   │   ├── currency-bo.pipe.ts       # Formato moneda boliviana
│       │   │   └── date-bo.pipe.ts           # Formato fecha Bolivia
│       │   └── validators/
│       │       ├── equal.validator.ts        # Reemplazo validateEqual
│       │       ├── date-range.validator.ts
│       │       └── number-range.validator.ts
│       │
│       ├── layout/                    # Reemplazo de template.xhtml
│       │   ├── layout.component.ts           # Template principal
│       │   ├── header/                       # Reemplazo header.xhtml
│       │   ├── menu/                         # Reemplazo menu.xhtml (154KB → Angular menu)
│       │   ├── footer/                       # Reemplazo footer.xhtml
│       │   └── breadcrumb/
│       │
│       └── modules/                   # Un módulo por dominio de negocio
│           ├── admin/
│           │   ├── admin.routes.ts
│           │   ├── companies/
│           │   │   ├── company-list.component.ts    # companyList.xhtml
│           │   │   ├── company-form.component.ts    # company.xhtml
│           │   │   └── company.service.ts
│           │   ├── users/
│           │   └── ...
│           ├── accounting/
│           │   ├── accounting.routes.ts
│           │   ├── vouchers/
│           │   ├── reports/
│           │   │   ├── balance-sheet/
│           │   │   ├── profit-loss/
│           │   │   └── diary-major/
│           │   └── ...
│           ├── employees/
│           ├── finances/
│           ├── warehouse/
│           ├── customers/
│           ├── fixedassets/
│           ├── production/
│           ├── budget/
│           ├── cashbox/
│           ├── purchases/
│           ├── contacts/
│           ├── products/
│           ├── treasury/
│           ├── dashboard/
│           └── ...
```

### 7.2 Ejemplo: Migración de Página XHTML → Angular Component

```html
<!-- ═══════════════════════════════════════════════════ -->
<!-- ANTES: companyList.xhtml (RichFaces)               -->
<!-- ═══════════════════════════════════════════════════ -->
<ui:composition template="/layout/template.xhtml">
    <ui:define name="body">
        <app:fieldset legend="#{messages['Company.search']}">
            <h:form>
                <app:quickSearch model="#{companyDataModel}"
                                criteria="#{companyDataModel.criteria.name}"
                                label="#{messages['Company.name']}"/>

                <rich:dataTable value="#{companyDataModel}"
                                var="companyItem"
                                rows="#{sessionUser.rowsPerPage}">
                    <rich:column>
                        <f:facet name="header">
                            <app:commandSortHeader property="company.name"
                                                   label="#{messages['Company.name']}"/>
                        </f:facet>
                        #{companyItem.name}
                    </rich:column>
                    <rich:column>
                        <f:facet name="header">#{messages['Company.login']}</f:facet>
                        #{companyItem.login}
                    </rich:column>
                    <rich:column>
                        <a4j:commandLink action="#{companyAction.select(companyItem)}"
                                         value="#{messages['Common.select']}"/>
                    </rich:column>
                </rich:dataTable>
                <app:dataScroller/>
            </h:form>
        </app:fieldset>
    </ui:define>
</ui:composition>
```

```typescript
// ═══════════════════════════════════════════════════
// DESPUÉS: company-list.component.ts (Angular + PrimeNG)
// ═══════════════════════════════════════════════════
@Component({
  selector: 'app-company-list',
  standalone: true,
  imports: [
    TableModule, InputTextModule, ButtonModule,
    FieldsetModule, FormsModule, TranslateModule
  ],
  template: `
    <p-fieldset [legend]="'Company.search' | translate">

      <!-- Quick Search -->
      <div class="flex gap-2 mb-3">
        <input pInputText
               [(ngModel)]="searchTerm"
               [placeholder]="'Company.name' | translate"
               (input)="onSearch($event)" />
        <p-button icon="pi pi-search"
                  (onClick)="loadCompanies()" />
      </div>

      <!-- Data Table -->
      <p-table [value]="companies"
               [lazy]="true"
               [paginator]="true"
               [rows]="pageSize"
               [totalRecords]="totalRecords"
               [sortField]="'name'"
               [sortOrder]="1"
               (onLazyLoad)="onLazyLoad($event)"
               [loading]="loading">

        <ng-template pTemplate="header">
          <tr>
            <th pSortableColumn="name">
              {{ 'Company.name' | translate }}
              <p-sortIcon field="name" />
            </th>
            <th pSortableColumn="login">
              {{ 'Company.login' | translate }}
              <p-sortIcon field="login" />
            </th>
            <th>{{ 'Common.actions' | translate }}</th>
          </tr>
        </ng-template>

        <ng-template pTemplate="body" let-company>
          <tr>
            <td>{{ company.name }}</td>
            <td>{{ company.login }}</td>
            <td>
              <p-button icon="pi pi-pencil" [text]="true"
                        [routerLink]="['/admin/companies', company.id]" />
              <p-button icon="pi pi-trash" [text]="true" severity="danger"
                        (onClick)="confirmDelete(company)" />
            </td>
          </tr>
        </ng-template>
      </p-table>
    </p-fieldset>
  `
})
export class CompanyListComponent implements OnInit {
  companies: CompanyDTO[] = [];
  totalRecords = 0;
  pageSize = 20;
  loading = false;
  searchTerm = '';

  private companyService = inject(CompanyService);
  private router = inject(Router);
  private confirmationService = inject(ConfirmationService);
  private messageService = inject(MessageService);

  ngOnInit() {
    this.loadCompanies();
  }

  onLazyLoad(event: TableLazyLoadEvent) {
    const page = (event.first ?? 0) / (event.rows ?? this.pageSize);
    const sort = event.sortField as string ?? 'name';
    const order = event.sortOrder === 1 ? 'ASC' : 'DESC';
    this.loadCompanies(page, sort, order);
  }

  loadCompanies(page = 0, sort = 'name', order = 'ASC') {
    this.loading = true;
    this.companyService.list(page, this.pageSize, this.searchTerm, sort, order)
      .subscribe({
        next: (result) => {
          this.companies = result.content;
          this.totalRecords = result.totalElements;
          this.loading = false;
        },
        error: () => this.loading = false
      });
  }

  onSearch(event: Event) {
    this.loadCompanies();
  }

  confirmDelete(company: CompanyDTO) {
    this.confirmationService.confirm({
      message: `¿Eliminar ${company.name}?`,
      accept: () => {
        this.companyService.delete(company.id).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Eliminado',
              detail: `${company.name} eliminado correctamente`
            });
            this.loadCompanies();
          }
        });
      }
    });
  }
}
```

### 7.3 Mapeo de Navegación: pages.xml → Angular Router

```typescript
// ═══════════════════════════════════════════════════
// ANTES: resources/WEB-INF/admin/pages.xml
// ═══════════════════════════════════════════════════
// <page view-id="/admin/*" login-required="true"/>
// <page view-id="/admin/companyList.xhtml">
//   <navigation from-action="#{companyAction.select(companyItem)}">
//     <rule if-outcome="Success">
//       <redirect view-id="/admin/company.xhtml"/>
//     </rule>
//   </navigation>
// </page>

// ═══════════════════════════════════════════════════
// DESPUÉS: admin.routes.ts (Angular)
// ═══════════════════════════════════════════════════
export const ADMIN_ROUTES: Routes = [
  {
    path: 'admin',
    canActivate: [authGuard],                    // login-required="true"
    canActivateChild: [permissionGuard('ADMIN')], // s:hasPermission
    children: [
      { path: 'companies', component: CompanyListComponent },
      { path: 'companies/new', component: CompanyFormComponent },
      { path: 'companies/:id', component: CompanyFormComponent },  // select → navigate
      { path: 'users', component: UserListComponent },
      { path: 'users/:id', component: UserFormComponent },
      // ...más rutas del módulo admin
    ]
  }
];
```

### 7.4 Mapeo de Mensajes i18n

```typescript
// ═══════════════════════════════════════════════════
// ANTES: messages_app.properties (Seam ResourceBundle)
// #{messages['Company.name']}
// #{messages['Common.info.created']}

// ═══════════════════════════════════════════════════
// DESPUÉS: Angular i18n con ngx-translate
// src/assets/i18n/es.json
{
  "Company": {
    "name": "Nombre de Empresa",
    "login": "Login",
    "search": "Búsqueda de Empresas"
  },
  "Common": {
    "select": "Seleccionar",
    "actions": "Acciones",
    "info": {
      "created": "Registro creado exitosamente",
      "updated": "Registro actualizado exitosamente",
      "deleted": "Registro eliminado exitosamente"
    },
    "error": {
      "duplicated": "El registro ya existe",
      "referentialIntegrity": "No se puede eliminar, existen registros relacionados",
      "concurrency": "El registro fue modificado por otro usuario"
    }
  }
}
// Script de conversión: Claude Code puede convertir messages_app.properties → es.json
```

---

## 8. MIGRACIÓN DE SEGURIDAD

### 8.1 Autenticación: Seam Security → Spring Security + JWT

```java
// ═══════════════════════════════════════════════════
// ANTES: AuthenticatorAction.java (Seam)
// ═══════════════════════════════════════════════════
// @In Credentials credentials;
// @In AppIdentity identity;
// authenticate() → Hash.instance().hash(password) → userService.findByUsernameAndPasswordAndCompany()
// identity.setPermissions(userService.getPermissions(currentUser))

// ═══════════════════════════════════════════════════
// DESPUÉS: Spring Security + JWT
// ═══════════════════════════════════════════════════
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())  // API REST stateless
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api-docs/**", "/swagger-ui/**").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Migración gradual: soportar hash legacy + bcrypt
        return new DelegatingPasswordEncoder("bcrypt", Map.of(
            "bcrypt", new BCryptPasswordEncoder(),
            "legacy", new LegacyPasswordEncoder()  // Hash.instance() legacy
        ));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200")); // Angular dev
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

// Auth Controller (reemplazo de login.xhtml + AuthenticatorAction)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.authenticate(
            request.username(), request.password(), request.companyLogin());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        AuthResponse response = authService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }
}

public record LoginRequest(
    @NotBlank String username,
    @NotBlank String password,
    @NotBlank String companyLogin
) {}

public record AuthResponse(
    String accessToken,
    String refreshToken,
    long expiresIn,
    UserDTO user,
    CompanyDTO company,
    List<String> permissions
) {}
```

### 8.2 Autorización: Seam Permissions → Spring Method Security

```java
// ANTES (XHTML): <s:hasPermission name="COMPANY" action="VIEW">...</s:hasPermission>
// ANTES (Action): identity.hasPermission("COMPANY", "VIEW")

// DESPUÉS (Controller):
@PreAuthorize("hasAuthority('COMPANY_VIEW')")
@GetMapping("/api/v1/companies")
public ResponseEntity<Page<CompanyDTO>> list(...) { ... }

@PreAuthorize("hasAuthority('COMPANY_CREATE')")
@PostMapping("/api/v1/companies")
public ResponseEntity<CompanyDTO> create(...) { ... }

// DESPUÉS (Angular): Directive + Guard
@Directive({ selector: '[appHasPermission]', standalone: true })
export class HasPermissionDirective {
  @Input('appHasPermission') permission!: string;
  private authService = inject(AuthService);

  ngOnInit() {
    if (!this.authService.hasPermission(this.permission)) {
      this.viewContainer.clear(); // Ocultar elemento
    }
  }
}

// Uso en template Angular:
// <p-button *appHasPermission="'COMPANY_CREATE'" label="Nuevo" .../>
```

### 8.3 Migración Gradual de Passwords

```java
// Soportar ambos formatos durante la migración
public class LegacyPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        // Replica Hash.instance().hash() del legacy
        return DigestUtils.sha256Hex(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return encode(rawPassword).equals(encodedPassword);
    }
}

// En el login: si el password es legacy, re-hash con bcrypt
@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public AuthResponse authenticate(String username, String password, String companyLogin) {
        User user = userRepository.findByUsernameAndCompanyLogin(username, companyLogin)
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // Migración gradual: si era hash legacy, actualizar a bcrypt
        if (user.getPassword().length() == 64) { // SHA-256 hex length
            user.setPassword("{bcrypt}" + new BCryptPasswordEncoder().encode(password));
            userRepository.save(user);
        }

        String token = jwtService.generateToken(user);
        // ...
    }
}
```

---

## 9. PLAN DE MIGRACIÓN DE BASE DE DATOS

### 9.1 Estrategia: Preservar Esquema

La base de datos MySQL es el activo más valioso. **NO se modifica el esquema** durante la migración — el nuevo sistema debe trabajar con la misma estructura.

### 9.2 Pasos

```
FASE 1: Setup
├── Instalar MySQL 8.4 LTS (compatibilidad con esquema existente)
├── Configurar Flyway para versionamiento
├── Exportar esquema actual como baseline (V1__baseline.sql)
├── Configurar Hibernate 6.x con validate (no auto-DDL)
└── Verificar mapeo de todas las 671 entidades

FASE 2: Migración de Entidades
├── Renombrar javax.persistence.* → jakarta.persistence.*
├── Actualizar @Filter/@FilterDef para Hibernate 6.x
├── Migrar @TableGenerator sequences (seqKhipus)
├── Migrar @EmbeddedId / composite keys
├── Migrar @Type (StringBooleanUserType → custom AttributeConverter)
├── Migrar Hibernate Envers annotations
└── Verificar compatibilidad con tablas _AUD existentes

FASE 3: Multi-tenancy
├── Reimplementar companyFilter con Hibernate 6 @FilterDef
├── Reimplementar businessUnitFilter
├── Configurar EntityManager con filtros automáticos
└── Tests de aislamiento de datos entre companies

FASE 4: Optimización (post-migración)
├── Evaluar migración a PostgreSQL (opcional)
├── Agregar índices faltantes
├── Optimizar queries N+1 detectadas
├── Implementar cache de segundo nivel (Redis)
└── Considerar read replicas para reportes
```

### 9.3 Mapeo de Tipos Hibernate

| Hibernate 3.x | Hibernate 6.x / Jakarta |
|----------------|-------------------------|
| `@Type(type="com...StringBooleanUserType")` | `@Convert(converter=StringBooleanConverter.class)` |
| `@GenericGenerator` (TABLE) | `@TableGenerator` (estándar JPA) |
| `@org.hibernate.annotations.Entity` | Removido (usar `@jakarta.persistence.Entity`) |
| `@TypeDef` | `@ConverterRegistration` |
| `org.hibernate.dialect.MySQLDialect` | `org.hibernate.dialect.MySQLDialect` (auto-detectado) |
| `javax.persistence.Column` | `jakarta.persistence.Column` |
| `javax.persistence.ManyToOne` | `jakarta.persistence.ManyToOne` |
| `org.hibernate.annotations.Filter` | `org.hibernate.annotations.Filter` (sin cambio) |
| `org.hibernate.annotations.FilterDef` | `org.hibernate.annotations.FilterDef` (sin cambio) |
| `@RevisionEntity` (Envers) | `@RevisionEntity` (mismo paquete Envers) |

### 9.4 Ejemplo de Migración de Entidad

```java
// ═══════════════════════════════════════════════════
// ANTES: Company.java (JPA 1.0, Hibernate 3.3)
// ═══════════════════════════════════════════════════
@TableGenerator(name = "Company_Generator",
    table = "seqKhipus", pkColumnName = "TABLE_SEQ",
    valueColumnName = "NEXT_VAL", pkColumnValue = "empresa",
    allocationSize = 1)
@Entity
@Table(name = "empresa")
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
public class Company implements BaseModel {

    @Id
    @Column(name = "idempresa")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Company_Generator")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 200)
    private String name;

    @Column(name = "login", nullable = false, unique = true, length = 50)
    private String login;

    @Type(type = "com.encens.khipus.model.usertype.StringBooleanUserType",
          parameters = {@Parameter(name = "true", value = "SI"),
                        @Parameter(name = "false", value = "NO")})
    @Column(name = "activo", nullable = false, length = 2)
    private Boolean active;

    @Version
    @Column(name = "version")
    private Long version;

    // getters/setters...
}

// ═══════════════════════════════════════════════════
// DESPUÉS: Company.java (JPA 3.1, Hibernate 6.6)
// ═══════════════════════════════════════════════════
@Entity
@Table(name = "empresa")
@FilterDef(name = "companyFilter",
           parameters = @ParamDef(name = "currentCompanyId", type = Long.class))
@Filter(name = "companyFilter", condition = "idempresa = :currentCompanyId")
@EntityListeners(AuditingEntityListener.class)
@Audited  // Hibernate Envers
@Getter @Setter @NoArgsConstructor
public class Company implements BaseModel {

    @Id
    @Column(name = "idempresa")
    @TableGenerator(name = "Company_Generator",
        table = "seqKhipus", pkColumnName = "TABLE_SEQ",
        valueColumnName = "NEXT_VAL", pkColumnValue = "empresa",
        allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Company_Generator")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 200)
    private String name;

    @Column(name = "login", nullable = false, unique = true, length = 50)
    private String login;

    @Convert(converter = StringBooleanConverter.class)
    @Column(name = "activo", nullable = false, length = 2)
    private Boolean active;

    @Version
    @Column(name = "version")
    private Long version;
}

// AttributeConverter (reemplazo de StringBooleanUserType)
@Converter
public class StringBooleanConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean value) {
        return Boolean.TRUE.equals(value) ? "SI" : "NO";
    }

    @Override
    public Boolean convertToEntityAttribute(String value) {
        return "SI".equals(value);
    }
}
```

---

## 10. MIGRACIÓN DE REPORTES

### 10.1 Estrategia

Los 266 archivos `.jrxml` representan lógica de negocio valiosa. La estrategia es **actualizar el engine, preservar los reportes**.

### 10.2 Plan

```
FASE 1: Compatibilidad
├── Actualizar JasperReports 3.7.4 → 7.x
├── Migrar JRXML schema version (actualizar namespace)
├── Verificar compilación de los 266 reportes
├── Resolver incompatibilidades de API
└── Actualizar dependencias: iText → OpenPDF, POI 5.x

FASE 2: Integración Spring Boot
├── Crear ReportService genérico
├── REST endpoints para generación de reportes
├── Streaming de PDF/Excel al frontend Angular
├── Cacheo de reportes compilados (.jasper)
└── Configurar fonts y recursos de reportes

FASE 3: Frontend Angular
├── Componente ReportViewerComponent (PDF inline con pdf.js)
├── Botones de exportación (PDF, Excel, CSV)
├── Formulario de parámetros de reporte
└── Integración con el módulo de reportes de cada dominio

FASE 4: Mejoras (post-migración)
├── Agregar exportación a nuevos formatos (CSV, JSON)
├── Considerar reportes interactivos (Dashboard BI)
├── Evaluar alternativas modernas (DynamicReports)
└── Templates HTML para reportes simples
```

### 10.3 Integración Spring Boot + Angular

```java
// Backend: ReportService genérico
@Service
@RequiredArgsConstructor
public class ReportService {

    @Value("classpath:reports/")
    private Resource reportsDir;

    public byte[] generatePdf(String reportName, Map<String, Object> parameters,
                              JRDataSource dataSource) throws JRException {
        InputStream jrxml = getClass().getResourceAsStream("/reports/" + reportName + ".jrxml");
        JasperReport report = JasperCompileManager.compileReport(jrxml);
        JasperPrint print = JasperFillManager.fillReport(report, parameters, dataSource);
        return JasperExportManager.exportReportToPdf(print);
    }

    public byte[] generateExcel(String reportName, Map<String, Object> parameters,
                                JRDataSource dataSource) throws JRException {
        // ... similar con JRXlsxExporter
    }
}

// Backend: REST Controller para reportes
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/{module}/{reportName}")
    public ResponseEntity<byte[]> generate(
            @PathVariable String module,
            @PathVariable String reportName,
            @RequestBody Map<String, Object> parameters,
            @RequestParam(defaultValue = "pdf") String format) throws JRException {

        byte[] content = switch (format) {
            case "pdf" -> reportService.generatePdf(module + "/" + reportName, parameters, null);
            case "excel" -> reportService.generateExcel(module + "/" + reportName, parameters, null);
            default -> throw new IllegalArgumentException("Formato no soportado: " + format);
        };

        String contentType = format.equals("pdf") ? "application/pdf"
            : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=" + reportName + "." + format)
            .body(content);
    }
}
```

```typescript
// Frontend Angular: Report Viewer
@Component({
  selector: 'app-report-viewer',
  template: `
    <div class="report-toolbar">
      <p-button icon="pi pi-file-pdf" label="PDF"
                (onClick)="generate('pdf')" [loading]="loading" />
      <p-button icon="pi pi-file-excel" label="Excel"
                (onClick)="generate('excel')" [loading]="loading" />
    </div>
    <iframe *ngIf="pdfUrl" [src]="pdfUrl" class="report-frame"></iframe>
  `
})
export class ReportViewerComponent {
  @Input() module!: string;
  @Input() reportName!: string;
  @Input() parameters: Record<string, any> = {};

  pdfUrl: SafeResourceUrl | null = null;
  loading = false;

  private reportService = inject(ReportService);
  private sanitizer = inject(DomSanitizer);

  generate(format: string) {
    this.loading = true;
    this.reportService.generate(this.module, this.reportName, this.parameters, format)
      .subscribe({
        next: (blob) => {
          if (format === 'pdf') {
            const url = URL.createObjectURL(blob);
            this.pdfUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
          } else {
            saveAs(blob, `${this.reportName}.xlsx`);
          }
          this.loading = false;
        },
        error: () => this.loading = false
      });
  }
}
```

### 10.4 Mapeo de Librerías

| Actual | Nuevo | Nota |
|--------|-------|------|
| JasperReports 3.7.4 | JasperReports 7.x | Actualizar JRXML schema |
| iText 2.1.7 | OpenPDF 2.x | Fork open-source de iText |
| POI 3.5 | POI 5.x | API compatible |
| JFreeChart 1.0.12 | JFreeChart 1.5.x | Compatible |

---

## 11. ESTRATEGIA DE MIGRACIÓN INCREMENTAL (STRANGLER FIG)

### 11.1 Principio

**NO** se hace una reescritura completa ("big bang"). Se usa el patrón **Strangler Fig**:
- El sistema nuevo crece alrededor del viejo
- Los módulos se migran uno a uno
- Ambos sistemas coexisten durante la migración
- El tráfico se redirige gradualmente

```
FASE 1: Coexistencia
┌──────────────────────┐     ┌──────────────────────┐
│   SISK13 LEGACY      │     │   SOPHIA 7.0     │
│   (JBoss/Seam)       │     │   (Spring Boot +     │
│                      │     │    Angular)           │
│ ┌──────────────────┐ │     │ ┌──────────────────┐ │
│ │ Módulos NO       │ │     │ │ Módulos YA       │ │
│ │ migrados         │ │     │ │ migrados         │ │
│ └──────────────────┘ │     │ └──────────────────┘ │
└──────────┬───────────┘     └──────────┬───────────┘
           │                            │
           └─────────┬──────────────────┘
                     ▼
              ┌──────────────┐
              │   MySQL DB   │
              │   (compartida│
              │    ambos)    │
              └──────────────┘

FASE FINAL: Migración completa
┌──────────────────────────────┐
│       SOPHIA 7.0         │
│   Todos los módulos migrados │
│   Legacy apagado             │
└──────────────────────────────┘
```

### 11.2 Coexistencia Técnica: Reverse Proxy

```
                    ┌──────────────────────┐
                    │     Nginx Proxy       │
                    │  (Reverse Proxy)      │
                    └──────┬───────────────┘
                           │
              ┌────────────┼────────────────┐
              │            │                │
   /admin/*   │  /legacy/* │   /api/*       │
   /accounting│            │   /assets/*    │
              ▼            ▼                ▼
     ┌────────────┐ ┌────────────┐ ┌────────────┐
     │  Angular   │ │ JBoss/Seam │ │ Spring Boot│
     │  (Nginx)   │ │  Legacy    │ │  API       │
     │  :4200     │ │  :8080     │ │  :8081     │
     └────────────┘ └────────────┘ └────────────┘
              │            │                │
              └────────────┼────────────────┘
                           ▼
                    ┌──────────────┐
                    │   MySQL DB   │
                    └──────────────┘
```

### 11.3 Orden de Migración de Módulos

El orden se basa en: **menor complejidad primero**, dependencias entre módulos, y valor de negocio.

```
WAVE 1 — Fundación (semanas 1-8)
├── [1] Admin (20 entidades, 22 vistas) — módulo base, autenticación
├── [2] Common (5 entidades, 1 vista) — utilidades compartidas
└── [3] Products (4 entidades, 9 vistas) — catálogo simple

WAVE 2 — Módulos Independientes (semanas 9-16)
├── [4] Contacts (22 entidades, 27 vistas)
├── [5] Treasury (1 entidad, 3 vistas)
├── [6] Budget (12 entidades, 19 vistas)
└── [7] Purchases (17 entidades, 7 vistas)

WAVE 3 — Módulos de Negocio Core (semanas 17-28)
├── [8] Warehouse (51 entidades, 83 vistas, 39 reportes)
├── [9] Customers (71 entidades, 76 vistas, 23 reportes)
├── [10] Fixed Assets (35 entidades, 67 vistas, 36 reportes)
└── [11] Cashbox (4 entidades, 9 vistas, 11 reportes)

WAVE 4 — Módulos Complejos (semanas 29-40)
├── [12] Accounting (2 entidades, 33 vistas, 28 reportes)
├── [13] Finances (139 entidades, 78 vistas, 25 reportes)
├── [14] Production (83 entidades, 102 vistas, 33 reportes)
└── [15] XProduction (15 entidades, 20 vistas)

WAVE 5 — Módulo Más Complejo (semanas 41-52)
├── [16] Employees (140 entidades, 187 vistas, 67 reportes)
│   ├── Planilla (40+ calculadoras tributarias)
│   ├── RRHH
│   └── Control de asistencia
└── [17] Dashboard (6 entidades, 49 vistas)

WAVE 6 — Finalización (semanas 53-60)
├── [18] Integración WISE
├── [19] REST API existente → merge con nueva API
├── [20] Académicos
└── [21] Cleanup y decommission legacy
```

### 11.4 Criterios de Migración por Módulo

Cada módulo migrado debe cumplir:

- [ ] Todas las entidades JPA migradas y mapeadas
- [ ] Todos los servicios migrados con lógica de negocio intacta
- [ ] API REST documentada con OpenAPI
- [ ] Todas las vistas Angular recreadas con funcionalidad equivalente
- [ ] Todos los reportes funcionando (PDF/Excel)
- [ ] Multi-tenancy (company/businessUnit filters) funcionando
- [ ] Auditoría Envers funcionando y compatible con tablas _AUD existentes
- [ ] Tests unitarios y de integración escritos (mínimo 80% cobertura)
- [ ] Validación funcional con usuarios finales
- [ ] Performance igual o mejor que el legacy

---

## 12. USO DE CLAUDE CODE EN LA MIGRACIÓN

### 12.1 Estrategia de Aceleración con Claude Code

Claude Code es la herramienta principal para acelerar esta migración. Su soporte para Spring Boot y Angular es excepcional.

### 12.2 Tareas Automatizables con Claude Code

#### A) Migración de Entidades JPA (Alta automatización ~85%)

```
Prompt tipo para Claude Code:
"Migra la entidad [Módulo]/[Entidad].java de javax.persistence a jakarta.persistence,
actualiza los @Type a @Convert, y genera el AttributeConverter si es necesario.
Mantén las anotaciones @Filter y @Audited. Genera un test de integración.
Genera también el Spring Data JPA Repository."
```

Claude Code puede:
- Convertir `javax.*` → `jakarta.*` automáticamente en batch
- Generar `AttributeConverter` para cada `@Type` custom
- Generar Spring Data JPA Repositories para cada entidad
- Generar DTOs a partir de entidades
- Generar mappers (MapStruct) entre entidades y DTOs

#### B) Migración de Servicios (Alta automatización ~80%)

```
Prompt tipo:
"Convierte EntityServiceBean.java de EJB Stateless a Spring @Service.
Reemplaza @In por constructor injection, @TransactionAttribute por @Transactional.
Mantén la lógica de negocio idéntica. Genera tests unitarios con Mockito."
```

Claude Code puede:
- Convertir EJB → Spring Service automáticamente
- Reemplazar patrones de inyección Seam → Spring
- Generar tests unitarios para cada servicio
- Identificar y refactorizar anti-patterns

#### C) Generación de API REST (Media automatización ~70%)

```
Prompt tipo:
"Basándote en EntityAction.java (Seam Action), genera un REST Controller
Spring Boot equivalente con DTOs, validación, y manejo de errores.
Incluye OpenAPI/Swagger annotations."
```

Claude Code puede:
- Analizar cada Action y generar el Controller REST equivalente
- Generar DTOs con validación (`@Valid`, `@NotNull`, etc.)
- Generar documentación OpenAPI
- Generar tests de integración con MockMvc

#### D) Migración de Vistas XHTML → Angular (Media automatización ~60%)

```
Prompt tipo:
"Analiza esta página XHTML con RichFaces y genera el componente Angular + PrimeNG
equivalente. Mapea rich:dataTable a p-table, rich:calendar a p-calendar,
rich:modalPanel a p-dialog. Genera el service HTTP y el routing."
```

Claude Code puede:
- Analizar XHTML y generar componentes Angular equivalentes
- Mapear componentes RichFaces → PrimeNG
- Generar formularios con validación reactive forms
- Generar servicios HTTP y modelos TypeScript
- **Limitación**: Requiere revisión humana para UX y flujos complejos

#### E) Migración de Reportes (Media automatización ~65%)

```
Prompt tipo:
"Actualiza este JRXML de JasperReports 3.7 a 7.x.
Genera el ReportService y el endpoint REST en Spring Boot.
Genera el componente Angular ReportViewer para este reporte."
```

#### F) Generación de Tests (Alta automatización ~90%)

```
Prompt tipo:
"Genera tests de integración con TestContainers + MySQL para EntityService.
Incluye tests para CRUD, validaciones, multi-tenancy filters, y Envers auditing.
Genera también tests de API con MockMvc para EntityController."
```

#### G) Conversión de i18n (Alta automatización ~95%)

```
Prompt tipo:
"Convierte messages_app.properties (formato Java properties, 528 KB) a
src/assets/i18n/es.json (formato JSON anidado para ngx-translate).
Agrupa las keys por módulo (Company.*, Employee.*, Common.*, etc.)"
```

### 12.3 Workflow Recomendado con Claude Code

```
Para CADA módulo:

1. ANÁLISIS (Claude Code)
   claude> "Analiza el módulo [X] completo: entidades, servicios, acciones, vistas.
            Identifica dependencias con otros módulos y complejidad."

2. ENTIDADES (Claude Code — batch)
   claude> "Migra todas las entidades del módulo [X] a Jakarta Persistence.
            Genera repositories Spring Data JPA para cada una.
            Genera DTOs y MapStruct mappers."

3. SERVICIOS (Claude Code — batch)
   claude> "Convierte todos los ServiceBeans del módulo [X] a Spring @Service.
            Mantén la lógica de negocio idéntica. Genera tests con Mockito."

4. CONTROLLERS (Claude Code — uno a uno)
   claude> "Genera el REST Controller para [Entidad]Action.java
            con DTOs, validación, OpenAPI docs, y manejo de errores.
            Genera tests MockMvc."

5. ANGULAR SERVICES (Claude Code — batch)
   claude> "Genera los servicios HTTP Angular para todos los endpoints
            del módulo [X]. Genera interfaces TypeScript para los DTOs."

6. VISTAS (Claude Code + humano)
   claude> "Genera el componente Angular para [vista].xhtml con PrimeNG"
   humano> Revisa UX, ajusta diseño, prueba flujos

7. REPORTES (Claude Code)
   claude> "Actualiza los JRXML del módulo [X], genera endpoints y viewers."

8. TESTS (Claude Code)
   claude> "Genera tests de integración para todo el módulo [X]."

9. VALIDACIÓN (humano)
   - Test funcional con usuarios
   - Comparar resultados con sistema legacy
   - Deploy a staging
```

### 12.4 Estimación de Aceleración

| Tarea | Sin Claude Code | Con Claude Code | Aceleración |
|-------|----------------|-----------------|-------------|
| Migrar 1 entidad + repo + DTO | 2-4 horas | 15-30 min | **5-8x** |
| Migrar 1 servicio + tests | 4-8 horas | 30-60 min | **6-8x** |
| Generar 1 controller + tests | 4-6 horas | 30-45 min | **6-8x** |
| Generar 1 vista Angular | 8-16 horas | 2-4 horas | **3-4x** |
| Generar tests/módulo | 2-3 días | 4-8 horas | **3-4x** |
| Convertir i18n (528 KB) | 2-3 días | 1-2 horas | **10x+** |
| **Migración total** | **~58 semanas** | **~22-30 semanas** | **~2-2.5x** |

### 12.5 CLAUDE.md para el Proyecto Nuevo

Crear un `CLAUDE.md` en el proyecto nuevo con instrucciones específicas:

```markdown
# CLAUDE.md — Sophia 7.0

## Contexto
Sophia 7.0 — migración de SISK13/KHIPUS (Seam 2.2/JBoss) a stack moderno.
Base de datos MySQL existente — NO modificar esquema.
Proyecto legacy en: D:/Intellij/sisk13 (referencia para lógica de negocio).

## Stack
- Backend: Spring Boot 3.4, Java 21, Maven, Spring Data JPA, Spring Security + JWT
- Frontend: Angular 19, PrimeNG 18, TypeScript 5.6, SCSS
- DB: MySQL 8.4 (esquema existente "khipus")
- Reports: JasperReports 7.x (JRXML migrados del legacy)
- Testing: JUnit 5 + Mockito + TestContainers (backend), Jasmine + Karma (frontend)

## Reglas de Migración
- javax.persistence.* → jakarta.persistence.*
- @In → Constructor injection (@RequiredArgsConstructor)
- @Stateless → @Service @Transactional
- GenericServiceBean → Service + Repository pattern
- @Filter multi-tenancy DEBE preservarse (companyFilter, businessUnitFilter)
- Hibernate Envers DEBE ser compatible con tablas _AUD existentes
- Lógica de negocio DEBE ser idéntica al legacy
- @Type(StringBooleanUserType) → @Convert(StringBooleanConverter.class)
- Seam @Name → Spring @Service/@Component (auto-detected)
- FacesMessages → ResponseEntity con ErrorResponse
- pages.xml navigation → Angular Router con guards

## Convenciones Backend
- Controllers en controller/{module}/
- Services en service/{module}/
- DTOs en dto/{module}/ (records de Java)
- Mappers con MapStruct en mapper/{module}/
- Repositories en repository/{module}/
- Tests en src/test correspondiente al paquete
- Validación con Bean Validation 3.0 (@Valid, @NotNull, etc.)
- Documentación con OpenAPI 3.1 (@Tag, @Operation, @Schema)
- Excepciones manejadas por GlobalExceptionHandler

## Convenciones Frontend
- Standalone components (no NgModules)
- Lazy-loaded routes por módulo
- PrimeNG para componentes UI
- ngx-translate para i18n
- Reactive Forms para formularios complejos
- Services con HttpClient + RxJS
- Interceptors para JWT y error handling

## Build y Test
- Backend: mvn clean test / mvn spring-boot:run
- Frontend: ng serve / ng test / ng build --configuration=production
- Docker: docker-compose up -d (MySQL + Redis + backend + frontend)
```

---

## 13. CRONOGRAMA Y FASES

### 13.1 Cronograma General (con Claude Code)

```
MES 1-2: FUNDACIÓN
├── Semana 1-2: Setup proyecto Spring Boot + Angular
│   ├── Estructura de proyecto (multi-module Maven)
│   ├── Docker Compose (MySQL 8.4, Redis 7)
│   ├── CI/CD pipeline (GitHub Actions)
│   ├── Spring Security + JWT base
│   ├── Angular base + PrimeNG theme + layout
│   ├── CORS + proxy config
│   └── Flyway baseline migration
│
├── Semana 3-4: Framework base
│   ├── Multi-tenancy filters config (Hibernate 6)
│   ├── Envers config compatible con tablas _AUD
│   ├── GlobalExceptionHandler
│   ├── StringBooleanConverter + otros converters
│   ├── Configuración i18n (convertir messages_app.properties)
│   ├── Angular shared components (data-table, select-popup, etc.)
│   └── Auth flow completo (login → JWT → refresh)
│
├── Semana 5-8: Wave 1 — Admin + Common + Products
│   ├── Migrar entidades, servicios, controllers, repos
│   ├── Migrar vistas Angular
│   ├── Tests de integración
│   └── VALIDACIÓN con usuarios

MES 3-4: MÓDULOS INDEPENDIENTES
├── Semana 9-12: Wave 2a — Contacts + Treasury
├── Semana 13-16: Wave 2b — Budget + Purchases
└── VALIDACIÓN con usuarios

MES 5-7: CORE DE NEGOCIO
├── Semana 17-20: Wave 3a — Warehouse (83 vistas, 39 reportes)
├── Semana 21-24: Wave 3b — Customers (76 vistas, 23 reportes)
├── Semana 25-28: Wave 3c — Fixed Assets + Cashbox
└── VALIDACIÓN con usuarios

MES 8-10: MÓDULOS COMPLEJOS
├── Semana 29-32: Wave 4a — Accounting (28 reportes)
├── Semana 33-36: Wave 4b — Finances (139 entidades)
├── Semana 37-40: Wave 4c — Production + XProduction
└── VALIDACIÓN con usuarios

MES 11-13: EMPLOYEES (MÁS COMPLEJO)
├── Semana 41-44: Entidades + Servicios Employee
│   ├── 40+ calculadoras de planilla
│   ├── Lógica tributaria/fiscal
│   └── Control de asistencia
├── Semana 45-48: Vistas Employee (187 páginas)
├── Semana 49-52: Reportes Employee (67 reportes) + Dashboard
└── VALIDACIÓN exhaustiva con usuarios

MES 14-15: FINALIZACIÓN
├── Semana 53-54: Integraciones (WISE, billing, email)
├── Semana 55-56: Performance testing + optimization
├── Semana 57-58: Migration dry-run + data validation
├── Semana 59-60: Go-live + monitoring
└── Decommission legacy
```

### 13.2 Hitos Clave

| Hito | Semana | Criterio de Éxito |
|------|--------|-------------------|
| **M1: Foundation ready** | 4 | Proyecto base compilando, CI/CD, auth, layout Angular |
| **M2: First module live** | 8 | Admin + Products migrados y validados |
| **M3: 30% migrado** | 16 | 7 módulos simples migrados y en producción |
| **M4: Core business live** | 28 | Warehouse, Customers, Fixed Assets migrados |
| **M5: 80% migrado** | 40 | Accounting, Finances, Production migrados |
| **M6: Feature complete** | 52 | Employees (el más complejo) migrado |
| **M7: Go-live** | 60 | Todos los módulos validados, legacy apagado |

### 13.3 Equipo Recomendado

| Rol | Cantidad | Responsabilidad |
|-----|----------|-----------------|
| Tech Lead / Arquitecto | 1 | Decisiones técnicas, code review, Claude Code senior |
| Backend Developer (Java) | 1-2 | Migración servicios + API REST con Claude Code |
| Frontend Developer (Angular) | 1 | Migración vistas Angular + PrimeNG con Claude Code |
| QA / Tester | 1 | Validación funcional, regression testing |
| **Total mínimo** | **3-4** | |

---

## 14. RIESGOS Y MITIGACIÓN

### 14.1 Matriz de Riesgos

| # | Riesgo | Probabilidad | Impacto | Mitigación |
|---|--------|-------------|---------|------------|
| R1 | Lógica de negocio perdida en migración | Alta | Crítico | Tests de regresión exhaustivos, comparar outputs con legacy |
| R2 | Incompatibilidad de datos con esquema existente | Media | Crítico | `hibernate.hbm2ddl.auto=validate`, NO modificar DDL |
| R3 | Tablas _AUD incompatibles con Envers 6 | Media | Alto | Prueba temprana en semana 2, puede requerir migration script |
| R4 | Reportes JRXML incompatibles con JasperReports 7 | Media | Alto | Compilar los 266 JRXML en semana 3, corregir temprano |
| R5 | Performance degradada | Baja | Alto | Benchmarks comparativos por módulo |
| R6 | Resistencia de usuarios al cambio de UI | Alta | Medio | Mantener flujos similares, capacitación, feedback temprano |
| R7 | Multi-tenancy rota | Baja | Crítico | Tests de aislamiento por empresa en cada módulo |
| R8 | 40+ calculadoras de planilla con errores | Media | Crítico | Test exhaustivo con datos reales, comparar cálculos centavo a centavo |
| R9 | Scope creep (agregar features durante migración) | Alta | Alto | Disciplina: migrar 1:1, mejoras DESPUÉS |
| R10 | Tiempo insuficiente | Media | Alto | Strangler Fig permite valor incremental |
| R11 | CORS / JWT mal configurado | Baja | Medio | Tests E2E tempranos, Postman collection compartida |
| R12 | Angular bundle size excesivo | Media | Bajo | Lazy loading por módulo, tree-shaking, budgets en angular.json |

### 14.2 Estrategia de Rollback

```
CADA módulo migrado tiene rollback independiente:

1. Nginx reverse proxy dirige tráfico al módulo nuevo
2. Si falla → cambiar ruta en nginx.conf al módulo legacy (< 5 min)
3. Ambos sistemas comparten la misma BD MySQL
4. Rollback no requiere rollback de datos

Excepción: Si se modifica el esquema (post-migración),
el rollback requiere migration reversa con Flyway.
```

### 14.3 Criterios de Go/No-Go por Módulo

Antes de poner en producción cada módulo migrado:

- [ ] 100% de funcionalidades del módulo legacy replicadas
- [ ] Tests de integración pasando (mínimo 80% cobertura)
- [ ] Tests E2E con Cypress/Playwright para flujos críticos
- [ ] Performance ≤ 110% del tiempo de respuesta legacy
- [ ] Multi-tenancy verificada con datos de al menos 2 empresas
- [ ] Auditoría Envers generando registros compatibles
- [ ] Reportes generando PDFs idénticos al legacy
- [ ] API documentada en Swagger UI
- [ ] Validación funcional aprobada por usuario clave
- [ ] Rollback plan probado en staging

---

## APÉNDICE A: ESTRUCTURA DEL PROYECTO NUEVO

```
sophia/
├── pom.xml                                # Parent POM (multi-module)
├── CLAUDE.md                              # Instrucciones para Claude Code
├── docker-compose.yml                     # MySQL 8.4 + Redis 7 + Nginx
├── .github/
│   └── workflows/
│       ├── ci.yml                         # Build + test en cada push
│       └── deploy.yml                     # Deploy a staging/prod
│
├── sophia-api/                            # ══ Backend Spring Boot ══
│   ├── pom.xml
│   ├── src/main/java/net/encens/sophia/
│   │   ├── SophiaApplication.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java        # Spring Security + JWT
│   │   │   ├── JpaConfig.java             # Hibernate + Envers
│   │   │   ├── MultiTenancyConfig.java    # Filtros company/BU
│   │   │   ├── CorsConfig.java            # CORS para Angular
│   │   │   ├── CacheConfig.java           # Redis cache
│   │   │   ├── OpenApiConfig.java         # Swagger UI
│   │   │   └── JasperReportsConfig.java   # Reportes
│   │   ├── model/                         # Entidades JPA (migradas)
│   │   │   ├── BaseModel.java
│   │   │   ├── admin/
│   │   │   ├── accounting/
│   │   │   ├── employees/
│   │   │   ├── finances/
│   │   │   ├── warehouse/
│   │   │   ├── customers/
│   │   │   ├── common/
│   │   │   │   ├── RevisionEntityInfo.java
│   │   │   │   └── StringBooleanConverter.java
│   │   │   └── ...
│   │   ├── repository/                    # Spring Data JPA
│   │   │   ├── admin/
│   │   │   │   ├── CompanyRepository.java
│   │   │   │   ├── UserRepository.java
│   │   │   │   └── CompanySpecifications.java
│   │   │   └── ...
│   │   ├── service/                       # Lógica de negocio
│   │   │   ├── admin/
│   │   │   │   ├── CompanyService.java
│   │   │   │   └── CompanyServiceImpl.java
│   │   │   └── ...
│   │   ├── controller/                    # REST Controllers
│   │   │   ├── admin/
│   │   │   │   └── CompanyController.java
│   │   │   └── ...
│   │   ├── dto/                           # Data Transfer Objects (records)
│   │   │   ├── admin/
│   │   │   │   ├── CompanyDTO.java
│   │   │   │   ├── CreateCompanyRequest.java
│   │   │   │   └── UpdateCompanyRequest.java
│   │   │   └── ...
│   │   ├── mapper/                        # MapStruct mappers
│   │   │   ├── admin/
│   │   │   │   └── CompanyMapper.java
│   │   │   └── ...
│   │   ├── exception/                     # Exception handlers
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── EntityNotFoundException.java
│   │   │   ├── DuplicateEntryException.java
│   │   │   ├── ReferentialIntegrityException.java
│   │   │   ├── ConcurrencyException.java
│   │   │   └── ErrorResponse.java
│   │   ├── security/                      # Auth, JWT, filters
│   │   │   ├── JwtService.java
│   │   │   ├── JwtAuthFilter.java
│   │   │   ├── AuthController.java
│   │   │   ├── AuthService.java
│   │   │   ├── TenantContextHolder.java
│   │   │   ├── TenantFilter.java
│   │   │   └── LegacyPasswordEncoder.java
│   │   ├── report/                        # Reportes
│   │   │   ├── ReportService.java
│   │   │   └── ReportController.java
│   │   └── util/                          # Utilidades migradas
│   │       ├── BigDecimalUtil.java        # Mantener (lógica financiera)
│   │       ├── MoneyUtil.java
│   │       └── ...
│   │
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-dev.yml
│   │   ├── application-prod.yml
│   │   ├── messages_app.properties        # i18n backend (validación)
│   │   ├── db/migration/                  # Flyway
│   │   │   └── V1__baseline.sql
│   │   └── reports/                       # JRXML migrados
│   │       ├── accounting/
│   │       ├── employees/
│   │       └── ...
│   │
│   └── src/test/java/net/encens/sophia/
│       ├── service/                       # Tests de servicio (Mockito)
│       ├── controller/                    # Tests de API (MockMvc)
│       ├── repository/                    # Tests de repo (TestContainers)
│       └── integration/                   # Tests E2E de módulo
│
├── sophia-web/                            # ══ Frontend Angular ══
│   ├── angular.json
│   ├── package.json
│   ├── tsconfig.json
│   ├── src/
│   │   ├── main.ts
│   │   ├── index.html
│   │   ├── styles.scss
│   │   ├── assets/
│   │   │   ├── i18n/
│   │   │   │   └── es.json               # Traducción convertida de properties
│   │   │   └── img/                       # Imágenes migradas
│   │   ├── environments/
│   │   └── app/
│   │       ├── app.routes.ts
│   │       ├── core/                      # Auth, interceptors, guards
│   │       ├── shared/                    # Componentes reutilizables
│   │       ├── layout/                    # Template, menu, header, footer
│   │       └── modules/                   # Un módulo lazy-loaded por dominio
│   │           ├── admin/
│   │           ├── accounting/
│   │           ├── employees/
│   │           ├── finances/
│   │           ├── warehouse/
│   │           ├── customers/
│   │           ├── fixedassets/
│   │           ├── production/
│   │           ├── budget/
│   │           ├── cashbox/
│   │           ├── purchases/
│   │           ├── contacts/
│   │           ├── products/
│   │           ├── treasury/
│   │           └── dashboard/
│   └── e2e/                               # Tests E2E (Cypress/Playwright)
│
├── nginx/
│   └── nginx.conf                         # Reverse proxy config
│
└── docs/
    ├── MIGRATION.md                       # Este documento
    └── API.md                             # Documentación adicional de API
```

---

## APÉNDICE B: CHECKLIST PRE-MIGRACIÓN

Antes de iniciar la migración:

- [ ] Backup completo de base de datos MySQL
- [ ] Backup completo de código fuente (tag en git: `v6.0.41-pre-migration`)
- [ ] Documentar todos los procesos de negocio críticos con usuarios
- [ ] Documentar todas las integraciones externas (WISE, billing, email)
- [ ] Inventariar reportes más usados (priorizar migración)
- [ ] Definir entorno de staging para el sistema nuevo
- [ ] Configurar CI/CD pipeline (GitHub Actions)
- [ ] Definir equipo y asignar responsabilidades
- [ ] Capacitación básica en Spring Boot / Angular (si necesario)
- [ ] Configurar Claude Code con CLAUDE.md del proyecto nuevo
- [ ] Setup Docker + Docker Compose para desarrollo local
- [ ] Definir métricas de éxito por módulo
- [ ] Crear Postman collection base para testing de API
- [ ] Configurar SonarQube o similar para calidad de código

---

## APÉNDICE C: COMANDOS CLAUDE CODE FRECUENTES

```bash
# ── ANÁLISIS ──
claude "Analiza el módulo employees: cuenta entidades, servicios, acciones,
        reportes. Lista las dependencias con otros módulos."

# ── BACKEND: ENTIDADES ──
claude "Migra todas las entidades en src/main/com/encens/khipus/model/admin/
        de javax.persistence a jakarta.persistence. Genera Spring Data
        repositories y MapStruct mappers para cada una."

# ── BACKEND: SERVICIO ──
claude "Convierte AdminServiceBean.java (EJB Stateless) a Spring @Service.
        Reemplaza @In por constructor injection. Genera tests con Mockito."

# ── BACKEND: CONTROLLER ──
claude "Basándote en CompanyAction.java, genera CompanyController.java (REST)
        con DTOs (Java records), validación Bean Validation, OpenAPI annotations,
        y manejo de excepciones. Genera tests MockMvc."

# ── FRONTEND: ANGULAR SERVICE ──
claude "Genera el CompanyService Angular (HttpClient) para consumir todos
        los endpoints de CompanyController. Genera interfaces TypeScript
        para CompanyDTO, CreateCompanyRequest, UpdateCompanyRequest."

# ── FRONTEND: VISTA ──
claude "Analiza admin/companyList.xhtml (RichFaces) y genera el componente
        Angular equivalente con PrimeNG Table, filtros, paginación lazy,
        y sorting. Usa standalone component con signals."

# ── REPORTES ──
claude "Actualiza balanceSheetReport.jrxml de JasperReports 3.7 a 7.x.
        Genera el ReportController y ReportService en Spring Boot.
        Genera el componente Angular ReportViewer."

# ── TESTS ──
claude "Genera tests de integración con TestContainers + MySQL para AdminService.
        Incluye tests de CRUD, multi-tenancy, y Envers auditing."

# ── I18N ──
claude "Convierte messages_app.properties a src/assets/i18n/es.json.
        Agrupa las keys por módulo en JSON anidado."

# ── VALIDACIÓN ──
claude "Compara la lógica de negocio entre el servicio legacy
        WarehouseServiceBean.java y el nuevo WarehouseService.java.
        Identifica cualquier diferencia funcional."
```

---

## APÉNDICE D: DEPENDENCIAS MAVEN DEL PROYECTO NUEVO

```xml
<!-- pom.xml (parent) — Dependencias principales -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.4.3</version>
</parent>

<properties>
    <java.version>21</java.version>
    <mapstruct.version>1.6.3</mapstruct.version>
    <jasperreports.version>7.0.1</jasperreports.version>
    <springdoc.version>2.8.3</springdoc.version>
</properties>

<dependencies>
    <!-- Spring Boot Core -->
    <dependency>spring-boot-starter-web</dependency>
    <dependency>spring-boot-starter-data-jpa</dependency>
    <dependency>spring-boot-starter-security</dependency>
    <dependency>spring-boot-starter-validation</dependency>
    <dependency>spring-boot-starter-mail</dependency>
    <dependency>spring-boot-starter-cache</dependency>
    <dependency>spring-boot-starter-data-redis</dependency>
    <dependency>spring-boot-starter-actuator</dependency>

    <!-- Database -->
    <dependency>mysql-connector-j</dependency>
    <dependency>flyway-mysql</dependency>

    <!-- Hibernate Envers (auditoría) -->
    <dependency>hibernate-envers</dependency>

    <!-- JWT -->
    <dependency>jjwt-api (0.12.x)</dependency>
    <dependency>jjwt-impl</dependency>
    <dependency>jjwt-jackson</dependency>

    <!-- Mapping -->
    <dependency>mapstruct (1.6.x)</dependency>

    <!-- Reporting -->
    <dependency>jasperreports (7.x)</dependency>
    <dependency>openpdf (2.x)</dependency>
    <dependency>poi-ooxml (5.x)</dependency>

    <!-- Documentation -->
    <dependency>springdoc-openapi-starter-webmvc-ui (2.8.x)</dependency>

    <!-- Utilities -->
    <dependency>lombok</dependency>
    <dependency>zxing-core (barcode)</dependency>
    <dependency>exp4j (expressions)</dependency>

    <!-- Testing -->
    <dependency>spring-boot-starter-test</dependency>
    <dependency>spring-security-test</dependency>
    <dependency>testcontainers (mysql)</dependency>
</dependencies>
```

---

> **Nota final**: Este documento es un plan vivo. Debe actualizarse conforme avanza la migración, especialmente las estimaciones de tiempo y los riesgos identificados. La clave del éxito es **migrar incrementalmente**, validar con usuarios reales en cada wave, y mantener la disciplina de no agregar features nuevas durante la migración.
>
> **Producto:** Sophia 7.0 | **Stack:** Spring Boot 3.4 + Angular 19 + PrimeNG 18 + MySQL 8.4 | **Paquete:** `net.encens.sophia`
