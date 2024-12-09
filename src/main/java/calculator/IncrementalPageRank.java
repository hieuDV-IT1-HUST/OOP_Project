package calculator;

import java.util.*;

import adjacency_list_builder.WeightedEdge;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import adjacency_list_builder.Edge;
import utils.FileUtils;

public class IncrementalPageRank {
    private static final Logger logger = LogManager.getLogger(IncrementalPageRank.class);
    private static final String PAGE_RANK_FILE = "output/PageRankPoints/pageRankPoints.json";
    private static final double DEFAULT_DAMPING_FACTOR = 0.85;
    private static final double CONVERGENCE_THRESHOLD = 0.0001;

    // Current PageRank archive and graph
    private final Map<String, Double> pageRanks = new HashMap<>();
    private Map<String, List<Edge>> adjacencyList = new HashMap<>();
    private Map<String, Double> weights = new HashMap<>();

    /**
     * Load the graph and initialize system.
     */
    public void loadGraph(String inputFilePath) {
        try {
            adjacencyList = FileUtils.readJsonFile(inputFilePath, new TypeReference<>() {});
            weights = normalizeWeights(adjacencyList);
            // Generate PageRank score from JSON file
            pageRanks.putAll(readPageRankFromFile());
            if (pageRanks.isEmpty()) {
                logger.warn("Unable to read initial PageRank score. Will initialize to default.");
                initializePageRanks();
            }
            logger.info("The graph loaded successfully.");
        } catch (Exception e) {
            logger.error("Error loading graph: {}", e.getMessage());
        }
    }

    /**
     * Read PageRank score from JSON file.
     */
    private Map<String, Double> readPageRankFromFile() {
        try {
            return FileUtils.readJsonFile(PAGE_RANK_FILE, new TypeReference<>() {});
        } catch (Exception e) {
            logger.error("Error reading initial PageRank file: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Initialize the PageRank score.
     */
    private void initializePageRanks() {
        Set<String> allNodes = getAllNodes();
        double initialRank = 1.0 / allNodes.size();
        allNodes.forEach(node -> pageRanks.put(node, initialRank));
    }

    /**
     * Get all nodes in the graph.
     */
    private Set<String> getAllNodes() {
        Set<String> allNodes = new HashSet<>(adjacencyList.keySet());
        adjacencyList.values().forEach(edges -> edges.forEach(edge -> allNodes.add(edge.target)));
        return allNodes;
    }

    /**
     * Update PageRank when adding a new edge.
     */
    public void addEdge(String source, String target, double weight, String type, String interactionType) {
        // Get the edge list if it exists, otherwise initialize a new list
        List<Edge> edges = adjacencyList.computeIfAbsent(source, _ -> new ArrayList<>());

        // Check if an edge exists between source and target
        Edge existingEdge = null;
        for (Edge edge : edges) {
            if (edge.source.equals(source) && edge.target.equals(target)) {
                existingEdge = edge;
                break;
            }
        }

        // If edge already exists, update weight
        if (existingEdge != null) {
            if (!existingEdge.interactionType.contains(interactionType) && (existingEdge.type.equals(type))) {
                existingEdge.addInteractionType(interactionType);
                existingEdge.weightedEdge.incrementWeight(weight);
            }
            logger.info("Update edge weights: {} -> {}, new weights: {}", source, target, existingEdge.weightedEdge.weight);
        } else {
            Edge newEdge = new Edge(source, target, type, interactionType, new WeightedEdge());
            newEdge.weightedEdge.incrementWeight(weight);
            edges.add(newEdge);
            logger.info("Add new edge: {} -> {}, weight: {}", source, target, weight);
        }

        weights = normalizeWeights(adjacencyList);

        updatePageRank(source, target);
    }

    /**
     * Update PageRank when there are changes.
     */
    private void updatePageRank(String source, String target) {
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.add(source);
        queue.add(target);
        visited.add(source);
        visited.add(target);

        while (!queue.isEmpty()) {
            String currentNode = queue.poll();
            double newRank = calculateNewRank(currentNode);

            if (Math.abs(pageRanks.getOrDefault(currentNode, 0.0) - newRank) > CONVERGENCE_THRESHOLD) {
                pageRanks.put(currentNode, newRank);

                // Add nodes that directly depend on currentNode to the queue
                adjacencyList.forEach((node, edges) -> {
                    if (edges.stream().anyMatch(edge -> edge.target.equals(currentNode)) && !visited.contains(node)) {
                        queue.add(node);
                        visited.add(node);
                    }
                });

                // Add the nodes that currentNode points to the queue
                List<Edge> outgoingEdges = adjacencyList.getOrDefault(currentNode, Collections.emptyList());
                for (Edge edge : outgoingEdges) {
                    if (!visited.contains(edge.target)) {
                        queue.add(edge.target);
                        visited.add(edge.target);
                    }
                }
            }
        }
    }

    /**
     * Calculate new PageRank score.
     */
    private double calculateNewRank(String node) {
        double rankSum = 0.0;
        double danglingSum = 0.0;

        // Calculate the cumulative part from related nodes
        for (Map.Entry<String, List<Edge>> entry : adjacencyList.entrySet()) {
            String source = entry.getKey();
            for (Edge edge : entry.getValue()) {
                if (edge.target.equals(node)) {
                    String edgeKey = source + "->" + node;
                    rankSum += pageRanks.getOrDefault(source, 0.0) * weights.getOrDefault(edgeKey, 0.0);
                }
            }
        }

        // Calculate the contribution from nodes with no outgoing edges
        for (String n : getAllNodes()) {
            if (!adjacencyList.containsKey(n) || adjacencyList.get(n).isEmpty()) {
                danglingSum += pageRanks.getOrDefault(n, 0.0);
            }
        }

        double danglingContribution = DEFAULT_DAMPING_FACTOR * danglingSum / getAllNodes().size();
        return (1 - DEFAULT_DAMPING_FACTOR) / getAllNodes().size() + danglingContribution + DEFAULT_DAMPING_FACTOR * rankSum;
    }

    /**
     * Normalize edge weights.
     */
    private Map<String, Double> normalizeWeights(Map<String, List<Edge>> adjList) {
        Map<String, Double> normalizedWeights = new HashMap<>();
        adjList.forEach((source, edges) -> {
            double totalWeight = edges.stream().mapToDouble(edge -> edge.weightedEdge.weight).sum();
            for (Edge edge : edges) {
                String edgeKey = source + "->" + edge.target;
                normalizedWeights.put(edgeKey, edge.weightedEdge.weight / totalWeight);
            }
        });
        return normalizedWeights;
    }

    /**
     * In kết quả PageRank.
     */
    public void printPageRanks() {
        pageRanks.forEach((node, rank) -> logger.info("Node: {}, PageRank: {}", node, String.format("%.6f", rank)));
    }

    public static void main(String[] args) {
        IncrementalPageRank incrementalPR = new IncrementalPageRank();
        incrementalPR.loadGraph("output/AdjList/directedSimpleGraph.json");

        // Thêm cạnh mới
        incrementalPR.addEdge("A", "B", 2.0, "", "");
        incrementalPR.addEdge("B", "C", 3.0, "", "");
        incrementalPR.addEdge("C", "D", 1.5, "", "");
        incrementalPR.addEdge("D", "A", 2.5, "", "");

        // In kết quả
        incrementalPR.printPageRanks();
    }
}