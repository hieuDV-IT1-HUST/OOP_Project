package main;

import config.AppConfig;
import sql.DatabaseInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        // Path to database initialization file
        String initialize_databasePath = AppConfig.getInitialize_databasePath();

        try {
            logger.info("Starting application...");

            // 1. Initialize the database if needed
            logger.info("Initializing database...");
            DatabaseInitializer.initializeDatabase(initialize_databasePath);

            // 2. Continue the main logic of the application
            logger.info("Application is ready to start.");
            runApplicationLogic();

        } catch (Exception e) {
            logger.error("Application failed to start due to an error.", e);
            throw new RuntimeException("Critical error during application startup", e);
        }
    }

    private static void runApplicationLogic() {
        // The main logic of the application
        logger.info("Running application logic...");
        // e.g: DataTransformer, adjListBuilder, v.v.
    }
}
