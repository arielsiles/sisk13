package com.encens.khipus.servlet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servlet para dashboard de Inventarios con cache optimizado
 * URL: /inventory-dashboard-api (configurado en web.xml)
 */
public class InventoryDashboardServlet extends BaseDashboardServlet {
    
    // Cache simple para resultados
    private static final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = 10 * 60 * 1000; // 10 minutos
    
    // Clase para entradas de cache
    private static class CacheEntry {
        final String data;
        final long timestamp;
        
        CacheEntry(String data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return (System.currentTimeMillis() - timestamp) > CACHE_DURATION_MS;
        }
    }
    
    private String currentGroupId;
    
    @Override
    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) 
            throws javax.servlet.ServletException, java.io.IOException {
        
        // Obtener parámetro groupId específico para inventarios
        currentGroupId = request.getParameter("groupId");
        
        // Llamar al método padre
        super.doGet(request, response);
    }

    @Override
    protected String handleDataRequest(String dataType, String startDate, String endDate) throws Exception {
        switch (dataType) {
            case "purchases_by_group":
                return getPurchasesByGroupData(startDate, endDate);
            case "groups":
                return getGroupsData(startDate, endDate);
            case "subgroups_by_group":
                return getSubgroupsByGroupData(startDate, endDate, currentGroupId);
            case "correlated_data":
                return getCorrelatedInventoryData(startDate, endDate);
            case "warehouses":
                return getWarehousesData(startDate, endDate);
            case "areas_expenses":
                return getAreasExpensesData(startDate, endDate);
            case "test":
                return getTestResponse();
            default:
                return "{\"error\":\"Unknown type: " + dataType + "\"}";
        }
    }
    
    /**
     * Volumen de compras por Grupo - Consulta optimizada con cache, LIMIT y filtro mínimo
     */
    private String getPurchasesByGroupData(String startDate, String endDate) {
        // Crear clave de cache basada en fechas
        String cacheKey = "purchases_by_group_" + startDate + "_" + endDate;
        
        // Verificar cache primero
        CacheEntry cacheEntry = cache.get(cacheKey);
        if (cacheEntry != null && !cacheEntry.isExpired()) {
            System.out.println("Cache HIT para purchases_by_group: " + cacheKey);
            return cacheEntry.data;
        }
        
        System.out.println("Cache MISS para purchases_by_group: " + cacheKey);
        
        String sql = "SELECT " +
                "g.descri as name, " +
                "SUM(d.total) as peso " +
                "FROM com_detoc d " +
                "JOIN com_encoc e ON d.id_com_encoc = e.id_com_encoc " +
                "JOIN inv_articulos i ON d.cod_art = i.cod_art " +
                "JOIN inv_grupos g ON i.cod_gru = g.cod_gru " +
                "WHERE e.fecha BETWEEN ? AND ? " +
                "GROUP BY g.descri, g.cod_gru " +
                "HAVING SUM(d.total) > 100 " +  // Solo grupos con compras > 100 Bs
                "ORDER BY SUM(d.total) DESC " +
                "LIMIT 15";  // Solo top 15 grupos
        
        String errorFallback = "[]";
        String result = executeQueryToJson(sql, startDate, endDate, errorFallback);
        
        // Guardar en cache
        cache.put(cacheKey, new CacheEntry(result));
        
        // Limpiar entradas expiradas ocasionalmente
        cleanExpiredCache();
        
        return result;
    }
    
    /**
     * Limpia entradas expiradas del cache (llamado ocasionalmente)
     * Compatible con Java 6/7
     */
    private void cleanExpiredCache() {
        // Solo limpiar si el cache tiene más de 10 entradas
        if (cache.size() > 10) {
            Iterator<Map.Entry<String, CacheEntry>> iterator = cache.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, CacheEntry> entry = iterator.next();
                if (entry.getValue().isExpired()) {
                    iterator.remove();
                }
            }
        }
    }
    
    /**
     * Obtiene lista de grupos para el selector
     */
    private String getGroupsData(String startDate, String endDate) {
        String cacheKey = "groups_" + startDate + "_" + endDate;
        
        CacheEntry cacheEntry = cache.get(cacheKey);
        if (cacheEntry != null && !cacheEntry.isExpired()) {
            System.out.println("Cache HIT para groups: " + cacheKey);
            return cacheEntry.data;
        }
        
        System.out.println("Cache MISS para groups: " + cacheKey);
        
        String sql = "SELECT DISTINCT " +
                "g.cod_gru as id, " +
                "g.descri as name " +
                "FROM inv_grupos g " +
                "JOIN inv_subgrupos s ON g.cod_gru = s.cod_gru " +
                "JOIN inv_articulos i ON s.cod_sub = i.cod_sub " +
                "JOIN com_detoc d ON i.cod_art = d.cod_art " +
                "JOIN com_encoc e ON d.id_com_encoc = e.id_com_encoc " +
                "WHERE e.fecha BETWEEN ? AND ? " +
                "ORDER BY g.descri";
        
        String errorFallback = "[]";
        String result = executeQueryToJson(sql, startDate, endDate, errorFallback);
        
        cache.put(cacheKey, new CacheEntry(result));
        cleanExpiredCache();
        
        return result;
    }
    
    /**
     * Volumen de compras por Subgrupo del grupo seleccionado
     */
    private String getSubgroupsByGroupData(String startDate, String endDate, String groupId) {
        if (groupId == null || groupId.trim().isEmpty()) {
            return "[]";
        }
        
        String cacheKey = "subgroups_" + groupId + "_" + startDate + "_" + endDate;
        
        CacheEntry cacheEntry = cache.get(cacheKey);
        if (cacheEntry != null && !cacheEntry.isExpired()) {
            System.out.println("Cache HIT para subgroups: " + cacheKey);
            return cacheEntry.data;
        }
        
        System.out.println("Cache MISS para subgroups: " + cacheKey);
        
        String sql = "SELECT " +
                "s.descri as name, " +
                "SUM(d.total) as peso " +
                "FROM com_detoc d " +
                "JOIN com_encoc e ON d.id_com_encoc = e.id_com_encoc " +
                "JOIN inv_articulos i ON d.cod_art = i.cod_art " +
                "JOIN inv_subgrupos s ON i.cod_sub = s.cod_sub " +
                "JOIN inv_grupos g ON s.cod_gru = g.cod_gru " +
                "WHERE i.cod_gru = s.cod_gru " +
                "AND e.fecha BETWEEN ? AND ? " +
                "AND g.cod_gru = ? " +
                "GROUP BY s.descri, s.cod_sub " +
                "HAVING SUM(d.total) > 50 " +
                "ORDER BY SUM(d.total) DESC " +
                "LIMIT 10";
        
        String errorFallback = "[]";
        String result = executeQueryToJsonWithParam(sql, startDate, endDate, groupId, errorFallback);
        
        cache.put(cacheKey, new CacheEntry(result));
        cleanExpiredCache();
        
        return result;
    }
    
    /**
     * Datos correlacionados: una consulta para ambos gráficos con correlación perfecta
     * Usa la consulta base proporcionada y procesa los datos en memoria
     */
    private String getCorrelatedInventoryData(String startDate, String endDate) {
        String cacheKey = "correlated_inventory_" + startDate + "_" + endDate;
        
        CacheEntry cacheEntry = cache.get(cacheKey);
        if (cacheEntry != null && !cacheEntry.isExpired()) {
            System.out.println("Cache HIT para correlated_inventory: " + cacheKey);
            return cacheEntry.data;
        }
        
        System.out.println("Cache MISS para correlated_inventory: " + cacheKey);
        
        // Consulta base exacta proporcionada
        String sql = "SELECT e.id_com_encoc, e.fecha, g.descri as grupo, s.descri as subgrupo, " +
                "d.cod_art, d.cant_sol, d.costo_uni, d.total as total_Bs, g.cod_gru, s.cod_sub " +
                "FROM com_detoc d " +
                "JOIN com_encoc e ON d.id_com_encoc = e.id_com_encoc " +
                "JOIN inv_articulos i ON d.cod_art = i.cod_art " +
                "JOIN inv_subgrupos s ON i.cod_sub = s.cod_sub " +
                "JOIN inv_grupos g ON s.cod_gru = g.cod_gru " +
                "WHERE i.cod_gru = s.cod_gru AND e.fecha BETWEEN ? AND ? " +
                "ORDER BY g.descri, s.descri";
        
        try {
            String result = executeCorrelatedQuery(sql, startDate, endDate);
            cache.put(cacheKey, new CacheEntry(result));
            cleanExpiredCache();
            return result;
        } catch (Exception e) {
            System.err.println("Error en consulta correlacionada: " + e.getMessage());
            // Fallback con estructura correlacionada
            return getCorrelatedFallbackData();
        }
    }
    
    /**
     * Ejecuta la consulta correlacionada y procesa los resultados en memoria
     */
    private String executeCorrelatedQuery(String sql, String startDate, String endDate) throws Exception {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        // Maps para procesar datos en memoria
        Map<String, Double> groupTotals = new LinkedHashMap<String, Double>();
        Map<String, String> groupIds = new LinkedHashMap<String, String>();
        Map<String, Map<String, Double>> subgroupsByGroup = new LinkedHashMap<String, Map<String, Double>>();
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setQueryTimeout(30);
            
            System.out.println("Ejecutando consulta correlacionada: " + sql);
            System.out.println("Fechas: " + startDate + " a " + endDate);
            
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            
            long startTime = System.currentTimeMillis();
            rs = stmt.executeQuery();
            
            int totalRows = 0;
            
            while (rs.next()) {
                String grupo = rs.getString("grupo");
                String subgrupo = rs.getString("subgrupo");
                String codGru = rs.getString("cod_gru");
                double totalBs = rs.getDouble("total_Bs");
                
                // Procesar grupos
                groupTotals.put(grupo, groupTotals.containsKey(grupo) ? 
                    groupTotals.get(grupo) + totalBs : totalBs);
                groupIds.put(grupo, codGru);
                
                // Procesar subgrupos por grupo
                if (!subgroupsByGroup.containsKey(codGru)) {
                    subgroupsByGroup.put(codGru, new LinkedHashMap<String, Double>());
                }
                Map<String, Double> subgroups = subgroupsByGroup.get(codGru);
                subgroups.put(subgrupo, subgroups.containsKey(subgrupo) ? 
                    subgroups.get(subgrupo) + totalBs : totalBs);
                
                totalRows++;
            }
            
            long queryTime = System.currentTimeMillis() - startTime;
            System.out.println("Consulta correlacionada ejecutada en " + queryTime + "ms. Filas procesadas: " + totalRows);
            System.out.println("Grupos encontrados: " + groupTotals.size());
            
            // Construir JSON correlacionado
            return buildCorrelatedJSON(groupTotals, groupIds, subgroupsByGroup);
            
        } finally {
            // Cerrar recursos
            if (rs != null) try { rs.close(); } catch (Exception e) { }
            if (stmt != null) try { stmt.close(); } catch (Exception e) { }
            if (conn != null) try { conn.close(); } catch (Exception e) { }
        }
    }
    
    /**
     * Construye el JSON correlacionado con grupos, sus IDs y subgrupos organizados
     */
    private String buildCorrelatedJSON(Map<String, Double> groupTotals, Map<String, String> groupIds, 
                                      Map<String, Map<String, Double>> subgroupsByGroup) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        // 1. Grupos para el gráfico principal y selector
        json.append("\"groups\":[");
        boolean firstGroup = true;
        for (Map.Entry<String, Double> entry : groupTotals.entrySet()) {
            if (!firstGroup) json.append(",");
            String groupName = entry.getKey();
            String groupId = groupIds.get(groupName);
            double total = entry.getValue();
            
            json.append("{")
                .append("\"id\":\"").append(escapeJson(groupId)).append("\",")
                .append("\"name\":\"").append(escapeJson(groupName)).append("\",")
                .append("\"peso\":").append(Math.round(total * 100.0) / 100.0)
                .append("}");
            firstGroup = false;
        }
        json.append("],");
        
        // 2. Subgrupos organizados por grupo
        json.append("\"subgroupsByGroup\":{");
        boolean firstSubgroupGroup = true;
        for (Map.Entry<String, Map<String, Double>> groupEntry : subgroupsByGroup.entrySet()) {
            if (!firstSubgroupGroup) json.append(",");
            String groupId = groupEntry.getKey();
            Map<String, Double> subgroups = groupEntry.getValue();
            
            json.append("\"").append(groupId).append("\":[");
            boolean firstSubgroup = true;
            for (Map.Entry<String, Double> subgroupEntry : subgroups.entrySet()) {
                if (!firstSubgroup) json.append(",");
                json.append("{")
                    .append("\"name\":\"").append(escapeJson(subgroupEntry.getKey())).append("\",")
                    .append("\"peso\":").append(Math.round(subgroupEntry.getValue() * 100.0) / 100.0)
                    .append("}");
                firstSubgroup = false;
            }
            json.append("]");
            firstSubgroupGroup = false;
        }
        json.append("}");
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * Datos de fallback con estructura correlacionada (sin datos mock)
     */
    private String getCorrelatedFallbackData() {
        return "{\"groups\":[],\"subgroupsByGroup\":{}}";
    }

    /**
     * Datos de almacenes (sin datos por implementar consulta real)
     */
    private String getWarehousesData(String startDate, String endDate) {
        return "[]";
    }
    
    /**
     * Gastos de inventario por área - Basado en reporte InventoryExpenseSummaryReport
     */
    private String getAreasExpensesData(String startDate, String endDate) {
        String sql = "SELECT " +
                "d.nombre as name, " +
                "SUM(md.monto) as peso " +
                "FROM inv_movdet md " +
                "JOIN inv_mov im ON md.no_cia = im.no_cia AND md.no_trans = im.no_trans AND md.estado = im.estado " +
                "JOIN inv_vales wv ON im.no_cia = wv.no_cia AND im.no_trans = wv.no_trans " +
                "JOIN inv_destino d ON wv.iddestino = d.iddestino " +
                "WHERE wv.fecha BETWEEN ? AND ? " +
                "AND wv.estado = 'APR' " +
                "AND wv.cod_doc = 'EGR' " +
                "AND wv.no_cia = '01' " +
                "GROUP BY d.nombre " +
                "HAVING SUM(md.monto) > 0 " +
                "ORDER BY SUM(md.monto) DESC " +
                "LIMIT 20";
        
        String errorFallback = "[]";
        return executeQueryToJson(sql, startDate, endDate, errorFallback);
    }
}