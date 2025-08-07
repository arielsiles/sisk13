# Dashboard Modular - KHIPUS

## Estructura del Proyecto

```
dashboard/
├── index.html                          # Punto de entrada principal
├── shared/                            # Componentes compartidos
│   ├── styles/
│   │   ├── common.css                 # Estilos base y layout
│   │   └── charts.css                 # Estilos específicos para gráficos
│   ├── js/
│   │   ├── dashboard-core.js          # Funciones core (API, encoding, etc.)
│   │   ├── chart-utils.js             # Utilidades para Highcharts
│   │   └── navigation.js              # Navegación entre dashboards
│   └── components/
│       ├── header.html                # Header reutilizable
│       └── controls.html              # Controles de fecha y navegación
├── production/
│   ├── production-dashboard.html      # Vista del dashboard de producción
│   └── production-charts.js           # Lógica específica de producción
├── inventory/
│   ├── inventory-dashboard.html       # Vista del dashboard de inventarios
│   └── inventory-charts.js            # Lógica específica de inventarios
└── finance/
    ├── finance-dashboard.html         # Vista del dashboard de finanzas
    └── finance-charts.js              # Lógica específica de finanzas
```

## Características

### Arquitectura Modular
- **Separación por responsabilidades**: Cada dashboard tiene su propio módulo
- **Componentes reutilizables**: Header, controles y utilidades compartidas
- **Carga dinámica**: Los dashboards se cargan bajo demanda
- **Escalabilidad**: Fácil agregar nuevos dashboards y gráficos

### Dashboards Disponibles

#### 1. Producción
- Gráfico de barras: Acopio de proveedores por peso
- Gráfico de dona: Distribución de materias primas
- Gráfico de pie: Distribución por zonas
- Estadísticas: Total productores, peso, materiales, zonas

#### 2. Inventarios
- Gráfico de barras: Stock por productos
- Gráfico de dona: Inventario por categorías
- Gráfico de pie: Distribución por almacenes
- Estadísticas: Total items, valor, categorías, almacenes

#### 3. Finanzas
- Gráfico de barras: Ingresos por concepto
- Gráfico de dona: Distribución de gastos
- Gráfico de pie: Flujo de caja por período
- Estadísticas: Ingresos, gastos, balance, cuentas activas

## API JavaScript

### DashboardCore
- `init()`: Inicializa el sistema
- `loadData()`: Carga datos del dashboard actual
- `testConnection()`: Prueba la conexión con la API
- `fetchData(type, startDate, endDate)`: Obtiene datos de la API
- `fixEncoding(text)`: Corrige problemas de codificación UTF-8

### DashboardNavigation
- `init()`: Inicializa la navegación
- `switchDashboard(type)`: Cambia entre dashboards
- `loadDashboard(type)`: Carga un dashboard específico

### ChartUtils
- `createBarChartConfig()`: Configuración para gráficos de barras
- `createDonutChartConfig()`: Configuración para gráficos de dona
- `createPieChartConfig()`: Configuración para gráficos de pie
- `setupResizeListeners()`: Configurar redimensionamiento automático

## Uso

1. **Acceso**: Abrir `/view/dashboard/index.html`
2. **Navegación**: Usar los tabs para cambiar entre dashboards
3. **Filtros**: Seleccionar fechas y hacer clic en "Actualizar Datos"
4. **Conexión**: Usar "Test Conexión" para verificar la API

## Compatibilidad

- **API**: Compatible con la API existente `/khipus/dashboard-api`
- **Datos**: Reutiliza los tipos de datos actuales (producers, materials, zones)
- **Funcionalidad**: Mantiene todas las características del dashboard original
- **Responsive**: Adaptable a dispositivos móviles y diferentes tamaños de pantalla

## Beneficios

- ✅ **Mantenibilidad**: Código organizado por módulos
- ✅ **Escalabilidad**: Fácil agregar nuevos dashboards
- ✅ **Performance**: Carga bajo demanda
- ✅ **Reutilización**: Componentes compartidos
- ✅ **Testing**: Cada módulo es independiente
- ✅ **Profesional**: Estructura enterprise-ready