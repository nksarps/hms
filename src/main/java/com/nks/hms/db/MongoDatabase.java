package com.nks.hms.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * MongoDB connection factory that provides MongoClient connections to the HMS NoSQL database.
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
 *   <li>MONGO_URI - Full MongoDB connection URI (default: mongodb://localhost:27017)</li>
 *   <li>MONGO_DB_NAME - Database name (default: hms_nosql)</li>
 * </ul>
 * 
 * <p>This class is final and has a private constructor to prevent instantiation.
 * All methods are static.
 * 
 * @see Dotenv
 */
public final class MongoDatabase {
    // Pulls connection settings from env/.env with sane defaults for local dev.
    private static final Dotenv DOTENV = Dotenv.configure().ignoreIfMissing().load();
    private static MongoClient mongoClient;
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private MongoDatabase() {
    }
    
    /**
     * Creates and returns a MongoDB database connection.
     * 
     * <p>Connection parameters are loaded from environment variables or .env file.
     * The MongoClient is created lazily on first access and reused for subsequent calls.
     * 
     * <p>Example usage:
     * <pre>
     * MongoDatabase database = MongoDatabase.getDatabase();
     * MongoCollection&lt;Document&gt; collection = database.getCollection("patient_notes");
     * </pre>
     * 
     * @return MongoDB database instance
     */
    public static com.mongodb.client.MongoDatabase getDatabase() {
        String uri = getEnvOrDefault("MONGO_URI", "mongodb://localhost:27017");
        String dbName = getEnvOrDefault("MONGO_DB_NAME", "hms_nosql");
        
        if (mongoClient == null) {
            mongoClient = MongoClients.create(uri);
        }
        
        return mongoClient.getDatabase(dbName);
    }
    
    /**
     * Closes the MongoDB client connection.
     * Should be called when shutting down the application.
     */
    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
        }
    }
    
    /**
     * Helper method to get environment variable with fallback to .env file and default value.
     * 
     * @param key The environment variable key
     * @param defaultValue The default value if not found
     * @return The resolved value
     */
    private static String getEnvOrDefault(String key, String defaultValue) {
        String systemValue = System.getenv(key);
        if (systemValue != null) {
            return systemValue;
        }
        return DOTENV.get(key, defaultValue);
    }
}
