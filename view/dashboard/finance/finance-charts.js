// Finance Dashboard - Gráficos y lógica específica de finanzas
window.FinanceDashboard = (function() {
    'use strict';
    
    let incomeChart, expensesChart, cashFlowChart;
    let charts = [];
    
    // Actualizar gráfico de ingresos (BARRA HORIZONTAL)
    function updateIncomeChart(data) {
        // Los datos ya vienen con el formato correcto del servlet (peso representa monto real)
        const incomeData = data.map(item => ({
            name: item.name,
            y: item.peso || 0,  // Usar directamente el valor del servlet
            color: (item.peso || 0) >= 0 ? 'rgba(40, 167, 69, 0.8)' : 'rgba(255, 99, 132, 0.8)'  // Verde para positivos, rojo para negativos
        }));
        
        // Configuración específica para barras horizontales de ingresos
        const config = {
            chart: {
                type: 'bar',  // Barras horizontales
                height: 400,
                backgroundColor: 'transparent'
            },
            title: {
                text: 'Ingresos por Concepto (incluye descuentos)',
                style: { color: '#333', fontSize: '16px', fontWeight: 'bold' }
            },
            xAxis: {
                categories: incomeData.map(item => item.name),
                title: { text: 'Conceptos de Ingresos' },
                labels: { style: { fontSize: '11px' } }
            },
            yAxis: {
                title: { text: 'Monto (Bs)' },
                labels: {
                    formatter: function() {
                        return 'Bs ' + Highcharts.numberFormat(this.value, 0, '.', ',');
                    }
                },
                plotLines: [{
                    value: 0,
                    color: '#666',
                    width: 1,
                    zIndex: 2
                }]
            },
            plotOptions: {
                bar: {
                    dataLabels: {
                        enabled: true,
                        formatter: function() {
                            const sign = this.y >= 0 ? '' : '-';
                            return sign + 'Bs ' + Highcharts.numberFormat(Math.abs(this.y), 0, '.', ',');
                        },
                        style: { fontSize: '10px', fontWeight: 'bold' }
                    },
                    colorByPoint: true  // Permitir colores diferentes por punto
                }
            },
            series: [{
                name: 'Ingresos',
                data: incomeData
            }],
            tooltip: {
                formatter: function() {
                    const tipo = this.y >= 0 ? 'Ingreso' : 'Descuento';
                    return '<b>' + this.point.name + '</b><br/>' +
                           tipo + ': Bs ' + Highcharts.numberFormat(this.y, 2, '.', ',');
                }
            },
            legend: { enabled: false },
            credits: { enabled: false }
        };
        
        incomeChart = Highcharts.chart('incomeChart', config);
        charts[0] = incomeChart;
    }
    
    // Actualizar gráfico de gastos (BARRA HORIZONTAL) 
    function updateExpensesChart(data) {
        // Los datos ya vienen con el formato correcto del servlet (peso representa monto real)
        const expenseData = data.map(item => ({
            ...item,
            peso: item.peso || 0  // Usar directamente el valor del servlet
        }));
        
        // Configuración específica para barras horizontales de gastos
        const config = {
            chart: {
                type: 'bar',  // Barras horizontales
                height: 400,
                backgroundColor: 'transparent'
            },
            title: {
                text: 'Gastos por Concepto',
                style: { color: '#333', fontSize: '16px', fontWeight: 'bold' }
            },
            xAxis: {
                categories: expenseData.map(item => item.name),
                title: { text: 'Conceptos de Gastos' },
                labels: { style: { fontSize: '11px' } }
            },
            yAxis: {
                title: { text: 'Monto (Bs)' },
                labels: {
                    formatter: function() {
                        return 'Bs ' + Highcharts.numberFormat(this.value, 0, '.', ',');
                    }
                }
            },
            plotOptions: {
                bar: {
                    dataLabels: {
                        enabled: true,
                        formatter: function() {
                            return 'Bs ' + Highcharts.numberFormat(this.y, 0, '.', ',');
                        },
                        style: { fontSize: '10px', fontWeight: 'bold' }
                    },
                    color: 'rgba(220, 53, 69, 0.8)'  // Rojo para gastos
                }
            },
            series: [{
                name: 'Gastos',
                data: expenseData.map(item => item.peso),
                color: 'rgba(220, 53, 69, 0.8)'
            }],
            tooltip: {
                formatter: function() {
                    return '<b>' + this.point.category + '</b><br/>' +
                           'Monto: Bs ' + Highcharts.numberFormat(this.y, 2, '.', ',');
                }
            },
            legend: { enabled: false },
            credits: { enabled: false }
        };
        
        expensesChart = Highcharts.chart('expensesChart', config);
        charts[1] = expensesChart;
    }
    
    // Actualizar gráfico de flujo de caja (PIE)
    function updateCashFlowChart(data) {
        // Los datos ya vienen con el formato correcto del servlet (peso representa monto real)
        const cashFlowData = data.map(item => ({
            name: item.name || 'Período',
            y: item.peso || 0,  // Usar directamente el valor del servlet
            color: item.peso >= 0 ? '#28A745' : '#DC3545'  // Verde para positivo, rojo para negativo
        }));
        
        const config = {
            chart: {
                type: 'pie',
                height: 350,
                backgroundColor: 'transparent'
            },
            title: {
                text: 'Flujo de Caja por Periodo',
                style: { color: '#333', fontSize: '16px', fontWeight: 'bold' }
            },
            plotOptions: {
                pie: {
                    allowPointSelect: true,
                    cursor: 'pointer',
                    dataLabels: {
                        enabled: true,
                        format: '<b>{point.name}</b><br>Bs {point.y:,.0f}',
                        style: { fontSize: '10px' }
                    },
                    showInLegend: true
                }
            },
            series: [{
                name: 'Flujo de Caja',
                data: cashFlowData
            }],
            tooltip: {
                formatter: function() {
                    return '<b>' + this.point.name + '</b><br/>' +
                           'Flujo: Bs ' + Highcharts.numberFormat(this.y, 2, '.', ',') + '<br/>' +
                           'Porcentaje: ' + Highcharts.numberFormat(this.percentage, 1) + '%';
                }
            },
            legend: {
                enabled: true,
                itemStyle: { fontSize: '11px' }
            },
            credits: { enabled: false }
        };
        
        cashFlowChart = Highcharts.chart('cashFlowChart', config);
        charts[2] = cashFlowChart;
    }
    
    // Actualizar estadísticas de finanzas
    function updateFinanceStats(incomeData, expensesData, cashFlowData) {
        // Usar directamente los valores reales del servlet (ya en Bolivianos)
        const totalIncome = incomeData.reduce((sum, item) => sum + (item.peso || 0), 0);
        const totalExpenses = expensesData.reduce((sum, item) => sum + (item.peso || 0), 0);
        const totalBalance = totalIncome - totalExpenses;
        const totalAccounts = incomeData.length + expensesData.length;
        
        // Formatear números con separadores de miles
        document.getElementById('totalIncome').textContent = 'Bs ' + Highcharts.numberFormat(totalIncome, 0, '.', ',');
        document.getElementById('totalExpenses').textContent = 'Bs ' + Highcharts.numberFormat(totalExpenses, 0, '.', ',');
        document.getElementById('totalBalance').textContent = 'Bs ' + Highcharts.numberFormat(totalBalance, 0, '.', ',');
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