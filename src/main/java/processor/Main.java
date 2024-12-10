package processor;

import others.utils.FileUtils;
import processor.pagerank.adjacency_list_builder.Builder;
import processor.pagerank.adjacency_list_builder.Edge;
import processor.pagerank.calculator.IncrementalPageRank;
import others.config.AppConfig;
import processor.dataprocessing.DatabaseInitializer;
import processor.dataprocessing.DataImporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import processor.pagerank.calculator.MultiRelationalWeightedPageRank;

import java.util.List;
import java.util.Map;

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

        // Step 3: Building Adjacency List
        Map<String, List<Edge>> adjacencyList;
        Map<String, List<Edge>> simpleGraphAdjList;
        try {
            logger.info("Start building Adjacency List...");
            Builder builder = new Builder();
            adjacencyList = builder.generateDSGAdjacencyList();
            simpleGraphAdjList = builder.convertToOwDSGAdjList(adjacencyList);

            // Ghi danh sách kề vào tệp JSON
            String outputFilePath = AppConfig.getDSGAdjListPath();
            String SGraphOutputFilePath = AppConfig.getOwDSGAdjListPath();
            FileUtils.writeJsonToFile(outputFilePath, adjacencyList);
            FileUtils.writeJsonToFile(SGraphOutputFilePath, simpleGraphAdjList);
            logger.info("Adjacency List has been saved in: {}", outputFilePath);
            logger.info("Simple Adjacency List has been saved in: {}", SGraphOutputFilePath);
        } catch (Exception e) {
            logger.error("Error when building Adjacency List: {}", e.getMessage(), e);
            return;
        }

        // Step 4: Import data from JSON files
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

        // Step 5: Compute PageRank
        try {
            logger.info("Starting PageRank computation...");

            // Initialize IncrementalPageRank
            IncrementalPageRank incrementalPageRank = new IncrementalPageRank();
            incrementalPageRank.computePageRank();

            MultiRelationalWeightedPageRank multiPageRank = new MultiRelationalWeightedPageRank();
            multiPageRank.computePageRank();
            logger.info("PageRank computation completed.");
        } catch (Exception e) {
            logger.error("Error during PageRank computation: {}", e.getMessage(), e);
        }
    }
}