// Production Dashboard - Gráficos y lógica específica de producción
window.ProductionDashboard = (function() {
    'use strict';

    let dailyProductionChart;
    let productionVsRawMaterialChart;
    let charts = [];

    /**
     * Crea etiquetas jerárquicas para el eje X: días en primera línea, meses agrupados en segunda línea
     * @param {string[]} dates - Array de fechas en formato "YYYY-MM-DD"
     * @returns {string[]} Array de etiquetas de texto con salto de línea en primer día del mes
     */
    function createHierarchicalDateLabels(dates) {
        const monthNames = ['', 'Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun',
                           'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];

        return dates.map((date, index) => {
            const parts = date.split('-');
            const day = parts[2];
            const month = parseInt(parts[1]);
            const monthYear = date.substring(0, 7); // "2024-05"

            // Detectar si es el primer día del mes en el dataset
            const isMonthStart = index === 0 ||
                                dates[index - 1].substring(0, 7) !== monthYear;

            if (isMonthStart) {
                // Mostrar día + mes usando <br> - Highcharts lo convierte a SVG <tspan>
                return day + '<br>' + monthNames[month];
            }
            // Solo mostrar día
            return day;
        });
    }

    /**
     * Procesa datos de producción diaria para el gráfico de líneas
     * Transforma array plano en estructura de series por producto
     */
    function processDailyProductionData(rawData) {
        if (!rawData || !Array.isArray(rawData) || rawData.length === 0) {
            console.log('No hay datos de produccion diaria disponibles');
            return { dates: [], series: [] };
        }

        // Agrupar datos por producto
        const productMap = {};
        const datesSet = new Set();

        rawData.forEach(item => {
            const fecha = item.fecha;
            const producto = item.producto || 'Sin Producto';
            const cantidad = item.cantidad_producto_tn || 0;

            datesSet.add(fecha);

            if (!productMap[producto]) {
                productMap[producto] = {};
            }
            productMap[producto][fecha] = cantidad;
        });

        // Convertir a arrays ordenados
        const dates = Array.from(datesSet).sort();
        const series = Object.keys(productMap).sort().map(producto => {
            return {
                name: DashboardCore.fixEncoding(producto),
                data: dates.map(fecha => productMap[producto][fecha] || 0)
            };
        });

        console.log(`Datos procesados: ${dates.length} fechas, ${series.length} productos`);
        return { dates, series };
    }

    /**
     * Procesa datos para el gráfico de Producción vs Materia Prima
     * Agrupa por fecha sumando todos los productos para obtener totales diarios
     */
    function processProductionVsRawMaterialData(rawData) {
        if (!rawData || !Array.isArray(rawData) || rawData.length === 0) {
            console.log('No hay datos disponibles para Producción vs Materia Prima');
            return { dates: [], series: [] };
        }

        // Agrupar por fecha sumando todos los productos
        const dateMap = {};

        rawData.forEach(item => {
            const fecha = item.fecha;
            const cantidadProducto = item.cantidad_producto_tn || 0;
            const cantidadMateriaPrima = item.cantidad_materia_prima_tn || 0;

            if (!dateMap[fecha]) {
                dateMap[fecha] = {
                    produccion: 0,
                    materiaPrima: 0
                };
            }
            dateMap[fecha].produccion += cantidadProducto;
            dateMap[fecha].materiaPrima += cantidadMateriaPrima;
        });

        // Convertir a arrays ordenados
        const dates = Object.keys(dateMap).sort();
        const series = [
            {
                name: 'Produccion Total',
                data: dates.map(fecha => dateMap[fecha].produccion),
                color: '#28a745'
            },
            {
                name: 'Materia Prima Total',
                data: dates.map(fecha => dateMap[fecha].materiaPrima),
                color: '#555'
            }
        ];

        console.log(`Datos procesados Producción vs Materia Prima: ${dates.length} fechas`);
        return { dates, series };
    }

    /**
     * Crea el gráfico de líneas de Producción Diaria
     */
    function createDailyProductionChart(data) {
        // Destruir chart existente para evitar distorsión al actualizar
        if (dailyProductionChart) {
            dailyProductionChart.destroy();
            dailyProductionChart = null;
        }

        // Limpiar completamente el contenedor para evitar residuos de DOM
        const container = document.getElementById('dailyProductionChart');
        if (container) {
            container.innerHTML = '';
        }

        const processed = processDailyProductionData(data);

        if (processed.dates.length === 0) {
            console.log('No hay datos para mostrar en el gráfico');
            // Mostrar mensaje en el gráfico
            const container = document.getElementById('dailyProductionChart');
            if (container) {
                container.innerHTML = '<div style="text-align: center; padding: 40px; color: #999;">' +
                    'No hay datos de produccion para el periodo seleccionado</div>';
            }
            return null;
        }

        const config = {
            chart: {
                type: 'line',
                backgroundColor: 'transparent',
                zoomType: 'x',
                marginBottom: 60,
                marginRight: 180,
                marginTop: 50,
                spacingBottom: 20
            },
            title: {
                text: DashboardCore.fixEncoding('Producción Diaria'),
                style: {
                    color: '#333',
                    fontSize: '20px',
                    fontWeight: 'bold'
                }
            },
            xAxis: {
                categories: createHierarchicalDateLabels(processed.dates),
                visible: true,
                title: {
                    text: ''
                },
                labels: {
                    enabled: true,
                    useHTML: false,
                    rotation: 0,
                    align: 'center',
                    style: {
                        fontSize: '10px',
                        whiteSpace: 'normal'
                    },
                    y: 20,
                    padding: 5,
                    overflow: 'allow'
                },
                tickLength: 5,
                tickmarkPlacement: 'on',
                crosshair: true
            },
            yAxis: {
                title: {
                    text: 'Cantidad (Tn)',
                    style: { fontSize: '14px' }
                },
                labels: {
                    formatter: function() {
                        return Highcharts.numberFormat(this.value, 1, '.', ',') + ' Tn';
                    }
                },
                min: 0
            },
            plotOptions: {
                line: {
                    dataLabels: {
                        enabled: false
                    },
                    marker: {
                        enabled: true,
                        radius: 4,
                        symbol: 'circle'
                    },
                    lineWidth: 2
                }
            },
            series: processed.series,
            tooltip: {
                shared: true,
                crosshairs: true,
                backgroundColor: 'rgba(255, 255, 255, 0.95)',
                borderRadius: 8,
                formatter: function() {
                    // Obtener la fecha real del array processed.dates usando el índice del punto
                    const dateIndex = this.points[0].point.index;
                    const dateStr = processed.dates[dateIndex]; // "2025-05-17"
                    const parts = dateStr.split('-');
                    const formattedDate = parts[2] + '/' + parts[1] + '/' + parts[0]; // "17/05/2025"

                    let tooltip = '<b>' + formattedDate + '</b><br/>';
                    let total = 0;
                    this.points.forEach(point => {
                        tooltip += '<span style="color:' + point.color + '">\u25CF</span> ' +
                                   point.series.name + ': <b>' +
                                   Highcharts.numberFormat(point.y, 2, '.', ',') + ' Tn</b><br/>';
                        total += point.y;
                    });
                    if (this.points.length > 1) {
                        tooltip += '<br/><b>Total: ' + Highcharts.numberFormat(total, 2, '.', ',') + ' Tn</b>';
                    }
                    return tooltip;
                }
            },
            legend: {
                enabled: true,
                layout: 'vertical',
                align: 'right',
                verticalAlign: 'middle',
                itemStyle: {
                    fontSize: '11px',
                    color: '#333',
                    cursor: 'pointer'
                },
                itemHoverStyle: {
                    color: '#000'
                },
                itemHiddenStyle: {
                    color: '#ccc'
                },
                symbolRadius: 5,
                symbolHeight: 10,
                symbolWidth: 10,
                itemDistance: 8
            },
            credits: { enabled: false },
            colors: ['#28a745', '#007bff', '#ffc107', '#dc3545', '#17a2b8', '#6f42c1', '#fd7e14', '#20c997']
        };

        dailyProductionChart = Highcharts.chart('dailyProductionChart', config);
        charts[0] = dailyProductionChart;

        return dailyProductionChart;
    }

    /**
     * Crea el gráfico de líneas de Producción vs Materia Prima
     */
    function createProductionVsRawMaterialChart(data) {
        // Destruir chart existente para evitar distorsión al actualizar
        if (productionVsRawMaterialChart) {
            productionVsRawMaterialChart.destroy();
            productionVsRawMaterialChart = null;
        }

        // Limpiar completamente el contenedor para evitar residuos de DOM
        const container = document.getElementById('productionVsRawMaterialChart');
        if (container) {
            container.innerHTML = '';
        }

        const processed = processProductionVsRawMaterialData(data);

        if (processed.dates.length === 0) {
            console.log('No hay datos para mostrar en el gráfico Producción vs Materia Prima');
            if (container) {
                container.innerHTML = '<div style="text-align: center; padding: 40px; color: #999;">' +
                    'No hay datos de produccion vs materia prima para el periodo seleccionado</div>';
            }
            return null;
        }

        const config = {
            chart: {
                type: 'line',
                backgroundColor: 'transparent',
                zoomType: 'x',
                marginBottom: 60,
                marginRight: 180,
                marginTop: 50,
                spacingBottom: 20
            },
            title: {
                text: DashboardCore.fixEncoding('Producción Total vs Materia Prima'),
                style: {
                    color: '#333',
                    fontSize: '20px',
                    fontWeight: 'bold'
                }
            },
            xAxis: {
                categories: createHierarchicalDateLabels(processed.dates),
                visible: true,
                title: {
                    text: ''
                },
                labels: {
                    enabled: true,
                    useHTML: false,
                    rotation: 0,
                    align: 'center',
                    style: {
                        fontSize: '10px',
                        whiteSpace: 'normal'
                    },
                    y: 20,
                    padding: 5,
                    overflow: 'allow'
                },
                tickLength: 5,
                tickmarkPlacement: 'on',
                crosshair: true
            },
            yAxis: {
                title: {
                    text: 'Cantidad (Tn)',
                    style: { fontSize: '14px' }
                },
                labels: {
                    formatter: function() {
                        return Highcharts.numberFormat(this.value, 1, '.', ',') + ' Tn';
                    }
                },
                min: 0
            },
            plotOptions: {
                line: {
                    dataLabels: {
                        enabled: false
                    },
                    marker: {
                        enabled: true,
                        radius: 4,
                        symbol: 'circle'
                    },
                    lineWidth: 2
                }
            },
            series: processed.series,
            tooltip: {
                shared: true,
                crosshairs: true,
                backgroundColor: 'rgba(255, 255, 255, 0.95)',
                borderRadius: 8,
                formatter: function() {
                    // Obtener la fecha real del array processed.dates usando el índice del punto
                    const dateIndex = this.points[0].point.index;
                    const dateStr = processed.dates[dateIndex]; // "2025-05-17"
                    const parts = dateStr.split('-');
                    const formattedDate = parts[2] + '/' + parts[1] + '/' + parts[0]; // "17/05/2025"

                    let tooltip = '<b>' + formattedDate + '</b><br/>';
                    this.points.forEach(point => {
                        tooltip += '<span style="color:' + point.color + '">\u25CF</span> ' +
                                   point.series.name + ': <b>' +
                                   Highcharts.numberFormat(point.y, 2, '.', ',') + ' Tn</b><br/>';
                    });
                    return tooltip;
                }
            },
            legend: {
                enabled: true,
                layout: 'vertical',
                align: 'right',
                verticalAlign: 'middle',
                itemStyle: {
                    fontSize: '11px',
                    color: '#333',
                    cursor: 'pointer'
                },
                itemHoverStyle: {
                    color: '#000'
                },
                itemHiddenStyle: {
                    color: '#ccc'
                },
                symbolRadius: 5,
                symbolHeight: 10,
                symbolWidth: 10,
                itemDistance: 8
            },
            credits: { enabled: false }
        };

        productionVsRawMaterialChart = Highcharts.chart('productionVsRawMaterialChart', config);
        charts[1] = productionVsRawMaterialChart;

        return productionVsRawMaterialChart;
    }

    /**
     * Actualiza las estadísticas en las cards superiores
     */
    function updateProductionStats(data) {
        if (!data || !Array.isArray(data) || data.length === 0) {
            document.getElementById('totalProducts').textContent = '0';
            document.getElementById('totalProductQuantity').textContent = '0.00';
            document.getElementById('totalRawMaterial').textContent = '0.00';
            document.getElementById('totalCost').textContent = '0.00';
            return;
        }

        // Calcular totales
        const uniqueProducts = new Set(data.map(item => item.producto));
        const totalProducts = uniqueProducts.size;

        const totalProductQuantity = data.reduce((sum, item) =>
            sum + (item.cantidad_producto_tn || 0), 0);

        const totalRawMaterial = data.reduce((sum, item) =>
            sum + (item.cantidad_materia_prima_tn || 0), 0);

        const totalCost = data.reduce((sum, item) =>
            sum + (item.costo_total || 0), 0);

        // Actualizar DOM
        document.getElementById('totalProducts').textContent = totalProducts;
        document.getElementById('totalProductQuantity').textContent =
            Highcharts.numberFormat(totalProductQuantity, 2, '.', ',');
        document.getElementById('totalRawMaterial').textContent =
            Highcharts.numberFormat(totalRawMaterial, 2, '.', ',');
        document.getElementById('totalCost').textContent =
            Highcharts.numberFormat(totalCost, 2, '.', ',');

        console.log('Estadísticas actualizadas:', {
            totalProducts,
            totalProductQuantity,
            totalRawMaterial,
            totalCost
        });
    }

    /**
     * Carga datos específicos de producción
     */
    async function loadData(startDate, endDate) {
        try {
            console.log('Cargando datos del dashboard de producción...');

            // Fetch datos de producción diaria
            const dailyData = await fetchProductionData('daily_production', startDate, endDate);

            // Actualizar estadísticas (cards)
            updateProductionStats(dailyData);

            // Crear gráfico de líneas de Producción Diaria
            createDailyProductionChart(dailyData);

            // Crear gráfico de Producción vs Materia Prima
            createProductionVsRawMaterialChart(dailyData);

            console.log('Dashboard de producción cargado exitosamente');

        } catch (error) {
            console.error('Error cargando datos de producción:', error);
            // Mostrar mensaje de error en cards
            document.getElementById('totalProducts').textContent = 'Error';
            document.getElementById('totalProductQuantity').textContent = 'Error';
            document.getElementById('totalRawMaterial').textContent = 'Error';
            document.getElementById('totalCost').textContent = 'Error';
            throw error;
        }
    }

    /**
     * Función para obtener datos de producción del backend
     */
    async function fetchProductionData(type, startDate, endDate) {
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
            console.log('API de producción no disponible:', error.message);
        }

        // Fallback a la API original (si existe)
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

        return DashboardCore.fixDataEncoding(data);
    }

    /**
     * Inicializa el dashboard de producción
     */
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
        fetchProductionData,
        processDailyProductionData,
        createDailyProductionChart,
        processProductionVsRawMaterialData,
        createProductionVsRawMaterialChart,
        updateProductionStats
    };
})();