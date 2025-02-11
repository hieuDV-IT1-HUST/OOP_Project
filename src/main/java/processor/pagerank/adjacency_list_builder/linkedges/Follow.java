package processor.pagerank.adjacency_list_builder.linkedges;

import processor.pagerank.adjacency_list_builder.Edge;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static processor.pagerank.adjacency_list_builder.AddOrUpdateEdge.addOrUpdateEdge;
import static processor.pagerank.adjacency_list_builder.ComputeWeight.computeWeight;

public class Follow {
    protected String follower;
    protected String followed;
    protected LocalDateTime followTime;

    public Follow(String follower, String followed) {
        this.follower = follower;
        this.followed = followed;
        this.followTime = LocalDateTime.now();  // Set follow time to current time
    }

    /**
     * Set up follow links: follower --> followed and followed --> follower.
     */
    public void establishFollowLinks(Map<String, List<Edge>> adjacencyList) {
        // follower --> followed
        double fo_weight = computeWeight("User -> User", "FOLLOW+", followTime);
        Edge edge = new Edge(follower, followed, "User -> User", "FOLLOW+");
        addOrUpdateEdge(adjacencyList, edge, fo_weight);

        // followed --> follower
        double rev_fo_weight = computeWeight("User -> User", "FOLLOW-", followTime);
        Edge reverseEdge = new Edge(followed, follower, "User -> User", "FOLLOW-");
        addOrUpdateEdge(adjacencyList, reverseEdge, rev_fo_weight);
    }
}