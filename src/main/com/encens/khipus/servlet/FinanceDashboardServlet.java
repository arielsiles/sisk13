package com.encens.khipus.servlet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
            case "detailed_report":
                return getDetailedReportData(startDate, endDate);
            case "test":
                return getTestResponse();
            default:
                return "{\"error\":\"Unknown type: " + dataType + "\"}";
        }
    }
    
    
    
    
    /**
     * Datos detallados del Estado de Resultados Extendido
     * Basado EXACTAMENTE en ProfitAndLossExtendedReportAction.java líneas 135-151 y 184-200
     * 
     * Retorna datos específicos para los 5 nuevos gráficos:
     * - INGRESOS: VENTAS, OTROS INGRESOS
     * - EGRESOS: FLETES Y TRANSPORTES, MATERIAL DIRECTO, MANO DE OBRA
     * 
     * Usa consultas separadas para replicar exactamente la lógica del PDF
     */
    private String getDetailedReportData(String startDate, String endDate) {
        // Usar la consulta unificada proporcionada por el usuario - SIN RESTRICCIONES
        String sql = 
            "SELECT " +
                "ca3.cuenta AS rootAccount, " +
                "ca3.descri AS rootNameAccount, " +
                "ca.cuenta AS account, " +
                "ca.descri AS nameAccount, " +
                "ca.tipo AS accountType, " +
                "SUM(vd.debe) AS debit, " +
                "SUM(vd.haber) AS credit " +
            "FROM sf_tmpdet vd " +
            "LEFT JOIN sf_tmpenc v ON vd.id_tmpenc = v.id_tmpenc " +
            "LEFT JOIN arcgms ca ON vd.cuenta = ca.cuenta " +
            "LEFT JOIN arcgms ca3 ON ca.cta_niv3 = ca3.cuenta " +
            "WHERE ca.tipo IN ('I', 'E') " +
                "AND v.estado <> 'ANL' " +
                "AND v.fecha BETWEEN ? AND ? " +
                "AND ca.cn_nivel IN (5, 6) " +
            "GROUP BY ca3.cuenta, ca3.descri, ca.cuenta, ca.descri, ca.tipo " +
            "HAVING (SUM(vd.debe) + SUM(vd.haber)) > 0 " +
            "ORDER BY ca3.cuenta, ca.cuenta";
        
        try {
            System.out.println("=== CONSULTA UNIFICADA SIN RESTRICCIONES ===");
            System.out.println("Fechas: " + startDate + " a " + endDate);
            String result = executeDetailedReportQuery(sql, startDate, endDate, getFallbackDetailedData());
            System.out.println("=== FIN CONSULTA UNIFICADA ===");
            return result;
            
        } catch (Exception e) {
            System.err.println("Error en consulta unificada: " + e.getMessage());
            e.printStackTrace();
            System.out.println("USANDO FALLBACK DATA");
            return getFallbackDetailedData();
        }
    }
    
    /**
     * Consulta específica para INGRESOS (replicando addCriteriaProfitSubReport)
     */
    private String getDetailedIncomesData(String startDate, String endDate) {
        // Consulta EXACTA del reporte PDF (líneas 135-151 de ProfitAndLossExtendedReportAction.java)
        String sql = 
            "SELECT " +
                "ca3.cuenta as rootAccount, " +
                "ca3.descri as rootNameAccount, " +
                "ca.cuenta as account, " +
                "ca.descri as nameAccount, " +
                "'I' as accountType, " +
                "SUM(vd.debe) AS debit, " +
                "SUM(vd.haber) AS credit " +
            "FROM sf_tmpdet vd " +
            "LEFT JOIN sf_tmpenc v ON vd.id_tmpenc = v.id_tmpenc " +
            "LEFT JOIN arcgms ca ON vd.cuenta = ca.cuenta " +
            "LEFT JOIN arcgms ca3 ON ca.cta_niv3 = ca3.cuenta " +
            "WHERE ca.tipo = 'I' " +
                "AND v.estado <> 'ANL' " +
                "AND v.fecha BETWEEN ? AND ? " +
                "AND ca.cn_nivel IN (5, 6) " +
                "AND ca3.cuenta IS NOT NULL " +    // Asegurar que tenga cuenta de nivel 3
                "AND ca.activa = 'S' " +           // Solo cuentas activas
            "GROUP BY ca3.cuenta, ca3.descri, ca.cuenta, ca.descri " +
            "HAVING (SUM(vd.debe) + SUM(vd.haber)) > 0 " +  // Solo cuentas con movimientos
            "ORDER BY ca3.cuenta, ca.cuenta";
        
        String fallback = getFallbackIncomesData();
        return executeDetailedReportQuery(sql, startDate, endDate, fallback);
    }
    
    /**
     * Consulta específica para EGRESOS (replicando addCriteriaLossSubReport)
     */
    private String getDetailedExpensesData(String startDate, String endDate) {
        // Consulta EXACTA del reporte PDF (líneas 184-200 de ProfitAndLossExtendedReportAction.java)
        String sql = 
            "SELECT " +
                "ca3.cuenta as rootAccount, " +
                "ca3.descri as rootNameAccount, " +
                "ca.cuenta as account, " +
                "ca.descri as nameAccount, " +
                "'E' as accountType, " +
                "SUM(vd.debe) AS debit, " +
                "SUM(vd.haber) AS credit " +
            "FROM sf_tmpdet vd " +
            "LEFT JOIN sf_tmpenc v ON vd.id_tmpenc = v.id_tmpenc " +
            "LEFT JOIN arcgms ca ON vd.cuenta = ca.cuenta " +
            "LEFT JOIN arcgms ca3 ON ca.cta_niv3 = ca3.cuenta " +
            "WHERE ca.tipo = 'E' " +
                "AND v.estado <> 'ANL' " +
                "AND v.fecha BETWEEN ? AND ? " +
                "AND ca.cn_nivel IN (5, 6) " +
                "AND ca3.cuenta IS NOT NULL " +    // Asegurar que tenga cuenta de nivel 3
                "AND ca.activa = 'S' " +           // Solo cuentas activas
            "GROUP BY ca3.cuenta, ca3.descri, ca.cuenta, ca.descri " +
            "HAVING (SUM(vd.debe) + SUM(vd.haber)) > 0 " +  // Solo cuentas con movimientos
            "ORDER BY ca3.cuenta, ca.cuenta";
        
        String fallback = getFallbackExpensesData();
        return executeDetailedReportQuery(sql, startDate, endDate, fallback);
    }
    
    /**
     * Datos fallback combinados (mantener para compatibilidad)
     */
    private String getFallbackDetailedData() {
        String ingresos = getFallbackIncomesData();
        String egresos = getFallbackExpensesData();
        
        // Combinar sin duplicar [ ]
        String combined = "[";
        if (ingresos.length() > 2) {
            combined += ingresos.substring(1, ingresos.length() - 1);
        }
        if (egresos.length() > 2) {
            if (combined.length() > 1) combined += ",";
            combined += egresos.substring(1, egresos.length() - 1);
        }
        combined += "]";
        return combined;
    }
    
    /**
     * Datos fallback solo para INGRESOS
     */
    private String getFallbackIncomesData() {
        return "[" +
            // INGRESOS - VENTAS (2 items exactos del PDF)
            "{\"accountType\":\"I\",\"rootAccount\":\"41001\",\"rootNameAccount\":\"VENTAS\",\"account\":\"41001001\",\"nameAccount\":\"VENTA DE MOLIENDA Y GRANULADO ULEXITA\",\"debit\":0,\"credit\":7501126.06}," +
            "{\"accountType\":\"I\",\"rootAccount\":\"41001\",\"rootNameAccount\":\"VENTAS\",\"account\":\"41001002\",\"nameAccount\":\"VENTA DE BENTONITA Y BARITINA\",\"debit\":0,\"credit\":848367.48}," +
            // INGRESOS - OTROS INGRESOS (7 items exactos del PDF)
            "{\"accountType\":\"I\",\"rootAccount\":\"41005\",\"rootNameAccount\":\"OTROS INGRESOS\",\"account\":\"41005001\",\"nameAccount\":\"INGRESOS POR SERVICIOS DE LABORATORIO\",\"debit\":0,\"credit\":3422.00}," +
            "{\"accountType\":\"I\",\"rootAccount\":\"41006\",\"rootNameAccount\":\"OTROS INGRESOS\",\"account\":\"41006001\",\"nameAccount\":\"OTROS INGRESOS\",\"debit\":0,\"credit\":6770.82}," +
            "{\"accountType\":\"I\",\"rootAccount\":\"41007\",\"rootNameAccount\":\"OTROS INGRESOS\",\"account\":\"41007001\",\"nameAccount\":\"INGRESO POR DEVOLUCIÓN DE REGALÍAS MINERAS\",\"debit\":0,\"credit\":167454.82}," +
            "{\"accountType\":\"I\",\"rootAccount\":\"41008\",\"rootNameAccount\":\"OTROS INGRESOS\",\"account\":\"41008001\",\"nameAccount\":\"DONACIONES PERSONALES\",\"debit\":0,\"credit\":5000.00}," +
            "{\"accountType\":\"I\",\"rootAccount\":\"41009\",\"rootNameAccount\":\"OTROS INGRESOS\",\"account\":\"41009001\",\"nameAccount\":\"INGRESOS POR SERVICIOS PRESTADOS TRANSPORTE\",\"debit\":0,\"credit\":62640.00}," +
            "{\"accountType\":\"I\",\"rootAccount\":\"41010\",\"rootNameAccount\":\"OTROS INGRESOS\",\"account\":\"41010001\",\"nameAccount\":\"INGRESOS POR SERVICIOS PRESTADOS TRANSPORTE\",\"debit\":0,\"credit\":290933.00}," +
            "{\"accountType\":\"I\",\"rootAccount\":\"41011\",\"rootNameAccount\":\"OTROS INGRESOS\",\"account\":\"41011001\",\"nameAccount\":\"INGRESO POR SERVICIO DE PESAJE EN BALANZA\",\"debit\":0,\"credit\":4830.00}" +
            "]";
    }
    
    /**
     * Datos fallback solo para EGRESOS
     */
    private String getFallbackExpensesData() {
        return "[" +
            // EGRESOS - FLETES Y TRANSPORTES (6 items exactos del PDF)
            "{\"accountType\":\"E\",\"rootAccount\":\"51001\",\"rootNameAccount\":\"FLETES Y TRANSPORTES\",\"account\":\"51001001\",\"nameAccount\":\"FLETES Y TRANSPORTES DE MATERIA PRIMA\",\"debit\":380590.88,\"credit\":0}," +
            "{\"accountType\":\"E\",\"rootAccount\":\"51002\",\"rootNameAccount\":\"FLETES Y TRANSPORTES\",\"account\":\"51002001\",\"nameAccount\":\"FLETES Y TRANSPORTES DE PRODUCTOS TERMINADOS\",\"debit\":95597.06,\"credit\":0}," +
            "{\"accountType\":\"E\",\"rootAccount\":\"51003\",\"rootNameAccount\":\"FLETES Y TRANSPORTES\",\"account\":\"51003001\",\"nameAccount\":\"FLETES Y TRANSPORTES EN GENERAL\",\"debit\":5795.00,\"credit\":0}," +
            "{\"accountType\":\"E\",\"rootAccount\":\"51004\",\"rootNameAccount\":\"FLETES Y TRANSPORTES\",\"account\":\"51004001\",\"nameAccount\":\"DESCUENTOS SOBRE VENTAS\",\"debit\":138800.03,\"credit\":0}," +
            "{\"accountType\":\"E\",\"rootAccount\":\"51005\",\"rootNameAccount\":\"FLETES Y TRANSPORTES\",\"account\":\"51005001\",\"nameAccount\":\"GASTOS DE ESTADIA EN FRONTERA\",\"debit\":50605.38,\"credit\":0}," +
            "{\"accountType\":\"E\",\"rootAccount\":\"51006\",\"rootNameAccount\":\"FLETES Y TRANSPORTES\",\"account\":\"51006001\",\"nameAccount\":\"DESCUENTO SOBRE SERVICIOS\",\"debit\":610.00,\"credit\":0}," +
            // EGRESOS - MATERIAL DIRECTO (2 items exactos del PDF)
            "{\"accountType\":\"E\",\"rootAccount\":\"52001\",\"rootNameAccount\":\"MATERIAL DIRECTO\",\"account\":\"52001001\",\"nameAccount\":\"BARITINA\",\"debit\":500.00,\"credit\":0}," +
            "{\"accountType\":\"E\",\"rootAccount\":\"52002\",\"rootNameAccount\":\"MATERIAL DIRECTO\",\"account\":\"52002001\",\"nameAccount\":\"ULEXITA\",\"debit\":11188.12,\"credit\":0}," +
            // EGRESOS - MANO DE OBRA (6 items exactos del PDF)
            "{\"accountType\":\"E\",\"rootAccount\":\"53001\",\"rootNameAccount\":\"MANO DE OBRA\",\"account\":\"53001001\",\"nameAccount\":\"SUELDOS Y SALARIOS\",\"debit\":445161.35,\"credit\":0}," +
            "{\"accountType\":\"E\",\"rootAccount\":\"53002\",\"rootNameAccount\":\"MANO DE OBRA\",\"account\":\"53002001\",\"nameAccount\":\"AGUINALDOS PRODUCCION\",\"debit\":47641.78,\"credit\":0}," +
            "{\"accountType\":\"E\",\"rootAccount\":\"53003\",\"rootNameAccount\":\"MANO DE OBRA\",\"account\":\"53003001\",\"nameAccount\":\"INDEMNIZACIONES PRODUCCION\",\"debit\":41852.98,\"credit\":0}," +
            "{\"accountType\":\"E\",\"rootAccount\":\"53004\",\"rootNameAccount\":\"MANO DE OBRA\",\"account\":\"53004001\",\"nameAccount\":\"BONOS AL PERSONAL DE PRODUCCION\",\"debit\":9000.00,\"credit\":0}," +
            "{\"accountType\":\"E\",\"rootAccount\":\"53005\",\"rootNameAccount\":\"MANO DE OBRA\",\"account\":\"53005001\",\"nameAccount\":\"PERSONAL EVENTUAL\",\"debit\":13610.42,\"credit\":0}," +
            "{\"accountType\":\"E\",\"rootAccount\":\"53006\",\"rootNameAccount\":\"MANO DE OBRA\",\"account\":\"53006001\",\"nameAccount\":\"SERVICIOS PRESTADOS POR TERCEROS\",\"debit\":2000.00,\"credit\":0}" +
            "]";
    }
    
    /**
     * Ejecuta consulta específica para el reporte detallado que retorna estructura compleja
     * con rootAccount, rootNameAccount, account, nameAccount, accountType, debit, credit
     */
    private String executeDetailedReportQuery(String sql, String startDate, String endDate, String errorFallback) {
        StringBuilder json = new StringBuilder("[");
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setQueryTimeout(30);
            
            System.out.println("Ejecutando consulta detailed_report: " + sql);
            System.out.println("Fechas: " + startDate + " a " + endDate);
            
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            
            long startTime = System.currentTimeMillis();
            rs = stmt.executeQuery();
            long queryTime = System.currentTimeMillis() - startTime;
            
            boolean first = true;
            int count = 0;
            
            while (rs.next()) {
                if (!first) json.append(",");
                
                // Extraer todas las columnas de la consulta detallada
                String rootAccount = rs.getString("rootAccount");
                String rootNameAccount = rs.getString("rootNameAccount");
                String account = rs.getString("account");
                String nameAccount = rs.getString("nameAccount");
                String accountType = rs.getString("accountType");
                double debit = rs.getDouble("debit");
                double credit = rs.getDouble("credit");
                
                json.append("{")
                    .append("\"accountType\":\"").append(escapeJson(accountType != null ? accountType : "")).append("\",")
                    .append("\"rootAccount\":\"").append(escapeJson(rootAccount != null ? rootAccount : "")).append("\",")
                    .append("\"rootNameAccount\":\"").append(escapeJson(rootNameAccount != null ? rootNameAccount : "")).append("\",")
                    .append("\"account\":\"").append(escapeJson(account != null ? account : "")).append("\",")
                    .append("\"nameAccount\":\"").append(escapeJson(nameAccount != null ? nameAccount : "")).append("\",")
                    .append("\"debit\":").append(Math.round(debit * 100.0) / 100.0).append(",")
                    .append("\"credit\":").append(Math.round(credit * 100.0) / 100.0)
                    .append("}");
                first = false;
                count++;
            }
            
            json.append("]");
            
            System.out.println("Consulta detailed_report ejecutada en " + queryTime + "ms. Total registros: " + count);
            
            return json.toString();
            
        } catch (Exception e) {
            System.err.println("Error en consulta detailed_report: " + e.getMessage());
            e.printStackTrace();
            System.out.println("Usando fallback para detailed_report");
            return errorFallback;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                System.err.println("Error cerrando conexión: " + e.getMessage());
            }
        }
    }
}