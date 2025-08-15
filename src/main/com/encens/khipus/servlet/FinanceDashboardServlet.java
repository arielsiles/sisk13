package com.encens.khipus.servlet;

/**
 * Servlet para dashboard de Finanzas
 * URL: /finance-dashboard-api (configurado en web.xml)
 * 
 * Implementa consultas reales basadas en el reporte "Estado de Ganancias y Pérdidas"
 * usando las tablas sf_tmpdet (VoucherDetail) y sf_tmpenc (Voucher)
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
     * Datos de INGRESOS basados en cuentas con accountType = 'I'
     * Consulta basada en ProfitAndLoss2ReportAction.java líneas 175-188
     * INCLUYE valores negativos como "DESCUENTO EN COMPRAS"
     */
    private String getIncomeData(String startDate, String endDate) {
        String sql = 
            "SELECT " +
                "ca3.descri as name, " +
                "(SUM(vd.haber) - SUM(vd.debe)) as peso " +
            "FROM sf_tmpdet vd " +
            "LEFT JOIN sf_tmpenc v ON vd.id_tmpenc = v.id_tmpenc " +
            "LEFT JOIN arcgms ca ON vd.cuenta = ca.cuenta " +
            "LEFT JOIN arcgms ca3 ON ca.cta_niv3 = ca3.cuenta " +
            "WHERE ca.tipo = 'I' " +
                "AND v.estado <> 'ANL' " +
                "AND v.fecha BETWEEN ? AND ? " +
            "GROUP BY ca3.cuenta, ca3.descri " +
            "HAVING (SUM(vd.haber) - SUM(vd.debe)) <> 0 " +  // Cambio: incluir negativos también
            "ORDER BY ABS(peso) DESC " +  // Ordenar por valor absoluto para mostrar primero los más significativos
            "LIMIT 15";
        
        String fallback = "[{\"name\":\"Ventas Directas\",\"peso\":25000},{\"name\":\"Servicios\",\"peso\":18000},{\"name\":\"Descuento en Compras\",\"peso\":-60589}]";
        return executeQueryToJson(sql, startDate, endDate, fallback);
    }
    
    /**
     * Datos de GASTOS basados en cuentas con accountType = 'E'
     * Consulta basada en ProfitAndLoss2ReportAction.java líneas 130-142
     */
    private String getExpensesData(String startDate, String endDate) {
        String sql = 
            "SELECT " +
                "ca3.descri as name, " +
                "(SUM(vd.debe) - SUM(vd.haber)) as peso " +
            "FROM sf_tmpdet vd " +
            "LEFT JOIN sf_tmpenc v ON vd.id_tmpenc = v.id_tmpenc " +
            "LEFT JOIN arcgms ca ON vd.cuenta = ca.cuenta " +
            "LEFT JOIN arcgms ca3 ON ca.cta_niv3 = ca3.cuenta " +
            "WHERE ca.tipo = 'E' " +
                "AND v.estado <> 'ANL' " +
                "AND v.fecha BETWEEN ? AND ? " +
            "GROUP BY ca3.cuenta, ca3.descri " +
            "HAVING (SUM(vd.debe) - SUM(vd.haber)) > 0 " +
            "ORDER BY peso DESC " +
            "LIMIT 15";
        
        String fallback = "[{\"name\":\"Gastos Operativos\",\"peso\":15000},{\"name\":\"Sueldos\",\"peso\":22000},{\"name\":\"Materiales\",\"peso\":8000}]";
        return executeQueryToJson(sql, startDate, endDate, fallback);
    }
    
    /**
     * Datos de flujo de caja por período mensual
     * Calcula el balance neto (ingresos - gastos) por mes
     */
    private String getCashFlowData(String startDate, String endDate) {
        String sql = 
            "SELECT " +
                "DATE_FORMAT(v.fecha, '%Y-%m') as name, " +
                "SUM(CASE WHEN ca.tipo = 'I' THEN (vd.haber - vd.debe) " +
                    "WHEN ca.tipo = 'E' THEN -(vd.debe - vd.haber) " +
                    "ELSE 0 END) as peso " +
            "FROM sf_tmpdet vd " +
            "LEFT JOIN sf_tmpenc v ON vd.id_tmpenc = v.id_tmpenc " +
            "LEFT JOIN arcgms ca ON vd.cuenta = ca.cuenta " +
            "WHERE ca.tipo IN ('I', 'E') " +
                "AND v.estado <> 'ANL' " +
                "AND v.fecha BETWEEN ? AND ? " +
            "GROUP BY DATE_FORMAT(v.fecha, '%Y-%m') " +
            "ORDER BY name DESC " +
            "LIMIT 12";
        
        String fallback = "[{\"name\":\"2025-08\",\"peso\":5000},{\"name\":\"2025-07\",\"peso\":7500},{\"name\":\"2025-06\",\"peso\":-2000},{\"name\":\"2025-05\",\"peso\":8200}]";
        return executeQueryToJson(sql, startDate, endDate, fallback);
    }
}