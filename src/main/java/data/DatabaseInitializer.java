package data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import config.AppConfig;

public class DatabaseInitializer {
    private static final Logger logger = LogManager.getLogger(DatabaseInitializer.class);

    // Initialize database from .sql file
    public static void initializeDatabase(String Path) {
        try (Connection connection = DatabaseConnector.connect()) {
            logger.info("Initializing database...");
            String schemaSQL = new String(Files.readAllBytes(Paths.get(Path)));

            // Split file into SQL statements
            String[] statements = schemaSQL.split(";");

            for (String sql : statements) {
                sql = sql.trim();
                if (sql.isEmpty()) continue;

                String tableName = extractTableName(sql);

                if (tableName != null) {
                    try (Statement statement = connection.createStatement()) {
                        statement.execute(sql);
                    }
                }
            }
            logger.info("Database initialization complete.");
        } catch (Exception e) {
            logger.error("Failed to initialize the database!", e);
            throw new RuntimeException("Failed to initialize the database!", e);
        }
    }

    // Extract table name from SQL statement
    private static String extractTableName(String sql) {
        sql = sql.toUpperCase();
        if (sql.startsWith("CREATE TABLE")) {
            int startIndex = sql.indexOf("TABLE") + 6;
            int endIndex = sql.indexOf("(");
            return sql.substring(startIndex, endIndex).trim();
        }
        logger.warn("Failed to extract table name from SQL command: {}", sql);
        return null;
    }
}