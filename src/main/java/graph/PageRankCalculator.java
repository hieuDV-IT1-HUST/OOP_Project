package graph;

import java.io.FileReader;
import java.util.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import data.DataTransformer.Edge;
import config.AppConfig;

public class PageRankCalculator {

    private static final Logger logger = LogManager.getLogger(PageRankCalculator.class);
    private static final double DEFAULT_DAMPING_FACTOR = 0.85;
    private static final double CONVERGENCE_THRESHOLD = 0.0001;
    private static final int MAX_ITERATIONS = 100;

    public static void main(String[] args) {
        try {
            String inputFilePath = AppConfig.getSimpleAdjacencyListPath();
            Map<String, List<Edge>> adjacencyList = readAdjacencyListFromJson(inputFilePath);

            logger.info("Read graph from JSON file successfully.");

            // Weight normalization
            Map<String, Double> weights = normalizeWeights(adjacencyList);

            // Calculate PageRank
            Map<String, Double> pageRanks = computePageRank(adjacencyList, weights);

            // Print results
            logger.info("PageRank results:");
            pageRanks.forEach((node, rank) ->
                    logger.info("Node {}: {}", node, String.format("%.6f", rank))
            );
        } catch (Exception e) {
            logger.error("Error computing PageRank: ", e);
        }
    }

    /**
     * Normalize weights for edges in the graph.
     */
    private static Map<String, Double> normalizeWeights(Map<String, List<Edge>> adjacencyList) {
        Map<String, Double> weights = new HashMap<>();

        for (String source : adjacencyList.keySet()) {
            // Calculate the sum of the weights of the edges going from source
            double totalWeight = adjacencyList.get(source)
                    .stream()
                    .mapToDouble(edge -> edge.weightedEdge.weight)
                    .sum();

            // Normalize the weight for each edge
            for (Edge edge : adjacencyList.get(source)) {
                double normalizedWeight = edge.weightedEdge.weight / totalWeight;
                weights.put(source + "->" + edge.target, normalizedWeight);
            }
        }

        logger.info("Normalize weights successfully.");
        return weights;
    }

    /**
     * Compute PageRank using normalized weights.
     */
    private static Map<String, Double> computePageRank(Map<String, List<Edge>> adjacencyList, Map<String, Double> weights) {
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

    /**
     * Read adjacency list from JSON file.
     */
    private static Map<String, List<Edge>> readAdjacencyListFromJson(String filePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new FileReader(filePath), new TypeReference<>() {});
    }
}
