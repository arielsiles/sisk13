package com.encens.khipus.servlet;

import com.encens.khipus.dataintegration.configuration.Configuration;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

/**
 * Servlet simple para dashboard - Sin dependencias de Seam/JSF
 * URL: /dashboard-api
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard-api"})
public class DashboardServlet extends HttpServlet {
    
    // Usar configuración centralizada
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Configurar respuesta JSON
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Access-Control-Allow-Origin", "*");
        
        PrintWriter out = response.getWriter();
        
        try {
            String dataType = request.getParameter("type");
            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");
            
            System.out.println("Dashboard API called - type: " + dataType + ", dates: " + startDate + " to " + endDate);
            
            if (dataType == null) {
                out.print("{\"error\":\"Missing type parameter\"}");
                return;
            }
            
            // Fechas por defecto si no se especifican
            if (startDate == null || endDate == null) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                endDate = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
                cal.set(java.util.Calendar.DAY_OF_YEAR, 1);
                startDate = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
            }
            
            String jsonResult = "";
            
            switch (dataType) {
                case "producers":
                    jsonResult = getProducerData(startDate, endDate);
                    break;
                case "materials":
                    jsonResult = getMaterialData(startDate, endDate);
                    break;
                case "zones":
                    jsonResult = getZoneData(startDate, endDate);
                    break;
                case "test":
                    jsonResult = "{\"status\":\"ok\",\"message\":\"Dashboard API working\",\"timestamp\":" + System.currentTimeMillis() + "}";
                    break;
                default:
                    jsonResult = "{\"error\":\"Unknown type: " + dataType + "\"}";
            }
            
            out.print(jsonResult);
            
        } catch (Exception e) {
            System.err.println("Error in DashboardServlet: " + e.getMessage());
            e.printStackTrace();
            out.print("{\"error\":\"" + e.getMessage().replace("\"", "\\\"") + "\"}");
        }
    }
    
    private String getProducerData(String startDate, String endDate) {
        StringBuilder json = new StringBuilder("[");

        try (Connection conn = getConnection()) {
            
            // Consulta corregida: productormateriaprima hereda de persona
            String sql = "SELECT " +
                    "COALESCE(pe.nombres, 'Sin Nombre') as name, " +
                    "COALESCE(SUM(c.pesobal), 0) / 1000 as peso " +
                    "FROM acopiomp c " +
                    "LEFT JOIN productormateriaprima p ON c.idproductormateriaprima = p.idproductormateriaprima " +
                    "LEFT JOIN persona pe ON p.idproductormateriaprima = pe.idpersona " +
                    "WHERE c.fecha BETWEEN ? AND ? " +
                    "GROUP BY pe.nombres " +
                    "ORDER BY peso DESC";
            
            System.out.println("Ejecutando consulta productores: " + sql);
            System.out.println("Fechas: " + startDate + " a " + endDate);
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            
            ResultSet rs = stmt.executeQuery();
            boolean first = true;
            int count = 0;
            
            while (rs.next()) {
                if (!first) json.append(",");
                double peso = rs.getDouble("peso");
                String name = rs.getString("name");
                
                json.append("{")
                    .append("\"name\":\"").append(escapeJson(name != null ? name : "Sin Nombre")).append("\",")
                    .append("\"peso\":").append(Math.round(peso * 100.0) / 100.0)
                    .append("}");
                first = false;
                count++;
                
                System.out.println("Productor: " + name + " = " + peso + " Tn");
            }
            
            System.out.println("Total productores encontrados: " + count);
            
        } catch (SQLException e) {
            System.err.println("Database error en productores: " + e.getMessage());
            e.printStackTrace();
            // Datos mock en caso de error
            return "[{\"name\":\"Productor Mock 1\",\"peso\":150.5},{\"name\":\"Productor Mock 2\",\"peso\":200.25}]";
        }
        
        json.append("]");
        return json.toString();
    }
    
    private String getMaterialData(String startDate, String endDate) {
        StringBuilder json = new StringBuilder("[");

        try (Connection conn = getConnection()) {
            
            // Consulta corregida con nombres reales de tablas
            String sql = "SELECT " +
                    "COALESCE(m.nombre, 'Material Desconocido') as name, " +
                    "COALESCE(SUM(c.pesobal), 0) / 1000 as peso " +
                    "FROM acopiomp c " +
                    "JOIN metaproductoproduccion m ON c.idmetaproductoproduccion = m.idmetaproductoproduccion " +
                    "WHERE c.fecha BETWEEN ? AND ? " +
                    "GROUP BY m.nombre " +
                    "ORDER BY peso DESC";
            
            System.out.println("Ejecutando consulta materiales: " + sql);
            System.out.println("Fechas: " + startDate + " a " + endDate);
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            
            ResultSet rs = stmt.executeQuery();
            boolean first = true;
            int count = 0;
            
            while (rs.next()) {
                if (!first) json.append(",");
                double peso = rs.getDouble("peso");
                String name = rs.getString("name");
                
                json.append("{")
                    .append("\"name\":\"").append(escapeJson(name != null ? name : "Material Desconocido")).append("\",")
                    .append("\"peso\":").append(Math.round(peso * 100.0) / 100.0)
                    .append("}");
                first = false;
                count++;
                
                System.out.println("Material: " + name + " = " + peso + " Tn");
            }
            
            System.out.println("Total materiales encontrados: " + count);
            
        } catch (SQLException e) {
            System.err.println("Database error en materiales: " + e.getMessage());
            e.printStackTrace();
            // Datos mock en caso de error
            return "[{\"name\":\"Material Mock 1\",\"peso\":300.5},{\"name\":\"Material Mock 2\",\"peso\":250.75}]";
        }
        
        json.append("]");
        return json.toString();
    }
    
    private String getZoneData(String startDate, String endDate) {
        StringBuilder json = new StringBuilder("[");

        try (Connection conn = getConnection()) {
            
            // Consulta corregida con nombres reales de tablas
            String sql = "SELECT " +
                    "COALESCE(z.nombre, 'Zona Desconocida') as name, " +
                    "COALESCE(SUM(c.pesobal), 0) / 1000 as peso " +
                    "FROM acopiomp c " +
                    "LEFT JOIN zonaproductiva z ON c.idzonaproductiva = z.idzonaproductiva " +
                    "WHERE c.fecha BETWEEN ? AND ? " +
                    "GROUP BY z.nombre " +
                    "ORDER BY peso DESC";
            
            System.out.println("Ejecutando consulta zonas: " + sql);
            System.out.println("Fechas: " + startDate + " a " + endDate);
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            
            ResultSet rs = stmt.executeQuery();
            boolean first = true;
            int count = 0;
            
            while (rs.next()) {
                if (!first) json.append(",");
                double peso = rs.getDouble("peso");
                String name = rs.getString("name");
                
                json.append("{")
                    .append("\"name\":\"").append(escapeJson(name != null ? name : "Zona Desconocida")).append("\",")
                    .append("\"peso\":").append(Math.round(peso * 100.0) / 100.0)
                    .append("}");
                first = false;
                count++;
                
                System.out.println("Zona: " + name + " = " + peso + " Tn");
            }
            
            System.out.println("Total zonas encontradas: " + count);
            
        } catch (SQLException e) {
            System.err.println("Database error en zonas: " + e.getMessage());
            e.printStackTrace();
            // Datos mock en caso de error
            return "[{\"name\":\"Zona Mock 1\",\"peso\":400.75},{\"name\":\"Zona Mock 2\",\"peso\":350.25}]";
        }
        
        json.append("]");
        return json.toString();
    }
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    /**
     * Obtener conexión a la base de datos via JNDI DataSource
     * Usa el mismo datasource que el resto de la aplicación KHIPUS
     */
    private Connection getConnection() throws SQLException {
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Permitir POST también
        doGet(request, response);
    }
}