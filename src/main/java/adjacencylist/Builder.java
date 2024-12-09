package adjacencylist;

import data.DatabaseConnector;
import linkedges.*;
import config.AppConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import data.sql.QueryLoader;
import utils.FileUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class Builder {
    private static final Logger logger = LogManager.getLogger(Builder.class);
    /**
     * Method to create an adjacency list from data in the database.
     * @return The adjacency list as a Map, where the key is the node and the value is the list of related edges.
     */
    public Map<String, List<Edge>> generateDSGAdjacencyList() {
        Map<String, List<Edge>> adjacencyList = new HashMap<>();

        try (Connection connection = DatabaseConnector.connect()) {

            // Process user_follows
            logger.info("Collecting edge from User_Follows...");
            try (PreparedStatement stmt = connection.prepareStatement(QueryLoader.getQuery("GET_USER_FOLLOWS"));
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String follower = 'U' + rs.getString("follower");
                    String followed = 'U' + rs.getString("followed");
                    Follow follow = new Follow(follower, followed);
                    follow.establishFollowLinks(adjacencyList);
                }
            }

            // Process Tweets table
            logger.info("Collecting edge from Tweets...");
            try (PreparedStatement stmt = connection.prepareStatement(QueryLoader.getQuery("GET_USER_TWEETS"));
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String user = 'U' + rs.getString("userID");
                    String tweet = 'T' + rs.getString("tweetID");
                    Post post = new Post(user, tweet);
                    post.establishBasicLinks(adjacencyList);
                }
            }

            // Process User_Tweets table
            logger.info("Collecting edge from User_Tweets table ...");
            try (PreparedStatement stmt = connection.prepareStatement(QueryLoader.getQuery("GET_TWEET_USER_INTERACTIONS"));
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String tweet = 'T' + rs.getString("tweetID");
                    String user = 'U' + rs.getString("userID");
                    String interactionType = rs.getString("interactionType");
                    String authorOrMed = 'U' + rs.getString("authorOrMentionedID");
                    String tweetQRID = 'T' + rs.getString("tweetQuoteReplyID"); // new Tweet ID

                    // Handle additional edges for interactions
                    switch (interactionType) {
                        case "RETWEET" -> {
                            Retweet retweet = new Retweet(user, tweet, authorOrMed);
                            retweet.establishRetweetsLinks(adjacencyList);
                        }
                        case "REPLY", "QUOTE" -> {
                            ReplyQuote replyQuote = new ReplyQuote(user, tweetQRID, tweet, authorOrMed);
                            replyQuote.establishReplyQuoteLinks(adjacencyList, interactionType);
                        }
                        case "MENTION" -> {
                            Mention mention = new Mention(user, tweet, authorOrMed);
                            mention.establishMentionsLinks(adjacencyList);
                        }
                    }
                }
            }

            logger.info("Finish adjacency list collecting.");

        } catch (Exception e) {
            logger.error("Error generating adjacency list ", e);
            throw new RuntimeException("Error generating adjacency list: ", e);
        }

        return adjacencyList;
    }

    public Map<String, List<Edge>> convertToOwDSGAdjList(Map<String, List<Edge>> adjacencyList) {
        Map<String, List<Edge>> simpleGraph = new HashMap<>();
        Set<String> processedEdges = new HashSet<>(); // Save pairs of edges processed

        // Iterate all edges in multi-calculator
        for (String source : adjacencyList.keySet()) {
            List<Edge> edges = adjacencyList.get(source);

            for (Edge edge : edges) {
                String target = edge.target;
                String edgeKey = source + "->" + target;
                String reverseEdgeKey = target + "->" + source;

                // Checking whether a pair of edge is processed or not
                if ((!processedEdges.contains(edgeKey)) && (!processedEdges.contains(reverseEdgeKey))) {
                    // Mark
                    processedEdges.add(edgeKey);
                    processedEdges.add(reverseEdgeKey);

                    double avgWeightSourceToTarget = edge.weightedEdge.weight / edge.weightedEdge.updateCount;

                    // Find symmetric edge (target -> source)
                    Optional<Edge> reverseEdgeOpt = adjacencyList.getOrDefault(target, Collections.emptyList())
                            .stream()
                            .filter(e -> e.target.equals(source))
                            .findFirst();

                    double avgWeightTargetToSource = reverseEdgeOpt
                            .map(e -> e.weightedEdge.weight / e.weightedEdge.updateCount)
                            .orElse(0.0);

                    // Calculate the average weighted difference
                    double weightDifference = avgWeightSourceToTarget - avgWeightTargetToSource;

                    if (weightDifference > 0) {
                        // Keep the source -> target edge
                        Edge newEdge = new Edge(source, target, "Simple Edge", "General");
                        newEdge.weightedEdge.updateCount++;
                        newEdge.weightedEdge.weight = Math.abs(weightDifference);
                        addEdgeToSimpleGraph(simpleGraph, newEdge);
                    } else if (weightDifference < 0) {
                        // Keep the target -> source edge
                        Edge newEdge = new Edge(target, source, "Simple Edge", "General");
                        newEdge.weightedEdge.weight = Math.abs(weightDifference);
                        newEdge.weightedEdge.updateCount++;
                        addEdgeToSimpleGraph(simpleGraph, newEdge);
                    }
                }

            }
        }

        return simpleGraph;
    }

    /**
     * Add edge to simple calculator.
     */
    private void addEdgeToSimpleGraph(Map<String, List<Edge>> simpleGraph, Edge edge) {
        simpleGraph.computeIfAbsent(edge.source, _ -> new ArrayList<>()).add(edge);
    }

    public static void main(String[] args) {
        Builder transformer = new Builder();
        Map<String, List<Edge>> adjacencyList = transformer.generateDSGAdjacencyList();
        Map<String, List<Edge>> simpleGraphAdjList = transformer.convertToOwDSGAdjList(adjacencyList);

        String outputFilePath = AppConfig.getDSGAdjListPath();
        String SGraphOutputFilePath = AppConfig.getOwDSGAdjListPath();

        try {
            FileUtils.writeJsonToFile(outputFilePath, adjacencyList);
            FileUtils.writeJsonToFile(SGraphOutputFilePath, simpleGraphAdjList);
            logger.info("Adjacency List is written to: {}", outputFilePath);
            logger.info("Simple Graph Adjacency List is written to: {}", SGraphOutputFilePath);
        } catch (RuntimeException e) {
            logger.error("Error writing adjacency list to file.", e);
        }
    }
}