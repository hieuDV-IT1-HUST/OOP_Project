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
    private static final double DEFAULT_DAMPING_FACTOR = 0.85;
    private static final double CONVERGENCE_THRESHOLD = 0.0001;

    // Lưu trữ PageRank hiện tại và đồ thị
    private final Map<String, Double> pageRanks = new HashMap<>();
    private Map<String, List<Edge>> adjacencyList = new HashMap<>();
    private Map<String, Double> weights = new HashMap<>();

    /**
     * Tải đồ thị và khởi tạo hệ thống.
     */
    public void loadGraph(String inputFilePath) {
        try {
            adjacencyList = FileUtils.readJsonFile(inputFilePath, new TypeReference<>() {});
            weights = normalizeWeights(adjacencyList);
            initializePageRank();
            logger.info("Đồ thị đã tải thành công.");
        } catch (Exception e) {
            logger.error("Lỗi khi tải đồ thị.", e);
        }
    }

    /**
     * Khởi tạo điểm PageRank ban đầu.
     */
    private void initializePageRank() {
        Set<String> allNodes = getAllNodes();
        double initialRank = 1.0 / allNodes.size();
        allNodes.forEach(node -> pageRanks.put(node, initialRank));
    }

    /**
     * Lấy tất cả các node trong đồ thị.
     */
    private Set<String> getAllNodes() {
        Set<String> allNodes = new HashSet<>(adjacencyList.keySet());
        adjacencyList.values().forEach(edges -> edges.forEach(edge -> allNodes.add(edge.target)));
        return allNodes;
    }

    /**
     * Cập nhật PageRank khi thêm một cạnh mới.
     */
    public void addEdge(String source, String target, double weight, String type, String interactionType) {
        // Lấy danh sách cạnh ra nếu có, nếu không khởi tạo danh sách mới
        List<Edge> edges = adjacencyList.computeIfAbsent(source, _ -> new ArrayList<>());

        // Kiểm tra xem cạnh giữa source và target đã tồn tại chưa
        Edge existingEdge = null;
        for (Edge edge : edges) {
            if (edge.source.equals(source) && edge.target.equals(target)) {
                existingEdge = edge;
                break;
            }
        }

        // Nếu cạnh đã tồn tại, cập nhật trọng số
        if (existingEdge != null) {
            if (!existingEdge.interactionType.contains(interactionType) && (existingEdge.type.equals(type))) {
                existingEdge.addInteractionType(interactionType);
                existingEdge.weightedEdge.incrementWeight(weight);
            }
            logger.info("Cập nhật trọng số cạnh: {} -> {}, trọng số mới: {}", source, target, existingEdge.weightedEdge.weight);
        } else {
            Edge newEdge = new Edge(source, target, type, interactionType, new WeightedEdge());
            newEdge.weightedEdge.incrementWeight(weight);
            edges.add(newEdge);
            logger.info("Thêm cạnh mới: {} -> {}, trọng số: {}", source, target, weight);
        }

        weights = normalizeWeights(adjacencyList);

        updatePageRank(source, target);
    }

    /**
     * Cập nhật PageRank khi có sự thay đổi.
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

                // Thêm các node phụ thuộc trực tiếp vào currentNode vào hàng đợi
                adjacencyList.forEach((node, edges) -> {
                    if (edges.stream().anyMatch(edge -> edge.target.equals(currentNode)) && !visited.contains(node)) {
                        queue.add(node);
                        visited.add(node);
                    }
                });

                // Thêm các node mà currentNode trỏ đến vào hàng đợi
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
     * Tính điểm PageRank mới.
     */
    private double calculateNewRank(String node) {
        double rankSum = 0.0;
        double danglingSum = 0.0;

        // Tính phần cộng dồn từ các node liên quan
        for (Map.Entry<String, List<Edge>> entry : adjacencyList.entrySet()) {
            String source = entry.getKey();
            for (Edge edge : entry.getValue()) {
                if (edge.target.equals(node)) {
                    String edgeKey = source + "->" + node;
                    rankSum += pageRanks.getOrDefault(source, 0.0) * weights.getOrDefault(edgeKey, 0.0);
                }
            }
        }

        // Tính phần đóng góp từ các node không có cạnh đi ra
        for (String n : getAllNodes()) {
            if (!adjacencyList.containsKey(n) || adjacencyList.get(n).isEmpty()) {
                danglingSum += pageRanks.getOrDefault(n, 0.0);
            }
        }

        double danglingContribution = DEFAULT_DAMPING_FACTOR * danglingSum / getAllNodes().size();
        return (1 - DEFAULT_DAMPING_FACTOR) / getAllNodes().size() + danglingContribution + DEFAULT_DAMPING_FACTOR * rankSum;
    }

    /**
     * Chuẩn hóa trọng số các cạnh.
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

    /**
     * Chạy thử nghiệm.
     */
    public static void main(String[] args) {
        IncrementalPageRank incrementalPR = new IncrementalPageRank();
        incrementalPR.loadGraph("path_to_input_file.json");

        // Thêm cạnh mới
        incrementalPR.addEdge("A", "B",2.0, "", "");
        incrementalPR.addEdge("B", "C",3.0, "", "");

        // In kết quả
        incrementalPR.printPageRanks();
    }
}