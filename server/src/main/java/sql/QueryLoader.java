package sql;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import config.AppConfig;

public class QueryLoader {
    private static final String QUERY_FILE_PATH = AppConfig.getQueriesPath();
    private static final Map<String, String> queries = new HashMap<>();

    static {
        try {
            String content = new String(Files.readAllBytes(Paths.get(QUERY_FILE_PATH)));
            String[] lines = content.split("\n");
            processLines(lines);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load SQL queries from file: " + QUERY_FILE_PATH, e);
        }
    }

    // Method of processing each line in the file
    private static void processLines(String[] lines) {
        final StringBuilder currentQuery = new StringBuilder();
        final Holder<String> currentKeyHolder = new Holder<>(null);

        for (final String rawLine : lines) {
            final String line = rawLine.trim();

            if (line.startsWith("--")) { // If a line started with "--"
                if (currentKeyHolder.value != null) {
                    queries.put(currentKeyHolder.value, currentQuery.toString().trim());
                }
                currentKeyHolder.value = line.substring(2).trim(); // Stories new key
                currentQuery.setLength(0); // Reset StringBuilder
            } else if (!line.isEmpty()) { // If not an empty line
                currentQuery.append(line).append(" ");
            }
        }

        // Stories last query
        if (currentKeyHolder.value != null) {
            queries.put(currentKeyHolder.value, currentQuery.toString().trim());
        }
    }

    // Get query by key
    public static String getQuery(String key) {
        return queries.get(key);
    }

    // Class Holder to process reassigned local variable
    private static class Holder<T> {
        T value;

        Holder(T value) {
            this.value = value;
        }
    }
}