// Inventory Dashboard - Gráficos y lógica específica de inventarios
window.InventoryDashboard = (function() {
    'use strict';
    
    let inventoryChart, categoriesChart, warehousesChart;
    let charts = [];
    
    // Actualizar gráfico de compras por grupo (COLUMNAS VERTICALES)
    function updateInventoryChart(data) {
        const config = ChartUtils.createColumnChartConfig(
            'inventoryChart',
            'Volumen de compras por Grupo',
            data,
            {
                xAxisTitle: 'Grupos',
                yAxisTitle: 'Monto Total (Bs)',
                seriesName: 'Compras',
                color: 'rgba(255, 99, 132, 0.8)',
                rotateLabels: true,
                showDataLabels: false, // No mostrar etiquetas sobre las barras
                tooltipFormat: '<b>{point.category}</b>: {point.y:,.2f} Bs' // Tooltip con formato Bs
            }
        );
        
        inventoryChart = Highcharts.chart('inventoryChart', config);
        charts[0] = inventoryChart;
    }
    
    // Actualizar gráfico de categorías (DONUT)
    function updateCategoriesChart(data) {
        const config = ChartUtils.createDonutChartConfig(
            'categoriesChart',
            DashboardCore.fixEncoding('Inventario por Categorías'),
            data,
            {
                colors: ChartUtils.colors.inventory,
                seriesName: DashboardCore.fixEncoding('Categorías'),
                tooltipFormat: '<b>{point.name}</b>: {point.percentage:.1f}% ({point.y} unid.)'
            }
        );
        
        categoriesChart = Highcharts.chart('categoriesChart', config);
        charts[1] = categoriesChart;
    }
    
    // Actualizar gráfico de almacenes (PIE)
    function updateWarehousesChart(data) {
        const config = ChartUtils.createPieChartConfig(
            'warehousesChart',
            DashboardCore.fixEncoding('Distribución por Almacenes'),
            data,
            {
                colors: ChartUtils.colors.inventory,
                seriesName: 'Almacenes',
                tooltipFormat: '<b>{point.name}</b>: {point.percentage:.1f}% ({point.y} unid.)'
            }
        );
        
        warehousesChart = Highcharts.chart('warehousesChart', config);
        charts[2] = warehousesChart;
    }
    
    // Actualizar estadísticas de inventarios
    function updateInventoryStats(inventoryData, categoriesData, warehousesData) {
        const totalItems = inventoryData.length; // Total de productos distintos
        const totalValue = inventoryData.reduce((sum, item) => sum + (item.peso || 0), 0) * 100; // Usar peso como valor estimado
        const totalCategories = categoriesData.length;
        const totalWarehouses = warehousesData.length;
        
        document.getElementById('totalItems').textContent = totalItems;
        document.getElementById('totalValue').textContent = Math.round(totalValue * 100) / 100;
        document.getElementById('totalCategories').textContent = totalCategories;
        document.getElementById('totalWarehouses').textContent = totalWarehouses;
    }
    
    // Mostrar indicador de carga en un gráfico específico
    function showChartLoading(chartId) {
        const container = document.getElementById(chartId);
        if (container) {
            container.innerHTML = `
                <div class="chart-loading">
                    <div class="loading-spinner"></div>
                    <p>Cargando datos...</p>
                </div>
            `;
        }
    }
    
    // Cargar datos específicos de inventarios con API propia
    async function loadData(startDate, endDate) {
        try {
            // Mostrar indicadores de carga
            showChartLoading('inventoryChart');
            showChartLoading('categoriesChart'); 
            showChartLoading('warehousesChart');
            
            console.log('Cargando datos de inventarios...', startDate, 'a', endDate);
            
            const [inventoryData, categoriesData, warehousesData] = await Promise.all([
                fetchInventoryData('purchases_by_group', startDate, endDate),  // Datos reales de compras por grupo
                fetchInventoryData('categories', startDate, endDate),          // Datos de categorías  
                fetchInventoryData('warehouses', startDate, endDate)           // Datos de almacenes
            ]);
            
            console.log('Datos de inventarios cargados:', {
                inventory: inventoryData.length,
                categories: categoriesData.length, 
                warehouses: warehousesData.length
            });
            
            // Actualizar gráficos
            updateInventoryChart(inventoryData);
            updateCategoriesChart(categoriesData);
            updateWarehousesChart(warehousesData);
            
            // Actualizar estadísticas
            updateInventoryStats(inventoryData, categoriesData, warehousesData);
            
        } catch (error) {
            console.error('Error cargando datos de inventarios:', error);
            
            // Mostrar error en los gráficos
            ['inventoryChart', 'categoriesChart', 'warehousesChart'].forEach(chartId => {
                const container = document.getElementById(chartId);
                if (container) {
                    container.innerHTML = `
                        <div class="chart-error">
                            <p>Error cargando datos</p>
                            <small>${error.message}</small>
                        </div>
                    `;
                }
            });
            
            throw error;
        }
    }
    
    // Función específica para obtener datos de inventarios
    async function fetchInventoryData(type, startDate, endDate) {
        // Intentar nueva API primero, fallback a datos mock
        try {
            const url = `/khipus/inventory-dashboard-api?type=${type}&startDate=${startDate}&endDate=${endDate}`;
            
            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Accept': 'application/json; charset=utf-8',
                    'Content-Type': 'application/json; charset=utf-8'
                }
            });
            
            if (response.ok) {
                const data = await response.json();
                if (!data.error) {
                    return DashboardCore.fixDataEncoding(data);
                }
            }
        } catch (error) {
            console.log('Nueva API de inventarios no disponible, usando datos mock...');
        }
        
        // Fallback a datos mock hasta que se despliegue el servlet
        const mockData = {
            'purchases_by_group': [
                {"name":"Alimentos", "peso":15420.75},
                {"name":"Materiales de Construcción", "peso":12800.50},
                {"name":"Herramientas", "peso":8900.25},
                {"name":"Productos Químicos", "peso":6750.80},
                {"name":"Equipos de Oficina", "peso":4200.30},
                {"name":"Insumos Médicos", "peso":3500.60},
                {"name":"Combustibles", "peso":2800.40}
            ],
            'categories': [
                {"name":"Categoría A", "peso":1200},
                {"name":"Categoría B", "peso":800},
                {"name":"Categoría C", "peso":600}
            ],
            'warehouses': [
                {"name":"Almacén Central", "peso":2500},
                {"name":"Almacén Norte", "peso":1800},
                {"name":"Almacén Sur", "peso":1200}
            ]
        };
        
        return mockData[type] || [];
    }
    
    // Inicializar dashboard de inventarios
    function init() {
        console.log('Inventory Dashboard inicializado');
        
        // Configurar listeners para redimensionamiento
        setTimeout(() => {
            ChartUtils.setupResizeListeners(charts);
        }, 1000);
    }
    
    // API pública
    return {
        init,
        loadData,
        updateInventoryChart,
        updateCategoriesChart,
        updateWarehousesChart,
        updateInventoryStats
    };
})();