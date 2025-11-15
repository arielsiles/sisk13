// Materia Prima Dashboard - Gráficos y lógica específica de materia prima
window.MateriaPrimaDashboard = (function() {
    'use strict';

    let dailyAcopioChart, producersChart, materialsChart, zonesChart;
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
     * Procesa datos de acopio diario para el gráfico de líneas
     * Transforma array plano en estructura de series por material
     */
    function processDailyAcopioData(rawData) {
        if (!rawData || !Array.isArray(rawData) || rawData.length === 0) {
            console.log('No hay datos de acopio diario disponibles');
            return { dates: [], series: [] };
        }

        // Agrupar datos por material
        const materialMap = {};
        const datesSet = new Set();

        rawData.forEach(item => {
            const fecha = item.fecha;
            const material = item.material || 'Material Desconocido';
            const peso = item.peso_diario_tn || 0;

            datesSet.add(fecha);

            if (!materialMap[material]) {
                materialMap[material] = {};
            }
            materialMap[material][fecha] = peso;
        });

        // Convertir a arrays ordenados
        const dates = Array.from(datesSet).sort();
        const series = Object.keys(materialMap).sort().map(material => {
            const dataArray = dates.map(fecha => materialMap[material][fecha] || 0);

            // Calcular total de la serie para mostrar en leyenda
            const total = dataArray.reduce((sum, val) => sum + val, 0);
            const totalFormatted = Highcharts.numberFormat(total, 2, '.', ',');

            return {
                name: DashboardCore.fixEncoding(material) + ' ' + totalFormatted + ' Tn',
                data: dataArray
            };
        });

        console.log(`Datos procesados: ${dates.length} fechas, ${series.length} materiales`);
        return { dates, series };
    }

    /**
     * Crea el gráfico de líneas de Acopio Diario
     */
    function createDailyAcopioChart(data) {
        // Destruir chart existente para evitar distorsión al actualizar
        if (dailyAcopioChart) {
            dailyAcopioChart.destroy();
            dailyAcopioChart = null;
        }

        // Limpiar completamente el contenedor para evitar residuos de DOM
        const container = document.getElementById('dailyAcopioChart');
        if (container) {
            container.innerHTML = '';
        }

        const processed = processDailyAcopioData(data);

        if (processed.dates.length === 0) {
            console.log('No hay datos para mostrar en el gráfico de acopio diario');
            if (container) {
                container.innerHTML = '<div style="text-align: center; padding: 40px; color: #999;">' +
                    'No hay datos de acopio para el periodo seleccionado</div>';
            }
            return null;
        }

        // Calcular total general de acopio (suma de todas las series)
        const totalGeneral = processed.series.reduce((sum, serie) => {
            const totalSerie = serie.data.reduce((s, val) => s + val, 0);
            return sum + totalSerie;
        }, 0);
        const totalGeneralFormatted = Highcharts.numberFormat(totalGeneral, 2, '.', ',');

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
                text: DashboardCore.fixEncoding('Acopio diario x Materia Prima'),
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
                    text: 'Peso (Tn)',
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
                itemDistance: 8,
                title: {
                    text: 'Acopio Total: ' + totalGeneralFormatted + ' Tn',
                    style: {
                        fontSize: '12px',
                        fontWeight: 'bold',
                        color: '#333'
                    }
                }
            },
            credits: { enabled: false },
            colors: ['#28a745', '#007bff', '#ffc107', '#dc3545', '#17a2b8', '#6f42c1', '#fd7e14', '#20c997']
        };

        dailyAcopioChart = Highcharts.chart('dailyAcopioChart', config);
        charts[0] = dailyAcopioChart;

        return dailyAcopioChart;
    }

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
        charts[1] = producersChart;
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
        charts[2] = materialsChart;
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
        charts[3] = zonesChart;
    }

    // Actualizar estadísticas de materia prima
    function updateMateriaPrimaStats(producersData, materialsData, zonesData) {
        const totalProducers = producersData.length;
        // FIX: Solo sumar materialsData, no producersData (evitar duplicación)
        // Ambos provienen de la misma tabla acopiomp, solo agrupados diferente
        const totalWeight = materialsData.reduce((sum, item) => sum + item.peso, 0);
        const totalMaterials = materialsData.length;
        const totalZones = zonesData.length;

        document.getElementById('totalProducers').textContent = totalProducers;
        document.getElementById('totalWeight').textContent = Math.round(totalWeight * 100) / 100;
        document.getElementById('totalMaterials').textContent = totalMaterials;
        document.getElementById('totalZones').textContent = totalZones;
    }

    // Cargar datos específicos de materia prima
    async function loadData(startDate, endDate) {
        try {
            const [dailyAcopioData, producersData, materialsData, zonesData] = await Promise.all([
                fetchMateriaPrimaData('daily_acopio', startDate, endDate),
                fetchMateriaPrimaData('producers', startDate, endDate),
                fetchMateriaPrimaData('materials', startDate, endDate),
                fetchMateriaPrimaData('zones', startDate, endDate)
            ]);

            // Crear gráfico de líneas de Acopio Diario (primero)
            createDailyAcopioChart(dailyAcopioData);

            // Actualizar gráficos existentes
            updateProducersChart(producersData);
            updateMaterialsChart(materialsData);
            updateZonesChart(zonesData);

            // Actualizar estadísticas
            updateMateriaPrimaStats(producersData, materialsData, zonesData);

            console.log('Dashboard de materia prima cargado exitosamente');

        } catch (error) {
            console.error('Error cargando datos de materia prima:', error);
            throw error;
        }
    }

    // Función específica para obtener datos de materia prima
    async function fetchMateriaPrimaData(type, startDate, endDate) {
        // Intentar nueva API primero, fallback a la original
        let url = `/khipus/materia-prima-dashboard-api?type=${type}&startDate=${startDate}&endDate=${endDate}`;

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
            console.log('Nueva API no disponible, usando API de producción como fallback...');
        }

        // Fallback a la API de producción (usa las mismas queries)
        url = `/khipus/production-dashboard-api?type=${type}&startDate=${startDate}&endDate=${endDate}`;

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
            console.log('API de producción no disponible, usando API original...');
        }

        // Fallback final a la API original
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

    // Inicializar dashboard de materia prima
    function init() {
        console.log('Materia Prima Dashboard inicializado');

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
        updateMateriaPrimaStats
    };
})();
