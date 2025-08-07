// Chart Utils - Utilidades para Highcharts
window.ChartUtils = (function() {
    'use strict';
    
    // Configuración base para gráficos
    const baseConfig = {
        chart: {
            backgroundColor: 'transparent',
            animation: false
        },
        responsive: {
            rules: [{
                condition: {
                    maxWidth: 500
                },
                chartOptions: {
                    legend: {
                        enabled: false
                    }
                }
            }]
        },
        credits: {
            enabled: false
        },
        exporting: {
            enabled: true
        }
    };
    
    // Colores predefinidos
    const colors = {
        primary: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40', '#C9CBCF', '#FF6B6B'],
        production: ['#54A0FF', '#5F27CD', '#00D2D3', '#FF9FF3', '#54A0FF'],
        inventory: ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7'],
        finance: ['#6C5CE7', '#A29BFE', '#FD79A8', '#FDCB6E', '#E17055']
    };
    
    // Crear configuración para gráfico de barras horizontales
    function createBarChartConfig(containerId, title, data, options = {}) {
        const sortedData = data.sort((a, b) => b.peso - a.peso).slice(0, 10);
        
        return {
            ...baseConfig,
            chart: {
                ...baseConfig.chart,
                type: 'bar'
            },
            title: {
                text: title,
                style: {
                    fontSize: '16px',
                    fontWeight: 'bold'
                }
            },
            xAxis: {
                categories: sortedData.map(item => item.name),
                title: {
                    text: options.xAxisTitle || 'Elementos'
                }
            },
            yAxis: {
                min: 0,
                title: {
                    text: options.yAxisTitle || 'Valores'
                }
            },
            legend: {
                enabled: false
            },
            plotOptions: {
                bar: {
                    dataLabels: {
                        enabled: true,
                        format: options.dataLabelFormat || '{y:.2f}'
                    },
                    color: options.color || 'rgba(54, 162, 235, 0.8)'
                }
            },
            series: [{
                name: options.seriesName || 'Datos',
                data: sortedData.map(item => item.peso || 0)
            }]
        };
    }
    
    // Crear configuración para gráfico de dona (pie con inner size)
    function createDonutChartConfig(containerId, title, data, options = {}) {
        const chartColors = options.colors || colors.primary;
        
        const chartData = data.map((item, index) => ({
            name: item.name,
            y: item.peso || 0,
            color: chartColors[index % chartColors.length]
        }));
        
        return {
            ...baseConfig,
            chart: {
                ...baseConfig.chart,
                type: 'pie'
            },
            title: {
                text: title,
                style: {
                    fontSize: '16px',
                    fontWeight: 'bold'
                }
            },
            plotOptions: {
                pie: {
                    innerSize: options.innerSize || '50%',
                    allowPointSelect: true,
                    cursor: 'pointer',
                    dataLabels: {
                        enabled: true,
                        format: '<b>{point.name}</b>: {point.percentage:.1f}%'
                    },
                    showInLegend: true
                }
            },
            tooltip: {
                pointFormat: options.tooltipFormat || '<b>{point.name}</b>: {point.percentage:.1f}% ({point.y:.2f})'
            },
            legend: {
                align: 'center',
                verticalAlign: 'bottom',
                layout: 'horizontal'
            },
            series: [{
                name: options.seriesName || 'Datos',
                data: chartData
            }]
        };
    }
    
    // Crear configuración para gráfico de pie simple
    function createPieChartConfig(containerId, title, data, options = {}) {
        const chartColors = options.colors || colors.primary;
        
        const chartData = data.map((item, index) => ({
            name: item.name,
            y: item.peso || 0,
            color: chartColors[index % chartColors.length]
        }));
        
        return {
            ...baseConfig,
            chart: {
                ...baseConfig.chart,
                type: 'pie'
            },
            title: {
                text: title,
                style: {
                    fontSize: '16px',
                    fontWeight: 'bold'
                }
            },
            plotOptions: {
                pie: {
                    allowPointSelect: true,
                    cursor: 'pointer',
                    dataLabels: {
                        enabled: true,
                        format: '<b>{point.name}</b>: {point.percentage:.1f}%'
                    },
                    showInLegend: true
                }
            },
            tooltip: {
                pointFormat: options.tooltipFormat || '<b>{point.name}</b>: {point.percentage:.1f}% ({point.y:.2f})'
            },
            legend: {
                align: 'center',
                verticalAlign: 'bottom',
                layout: 'horizontal'
            },
            series: [{
                name: options.seriesName || 'Datos',
                data: chartData
            }]
        };
    }
    
    // Función para redimensionar gráficos
    function resizeChart(chart) {
        if (chart) {
            chart.setSize(null, null, false);
            chart.reflow();
        }
    }
    
    // Función para redimensionar múltiples gráficos
    function resizeCharts(chartArray) {
        chartArray.forEach(chart => {
            if (chart) {
                resizeChart(chart);
            }
        });
    }
    
    // Configurar listeners para redimensionamiento
    function setupResizeListeners(chartArray) {
        // Listener para redimensionamiento de ventana
        window.addEventListener('resize', function() {
            clearTimeout(window.resizeTimeout);
            window.resizeTimeout = setTimeout(function() {
                resizeCharts(chartArray);
            }, 250);
        });
        
        // Observer para detectar cambios en el contenedor de gráficos
        if (window.ResizeObserver) {
            const chartsContainer = document.querySelector('.charts-container');
            if (chartsContainer) {
                const resizeObserver = new ResizeObserver(function(entries) {
                    clearTimeout(window.observerTimeout);
                    window.observerTimeout = setTimeout(function() {
                        resizeCharts(chartArray);
                    }, 100);
                });
                
                resizeObserver.observe(chartsContainer);
            }
        }
    }
    
    // API pública
    return {
        colors,
        createBarChartConfig,
        createDonutChartConfig,
        createPieChartConfig,
        resizeChart,
        resizeCharts,
        setupResizeListeners
    };
})();