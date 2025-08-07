# Refactorización Backend - Dashboard Servlets

## Cambios Realizados

### 1. Reestructuración de Servlets

**Antes**: Un solo `DashboardServlet.java` con todas las consultas mezcladas

**Ahora**: Sistema modular con servlets especializados:

- **`BaseDashboardServlet.java`** - Clase base con funciones comunes
- **`ProductionDashboardServlet.java`** - Consultas de producción
- **`InventoryDashboardServlet.java`** - Consultas de inventarios  
- **`FinanceDashboardServlet.java`** - Consultas de finanzas

### 2. URLs de API Actualizadas

| Dashboard | URL Anterior | URL Nueva |
|-----------|-------------|-----------|
| Producción | `/khipus/dashboard-api` | `/khipus/production-dashboard-api` |
| Inventarios | `/khipus/dashboard-api` | `/khipus/inventory-dashboard-api` |
| Finanzas | `/khipus/dashboard-api` | `/khipus/finance-dashboard-api` |

### 3. Nuevo Gráfico de Inventarios

#### Consulta SQL Implementada:
```sql
SELECT 
    g.descri as name,
    SUM(d.total) as peso
FROM com_detoc d
JOIN com_encoc e ON d.id_com_encoc = e.id_com_encoc
JOIN inv_articulos i ON d.cod_art = i.cod_art
JOIN inv_grupos g ON i.cod_gru = g.cod_gru
WHERE e.fecha BETWEEN ? AND ?
GROUP BY g.descri, g.cod_gru
ORDER BY SUM(d.total) DESC
```

#### Características del Gráfico:
- **Tipo**: Columnas verticales con etiquetas rotadas
- **Título**: "Volumen de compras por Grupo"
- **Datos**: Monto total en Bs por grupo de artículos
- **API**: `/khipus/inventory-dashboard-api?type=purchases_by_group`

### 4. Arquitectura del BaseDashboardServlet

#### Funciones Comunes:
- `setupJsonResponse()` - Configuración de headers HTTP
- `getDefaultDates()` - Manejo de fechas por defecto
- `getConnection()` - Conexión a base de datos
- `escapeJson()` - Escape de caracteres especiales
- `executeQueryToJson()` - Ejecutor genérico de consultas SQL

#### Método Abstracto:
```java
protected abstract String handleDataRequest(String dataType, String startDate, String endDate) throws Exception;
```

### 5. Beneficios de la Reestructuración

#### **Mantenibilidad**
- Código separado por responsabilidades
- Fácil identificar y modificar consultas específicas
- Reducción de acoplamiento entre dashboards

#### **Escalabilidad**
- Agregar nuevos dashboards es trivial
- Cada servlet maneja su propia lógica
- APIs independientes por dashboard

#### **Performance**
- Consultas optimizadas por dominio
- Menor carga en cada endpoint
- Cacheo específico por tipo de datos

#### **Testing**
- Cada servlet se puede probar independientemente
- Mock data específico por dashboard
- Aislamiento de errores

### 6. Compatibilidad

#### **Datos de Producción**
- **REAL**: Consultas existentes funcionando
- **APIs**: `/production-dashboard-api?type=producers|materials|zones`

#### **Datos de Inventarios**
- **REAL**: Primer gráfico con consulta de compras por grupo
- **MOCK**: Segundo y tercer gráfico (categorías, almacenes)
- **API**: `/inventory-dashboard-api?type=purchases_by_group|categories|warehouses`

#### **Datos de Finanzas**
- **MOCK**: Todos los gráficos (se pueden implementar después)
- **API**: `/finance-dashboard-api?type=income|expenses|cash_flow`

### 7. Migración del Frontend

#### **ChartUtils.js**
- Agregada función `createColumnChartConfig()` para gráficos verticales
- Soporte para etiquetas rotadas y mejor formato de datos

#### **Dashboards Actualizados**
- Cada dashboard usa su propia API específica
- Funciones `fetchXXXData()` especializadas
- Manejo de errores mejorado

### 8. Archivo Legacy

**`DashboardServlet.java`** original se puede eliminar después de verificar que todo funciona correctamente.

### 9. Próximos Pasos

1. **Testing**: Verificar que todos los endpoints respondan correctamente
2. **Datos Reales**: Implementar consultas reales para categorías y almacenes en inventarios
3. **Finanzas**: Implementar consultas reales para el dashboard de finanzas
4. **Optimización**: Agregar cacheo y optimización de consultas
5. **Monitoreo**: Implementar logging detallado para cada servlet

### 10. Estructura de Archivos

```
src/main/com/encens/khipus/servlet/
├── BaseDashboardServlet.java       ✅ Clase base
├── ProductionDashboardServlet.java ✅ Producción  
├── InventoryDashboardServlet.java  ✅ Inventarios
├── FinanceDashboardServlet.java    ✅ Finanzas
└── DashboardServlet.java          ⚠️  (eliminar después)
```

La reestructuración está completa y lista para testing en el entorno de desarrollo.