package processor.pagerank.adjacency_list_builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AddOrUpdateEdge {
    /**
     * Add or update the weight of an edge in the adjacency list.
     */
    public static void addOrUpdateEdge(Map<String, List<Edge>> adjacencyList, Edge edge, double weight) {
        List<Edge> edges = adjacencyList.computeIfAbsent(edge.source, _ -> new ArrayList<>());

        Optional<Edge> existingEdge = edges.stream()
                .filter(e -> e.source.equals(edge.source)
                        && e.target.equals(edge.target))
                .findFirst();

        if (existingEdge.isPresent()) {
            Edge existing = existingEdge.get();

            // if exists edge, compute maximum weight of interaction Type can be received,
            // else increase weight and add interactionType to interactionType List.
            if (!existing.interactionType.contains(edge.interactionType) && (existing.type.equals(edge.type))) {
                existing.addInteractionType(edge.interactionType);
                existing.weightedEdge.incrementWeight(weight);
            } else {
                existing.weightedEdge.weight = Math.max(existing.weightedEdge.weight, edge.weightedEdge.weight);
            }
        } else {
            edge.weightedEdge.incrementWeight(weight);
            edges.add(edge);
        }
    }
}
