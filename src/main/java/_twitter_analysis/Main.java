package _twitter_analysis;

import config.AppConfig;
import sql.DatabaseInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        // Đường dẫn tới file khởi tạo database
        String initialize_databasePath = AppConfig.getInitialize_databasePath();

        try {
            logger.info("Starting application...");

            // 1. Khởi tạo cơ sở dữ liệu nếu cần
            logger.info("Initializing database...");
            DatabaseInitializer.initializeDatabase(initialize_databasePath);

            // 2. Tiếp tục các logic chính của ứng dụng
            logger.info("Application is ready to start.");
            runApplicationLogic();

        } catch (Exception e) {
            logger.error("Application failed to start due to an error.", e);
            throw new RuntimeException("Critical error during application startup", e);
        }
    }

    private static void runApplicationLogic() {
        // Các logic chính của ứng dụng
        logger.info("Running application logic...");
        // Ví dụ: DataTransformer, GraphBuilder, v.v.
    }
}
