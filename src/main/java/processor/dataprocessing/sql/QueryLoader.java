package processor.dataprocessing.sql;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import others.config.AppConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class QueryLoader {
    private static final Logger logger = LogManager.getLogger(QueryLoader.class);
//    private static final String QUERY_FILE_PATH = "src/main/java/sql/queries.sql";
    private static final String QUERY_FILE_PATH = AppConfig.getQueriesPath();
    private static final Map<String, String> queries = new HashMap<>();

    static {
        try {
            logger.info("Loading SQL Queries from: {}", QUERY_FILE_PATH);
            String content = new String(Files.readAllBytes(Paths.get(QUERY_FILE_PATH)));
            String[] lines = content.split("\n");
            processLines(lines);
            logger.info("Loaded {} SQL Queries.", queries.size());
        } catch (IOException e) {
            logger.error("Cannot load SQL Queries from: {}", QUERY_FILE_PATH, e);
            throw new RuntimeException("Failed to load SQL queries from file: " + QUERY_FILE_PATH, e);
        }
    }

    private static void processLines(String[] lines) {
        StringBuilder currentQuery = new StringBuilder();
        String currentKey = null;

        for (String rawLine : lines) {
            String line = rawLine.trim();

            if (line.startsWith("--")) {
                if (currentKey != null) {
                    queries.put(currentKey, currentQuery.toString().trim());
                }
                // Lưu key mới
                currentKey = line.substring(2).trim();
                currentQuery.setLength(0);
            } else if (!line.isEmpty()) {
                currentQuery.append(line).append(" ");
            }
        }

        if (currentKey != null) {
            queries.put(currentKey, currentQuery.toString().trim());
        }
    }

    public static String getQuery(String key) {
        String query = queries.get(key);
        if (query == null) {
            logger.warn("Cannot find SQL Query for: {}", key);
        }
        return query;
    }

    public static void testQueries() {
        queries.forEach((key, value) -> {
            logger.info("Key: {}\nQuery: {}", key, value);
        });
    }
}