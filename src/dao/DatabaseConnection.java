package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/hotel_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connected successfully!");
            return conn;
        } catch (SQLException e) {
            System.err.println("❌ Connection error: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        getConnection(); // quick test
    }
}
