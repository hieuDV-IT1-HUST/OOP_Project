package adjacency_list_builder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The class representing an edge in an adjacency list.
 */
public class Edge {
    public String source;
    public String target;
    public String type;
    public String interactionType;
    public WeightedEdge weightedEdge;

    // Constructor có annotation để hỗ trợ deserialization
    @JsonCreator
    public Edge(
            @JsonProperty("source") String source,
            @JsonProperty("target") String target,
            @JsonProperty("type") String type,
            @JsonProperty("interactionType") String interactionType,
            @JsonProperty("weightedEdge") WeightedEdge weightedEdge
    ) {
        this.source = source;
        this.target = target;
        this.type = type;
        this.interactionType = interactionType;
        this.weightedEdge = weightedEdge != null ? weightedEdge : new WeightedEdge();
        this.weightedEdge.updateCount++;
    }

    public Edge(String source, String target, String type, String interactionType) {
        this.source = source;
        this.target = target;
        this.type = type;
        this.interactionType = interactionType;
        this.weightedEdge = new WeightedEdge();
    }

    public void addInteractionType(String interactionType) {
        if (!this.interactionType.contains(interactionType)) {
            if (!this.interactionType.isEmpty()) {
                this.interactionType += ", ";
            }
            this.interactionType += interactionType;
        }
    }

    @Override
    public String toString() {
        return String.format(
                "Edge { source: '%s', target: '%s', type: '%s', interactionType: %s, weight: %.2f, updates: %d }",
                source, target, type, interactionType, weightedEdge.weight, weightedEdge.updateCount);
    }
}