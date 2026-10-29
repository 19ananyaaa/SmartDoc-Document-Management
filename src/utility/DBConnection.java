package utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for establishing a database connection to MySQL.
 * Configured for MySQL 8.x.
 */
public class DBConnection {

    // Database configurations
    private static final String URL = "jdbc:mysql://localhost:3306/smart_document_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";

    private static final String USER = "root";

    private static final String PASSWORD = "root";

    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    static {
        try {
            // Load the MySQL JDBC driver class
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Add Connector/J to your classpath/lib folder.");
            e.printStackTrace();
        }
    }

    /**
     * Gets a connection to the database.
     * 
     * @return Connection object
     * @throws SQLException if database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Closes DB resources helper method to prevent resource leaks.
     * 
     * @param conn the connection to close
     * @param stmt the statement to close
     * @param rs   the result set to close
     */
    public static void closeResources(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    System.err.println("Error closing resource: " + e.getMessage());
                }
            }
        }
    }
}
