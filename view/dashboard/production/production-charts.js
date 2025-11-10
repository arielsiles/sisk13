// Production Dashboard - Gráficos y lógica específica de producción
window.ProductionDashboard = (function() {
    'use strict';

    let charts = [];

    // Cargar datos específicos de producción
    async function loadData(startDate, endDate) {
        try {
            console.log('Dashboard de Producción: Sin gráficos configurados aún');
            console.log('Los gráficos de materia prima se han movido al dashboard "Materia Prima"');

            // TODO: Implementar nuevos gráficos de producción aquí
            // Ejemplos:
            // - Métricas de eficiencia de producción
            // - Tiempos de producción por producto
            // - Análisis de rendimiento
            // - Comparativas de producción por período

        } catch (error) {
            console.error('Error cargando datos de producción:', error);
            throw error;
        }
    }

    // Función específica para obtener datos de producción (mantener para futuros gráficos)
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
            console.log('API de producción no disponible');
        }

        // Fallback a la API original
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

    // Inicializar dashboard de producción
    function init() {
        console.log('Production Dashboard inicializado (modo placeholder)');
        console.log('ℹ️ Los gráficos de materia prima están ahora en el dashboard "Materia Prima"');
    }

    // API pública
    return {
        init,
        loadData,
        fetchProductionData
    };
})();