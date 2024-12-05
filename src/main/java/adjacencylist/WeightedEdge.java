package adjacencylist;

/**
 * A class represents an edge with a weight and a number of accumulations.
 */
public class WeightedEdge {
    public double weight;
    public int updateCount;

    public WeightedEdge() {
        this.weight = 0;
        this.updateCount = 0;
    }

    public void incrementWeight(double weight) {
        if (updateCount < 10) {
            this.weight += weight;
            this.updateCount++;
        }
    }
}