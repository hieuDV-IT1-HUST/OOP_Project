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

    // Static block để tải file khi lớp được khởi tạo
    static {
        try {
            logger.info("Đang tải file truy vấn SQL từ: {}", QUERY_FILE_PATH);
            String content = new String(Files.readAllBytes(Paths.get(QUERY_FILE_PATH)));
            String[] lines = content.split("\n");
            processLines(lines);
            logger.info("Đã tải xong {} truy vấn SQL.", queries.size());
        } catch (IOException e) {
            logger.error("Không thể tải file truy vấn từ: {}", QUERY_FILE_PATH, e);
            throw new RuntimeException("Failed to load SQL queries from file: " + QUERY_FILE_PATH, e);
        }
    }

    // Phương thức xử lý từng dòng trong file queries.sql
    private static void processLines(String[] lines) {
        StringBuilder currentQuery = new StringBuilder();
        String currentKey = null;

        for (String rawLine : lines) {
            String line = rawLine.trim();

            if (line.startsWith("--")) { // Nếu dòng bắt đầu bằng '--'
                if (currentKey != null) {
                    // Thêm truy vấn hiện tại vào Map
                    queries.put(currentKey, currentQuery.toString().trim());
                }
                // Lưu key mới
                currentKey = line.substring(2).trim();
                currentQuery.setLength(0); // Reset StringBuilder
            } else if (!line.isEmpty()) {
                currentQuery.append(line).append(" "); // Gắn nội dung truy vấn
            }
        }

        // Lưu truy vấn cuối cùng
        if (currentKey != null) {
            queries.put(currentKey, currentQuery.toString().trim());
        }
    }

    // Lấy truy vấn SQL theo key
    public static String getQuery(String key) {
        String query = queries.get(key);
        if (query == null) {
            logger.warn("Không tìm thấy truy vấn cho key: {}", key);
        }
        return query;
    }

    // Phương thức kiểm tra nhanh các truy vấn đã được tải
    public static void testQueries() {
        queries.forEach((key, value) -> {
            logger.info("Key: {}\nQuery: {}", key, value);
        });
    }
}