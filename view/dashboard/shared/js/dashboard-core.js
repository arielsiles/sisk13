// Dashboard Core - Funciones base y configuración global
window.DashboardCore = (function() {
    'use strict';
    
    // Configuración
    const API_BASE = '/khipus/dashboard-api';
    let currentDashboard = 'production';
    
    // Función para corregir codificación UTF-8 malformada universalmente
    function fixEncoding(text) {
        if (typeof text !== 'string') return text;
        
        // Método 1: Decodificación automática UTF-8
        try {
            const decoded = decodeURIComponent(escape(text));
            if (decoded !== text && !decoded.includes('�')) {
                return decoded;
            }
        } catch (e) {
            // Si falla, continuar con método manual
        }
        
        // Método 2: Corrección manual paso a paso
        let result = text;
        
        // Mapeo directo de palabras problemáticas conocidas
        const wordMap = {
            'CategorÃ­as': 'Categorías',
            'ProducciÃ³n': 'Producción',
            'DistribuciÃ³n': 'Distribución', 
            'GestiÃ³n': 'Gestión',
            'AdministraciÃ³n': 'Administración',
            'LogÃ­stica': 'Logística',
            'NutriciÃ³n': 'Nutrición',
            'InformaciÃ³n': 'Información'
        };
        
        // Aplicar correcciones de palabras
        for (const [wrong, correct] of Object.entries(wordMap)) {
            result = result.replace(new RegExp(wrong, 'g'), correct);
        }
        
        // Corrección de caracteres individuales usando replace simple
        result = result
            .replace(/Ã­/g, 'í')
            .replace(/Ã³/g, 'ó') 
            .replace(/Ã¡/g, 'á')
            .replace(/Ã©/g, 'é')
            .replace(/Ãº/g, 'ú')
            .replace(/Ã±/g, 'ñ')
            .replace(/Ã¼/g, 'ü');
            
        return result;
    }
    
    // Función para limpiar objeto de datos
    function fixDataEncoding(data) {
        if (Array.isArray(data)) {
            return data.map(item => fixDataEncoding(item));
        } else if (typeof data === 'object' && data !== null) {
            const fixedData = {};
            for (const [key, value] of Object.entries(data)) {
                fixedData[key] = typeof value === 'string' ? fixEncoding(value) : fixDataEncoding(value);
            }
            return fixedData;
        }
        return data;
    }
    
    // Variables para manejo de períodos
    let isManualDateChange = false;
    
    // Inicializar fechas
    function initDates() {
        const today = new Date();
        const startOfYear = new Date(today.getFullYear(), 0, 1);
        
        const endDateElement = document.getElementById('endDate');
        const startDateElement = document.getElementById('startDate');
        
        if (endDateElement) {
            endDateElement.value = today.toISOString().split('T')[0];
        }
        if (startDateElement) {
            startDateElement.value = startOfYear.toISOString().split('T')[0];
        }
        
        // Establecer período inicial como "Año actual"
        const periodSelector = document.getElementById('periodSelector');
        if (periodSelector) {
            periodSelector.value = 'current_year';
        }
        
        // Agregar listeners para detectar cambios manuales
        setupDateChangeListeners();
    }
    
    // Configurar listeners para detectar cambios manuales en fechas
    function setupDateChangeListeners() {
        const startDateElement = document.getElementById('startDate');
        const endDateElement = document.getElementById('endDate');
        
        if (startDateElement) {
            startDateElement.addEventListener('input', function() {
                if (!isManualDateChange) {
                    resetToCustomPeriod();
                }
            });
        }
        
        if (endDateElement) {
            endDateElement.addEventListener('input', function() {
                if (!isManualDateChange) {
                    resetToCustomPeriod();
                }
            });
        }
    }
    
    // Calcular fechas según el período seleccionado
    function calculatePeriodDates(period) {
        const today = new Date();
        let startDate, endDate;
        
        switch (period) {
            case 'current_month':
                startDate = new Date(today.getFullYear(), today.getMonth(), 1);
                endDate = new Date(today);
                break;
                
            case 'last_quarter':
                const currentQuarter = Math.floor(today.getMonth() / 3);
                const lastQuarter = currentQuarter === 0 ? 3 : currentQuarter - 1;
                const quarterYear = currentQuarter === 0 ? today.getFullYear() - 1 : today.getFullYear();
                
                startDate = new Date(quarterYear, lastQuarter * 3, 1);
                endDate = new Date(quarterYear, lastQuarter * 3 + 3, 0); // Último día del trimestre
                break;
                
            case 'current_year':
                startDate = new Date(today.getFullYear(), 0, 1);
                endDate = new Date(today);
                break;
                
            case 'last_30_days':
                startDate = new Date(today);
                startDate.setDate(today.getDate() - 30);
                endDate = new Date(today);
                break;
                
            case 'last_6_months':
                startDate = new Date(today);
                startDate.setMonth(today.getMonth() - 6);
                endDate = new Date(today);
                break;
                
            case 'first_semester':
                startDate = new Date(today.getFullYear(), 0, 1); // 1 enero
                const firstSemesterEnd = new Date(today.getFullYear(), 5, 30); // 30 junio
                // Si hoy es menor al último día del semestre, usar hoy; si no, usar fin del semestre
                endDate = today < firstSemesterEnd ? new Date(today) : firstSemesterEnd;
                break;
                
            case 'second_semester':
                startDate = new Date(today.getFullYear(), 6, 1); // 1 julio
                const secondSemesterEnd = new Date(today.getFullYear(), 11, 31); // 31 diciembre
                // Si hoy es menor al último día del semestre, usar hoy; si no, usar fin del semestre
                endDate = today < secondSemesterEnd ? new Date(today) : secondSemesterEnd;
                break;
                
            case 'custom':
            default:
                return null; // No calcular fechas para personalizado
        }
        
        return {
            startDate: startDate.toISOString().split('T')[0],
            endDate: endDate.toISOString().split('T')[0]
        };
    }
    
    // Aplicar período seleccionado
    function applyDatePeriod(period) {
        const periodData = calculatePeriodDates(period);
        
        if (periodData) {
            // Período predefinido - actualizar fechas y cargar datos automáticamente
            isManualDateChange = true; // Evitar trigger de resetToCustomPeriod
            updateDateFields(periodData.startDate, periodData.endDate, true);
            isManualDateChange = false;
            
            // Auto-ejecutar actualización de datos para períodos predefinidos
            loadData();
        } else {
            // Período personalizado - solo habilitar campos, usuario decide cuándo actualizar
            updateDateFields(null, null, false);
        }
    }
    
    // Actualizar campos de fecha y su estado
    function updateDateFields(startDate, endDate, disabled) {
        const startDateElement = document.getElementById('startDate');
        const endDateElement = document.getElementById('endDate');
        
        if (startDateElement && endDateElement) {
            if (startDate && endDate) {
                startDateElement.value = startDate;
                endDateElement.value = endDate;
            }
            
            startDateElement.disabled = disabled;
            endDateElement.disabled = disabled;
        }
    }
    
    
    // Resetear a período personalizado (cuando usuario edita fechas manualmente)
    function resetToCustomPeriod() {
        const periodSelector = document.getElementById('periodSelector');
        if (periodSelector && periodSelector.value !== 'custom') {
            periodSelector.value = 'custom';
            updateDateFields(null, null, false); // Habilitar campos
        }
    }
    
    // Mostrar status en el header
    function showStatus(message, type = 'loading') {
        const headerContainer = document.getElementById('headerContainer');
        const headerText = document.getElementById('headerText');
        
        if (!headerContainer || !headerText) {
            console.log(`Status (${type}): ${message}`);
            return;
        }
        
        // Cambiar texto y aplicar clase de estado
        headerText.textContent = message;
        headerContainer.className = `header ${type}`;
    }
    
    function hideStatus() {
        const headerContainer = document.getElementById('headerContainer');
        const headerText = document.getElementById('headerText');
        
        if (!headerContainer || !headerText) {
            return;
        }
        
        // Restaurar título original y remover clases de estado
        headerText.textContent = 'Sistema de Reportes Simplificado';
        headerContainer.className = 'header';
    }
    
    // Test de conexión
    async function testConnection() {
        showStatus('Probando conexion con el servidor...', 'loading');
        
        try {
            const response = await fetch(`${API_BASE}?type=test`);
            const data = await response.json();
            
            if (data.status === 'ok') {
                showStatus('Conexión exitosa con el servidor!', 'success');
                setTimeout(hideStatus, 3000);
            } else {
                showStatus('Error en la respuesta del servidor', 'error');
            }
        } catch (error) {
            console.error('Error de conexión:', error);
            showStatus(`Error de conexion: ${error.message}`, 'error');
        }
    }
    
    // Fetch datos de la API
    async function fetchData(type, startDate, endDate) {
        const url = `${API_BASE}?type=${type}&startDate=${startDate}&endDate=${endDate}`;
        
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
        return fixDataEncoding(data);
    }
    
    // Cargar datos
    async function loadData() {
        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;
        
        if (!startDate || !endDate) {
            alert('Por favor selecciona ambas fechas');
            return;
        }
        
        // Verificar que las fechas son válidas
        const start = new Date(startDate);
        const end = new Date(endDate);
        
        if (start > end) {
            alert('La fecha de inicio no puede ser posterior a la fecha de fin');
            return;
        }
        
        const filterBtn = document.getElementById('filterBtn');
        filterBtn.disabled = true;
        filterBtn.textContent = 'Cargando...';
        
        showStatus(`Cargando datos del ${startDate} al ${endDate}...`, 'loading');
        
        try {
            // Cargar datos según el dashboard actual
            const dashboardModule = window[`${currentDashboard.charAt(0).toUpperCase() + currentDashboard.slice(1)}Dashboard`];
            if (dashboardModule && dashboardModule.loadData) {
                await dashboardModule.loadData(startDate, endDate);
            }
            
            showStatus('Datos actualizados correctamente!', 'success');
            setTimeout(hideStatus, 3000);
            
        } catch (error) {
            console.error('Error cargando datos:', error);
            showStatus(`Error: ${error.message}`, 'error');
        } finally {
            filterBtn.disabled = false;
            filterBtn.textContent = 'Actualizar Datos';
        }
    }
    
    // Cargar componente HTML
    async function loadComponent(componentPath, containerId) {
        try {
            const response = await fetch(componentPath);
            const html = await response.text();
            document.getElementById(containerId).innerHTML = html;
        } catch (error) {
            console.error(`Error cargando componente ${componentPath}:`, error);
        }
    }
    
    // Inicialización
    async function init() {
        try {
            // Cargar componentes compartidos
            await loadComponent('shared/components/header.html', 'header-container');
            await loadComponent('shared/components/controls.html', 'controls-container');
            
            // Esperar un momento para que se renderice el DOM
            await new Promise(resolve => setTimeout(resolve, 100));
            
            // Inicializar fechas
            initDates();
            
            console.log('Dashboard Core inicializado correctamente');
            
        } catch (error) {
            console.error('Error inicializando Dashboard Core:', error);
            throw error;
        }
    }
    
    // API pública
    return {
        init,
        currentDashboard: () => currentDashboard,
        setCurrentDashboard: (dashboard) => currentDashboard = dashboard,
        fetchData,
        loadData,
        testConnection,
        showStatus,
        hideStatus,
        fixEncoding,
        fixDataEncoding,
        loadComponent,
        // Nuevas funciones para períodos
        applyDatePeriod,
        updateDateFields,
        resetToCustomPeriod
    };
})();