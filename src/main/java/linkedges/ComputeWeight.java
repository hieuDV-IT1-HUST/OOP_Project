package linkedges;

public class ComputeWeight {
    /**
     * Compute weight for an edge
     * @param type a type of edge considering
     * @param interactionType an interaction between 2 nodes
     * @return weight
     */
    public static double computeWeight(String type, String interactionType) {
        if (type.equals("A")) {
            if (interactionType.equals("abc")) {
                return 1;
            }
        }
        return 1.0;
    }

}
