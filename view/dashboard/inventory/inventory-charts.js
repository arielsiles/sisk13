// Inventory Dashboard - Gráficos y lógica específica de inventarios
window.InventoryDashboard = (function() {
    'use strict';
    
    let inventoryChart, subgroupsChart, areasChart;
    let charts = [];
    let currentSelectedGroup = null;
    let correlatedData = null;
    
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
                tooltipFormat: 'Monto Total: {point.y:,.2f} Bs' // Tooltip con formato Bs
            }
        );
        
        inventoryChart = Highcharts.chart('inventoryChart', config);
        charts[0] = inventoryChart;
    }
    
    // Actualizar gráfico de subgrupos (COLUMNAS VERTICALES)
    function updateSubgroupsChart(data, groupName) {
        const config = ChartUtils.createColumnChartConfig(
            'categoriesChart',
            DashboardCore.fixEncoding('Volumen de compras por Subgrupo'),
            data,
            {
                xAxisTitle: 'Subgrupos',
                yAxisTitle: 'Monto Total (Bs)',
                seriesName: 'Compras',
                color: 'rgba(54, 162, 235, 0.8)',
                rotateLabels: true,
                showDataLabels: false,
                tooltipFormat: 'Monto Total: {point.y:,.2f} Bs'
            }
        );
        
        subgroupsChart = Highcharts.chart('categoriesChart', config);
        charts[1] = subgroupsChart;
    }
    
    // Actualizar gráfico de gastos por área (COLUMNAS VERTICALES)
    function updateAreasExpensesChart(data) {
        // Calcular total de gastos
        const total = data.reduce((sum, item) => sum + (item.peso || 0), 0);
        const formattedTotal = total.toLocaleString('es-BO', { 
            minimumFractionDigits: 2, 
            maximumFractionDigits: 2 
        });
        
        const config = ChartUtils.createColumnChartConfig(
            'areasChart',
            DashboardCore.fixEncoding('Gastos de Inventario por Área'),
            data,
            {
                xAxisTitle: 'Areas de Costo',
                yAxisTitle: 'Monto Total (Bs)',
                seriesName: 'Gastos',
                color: 'rgba(255, 159, 64, 0.8)',
                rotateLabels: true,
                showDataLabels: false,
                tooltipFormat: 'Monto Total: {point.y:,.2f} Bs',
                subtitle: `Total: ${formattedTotal} Bs`
            }
        );
        
        areasChart = Highcharts.chart('areasChart', config);
        charts[2] = areasChart;
    }
    
    // Actualizar estadísticas de inventarios
    function updateInventoryStats(inventoryData, categoriesData, areasData) {
        const totalItems = inventoryData.length; // Total de productos distintos
        const totalValue = inventoryData.reduce((sum, item) => sum + (item.peso || 0), 0) * 100; // Usar peso como valor estimado
        const totalCategories = categoriesData.length;
        const totalAreas = areasData.length;
        
        document.getElementById('totalItems').textContent = totalItems;
        document.getElementById('totalValue').textContent = Math.round(totalValue * 100) / 100;
        document.getElementById('totalCategories').textContent = totalCategories;
        document.getElementById('totalWarehouses').textContent = totalAreas;
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
            showChartLoading('areasChart');
            
            console.log('Cargando datos de inventarios...', startDate, 'a', endDate);
            
            // Usar nueva API correlacionada - una sola llamada para ambos gráficos
            const [correlatedResponse, areasData] = await Promise.all([
                fetchInventoryData('correlated_data', startDate, endDate),     // Datos correlacionados grupos + subgrupos
                fetchInventoryData('areas_expenses', startDate, endDate)       // Datos de gastos por área
            ]);
            
            // Guardar datos correlacionados globalmente
            correlatedData = correlatedResponse;
            
            // Extraer grupos para gráfico 1 y selector
            const groupsData = correlatedData.groups || [];
            
            // Cargar grupos en el selector
            populateGroupSelector(groupsData);
            
            // Obtener subgrupos del primer grupo seleccionado
            let subgroupsData = [];
            if (groupsData && groupsData.length > 0) {
                currentSelectedGroup = groupsData[0];
                subgroupsData = correlatedData.subgroupsByGroup[currentSelectedGroup.id] || [];
            }
            
            console.log('Datos correlacionados de inventarios cargados:', {
                groups: groupsData.length,
                subgroups: subgroupsData.length,
                areas: areasData.length,
                totalGroupsInData: Object.keys(correlatedData.subgroupsByGroup || {}).length
            });
            
            // Actualizar gráficos con datos correlacionados
            updateInventoryChart(groupsData);  // Gráfico 1: grupos
            updateSubgroupsChart(subgroupsData, currentSelectedGroup ? currentSelectedGroup.name : null);  // Gráfico 2: subgrupos
            updateAreasExpensesChart(areasData);
            
            // Actualizar estadísticas
            updateInventoryStats(groupsData, subgroupsData, areasData);
            
        } catch (error) {
            console.error('Error cargando datos de inventarios:', error);
            
            // Mostrar error en los gráficos
            ['inventoryChart', 'categoriesChart', 'areasChart'].forEach(chartId => {
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
    async function fetchInventoryData(type, startDate, endDate, groupId = null) {
        // Intentar nueva API primero, fallback a datos mock
        try {
            let url = `/khipus/inventory-dashboard-api?type=${type}&startDate=${startDate}&endDate=${endDate}`;
            if (groupId) {
                url += `&groupId=${groupId}`;
            }
            
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
            'groups': [
                {"id":"1", "name":"Alimentos"},
                {"id":"2", "name":"Materiales de Construcción"},
                {"id":"3", "name":"Herramientas"}
            ],
            'correlated_data': {
                "groups": [
                    {"id":"1", "name":"Productos Alimenticios", "peso":15420.75},
                    {"id":"2", "name":"Materiales de Construcción", "peso":12800.50},
                    {"id":"3", "name":"Herramientas y Equipos", "peso":8900.25}
                ],
                "subgroupsByGroup": {
                    "1": [
                        {"name":"Lácteos y Derivados", "peso":8000},
                        {"name":"Cereales y Granos", "peso":5000},
                        {"name":"Carnes y Embutidos", "peso":2420.75}
                    ],
                    "2": [
                        {"name":"Cemento y Agregados", "peso":7500},
                        {"name":"Materiales Metálicos", "peso":3300.50},
                        {"name":"Pinturas y Acabados", "peso":2000}
                    ],
                    "3": [
                        {"name":"Herramientas Manuales", "peso":4500},
                        {"name":"Equipos Eléctricos", "peso":2900.25},
                        {"name":"Maquinaria Menor", "peso":1500}
                    ]
                }
            },
            'warehouses': [
                {"name":"Almacén Central", "peso":2500},
                {"name":"Almacén Norte", "peso":1800},
                {"name":"Almacén Sur", "peso":1200}
            ],
            'areas_expenses': [
                {"name":"ADMINISTRACIÓN", "peso":15001.59},
                {"name":"PRODUCCIÓN", "peso":163221.34},
                {"name":"ALMACÉN", "peso":5489.53},
                {"name":"MANTENIMIENTO GRAL", "peso":14881.92},
                {"name":"PROYECTOS", "peso":548237.87},
                {"name":"COMERCIALIZACIÓN", "peso":950.28},
                {"name":"LABORATORIO", "peso":7113.34}
            ]
        };
        
        return mockData[type] || [];
    }
    
    // Actualizar display del total del grupo seleccionado
    function updateGroupTotal(group) {
        const totalElement = document.getElementById('groupTotal');
        if (totalElement && group && group.peso !== undefined) {
            const formattedTotal = group.peso.toLocaleString('es-BO', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
            totalElement.textContent = `Total: ${formattedTotal} Bs`;
        } else if (totalElement) {
            totalElement.textContent = 'Total: - Bs';
        }
    }
    
    // Poblar selector de grupos
    function populateGroupSelector(groupsData) {
        const selector = document.getElementById('groupSelector');
        if (!selector || !groupsData || groupsData.length === 0) {
            return;
        }
        
        // Limpiar opciones existentes
        selector.innerHTML = '<option value="">Seleccione un grupo</option>';
        
        // Agregar opciones de grupos
        groupsData.forEach(group => {
            const option = document.createElement('option');
            option.value = group.id;
            option.textContent = group.name;
            selector.appendChild(option);
        });
        
        // Seleccionar primer grupo por defecto
        if (groupsData.length > 0) {
            selector.value = groupsData[0].id;
            currentSelectedGroup = groupsData[0];
            // Mostrar total del grupo inicial
            updateGroupTotal(currentSelectedGroup);
        }
        
        // Evento de cambio de grupo
        selector.addEventListener('change', async function() {
            const selectedGroupId = this.value;
            if (!selectedGroupId) return;
            
            // Encontrar el grupo seleccionado
            const selectedGroup = groupsData.find(g => g.id === selectedGroupId);
            if (!selectedGroup) return;
            
            currentSelectedGroup = selectedGroup;
            
            // Actualizar total del grupo seleccionado
            updateGroupTotal(selectedGroup);
            
            try {
                // Obtener subgrupos del grupo seleccionado desde datos correlacionados en memoria
                const subgroupsData = correlatedData.subgroupsByGroup[selectedGroupId] || [];
                
                // Actualizar gráfico inmediatamente (no necesita loading ya que los datos están en memoria)
                updateSubgroupsChart(subgroupsData, selectedGroup.name);
                
                console.log('Subgrupos cargados para grupo:', selectedGroup.name, subgroupsData.length, 'elementos desde datos correlacionados');
                
            } catch (error) {
                console.error('Error cargando subgrupos:', error);
                const container = document.getElementById('categoriesChart');
                if (container) {
                    container.innerHTML = `
                        <div class="chart-error">
                            <p>Error cargando subgrupos</p>
                            <small>${error.message}</small>
                        </div>
                    `;
                }
            }
        });
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
        updateSubgroupsChart,
        updateAreasExpensesChart,
        updateInventoryStats,
        populateGroupSelector,
        updateGroupTotal
    };
})();