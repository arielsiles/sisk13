package com.encens.khipus.servlet;

import com.encens.khipus.dataintegration.configuration.Configuration;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.SimpleDateFormat;

/**
 * Clase base para todos los servlets de dashboard
 * Contiene funciones comunes para manejo de BD, fechas y JSON
 */
public abstract class BaseDashboardServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Configurar respuesta JSON
        setupJsonResponse(response);
        
        PrintWriter out = response.getWriter();
        
        try {
            String dataType = request.getParameter("type");
            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");
            
            System.out.println(getClass().getSimpleName() + " API called - type: " + dataType + ", dates: " + startDate + " to " + endDate);
            
            if (dataType == null) {
                out.print("{\"error\":\"Missing type parameter\"}");
                return;
            }
            
            // Fechas por defecto si no se especifican
            String[] dates = getDefaultDates(startDate, endDate);
            startDate = dates[0];
            endDate = dates[1];
            
            String jsonResult = handleDataRequest(dataType, startDate, endDate);
            out.print(jsonResult);
            
        } catch (Exception e) {
            System.err.println("Error in " + getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Permitir POST también
        doGet(request, response);
    }
    
    /**
     * Método abstracto que debe implementar cada servlet específico
     */
    protected abstract String handleDataRequest(String dataType, String startDate, String endDate) throws Exception;
    
    /**
     * Configurar respuesta JSON con headers apropiados
     */
    protected void setupJsonResponse(HttpServletResponse response) {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Access-Control-Allow-Origin", "*");
    }
    
    /**
     * Obtener fechas por defecto si no se especifican
     */
    protected String[] getDefaultDates(String startDate, String endDate) {
        if (startDate == null || endDate == null) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            endDate = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
            cal.set(java.util.Calendar.DAY_OF_YEAR, 1);
            startDate = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
        }
        return new String[]{startDate, endDate};
    }
    
    /**
     * Obtener conexión a la base de datos via JNDI DataSource
     * Usa el mismo datasource que el resto de la aplicación KHIPUS
     */
    protected Connection getConnection() throws SQLException {
        String dataSourceJNDI = Configuration.i.getLocalDataSource();
        try {
            Context context = new InitialContext();
            DataSource dataSource = (DataSource) context.lookup(dataSourceJNDI);

            if (dataSource == null) {
                throw new SQLException("DataSource not found: " + dataSourceJNDI);
            }

            System.out.println("Getting connection from JNDI DataSource: " + dataSourceJNDI);
            return dataSource.getConnection();

        } catch (NamingException e) {
            throw new SQLException("Cannot lookup JNDI DataSource: " + dataSourceJNDI, e);
        }
    }
    
    /**
     * Escapar caracteres especiales en JSON
     */
    protected String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * Manejar respuesta de test de conexión
     */
    protected String getTestResponse() {
        return "{\"status\":\"ok\",\"message\":\"Dashboard API working\",\"timestamp\":" + System.currentTimeMillis() + "}";
    }
    
    /**
     * Ejecutar consulta SQL y construir JSON con estructura name/peso
     * Incluye timeout de 30 segundos para evitar consultas lentas
     */
    protected String executeQueryToJson(String sql, String startDate, String endDate, String errorFallback) {
        StringBuilder json = new StringBuilder("[");
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            
            // Configurar timeout de 30 segundos para evitar consultas lentas
            stmt.setQueryTimeout(30);
            
            System.out.println("Ejecutando consulta con timeout de 30s: " + sql);
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
                
                String name = rs.getString("name");
                double value = rs.getDouble(2); // Segunda columna (peso, total_Bs, etc)
                
                json.append("{")
                    .append("\"name\":\"").append(escapeJson(name != null ? name : "Sin Nombre")).append("\",")
                    .append("\"peso\":").append(Math.round(value * 100.0) / 100.0)
                    .append("}");
                first = false;
                count++;
                
                System.out.println("Resultado: " + name + " = " + value);
            }
            
            System.out.println("Consulta ejecutada en " + queryTime + "ms. Total registros: " + count);
            
        } catch (SQLTimeoutException e) {
            System.err.println("Query timeout después de 30 segundos. Usando datos fallback.");
            return errorFallback;
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            return errorFallback;
        } finally {
            // Cerrar recursos manualmente para compatibilidad Java 6
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.err.println("Error cerrando ResultSet: " + e.getMessage());
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.err.println("Error cerrando PreparedStatement: " + e.getMessage());
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error cerrando Connection: " + e.getMessage());
                }
            }
        }
        
        json.append("]");
        return json.toString();
    }
    
    /**
     * Ejecutar consulta SQL con 3 parámetros y construir JSON con estructura name/peso
     */
    protected String executeQueryToJsonWithParam(String sql, String startDate, String endDate, String param3, String errorFallback) {
        StringBuilder json = new StringBuilder("[");
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setQueryTimeout(30);
            
            System.out.println("Ejecutando consulta con 3 parámetros: " + sql);
            System.out.println("Parámetros: " + startDate + ", " + endDate + ", " + param3);
            
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            stmt.setString(3, param3);
            
            long startTime = System.currentTimeMillis();
            rs = stmt.executeQuery();
            long queryTime = System.currentTimeMillis() - startTime;
            
            boolean first = true;
            int count = 0;
            
            while (rs.next()) {
                if (!first) json.append(",");
                
                String name = rs.getString("name");
                double value = rs.getDouble(2);
                
                json.append("{")
                    .append("\"name\":\"").append(escapeJson(name != null ? name : "Sin Nombre")).append("\",")
                    .append("\"peso\":").append(Math.round(value * 100.0) / 100.0)
                    .append("}");
                first = false;
                count++;
                
                System.out.println("Resultado: " + name + " = " + value);
            }
            
            System.out.println("Consulta con 3 parámetros ejecutada en " + queryTime + "ms. Total registros: " + count);
            
        } catch (SQLTimeoutException e) {
            System.err.println("Query timeout después de 30 segundos. Usando datos fallback.");
            return errorFallback;
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            return errorFallback;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.err.println("Error cerrando ResultSet: " + e.getMessage());
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.err.println("Error cerrando PreparedStatement: " + e.getMessage());
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error cerrando Connection: " + e.getMessage());
                }
            }
        }
        
        json.append("]");
        return json.toString();
    }
}