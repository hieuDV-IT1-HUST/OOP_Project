package data;

import config.AppConfig;
import graph.adjacencyListBuilder.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sql.QueryLoader;
import utils.FileUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class DataTransformer {
    private static final Logger logger = LogManager.getLogger(DataTransformer.class);
    /**
     * A class represents an edge with a weight and a number of accumulations.
     */
    public static class WeightedEdge {
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

    /**
     * The class representing an edge in an adjacency list.
     */
    public static class Edge {
        public String source;
        public String target;
        public String type;
        public String interactionType;
        public WeightedEdge weightedEdge;

        public Edge(String source, String target, String type, String interactionType) {
            this.source = source;
            this.target = target;
            this.type = type;
            this.interactionType = interactionType;
            this.weightedEdge = new WeightedEdge();
        }

        public void addInteractionType(String interactionType) {
            if (!this.interactionType.contains(interactionType)) {
                if (!this.interactionType.isEmpty()) {
                    this.interactionType += ", ";
                }
                this.interactionType += interactionType;
            }
        }

        @Override
        public String toString() {
            return String.format(
                    "Edge { source: '%s', target: '%s', type: '%s', interactionType: %s, weight: %.2f, updates: %d }",
                    source, target, type, interactionType, weightedEdge.weight, weightedEdge.updateCount);
        }
    }

    /**
     * Method to create an adjacency list from data in the database.
     * @return The adjacency list as a Map, where the key is the node and the value is the list of related edges.
     */
    public Map<String, List<Edge>> generateAdjacencyList() {
        Map<String, List<Edge>> adjacencyList = new HashMap<>();

        try (Connection connection = DatabaseConnector.connect()) {

            // Process user_follows
            logger.info("Collecting edge from User_Follows...");
            try (PreparedStatement stmt = connection.prepareStatement(QueryLoader.getQuery("GET_USER_FOLLOWS"));
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String follower = rs.getString("follower");
                    String following = rs.getString("following");
                    Follow follow = new Follow(follower, following);
                    follow.establishFollowLinks(adjacencyList);
                }
            }

            // Process Tweets table
            logger.info("Collecting edge from Tweets...");
            try (PreparedStatement stmt = connection.prepareStatement(QueryLoader.getQuery("GET_USER_TWEETS"));
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String user = rs.getString("username");
                    String tweet = rs.getString("tweetID");
                    Post post = new Post(user, tweet);
                    post.establishBasicLinks(adjacencyList);
                }
            }

            // Process User_Tweets table
            logger.info("Collecting edge from User_Tweets table ...");
            try (PreparedStatement stmt = connection.prepareStatement(QueryLoader.getQuery("GET_TWEET_USER_INTERACTIONS"));
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String tweet = rs.getString("tweetID");
                    String user = rs.getString("username");
                    String interactionType = rs.getString("interactionType");
                    String authorOrMed = rs.getString("authorOrMed");
                    String tweetQRID = rs.getString("tweetQRID"); // new Tweet ID

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

    /**
     * Add or update the weight of an edge in the adjacency list.
     */
    public static void addOrUpdateEdge(Map<String, List<Edge>> adjacencyList, Edge edge, double weight) {
        List<Edge> edges = adjacencyList.computeIfAbsent(edge.source, _ -> new ArrayList<>());

        Optional<Edge> existingEdge = edges.stream()
                .filter(e -> e.source.equals(edge.source)
                        && e.target.equals(edge.target)
                        && e.type.equals(edge.type))
                .findFirst();

        if (existingEdge.isPresent()) {
            Edge existing = existingEdge.get();

            // if exists edge, compute maximum weight of interaction Type can be received,
            // else increase weight and add interactionType to interactionType List.
            if (!existing.interactionType.contains(edge.interactionType)) {
                existing.addInteractionType(edge.interactionType);
                existing.weightedEdge.incrementWeight(weight);
            } else {
                existing.weightedEdge.weight = Math.max(existing.weightedEdge.weight, edge.weightedEdge.weight);
            }
        } else {
            edge.weightedEdge.incrementWeight(weight);
            edges.add(edge);
        }
    }

    public Map<String, List<Edge>> convertToSimpleGraph(Map<String, List<Edge>> adjacencyList) {
        Map<String, List<Edge>> simpleGraph = new HashMap<>();
        Set<String> processedEdges = new HashSet<>(); // Save pairs of edges processed

        // Iterate all edges in multi-graph
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
     * Add edge to graph.
     */
    private void addEdgeToSimpleGraph(Map<String, List<Edge>> simpleGraph, Edge edge) {
        simpleGraph.computeIfAbsent(edge.source, _ -> new ArrayList<>()).add(edge);
    }

    public static void main(String[] args) {
        DataTransformer transformer = new DataTransformer();
        Map<String, List<Edge>> adjacencyList = transformer.generateAdjacencyList();
        Map<String, List<Edge>> simpleGraphAdjList = transformer.convertToSimpleGraph(adjacencyList);

        String outputFilePath = AppConfig.getAdjacencyListPath();
        String SGraphOutputFilePath = AppConfig.getSimpleAdjacencyListPath();

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