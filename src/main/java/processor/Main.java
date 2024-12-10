package processor;

import processor.pagerank.calculator.IncrementalPageRank;
import processor.pagerank.calculator.MultiRelationalWeightedPageRank;
import others.config.AppConfig;
import processor.dataprocessing.DatabaseInitializer;
import processor.dataprocessing.DataImporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        // Step 1: Load configuration from AppConfig
        try {
            AppConfig.loadProperties();
            logger.info("Configuration loaded successfully.");
        } catch (Exception e) {
            logger.error("Failed to load configuration: {}", e.getMessage(), e);
            return;
        }

        // Step 2: Initialize the database
        try {
            logger.info("Starting database initialization...");
            String schemaFilePath = AppConfig.getInitialize_databasePath(); // Path to schema file
            DatabaseInitializer.initializeDatabase(schemaFilePath);
            logger.info("Database initialization completed.");
        } catch (Exception e) {
            logger.error("Error initializing database: {}", e.getMessage(), e);
            return;
        }

        // Step 3: Import data from JSON files
        try {
            logger.info("Starting data import...");
            String rootDirectory = "output/Data"; // Directory containing data to import
            DataImporter dataImporter = new DataImporter();
            dataImporter.run(rootDirectory);
            logger.info("Data import completed.");
        } catch (Exception e) {
            logger.error("Error importing data: {}", e.getMessage(), e);
            return;
        }

        // Step 4: Compute PageRank
        try {
            logger.info("Starting PageRank computation...");

            // Initialize IncrementalPageRank
            IncrementalPageRank incrementalPageRank = new IncrementalPageRank();
            incrementalPageRank.computePageRank();
            logger.info("PageRank computation completed.");
        } catch (Exception e) {
            logger.error("Error during PageRank computation: {}", e.getMessage(), e);
        }
    }
}