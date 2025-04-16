package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for handling database connections
 */
public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/pidatabase";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static Connection connection;
    
    /**
     * Gets a connection to the database
     * @return A connection to the database
     * @throws SQLException If a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                // Load the JDBC driver
                Class.forName("com.mysql.jdbc.Driver");
                
                // Create a connection to the database
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
            return connection;
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }
    
    /**
     * Closes the connection to the database
     * @throws SQLException If a database access error occurs
     */
    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
} 