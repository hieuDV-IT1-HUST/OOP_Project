package processor.pagerank.adjacency_list_builder.linkedges;

import processor.pagerank.adjacency_list_builder.Edge;
import java.util.List;
import java.util.Map;

import static processor.pagerank.adjacency_list_builder.AddOrUpdateEdge.addOrUpdateEdge;
import static processor.pagerank.adjacency_list_builder.ComputeWeight.computeWeight;

public class Follow {
    protected String follower;
    protected String following;

    public Follow(String follower, String following) {
        this.follower = follower;
        this.following = following;
    }

    /**
     * Set up follow links: follower --> following and following --> follower.
     */
    public void establishFollowLinks(Map<String, List<Edge>> adjacencyList) {
        // follower --> following
        double fo_weight = computeWeight("User -> User", "FOLLOW");
        Edge edge = new Edge(follower, following, "User -> User", "FOLLOW+");
        addOrUpdateEdge(adjacencyList, edge, fo_weight + 6); // 7.0

        // following --> follower
        //////////////////////////////// WEIGHT = ???/////////////////////////////////////
        double rev_fo_weight = 1.0;
        Edge reverseEdge = new Edge(following, follower, "User -> User", "FOLLOW-");
        addOrUpdateEdge(adjacencyList, reverseEdge, rev_fo_weight + 1); // 2.0
    }
}