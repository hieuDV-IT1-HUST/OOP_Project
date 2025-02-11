package processor;

import others.config.AppConfig;
import processor.data.DatabaseInitializer;
import processor.pagerank.adjacency_list_builder.Builder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import others.utils.FileUtils;
import processor.pagerank.adjacency_list_builder.Edge;
import processor.pagerank.calculator.IncrementalPageRank;
import processor.pagerank.calculator.MultiRelationalWeightedPageRank;

import java.util.List;
import java.util.Map;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        // Load configuration from AppConfig
        AppConfig.loadProperties();

        // Step 1: Initialize the database
        try {
            logger.info("Starting database initialization...");
            String schemaFilePath = AppConfig.getInitialize_databasePath(); // Schema file path
            DatabaseInitializer.initializeDatabase(schemaFilePath);
            logger.info("Database initialization completed.");
        } catch (Exception e) {
            logger.error("Error during database initialization: {}", e.getMessage(), e);
            return;
        }

        // Step 2: Import data from JSON files

        // Step 3: Build the adjacency list
        Map<String, List<Edge>> adjacencyList;
        Map<String, List<Edge>> simpleGraphAdjList;
        try {
            logger.info("Starting adjacency list construction...");
            Builder builder = new Builder();
            adjacencyList = builder.generateDSGAdjacencyList();
            simpleGraphAdjList = builder.convertToOwDSGAdjList(adjacencyList);

            // Write adjacency list to JSON file
            String outputFilePath = AppConfig.getDSGAdjListPath();
            String SGraphOutputFilePath = AppConfig.getOwDSGAdjListPath();
            FileUtils.writeJsonToFile(outputFilePath, adjacencyList);
            FileUtils.writeJsonToFile(SGraphOutputFilePath, simpleGraphAdjList);
            logger.info("Adjacency list written to: {}", outputFilePath);
            logger.info("Simple graph adjacency list written to: {}", SGraphOutputFilePath);
        } catch (Exception e) {
            logger.error("Error during adjacency list construction: {}", e.getMessage(), e);
            return;
        }

        // Step 4: Compute PageRank
        try {
            logger.info("Starting PageRank computation...");
            String outputFilePath = AppConfig.getPageRankOutputPath(); // PageRank results path
            IncrementalPageRank pageRankCalculator = new IncrementalPageRank();
            pageRankCalculator.computePageRank();

            MultiRelationalWeightedPageRank MRWPageRankCalculator = new MultiRelationalWeightedPageRank();
            MRWPageRankCalculator.computePageRank();

            logger.info("PageRank results written to: {}", outputFilePath);
        } catch (Exception e) {
            logger.error("Error during PageRank computation: {}", e.getMessage(), e);
        }
    }
}