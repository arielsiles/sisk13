# Solución para Errores 404 - Nuevos Servlets

## Problema
Los nuevos servlets no están siendo reconocidos por JBoss AS 5.1 porque:
1. Las anotaciones `@WebServlet` no son compatibles con JBoss AS 5.1 
2. Los servlets no estaban declarados en `web.xml`

## ✅ Solución Implementada

### 1. **Configuración en web.xml**
He agregado los nuevos servlets en `resources/WEB-INF/web.xml`:

```xml
<!-- Production Dashboard Servlet -->
<servlet>
    <servlet-name>ProductionDashboardServlet</servlet-name>
    <servlet-class>com.encens.khipus.servlet.ProductionDashboardServlet</servlet-class>
    <load-on-startup>3</load-on-startup>
</servlet>

<!-- Inventory Dashboard Servlet -->  
<servlet>
    <servlet-name>InventoryDashboardServlet</servlet-name>
    <servlet-class>com.encens.khipus.servlet.InventoryDashboardServlet</servlet-class>
    <load-on-startup>4</load-on-startup>
</servlet>

<!-- Finance Dashboard Servlet -->
<servlet>
    <servlet-name>FinanceDashboardServlet</servlet-name>
    <servlet-class>com.encens.khipus.servlet.FinanceDashboardServlet</servlet-class>
    <load-on-startup>5</load-on-startup>
</servlet>

<!-- Y sus mappings -->
<servlet-mapping>
    <servlet-name>ProductionDashboardServlet</servlet-name>
    <url-pattern>/production-dashboard-api</url-pattern>
</servlet-mapping>

<servlet-mapping>
    <servlet-name>InventoryDashboardServlet</servlet-name>
    <url-pattern>/inventory-dashboard-api</url-pattern>
</servlet-mapping>

<servlet-mapping>
    <servlet-name>FinanceDashboardServlet</servlet-name>
    <url-pattern>/finance-dashboard-api</url-pattern>
</servlet-mapping>
```

### 2. **Removidas anotaciones @WebServlet**
He eliminado las anotaciones `@WebServlet` de todos los servlets para evitar conflictos.

## 🚀 Pasos para Aplicar la Solución

### 1. **Recompilar y Redesplegar**
```bash
# Detener JBoss
./jboss/bin/shutdown.sh

# Limpiar y compilar
ant clean
ant explode

# Reiniciar JBoss
./jboss/bin/run.sh
```

### 2. **Verificar que funciona**
Probar estas URLs en el navegador:
- `http://localhost:8081/khipus/production-dashboard-api?type=test`
- `http://localhost:8081/khipus/inventory-dashboard-api?type=test`
- `http://localhost:8081/khipus/finance-dashboard-api?type=test`

**Respuesta esperada:**
```json
{"status":"ok","message":"Dashboard API working","timestamp":1692123456789}
```

### 3. **Verificar Dashboard**
- Abrir `http://localhost:8081/khipus/view/dashboard/index.html`
- **Producción**: Debería mostrar datos reales (sin errores 404)
- **Inventarios**: Debería mostrar el gráfico de "Volumen de compras por Grupo" con datos reales
- **Finanzas**: Debería funcionar con datos mock

## Estado Esperado Después del Fix

### **Dashboard de Producción** ✅
- Gráficos con datos reales de la base de datos
- Sin errores 404 en la consola

### **Dashboard de Inventarios** ✅  
- **Primer gráfico**: "Volumen de compras por Grupo" con datos REALES de la consulta SQL
- **Segundo y tercer gráfico**: Datos mock (se pueden implementar después)

### **Dashboard de Finanzas** ✅
- Todos los gráficos con datos mock por ahora

## Logs a Revisar

### **En JBoss al iniciar:**
Deberías ver algo como:
```
INFO [TomcatDeployment] deploy, ctxPath=/khipus
INFO - ProductionDashboardServlet registrado en /production-dashboard-api
INFO - InventoryDashboardServlet registrado en /inventory-dashboard-api
INFO - FinanceDashboardServlet registrado en /finance-dashboard-api
```

### **En la consola del navegador:**
- ❌ ANTES: `GET http://localhost:8081/khipus/production-dashboard-api?type=producers 404 (No Encontrado)`
- ✅ DESPUÉS: Sin errores 404, carga de datos exitosa

## Consulta SQL Real para Inventarios

El primer gráfico de inventarios ahora usa esta consulta REAL:

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

Esta consulta agrega el monto total de compras (`total_Bs`) por grupo de artículos en el rango de fechas seleccionado.

## ✅ TODO LISTO

Después de recompilar y reiniciar JBoss, todo debería funcionar correctamente sin errores 404.