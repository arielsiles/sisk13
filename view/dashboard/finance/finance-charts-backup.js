// Finance Dashboard - Gráficos y lógica específica de finanzas
window.FinanceDashboard = (function() {
    'use strict';
    
    let incomeChart, expensesChart, cashFlowChart;
    let ventasChart, otrosIngresosChart, fletesTransportesChart, materialDirectoChart, manoObraChart;
    let charts = [];
    
    // Configuración para clasificación dinámica de los 5 gráficos
    // Los gráficos se generan automáticamente basados en los valores de rootNameAccount
    const DYNAMIC_CHARTS_CONFIG = {
        maxIngresos: 2,    // Máximo 2 gráficos de ingresos (lado izquierdo)
        maxEgresos: 3,     // Máximo 3 gráficos de egresos (lado derecho)
        minItems: 1,       // Mínimo items por gráfico para mostrarlo
        minAmount: 100     // Monto mínimo para considerar significativo
    };
    
    // ========================================================================
    // FUNCIONES PARA GRÁFICOS DETALLADOS DEL ESTADO DE RESULTADOS EXTENDIDO
    // ========================================================================
    // Estas funciones procesan datos del reporte "Estado_Resultados_Ext.pdf"
    // que se genera desde view/accounting/profitAndLossExtendedReport.xhtml
    // y usa las consultas SQL del ProfitAndLossExtendedReportAction.java
    //
    // Los 5 nuevos gráficos implementados son:
    // - INGRESOS: 1) VENTAS, 2) OTROS INGRESOS  
    // - EGRESOS:  3) FLETES Y TRANSPORTES, 4) MATERIAL DIRECTO, 5) MANO DE OBRA
    // ========================================================================
    
    // Función para clasificar datos en los 5 gráficos específicos
    function classifyDetailedReportData(rawData) {
        if (!rawData || !Array.isArray(rawData)) {
            return {
                ventas: [],
                otrosIngresos: [],
                fletesTransportes: [],
                materialDirecto: [],
                manoObra: []
            };
        }
        
        // LOGGING TEMPORAL PARA DIAGNÓSTICO
        console.log('=== DIAGNÓSTICO: DATOS RECIBIDOS PARA CLASIFICACIÓN ===');
        console.log('Total items:', rawData.length);
        
        // Mostrar estructura de los primeros items
        rawData.slice(0, 5).forEach((item, i) => {
            console.log(`Item ${i + 1}:`, {
                accountType: item.accountType,
                rootAccount: item.rootAccount,
                rootNameAccount: item.rootNameAccount,
                account: item.account,
                nameAccount: item.nameAccount,
                debit: item.debit,
                credit: item.credit
            });
        });
        
        // Contar items por accountType
        const byType = rawData.reduce((acc, item) => {
            acc[item.accountType] = (acc[item.accountType] || 0) + 1;
            return acc;
        }, {});
        console.log('Items por tipo:', byType);
        
        // Mostrar rootAccounts únicos para egresos
        const egresosRootAccounts = [...new Set(
            rawData.filter(item => item.accountType === 'E')
                   .map(item => item.rootAccount)
        )].sort();
        console.log('RootAccounts de EGRESOS encontrados:', egresosRootAccounts);
        
        // Mostrar patrones que estamos buscando para FLETES Y TRANSPORTES
        console.log('Patrones FLETES_TRANSPORTES que buscamos:', ACCOUNT_MAPPING.FLETES_TRANSPORTES.rootAccountPatterns);
        console.log('Keywords FLETES_TRANSPORTES que buscamos:', ACCOUNT_MAPPING.FLETES_TRANSPORTES.keywords);
        
        const result = {
            ventas: [],
            otrosIngresos: [],
            fletesTransportes: [],
            materialDirecto: [],
            manoObra: []
        };
        
        rawData.forEach((item, index) => {
            // Calcular el monto neto según el tipo de cuenta
            const isIngreso = item.accountType === 'I';
            const montoNeto = isIngreso ? 
                (item.credit || 0) - (item.debit || 0) :  // Para ingresos: crédito - débito
                (item.debit || 0) - (item.credit || 0);   // Para egresos: débito - crédito
            
            if (montoNeto === 0) {
                return;
            }
            
            const dataPoint = {
                name: item.nameAccount || 'Sin Nombre',
                peso: Math.abs(montoNeto) // Usar valor absoluto para gráficos
            };
            
            // Clasificar usando el sistema de mapeo
            let classified = false;
            
            // 1. Verificar por rootAccount primero (más específico)
            for (const [category, config] of Object.entries(ACCOUNT_MAPPING)) {
                if (config.rootAccountPatterns.some(pattern => 
                    item.rootAccount && item.rootAccount.toString().startsWith(pattern)
                )) {
                    const categoryKey = category.toLowerCase();
                    if (categoryKey === 'ventas') {
                        result.ventas.push(dataPoint);
                    } else if (categoryKey === 'otros_ingresos') {
                        result.otrosIngresos.push(dataPoint);
                    } else if (categoryKey === 'fletes_transportes') {
                        console.log(`✅ FLETES Y TRANSPORTES - Item clasificado por rootAccount:`, {
                            rootAccount: item.rootAccount,
                            pattern: config.rootAccountPatterns.find(p => item.rootAccount.toString().startsWith(p)),
                            nameAccount: item.nameAccount,
                            montoNeto: montoNeto,
                            debit: item.debit,
                            credit: item.credit
                        });
                        result.fletesTransportes.push(dataPoint);
                    } else if (categoryKey === 'material_directo') {
                        result.materialDirecto.push(dataPoint);
                    } else if (categoryKey === 'mano_obra') {
                        result.manoObra.push(dataPoint);
                    }
                    classified = true;
                    break;
                }
            }
            
            // 2. Si no se clasificó por rootAccount, intentar por keywords
            if (!classified) {
                const accountName = (item.nameAccount || '').toUpperCase();
                const rootName = (item.rootNameAccount || '').toUpperCase();
                const fullText = accountName + ' ' + rootName;
                
                for (const [category, config] of Object.entries(ACCOUNT_MAPPING)) {
                    if (config.keywords.some(keyword => 
                        fullText.includes(keyword.toUpperCase())
                    )) {
                        const categoryKey = category.toLowerCase();
                        if (categoryKey === 'ventas') {
                            result.ventas.push(dataPoint);
                        } else if (categoryKey === 'otros_ingresos') {
                            result.otrosIngresos.push(dataPoint);
                        } else if (categoryKey === 'fletes_transportes') {
                            console.log(`✅ FLETES Y TRANSPORTES - Item clasificado por keyword:`, {
                                keyword: config.keywords.find(k => fullText.includes(k.toUpperCase())),
                                fullText: fullText,
                                nameAccount: item.nameAccount,
                                rootNameAccount: item.rootNameAccount,
                                montoNeto: montoNeto
                            });
                            result.fletesTransportes.push(dataPoint);
                        } else if (categoryKey === 'material_directo') {
                            result.materialDirecto.push(dataPoint);
                        } else if (categoryKey === 'mano_obra') {
                            result.manoObra.push(dataPoint);
                        }
                        classified = true;
                        break;
                    }
                }
            }
        });
        
        // Ordenar items por monto descendente dentro de cada categoría
        Object.keys(result).forEach(key => {
            result[key].sort((a, b) => b.peso - a.peso);
        });
        
        // LOGGING FINAL DE RESULTADOS
        console.log('=== RESULTADO FINAL CLASIFICACIÓN ===');
        console.log(`VENTAS: ${result.ventas.length} items`);
        console.log(`OTROS INGRESOS: ${result.otrosIngresos.length} items`);
        console.log(`FLETES Y TRANSPORTES: ${result.fletesTransportes.length} items`);
        console.log(`MATERIAL DIRECTO: ${result.materialDirecto.length} items`);
        console.log(`MANO DE OBRA: ${result.manoObra.length} items`);
        
        // DETALLE ESPECÍFICO DE FLETES Y TRANSPORTES
        if (result.fletesTransportes.length > 0) {
            console.log('=== DETALLE FLETES Y TRANSPORTES ===');
            result.fletesTransportes.forEach((item, i) => {
                console.log(`${i + 1}. ${item.name}: Bs ${item.peso.toLocaleString()}`);
            });
            const total = result.fletesTransportes.reduce((sum, item) => sum + item.peso, 0);
            console.log(`TOTAL FLETES Y TRANSPORTES: Bs ${total.toLocaleString()}`);
        } else {
            console.log('⚠️ NO SE CLASIFICARON ITEMS EN FLETES Y TRANSPORTES');
        }
        
        return result;
    }
    
    // Función para actualizar los 5 gráficos específicos basándose en datos clasificados
    function updateDetailedCharts(classifiedData) {
        
        // Verificar que los contenedores existen
        const containers = {
            ventas: document.getElementById('ventasChart'),
            otrosIngresos: document.getElementById('otrosIngresosChart'),
            fletesTransportes: document.getElementById('fletesTransportesChart'),
            materialDirecto: document.getElementById('materialDirectoChart'),
            manoObra: document.getElementById('manoObraChart')
        };
        
        // Verificar contenedores
        let missingContainers = [];
        Object.entries(containers).forEach(([key, container]) => {
            if (!container) {
                missingContainers.push(key);
            }
        });
        
        if (missingContainers.length > 0) {
            return;
        }
        
        // Actualizar cada gráfico específico
        setTimeout(() => {
            if (classifiedData.ventas.length > 0) {
                updateVentasChart(classifiedData.ventas);
            }
            if (classifiedData.otrosIngresos.length > 0) {
                updateOtrosIngresosChart(classifiedData.otrosIngresos);
            }
            if (classifiedData.fletesTransportes.length > 0) {
                updateFletesTransportesChart(classifiedData.fletesTransportes);
            }
            if (classifiedData.materialDirecto.length > 0) {
                updateMaterialDirectoChart(classifiedData.materialDirecto);
            }
            if (classifiedData.manoObra.length > 0) {
                updateManoObraChart(classifiedData.manoObra);
            }
        }, 100);
    }
    
    // Función para limpiar gráficos existentes
    function clearExistingCharts() {
        // Destruir gráficos Highcharts existentes
        if (ventasChart) { ventasChart.destroy(); ventasChart = null; }
        if (otrosIngresosChart) { otrosIngresosChart.destroy(); otrosIngresosChart = null; }
        if (fletesTransportesChart) { fletesTransportesChart.destroy(); fletesTransportesChart = null; }
        if (materialDirectoChart) { materialDirectoChart.destroy(); materialDirectoChart = null; }
        if (manoObraChart) { manoObraChart.destroy(); manoObraChart = null; }
        
        // Limpiar array de gráficos
        charts.forEach(chart => {
            if (chart && chart.destroy) chart.destroy();
        });
        charts = [];
    }
    
    // Actualizar gráfico de ingresos (BARRA HORIZONTAL)
    function updateIncomeChart(data) {
        // Los datos ya vienen con el formato correcto del servlet (peso representa monto real)
        const incomeData = data.map(item => ({
            name: item.name,
            y: item.peso || 0,  // Usar directamente el valor del servlet
            color: (item.peso || 0) >= 0 ? 'rgba(40, 167, 69, 0.8)' : 'rgba(255, 99, 132, 0.8)'  // Verde para positivos, rojo para negativos
        }));
        
        // Calcular monto total para mostrar en el título
        const montoTotal = data.reduce((sum, item) => sum + (item.peso || 0), 0);
        const montoFormateado = 'Bs ' + Highcharts.numberFormat(montoTotal, 0, '.', ',');
        
        // Configuración específica para barras horizontales de ingresos
        const config = {
            chart: {
                type: 'bar',  // Barras horizontales
                height: 400,
                backgroundColor: 'transparent'
            },
            title: {
                text: `Ingresos por Concepto (incluye descuentos)<br><span style="font-size: 12px; color: #666; font-weight: normal;">Total: ${montoFormateado}</span>`,
                useHTML: true,
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
        
        // Calcular monto total para mostrar en el título
        const montoTotal = data.reduce((sum, item) => sum + (item.peso || 0), 0);
        const montoFormateado = 'Bs ' + Highcharts.numberFormat(montoTotal, 0, '.', ',');
        
        // Configuración específica para barras horizontales de gastos
        const config = {
            chart: {
                type: 'bar',  // Barras horizontales
                height: 400,
                backgroundColor: 'transparent'
            },
            title: {
                text: `Gastos por Concepto<br><span style="font-size: 12px; color: #666; font-weight: normal;">Total: ${montoFormateado}</span>`,
                useHTML: true,
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
    
    // Actualizar gráfico de VENTAS (BARRA HORIZONTAL)
    // Basado en Estado_Resultados_Ext.pdf - Sección INGRESOS > VENTAS
    function updateVentasChart(data) {
        // Calcular monto total para mostrar en el título
        const montoTotal = data.reduce((sum, item) => sum + (item.peso || 0), 0);
        const montoFormateado = 'Bs ' + Highcharts.numberFormat(montoTotal, 0, '.', ',');
        
        const config = {
            chart: {
                type: 'bar',
                height: 400,
                backgroundColor: 'transparent'
            },
            title: {
                text: `Ingresos por Ventas<br><span style="font-size: 12px; color: #666; font-weight: normal;">Total: ${montoFormateado}</span>`,
                useHTML: true,
                style: { color: '#333', fontSize: '16px', fontWeight: 'bold' }
            },
            xAxis: {
                categories: data.map(item => item.name),
                title: { text: 'Productos/Servicios' },
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
                    color: 'rgba(40, 167, 69, 0.8)' // Verde para ventas
                }
            },
            series: [{
                name: 'Ventas',
                data: data.map(item => item.peso),
                color: 'rgba(40, 167, 69, 0.8)'
            }],
            tooltip: {
                formatter: function() {
                    return '<b>' + this.point.category + '</b><br/>' +
                           'Ventas: Bs ' + Highcharts.numberFormat(this.y, 2, '.', ',');
                }
            },
            legend: { enabled: false },
            credits: { enabled: false }
        };
        
        ventasChart = Highcharts.chart('ventasChart', config);
        charts[3] = ventasChart;
    }
    
    // Actualizar gráfico de OTROS INGRESOS (BARRA HORIZONTAL)
    // Basado en Estado_Resultados_Ext.pdf - Sección INGRESOS > OTROS INGRESOS
    function updateOtrosIngresosChart(data) {
        // Calcular monto total para mostrar en el título
        const montoTotal = data.reduce((sum, item) => sum + (item.peso || 0), 0);
        const montoFormateado = 'Bs ' + Highcharts.numberFormat(montoTotal, 0, '.', ',');
        
        const config = {
            chart: {
                type: 'bar',
                height: 400,
                backgroundColor: 'transparent'
            },
            title: {
                text: `Otros Ingresos<br><span style="font-size: 12px; color: #666; font-weight: normal;">Total: ${montoFormateado}</span>`,
                useHTML: true,
                style: { color: '#333', fontSize: '16px', fontWeight: 'bold' }
            },
            xAxis: {
                categories: data.map(item => item.name),
                title: { text: 'Conceptos de Ingresos' },
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
                    color: 'rgba(34, 139, 34, 0.8)' // Verde más oscuro para otros ingresos
                }
            },
            series: [{
                name: 'Otros Ingresos',
                data: data.map(item => item.peso),
                color: 'rgba(34, 139, 34, 0.8)'
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
        
        otrosIngresosChart = Highcharts.chart('otrosIngresosChart', config);
        charts[4] = otrosIngresosChart;
    }
    
    // Actualizar gráfico de FLETES Y TRANSPORTES (BARRA HORIZONTAL)
    // Basado en Estado_Resultados_Ext.pdf - Sección EGRESOS > FLETES Y TRANSPORTES
    function updateFletesTransportesChart(data) {
        // Calcular monto total para mostrar en el título
        const montoTotal = data.reduce((sum, item) => sum + (item.peso || 0), 0);
        const montoFormateado = 'Bs ' + Highcharts.numberFormat(montoTotal, 0, '.', ',');
        
        const config = {
            chart: {
                type: 'bar',
                height: 400,
                backgroundColor: 'transparent'
            },
            title: {
                text: `Gastos en Fletes y Transportes<br><span style="font-size: 12px; color: #666; font-weight: normal;">Total: ${montoFormateado}</span>`,
                useHTML: true,
                style: { color: '#333', fontSize: '16px', fontWeight: 'bold' }
            },
            xAxis: {
                categories: data.map(item => item.name),
                title: { text: 'Conceptos de Transporte' },
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
                    color: 'rgba(220, 53, 69, 0.8)' // Rojo para gastos
                }
            },
            series: [{
                name: 'Fletes y Transportes',
                data: data.map(item => item.peso),
                color: 'rgba(220, 53, 69, 0.8)'
            }],
            tooltip: {
                formatter: function() {
                    return '<b>' + this.point.category + '</b><br/>' +
                           'Gasto: Bs ' + Highcharts.numberFormat(this.y, 2, '.', ',');
                }
            },
            legend: { enabled: false },
            credits: { enabled: false }
        };
        
        fletesTransportesChart = Highcharts.chart('fletesTransportesChart', config);
        charts[5] = fletesTransportesChart;
    }
    
    // Actualizar gráfico de MATERIAL DIRECTO (BARRA HORIZONTAL)
    // Basado en Estado_Resultados_Ext.pdf - Sección EGRESOS > MATERIAL DIRECTO
    function updateMaterialDirectoChart(data) {
        // Calcular monto total para mostrar en el título
        const montoTotal = data.reduce((sum, item) => sum + (item.peso || 0), 0);
        const montoFormateado = 'Bs ' + Highcharts.numberFormat(montoTotal, 0, '.', ',');
        
        const config = {
            chart: {
                type: 'bar',
                height: 400,
                backgroundColor: 'transparent'
            },
            title: {
                text: `Gastos en Material Directo<br><span style="font-size: 12px; color: #666; font-weight: normal;">Total: ${montoFormateado}</span>`,
                useHTML: true,
                style: { color: '#333', fontSize: '16px', fontWeight: 'bold' }
            },
            xAxis: {
                categories: data.map(item => item.name),
                title: { text: 'Materiales' },
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
                    color: 'rgba(255, 99, 132, 0.8)' // Rojo más claro para material directo
                }
            },
            series: [{
                name: 'Material Directo',
                data: data.map(item => item.peso),
                color: 'rgba(255, 99, 132, 0.8)'
            }],
            tooltip: {
                formatter: function() {
                    return '<b>' + this.point.category + '</b><br/>' +
                           'Gasto: Bs ' + Highcharts.numberFormat(this.y, 2, '.', ',');
                }
            },
            legend: { enabled: false },
            credits: { enabled: false }
        };
        
        materialDirectoChart = Highcharts.chart('materialDirectoChart', config);
        charts[6] = materialDirectoChart;
    }
    
    // Actualizar gráfico de MANO DE OBRA (BARRA HORIZONTAL)
    // Basado en Estado_Resultados_Ext.pdf - Sección EGRESOS > MANO DE OBRA
    function updateManoObraChart(data) {
        // Calcular monto total para mostrar en el título
        const montoTotal = data.reduce((sum, item) => sum + (item.peso || 0), 0);
        const montoFormateado = 'Bs ' + Highcharts.numberFormat(montoTotal, 0, '.', ',');
        
        const config = {
            chart: {
                type: 'bar',
                height: 400,
                backgroundColor: 'transparent'
            },
            title: {
                text: `Gastos en Mano de Obra<br><span style="font-size: 12px; color: #666; font-weight: normal;">Total: ${montoFormateado}</span>`,
                useHTML: true,
                style: { color: '#333', fontSize: '16px', fontWeight: 'bold' }
            },
            xAxis: {
                categories: data.map(item => item.name),
                title: { text: 'Conceptos de Personal' },
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
                    color: 'rgba(139, 0, 0, 0.8)' // Rojo oscuro para mano de obra
                }
            },
            series: [{
                name: 'Mano de Obra',
                data: data.map(item => item.peso),
                color: 'rgba(139, 0, 0, 0.8)'
            }],
            tooltip: {
                formatter: function() {
                    return '<b>' + this.point.category + '</b><br/>' +
                           'Gasto: Bs ' + Highcharts.numberFormat(this.y, 2, '.', ',');
                }
            },
            legend: { enabled: false },
            credits: { enabled: false }
        };
        
        manoObraChart = Highcharts.chart('manoObraChart', config);
        charts[7] = manoObraChart;
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
            // Cargar datos originales para mantener compatibilidad
            const [incomeData, expensesData, cashFlowData, detailedReportData] = await Promise.all([
                fetchFinanceData('income', startDate, endDate),    // Datos de ingresos
                fetchFinanceData('expenses', startDate, endDate),  // Datos de gastos
                fetchFinanceData('cash_flow', startDate, endDate),  // Datos de flujo de caja
                fetchFinanceData('detailed_report', startDate, endDate)  // Datos detallados del reporte
            ]);
            
            // Actualizar gráficos originales
            updateIncomeChart(incomeData);
            updateExpensesChart(expensesData);
            updateCashFlowChart(cashFlowData);
            
            // Clasificar datos en los 5 gráficos específicos
            const classifiedData = classifyDetailedReportData(detailedReportData);
            
            // Actualizar los 5 gráficos específicos
            updateDetailedCharts(classifiedData);
            
            // Actualizar estadísticas
            updateFinanceStats(incomeData, expensesData, cashFlowData);
            
        } catch (error) {
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
            // API no disponible, usar datos mock
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
            ],
            'detailed_report': [
                // INGRESOS - VENTAS (2 items del PDF)
                {"accountType":"I", "rootAccount":"41001", "rootNameAccount":"VENTAS", "account":"41001001", "nameAccount":"VENTA DE MOLIENDA Y GRANULADO ULEXITA", "debit":0, "credit":7501126.06},
                {"accountType":"I", "rootAccount":"41001", "rootNameAccount":"VENTAS", "account":"41001002", "nameAccount":"VENTA DE BENTONITA Y BARITINA", "debit":0, "credit":848367.48},
                
                // INGRESOS - OTROS INGRESOS (7 items del PDF completos - rootAccount diversificados)
                {"accountType":"I", "rootAccount":"41005", "rootNameAccount":"OTROS INGRESOS", "account":"41005001", "nameAccount":"INGRESOS POR SERVICIOS DE LABORATORIO", "debit":0, "credit":3422.00},
                {"accountType":"I", "rootAccount":"41006", "rootNameAccount":"OTROS INGRESOS", "account":"41006001", "nameAccount":"OTROS INGRESOS", "debit":0, "credit":6770.82},
                {"accountType":"I", "rootAccount":"41007", "rootNameAccount":"OTROS INGRESOS", "account":"41007001", "nameAccount":"INGRESO POR DEVOLUCIÓN DE REGALÍAS MINERAS", "debit":0, "credit":167454.82},
                {"accountType":"I", "rootAccount":"41008", "rootNameAccount":"OTROS INGRESOS", "account":"41008001", "nameAccount":"DONACIONES PERSONALES", "debit":0, "credit":5000.00},
                {"accountType":"I", "rootAccount":"41009", "rootNameAccount":"OTROS INGRESOS", "account":"41009001", "nameAccount":"INGRESOS POR SERVICIOS PRESTADOS TRANSPORTE", "debit":0, "credit":62640.00},
                {"accountType":"I", "rootAccount":"41010", "rootNameAccount":"OTROS INGRESOS", "account":"41010001", "nameAccount":"INGRESOS POR SERVICIOS PRESTADOS TRANSPORTE", "debit":0, "credit":290933.00},
                {"accountType":"I", "rootAccount":"41011", "rootNameAccount":"OTROS INGRESOS", "account":"41011001", "nameAccount":"INGRESO POR SERVICIO DE PESAJE EN BALANZA", "debit":0, "credit":4830.00},
                
                // EGRESOS - FLETES Y TRANSPORTES (6 items del PDF completos - rootAccount diversificados)
                {"accountType":"E", "rootAccount":"51001", "rootNameAccount":"FLETES Y TRANSPORTES", "account":"51001001", "nameAccount":"FLETES Y TRANSPORTES DE MATERIA PRIMA", "debit":380590.88, "credit":0},
                {"accountType":"E", "rootAccount":"51002", "rootNameAccount":"FLETES Y TRANSPORTES", "account":"51002001", "nameAccount":"FLETES Y TRANSPORTES DE PRODUCTOS TERMINADOS", "debit":95597.06, "credit":0},
                {"accountType":"E", "rootAccount":"51003", "rootNameAccount":"FLETES Y TRANSPORTES", "account":"51003001", "nameAccount":"FLETES Y TRANSPORTES EN GENERAL", "debit":5795.00, "credit":0},
                {"accountType":"E", "rootAccount":"51004", "rootNameAccount":"FLETES Y TRANSPORTES", "account":"51004001", "nameAccount":"DESCUENTOS SOBRE VENTAS", "debit":138800.03, "credit":0},
                {"accountType":"E", "rootAccount":"51005", "rootNameAccount":"FLETES Y TRANSPORTES", "account":"51005001", "nameAccount":"GASTOS DE ESTADIA EN FRONTERA", "debit":50605.38, "credit":0},
                {"accountType":"E", "rootAccount":"51006", "rootNameAccount":"FLETES Y TRANSPORTES", "account":"51006001", "nameAccount":"DESCUENTO SOBRE SERVICIOS", "debit":610.00, "credit":0},
                
                // EGRESOS - MATERIAL DIRECTO (2 items del PDF - rootAccount diversificados)
                {"accountType":"E", "rootAccount":"52001", "rootNameAccount":"MATERIAL DIRECTO", "account":"52001001", "nameAccount":"BARITINA", "debit":500.00, "credit":0},
                {"accountType":"E", "rootAccount":"52002", "rootNameAccount":"MATERIAL DIRECTO", "account":"52002001", "nameAccount":"ULEXITA", "debit":11188.12, "credit":0},
                
                // EGRESOS - MANO DE OBRA (6 items del PDF completos - rootAccount diversificados)
                {"accountType":"E", "rootAccount":"53001", "rootNameAccount":"MANO DE OBRA", "account":"53001001", "nameAccount":"SUELDOS Y SALARIOS", "debit":445161.35, "credit":0},
                {"accountType":"E", "rootAccount":"53002", "rootNameAccount":"MANO DE OBRA", "account":"53002001", "nameAccount":"AGUINALDOS PRODUCCION", "debit":47641.78, "credit":0},
                {"accountType":"E", "rootAccount":"53003", "rootNameAccount":"MANO DE OBRA", "account":"53003001", "nameAccount":"INDEMNIZACIONES PRODUCCION", "debit":41852.98, "credit":0},
                {"accountType":"E", "rootAccount":"53004", "rootNameAccount":"MANO DE OBRA", "account":"53004001", "nameAccount":"BONOS AL PERSONAL DE PRODUCCION", "debit":9000.00, "credit":0},
                {"accountType":"E", "rootAccount":"53005", "rootNameAccount":"MANO DE OBRA", "account":"53005001", "nameAccount":"PERSONAL EVENTUAL", "debit":13610.42, "credit":0},
                {"accountType":"E", "rootAccount":"53006", "rootNameAccount":"MANO DE OBRA", "account":"53006001", "nameAccount":"SERVICIOS PRESTADOS POR TERCEROS", "debit":2000.00, "credit":0}
            ]
        };
        
        return mockData[type] || [];
    }
    
    // Inicializar dashboard de finanzas
    function init() {
        
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
        updateFinanceStats,
        // Funciones específicas para los 5 gráficos
        classifyDetailedReportData,
        updateDetailedCharts,
        clearExistingCharts,
        updateVentasChart,
        updateOtrosIngresosChart,
        updateFletesTransportesChart,
        updateMaterialDirectoChart,
        updateManoObraChart
    };
})();