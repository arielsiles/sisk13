# Guía de Migración - Dashboard Modular

## Migración del Dashboard Original

### Archivo Original vs Nuevo Sistema

**Antes**: `view/dashboard.html` (1400+ líneas)
**Ahora**: Sistema modular con múltiples archivos

### Cambios en URLs

- **Anterior**: `/view/dashboard.html`
- **Nuevo**: `/view/dashboard/index.html`

### Migración de Código Personalizado

Si tienes modificaciones en el dashboard original, aquí está cómo migrarlas:

#### 1. Nuevos Gráficos
**Antes** (en dashboard.html):
```javascript
function updateCustomChart(data) {
    customChart = Highcharts.chart('customChart', {
        // configuración...
    });
}
```

**Ahora** (en el módulo correspondiente):
```javascript
// En production-charts.js, inventory-charts.js o finance-charts.js
function updateCustomChart(data) {
    const config = ChartUtils.createBarChartConfig(
        'customChart',
        'Título del Gráfico',
        data,
        {
            // opciones personalizadas
        }
    );
    
    customChart = Highcharts.chart('customChart', config);
}
```

#### 2. Nuevos Tipos de Datos
**Antes** (en dashboard.html):
```javascript
const [newData] = await Promise.all([
    fetchData('newDataType', startDate, endDate)
]);
```

**Ahora** (en el módulo correspondiente):
```javascript
// Agregar en la función loadData del módulo
const [newData] = await Promise.all([
    DashboardCore.fetchData('newDataType', startDate, endDate)
]);
```

#### 3. Nuevas Estadísticas
**Antes** (en dashboard.html):
```html
<div class="stat-card">
    <div class="stat-value" id="customStat">-</div>
    <div class="stat-label">Custom Metric</div>
</div>
```

**Ahora**:
1. Agregar HTML en el archivo `.html` del módulo correspondiente
2. Actualizar JavaScript en el archivo `.js` correspondiente

### Agregar Nuevo Dashboard

#### 1. Crear estructura de archivos
```bash
mkdir view/dashboard/newdashboard
```

#### 2. Crear archivos del módulo
- `newdashboard/newdashboard-dashboard.html`: Vista HTML
- `newdashboard/newdashboard-charts.js`: Lógica JavaScript

#### 3. Registrar en navigation.js
```javascript
// En shared/js/navigation.js
const dashboards = {
    // ... existentes ...
    newdashboard: {
        name: 'Nuevo Dashboard',
        path: 'newdashboard/newdashboard-dashboard.html',
        script: 'newdashboard/newdashboard-charts.js'
    }
};
```

#### 4. Agregar botón de navegación
```html
<!-- En shared/components/controls.html -->
<button class="tab-btn" data-dashboard="newdashboard" onclick="DashboardNavigation.switchDashboard('newdashboard')">
    Nuevo Dashboard
</button>
```

### Ejemplo Completo: Dashboard de Ventas

#### 1. Archivo HTML (sales/sales-dashboard.html)
```html
<div class="stats">
    <div class="stat-card">
        <div class="stat-value" id="totalSales">-</div>
        <div class="stat-label">Ventas Totales</div>
    </div>
</div>

<div class="charts-container">
    <div class="chart-box">
        <div id="salesChart" class="chart-container"></div>
    </div>
</div>
```

#### 2. Archivo JavaScript (sales/sales-charts.js)
```javascript
window.SalesDashboard = (function() {
    'use strict';
    
    let salesChart;
    let charts = [];
    
    function updateSalesChart(data) {
        const config = ChartUtils.createBarChartConfig(
            'salesChart',
            'Ventas por Período',
            data,
            {
                xAxisTitle: 'Períodos',
                yAxisTitle: 'Monto ($)',
                dataLabelFormat: '${y:.2f}',
                seriesName: 'Ventas',
                color: 'rgba(75, 192, 192, 0.8)'
            }
        );
        
        salesChart = Highcharts.chart('salesChart', config);
        charts[0] = salesChart;
    }
    
    async function loadData(startDate, endDate) {
        try {
            const salesData = await DashboardCore.fetchData('sales', startDate, endDate);
            updateSalesChart(salesData);
            
            // Actualizar estadísticas
            const totalSales = salesData.reduce((sum, item) => sum + item.peso, 0);
            document.getElementById('totalSales').textContent = totalSales;
            
        } catch (error) {
            console.error('Error cargando datos de ventas:', error);
            throw error;
        }
    }
    
    function init() {
        console.log('Sales Dashboard inicializado');
        setTimeout(() => {
            ChartUtils.setupResizeListeners(charts);
        }, 1000);
    }
    
    return {
        init,
        loadData
    };
})();
```

### Ventajas del Nuevo Sistema

1. **Mantenibilidad**: Código separado por responsabilidades
2. **Performance**: Carga bajo demanda de recursos
3. **Escalabilidad**: Fácil agregar nuevos dashboards
4. **Testing**: Cada módulo es independiente
5. **Colaboración**: Múltiples desarrolladores pueden trabajar simultáneamente

### Compatibilidad Hacia Atrás

- La API `/khipus/dashboard-api` se mantiene igual
- Los tipos de datos (producers, materials, zones) funcionan igual
- Toda la funcionalidad existente se preserva