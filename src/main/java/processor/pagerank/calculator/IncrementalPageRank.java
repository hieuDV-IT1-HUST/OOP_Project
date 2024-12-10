package processor.pagerank.calculator;

import processor.pagerank.adjacency_list_builder.Builder;
import processor.pagerank.adjacency_list_builder.Edge;
import com.fasterxml.jackson.core.type.TypeReference;
import others.config.AppConfig;
import processor.dataprocessing.DatabaseConnector;
import processor.dataprocessing.sql.QueryLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import others.utils.FileUtils;

import java.io.IOException;
import java.sql.Connection;
import java.util.*;

import static processor.pagerank.adjacency_list_builder.AddOrUpdateEdge.addOrUpdateEdge;

/**
 * Incremental PageRank computation with graph updates.
 */
public class IncrementalPageRank extends BasePageRank {

    private static final Logger logger = LogManager.getLogger(IncrementalPageRank.class);
    private final Map<String, List<Edge>> newAdjacencyList = new HashMap<>();

//    public IncrementalPageRank() {}

    @Override
    public void computePageRank() {
        try (Connection connection = DatabaseConnector.connect()) {
            AppConfig.loadProperties();
            String outputFilePath = AppConfig.getIncrementalPageRankOutputPath();
            loadOldGraph();
            processGraphUpdate();
            Map<String, String> userMap = fetchUsernames(connection, QueryLoader.getQuery("GET_ALL_USERS"));
            Map<String, String> formattedPageRanks = new HashMap<>();
            pageRanks.forEach((ID, score) -> {
                String displayKey = ID.startsWith("U")
                        ? userMap.getOrDefault(ID.substring(1), ID)
                        : ID;
                formattedPageRanks.put(displayKey, String.format("%.7f", score));
            });
            // Write PageRank to file
            FileUtils.writeJsonToFile(outputFilePath, formattedPageRanks);
        } catch (IOException ioe){
            logger.warn("No such graph found.");
        }
        catch (Exception e) {
            logger.error("Error computing Incremental PageRank: ", e);
        }
    }

    /**
     * Load the old graph from files.
     */
    private void loadOldGraph() throws IOException {
        AppConfig.loadProperties();
        adjacencyList = FileUtils.readJsonFile(AppConfig.getDSGAdjListPath(), new TypeReference<>() {
        });
        weights = normalizeWeights(adjacencyList);
        pageRanks.putAll(FileUtils.readJsonFile(AppConfig.getPageRankOutputPath(), new TypeReference<>() {
        }));
        logger.info("Old graph loaded successfully.");
    }

    /**
     * Process updates in the graph and adjust PageRank scores.
     */
    private void processGraphUpdate() {
        newAdjacencyList.putAll(new Builder().generateDSGAdjacencyList());
        List<Edge> newEdges = extractNewEdges();

        if (newEdges.isEmpty()) {
            logger.info("No new edges detected. Skipping PageRank update.");
            return;
        }

        int oldEdgeCount = countTotalEdges(adjacencyList);
        int newEdgeCount = newEdges.size();
        double edgeRatio = (double) newEdgeCount / oldEdgeCount;

        if (edgeRatio < 0.1) {
            logger.info("New edges detected: {} ({}% of the current graph). Proceeding with incremental update.",
                    newEdgeCount, String.format("%.2f", edgeRatio * 100));

            for (Edge edge : newEdges) {
                addOrUpdateEdge(adjacencyList, edge, edge.weightedEdge.weight);
                weights = normalizeWeights(adjacencyList);
                updatePageRank(edge.source, edge.target);
            }
            logger.info("PageRank successfully updated using incremental approach.");
            isComputed = true;
        } else {
            logger.warn("Too many new edges detected: {} ({}% of the current graph). Switching to full PageRank computation.",
                    newEdgeCount, String.format("%.2f", edgeRatio * 100));
            FileUtils.writeJsonToFile(AppConfig.getDSGAdjListPath(), newAdjacencyList);
        }
    }

    /**
     * Count the total number of edges in the current graph.
     */
    private int countTotalEdges(Map<String, List<Edge>> graph) {
        return graph.values().stream().mapToInt(List::size).sum();
    }

    /**
     * Extract edges that are new in the updated graph.
     */
    private List<Edge> extractNewEdges() {
        List<Edge> newEdges = new ArrayList<>();
        for (String source : newAdjacencyList.keySet()) {
            List<Edge> edges = newAdjacencyList.get(source);
            List<Edge> oldEdges = adjacencyList.getOrDefault(source, Collections.emptyList());

            edges.stream()
                    .filter(edge -> oldEdges.stream().noneMatch(
                            oldEdge -> oldEdge.target.equals(edge.target)
                                    && oldEdge.type.equals(edge.type)
                                    && oldEdge.interactionType.equals(edge.interactionType)
                    ))
                    .forEach(newEdges::add);
        }
        return newEdges;
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

                adjacencyList.forEach((node, edges) -> {
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

        for (Map.Entry<String, List<Edge>> entry : adjacencyList.entrySet()) {
            String source = entry.getKey();
            for (Edge edge : entry.getValue()) {
                if (edge.target.equals(node)) {
                    rankSum += pageRanks.getOrDefault(source, 0.0) * weights.getOrDefault(source + "->" + node, 0.0);
                }
            }
        }

        for (String n : getAllNodes()) {
            if (!adjacencyList.containsKey(n) || adjacencyList.get(n).isEmpty()) {
                danglingSum += pageRanks.getOrDefault(n, 0.0);
            }
        }

        double danglingContribution = DEFAULT_DAMPING_FACTOR * danglingSum / getAllNodes().size();
        return (1 - DEFAULT_DAMPING_FACTOR) / getAllNodes().size() +
                danglingContribution + DEFAULT_DAMPING_FACTOR * rankSum;
    }

    /**
     * Main method for testing Incremental PageRank.
     */
    public static void main(String[] args) {
        IncrementalPageRank pageRankCalculator = new IncrementalPageRank();
        pageRankCalculator.computePageRank();
    }
}