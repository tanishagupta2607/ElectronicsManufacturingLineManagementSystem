package com.emls;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Config {
    private static final Properties PROPS = new Properties();
    static {
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) PROPS.load(in);
        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }
    private Config() {}

    public static String get(String key, String fallback) {
        String env = System.getenv("EMLS_" + key.toUpperCase().replace('.', '_'));
        if (env != null && !env.isBlank()) return env;
        return PROPS.getProperty(key, fallback);
    }

    public static Connection connect() throws SQLException {
        String url = get("db.url", "jdbc:oracle:thin:@localhost:1521/XEPDB1");
        String user = get("db.user", "EMLS_APP");
        String password = get("db.password", "");
        if (password.isBlank()) throw new SQLException(
            "Database password is not configured. Set EMLS_DB_PASSWORD or db.password in application.properties.");
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("Oracle JDBC driver (ojdbc) is missing from the classpath.", ex);
        }
        return DriverManager.getConnection(url, user, password);
    }
}
