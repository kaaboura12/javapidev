package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class to manage database connections
 */
public class MyDb {
    
    private static MyDb instance;
    private Connection connection;
    
    // Database connection properties
    private static final String URL = "jdbc:mysql://localhost:3306/pidatabase";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    
    /**
     * Private constructor to prevent instantiation
     */
    private MyDb() {
        try {
            // Register JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Open a connection
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }
    
    /**
     * Get the singleton instance
     * @return the MyDb instance
     */
    public static synchronized MyDb getInstance() {
        if (instance == null) {
            instance = new MyDb();
        }
        return instance;
    }
    
    /**
     * Get the database connection
     * @return the connection object
     */
    public Connection getConnection() {
        try {
            // Check if connection is closed or invalid, and if so, create a new one
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("Error checking connection: " + e.getMessage());
        }
        return connection;
    }
    
    /**
     * Close the database connection
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
