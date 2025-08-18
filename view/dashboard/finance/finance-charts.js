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
        "5340000000": { 
            enabled: true, 
            accountType: "E", 
            name: "GASTOS DE COMERCIALIZACION",
            description: "Gastos relacionados con ventas y marketing"
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
        
        // Fallback: datos mock con códigos reales de 10 dígitos
        return [
            // INGRESOS - VENTAS
            {"accountType":"I", "rootAccount":"4110000000", "rootNameAccount":"VENTAS", "account":"4110000001", "nameAccount":"VENTA DE MOLIENDA Y GRANULADO ULEXITA", "debit":0, "credit":7501126.06},
            {"accountType":"I", "rootAccount":"4110000000", "rootNameAccount":"VENTAS", "account":"4110000002", "nameAccount":"VENTA DE BENTONITA Y BARITINA", "debit":0, "credit":848367.48},
            
            // INGRESOS - INGRESOS EXTRAORDINARIOS
            {"accountType":"I", "rootAccount":"4210000000", "rootNameAccount":"INGRESOS EXTRAORDINARIOS", "account":"4210000001", "nameAccount":"INGRESOS POR SERVICIOS DE LABORATORIO", "debit":0, "credit":3422.00},
            {"accountType":"I", "rootAccount":"4210000000", "rootNameAccount":"INGRESOS EXTRAORDINARIOS", "account":"4210000002", "nameAccount":"OTROS INGRESOS", "debit":0, "credit":6770.82},
            {"accountType":"I", "rootAccount":"4210000000", "rootNameAccount":"INGRESOS EXTRAORDINARIOS", "account":"4210000003", "nameAccount":"INGRESO POR DEVOLUCIÓN DE REGALÍAS MINERAS", "debit":0, "credit":167454.82},
            {"accountType":"I", "rootAccount":"4210000000", "rootNameAccount":"INGRESOS EXTRAORDINARIOS", "account":"4210000004", "nameAccount":"DONACIONES PERSONALES", "debit":0, "credit":5000.00},
            {"accountType":"I", "rootAccount":"4210000000", "rootNameAccount":"INGRESOS EXTRAORDINARIOS", "account":"4210000005", "nameAccount":"INGRESOS POR SERVICIOS PRESTADOS TRANSPORTE", "debit":0, "credit":62640.00},
            {"accountType":"I", "rootAccount":"4210000000", "rootNameAccount":"INGRESOS EXTRAORDINARIOS", "account":"4210000006", "nameAccount":"INGRESOS POR SERVICIOS PRESTADOS TRANSPORTE", "debit":0, "credit":290933.00},
            {"accountType":"I", "rootAccount":"4210000000", "rootNameAccount":"INGRESOS EXTRAORDINARIOS", "account":"4210000007", "nameAccount":"INGRESO POR SERVICIO DE PESAJE EN BALANZA", "debit":0, "credit":4830.00},
            
            // EGRESOS - FLETES Y TRANSPORTES
            {"accountType":"E", "rootAccount":"5210000000", "rootNameAccount":"FLETES Y TRANSPORTES", "account":"5210000001", "nameAccount":"FLETES Y TRANSPORTES DE MATERIA PRIMA", "debit":380590.88, "credit":0},
            {"accountType":"E", "rootAccount":"5210000000", "rootNameAccount":"FLETES Y TRANSPORTES", "account":"5210000002", "nameAccount":"FLETES Y TRANSPORTES DE PRODUCTOS TERMINADOS", "debit":95597.06, "credit":0},
            {"accountType":"E", "rootAccount":"5210000000", "rootNameAccount":"FLETES Y TRANSPORTES", "account":"5210000003", "nameAccount":"FLETES Y TRANSPORTES EN GENERAL", "debit":5795.00, "credit":0},
            {"accountType":"E", "rootAccount":"5210000000", "rootNameAccount":"FLETES Y TRANSPORTES", "account":"5210000004", "nameAccount":"DESCUENTOS SOBRE VENTAS", "debit":138800.03, "credit":0},
            {"accountType":"E", "rootAccount":"5210000000", "rootNameAccount":"FLETES Y TRANSPORTES", "account":"5210000005", "nameAccount":"GASTOS DE ESTADIA EN FRONTERA", "debit":50605.38, "credit":0},
            {"accountType":"E", "rootAccount":"5210000000", "rootNameAccount":"FLETES Y TRANSPORTES", "account":"5210000006", "nameAccount":"DESCUENTO SOBRE SERVICIOS", "debit":610.00, "credit":0},
            
            // EGRESOS - MATERIAL DIRECTO
            {"accountType":"E", "rootAccount":"5310000000", "rootNameAccount":"MATERIAL DIRECTO", "account":"5310000001", "nameAccount":"BARITINA", "debit":500.00, "credit":0},
            {"accountType":"E", "rootAccount":"5310000000", "rootNameAccount":"MATERIAL DIRECTO", "account":"5310000002", "nameAccount":"ULEXITA", "debit":11188.12, "credit":0},
            
            // EGRESOS - MANO DE OBRA
            {"accountType":"E", "rootAccount":"5320000000", "rootNameAccount":"MANO DE OBRA", "account":"5320000001", "nameAccount":"SUELDOS Y SALARIOS", "debit":445161.35, "credit":0},
            {"accountType":"E", "rootAccount":"5320000000", "rootNameAccount":"MANO DE OBRA", "account":"5320000002", "nameAccount":"AGUINALDOS PRODUCCION", "debit":47641.78, "credit":0},
            {"accountType":"E", "rootAccount":"5320000000", "rootNameAccount":"MANO DE OBRA", "account":"5320000003", "nameAccount":"INDEMNIZACIONES PRODUCCION", "debit":41852.98, "credit":0},
            {"accountType":"E", "rootAccount":"5320000000", "rootNameAccount":"MANO DE OBRA", "account":"5320000004", "nameAccount":"BONOS AL PERSONAL DE PRODUCCION", "debit":9000.00, "credit":0},
            {"accountType":"E", "rootAccount":"5320000000", "rootNameAccount":"MANO DE OBRA", "account":"5320000005", "nameAccount":"PERSONAL EVENTUAL", "debit":13610.42, "credit":0},
            {"accountType":"E", "rootAccount":"5320000000", "rootNameAccount":"MANO DE OBRA", "account":"5320000006", "nameAccount":"SERVICIOS PRESTADOS POR TERCEROS", "debit":2000.00, "credit":0}
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
        // Acceso a configuración
        CHART_CONFIG: CHART_CONFIG
    };
})();