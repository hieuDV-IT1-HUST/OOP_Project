package processor.pagerank.adjacency_list_builder.linkedges;

import processor.pagerank.adjacency_list_builder.Edge;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static processor.pagerank.adjacency_list_builder.AddOrUpdateEdge.addOrUpdateEdge;
import static processor.pagerank.adjacency_list_builder.ComputeWeight.computeWeight;

public class Post {
    protected String user;
    protected String newTweetID;
    protected LocalDateTime createdAt;

    /**
     * Constructor for Post with creation time
     * @param username the user who posted the tweet
     * @param newTweetID the ID of the new tweet
     * @param createdAt the creation time of the tweet
     */
    public Post(String username, String newTweetID, LocalDateTime createdAt) {
        this.user = username;
        this.newTweetID = newTweetID;
        this.createdAt = createdAt;
    }

    /**
     * Set up basic links: User --> Tweet and Tweet --> User.
     */
    public void establishBasicLinks(Map<String, List<Edge>> adjacencyList) {
        // User --> newTweet
        double poWeight = computeWeight("User -> Tweet", "POST+", createdAt);
        Edge edge = new Edge(user, newTweetID, "User -> Tweet", "POST+");
        addOrUpdateEdge(adjacencyList, edge, poWeight); // Dynamic weight based on time decay

        // newTweet --> User
        double revPoWeight = computeWeight("Tweet -> User", "POST-", createdAt);
        Edge reverseEdge = new Edge(newTweetID, user, "Tweet -> User", "POST-");
        addOrUpdateEdge(adjacencyList, reverseEdge, revPoWeight); // Dynamic weight based on time decay
    }
}