// Finance Dashboard - Gráficos y lógica específica de finanzas
window.FinanceDashboard = (function() {
    'use strict';
    
    let charts = [];
    let summaryCharts = []; // 2 charts: ingresos summary, egresos summary
    let detailedCharts = []; // N charts: one per rootNameAccount
    let explorerChart = null; // 1 chart: gráfico explorador dinámico
    let currentExplorerData = null; // Datos actuales del explorador para paginación
    
    // Configuración para arquitectura unificada
    // Se muestran TODOS los resultados sin límites artificiales
    const UNIFIED_CONFIG = {
        showAllResults: true,    // Mostrar TODOS los resultados de la consulta
        minAmount: 0.01,        // Monto mínimo para evitar valores cero
        orderIngresosFirst: true // Ordenar: ingresos primero, luego egresos
    };
    
    // Configuración de gráficos por rootAccount (códigos de 10 dígitos)
    // Permite controlar qué gráficos mostrar/ocultar dinámicamente
    const CHART_CONFIG = {
        // INGRESOS
        "4110000000": { 
            enabled: true, 
            accountType: "I", 
            name: "VENTAS",
            description: "Ventas de productos y servicios principales"
        },
        "4120000000": { 
            enabled: true, 
            accountType: "I", 
            name: "DEVOLUCIONES, REBAJAS Y DESCUENTOS DE BIENES Y/O SE",
            description: "Ajustes y correcciones en ventas"
        },
        "4150000000": {
            enabled: true, 
            accountType: "I", 
            name: "OTROS INGRESOS",
            description: "Otros Ingresos"
        },
        "4210000000": { 
            enabled: true, 
            accountType: "I", 
            name: "INGRESOS EXTRAORDINARIOS",
            description: "Ingresos no operacionales"
        },
        
        // EGRESOS
        "5210000000": { 
            enabled: true, 
            accountType: "E", 
            name: "FLETES Y TRANSPORTES",
            description: "Costos de transporte y logística"
        },
        "5310000000": { 
            enabled: true, 
            accountType: "E", 
            name: "MATERIAL DIRECTO",
            description: "Materias primas y materiales directos"
        },
        "5320000000": { 
            enabled: true, 
            accountType: "E", 
            name: "MANO DE OBRA",
            description: "Costos de personal y nómina"
        },
        "5410000000": {
            enabled: true, 
            accountType: "E", 
            name: "COSTOS DE LABORATORIO",
            description: "Gastos relacionados con LABORATORIO"
        },
        "5350000000": {
            enabled: true,
            accountType: "E",
            name: "PROYECTOS",
            description: "Gastos relacionados con PROYECTOS"
        },
        "5560000000": {
            enabled: true,
            accountType: "E",
            name: "TRACTOCAMION",
            description: "Gastos relacionados con TRACTOCAMION"
        }
    };
    
    // ========================================================================
    // FUNCIONES PARA GRÁFICOS DETALLADOS DEL ESTADO DE RESULTADOS EXTENDIDO
    // ========================================================================
    
    // Funciones de gestión de configuración
    function isChartEnabled(rootAccount) {
        const config = CHART_CONFIG[rootAccount];
        return config && config.enabled;
    }
    
    function getChartConfig(rootAccount) {
        return CHART_CONFIG[rootAccount] || null;
    }
    
    function toggleChart(rootAccount, enabled) {
        if (CHART_CONFIG[rootAccount]) {
            CHART_CONFIG[rootAccount].enabled = enabled;
            return true;
        }
        return false;
    }
    
    function getEnabledCharts() {
        return Object.keys(CHART_CONFIG).filter(rootAccount => 
            CHART_CONFIG[rootAccount].enabled
        );
    }
    
    function getChartsByType(accountType) {
        return Object.keys(CHART_CONFIG).filter(rootAccount => 
            CHART_CONFIG[rootAccount].accountType === accountType && 
            CHART_CONFIG[rootAccount].enabled
        );
    }
    
    // Función para obtener cuentas NO configuradas (para gráfico explorador)
    function getUnConfiguredAccounts(rawData) {
        if (!rawData || !Array.isArray(rawData)) {
            return [];
        }
        
        const configuredAccounts = Object.keys(CHART_CONFIG);
        const uniqueAccounts = [];
        
        rawData.forEach(item => {
            // Solo incluir cuentas que NO están en CHART_CONFIG
            if (!configuredAccounts.includes(item.rootAccount)) {
                // Evitar duplicados por rootAccount
                const exists = uniqueAccounts.find(acc => acc.rootAccount === item.rootAccount);
                if (!exists) {
                    uniqueAccounts.push({
                        rootAccount: item.rootAccount,
                        rootNameAccount: item.rootNameAccount || 'Sin Nombre',
                        accountType: item.accountType
                    });
                }
            }
        });
        
        // Separar por tipo y ordenar: Ingresos primero, luego Egresos
        const ingresosAccounts = uniqueAccounts
            .filter(acc => acc.accountType === 'I')
            .sort((a, b) => a.rootNameAccount.localeCompare(b.rootNameAccount));
            
        const egresosAccounts = uniqueAccounts
            .filter(acc => acc.accountType === 'E')
            .sort((a, b) => a.rootNameAccount.localeCompare(b.rootNameAccount));
        
        // Combinar: Ingresos primero, Egresos después
        return [...ingresosAccounts, ...egresosAccounts];
    }
    
    // ========================================================================
    // Estas funciones procesan datos del reporte "Estado_Resultados_Ext.pdf"
    // que se genera desde view/accounting/profitAndLossExtendedReport.xhtml
    // y usa las consultas SQL del ProfitAndLossExtendedReportAction.java
    //
    // Los 5 nuevos gráficos implementados son:
    // - INGRESOS: 1) VENTAS, 2) OTROS INGRESOS  
    // - EGRESOS:  3) FLETES Y TRANSPORTES, 4) MATERIAL DIRECTO, 5) MANO DE OBRA
    // ========================================================================
    
    // Función para procesar TODOS los datos (para summary y cards) - SIN FILTROS
    function processAllDataForSummary(rawData) {
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
        
        return Object.values(groups).filter(group => 
            group.items.length > 0 && Math.abs(group.totalAmount) >= UNIFIED_CONFIG.minAmount
        );
    }
    
    // Función para procesar datos CON FILTROS (para gráficos detallados) - CHART_CONFIG
    function processDataWithChartConfig(rawData) {
        const groups = {};
        
        rawData.forEach((item, index) => {
            // VALIDAR CONFIGURACIÓN: Solo procesar cuentas habilitadas
            const rootAccount = item.rootAccount;
            const chartConfig = CHART_CONFIG[rootAccount];
            
            if (!chartConfig || !chartConfig.enabled) {
                // Saltar cuentas deshabilitadas en configuración
                return;
            }
            
            // VALIDAR CONSISTENCIA: Verificar que el accountType coincida
            if (chartConfig.accountType !== item.accountType) {
                console.warn(`⚠️ Inconsistencia en ${rootAccount}: esperado ${chartConfig.accountType}, encontrado ${item.accountType}`);
            }
            
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
                    totalAmount: 0,
                    configName: chartConfig.name, // Nombre de configuración
                    configDescription: chartConfig.description
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
        
        return Object.values(groups).filter(group => 
            group.items.length > 0 && Math.abs(group.totalAmount) >= UNIFIED_CONFIG.minAmount
        );
    }
    
    // Función principal para procesar datos de la consulta unificada
    function processUnifiedData(rawData) {
        if (!rawData || !Array.isArray(rawData)) {
            return {
                summaryData: { ingresos: [], egresos: [] },
                detailedGroups: [],
                allGroups: []
            };
        }
        
        // FLUJO 1: Procesar TODOS los datos para summary y cards (SIN FILTROS)
        const allGroups = processAllDataForSummary(rawData);
        
        // FLUJO 2: Procesar datos FILTRADOS para gráficos detallados (CON CHART_CONFIG)
        const filteredGroups = processDataWithChartConfig(rawData);
        
        // Separar grupos completos por tipo para summary
        const allIngresosGroups = allGroups
            .filter(group => group.accountType === 'I')
            .sort((a, b) => Math.abs(b.totalAmount) - Math.abs(a.totalAmount));
            
        const allEgresosGroups = allGroups
            .filter(group => group.accountType === 'E')
            .sort((a, b) => Math.abs(b.totalAmount) - Math.abs(a.totalAmount));
        
        // Crear datos para gráficos summary usando TODOS los datos
        const summaryIngresos = allIngresosGroups.map(group => ({
            name: group.rootNameAccount,
            peso: group.totalAmount // Mantener valores negativos en summary
        }));
        
        const summaryEgresos = allEgresosGroups.map(group => ({
            name: group.rootNameAccount,
            peso: group.totalAmount // Mantener valores negativos en summary
        }));
        
        // Separar grupos filtrados por tipo para detailed charts
        const filteredIngresosGroups = filteredGroups
            .filter(group => group.accountType === 'I')
            .sort((a, b) => Math.abs(b.totalAmount) - Math.abs(a.totalAmount));
            
        const filteredEgresosGroups = filteredGroups
            .filter(group => group.accountType === 'E')
            .sort((a, b) => Math.abs(b.totalAmount) - Math.abs(a.totalAmount));
        
        // Ordenar grupos filtrados para detailed charts (ingresos primero, luego egresos)
        const orderedFilteredGroups = UNIFIED_CONFIG.orderIngresosFirst ? 
            [...filteredIngresosGroups, ...filteredEgresosGroups] : 
            [...filteredEgresosGroups, ...filteredIngresosGroups];
        
        // Ordenar items dentro de cada grupo filtrado por valor absoluto (mantener signos)
        orderedFilteredGroups.forEach(group => {
            group.items.sort((a, b) => Math.abs(b.peso) - Math.abs(a.peso));
        });
        
        
        return {
            summaryData: {
                ingresos: summaryIngresos,  // Basado en TODOS los datos
                egresos: summaryEgresos     // Basado en TODOS los datos
            },
            detailedGroups: orderedFilteredGroups,  // Basado en datos FILTRADOS
            allGroups: allGroups  // TODOS los grupos para cards y validaciones
        };
    }
    
    // Función para crear los gráficos summary (2 gráficos: ingresos + egresos agregados)
    function createSummaryCharts(summaryData) {
        
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
                createDetailedChart(group, containerId, globalIndex);
                globalIndex++;
            });
        }
        
        // Crear sección de egresos detallados si hay datos
        if (egresosGroups.length > 0) {
            createSectionSeparator('EGRESOS', 'Egresos - Estado de Resultado');
            
            egresosGroups.forEach((group, localIndex) => {
                const containerId = createDynamicContainerWithLayout(group, globalIndex, localIndex, 'egresos');
                createDetailedChart(group, containerId, globalIndex);
                globalIndex++;
            });
        }
        
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
        } catch (error) {
            console.error(`❌ Error creando gráfico detallado "${group.rootNameAccount}":`, error);
        }
    }
    
    // Función para crear separadores de sección (estilo minimalista)
    function createSectionSeparator(type, title) {
        const mainContainer = getMainChartsContainer();
        
        // Crear contenedor del separador
        const separatorContainer = document.createElement('div');
        separatorContainer.className = 'section-separator';
        separatorContainer.style.width = '100%';
        separatorContainer.style.marginTop = '20px';  // Reducido de 40px
        separatorContainer.style.marginBottom = '15px'; // Reducido de 30px
        separatorContainer.style.textAlign = 'center';
        
        // Determinar icono y título (usando símbolos HTML seguros)
        const isIngresos = type === 'INGRESOS';
        const icon = isIngresos ? '&uarr;' : '&darr;'; // ↑ flecha arriba, ↓ flecha abajo
        const newTitle = isIngresos ? 'Detalle de Ingresos' : 'Detalle de Egresos';
        const fullTitle = `${newTitle}`; // Por ahora solo el título, agregar icono después
        
        // Crear el título minimalista con icono HTML
        const titleElement = document.createElement('h3');
        titleElement.style.color = isIngresos ? '#28a745' : '#dc3545';
        titleElement.style.fontSize = '18px'; // Reducido de 20px
        titleElement.style.fontWeight = 'bold';
        titleElement.style.margin = '0';
        titleElement.style.padding = '8px 0'; // Reducido de 15px
        // Eliminar borderTop - solo mantener bottom más sutil
        titleElement.style.borderBottom = `1px solid ${isIngresos ? '#28a745' : '#dc3545'}`; // Reducido de 2px
        
        // Usar innerHTML para símbolos HTML seguros
        const arrowIcon = isIngresos ? '&#8593;' : '&#8595;'; // ↑ ↓
        titleElement.innerHTML = `${arrowIcon} ${newTitle}`;
        
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
    
    // ========================================================================
    // GRÁFICO EXPLORADOR DINÁMICO - Para cuentas NO configuradas
    // ========================================================================
    
    // Función principal para crear el gráfico explorador con selector
    function createDynamicExplorerChart(rawData) {
        // Obtener cuentas no configuradas
        const unConfiguredAccounts = getUnConfiguredAccounts(rawData);
        
        if (unConfiguredAccounts.length === 0) {
            console.info('ℹ️ No hay cuentas adicionales para explorar (todas están en CHART_CONFIG)');
            return;
        }
        
        // Crear contenedor del explorador
        const explorerContainer = createExplorerContainer();
        
        // Crear selector con cuentas no configuradas
        const selector = createExplorerSelector(unConfiguredAccounts, explorerContainer);
        
        // Crear gráfico inicial con la primera cuenta de EGRESOS
        const firstEgresoAccount = unConfiguredAccounts.find(account => account.accountType === 'E');
        const defaultAccount = firstEgresoAccount || unConfiguredAccounts[0]; // Fallback al primero si no hay egresos
        
        // Sincronizar selector con la cuenta por defecto
        selector.value = defaultAccount.rootAccount;
        
        // Actualizar gráfico con la cuenta por defecto
        updateExplorerChart(defaultAccount.rootAccount, rawData);
        
        console.info(`📊 Grafico explorador creado con ${unConfiguredAccounts.length} cuentas adicionales`);
    }
    
    // Función para crear el contenedor del explorador
    function createExplorerContainer() {
        const mainContainer = getMainChartsContainer();
        
        // Crear separador visual simple
        const separatorDiv = document.createElement('div');
        separatorDiv.className = 'explorer-separator';
        separatorDiv.style.width = '100%';
        separatorDiv.style.marginTop = '40px';
        separatorDiv.style.marginBottom = '20px';
        separatorDiv.style.borderTop = '2px solid #e0e0e0';
        separatorDiv.style.paddingTop = '20px';
        
        mainContainer.appendChild(separatorDiv);
        
        // Crear contenedor principal del explorador
        const explorerSection = document.createElement('div');
        explorerSection.id = 'explorerSection';
        explorerSection.className = 'explorer-section';
        explorerSection.style.width = '100%';
        explorerSection.style.marginBottom = '20px';
        
        // Crear header compacto con flexbox (una sola fila)
        const explorerHeader = document.createElement('div');
        explorerHeader.className = 'explorer-header';
        explorerHeader.style.display = 'flex';
        explorerHeader.style.justifyContent = 'space-between';
        explorerHeader.style.alignItems = 'center';
        explorerHeader.style.marginBottom = '15px';
        explorerHeader.style.padding = '10px 20px';
        explorerHeader.style.backgroundColor = '#f8f9fa';
        explorerHeader.style.borderRadius = '5px';
        explorerHeader.style.border = '1px solid #dee2e6';
        
        // Crear sección izquierda (selector)
        const leftSection = document.createElement('div');
        leftSection.className = 'explorer-left';
        leftSection.style.display = 'flex';
        leftSection.style.alignItems = 'center';
        
        // Crear sección central (totales)
        const centerSection = document.createElement('div');
        centerSection.id = 'explorerCenter';
        centerSection.className = 'explorer-center';
        centerSection.style.display = 'flex';
        centerSection.style.alignItems = 'center';
        centerSection.style.fontSize = '14px';
        centerSection.style.fontWeight = 'bold';
        centerSection.style.color = '#495057';
        
        // Crear sección derecha (título)
        const rightSection = document.createElement('div');
        rightSection.className = 'explorer-right';
        rightSection.style.display = 'flex';
        rightSection.style.alignItems = 'center';
        rightSection.style.fontSize = '16px';
        rightSection.style.fontWeight = 'bold';
        rightSection.style.color = '#666';
        rightSection.innerHTML = '[+] Explorador Dinamico - Otras Cuentas'; // Sin acentos
        
        explorerHeader.appendChild(leftSection);
        explorerHeader.appendChild(centerSection);
        explorerHeader.appendChild(rightSection);
        explorerSection.appendChild(explorerHeader);
        
        // Crear contenedor del gráfico (expandido 100%) con estilo consistente
        const chartBox = document.createElement('div');
        chartBox.className = 'chart-box';
        chartBox.style.width = '100%';
        chartBox.style.backgroundColor = '#fff';
        chartBox.style.border = '1px solid #dee2e6';
        chartBox.style.borderRadius = '5px';
        chartBox.style.padding = '15px';
        
        const chartContainer = document.createElement('div');
        chartContainer.id = 'explorerChart';
        chartContainer.className = 'chart-container';
        chartContainer.style.height = '450px'; // Ajustado para el nuevo header
        
        chartBox.appendChild(chartContainer);
        explorerSection.appendChild(chartBox);
        
        mainContainer.appendChild(explorerSection);
        
        return explorerSection;
    }
    
    // Función para crear el selector minimalista con checkboxes de paginación
    function createExplorerSelector(unConfiguredAccounts, container) {
        const leftSection = container.querySelector('.explorer-left');
        
        // Crear selector sin label (minimalista) con margen
        const selector = document.createElement('select');
        selector.id = 'explorerSelector';
        selector.className = 'explorer-selector';
        selector.style.padding = '8px 12px';
        selector.style.border = '1px solid #ced4da';
        selector.style.borderRadius = '4px';
        selector.style.backgroundColor = '#fff';
        selector.style.fontSize = '14px';
        selector.style.minWidth = '280px';
        selector.style.cursor = 'pointer';
        selector.style.marginRight = '15px'; // Espacio para checkboxes
        
        // Agregar opciones con separadores visuales por tipo
        unConfiguredAccounts.forEach((account, index) => {
            // Agregar separador "Ingresos" antes del primer ingreso
            if (index === 0 && account.accountType === 'I') {
                const separator = document.createElement('option');
                separator.disabled = true;
                separator.textContent = 'Ingresos';
                separator.style.fontWeight = 'bold';
                separator.style.textAlign = 'left';
                separator.style.backgroundColor = '#f8f9fa';
                separator.style.color = '#666';
                selector.appendChild(separator);
            }
            
            // Agregar separador "Egresos" antes del primer egreso
            if (index > 0 && 
                unConfiguredAccounts[index-1].accountType === 'I' && 
                account.accountType === 'E') {
                
                const separator = document.createElement('option');
                separator.disabled = true;
                separator.textContent = 'Egresos';
                separator.style.fontWeight = 'bold';
                separator.style.textAlign = 'left';
                separator.style.backgroundColor = '#f8f9fa';
                separator.style.color = '#666';
                selector.appendChild(separator);
            }
            
            // Agregar opción normal sin identificadores
            const option = document.createElement('option');
            option.value = account.rootAccount;
            option.textContent = account.rootNameAccount;
            selector.appendChild(option);
        });
        
        // Agregar evento onChange
        selector.addEventListener('change', function() {
            const selectedAccount = this.value;
            // Llamar a la función global para actualizar el gráfico
            updateExplorerChart(selectedAccount, window.currentRawData);
        });
        
        leftSection.appendChild(selector);
        
        // Crear checkboxes de paginación
        createPaginationCheckboxes(leftSection);
        
        return selector;
    }
    
    // Función para crear checkboxes de paginación
    function createPaginationCheckboxes(leftSection) {
        const checkboxLabels = ['1ros', '2dos', '3ros', '4tos'];
        
        checkboxLabels.forEach((label, index) => {
            // Crear contenedor para checkbox y label
            const checkboxContainer = document.createElement('div');
            checkboxContainer.style.display = 'inline-flex';
            checkboxContainer.style.alignItems = 'center';
            checkboxContainer.style.marginRight = '12px';
            checkboxContainer.style.fontSize = '13px';
            
            // Crear checkbox
            const checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.id = `pagination_${label}`;
            checkbox.className = 'pagination-checkbox';
            checkbox.checked = true; // Marcados por defecto
            checkbox.disabled = true; // Deshabilitados por defecto
            checkbox.style.marginRight = '4px';
            checkbox.style.cursor = 'pointer';
            
            // Crear label
            const labelElement = document.createElement('label');
            labelElement.htmlFor = checkbox.id;
            labelElement.textContent = label;
            labelElement.style.cursor = 'pointer';
            labelElement.style.userSelect = 'none';
            labelElement.style.color = '#666';
            
            // Agregar evento change
            checkbox.addEventListener('change', function() {
                updateChartWithPagination();
            });
            
            checkboxContainer.appendChild(checkbox);
            checkboxContainer.appendChild(labelElement);
            leftSection.appendChild(checkboxContainer);
        });
    }
    
    // Función para actualizar el gráfico explorador según la cuenta seleccionada
    function updateExplorerChart(selectedRootAccount, rawData) {
        if (!rawData || !Array.isArray(rawData)) {
            console.warn('⚠️ No hay datos para actualizar grafico explorador');
            return;
        }
        
        // Filtrar datos por la cuenta seleccionada
        const accountData = rawData.filter(item => item.rootAccount === selectedRootAccount);
        
        if (accountData.length === 0) {
            console.warn(`⚠️ No hay datos para la cuenta ${selectedRootAccount}`);
            return;
        }
        
        // Procesar datos de la cuenta seleccionada
        const processedItems = [];
        let totalAmount = 0;
        const firstItem = accountData[0];
        const isIngreso = firstItem.accountType === 'I';
        const accountName = firstItem.rootNameAccount || 'Sin Nombre';
        
        accountData.forEach(item => {
            // Calcular monto neto según tipo de cuenta
            const montoNeto = isIngreso ? 
                (item.credit || 0) - (item.debit || 0) :  // Para ingresos: crédito - débito
                (item.debit || 0) - (item.credit || 0);   // Para egresos: débito - crédito
            
            if (Math.abs(montoNeto) >= UNIFIED_CONFIG.minAmount) {
                processedItems.push({
                    name: item.nameAccount || 'Sin Nombre',
                    peso: montoNeto,
                    originalData: item
                });
                totalAmount += montoNeto;
            }
        });
        
        // Ordenar por valor absoluto (mantener signos)
        processedItems.sort((a, b) => Math.abs(b.peso) - Math.abs(a.peso));
        
        // Guardar datos completos para paginación
        currentExplorerData = {
            allItems: processedItems,
            accountName: accountName,
            totalAmount: totalAmount,
            isIngreso: isIngreso
        };
        
        // Controlar checkboxes según cantidad de items
        controlPaginationCheckboxes(processedItems.length);
        
        // Aplicar paginación si está activa
        const filteredItems = applyPagination(processedItems);
        
        // Recalcular total para items filtrados
        const filteredTotal = filteredItems.reduce((sum, item) => sum + (item.peso || 0), 0);
        
        // Crear datos del gráfico con colores dinámicos
        const chartData = filteredItems.map(item => {
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
        
        // Actualizar información en el header central
        const centerSection = document.getElementById('explorerCenter');
        if (centerSection) {
            const montoFormateado = 'Bs ' + Highcharts.numberFormat(filteredTotal, 0, '.', ',');
            centerSection.innerHTML = `Total: ${montoFormateado} | ${filteredItems.length} conceptos`;
        }
        
        // Configuración del gráfico explorador (barras horizontales, sin título)
        const tipoLabel = isIngreso ? 'Ingresos' : 'Egresos';
        
        const config = {
            chart: {
                type: 'bar', // Cambio a barras horizontales
                height: 450,
                backgroundColor: 'transparent'
            },
            title: {
                text: null // Sin título en el gráfico
            },
            xAxis: {
                categories: filteredItems.map(item => item.name),
                title: { text: `Conceptos de ${tipoLabel}` }, // Sin acentos
                labels: { 
                    style: { fontSize: '11px' }
                    // Sin rotación para barras horizontales
                }
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
                bar: { // Barras horizontales
                    dataLabels: {
                        enabled: true, // Habilitado para barras horizontales
                        formatter: function() {
                            const signo = this.y >= 0 ? '' : '-';
                            const valor = Math.abs(this.y);
                            return signo + 'Bs ' + Highcharts.numberFormat(valor, 0, '.', ',');
                        },
                        style: { fontSize: '11px', fontWeight: 'bold' }
                    },
                    colorByPoint: true // Permitir colores diferentes por punto
                }
            },
            series: [{
                name: accountName,
                data: chartData
            }],
            tooltip: {
                formatter: function() {
                    const tipoDetalle = this.y >= 0 ? 
                        (isIngreso ? 'Ingreso' : 'Gasto') : 
                        (isIngreso ? 'Descuento/Devolucion' : 'Credito/Ajuste'); // Sin acentos
                    const signo = this.y >= 0 ? '' : '-';
                    const valor = Math.abs(this.y);
                    return '<b>' + this.point.name + '</b><br/>' +
                           tipoDetalle + ': ' + signo + 'Bs ' + Highcharts.numberFormat(valor, 0, '.', ',');
                }
            },
            legend: { enabled: false },
            credits: { enabled: false }
        };
        
        // Crear o actualizar el gráfico
        try {
            // Destruir gráfico anterior si existe
            if (explorerChart && explorerChart.destroy) {
                explorerChart.destroy();
            }
            
            // Crear nuevo gráfico
            explorerChart = Highcharts.chart('explorerChart', config);
            
        } catch (error) {
            console.error(`❌ Error creando/actualizando grafico explorador:`, error);
        }
    }
    
    // Función para controlar la habilitación de checkboxes según cantidad de items
    function controlPaginationCheckboxes(itemCount) {
        const checkboxes = document.querySelectorAll('.pagination-checkbox');
        const shouldEnable = itemCount > 30;
        
        checkboxes.forEach(checkbox => {
            checkbox.disabled = !shouldEnable;
            checkbox.checked = shouldEnable; // Marcar todos si se habilita
            
            // Cambiar estilo visual
            const label = checkbox.nextSibling;
            if (label) {
                label.style.color = shouldEnable ? '#495057' : '#ccc';
                label.style.cursor = shouldEnable ? 'pointer' : 'default';
            }
        });
    }
    
    // Función para aplicar paginación según checkboxes seleccionados
    function applyPagination(allItems) {
        const checkboxes = document.querySelectorAll('.pagination-checkbox');
        const isAnyEnabled = Array.from(checkboxes).some(cb => !cb.disabled);
        
        // Si no hay paginación activa, retornar todos los items
        if (!isAnyEnabled) {
            return allItems;
        }
        
        // Dividir en cuatro grupos (ya están ordenados por valor absoluto)
        const cuartoSize = Math.ceil(allItems.length / 4);
        const grupos = {
            '1ros': allItems.slice(0, cuartoSize),
            '2dos': allItems.slice(cuartoSize, cuartoSize * 2),
            '3ros': allItems.slice(cuartoSize * 2, cuartoSize * 3),
            '4tos': allItems.slice(cuartoSize * 3)
        };
        
        // Obtener items según checkboxes seleccionados
        let selectedItems = [];
        checkboxes.forEach(checkbox => {
            if (checkbox.checked && !checkbox.disabled) {
                const grupo = checkbox.id.replace('pagination_', '');
                selectedItems = selectedItems.concat(grupos[grupo] || []);
            }
        });
        
        return selectedItems;
    }
    
    // Función para actualizar gráfico con paginación (llamada por checkboxes)
    function updateChartWithPagination() {
        if (!currentExplorerData) return;
        
        const filteredItems = applyPagination(currentExplorerData.allItems);
        const filteredTotal = filteredItems.reduce((sum, item) => sum + (item.peso || 0), 0);
        
        // Actualizar información en el header central
        const centerSection = document.getElementById('explorerCenter');
        if (centerSection) {
            const montoFormateado = 'Bs ' + Highcharts.numberFormat(filteredTotal, 0, '.', ',');
            centerSection.innerHTML = `Total: ${montoFormateado} | ${filteredItems.length} conceptos`;
        }
        
        // Recrear gráfico con items filtrados
        createExplorerChartWithData(filteredItems, currentExplorerData.accountName, currentExplorerData.isIngreso);
    }
    
    // Función auxiliar para crear gráfico con datos específicos
    function createExplorerChartWithData(items, accountName, isIngreso) {
        const tipoLabel = isIngreso ? 'Ingresos' : 'Egresos';
        
        // Crear datos del gráfico con colores dinámicos
        const chartData = items.map(item => {
            const valor = item.peso || 0;
            const esPositivo = valor >= 0;
            
            // Colores según signo del valor
            let color;
            if (isIngreso) {
                color = esPositivo ? 'rgba(40, 167, 69, 0.9)' : 'rgba(220, 53, 69, 0.9)';
            } else {
                color = esPositivo ? 'rgba(220, 53, 69, 0.9)' : 'rgba(40, 167, 69, 0.9)';
            }
            
            return {
                name: item.name,
                y: valor,
                color: color
            };
        });
        
        const config = {
            chart: {
                type: 'bar',
                height: 450,
                backgroundColor: 'transparent'
            },
            title: {
                text: null
            },
            xAxis: {
                categories: items.map(item => item.name),
                title: { text: `Conceptos de ${tipoLabel}` },
                labels: { 
                    style: { fontSize: '11px' }
                }
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
                        style: { fontSize: '11px', fontWeight: 'bold' }
                    },
                    colorByPoint: true
                }
            },
            series: [{
                name: accountName,
                data: chartData
            }],
            tooltip: {
                formatter: function() {
                    const tipoDetalle = this.y >= 0 ? 
                        (isIngreso ? 'Ingreso' : 'Gasto') : 
                        (isIngreso ? 'Descuento/Devolucion' : 'Credito/Ajuste');
                    const signo = this.y >= 0 ? '' : '-';
                    const valor = Math.abs(this.y);
                    return '<b>' + this.point.name + '</b><br/>' +
                           tipoDetalle + ': ' + signo + 'Bs ' + Highcharts.numberFormat(valor, 0, '.', ',');
                }
            },
            legend: { enabled: false },
            credits: { enabled: false }
        };
        
        // Crear o actualizar el gráfico
        try {
            if (explorerChart && explorerChart.destroy) {
                explorerChart.destroy();
            }
            explorerChart = Highcharts.chart('explorerChart', config);
        } catch (error) {
            console.error('❌ Error actualizando grafico con paginacion:', error);
        }
    }
    
    // Función para limpiar el gráfico explorador
    function clearExplorerChart() {
        if (explorerChart && explorerChart.destroy) {
            explorerChart.destroy();
            explorerChart = null;
        }
        
        // Resetear datos del explorador
        currentExplorerData = null;
        
        // Limpiar contenedor del explorador
        const explorerSection = document.getElementById('explorerSection');
        if (explorerSection) {
            explorerSection.remove();
        }
        
        // Limpiar separador del explorador
        const explorerSeparator = document.querySelector('.explorer-separator');
        if (explorerSeparator) {
            explorerSeparator.remove();
        }
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
        
        // Limpiar explorer chart
        clearExplorerChart();
        
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
        
        // Formatear números con separadores de miles (preservando signos)
        document.getElementById('totalIncome').textContent = 'Bs ' + Highcharts.numberFormat(totalIncome, 0, '.', ',');
        document.getElementById('totalExpenses').textContent = 'Bs ' + Highcharts.numberFormat(totalExpenses, 0, '.', ',');
        document.getElementById('totalBalance').textContent = 'Bs ' + Highcharts.numberFormat(totalBalance, 0, '.', ',');
        document.getElementById('totalAccounts').textContent = totalAccounts;
    }
    
    // Función para validar integridad de datos
    function validateDataIntegrity(processedData, summaryIncome, summaryExpenses) {
        // Calcular totales usando allGroups (datos completos) respetando signos
        let allDataIncome = 0;
        let allDataExpenses = 0;
        
        processedData.allGroups.forEach(group => {
            if (group.accountType === 'I') {
                allDataIncome += group.totalAmount; // Incluye negativos
            } else if (group.accountType === 'E') {
                allDataExpenses += group.totalAmount; // Incluye negativos
            }
        });
        
        // Verificar que los totales coincidan (considerando signos)
        const incomeDifference = Math.abs(summaryIncome - allDataIncome);
        const expensesDifference = Math.abs(summaryExpenses - allDataExpenses);
        const tolerance = 0.01; // Tolerancia para diferencias de redondeo
        
        // Solo alertar en caso de errores (mantener console.error)
        if (incomeDifference > tolerance) {
            console.error(`❌ ALERTA: Discrepancia en INGRESOS de Bs ${incomeDifference.toFixed(2)}`);
        }
        
        if (expensesDifference > tolerance) {
            console.error(`❌ ALERTA: Discrepancia en EGRESOS de Bs ${expensesDifference.toFixed(2)}`);
        }
        
        // Log informativo sobre configuración aplicada
        const totalEnabledCharts = processedData.detailedGroups.length;
        const totalAllGroups = processedData.allGroups.length;
        
        if (totalEnabledCharts < totalAllGroups) {
            console.info(`ℹ️ CONFIGURACIÓN: Mostrando ${totalEnabledCharts} de ${totalAllGroups} gráficos detallados disponibles`);
        }
    }
    
    // Cargar datos de la consulta unificada
    async function loadData(startDate, endDate) {
        try {
            
            // Cargar SOLO datos de detailed_report (consulta unificada)
            const rawData = await fetchFinanceData('detailed_report', startDate, endDate);
            
            // Verificar si hay datos disponibles
            if (!rawData || rawData.length === 0) {
                console.warn('⚠️ No hay datos disponibles para el período seleccionado');
                showNoDataMessage();
                return;
            }
            
            // Procesar datos con la nueva arquitectura unificada
            const processedData = processUnifiedData(rawData);
            
            // Verificar si después del procesamiento hay datos válidos
            if (processedData.allGroups.length === 0) {
                console.warn('⚠️ No hay datos válidos después del procesamiento');
                showNoDataMessage();
                return;
            }
            
            // Limpiar gráficos existentes
            clearAllCharts();
            
            // Crear 2 gráficos summary (ingresos + egresos agregados por rootNameAccount)
            createSummaryCharts(processedData.summaryData);
            
            // Crear N gráficos detallados (uno por cada rootNameAccount)
            createDetailedCharts(processedData.detailedGroups);
            
            // Guardar datos para el explorador (necesario para el selector)
            window.currentRawData = rawData;
            
            // Crear gráfico explorador dinámico (cuentas NO configuradas)
            createDynamicExplorerChart(rawData);
            
            // Actualizar estadísticas basadas en los datos procesados
            updateUnifiedStats(processedData);
            
            console.info('✅ Dashboard cargado con datos dinamicos de la consulta SQL');
            
        } catch (error) {
            console.error('❌ Error cargando arquitectura unificada:', error);
            showErrorMessage('Error al cargar datos: ' + error.message);
            throw error;
        }
    }
    
    // Función para mostrar mensaje cuando no hay datos
    function showNoDataMessage() {
        // Limpiar gráficos existentes
        clearAllCharts();
        
        // Actualizar cards con valores en cero
        document.getElementById('totalIncome').textContent = 'Bs 0';
        document.getElementById('totalExpenses').textContent = 'Bs 0';
        document.getElementById('totalBalance').textContent = 'Bs 0';
        document.getElementById('totalAccounts').textContent = '0';
        
        // Mostrar mensaje informativo en contenedor principal
        const mainContainer = getMainChartsContainer();
        mainContainer.innerHTML = `
            <div style="text-align: center; padding: 40px; color: #666;">
                <h3>📊 No hay datos disponibles</h3>
                <p>No se encontraron registros para el período seleccionado.</p>
                <p><small>Sistema dinámico - Los datos provienen directamente de la consulta SQL</small></p>
            </div>
        `;
    }
    
    // Función para mostrar mensaje de error
    function showErrorMessage(message) {
        // Limpiar gráficos existentes
        clearAllCharts();
        
        // Mostrar mensaje de error en contenedor principal
        const mainContainer = getMainChartsContainer();
        mainContainer.innerHTML = `
            <div style="text-align: center; padding: 40px; color: #dc3545;">
                <h3>❌ Error al cargar dashboard</h3>
                <p>${message}</p>
                <p><small>Verificar conexión con base de datos y servlet</small></p>
            </div>
        `;
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
            console.warn('❌ API no disponible - Sistema totalmente dinamico requiere consulta SQL:', error.message);
        }
        
        // Sistema 100% dinamico: Sin datos mock predefinidos
        // Si la API falla, retornar array vacio para mantener coherencia
        console.info('ℹ️ No hay datos disponibles - Verificar conexion con base de datos');
        return [];
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
        processAllDataForSummary,
        processDataWithChartConfig,
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
        getMainChartsContainer,
        // Funciones de configuración
        isChartEnabled,
        getChartConfig,
        toggleChart,
        getEnabledCharts,
        getChartsByType,
        // Funciones de explorador dinámico
        getUnConfiguredAccounts,
        createDynamicExplorerChart,
        updateExplorerChart,
        clearExplorerChart,
        // Funciones de paginación
        controlPaginationCheckboxes,
        applyPagination,
        updateChartWithPagination,
        createExplorerChartWithData,
        // Funciones de manejo de errores
        showNoDataMessage,
        showErrorMessage,
        // Acceso a configuración
        CHART_CONFIG: CHART_CONFIG
    };
})();