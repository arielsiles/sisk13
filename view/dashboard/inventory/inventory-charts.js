// Inventory Dashboard - Gráficos y lógica específica de inventarios
window.InventoryDashboard = (function() {
    'use strict';
    
    let inventoryChart, categoriesChart, warehousesChart;
    let charts = [];
    
    // Actualizar gráfico de inventarios (BARRA HORIZONTAL)
    function updateInventoryChart(data) {
        const config = ChartUtils.createBarChartConfig(
            'inventoryChart',
            'Stock por Productos',
            data,
            {
                xAxisTitle: 'Productos',
                yAxisTitle: 'Cantidad',
                dataLabelFormat: '{y} unid.',
                seriesName: 'Stock',
                color: 'rgba(255, 99, 132, 0.8)'
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
    
    // Cargar datos específicos de inventarios (reutilizando tipos existentes)
    async function loadData(startDate, endDate) {
        try {
            const [inventoryData, categoriesData, warehousesData] = await Promise.all([
                DashboardCore.fetchData('producers', startDate, endDate),  // Reutilizar como productos
                DashboardCore.fetchData('materials', startDate, endDate),  // Reutilizar como categorías  
                DashboardCore.fetchData('zones', startDate, endDate)       // Reutilizar como almacenes
            ]);
            
            // Actualizar gráficos
            updateInventoryChart(inventoryData);
            updateCategoriesChart(categoriesData);
            updateWarehousesChart(warehousesData);
            
            // Actualizar estadísticas
            updateInventoryStats(inventoryData, categoriesData, warehousesData);
            
        } catch (error) {
            console.error('Error cargando datos de inventarios:', error);
            throw error;
        }
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