package com.encens.khipus.servlet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Servlet para dashboard de Producción
 * URL: /production-dashboard-api (configurado en web.xml)
 */
public class ProductionDashboardServlet extends BaseDashboardServlet {

    @Override
    protected String handleDataRequest(String dataType, String startDate, String endDate) throws Exception {
        switch (dataType) {
            case "daily_production":
                return getDailyProductionData(startDate, endDate);
            case "producers":
                return getProducerData(startDate, endDate);
            case "materials":
                return getMaterialData(startDate, endDate);
            case "zones":
                return getZoneData(startDate, endDate);
            case "test":
                return getTestResponse();
            default:
                return "{\"error\":\"Unknown type: " + dataType + "\"}";
        }
    }
    
    private String getProducerData(String startDate, String endDate) {
        String sql = "SELECT " +
                "COALESCE(pe.nombres, 'Sin Nombre') as name, " +
                "COALESCE(SUM(c.pesobal), 0) / 1000 as peso " +
                "FROM acopiomp c " +
                "LEFT JOIN productormateriaprima p ON c.idproductormateriaprima = p.idproductormateriaprima " +
                "LEFT JOIN persona pe ON p.idproductormateriaprima = pe.idpersona " +
                "WHERE c.fecha BETWEEN ? AND ? " +
                "GROUP BY pe.nombres " +
                "ORDER BY peso DESC";
        
        String errorFallback = "[]";
        return executeQueryToJson(sql, startDate, endDate, errorFallback);
    }

    private String getMaterialData(String startDate, String endDate) {
        String sql = "SELECT " +
                "COALESCE(m.nombre, 'Material Desconocido') as name, " +
                "COALESCE(SUM(c.pesobal), 0) / 1000 as peso " +
                "FROM acopiomp c " +
                "JOIN metaproductoproduccion m ON c.idmetaproductoproduccion = m.idmetaproductoproduccion " +
                "WHERE c.fecha BETWEEN ? AND ? " +
                "GROUP BY m.nombre " +
                "ORDER BY peso DESC";

        String errorFallback = "[]";
        return executeQueryToJson(sql, startDate, endDate, errorFallback);
    }

    private String getZoneData(String startDate, String endDate) {
        String sql = "SELECT " +
                "COALESCE(z.nombre, 'Zona Desconocida') as name, " +
                "COALESCE(SUM(c.pesobal), 0) / 1000 as peso " +
                "FROM acopiomp c " +
                "LEFT JOIN zonaproductiva z ON c.idzonaproductiva = z.idzonaproductiva " +
                "WHERE c.fecha BETWEEN ? AND ? " +
                "GROUP BY z.nombre " +
                "ORDER BY peso DESC";

        String errorFallback = "[]";
        return executeQueryToJson(sql, startDate, endDate, errorFallback);
    }

    /**
     * Obtiene datos de producción diaria con cantidades en toneladas
     * Consulta: xpr_producto + xpr_produccion + xpr_plan + inv_articulos
     */
    private String getDailyProductionData(String startDate, String endDate) {
        String sql =
                "SELECT xp.fecha, " +
                "       COALESCE(a.descri, 'Sin Producto') as producto, " +
                "       COALESCE(SUM(p.cantidad), 0) / 1000 as cantidad_producto_tn, " +
                "       COALESCE(SUM(pr.totalmp), 0) / 1000 as cantidad_materia_prima_tn, " +
                "       COALESCE(SUM(pr.costototal), 0) as costo_total " +
                "FROM xpr_producto p " +
                "JOIN xpr_produccion pr ON p.idproduccion = pr.idproduccion " +
                "JOIN xpr_plan xp ON pr.idplan = xp.idplan " +
                "JOIN inv_articulos a ON p.cod_art = a.cod_art " +
                "WHERE xp.fecha BETWEEN ? AND ? " +
                "GROUP BY xp.fecha, a.descri " +
                "ORDER BY xp.fecha ASC, a.descri ASC";

        String errorFallback = "[]";
        return executeDailyProductionQuery(sql, startDate, endDate, errorFallback);
    }

    /**
     * Ejecuta consulta de producción diaria y retorna JSON con estructura compleja
     */
    private String executeDailyProductionQuery(String sql, String startDate, String endDate, String errorFallback) {
        StringBuilder json = new StringBuilder("[");
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setQueryTimeout(30);

            System.out.println("Ejecutando consulta de producción diaria...");
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

                // Extraer columnas
                String fecha = rs.getString("fecha");
                String producto = rs.getString("producto");
                double cantidadProductoTn = rs.getDouble("cantidad_producto_tn");
                double cantidadMateriaPrimaTn = rs.getDouble("cantidad_materia_prima_tn");
                double costoTotal = rs.getDouble("costo_total");

                json.append("{")
                    .append("\"fecha\":\"").append(escapeJson(fecha != null ? fecha : "")).append("\",")
                    .append("\"producto\":\"").append(escapeJson(producto != null ? producto : "Sin Producto")).append("\",")
                    .append("\"cantidad_producto_tn\":").append(Math.round(cantidadProductoTn * 100.0) / 100.0).append(",")
                    .append("\"cantidad_materia_prima_tn\":").append(Math.round(cantidadMateriaPrimaTn * 100.0) / 100.0).append(",")
                    .append("\"costo_total\":").append(Math.round(costoTotal * 100.0) / 100.0)
                    .append("}");
                first = false;
                count++;
            }

            json.append("]");

            System.out.println("Consulta ejecutada en " + queryTime + "ms. Registros: " + count);

            return json.toString();

        } catch (Exception e) {
            System.err.println("Error en consulta de producción diaria: " + e.getMessage());
            e.printStackTrace();
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