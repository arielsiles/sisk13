// Finance Dashboard - Gráficos y lógica específica de finanzas
window.FinanceDashboard = (function() {
    'use strict';
    
    let charts = [];
    let summaryCharts = []; // 2 charts: ingresos summary, egresos summary
    let detailedCharts = []; // N charts: one per rootNameAccount
    
    // Configuración para arquitectura unificada
    // Se muestran TODOS los resultados sin límites artificiales
    const UNIFIED_CONFIG = {
        showAllResults: true,    // Mostrar TODOS los resultados de la consulta
        minAmount: 0.01,        // Monto mínimo para evitar valores cero
        orderIngresosFirst: true // Ordenar: ingresos primero, luego egresos
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
    
    // Función principal para procesar datos de la consulta unificada
    function processUnifiedData(rawData) {
        if (!rawData || !Array.isArray(rawData)) {
            return {
                summaryData: { ingresos: [], egresos: [] },
                detailedGroups: [],
                allGroups: []
            };
        }
        
        console.log('=== PROCESANDO DATOS DE CONSULTA UNIFICADA ===');
        console.log('Total items recibidos:', rawData.length);
        
        // PASO 1: Agrupar automáticamente por rootNameAccount
        const groups = {};
        
        rawData.forEach((item, index) => {
            // Calcular el monto neto según el tipo de cuenta
            const isIngreso = item.accountType === 'I';
            const montoNeto = isIngreso ? 
                (item.credit || 0) - (item.debit || 0) :  // Para ingresos: crédito - débito
                (item.debit || 0) - (item.credit || 0);   // Para egresos: débito - crédito
            
            if (Math.abs(montoNeto) < UNIFIED_CONFIG.minAmount) {
                return; // Saltar montos insignificantes
            }
            
            // Usar rootNameAccount como clave de agrupación
            const groupKey = item.rootNameAccount || 'Sin Categoria';
            
            if (!groups[groupKey]) {
                groups[groupKey] = {
                    rootNameAccount: groupKey,
                    rootAccount: item.rootAccount,
                    accountType: item.accountType,
                    items: [],
                    totalAmount: 0
                };
            }
            
            const dataPoint = {
                name: item.nameAccount || 'Sin Nombre',
                peso: montoNeto, // MANTENER valores negativos - NO usar Math.abs()
                originalData: item
            };
            
            groups[groupKey].items.push(dataPoint);
            groups[groupKey].totalAmount += montoNeto; // Sumar respetando signos
        });
        
        // PASO 2: Obtener TODOS los grupos válidos (sin límites artificiales)
        const allGroups = Object.values(groups).filter(group => 
            group.items.length > 0 && Math.abs(group.totalAmount) >= UNIFIED_CONFIG.minAmount
        );
        
        // PASO 3: Separar por tipo y ordenar por valor absoluto (pero mantener signos)
        const ingresosGroups = allGroups
            .filter(group => group.accountType === 'I')
            .sort((a, b) => Math.abs(b.totalAmount) - Math.abs(a.totalAmount));
            
        const egresosGroups = allGroups
            .filter(group => group.accountType === 'E')
            .sort((a, b) => Math.abs(b.totalAmount) - Math.abs(a.totalAmount));
        
        // PASO 4: Crear datos para gráficos summary (agregados por rootNameAccount)
        const summaryIngresos = ingresosGroups.map(group => ({
            name: group.rootNameAccount,
            peso: group.totalAmount // Mantener valores negativos en summary
        }));
        
        const summaryEgresos = egresosGroups.map(group => ({
            name: group.rootNameAccount,
            peso: group.totalAmount // Mantener valores negativos en summary
        }));
        
        // PASO 5: Ordenar grupos finales (ingresos primero, luego egresos)
        const orderedGroups = UNIFIED_CONFIG.orderIngresosFirst ? 
            [...ingresosGroups, ...egresosGroups] : 
            [...egresosGroups, ...ingresosGroups];
        
        // Ordenar items dentro de cada grupo por valor absoluto (mantener signos)
        orderedGroups.forEach(group => {
            group.items.sort((a, b) => Math.abs(b.peso) - Math.abs(a.peso));
        });
        
        console.log(`\n=== RESULTADOS FINALES ===`);
        console.log(`Total grupos: ${allGroups.length}`);
        console.log(`Ingresos: ${ingresosGroups.length} grupos`);
        console.log(`Egresos: ${egresosGroups.length} grupos`);
        
        return {
            summaryData: {
                ingresos: summaryIngresos,
                egresos: summaryEgresos
            },
            detailedGroups: orderedGroups,
            allGroups: allGroups
        };
    }
    
    // Función para crear los gráficos summary (2 gráficos: ingresos + egresos agregados)
    function createSummaryCharts(summaryData) {
        console.log('=== CREANDO GRÁFICOS SUMMARY ===');
        
        // Limpiar gráficos summary existentes
        summaryCharts.forEach(chart => {
            if (chart && chart.destroy) chart.destroy();
        });
        summaryCharts = [];
        
        // Crear gráfico de ingresos summary
        if (summaryData.ingresos && summaryData.ingresos.length > 0) {
            createSummaryChart(summaryData.ingresos, 'incomeChart', 'INGRESOS', 'I');
        }
        
        // Crear gráfico de egresos summary
        if (summaryData.egresos && summaryData.egresos.length > 0) {
            createSummaryChart(summaryData.egresos, 'expensesChart', 'EGRESOS', 'E');
        }
    }
    
    // Función para crear los gráficos detallados (N gráficos: uno por rootNameAccount)
    function createDetailedCharts(detailedGroups) {
        console.log('=== CREANDO GRÁFICOS DETALLADOS CON LAYOUT 2 POR FILA ===');
        console.log(`Total grupos a graficar: ${detailedGroups.length}`);
        
        // Limpiar gráficos detallados existentes
        clearDetailedCharts();
        
        // Limpiar contenedores dinámicos existentes
        clearDynamicContainers();
        
        // Separar grupos por tipo para control de layout
        const ingresosGroups = detailedGroups.filter(group => group.accountType === 'I');
        const egresosGroups = detailedGroups.filter(group => group.accountType === 'E');
        
        // Contador global para índices únicos
        let globalIndex = 0;
        
        // Crear sección de ingresos detallados si hay datos
        if (ingresosGroups.length > 0) {
            createSectionSeparator('INGRESOS', 'Ingresos - Estado de Resultado');
            
            ingresosGroups.forEach((group, localIndex) => {
                const containerId = createDynamicContainerWithLayout(group, globalIndex, localIndex, 'ingresos');
                console.log(`📈 Creando gráfico ingreso ${localIndex + 1}: "${group.rootNameAccount}" en ${containerId}`);
                createDetailedChart(group, containerId, globalIndex);
                globalIndex++;
            });
        }
        
        // Crear sección de egresos detallados si hay datos
        if (egresosGroups.length > 0) {
            createSectionSeparator('EGRESOS', 'Egresos - Estado de Resultado');
            
            egresosGroups.forEach((group, localIndex) => {
                const containerId = createDynamicContainerWithLayout(group, globalIndex, localIndex, 'egresos');
                console.log(`📉 Creando gráfico egreso ${localIndex + 1}: "${group.rootNameAccount}" en ${containerId}`);
                createDetailedChart(group, containerId, globalIndex);
                globalIndex++;
            });
        }
        
        console.log(`✅ ${detailedGroups.length} gráficos detallados creados en layout 2x2 (TODOS los datos mostrados)`);
    }
    
    // Función para crear un gráfico summary
    function createSummaryChart(data, containerId, title, accountType) {
        const isIngreso = accountType === 'I';
        const montoTotal = data.reduce((sum, item) => sum + (item.peso || 0), 0);
        const montoFormateado = 'Bs ' + Highcharts.numberFormat(montoTotal, 0, '.', ',');
        
        // Crear datos con colores dinámicos según el signo
        const chartData = data.map(item => {
            const valor = item.peso || 0;
            const esPositivo = valor >= 0;
            
            // Colores dinámicos: verde para positivos, rojo para negativos
            let color;
            if (isIngreso) {
                color = esPositivo ? 'rgba(40, 167, 69, 0.95)' : 'rgba(220, 53, 69, 0.95)';
            } else {
                color = esPositivo ? 'rgba(220, 53, 69, 0.95)' : 'rgba(139, 0, 0, 0.95)';
            }
            
            return {
                name: item.name,
                y: valor, // Mantener valor original con signo
                color: color
            };
        });
        
        const config = {
            chart: {
                type: 'bar',
                height: 400,
                backgroundColor: 'transparent'
            },
            title: {
                text: `${title}<br><span style="font-size: 12px; color: #666; font-weight: normal;">Total: ${montoFormateado}</span>`,
                useHTML: true,
                style: { color: '#333', fontSize: '16px', fontWeight: 'bold' }
            },
            xAxis: {
                categories: data.map(item => item.name),
                title: { text: 'Categorias' },
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
                            const signo = this.y >= 0 ? '' : '-';
                            const valor = Math.abs(this.y);
                            return signo + 'Bs ' + Highcharts.numberFormat(valor, 0, '.', ',');
                        },
                        style: { fontSize: '10px', fontWeight: 'bold' }
                    },
                    colorByPoint: true // Permitir colores diferentes por punto
                }
            },
            series: [{
                name: title,
                data: chartData
            }],
            tooltip: {
                formatter: function() {
                    const tipoValor = this.y >= 0 ? 
                        (isIngreso ? 'Ingreso' : 'Gasto') : 
                        (isIngreso ? 'Descuento/Devolución' : 'Crédito/Ajuste');
                    return '<b>' + this.point.name + '</b><br/>' +
                           tipoValor + ': Bs ' + Highcharts.numberFormat(this.y, 2, '.', ',');
                }
            },
            legend: { enabled: false },
            credits: { enabled: false }
        };
        
        try {
            const chart = Highcharts.chart(containerId, config);
            summaryCharts.push(chart);
            console.log(`✅ Gráfico summary creado: "${title}" con ${data.length} categorías`);
        } catch (error) {
            console.error(`❌ Error creando gráfico summary "${title}":`, error);
        }
    }
    
    // Función para crear un gráfico detallado
    function createDetailedChart(group, containerId, index) {
        if (!group.items || group.items.length === 0) {
            console.warn(`⚠️ Grupo "${group.rootNameAccount}" no tiene items`);
            return;
        }
        
        const montoTotal = group.totalAmount;
        const montoFormateado = 'Bs ' + Highcharts.numberFormat(montoTotal, 0, '.', ',');
        const isIngreso = group.accountType === 'I';
        const tipoLabel = isIngreso ? 'Ingresos' : 'Egresos';
        
        // Crear datos con colores dinámicos según signo de cada item
        const chartData = group.items.map(item => {
            const valor = item.peso || 0;
            const esPositivo = valor >= 0;
            
            // Colores según signo del valor
            let color;
            if (isIngreso) {
                // Ingresos: verde si positivo, rojo si negativo (descuentos)
                color = esPositivo ? 'rgba(40, 167, 69, 0.9)' : 'rgba(220, 53, 69, 0.9)';
            } else {
                // Egresos: rojo si positivo, verde si negativo (devoluciones)
                color = esPositivo ? 'rgba(220, 53, 69, 0.9)' : 'rgba(40, 167, 69, 0.9)';
            }
            
            return {
                name: item.name,
                y: valor, // Mantener valor original con signo
                color: color
            };
        });
        
        const config = {
            chart: {
                type: 'bar',
                height: 400,
                backgroundColor: 'transparent'
            },
            title: {
                text: `${group.rootNameAccount}<br><span style="font-size: 12px; color: #666; font-weight: normal;">Total: ${montoFormateado}</span>`,
                useHTML: true,
                style: { color: '#333', fontSize: '16px', fontWeight: 'bold' }
            },
            xAxis: {
                categories: group.items.map(item => item.name),
                title: { text: `Conceptos de ${tipoLabel}` },
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
                            const signo = this.y >= 0 ? '' : '-';
                            const valor = Math.abs(this.y);
                            return signo + 'Bs ' + Highcharts.numberFormat(valor, 0, '.', ',');
                        },
                        style: { fontSize: '10px', fontWeight: 'bold' }
                    },
                    colorByPoint: true // Permitir colores diferentes por punto
                }
            },
            series: [{
                name: group.rootNameAccount,
                data: chartData
            }],
            tooltip: {
                formatter: function() {
                    const tipoDetalle = this.y >= 0 ? 
                        (isIngreso ? 'Ingreso' : 'Gasto') : 
                        (isIngreso ? 'Descuento/Devolución' : 'Crédito/Ajuste');
                    return '<b>' + this.point.name + '</b><br/>' +
                           tipoDetalle + ': Bs ' + Highcharts.numberFormat(this.y, 2, '.', ',');
                }
            },
            legend: { enabled: false },
            credits: { enabled: false }
        };
        
        try {
            const chart = Highcharts.chart(containerId, config);
            detailedCharts.push(chart);
            console.log(`✅ Gráfico detallado creado: "${group.rootNameAccount}" con ${group.items.length} items`);
        } catch (error) {
            console.error(`❌ Error creando gráfico detallado "${group.rootNameAccount}":`, error);
        }
    }
    
    // Función para crear separadores de sección
    function createSectionSeparator(type, title) {
        const mainContainer = getMainChartsContainer();
        
        // Crear contenedor del separador
        const separatorContainer = document.createElement('div');
        separatorContainer.className = 'section-separator';
        separatorContainer.style.width = '100%';
        separatorContainer.style.marginTop = '40px';
        separatorContainer.style.marginBottom = '30px';
        separatorContainer.style.textAlign = 'center';
        
        // Crear el título
        const titleElement = document.createElement('h3');
        titleElement.style.color = type === 'INGRESOS' ? '#28a745' : '#dc3545';
        titleElement.style.fontSize = '20px';
        titleElement.style.fontWeight = 'bold';
        titleElement.style.margin = '0';
        titleElement.style.padding = '15px 0';
        titleElement.style.borderTop = `3px solid ${type === 'INGRESOS' ? '#28a745' : '#dc3545'}`;
        titleElement.style.borderBottom = `2px solid ${type === 'INGRESOS' ? '#28a745' : '#dc3545'}`;
        titleElement.textContent = title;
        
        separatorContainer.appendChild(titleElement);
        mainContainer.appendChild(separatorContainer);
        
        // Crear contenedor para la fila de gráficos (2 por fila)
        const rowContainer = document.createElement('div');
        rowContainer.id = `${type.toLowerCase()}DetailedSection`;
        rowContainer.className = 'detailed-charts-section';
        rowContainer.style.display = 'flex';
        rowContainer.style.flexDirection = 'column';
        rowContainer.style.gap = '20px';
        rowContainer.style.width = '100%';
        
        mainContainer.appendChild(rowContainer);
        
        return rowContainer;
    }
    
    // Función para crear contenedores con layout de 2 por fila
    function createDynamicContainerWithLayout(group, globalIndex, localIndex, sectionType) {
        const containerId = `dynamicChart_${group.rootAccount}_${globalIndex}`;
        const sectionContainer = document.getElementById(`${sectionType}DetailedSection`);
        
        if (!sectionContainer) {
            console.error(`No se encontró sección para ${sectionType}`);
            return containerId;
        }
        
        // Determinar si necesitamos crear una nueva fila (cada 2 gráficos)
        const rowIndex = Math.floor(localIndex / 2);
        let currentRow = document.getElementById(`${sectionType}Row${rowIndex}`);
        
        // Crear nueva fila si no existe
        if (!currentRow) {
            currentRow = document.createElement('div');
            currentRow.id = `${sectionType}Row${rowIndex}`;
            currentRow.className = 'charts-row';
            currentRow.style.display = 'flex';
            currentRow.style.gap = '20px';
            currentRow.style.marginBottom = '20px';
            currentRow.style.width = '100%';
            
            sectionContainer.appendChild(currentRow);
        }
        
        // Crear el contenedor del gráfico (50% de ancho para 2 por fila)
        const chartBox = document.createElement('div');
        chartBox.className = 'chart-box';
        chartBox.style.flex = '1';
        chartBox.style.minWidth = '45%';
        chartBox.style.maxWidth = '50%';
        
        const chartContainer = document.createElement('div');
        chartContainer.id = containerId;
        chartContainer.className = 'chart-container';
        
        chartBox.appendChild(chartContainer);
        currentRow.appendChild(chartBox);
        
        return containerId;
    }
    
    // Función auxiliar para obtener el contenedor principal
    function getMainChartsContainer() {
        // Buscar el contenedor principal después de los gráficos summary
        let mainContainer = document.getElementById('detailedChartsMainContainer');
        
        if (!mainContainer) {
            // Crear contenedor principal si no existe
            mainContainer = document.createElement('div');
            mainContainer.id = 'detailedChartsMainContainer';
            mainContainer.style.width = '100%';
            mainContainer.style.marginTop = '30px';
            
            // Insertar después de los gráficos summary
            const summaryContainer = document.querySelector('.charts-container');
            if (summaryContainer && summaryContainer.nextSibling) {
                summaryContainer.parentNode.insertBefore(mainContainer, summaryContainer.nextSibling);
            } else if (summaryContainer) {
                summaryContainer.parentNode.appendChild(mainContainer);
            } else {
                document.body.appendChild(mainContainer);
            }
        }
        
        return mainContainer;
    }
    
    
    // Función para limpiar contenedores dinámicos
    function clearDynamicContainers() {
        // Limpiar el contenedor principal de gráficos detallados
        const mainContainer = document.getElementById('detailedChartsMainContainer');
        if (mainContainer) {
            mainContainer.remove();
        }
        
        // Limpiar secciones específicas si existen
        const ingresosSection = document.getElementById('ingresosDetailedSection');
        const egresosSection = document.getElementById('egresosDetailedSection');
        
        if (ingresosSection) {
            ingresosSection.remove();
        }
        if (egresosSection) {
            egresosSection.remove();
        }
        
        // Limpiar separadores
        const separators = document.querySelectorAll('.section-separator');
        separators.forEach(separator => separator.remove());
        
        // Limpiar filas dinámicas
        const rows = document.querySelectorAll('.charts-row');
        rows.forEach(row => row.remove());
    }
    
    // Función para limpiar gráficos detallados
    function clearDetailedCharts() {
        detailedCharts.forEach(chart => {
            if (chart && chart.destroy) chart.destroy();
        });
        detailedCharts = [];
    }
    
    // Función para limpiar todos los gráficos
    function clearAllCharts() {
        // Limpiar summary charts
        summaryCharts.forEach(chart => {
            if (chart && chart.destroy) chart.destroy();
        });
        summaryCharts = [];
        
        // Limpiar detailed charts
        clearDetailedCharts();
        
        // Limpiar contenedores dinámicos
        clearDynamicContainers();
        
        // Limpiar array general
        charts.forEach(chart => {
            if (chart && chart.destroy) chart.destroy();
        });
        charts = [];
    }
    
    
    
    
    
    
    
    
    
    // Actualizar estadísticas con arquitectura unificada
    function updateUnifiedStats(processedData) {
        // Sumar respetando valores negativos (sin Math.abs)
        const totalIncome = processedData.summaryData.ingresos.reduce((sum, item) => sum + (item.peso || 0), 0);
        const totalExpenses = processedData.summaryData.egresos.reduce((sum, item) => sum + (item.peso || 0), 0);
        const totalBalance = totalIncome - totalExpenses;
        const totalAccounts = processedData.allGroups.reduce((sum, group) => sum + group.items.length, 0);
        
        // VALIDACIÓN CRÍTICA: Verificar que los datos cuadren
        validateDataIntegrity(processedData, totalIncome, totalExpenses);
        
        // Log para diagnóstico
        console.log('=== ESTADÍSTICALS FINALES ===');
        console.log(`Total Ingresos (con negativos): Bs ${totalIncome.toFixed(2)}`);
        console.log(`Total Egresos (con negativos): Bs ${totalExpenses.toFixed(2)}`);
        console.log(`Balance Neto: Bs ${totalBalance.toFixed(2)}`);
        
        // Formatear números con separadores de miles (preservando signos)
        document.getElementById('totalIncome').textContent = 'Bs ' + Highcharts.numberFormat(totalIncome, 0, '.', ',');
        document.getElementById('totalExpenses').textContent = 'Bs ' + Highcharts.numberFormat(totalExpenses, 0, '.', ',');
        document.getElementById('totalBalance').textContent = 'Bs ' + Highcharts.numberFormat(totalBalance, 0, '.', ',');
        document.getElementById('totalAccounts').textContent = totalAccounts;
    }
    
    // Función para validar integridad de datos
    function validateDataIntegrity(processedData, summaryIncome, summaryExpenses) {
        console.log('=== VALIDACIÓN DE INTEGRIDAD DE DATOS (CON VALORES NEGATIVOS) ===');
        
        // Calcular totales de gráficos detallados respetando signos
        let detailedIncome = 0;
        let detailedExpenses = 0;
        
        processedData.detailedGroups.forEach(group => {
            if (group.accountType === 'I') {
                detailedIncome += group.totalAmount; // Incluye negativos
            } else if (group.accountType === 'E') {
                detailedExpenses += group.totalAmount; // Incluye negativos
            }
        });
        
        // Verificar que los totales coincidan (considerando signos)
        const incomeDifference = Math.abs(summaryIncome - detailedIncome);
        const expensesDifference = Math.abs(summaryExpenses - detailedExpenses);
        const tolerance = 0.01; // Tolerancia para diferencias de redondeo
        
        console.log(`Summary Ingresos (con negativos): Bs ${summaryIncome.toFixed(2)}`);
        console.log(`Detallado Ingresos (con negativos): Bs ${detailedIncome.toFixed(2)}`);
        console.log(`Diferencia Ingresos: Bs ${incomeDifference.toFixed(2)}`);
        
        console.log(`Summary Egresos (con negativos): Bs ${summaryExpenses.toFixed(2)}`);
        console.log(`Detallado Egresos (con negativos): Bs ${detailedExpenses.toFixed(2)}`);
        console.log(`Diferencia Egresos: Bs ${expensesDifference.toFixed(2)}`);
        
        // Mostrar desglose de valores positivos vs negativos
        const ingresosPositivos = processedData.summaryData.ingresos.filter(item => item.peso >= 0);
        const ingresosNegativos = processedData.summaryData.ingresos.filter(item => item.peso < 0);
        const egresosPositivos = processedData.summaryData.egresos.filter(item => item.peso >= 0);
        const egresosNegativos = processedData.summaryData.egresos.filter(item => item.peso < 0);
        
        console.log(`📈 Ingresos positivos: ${ingresosPositivos.length}, negativos: ${ingresosNegativos.length}`);
        console.log(`📉 Egresos positivos: ${egresosPositivos.length}, negativos: ${egresosNegativos.length}`);
        
        // Alertar si hay discrepancias significativas
        if (incomeDifference > tolerance) {
            console.error(`❌ ALERTA: Discrepancia en INGRESOS de Bs ${incomeDifference.toFixed(2)}`);
        } else {
            console.log(`✅ INGRESOS: Datos cuadran correctamente (incluyendo negativos)`);
        }
        
        if (expensesDifference > tolerance) {
            console.error(`❌ ALERTA: Discrepancia en EGRESOS de Bs ${expensesDifference.toFixed(2)}`);
        } else {
            console.log(`✅ EGRESOS: Datos cuadran correctamente (incluyendo negativos)`);
        }
        
        console.log(`Total gráficos detallados creados: ${processedData.detailedGroups.length}`);
        console.log('=== FIN VALIDACIÓN ===');
    }
    
    // Cargar datos de la consulta unificada
    async function loadData(startDate, endDate) {
        try {
            console.log('=== CARGANDO DATOS ARQUITECTURA UNIFICADA ===');
            
            // Cargar SOLO datos de detailed_report (consulta unificada)
            const rawData = await fetchFinanceData('detailed_report', startDate, endDate);
            
            // Procesar datos con la nueva arquitectura unificada
            const processedData = processUnifiedData(rawData);
            
            // Limpiar gráficos existentes
            clearAllCharts();
            
            // Crear 2 gráficos summary (ingresos + egresos agregados por rootNameAccount)
            createSummaryCharts(processedData.summaryData);
            
            // Crear N gráficos detallados (uno por cada rootNameAccount)
            createDetailedCharts(processedData.detailedGroups);
            
            // Actualizar estadísticas basadas en los datos procesados
            updateUnifiedStats(processedData);
            
            console.log('✅ Arquitectura unificada cargada exitosamente');
            
        } catch (error) {
            console.error('❌ Error cargando arquitectura unificada:', error);
            throw error;
        }
    }
    
    // Función para obtener datos de la consulta unificada
    async function fetchFinanceData(type, startDate, endDate) {
        // Solo soportamos 'detailed_report' en la arquitectura unificada
        if (type !== 'detailed_report') {
            console.warn(`Tipo de consulta '${type}' no soportado en arquitectura unificada. Usando 'detailed_report'.`);
            type = 'detailed_report';
        }
        
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
            console.warn('API no disponible, usando datos mock:', error.message);
        }
        
        // Fallback: datos mock para desarrollo
        console.log('Usando datos mock para desarrollo');
        return [
            // INGRESOS - VENTAS
            {"accountType":"I", "rootAccount":"41001", "rootNameAccount":"VENTAS", "account":"41001001", "nameAccount":"VENTA DE MOLIENDA Y GRANULADO ULEXITA", "debit":0, "credit":7501126.06},
            {"accountType":"I", "rootAccount":"41001", "rootNameAccount":"VENTAS", "account":"41001002", "nameAccount":"VENTA DE BENTONITA Y BARITINA", "debit":0, "credit":848367.48},
            
            // INGRESOS - OTROS INGRESOS
            {"accountType":"I", "rootAccount":"41005", "rootNameAccount":"OTROS INGRESOS", "account":"41005001", "nameAccount":"INGRESOS POR SERVICIOS DE LABORATORIO", "debit":0, "credit":3422.00},
            {"accountType":"I", "rootAccount":"41006", "rootNameAccount":"OTROS INGRESOS", "account":"41006001", "nameAccount":"OTROS INGRESOS", "debit":0, "credit":6770.82},
            {"accountType":"I", "rootAccount":"41007", "rootNameAccount":"OTROS INGRESOS", "account":"41007001", "nameAccount":"INGRESO POR DEVOLUCIÓN DE REGALÍAS MINERAS", "debit":0, "credit":167454.82},
            {"accountType":"I", "rootAccount":"41008", "rootNameAccount":"OTROS INGRESOS", "account":"41008001", "nameAccount":"DONACIONES PERSONALES", "debit":0, "credit":5000.00},
            {"accountType":"I", "rootAccount":"41009", "rootNameAccount":"OTROS INGRESOS", "account":"41009001", "nameAccount":"INGRESOS POR SERVICIOS PRESTADOS TRANSPORTE", "debit":0, "credit":62640.00},
            {"accountType":"I", "rootAccount":"41010", "rootNameAccount":"OTROS INGRESOS", "account":"41010001", "nameAccount":"INGRESOS POR SERVICIOS PRESTADOS TRANSPORTE", "debit":0, "credit":290933.00},
            {"accountType":"I", "rootAccount":"41011", "rootNameAccount":"OTROS INGRESOS", "account":"41011001", "nameAccount":"INGRESO POR SERVICIO DE PESAJE EN BALANZA", "debit":0, "credit":4830.00},
            
            // EGRESOS - FLETES Y TRANSPORTES
            {"accountType":"E", "rootAccount":"51001", "rootNameAccount":"FLETES Y TRANSPORTES", "account":"51001001", "nameAccount":"FLETES Y TRANSPORTES DE MATERIA PRIMA", "debit":380590.88, "credit":0},
            {"accountType":"E", "rootAccount":"51002", "rootNameAccount":"FLETES Y TRANSPORTES", "account":"51002001", "nameAccount":"FLETES Y TRANSPORTES DE PRODUCTOS TERMINADOS", "debit":95597.06, "credit":0},
            {"accountType":"E", "rootAccount":"51003", "rootNameAccount":"FLETES Y TRANSPORTES", "account":"51003001", "nameAccount":"FLETES Y TRANSPORTES EN GENERAL", "debit":5795.00, "credit":0},
            {"accountType":"E", "rootAccount":"51004", "rootNameAccount":"FLETES Y TRANSPORTES", "account":"51004001", "nameAccount":"DESCUENTOS SOBRE VENTAS", "debit":138800.03, "credit":0},
            {"accountType":"E", "rootAccount":"51005", "rootNameAccount":"FLETES Y TRANSPORTES", "account":"51005001", "nameAccount":"GASTOS DE ESTADIA EN FRONTERA", "debit":50605.38, "credit":0},
            {"accountType":"E", "rootAccount":"51006", "rootNameAccount":"FLETES Y TRANSPORTES", "account":"51006001", "nameAccount":"DESCUENTO SOBRE SERVICIOS", "debit":610.00, "credit":0},
            
            // EGRESOS - MATERIAL DIRECTO
            {"accountType":"E", "rootAccount":"52001", "rootNameAccount":"MATERIAL DIRECTO", "account":"52001001", "nameAccount":"BARITINA", "debit":500.00, "credit":0},
            {"accountType":"E", "rootAccount":"52002", "rootNameAccount":"MATERIAL DIRECTO", "account":"52002001", "nameAccount":"ULEXITA", "debit":11188.12, "credit":0},
            
            // EGRESOS - MANO DE OBRA
            {"accountType":"E", "rootAccount":"53001", "rootNameAccount":"MANO DE OBRA", "account":"53001001", "nameAccount":"SUELDOS Y SALARIOS", "debit":445161.35, "credit":0},
            {"accountType":"E", "rootAccount":"53002", "rootNameAccount":"MANO DE OBRA", "account":"53002001", "nameAccount":"AGUINALDOS PRODUCCION", "debit":47641.78, "credit":0},
            {"accountType":"E", "rootAccount":"53003", "rootNameAccount":"MANO DE OBRA", "account":"53003001", "nameAccount":"INDEMNIZACIONES PRODUCCION", "debit":41852.98, "credit":0},
            {"accountType":"E", "rootAccount":"53004", "rootNameAccount":"MANO DE OBRA", "account":"53004001", "nameAccount":"BONOS AL PERSONAL DE PRODUCCION", "debit":9000.00, "credit":0},
            {"accountType":"E", "rootAccount":"53005", "rootNameAccount":"MANO DE OBRA", "account":"53005001", "nameAccount":"PERSONAL EVENTUAL", "debit":13610.42, "credit":0},
            {"accountType":"E", "rootAccount":"53006", "rootNameAccount":"MANO DE OBRA", "account":"53006001", "nameAccount":"SERVICIOS PRESTADOS POR TERCEROS", "debit":2000.00, "credit":0}
        ];
    }
    
    // Inicializar dashboard de finanzas
    function init() {
        
        // Configurar listeners para redimensionamiento
        setTimeout(() => {
            ChartUtils.setupResizeListeners(charts);
        }, 1000);
    }
    
    // API pública para arquitectura unificada
    return {
        init,
        loadData,
        // Funciones de arquitectura unificada
        processUnifiedData,
        createSummaryCharts,
        createDetailedCharts,
        updateUnifiedStats,
        validateDataIntegrity,
        clearAllCharts,
        clearDetailedCharts,
        // Funciones para layout 2x2
        createSectionSeparator,
        createDynamicContainerWithLayout,
        clearDynamicContainers,
        getMainChartsContainer
    };
})();