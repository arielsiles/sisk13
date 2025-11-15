package com.encens.khipus.servlet;

import java.sql.*;

/**
 * Servlet para dashboard de Materia Prima
 * URL: /materia-prima-dashboard-api (configurado en web.xml)
 */
public class MateriaPrimaDashboardServlet extends BaseDashboardServlet {

    @Override
    protected String handleDataRequest(String dataType, String startDate, String endDate) throws Exception {
        switch (dataType) {
            case "daily_acopio":
                return getDailyAcopioData(startDate, endDate);
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

    private String getDailyAcopioData(String startDate, String endDate) {
        String sql = "SELECT " +
                "c.fecha as fecha, " +
                "COALESCE(m.nombre, 'Material Desconocido') as material, " +
                "COALESCE(SUM(c.pesobal), 0) / 1000 as peso_diario_tn " +
                "FROM acopiomp c " +
                "JOIN metaproductoproduccion m ON c.idmetaproductoproduccion = m.idmetaproductoproduccion " +
                "WHERE c.fecha BETWEEN ? AND ? " +
                "GROUP BY c.fecha, m.idmetaproductoproduccion, m.nombre " +
                "ORDER BY c.fecha ASC, m.nombre ASC";

        System.out.println("Ejecutando consulta de acopio diario con timeout de 30s: " + sql);
        System.out.println("Fechas: " + startDate + " a " + endDate);

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setQueryTimeout(30);
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);

            long startTime = System.currentTimeMillis();
            ResultSet rs = stmt.executeQuery();
            long queryTime = System.currentTimeMillis() - startTime;

            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            int count = 0;

            while (rs.next()) {
                if (!first) json.append(",");

                String fecha = rs.getString("fecha");
                String material = rs.getString("material");
                double peso = rs.getDouble("peso_diario_tn");

                json.append("{")
                    .append("\"fecha\":\"").append(escapeJson(fecha != null ? fecha : "")).append("\",")
                    .append("\"material\":\"").append(escapeJson(material != null ? material : "Material Desconocido")).append("\",")
                    .append("\"peso_diario_tn\":").append(Math.round(peso * 100.0) / 100.0)
                    .append("}");

                first = false;
                count++;

                if (count <= 5) {
                    System.out.println("Resultado acopio: " + fecha + " - " + material + " = " + peso + " Tn");
                }
            }

            json.append("]");
            System.out.println("Consulta acopio diario ejecutada en " + queryTime + "ms. Total registros: " + count);

            return json.toString();

        } catch (SQLTimeoutException e) {
            System.err.println("Query timeout después de 30 segundos en acopio diario.");
            e.printStackTrace();
            return "[]";
        } catch (SQLException e) {
            System.err.println("Database error en acopio diario: " + e.getMessage());
            e.printStackTrace();
            return "[]";
        } catch (Exception e) {
            System.err.println("Error general en acopio diario: " + e.getMessage());
            e.printStackTrace();
            return "[]";
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
}
