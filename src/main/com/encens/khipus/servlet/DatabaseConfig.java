package com.encens.khipus.servlet;

import java.io.InputStream;

/**
 * Configuración de base de datos para el dashboard simple
 */
public class DatabaseConfig {
    private static String DB_URL = "jdbc:mysql://localhost:3306/terdemol";
    private static String DB_USER = "adm";
    private static String DB_PASS = "Cisc.13";
    private static String DB_DRIVER = "com.mysql.jdbc.Driver";
    
    static {
        loadConfig();
    }
    
    private static void loadConfig() {
        try {
            // Intentar cargar configuración desde persistence.xml o datasource
            InputStream is = DatabaseConfig.class.getResourceAsStream("/META-INF/persistence.xml");
            if (is != null) {
                // Parsear persistence.xml para obtener configuración
                System.out.println("Cargando configuración desde persistence.xml...");
                // Por ahora usar configuración por defecto
            }
            
            // Cargar driver MySQL
            Class.forName(DB_DRIVER);
            System.out.println("✅ Driver MySQL cargado correctamente");
            
        } catch (Exception e) {
            System.err.println("⚠️  Error cargando configuración DB: " + e.getMessage());
            // Usar configuración por defecto
        }
    }
    
    public static String getDbUrl() {
        return DB_URL;
    }
    
    public static String getDbUser() {
        return DB_USER;
    }
    
    public static String getDbPassword() {
        return DB_PASS;
    }
    
    // Método para configurar manualmente (si es necesario)
    public static void configure(String url, String user, String password) {
        DB_URL = url;
        DB_USER = user;
        DB_PASS = password;
        System.out.println("✅ Configuración DB actualizada: " + DB_URL);
    }
}