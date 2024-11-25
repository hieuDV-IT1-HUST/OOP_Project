package sql;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class QueryLoader {
    private static final String QUERY_FILE_PATH = "src/main/java/sql/queries.sql";
    private static final Map<String, String> queries = new HashMap<>();

    static {
        try {
            String content = new String(Files.readAllBytes(Paths.get(QUERY_FILE_PATH)));
            String[] lines = content.split("\n");
            String currentKey = null;
            StringBuilder currentQuery = new StringBuilder();

            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("--")) {
                    if (currentKey != null) {
                        queries.put(currentKey, currentQuery.toString().trim());
                    }
                    currentKey = line.substring(2).trim(); // Lấy tên truy vấn sau "--"
                    currentQuery = new StringBuilder();
                } else if (!line.isEmpty()) {
                    currentQuery.append(line).append(" ");
                }
            }
            if (currentKey != null) {
                queries.put(currentKey, currentQuery.toString().trim());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load SQL queries from file: " + QUERY_FILE_PATH, e);
        }
    }

    public static String getQuery(String key) {
        return queries.get(key);
    }
}