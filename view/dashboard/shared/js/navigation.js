// Dashboard Navigation - Navegación entre dashboards
window.DashboardNavigation = (function() {
    'use strict';
    
    const dashboards = {
        production: {
            name: 'Producción',
            path: 'production/production-dashboard.html',
            script: 'production/production-charts.js'
        },
        inventory: {
            name: 'Inventarios', 
            path: 'inventory/inventory-dashboard.html',
            script: 'inventory/inventory-charts.js'
        },
        finance: {
            name: 'Finanzas',
            path: 'finance/finance-dashboard.html',
            script: 'finance/finance-charts.js'
        }
    };
    
    let loadedScripts = new Set();
    
    // Cargar script dinámicamente
    function loadScript(src) {
        return new Promise((resolve, reject) => {
            if (loadedScripts.has(src)) {
                resolve();
                return;
            }
            
            const script = document.createElement('script');
            script.src = src;
            script.onload = () => {
                loadedScripts.add(src);
                resolve();
            };
            script.onerror = () => reject(new Error(`Failed to load script: ${src}`));
            document.head.appendChild(script);
        });
    }
    
    // Cambiar dashboard
    async function switchDashboard(dashboardType) {
        if (!dashboards[dashboardType]) {
            console.error(`Dashboard desconocido: ${dashboardType}`);
            return;
        }
        
        try {
            // Actualizar tabs (solo si existen)
            const tabButtons = document.querySelectorAll('.tab-btn');
            if (tabButtons.length > 0) {
                tabButtons.forEach(btn => {
                    btn.classList.remove('active');
                });
                
                const activeTab = document.querySelector(`[data-dashboard="${dashboardType}"]`);
                if (activeTab) {
                    activeTab.classList.add('active');
                }
            }
            
            // Cargar HTML del dashboard
            await DashboardCore.loadComponent(
                dashboards[dashboardType].path, 
                'dashboard-container'
            );
            
            // Cargar script específico del dashboard
            await loadScript(dashboards[dashboardType].script);
            
            // Actualizar dashboard actual
            DashboardCore.setCurrentDashboard(dashboardType);
            
            // Inicializar el dashboard específico si tiene método init
            const dashboardModule = window[`${dashboardType.charAt(0).toUpperCase() + dashboardType.slice(1)}Dashboard`];
            if (dashboardModule && dashboardModule.init) {
                dashboardModule.init();
            }
            
            // Cargar datos del nuevo dashboard (solo si tenemos los controles cargados)
            const startDate = document.getElementById('startDate');
            const endDate = document.getElementById('endDate');
            if (startDate && endDate && startDate.value && endDate.value) {
                DashboardCore.loadData();
            }
            
        } catch (error) {
            console.error(`Error cargando dashboard ${dashboardType}:`, error);
            // Solo mostrar status si el elemento existe
            if (typeof DashboardCore.showStatus === 'function') {
                try {
                    DashboardCore.showStatus(`Error cargando dashboard: ${error.message}`, 'error');
                } catch (statusError) {
                    console.log('No se pudo mostrar el status, elementos no cargados aún');
                }
            }
        }
    }
    
    // Cargar dashboard específico (usado para carga inicial)
    async function loadDashboard(dashboardType) {
        await switchDashboard(dashboardType);
    }
    
    // Inicializar navegación
    function init() {
        // Los eventos onclick ya están definidos en el HTML de controls
        console.log('Dashboard Navigation inicializado');
    }
    
    // API pública
    return {
        init,
        switchDashboard,
        loadDashboard
    };
})();