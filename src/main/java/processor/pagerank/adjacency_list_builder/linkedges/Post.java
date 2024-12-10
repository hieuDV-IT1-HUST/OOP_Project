package processor.pagerank.adjacency_list_builder.linkedges;

import processor.pagerank.adjacency_list_builder.Edge;

import java.util.List;
import java.util.Map;

import static processor.pagerank.adjacency_list_builder.AddOrUpdateEdge.addOrUpdateEdge;
import static processor.pagerank.adjacency_list_builder.ComputeWeight.computeWeight;

public class Post {
    protected String user;
    protected String new_tweet;

    public Post(String username, String newTweetID) {
        this.user = username;
        this.new_tweet = newTweetID;
    }

    /**
     * Set up basic links: User --> Tweet and Tweet --> User.
     */
    public void establishBasicLinks(Map<String, List<Edge>> adjacencyList) {
        // User --> new_Tweet
        double po_weight = computeWeight("User -> Tweet", "POST");
        Edge edge = new Edge(user, new_tweet, "User -> Tweet", "POST+");
        addOrUpdateEdge(adjacencyList, edge, po_weight + 6.0); //7.0

        // new_Tweet --> User
        //////////////////////////////////////// WEIGHT = ???////////////////////////////////////
        double rev_po_weight = 1.0;
        Edge reverseEdge = new Edge(new_tweet, user, "Tweet -> User", "POST-");
        addOrUpdateEdge(adjacencyList, reverseEdge, rev_po_weight + 4.0); // 5.0
    }
}