# Guía de Deployment - Nuevos Servlets de Dashboard

## Estado Actual

### ✅ **Funcionando Ahora (Fallback)**
- **Dashboard de Producción**: Usa API original `/khipus/dashboard-api`
- **Dashboard de Inventarios**: Usa datos mock temporales con el nuevo gráfico de columnas
- **Dashboard de Finanzas**: Usa datos mock temporales

### 🚧 **Para Implementar (Nuevos Servlets)**
Los siguientes servlets necesitan ser compilados y deployados:

## Archivos Creados/Modificados

### **Nuevos Servlets Java**
```
src/main/com/encens/khipus/servlet/
├── BaseDashboardServlet.java       ✅ Creado
├── ProductionDashboardServlet.java ✅ Creado  
├── InventoryDashboardServlet.java  ✅ Creado
└── FinanceDashboardServlet.java    ✅ Creado
```

### **Frontend Actualizado**
```
view/dashboard/
├── shared/js/chart-utils.js        ✅ Actualizado (nueva función createColumnChartConfig)
├── production/production-charts.js ✅ Actualizado (fallback API)
├── inventory/inventory-charts.js   ✅ Actualizado (fallback mock)
└── finance/finance-charts.js       ✅ Actualizado (fallback mock)
```

## Proceso de Deployment

### 1. **Compilar el Proyecto**
```bash
# Desde la raíz del proyecto SISK13
ant clean
ant explode
```

### 2. **IMPORTANTE: Configuración web.xml**
Los nuevos servlets están configurados en `resources/WEB-INF/web.xml`:
- ProductionDashboardServlet → `/production-dashboard-api`
- InventoryDashboardServlet → `/inventory-dashboard-api`  
- FinanceDashboardServlet → `/finance-dashboard-api`

### 3. **Verificar Deployment en JBoss**
Los nuevos servlets deberían estar disponibles en:
- `http://localhost:8081/khipus/production-dashboard-api`
- `http://localhost:8081/khipus/inventory-dashboard-api` 
- `http://localhost:8081/khipus/finance-dashboard-api`

### 3. **Verificar URLs**
Probar estas URLs en el navegador:
```
GET /khipus/production-dashboard-api?type=test
GET /khipus/inventory-dashboard-api?type=test  
GET /khipus/finance-dashboard-api?type=test
```

**Respuesta esperada:**
```json
{"status":"ok","message":"Dashboard API working","timestamp":1692123456789}
```

### 4. **Reiniciar JBoss** (si es necesario)
```bash
# Detener JBoss
./jboss/bin/shutdown.sh

# Iniciar JBoss  
./jboss/bin/run.sh
```

## Verificación del Funcionamiento

### **Dashboard de Producción**
- ✅ **Debe funcionar inmediatamente** (usa fallback a API original)
- 🔄 **Después del deployment**: Usará nueva API automáticamente

### **Dashboard de Inventarios** 
- ✅ **Funciona con datos mock** (muestra el nuevo gráfico de columnas)
- 🔄 **Después del deployment**: Mostrará datos reales de compras por grupo

### **Dashboard de Finanzas**
- ✅ **Funciona con datos mock** 
- 🔄 **Después del deployment**: Usará nueva API (mock por ahora)

## Logs para Debuggear

### **En la Consola del Navegador:**
```javascript
// Si funcionan las nuevas APIs, verás:
"Production Dashboard inicializado"
"Inventory Dashboard inicializado" 
"Finance Dashboard inicializado"

// Si usa fallback, verás:
"Nueva API no disponible, usando API original..."
"Nueva API de inventarios no disponible, usando datos mock..."
"Nueva API de finanzas no disponible, usando datos mock..."
```

### **En los Logs del Servidor:**
```
Sistema - ProductionDashboardServlet API called - type: producers
Sistema - InventoryDashboardServlet API called - type: purchases_by_group
Sistema - FinanceDashboardServlet API called - type: income
```

## Troubleshooting

### **Error 404 en nuevas APIs**
**Causa**: Servlets no deployados correctamente
**Solución**: 
1. Verificar que los archivos `.java` estén en `src/main/com/encens/khipus/servlet/`
2. Ejecutar `ant clean explode`
3. Reiniciar JBoss

### **Error de Compilación**
**Causa**: Posibles problemas de sintaxis o dependencias
**Solución**:
1. Revisar logs de compilación de Ant
2. Verificar que `DatabaseConfig` esté disponible
3. Verificar imports correctos

### **Error de Base de Datos**
**Causa**: Consultas SQL incorrectas o tablas no disponibles
**Solución**:
1. Verificar que las tablas `com_detoc`, `com_encoc`, `inv_articulos`, `inv_grupos` existan
2. Los servlets tienen fallback a datos mock en caso de error SQL

## Migración Gradual

### **Fase 1** ✅ **ACTUAL** 
- Producción: API original (datos reales)
- Inventarios: Datos mock (nuevo gráfico de columnas)
- Finanzas: Datos mock

### **Fase 2** 🚧 **DESPUÉS DEL DEPLOYMENT**
- Producción: Nueva API (datos reales)
- Inventarios: Nueva API (datos reales de compras por grupo)
- Finanzas: Nueva API (mock, se puede implementar después)

### **Fase 3** 🔮 **FUTURO**
- Implementar consultas reales para categorías y almacenes en inventarios
- Implementar consultas reales para finanzas
- Optimización y cacheo

## Beneficios Post-Deployment

- ✅ **APIs especializadas** por dashboard
- ✅ **Datos reales** para inventarios (compras por grupo)
- ✅ **Mejor mantenibilidad** del código backend
- ✅ **Escalabilidad** para agregar nuevos dashboards
- ✅ **Separación de responsabilidades**

El sistema está diseñado para funcionar tanto antes como después del deployment, garantizando continuidad del servicio.