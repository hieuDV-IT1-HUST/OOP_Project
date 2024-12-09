package main;

import calculator.MultiRelationalWeightedPageRank;
import config.AppConfig;
import data.DatabaseInitializer;
import data.DataImporter;
import adjacency_list_builder.Builder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.FileUtils;

import java.util.List;
import java.util.Map;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        // Tải cấu hình từ AppConfig
        AppConfig.loadProperties();

        // Bước 1: Khởi tạo cơ sở dữ liệu
        try {
            logger.info("Bắt đầu khởi tạo cơ sở dữ liệu...");
            String schemaFilePath = AppConfig.getInitialize_databasePath(); // Đường dẫn file schema
            DatabaseInitializer.initializeDatabase(schemaFilePath);
            logger.info("Khởi tạo cơ sở dữ liệu hoàn tất.");
        } catch (Exception e) {
            logger.error("Lỗi khi khởi tạo cơ sở dữ liệu: {}", e.getMessage(), e);
            return;
        }

        // Bước 2: Nhập dữ liệu từ các tệp JSON
        try {
            logger.info("Bắt đầu import dữ liệu...");
            String rootDirectory = "output/Data"; // Thư mục chứa dữ liệu import
            DataImporter dataImporter = new DataImporter();
            dataImporter.run(rootDirectory);
            logger.info("Import dữ liệu hoàn tất.");
        } catch (Exception e) {
            logger.error("Lỗi khi import dữ liệu: {}", e.getMessage(), e);
            return;
        }

        // Bước 3: Xây dựng danh sách kề (Adjacency List)
        Map<String, List<adjacency_list_builder.Edge>> adjacencyList;
        Map<String, List<adjacency_list_builder.Edge>> simpleGraphAdjList;
        try {
            logger.info("Bắt đầu xây dựng danh sách kề...");
            Builder builder = new Builder();
            adjacencyList = builder.generateDSGAdjacencyList();
            simpleGraphAdjList = builder.convertToOwDSGAdjList(adjacencyList);

            // Ghi danh sách kề vào tệp JSON
            String outputFilePath = AppConfig.getDSGAdjListPath();
            String SGraphOutputFilePath = AppConfig.getOwDSGAdjListPath();
            FileUtils.writeJsonToFile(outputFilePath, adjacencyList);
            FileUtils.writeJsonToFile(SGraphOutputFilePath, simpleGraphAdjList);
            logger.info("Danh sách kề đã được ghi vào: {}", outputFilePath);
            logger.info("Danh sách kề đồ thị đơn giản đã được ghi vào: {}", SGraphOutputFilePath);
        } catch (Exception e) {
            logger.error("Lỗi khi xây dựng danh sách kề: {}", e.getMessage(), e);
            return;
        }

        // Bước 4: Tính toán PageRank
        try {
            logger.info("Bắt đầu tính toán PageRank...");
            String inputFilePath = AppConfig.getDSGAdjListPath(); // Đầu vào danh sách kề
            String outputFilePath = AppConfig.getPageRankOutputPath(); // Đường dẫn kết quả PageRank
            Map<String, List<adjacency_list_builder.Edge>> loadedAdjacencyList =
                    FileUtils.readJsonFile(inputFilePath, new com.fasterxml.jackson.core.type.TypeReference<>() {});

            // Gọi phương thức PageRank
            Map<String, Double> weights = MultiRelationalWeightedPageRank.normalizeWeights(loadedAdjacencyList);
            Map<String, Double> pageRanks = MultiRelationalWeightedPageRank.computePageRank(loadedAdjacencyList, weights);

            // Ghi kết quả PageRank vào tệp JSON
            FileUtils.writeJsonToFile(outputFilePath, pageRanks);
            logger.info("PageRank đã được ghi vào: {}", outputFilePath);
        } catch (Exception e) {
            logger.error("Lỗi khi tính toán PageRank: {}", e.getMessage(), e);
        }
    }
}