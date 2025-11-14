package com.encens.khipus.servlet;

/**
 * Servlet para dashboard de Materia Prima
 * URL: /materia-prima-dashboard-api (configurado en web.xml)
 */
public class MateriaPrimaDashboardServlet extends BaseDashboardServlet {

    @Override
    protected String handleDataRequest(String dataType, String startDate, String endDate) throws Exception {
        switch (dataType) {
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
}
