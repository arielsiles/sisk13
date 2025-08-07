package com.encens.khipus.servlet;

import java.util.Iterator;
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
    
    @Override
    protected String handleDataRequest(String dataType, String startDate, String endDate) throws Exception {
        switch (dataType) {
            case "purchases_by_group":
                return getPurchasesByGroupData(startDate, endDate);
            case "categories":
                return getCategoriesData(startDate, endDate);
            case "warehouses":
                return getWarehousesData(startDate, endDate);
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
        
        String errorFallback = "[{\"name\":\"Alimentos\",\"peso\":15000.50},{\"name\":\"Materiales\",\"peso\":12000.25},{\"name\":\"Herramientas\",\"peso\":8500.75}]";
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
     * Datos de categorías (mock por ahora - se puede implementar después)
     */
    private String getCategoriesData(String startDate, String endDate) {
        // Mock data para categorías - se puede reemplazar con consulta real después
        return "[{\"name\":\"Categoría A\",\"peso\":1200},{\"name\":\"Categoría B\",\"peso\":800},{\"name\":\"Categoría C\",\"peso\":600}]";
    }
    
    /**
     * Datos de almacenes (mock por ahora - se puede implementar después)
     */
    private String getWarehousesData(String startDate, String endDate) {
        // Mock data para almacenes - se puede reemplazar con consulta real después
        return "[{\"name\":\"Almacén Central\",\"peso\":2500},{\"name\":\"Almacén Norte\",\"peso\":1800},{\"name\":\"Almacén Sur\",\"peso\":1200}]";
    }
}