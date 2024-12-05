package linkedges;

import adjacencylist.Edge;

import java.util.List;
import java.util.Map;

import static linkedges.AddOrUpdateEdge.addOrUpdateEdge;
import static linkedges.ComputeWeight.computeWeight;

public class Mention extends Post {
    protected String mentionedUser;

    public Mention(String username, String newTweetID, String mentionedUser) {
        super(username, newTweetID);
        this.mentionedUser = mentionedUser;
    }

    /**
     * Set up mention links: new_Tweet --> User, User --> new_Tweet, new_Tweet --> medUser.
     */

    public void establishMentionsLinks(Map<String, List<Edge>> adjacencyList) {
        establishBasicLinks(adjacencyList);

        // newTweet -> mentionedUser
        double ntu_weight = computeWeight("Tweet -> User", "MENTION");
        Edge ntu_edge = new Edge(new_tweet, mentionedUser, "Tweet -> User", "MENTION");
        addOrUpdateEdge(adjacencyList, ntu_edge, ntu_weight + 4.0); // 5.0
    }
}