package calculator;

import adjacency_list_builder.Edge;
import com.fasterxml.jackson.core.type.TypeReference;
import config.AppConfig;
import data.DatabaseConnector;
import data.sql.QueryLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.FileUtils;

import java.sql.Connection;
import java.util.*;

/**
 * MultiRelationalWeightedPageRanks computes PageRank for a multi-relational weighted graph.
 */
public class MultiRelationalWeightedPageRank extends BasePageRank {

    private static final Logger logger = LogManager.getLogger(MultiRelationalWeightedPageRank.class);

//    public MultiRelationalWeightedPageRank() {}

    @Override
    protected void computePageRank() {
        // boolean displayUsername = true;
        try (Connection connection = DatabaseConnector.connect()) {
            AppConfig.loadProperties();

            // Load adjacency list and output file path
            String inputFilePath = AppConfig.getDSGAdjListPath();
            String outputFilePath = AppConfig.getPageRankOutputPath();
            adjacencyList = FileUtils.readJsonFile(inputFilePath, new TypeReference<>() {});

            logger.info("Read adjacency list from JSON file successfully.");

            // Normalize edge weights and calculate PageRank
            weights = normalizeWeights(adjacencyList);
            pageRanks = computePageRank(adjacencyList, weights);

            // Fetch usernames from database
            Map<String, String> userMap = fetchUsernames(connection, QueryLoader.getQuery("GET_ALL_USERS"));

            // Format results for output
            Map<String, String> formattedPageRanks = new HashMap<>();
            pageRanks.forEach((ID, score) -> {
                String displayKey = ID.startsWith("U") //&& displayUsername
                        ? userMap.getOrDefault(ID.substring(1), ID)
                        : ID;
                formattedPageRanks.put(displayKey, String.format("%.6f", score));
            });

            // Write PageRank results to file
            FileUtils.writeJsonToFile(outputFilePath, formattedPageRanks);
            logger.info("PageRank computation completed and results written to file.");
        } catch (Exception e) {
            logger.error("Error computing PageRank: ", e);
        }
    }

    /**
     * Compute PageRank using normalized weights.
     */
    public static Map<String, Double> computePageRank(Map<String, List<Edge>> adjacencyList, Map<String, Double> weights) {
        Set<String> allNodes = new HashSet<>(adjacencyList.keySet());
        adjacencyList.values().forEach(edges ->
                edges.forEach(edge -> allNodes.add(edge.target))
        );

        // Initialize PageRank for all nodes
        double initialRank = 1.0 / allNodes.size();
        Map<String, Double> pageRanks = new HashMap<>();
        allNodes.forEach(node -> pageRanks.put(node, initialRank));

        Map<String, Double> prevRanks = new HashMap<>();

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            prevRanks.putAll(pageRanks);

            double danglingSum = 0.0;

            // Sum the PageRank of nodes with no outgoing edges
            for (String node : allNodes) {
                if (!adjacencyList.containsKey(node) || adjacencyList.get(node).isEmpty()) {
                    danglingSum += prevRanks.getOrDefault(node, 0.0);
                }
            }

            // Distribute new PageRank to each node
            for (String node : allNodes) {
                double rankSum = 0.0;

                // Calculate the sum of the PageRank weights of the nodes pointing to the current node
                for (String otherNode : adjacencyList.keySet()) {
                    List<Edge> outgoingEdges = adjacencyList.get(otherNode);
                    if (outgoingEdges != null) {
                        for (Edge edge : outgoingEdges) {
                            if (edge.target.equals(node)) {
                                String edgeKey = otherNode + "->" + node;
                                rankSum += prevRanks.getOrDefault(otherNode, 0.0) * weights.getOrDefault(edgeKey, 0.0);
                            }
                        }
                    }
                }

                // Add the part from the nodes with no outgoing edges
                double danglingContribution = DEFAULT_DAMPING_FACTOR * danglingSum / allNodes.size();

                // Calculate new PageRank value
                pageRanks.put(node,
                        (1 - DEFAULT_DAMPING_FACTOR) / allNodes.size() +
                                danglingContribution +
                                DEFAULT_DAMPING_FACTOR * rankSum
                );
            }

            // Convergence check
            if (hasConverged(pageRanks, prevRanks)) {
                logger.info("Converged after {} time(s).", iteration + 1);
                break;
            }
        }

        return pageRanks;
    }

    /**
     * Check convergence between two iterations.
     */
    private static boolean hasConverged(Map<String, Double> current, Map<String, Double> previous) {
        for (String node : current.keySet()) {
            if (Math.abs(current.get(node) - previous.get(node)) > CONVERGENCE_THRESHOLD) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        MultiRelationalWeightedPageRank pageRankCalculator = new MultiRelationalWeightedPageRank();
        pageRankCalculator.computePageRank();
    }
}