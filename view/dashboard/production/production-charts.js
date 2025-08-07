// Production Dashboard - Gráficos y lógica específica de producción
window.ProductionDashboard = (function() {
    'use strict';
    
    let producersChart, materialsChart, zonesChart;
    let charts = [];
    
    // Actualizar gráfico de productores (BARRA HORIZONTAL)
    function updateProducersChart(data) {
        const config = ChartUtils.createBarChartConfig(
            'producersChart',
            'Acopio de Proveedores por Peso',
            data,
            {
                xAxisTitle: 'Proveedores',
                yAxisTitle: 'Peso en Toneladas',
                dataLabelFormat: '{y:.2f} Tn',
                seriesName: 'Peso Total',
                color: 'rgba(54, 162, 235, 0.8)'
            }
        );
        
        producersChart = Highcharts.chart('producersChart', config);
        charts[0] = producersChart;
    }
    
    // Actualizar gráfico de materiales (DONUT)
    function updateMaterialsChart(data) {
        const config = ChartUtils.createDonutChartConfig(
            'materialsChart',
            DashboardCore.fixEncoding('Distribución de Materias Primas por Peso'),
            data,
            {
                colors: ChartUtils.colors.production,
                seriesName: 'Materiales',
                tooltipFormat: '<b>{point.name}</b>: {point.percentage:.1f}% ({point.y:.2f} Tn)'
            }
        );
        
        materialsChart = Highcharts.chart('materialsChart', config);
        charts[1] = materialsChart;
    }
    
    // Actualizar gráfico de zonas (PIE)
    function updateZonesChart(data) {
        const config = ChartUtils.createPieChartConfig(
            'zonesChart',
            DashboardCore.fixEncoding('Distribución de Acopio por Zonas'),
            data,
            {
                colors: ChartUtils.colors.production,
                seriesName: 'Zonas',
                tooltipFormat: '<b>{point.name}</b>: {point.percentage:.1f}% ({point.y:.2f} Tn)'
            }
        );
        
        zonesChart = Highcharts.chart('zonesChart', config);
        charts[2] = zonesChart;
    }
    
    // Actualizar estadísticas de producción
    function updateProductionStats(producersData, materialsData, zonesData) {
        const totalProducers = producersData.length;
        const totalWeight = producersData.reduce((sum, item) => sum + item.peso, 0) +
                          materialsData.reduce((sum, item) => sum + item.peso, 0);
        const totalMaterials = materialsData.length;
        const totalZones = zonesData.length;
        
        document.getElementById('totalProducers').textContent = totalProducers;
        document.getElementById('totalWeight').textContent = Math.round(totalWeight * 100) / 100;
        document.getElementById('totalMaterials').textContent = totalMaterials;
        document.getElementById('totalZones').textContent = totalZones;
    }
    
    // Cargar datos específicos de producción
    async function loadData(startDate, endDate) {
        try {
            const [producersData, materialsData, zonesData] = await Promise.all([
                fetchProductionData('producers', startDate, endDate),
                fetchProductionData('materials', startDate, endDate),
                fetchProductionData('zones', startDate, endDate)
            ]);
            
            // Actualizar gráficos
            updateProducersChart(producersData);
            updateMaterialsChart(materialsData);
            updateZonesChart(zonesData);
            
            // Actualizar estadísticas
            updateProductionStats(producersData, materialsData, zonesData);
            
        } catch (error) {
            console.error('Error cargando datos de producción:', error);
            throw error;
        }
    }
    
    // Función específica para obtener datos de producción
    async function fetchProductionData(type, startDate, endDate) {
        // Intentar nueva API primero, fallback a la original
        let url = `/khipus/production-dashboard-api?type=${type}&startDate=${startDate}&endDate=${endDate}`;
        
        try {
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
            console.log('Nueva API no disponible, usando API original...');
        }
        
        // Fallback a la API original
        url = `/khipus/dashboard-api?type=${type}&startDate=${startDate}&endDate=${endDate}`;
        
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Accept': 'application/json; charset=utf-8',
                'Content-Type': 'application/json; charset=utf-8'
            }
        });
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        const data = await response.json();
        
        if (data.error) {
            throw new Error(data.error);
        }
        
        // Aplicar corrección de codificación a todos los datos
        return DashboardCore.fixDataEncoding(data);
    }
    
    // Inicializar dashboard de producción
    function init() {
        console.log('Production Dashboard inicializado');
        
        // Configurar listeners para redimensionamiento
        setTimeout(() => {
            ChartUtils.setupResizeListeners(charts);
        }, 1000);
    }
    
    // API pública
    return {
        init,
        loadData,
        updateProducersChart,
        updateMaterialsChart, 
        updateZonesChart,
        updateProductionStats
    };
})();