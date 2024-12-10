package calculator;

import adjacency_list_builder.Edge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public abstract class BasePageRank {

    protected static final double DEFAULT_DAMPING_FACTOR = 0.85;
    protected static final double CONVERGENCE_THRESHOLD = 0.0001;
    protected static final int MAX_ITERATIONS = 100;
    private static final Logger logger = LogManager.getLogger(BasePageRank.class);

    protected Map<String, Double> pageRanks = new HashMap<>();
    protected Map<String, List<Edge>> adjacencyList = new HashMap<>();
    protected Map<String, Double> weights = new HashMap<>();

//    protected BasePageRank() {}

    /**
     * Fetch usernames from the database.
     */
    protected static Map<String, String> fetchUsernames(Connection connection, String query) {
        Map<String, String> userMap = new HashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String userID = rs.getString("userID");
                String username = rs.getString("username");
                userMap.put(userID, username);
            }
            logger.info("Fetched usernames from the database successfully.");
        } catch (Exception e) {
            logger.error("Error fetching usernames: ", e);
        }
        return userMap;
    }

    /**
     * Normalize weights for edges in the adjacency list.
     */
    protected Map<String, Double> normalizeWeights(Map<String, List<Edge>> adjList) {
        Map<String, Double> normalizedWeights = new HashMap<>();

        for (Map.Entry<String, List<Edge>> entry : adjList.entrySet()) {
            String source = entry.getKey();
            double totalWeight = entry.getValue().stream()
                    .mapToDouble(edge -> edge.weightedEdge.weight)
                    .sum();

            for (Edge edge : entry.getValue()) {
                double normalizedWeight = edge.weightedEdge.weight / totalWeight;
                normalizedWeights.put(source + "->" + edge.target, normalizedWeight);
            }
        }
        logger.info("Normalized weights successfully.");
        return normalizedWeights;
    }
    protected Set<String> getAllNodes() {
        Set<String> allNodes = new HashSet<>(adjacencyList.keySet());
        adjacencyList.values().forEach(edges -> edges.forEach(edge -> allNodes.add(edge.target)));
        return allNodes;
    }

    /**
     * Abstract method for computing PageRank, implemented by subclasses.
     */
    protected abstract void computePageRank();

}