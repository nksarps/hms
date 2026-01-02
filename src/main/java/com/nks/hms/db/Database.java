package com.nks.hms.db;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private static final Dotenv DOTENV = Dotenv.configure().ignoreIfMissing().load();

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        String url = getSetting("DB_URL", "jdbc:mysql://localhost:3306/hms?useSSL=false");
        String user = getSetting("DB_USER", "root");
        String password = getSetting("DB_PASSWORD", "");
        return DriverManager.getConnection(url, user, password);
    }

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
