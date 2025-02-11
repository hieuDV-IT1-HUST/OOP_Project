package processor.pagerank.adjacency_list_builder;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import others.config.CustomDoubleSerializer;

/**
 * A class represents an edge with a weight and a number of accumulations.
 */
public class WeightedEdge {
    @JsonSerialize(using = CustomDoubleSerializer.class)
    public double weight;

    public int updateCount;

    public WeightedEdge() {
        this.weight = 0;
        this.updateCount = 0;
    }

    public void incrementWeight(double weight) {
        this.weight += weight;
        this.updateCount++;
    }
}