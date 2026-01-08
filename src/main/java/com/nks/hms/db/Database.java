package com.nks.hms.db;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection factory that provides JDBC connections to the MySQL HMS database.
 * 
 * <p>Configuration is loaded from multiple sources in order of precedence:
 * <ol>
 *   <li>System environment variables (highest priority)</li>
 *   <li>.env file in the project root (via dotenv library)</li>
 *   <li>Hard-coded defaults for local development (lowest priority)</li>
 * </ol>
 * 
 * <p>Supported configuration keys:
 * <ul>
 *   <li>DB_URL - Full JDBC connection URL (default: jdbc:mysql://localhost:3306/hms?useSSL=false)</li>
 *   <li>DB_USER - Database username (default: root)</li>
 *   <li>DB_PASSWORD - Database password (default: empty string)</li>
 * </ul>
 * 
 * <p>This class is final and has a private constructor to prevent instantiation.
 * All methods are static.
 * 
 * @see Dotenv
 */
public final class Database {
    // Pulls connection settings from env/.env with sane defaults for local dev.
    private static final Dotenv DOTENV = Dotenv.configure().ignoreIfMissing().load();

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private Database() {
    }

    /**
     * Creates and returns a new JDBC connection to the HMS database.
     * 
     * <p>Connection parameters are loaded from environment variables or .env file.
     * Each call creates a new connection; callers are responsible for closing
     * the connection when done (typically via try-with-resources).
     * 
     * <p>Example usage:
     * <pre>
     * try (Connection conn = Database.getConnection()) {
     *     // Use connection
     * }
     * </pre>
     * 
     * @return A new JDBC connection to the database
     * @throws SQLException If connection cannot be established due to network issues,
     *                      authentication failure, or database unavailability
     */
    // Build a JDBC connection using env/override settings.
    public static Connection getConnection() throws SQLException {
        String url = getSetting("DB_URL", "jdbc:mysql://localhost:3306/hms?useSSL=false");
        String user = getSetting("DB_USER", "root");
        String password = getSetting("DB_PASSWORD", "");
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Retrieves a configuration value from multiple sources.
     * 
     * <p>Checks in order:
     * <ol>
     *   <li>System environment variable (System.getenv)</li>
     *   <li>.env file via Dotenv library</li>
     *   <li>Provided fallback value</li>
     * </ol>
     * 
     * @param key The configuration key to look up (e.g., "DB_URL")
     * @param fallback Default value to return if key is not found in any source
     * @return The configuration value from the highest priority source, or fallback if not found
     */
    private static String getSetting(String key, String fallback) {
        String value = System.getenv(key);
        if (value != null && !value.isBlank()) {
            return value;
        }
        value = DOTENV.get(key);
        if (value != null && !value.isBlank()) {
            return value;
        }
        return fallback;
    }
}
