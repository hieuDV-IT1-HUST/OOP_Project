package graph.adjacencylistbuilder;

import data.DataTransformer.Edge;

import java.util.List;
import java.util.Map;

import static data.DataTransformer.addOrUpdateEdge;
import static data.DataTransformer.computeWeight;

public class Retweet {
    protected String user;
    protected String tweet;
    protected String userTweetedTweet;

    public Retweet(String username, String TweetID, String userTweetedTweet) {
        this.user = username;
        this.tweet = TweetID;
        this.userTweetedTweet = userTweetedTweet;
    }

    /**
     * Thiết lập các liên kết: User -(S)-> Tweet, Tweet -(W)-> User, User -(S)-> User_tweeted_tweet.
     */
    public void establishRetweetsLinks(Map<String, List<Edge>> adjacencyList) {
        // User --> Tweet
        double ret_weight = computeWeight("User -> Tweet", "RETWEET");
        Edge edge = new Edge(user, tweet, "User -> Tweet", "RETWEET");
        addOrUpdateEdge(adjacencyList, edge, ret_weight + 2); // 4.0

        // Tweet --> User
        //////////////////////////////////////// WEIGHT = ???////////////////////////////////////
        double rev_ret_weight = 1.0;
        Edge reverseEdge = new Edge(tweet, user, "Tweet -> User", "RETWEET");
        addOrUpdateEdge(adjacencyList, reverseEdge, rev_ret_weight); // 1.0

        // User -> User tweeted tweet
        double uut_weight = computeWeight("User -> User", "RETWEET");
        Edge uut_edge = new Edge(user, userTweetedTweet, "User -> User", "RETWEET");
        addOrUpdateEdge(adjacencyList, uut_edge, uut_weight + 4); // 5.0
    }
}