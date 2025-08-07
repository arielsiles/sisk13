package com.encens.khipus.servlet;

/**
 * Servlet para dashboard de Finanzas
 * URL: /finance-dashboard-api (configurado en web.xml)
 */
public class FinanceDashboardServlet extends BaseDashboardServlet {
    
    @Override
    protected String handleDataRequest(String dataType, String startDate, String endDate) throws Exception {
        switch (dataType) {
            case "income":
                return getIncomeData(startDate, endDate);
            case "expenses":
                return getExpensesData(startDate, endDate);
            case "cash_flow":
                return getCashFlowData(startDate, endDate);
            case "test":
                return getTestResponse();
            default:
                return "{\"error\":\"Unknown type: " + dataType + "\"}";
        }
    }
    
    /**
     * Datos de ingresos (mock por ahora - se puede implementar después)
     */
    private String getIncomeData(String startDate, String endDate) {
        // Mock data para ingresos - se puede reemplazar con consulta real después
        return "[{\"name\":\"Ventas Directas\",\"peso\":25000},{\"name\":\"Servicios\",\"peso\":18000},{\"name\":\"Comisiones\",\"peso\":12000}]";
    }
    
    /**
     * Datos de gastos (mock por ahora - se puede implementar después)
     */
    private String getExpensesData(String startDate, String endDate) {
        // Mock data para gastos - se puede reemplazar con consulta real después
        return "[{\"name\":\"Gastos Operativos\",\"peso\":15000},{\"name\":\"Sueldos\",\"peso\":22000},{\"name\":\"Materiales\",\"peso\":8000}]";
    }
    
    /**
     * Datos de flujo de caja (mock por ahora - se puede implementar después)
     */
    private String getCashFlowData(String startDate, String endDate) {
        // Mock data para flujo de caja - se puede reemplazar con consulta real después
        return "[{\"name\":\"Enero\",\"peso\":5000},{\"name\":\"Febrero\",\"peso\":7500},{\"name\":\"Marzo\",\"peso\":-2000},{\"name\":\"Abril\",\"peso\":8200}]";
    }
}