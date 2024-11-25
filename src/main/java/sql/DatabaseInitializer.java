package sql;

import data.DatabaseConnector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {
    private static final Logger logger = LogManager.getLogger(DatabaseInitializer.class);

    // Khởi tạo cơ sở dữ liệu từ file .sql
    public static void initializeDatabase(String Path) {
        try (Connection connection = DatabaseConnector.connect()) {
            logger.info("Initializing database...");
            String schemaSQL = new String(Files.readAllBytes(Paths.get(Path)));

            // Chia file thành từng câu lệnh SQL
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

    // Trích xuất tên bảng từ câu lệnh SQL
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
    public static void main(String[] args) {
        String schemaFilePath = "src/main/java/sql/schema.sql";

        logger.info("Initializing database....");
        DatabaseInitializer.initializeDatabase(schemaFilePath);
        logger.info("Initialization complete.");
    }
}