# Dashboard Moderno - Estado del Proyecto

**Última actualización:** 2025-11-15
**Versión:** 1.1 - Estable (con gráfico Acopio Diario)
**Branch:** terdemol_dashboard
**Commits importantes:**
- `4c5cb26d` Dashboard, release v6.0.62
- `0d6e9832` Dashboard, quitando mocks de ejemplo
- `a47a4207` Uso datasource java:/khipusDatasource en Dashboard
- `9fe992dc` v6.0.61 dashboard rev1

---

## Estado Actual

### Dashboards Implementados ✅

#### 1. Producción (`/view/dashboard/production/`)

**Estado:** ✅ Completamente funcional

**Gráficos:**
- **Gráfico 1: Producción Diaria** (line chart)
  - Muestra producción de múltiples productos por fecha
  - Cada producto es una serie (línea) diferente
  - Eje X: Fechas jerárquicas (días arriba, meses agrupados abajo)
  - Eje Y: Cantidad en toneladas (Tn)
  - Tooltips con fecha completa dd/MM/yyyy y valor
  - Leyenda en lado derecho, vertical, sin decoración
  - SQL: `xpr_producto` + `xpr_produccion` + `xpr_plan` + `inv_articulos`

- **Gráfico 2: Producción Total vs Materia Prima**
  - 2 líneas comparativas:
    - Verde: Total Producción (agregado de todos los productos)
    - Gris oscuro: Total Materia Prima usada
  - Agregación por fecha (suma de todos los productos por día)
  - Mismo formato de eje X jerárquico
  - Título actualizado: "Producción Total vs Materia Prima"

**Cards Estadísticas (4):**
1. Productos (cantidad de productos diferentes)
2. Producción Total (Tn)
3. Materia Prima Total (Tn)
4. Costo Total (Bs)

**Archivos:**
- `production-dashboard.html` - Estructura HTML
- `production-charts.js` - Lógica de gráficos
- `ProductionDashboardServlet.java` - API backend

#### 2. Materia Prima (`/view/dashboard/materiaPrima/`)

**Estado:** ✅ Completamente funcional

**Gráficos:**
- **Gráfico 1: Acopio diario x Materia Prima** (line chart) ⭐ NUEVO
  - Múltiples líneas, una por cada tipo de material
  - Eje X: Fechas jerárquicas (días arriba, meses agrupados abajo)
  - Eje Y: Peso en toneladas (Tn)
  - Leyenda derecha con totales por material (ej: "BARITINA 277.74 Tn")
  - Título de leyenda muestra total general: "Acopio Total: 8,121.32 Tn"
  - Tooltips compartidos con fecha dd/MM/yyyy
  - Fondo blanco con padding y sombra (estilo card)
  - SQL: `acopiomp` + `metaproductoproduccion` agrupado por fecha y material
  - Procesamiento manual de ResultSet (columnas: fecha, material, peso_diario_tn)

- **Gráfico 2: Acopio de Proveedores por Peso** (barra horizontal)
  - Top 10 productores por peso total

- **Gráfico 3: Distribución de Materias Primas por Peso** (donut)
  - Distribución porcentual por tipo de material

- **Gráfico 4: Distribución de Acopio por Zonas** (pie)
  - Distribución por zonas productivas

**Cards Estadísticas (4):**
1. Total Productores
2. Peso Total (Tn) - ⚠️ Corregido: ahora suma solo materialsData (evita duplicación)
3. Tipos de Material
4. Zonas Activas

**Características:**
- Datos de tabla `acopiomp`
- Unidades en toneladas (Tn)
- Gráficos interactivos (line, bar, donut, pie)
- JOIN con `productormateriaprima`, `persona`, `metaproductoproduccion`, `zonaproductiva`

**Archivos:**
- `materia-prima-dashboard.html`
- `materia-prima-charts.js`
- `MateriaPrimaDashboardServlet.java`

#### 3. Inventarios (`/view/dashboard/inventory/`)

**Estado:** ✅ Completamente funcional con cache optimizado

**Gráficos:**
1. Volumen de Compras por Grupo (sunburst)
   - Top 15 grupos con compras > 100 Bs
   - Cache de 10 minutos
2. Volumen de Compras por Subgrupo (sunburst)
   - Interactivo: se actualiza al seleccionar grupo
   - Top 10 subgrupos con compras > 50 Bs
   - Selector de grupo dinámico
3. Gastos de Inventario por Área (sunburst)
   - Basado en `inv_movdet` + `inv_vales` + `inv_destino`
   - Solo egresos aprobados

**Características Especiales:**
- Datos correlacionados: Una consulta SQL procesa datos para ambos gráficos 1 y 2
- Sistema de cache: `CacheEntry` con expiración de 10 minutos
- Limpieza automática de cache cuando > 10 entradas
- Estructura JSON: `{groups: [...], subgroupsByGroup: {...}}`

**Archivos:**
- `inventory-dashboard.html`
- `inventory-charts.js`
- `InventoryDashboardServlet.java` (con cache)

#### 4. Finanzas (`/view/dashboard/finance/`)

**Estado:** ✅ Implementado

**Cards Estadísticas (4):**
1. Total Ingresos
2. Total Gastos
3. Balance Neto
4. Conceptos Registrados

**Gráficos (2):**
1. Gráfico de Ingresos
2. Gráfico de Gastos

**Archivos:**
- `finance-dashboard.html`
- `finance-charts.js`
- `FinanceDashboardServlet.java`

---

## Arquitectura Técnica

### Backend (Java Servlets)

**Clase Base:**
```
BaseDashboardServlet.java
├── getConnection() → JNDI DataSource lookup
├── handleDataRequest() → Método abstracto
├── executeQueryToJson() → Ejecuta SQL y retorna JSON
├── executeQueryToJsonWithParam() → Con parámetro adicional
└── escapeJson() → Escapar caracteres especiales
```

**Servlets Específicos:**
- `ProductionDashboardServlet.java`
  - `getDailyProductionData()` - Producción diaria con JOIN 4 tablas
  - `getProducerData()`, `getMaterialData()`, `getZoneData()`

- `MateriaPrimaDashboardServlet.java`
  - `getProducerData()`, `getMaterialData()`, `getZoneData()`

- `InventoryDashboardServlet.java`
  - `getPurchasesByGroupData()` - Con cache
  - `getGroupsData()` - Lista para selector
  - `getSubgroupsByGroupData()` - Filtrado por grupo
  - `getCorrelatedInventoryData()` - Consulta correlacionada
  - `getAreasExpensesData()`

- `FinanceDashboardServlet.java`
  - [Endpoints según implementación]

**Conexión a Base de Datos:**
```java
// JNDI DataSource lookup (NO hardcodear credenciales)
String dataSourceJNDI = Configuration.i.getLocalDataSource();
Context context = new InitialContext();
DataSource dataSource = (DataSource) context.lookup(dataSourceJNDI);
Connection conn = dataSource.getConnection();
```

**Características:**
- Timeout de queries: 30 segundos
- Error handling: Retorna `[]` vacío, nunca mock data
- Logging: System.out.println para debug
- UTF-8: Response con charset UTF-8
- CORS: Access-Control-Allow-Origin: *

### Frontend (JavaScript + HTML)

**Archivos Compartidos:**
```
view/dashboard/shared/
├── components/
│   ├── controls.html (sin botón Test Conexión)
│   └── loading-overlay.html
├── scripts/
│   ├── dashboard-core.js (carga datos, períodos, encoding)
│   └── dashboard-navigation.js (cambio de tabs)
└── styles/
    ├── base.css
    ├── charts.css (min-height: 400px)
    └── dashboard.css
```

**dashboard-core.js - Funciones Principales:**
- `loadData()` - Carga datos del dashboard activo
- `applyDatePeriod(period)` - Aplica período predefinido
- `fixEncoding(text)` - Corrige caracteres UTF-8 mal codificados
- `formatNumber(num, decimals)` - Formato de números
- `getCurrentDashboard()` - Retorna dashboard activo

**dashboard-navigation.js:**
- `switchDashboard(dashboardName)` - Cambia entre dashboards
- `initializeNavigation()` - Inicializa tabs

**Estructura por Dashboard:**
```
production/
├── production-dashboard.html
└── production-charts.js
    ├── createDailyProductionChart()
    ├── createProductionVsRawMaterialChart()
    ├── createHierarchicalDateLabels() ⭐
    └── updateProductionCharts()
```

**Highcharts:**
- Versión: Compatible con useHTML: false
- Responsive: true
- Encoding: UTF-8 con DashboardCore.fixEncoding()
- Destrucción: `chart.destroy()` + `container.innerHTML = ''`

---

## Patrones Implementados ⭐

### 1. Fechas Jerárquicas (Producción)

**Problema:** Mostrar fechas de forma compacta con días y meses agrupados

**Solución:**
```javascript
function createHierarchicalDateLabels(dates) {
    const monthNames = ['', 'Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun',
                       'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];

    return dates.map((date, index) => {
        const parts = date.split('-');  // '2024-05-15'
        const day = parts[2];            // '15'
        const month = parseInt(parts[1]); // 5
        const monthYear = date.substring(0, 7); // '2024-05'

        // Detectar si es el primer día de un nuevo mes
        const isMonthStart = index === 0 ||
                            dates[index - 1].substring(0, 7) !== monthYear;

        if (isMonthStart) {
            return day + '<br>' + monthNames[month]; // '15<br>May'
        }
        return day; // '16'
    });
}
```

**Configuración Highcharts:**
```javascript
xAxis: {
    categories: createHierarchicalDateLabels(processed.dates),
    labels: {
        useHTML: false,  // ⚠️ IMPORTANTE: false para SVG correcto
        rotation: 0,
        align: 'center',
        style: { fontSize: '10px', whiteSpace: 'normal' },
        y: 20
    }
}
```

**Notas:**
- Usar `<br>` NO `\n` (SVG no soporta \n)
- `useHTML: false` previene distorsión al actualizar datos
- Detectar límites de mes comparando YYYY-MM

### 2. Datos Correlacionados (Inventarios)

**Problema:** Dos gráficos relacionados (Grupos → Subgrupos) requieren consistencia

**Solución:** Una consulta SQL, procesamiento en memoria

```java
// 1. Consulta única con todos los datos
String sql = "SELECT e.fecha, g.descri as grupo, s.descri as subgrupo, "
           + "d.total as total_Bs, g.cod_gru, s.cod_sub "
           + "FROM com_detoc d "
           + "JOIN com_encoc e ON d.id_com_encoc = e.id_com_encoc "
           + "JOIN inv_articulos i ON d.cod_art = i.cod_art "
           + "JOIN inv_subgrupos s ON i.cod_sub = s.cod_sub "
           + "JOIN inv_grupos g ON s.cod_gru = g.cod_gru "
           + "WHERE i.cod_gru = s.cod_gru AND e.fecha BETWEEN ? AND ?";

// 2. Procesar en memoria
while (rs.next()) {
    String grupo = rs.getString("grupo");
    String subgrupo = rs.getString("subgrupo");
    String codGru = rs.getString("cod_gru");
    double totalBs = rs.getDouble("total_Bs");

    // Acumular grupos
    groupTotals.put(grupo, groupTotals.get(grupo) + totalBs);

    // Acumular subgrupos por grupo
    subgroupsByGroup.get(codGru).put(subgrupo, total);
}

// 3. Retornar estructura correlacionada
return "{\"groups\":[...],\"subgroupsByGroup\":{...}}";
```

**Ventajas:**
- Correlación perfecta (misma fuente de datos)
- Una sola consulta a BD
- Selector de grupo siempre sincronizado

### 3. Cache con Expiración (Inventarios)

```java
private static class CacheEntry {
    final String data;
    final long timestamp;

    CacheEntry(String data) {
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    boolean isExpired() {
        return (System.currentTimeMillis() - timestamp) > CACHE_DURATION_MS;
    }
}

// Uso
String cacheKey = "purchases_by_group_" + startDate + "_" + endDate;
CacheEntry entry = cache.get(cacheKey);
if (entry != null && !entry.isExpired()) {
    return entry.data; // Cache HIT
}
// Cache MISS - ejecutar consulta
cache.put(cacheKey, new CacheEntry(result));
```

### 4. Destrucción Correcta de Charts

**Problema:** Al actualizar fechas, el chart se distorsiona y pierde fechas

**Causa:** DOM fragments del chart anterior permanecen

**Solución:**
```javascript
function updateChart(containerId, data) {
    const container = document.getElementById(containerId);

    // 1. Destruir chart existente
    if (window[containerId + 'Chart']) {
        window[containerId + 'Chart'].destroy();
        window[containerId + 'Chart'] = null;
    }

    // 2. Limpiar contenedor DOM
    container.innerHTML = '';

    // 3. Crear nuevo chart
    window[containerId + 'Chart'] = Highcharts.chart(containerId, config);
}
```

---

## Consultas SQL Importantes

### Producción Diaria
```sql
SELECT xp.fecha,
       COALESCE(a.descri, 'Sin Producto') as producto,
       COALESCE(SUM(p.cantidad), 0) / 1000 as cantidad_producto_tn,
       COALESCE(SUM(pr.totalmp), 0) / 1000 as cantidad_materia_prima_tn,
       COALESCE(SUM(pr.costototal), 0) as costo_total
FROM xpr_producto p
JOIN xpr_produccion pr ON p.idproduccion = pr.idproduccion
JOIN xpr_plan xp ON pr.idplan = xp.idplan
JOIN inv_articulos a ON p.cod_art = a.cod_art
WHERE xp.fecha BETWEEN ? AND ?
GROUP BY xp.fecha, a.descri
ORDER BY xp.fecha ASC, a.descri ASC
```

### Materia Prima - Acopio Diario ⭐ NUEVO
```sql
SELECT c.fecha as fecha,
       COALESCE(m.nombre, 'Material Desconocido') as material,
       COALESCE(SUM(c.pesobal), 0) / 1000 as peso_diario_tn
FROM acopiomp c
JOIN metaproductoproduccion m ON c.idmetaproductoproduccion = m.idmetaproductoproduccion
WHERE c.fecha BETWEEN ? AND ?
GROUP BY c.fecha, m.idmetaproductoproduccion, m.nombre
ORDER BY c.fecha ASC, m.nombre ASC
```
**Nota:** Procesamiento manual de ResultSet (no usa `executeQueryToJson` porque necesita columnas específicas: fecha, material, peso_diario_tn)

### Materia Prima - Productores
```sql
SELECT COALESCE(pe.nombres, 'Sin Nombre') as name,
       COALESCE(SUM(c.pesobal), 0) / 1000 as peso
FROM acopiomp c
LEFT JOIN productormateriaprima p ON c.idproductormateriaprima = p.idproductormateriaprima
LEFT JOIN persona pe ON p.idproductormateriaprima = pe.idpersona
WHERE c.fecha BETWEEN ? AND ?
GROUP BY pe.nombres
ORDER BY peso DESC
```

### Inventarios - Grupos (con cache y LIMIT)
```sql
SELECT g.descri as name,
       SUM(d.total) as peso
FROM com_detoc d
JOIN com_encoc e ON d.id_com_encoc = e.id_com_encoc
JOIN inv_articulos i ON d.cod_art = i.cod_art
JOIN inv_grupos g ON i.cod_gru = g.cod_gru
WHERE e.fecha BETWEEN ? AND ?
GROUP BY g.descri, g.cod_gru
HAVING SUM(d.total) > 100
ORDER BY SUM(d.total) DESC
LIMIT 15
```

### Inventarios - Gastos por Área
```sql
SELECT d.nombre as name,
       SUM(md.monto) as peso
FROM inv_movdet md
JOIN inv_mov im ON md.no_cia = im.no_cia AND md.no_trans = im.no_trans AND md.estado = im.estado
JOIN inv_vales wv ON im.no_cia = wv.no_cia AND im.no_trans = wv.no_trans
JOIN inv_destino d ON wv.iddestino = d.iddestino
WHERE wv.fecha BETWEEN ? AND ?
AND wv.estado = 'APR'
AND wv.cod_doc = 'EGR'
AND wv.no_cia = '01'
GROUP BY d.nombre
HAVING SUM(md.monto) > 0
ORDER BY SUM(md.monto) DESC
LIMIT 20
```

---

## Configuración y Despliegue

### Base de Datos

**Desarrollo:**
- Host: localhost:3306
- Schema: terdemol / sic_terdemol (ver Constants.java)
- JNDI: `java:/khipusDatasource`
- Archivo: `resources/khipus-dev-ds.xml`

**Producción:**
- Host: 10.0.0.100:3306
- Schema: khipus
- JNDI: `java:/khipusDatasource`
- Archivo: `resources/khipus-prod-ds.xml`

### Build

```bash
# Compilar
ant explode

# Limpiar y compilar
ant clean explode

# Desplegar a JBoss
ant deploy

# Con perfil específico
ant -Dprofile=prod deploy
```

### URLs

- Desarrollo: `http://localhost:8080/khipus/dashboard/`
- Producción: `[URL servidor]/khipus/dashboard/`

**APIs:**
- `/dashboard-api?type=test` (legacy)
- `/production-dashboard-api?type=daily_production&startDate=...&endDate=...`
- `/materia-prima-dashboard-api?type=producers&startDate=...&endDate=...`
- `/inventory-dashboard-api?type=purchases_by_group&startDate=...&endDate=...`
- `/finance-dashboard-api?type=...&startDate=...&endDate=...`

---

## Notas Importantes para Claude Code 🤖

### ⚠️ REGLAS CRÍTICAS

1. **NUNCA usar mock data**
   - Error fallback siempre: `[]` vacío
   - Mostrar "No hay datos" en frontend
   - Mock data confunde usuarios en producción

2. **Fechas jerárquicas en line charts**
   - Usar `<br>` NO `\n` (SVG no soporta \n)
   - `useHTML: false` para prevenir distorsión
   - Detectar límites de mes: comparar `YYYY-MM`
   - Primer día del mes: mostrar `día<br>mes`
   - Otros días: solo `día`

3. **Conexión a Base de Datos**
   - SIEMPRE usar JNDI DataSource
   - NUNCA hardcodear credenciales
   - Usar `Configuration.i.getLocalDataSource()`
   - Timeout queries: 30 segundos

4. **Encoding UTF-8**
   - Backend: `response.setCharacterEncoding("UTF-8")`
   - Frontend: Usar `DashboardCore.fixEncoding(text)`
   - Corrige: Ã© → é, Ã³ → ó, etc.

5. **Actualización de Charts**
   - Siempre: `chart.destroy()` + `container.innerHTML = ''`
   - Previene distorsión y fragmentos DOM
   - Recrear chart completamente

6. **Cache (solo consultas pesadas)**
   - Duración: 10 minutos
   - Clave: incluir startDate + endDate
   - Limpieza automática cuando > 10 entradas
   - Log cache HIT/MISS para debug

7. **Unidades**
   - Producción/Materia Prima: Toneladas (Tn) = pesobal / 1000
   - Finanzas: Bolivianos (Bs)
   - Fechas: yyyy-MM-dd en SQL, dd/MM/yyyy en tooltips

8. **Período Inicial Automático** ⭐ NUEVO
   - Al cargar dashboard, se selecciona automáticamente el semestre según fecha actual
   - Enero-Junio (meses 0-5): "Primer Semestre" (01/01 - 30/06)
   - Julio-Diciembre (meses 6-11): "Segundo Semestre" (01/07 - 31/12)
   - Implementado en `dashboard-core.js` función `initDates()`
   - Reduce carga inicial: 6 meses vs 12 meses completos

### 🔧 Troubleshooting Común

**Chart no se ve / se corta:**
- Verificar `min-height: 400px` en CSS (NO `height`)
- Verificar `marginBottom`, `marginRight` suficientes
- Verificar `useHTML: false` en labels

**Fechas solapadas / no visibles:**
- Usar patrón de fechas jerárquicas
- NO usar dynamic rotation
- Leyenda a la derecha, NO abajo

**"No hay datos" en producción:**
- Verificar JNDI DataSource configurado
- Verificar schema correcto en Constants.java
- Verificar tablas existen con datos en rango de fechas

**Caracteres raros (Ã©, Ã³):**
- Aplicar `DashboardCore.fixEncoding()` a todos los textos

---

## Próximos Pasos (Sugeridos)

### Mejoras Pendientes
- [ ] Agregar más gráficos a Finanzas si se requiere
- [ ] Implementar exportación a PDF/Excel
- [ ] Agregar filtros adicionales (por productor, zona, etc.)
- [ ] Optimizar consultas lentas (>2 segundos)
- [ ] Testing en diferentes navegadores (Chrome, Firefox, Edge)

### Funcionalidades Futuras
- [ ] Dashboard de Ventas
- [ ] Dashboard de Recursos Humanos
- [ ] Comparación año anterior
- [ ] Proyecciones y tendencias
- [ ] Alertas y notificaciones

---

## Historial de Cambios

### v1.1 - 2025-11-15 (Gráfico Acopio Diario + Mejoras)
- ✅ **NUEVO:** Gráfico de líneas "Acopio diario x Materia Prima" en dashboard Materia Prima
  - Múltiples series por tipo de material
  - Fechas jerárquicas (días/meses) reutilizando patrón de Producción
  - Totales por material en leyenda (ej: "BARITINA 277.74 Tn")
  - Total general en título de leyenda ("Acopio Total: 8,121.32 Tn")
  - Fondo blanco con estilo card consistente
- ✅ **FIX:** Corregido bug de duplicación en card "Peso Total" (sumaba producersData + materialsData)
- ✅ **Backend:** Nuevo endpoint `daily_acopio` en MateriaPrimaDashboardServlet
  - Procesamiento manual de ResultSet para estructura específica
  - SQL: GROUP BY fecha + material con agregación diaria
- ✅ **Mejoras UI:**
  - Actualizado título gráfico Producción: "Producción Total vs Materia Prima"
  - Actualizado título gráfico Materia Prima: "Acopio diario x Materia Prima"
- ✅ **UX:** Período inicial cambiado de "Año actual" a semestre automático
  - Enero-Junio: selecciona "Primer Semestre"
  - Julio-Diciembre: selecciona "Segundo Semestre"
  - Reduce carga inicial de datos (6 meses vs 12 meses)

### v1.0 - 2025-11-14 (Versión Estable)
- ✅ 4 dashboards implementados y funcionales
- ✅ Migración a JNDI DataSource
- ✅ Eliminación de mock data
- ✅ Fechas jerárquicas en Producción
- ✅ Cache optimizado en Inventarios
- ✅ UI limpia sin botones innecesarios

### Commits Importantes
- `9fe992dc` v6.0.61 dashboard rev1
- `8f69ee9c` Dashboard Produccion rev2, v6.0.60.2
- `e51806e3` Dashboard Produccion rev2, titulos grafico 2
- `77d36e29` Dashboard Produccion rev2, tooltip fecha
- `e949c4fd` Dashboard Produccion rev1, eje x ok
- `3dbd52ec` Nuevo Dashboard 'Materia Prima'
- `a47a4207` Uso datasource java:/khipusDatasource
- `0d6e9832` Dashboard, quitando mocks de ejemplo

---

## Estructura Completa de Archivos

```
D:\Intellij\sisk13\
│
├── src/main/com/encens/khipus/
│   ├── servlet/
│   │   ├── BaseDashboardServlet.java ⭐ (base con JNDI)
│   │   ├── DashboardServlet.java (legacy)
│   │   ├── ProductionDashboardServlet.java
│   │   ├── MateriaPrimaDashboardServlet.java
│   │   ├── InventoryDashboardServlet.java (con cache)
│   │   └── FinanceDashboardServlet.java
│   │
│   └── util/
│       └── Constants.java (schemas configurables)
│
├── view/dashboard/
│   │
│   ├── shared/
│   │   ├── components/
│   │   │   ├── controls.html (sin Test Conexión)
│   │   │   └── loading-overlay.html
│   │   ├── js/
│   │   │   ├── dashboard-core.js ⭐ (períodos, semestre automático)
│   │   │   └── dashboard-navigation.js (cambio tabs)
│   │   └── styles/
│   │       ├── base.css
│   │       ├── charts.css (min-height: 400px)
│   │       └── dashboard.css
│   │
│   ├── production/
│   │   ├── production-dashboard.html (4 cards + 2 charts)
│   │   └── production-charts.js ⭐ (fechas jerárquicas)
│   │
│   ├── materiaPrima/
│   │   ├── materia-prima-dashboard.html (4 cards + 1 line chart + 3 charts)
│   │   └── materia-prima-charts.js ⭐ (fechas jerárquicas + totales)
│   │
│   ├── inventory/
│   │   ├── inventory-dashboard.html (selector + 3 charts)
│   │   └── inventory-charts.js (datos correlacionados)
│   │
│   └── finance/
│       ├── finance-dashboard.html (4 cards + 2 charts)
│       └── finance-charts.js
│
├── resources/
│   ├── khipus-dev-ds.xml (JNDI datasource dev)
│   ├── khipus-prod-ds.xml (JNDI datasource prod)
│   └── WEB-INF/
│       └── web.xml (servlet mappings)
│
├── CLAUDE.md (instrucciones generales proyecto)
├── DASHBOARD_STATUS.md ⭐ (este archivo - memoria del dashboard)
└── build.xml (ant build script)
```

---

## Recursos y Referencias

### Documentación
- Highcharts: https://api.highcharts.com/highcharts/
- JBoss Seam 2.1: http://docs.jboss.org/seam/2.1.2/reference/en-US/html/
- JNDI DataSource: https://docs.oracle.com/javaee/6/tutorial/doc/bncjh.html

### Contacto y Soporte
- Desarrollador: [Tu nombre/equipo]
- Repositorio: [URL si aplica]

---

**INSTRUCCIONES PARA CLAUDE CODE:**

Cuando retomes este proyecto después de días/semanas:

1. **Lee este archivo completo primero** - Contiene TODO el contexto
2. **Verifica el branch:** `terdemol_dashboard`
3. **Revisa commits recientes:** `git log --oneline -10`
4. **Pregunta al usuario:** ¿Qué dashboard/gráfico quieren desarrollar?
5. **Consulta la sección correspondiente** de este documento
6. **Sigue las reglas críticas** marcadas con ⚠️
7. **NO necesitas recordar manualmente** - toda la info está aquí

**Patrones implementados más importantes:**
- Fechas jerárquicas → Sección "Patrones Implementados #1"
- Datos correlacionados → Sección "Patrones Implementados #2"
- Cache con expiración → Sección "Patrones Implementados #3"

**Ante dudas:**
- Buscar en "Notas Importantes para Claude Code"
- Revisar "Consultas SQL Importantes"
- Verificar archivos en "Estructura Completa"

---

*Fin del documento - Versión 1.0*
