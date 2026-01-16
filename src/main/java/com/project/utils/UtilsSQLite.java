package com.project.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UtilsSQLite {

    /**
     * Estableix connexió amb la base de dades SQLite.
     * @param filePath Ruta al fitxer .db
     * @return Objecte Connection obert
     * @throws SQLException Si hi ha error de connexió
     */
    public static Connection connect(String filePath) throws SQLException {
        String url = "jdbc:sqlite:" + filePath;
        Connection conn = DriverManager.getConnection(url);
        
        if (conn != null) {
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("BBDD driver: " + meta.getDriverName());
            System.out.println("BBDD SQLite connectada");
        }
        return conn;
    }

    /**
     * Tanca la connexió de manera segura.
     * @param conn Connexió a tancar
     */
    public static void disconnect(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("DDBB SQLite desconnectada");
            }
        } catch (SQLException ex) {
            System.err.println("Error al tancar la connexió: " + ex.getMessage());
        }
    }

    /**
     * Llista les taules existents a la base de dades.
     * Utilitza try-with-resources per tancar el ResultSet automàticament.
     */
    public static List<String> listTables(Connection conn) throws SQLException {
        List<String> list = new ArrayList<>();
        
        // Obtenim les metadades i el ResultSet dins d'un try-with-resources
        try (ResultSet rs = conn.getMetaData().getTables(null, null, null, null)) {
            while (rs.next()) {
                list.add(rs.getString("TABLE_NAME"));
            }
        }
        return list;
    }

    /**
     * Executa una consulta de tipus UPDATE, INSERT o DELETE.
     * Tanca el Statement automàticament.
     */
    public static int queryUpdate(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }

    /**
     * Executa una consulta de tipus SELECT.
     * NOTA: El Statement NO es tanca aquí perquè es necessita per llegir el ResultSet.
     * Qui cridi aquest mètode ha de tancar el ResultSet (i implícitament el Statement).
     */
    public static ResultSet querySelect(Connection conn, String sql) throws SQLException {
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(sql);
    }
}