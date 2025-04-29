package Utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to run SQL scripts at application startup
 */
public class RunSQL {
    private static final Logger LOGGER = Logger.getLogger(RunSQL.class.getName());
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/pidatabase";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    /**
     * Run the SQL file from resources
     * @param sqlResourcePath The path to the SQL file in resources
     * @return true if the script executed successfully, false otherwise
     */
    public static boolean runSQLScript(String sqlResourcePath) {
        try (
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement stmt = conn.createStatement();
            InputStream is = RunSQL.class.getResourceAsStream(sqlResourcePath);
        ) {
            if (is == null) {
                LOGGER.log(Level.SEVERE, "Could not find SQL script at path: " + sqlResourcePath);
                return false;
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip comments and empty lines
                if (line.trim().isEmpty() || line.trim().startsWith("--")) {
                    continue;
                }
                sb.append(line);
                
                // Execute when we reach the end of a statement
                if (line.trim().endsWith(";")) {
                    String sql = sb.toString();
                    try {
                        stmt.execute(sql);
                    } catch (Exception e) {
                        // Log but continue - some errors might be expected (e.g., column already exists)
                        LOGGER.log(Level.WARNING, "Error executing SQL: " + e.getMessage());
                    }
                    sb = new StringBuilder();
                }
            }
            
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error running SQL script: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Run a direct SQL command
     * @param sql The SQL command to execute
     * @return true if the command executed successfully, false otherwise
     */
    public static boolean executeSQL(String sql) {
        try (
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sql);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error executing SQL: " + e.getMessage(), e);
            return false;
        }
    }
} 