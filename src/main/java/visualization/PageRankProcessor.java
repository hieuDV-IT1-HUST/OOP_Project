package visualization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.DatabaseConnector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class PageRankProcessor {
    private static final Logger logger = LogManager.getLogger(PageRankProcessor.class);
    private static final String QUERY_USERNAME_BY_USERID = "SELECT username FROM Users WHERE userID = ?";

    /**
     * Resolve a single userID to username.
     *
     * @param userID The userID to resolve.
     * @return The corresponding username, or null if not found.
     */
    private String resolveUsername(int userID) {
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(QUERY_USERNAME_BY_USERID)) {

            preparedStatement.setInt(1, userID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("username");
                }
            }
        } catch (Exception e) {
            logger.error("Error resolving username for userID: {}", userID, e);
        }
        return null; // Return null if no username is found
    }

    /**
     * Process the PageRank points by converting userIDs to usernames and saving the output.
     *
     * @param inputFilePath  Path to the input JSON file containing PageRank points.
     * @param outputFilePath Path to save the processed JSON file.
     */
    public void processPageRankFile(String inputFilePath, String outputFilePath) {
        try {
            // Đọc file JSON đầu vào
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Double> pageRankData = mapper.readValue(new File(inputFilePath), new TypeReference<Map<String, Double>>() {});

            // Xử lý dữ liệu PageRank
            Map<String, Double> processedData = new HashMap<>();
            for (String key : pageRankData.keySet()) {
                if (key.startsWith("U")) {
                    // Chuyển userID thành username
                    int userID = Integer.parseInt(key.substring(1));
                    String username = resolveUsername(userID);
                    if (username != null) {
                        processedData.put(username, pageRankData.get(key)); // Thêm '@' vào username
                    } else {
                        logger.warn("Không tìm thấy username cho userID: {}", userID);
                    }
                } else {
                    // Giữ nguyên các key không phải userID (ví dụ: Tweet ID)
                    processedData.put(key, pageRankData.get(key));
                }
            }

            // Ghi dữ liệu đã xử lý vào file JSON với định dạng đẹp
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputFilePath), processedData);
            logger.info("PageRank file đã được xử lý và lưu vào: {}", outputFilePath);

        } catch (Exception e) {
            logger.error("Lỗi khi xử lý file PageRank: ", e);
        }
    }

    public static void main(String[] args) {
        // File paths
        String inputFilePath = "output/PageRankPoints/pageRankPoints.json";
        String outputFilePath = "output/PageRankPoints/pageRankPointsProcessed.json";

        // Process the PageRank file
        PageRankProcessor processor = new PageRankProcessor();
        processor.processPageRankFile(inputFilePath, outputFilePath);
    }
}