package processor.pagerank.adjacency_list_builder.linkedges;

import java.util.*;
import processor.pagerank.adjacency_list_builder.Edge;

import static processor.pagerank.adjacency_list_builder.AddOrUpdateEdge.addOrUpdateEdge;
import static processor.pagerank.adjacency_list_builder.ComputeWeight.computeWeight;

public class ReplyQuote extends Post {
    private final String originalTweetID;
    private final String userTweetedTweet;

    public ReplyQuote(String username, String newTweetID, String originalTweetID, String userTweetedTweet) {
        super(username, newTweetID);
        this.originalTweetID = originalTweetID;
        this.userTweetedTweet = userTweetedTweet;
    }

    /**
     * Set up links related to REPLY, QUOTE type.
     */
//  REPLY: User -(S)-> new_Tweet, new_Tweet -(S)-> User, new_Tweet -(SS)-> Tweet, Tweet -(N)-> new_Tweet,
//  User -> User_tweeted_tweet, User -> Tweet, Tweet -> User, new_Tweet -> User_tweeted_tweet.
//  QUOTE == REPLY but differ weight.
    public void establishReplyQuoteLinks(Map<String, List<Edge>> adjacencyList, String interactionType) {
        // Basic links from Post class
        establishBasicLinks(adjacencyList);

        // new_Tweet --> original Tweet
        double tt_weight = computeWeight("Tweet -> Tweet", interactionType);
        Edge tt_edge = new Edge(new_tweet, originalTweetID, "Tweet -> Tweet", interactionType + "+");
        addOrUpdateEdge(adjacencyList, tt_edge, tt_weight + 7); // 8.0

        // original Tweet --> new_Tweet
        double rev_weight = 1.0;
        Edge reverseEdge = new Edge(originalTweetID, new_tweet, "Tweet -> Tweet", interactionType + "-");
        addOrUpdateEdge(adjacencyList, reverseEdge, rev_weight + 3); // 4.0

        // User --> User_tweeted_original
        double uu_weight = computeWeight("User -> User", interactionType);
        Edge uu_edge = new Edge(user, userTweetedTweet, "User -> User", interactionType + "+");
        addOrUpdateEdge(adjacencyList, uu_edge, uu_weight + 4); // 5.0

        // User --> original Tweet
        double ut_weight = computeWeight("User -> Tweet", interactionType);
        Edge ut_edge = new Edge(user, originalTweetID, "User -> Tweet", interactionType + "+");
        addOrUpdateEdge(adjacencyList, ut_edge, ut_weight + 5);

        // original Tweet --> User
        double rev_ut_weight = 1.0;
        Edge rev_ut_edge = new Edge(originalTweetID, user, "Tweet -> User", interactionType + "-");
        addOrUpdateEdge(adjacencyList, rev_ut_edge, rev_ut_weight + 1); // 2.0

        // new_Tweet --> User_tweeted_original
        double ntu_weight = computeWeight("Tweet -> User", interactionType);
        Edge ntu_edge = new Edge(new_tweet, userTweetedTweet, "Tweet -> User", interactionType + "+");
        addOrUpdateEdge(adjacencyList, ntu_edge, ntu_weight + 5); // 6.0
    }
}