// Finance Dashboard - Gráficos y lógica específica de finanzas
window.FinanceDashboard = (function() {
    'use strict';
    
    let incomeChart, expensesChart, cashFlowChart;
    let charts = [];
    
    // Actualizar gráfico de ingresos (BARRA HORIZONTAL)
    function updateIncomeChart(data) {
        // Convertir peso a monto para ingresos
        const incomeData = data.map(item => ({
            ...item,
            peso: (item.peso || 0) * 1000
        }));
        
        const config = ChartUtils.createBarChartConfig(
            'incomeChart',
            'Ingresos por Concepto',
            incomeData,
            {
                xAxisTitle: 'Conceptos',
                yAxisTitle: 'Monto ($)',
                dataLabelFormat: '${y:.2f}',
                seriesName: 'Ingresos',
                color: 'rgba(40, 167, 69, 0.8)'
            }
        );
        
        incomeChart = Highcharts.chart('incomeChart', config);
        charts[0] = incomeChart;
    }
    
    // Actualizar gráfico de gastos (DONUT)
    function updateExpensesChart(data) {
        // Convertir peso a monto para gastos
        const expenseData = data.map(item => ({
            ...item,
            peso: (item.peso || 0) * 800
        }));
        
        const config = ChartUtils.createDonutChartConfig(
            'expensesChart',
            DashboardCore.fixEncoding('Distribución de Gastos'),
            expenseData,
            {
                colors: ['#DC3545', '#FD7E14', '#FFC107', '#28A745', '#17A2B8', '#6F42C1', '#E83E8C', '#6C757D'],
                seriesName: 'Gastos',
                tooltipFormat: '<b>{point.name}</b>: {point.percentage:.1f}% (${point.y:.2f})'
            }
        );
        
        expensesChart = Highcharts.chart('expensesChart', config);
        charts[1] = expensesChart;
    }
    
    // Actualizar gráfico de flujo de caja (PIE)
    function updateCashFlowChart(data) {
        // Convertir peso a monto para flujo de caja
        const cashFlowData = data.map(item => ({
            ...item,
            peso: (item.peso || 0) * 600
        }));
        
        const config = ChartUtils.createPieChartConfig(
            'cashFlowChart',
            DashboardCore.fixEncoding('Flujo de Caja por Período'),
            cashFlowData,
            {
                colors: ['#28A745', '#DC3545', '#17A2B8', '#FFC107', '#6F42C1', '#FD7E14', '#20C997', '#6C757D'],
                seriesName: 'Flujo',
                tooltipFormat: '<b>{point.name}</b>: {point.percentage:.1f}% (${point.y:.2f})'
            }
        );
        
        cashFlowChart = Highcharts.chart('cashFlowChart', config);
        charts[2] = cashFlowChart;
    }
    
    // Actualizar estadísticas de finanzas
    function updateFinanceStats(incomeData, expensesData, cashFlowData) {
        const totalIncome = incomeData.reduce((sum, item) => sum + (item.peso || 0), 0) * 1000; // Usar peso como base monetaria
        const totalExpenses = expensesData.reduce((sum, item) => sum + (item.peso || 0), 0) * 800; // Usar peso como base monetaria
        const totalBalance = totalIncome - totalExpenses;
        const totalAccounts = incomeData.length + expensesData.length;
        
        document.getElementById('totalIncome').textContent = Math.round(totalIncome * 100) / 100;
        document.getElementById('totalExpenses').textContent = Math.round(totalExpenses * 100) / 100;
        document.getElementById('totalBalance').textContent = Math.round(totalBalance * 100) / 100;
        document.getElementById('totalAccounts').textContent = totalAccounts;
    }
    
    // Cargar datos específicos de finanzas
    async function loadData(startDate, endDate) {
        try {
            const [incomeData, expensesData, cashFlowData] = await Promise.all([
                fetchFinanceData('income', startDate, endDate),    // Datos de ingresos
                fetchFinanceData('expenses', startDate, endDate),  // Datos de gastos
                fetchFinanceData('cash_flow', startDate, endDate)  // Datos de flujo de caja
            ]);
            
            // Actualizar gráficos  
            updateIncomeChart(incomeData);
            updateExpensesChart(expensesData);
            updateCashFlowChart(cashFlowData);
            
            // Actualizar estadísticas
            updateFinanceStats(incomeData, expensesData, cashFlowData);
            
        } catch (error) {
            console.error('Error cargando datos de finanzas:', error);
            throw error;
        }
    }
    
    // Función específica para obtener datos de finanzas
    async function fetchFinanceData(type, startDate, endDate) {
        // Intentar nueva API primero, fallback a datos mock
        try {
            const url = `/khipus/finance-dashboard-api?type=${type}&startDate=${startDate}&endDate=${endDate}`;
            
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
            console.log('Nueva API de finanzas no disponible, usando datos mock...');
        }
        
        // Fallback a datos mock hasta que se despliegue el servlet
        const mockData = {
            'income': [
                {"name":"Ventas Directas", "peso":25000},
                {"name":"Servicios", "peso":18000},
                {"name":"Comisiones", "peso":12000},
                {"name":"Intereses", "peso":5500}
            ],
            'expenses': [
                {"name":"Gastos Operativos", "peso":15000},
                {"name":"Sueldos", "peso":22000},
                {"name":"Materiales", "peso":8000},
                {"name":"Servicios", "peso":6500}
            ],
            'cash_flow': [
                {"name":"Enero", "peso":5000},
                {"name":"Febrero", "peso":7500},
                {"name":"Marzo", "peso":-2000},
                {"name":"Abril", "peso":8200},
                {"name":"Mayo", "peso":6800}
            ]
        };
        
        return mockData[type] || [];
    }
    
    // Inicializar dashboard de finanzas
    function init() {
        console.log('Finance Dashboard inicializado');
        
        // Configurar listeners para redimensionamiento
        setTimeout(() => {
            ChartUtils.setupResizeListeners(charts);
        }, 1000);
    }
    
    // API pública
    return {
        init,
        loadData,
        updateIncomeChart,
        updateExpensesChart,
        updateCashFlowChart,
        updateFinanceStats
    };
})();