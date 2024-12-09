package calculator;

import adjacency_list_builder.Builder;
import adjacency_list_builder.Edge;
import adjacency_list_builder.WeightedEdge;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.FileUtils;

import java.util.*;

public class IncrementalPageRank {
    private static final Logger logger = LogManager.getLogger(IncrementalPageRank.class);
    private static final String OLD_ADJ_LIST_FILE = "output/AdjList/directedSimpleGraph.json";
    private static final String PAGERANK_POINTS_FILE = "output/PageRankPoints/pageRankPoints.json";
    private static final double DEFAULT_DAMPING_FACTOR = 0.85;
    private static final double CONVERGENCE_THRESHOLD = 0.0001;

    private final Map<String, Double> pageRanks = new HashMap<>();
    private Map<String, List<Edge>> oldAdjacencyList = new HashMap<>();
    private Map<String, List<Edge>> newAdjacencyList = new HashMap<>();
    private Map<String, Double> weights = new HashMap<>();

    /**
     * Load the old graph and initialize PageRank.
     */
    public void loadOldGraph() {
        try {
            oldAdjacencyList = FileUtils.readJsonFile(OLD_ADJ_LIST_FILE, new TypeReference<>() {});
            weights = normalizeWeights(oldAdjacencyList);
            pageRanks.putAll(FileUtils.readJsonFile(PAGERANK_POINTS_FILE, new TypeReference<>() {}));
            logger.info("The old graph loaded successfully.");
        } catch (Exception e) {
            logger.error("Error loading old graph: {}", e.getMessage());
        }
    }

    /**
     * Extract new edges and update PageRank.
     */
    public void processGraphUpdate() {
        Builder builder = new Builder();
        newAdjacencyList = builder.generateDSGAdjacencyList();

        List<Edge> newEdges = extractNewEdges();
        if (newEdges.isEmpty()) {
            logger.info("No new edges detected. Skipping PageRank update.");
            return;
        }

        for (Edge edge : newEdges) {
            addEdge(edge.source, edge.target, edge.weightedEdge.weight, edge.type, edge.interactionType);
        }

        logger.info("PageRank updated successfully.");
    }

    /**
     * Extract edges that are present in the new graph but not in the old graph.
     */
    private List<Edge> extractNewEdges() {
        List<Edge> newEdges = new ArrayList<>();
        for (Map.Entry<String, List<Edge>> entry : newAdjacencyList.entrySet()) {
            String source = entry.getKey();
            List<Edge> newSourceEdges = entry.getValue();
            List<Edge> oldSourceEdges = oldAdjacencyList.getOrDefault(source, Collections.emptyList());

            for (Edge newEdge : newSourceEdges) {
                boolean isNew = oldSourceEdges.stream().noneMatch(
                        oldEdge -> oldEdge.target.equals(newEdge.target) &&
                                oldEdge.type.equals(newEdge.type) &&
                                oldEdge.interactionType.equals(newEdge.interactionType)
                );
                if (isNew) {
                    newEdges.add(newEdge);
                    logger.info("New edge detected: {} -> {}", newEdge.source, newEdge.target);
                }
            }
        }
        return newEdges;
    }

    /**
     * Add new edge and recalculate PageRank incrementally.
     */
    private void addEdge(String source, String target, double weight, String type, String interactionType) {
        List<Edge> edges = oldAdjacencyList.computeIfAbsent(source, _ -> new ArrayList<>());

        Edge existingEdge = edges.stream()
                .filter(edge -> edge.source.equals(source) && edge.target.equals(target))
                .findFirst()
                .orElse(null);

        if (existingEdge != null) {
            if (!existingEdge.interactionType.contains(interactionType)) {
                existingEdge.addInteractionType(interactionType);
                existingEdge.weightedEdge.incrementWeight(weight);
            }
            logger.info("Updated edge: {} -> {}, new weight: {}", source, target, existingEdge.weightedEdge.weight);
        } else {
            Edge newEdge = new Edge(source, target, type, interactionType, new WeightedEdge());
            newEdge.weightedEdge.incrementWeight(weight);
            edges.add(newEdge);
            logger.info("Added new edge: {} -> {}, weight: {}", source, target, weight);
        }

        weights = normalizeWeights(oldAdjacencyList);
        updatePageRank(source, target);
    }

    /**
     * Update PageRank scores for affected nodes.
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

                oldAdjacencyList.forEach((node, edges) -> {
                    if (edges.stream().anyMatch(edge -> edge.target.equals(currentNode)) && !visited.contains(node)) {
                        queue.add(node);
                        visited.add(node);
                    }
                });
            }
        }
    }

    /**
     * Calculate new PageRank score.
     */
    private double calculateNewRank(String node) {
        double rankSum = 0.0;
        double danglingSum = 0.0;

        for (Map.Entry<String, List<Edge>> entry : oldAdjacencyList.entrySet()) {
            String source = entry.getKey();
            for (Edge edge : entry.getValue()) {
                if (edge.target.equals(node)) {
                    rankSum += pageRanks.getOrDefault(source, 0.0) * weights.getOrDefault(source + "->" + node, 0.0);
                }
            }
        }

        for (String n : getAllNodes(oldAdjacencyList)) {
            if (!oldAdjacencyList.containsKey(n) || oldAdjacencyList.get(n).isEmpty()) {
                danglingSum += pageRanks.getOrDefault(n, 0.0);
            }
        }

        double danglingContribution = DEFAULT_DAMPING_FACTOR * danglingSum / getAllNodes(oldAdjacencyList).size();
        return (1 - DEFAULT_DAMPING_FACTOR) / getAllNodes(oldAdjacencyList).size() +
                danglingContribution + DEFAULT_DAMPING_FACTOR * rankSum;
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

    private Set<String> getAllNodes(Map<String, List<Edge>> adjList) {
        Set<String> allNodes = new HashSet<>(adjList.keySet());
        adjList.values().forEach(edges -> edges.forEach(edge -> allNodes.add(edge.target)));
        return allNodes;
    }

    /**
     * In kết quả PageRank.
     */
    public void printPageRanks() {
        pageRanks.forEach((node, rank) -> logger.info("Node: {}, PageRank: {}", node, String.format("%.6f", rank)));
    }

    public static void main(String[] args) {
        IncrementalPageRank incrementalPR = new IncrementalPageRank();
        incrementalPR.loadOldGraph();
        incrementalPR.processGraphUpdate();
        // In kết quả
        incrementalPR.printPageRanks();
    }
}