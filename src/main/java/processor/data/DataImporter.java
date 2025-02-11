package processor.data;

// DataImporter: Import raw data from file (JSON, CSV, etc.) into database.
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import others.config.AppConfig;
import processor.data.node.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import processor.data.node.User_Tweet;
import processor.data.node.Tweet;
import processor.data.services.TweetService;
import processor.data.services.UserService;
import processor.data.services.UserTweetService;
import processor.data.sql.QueryLoader;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DataImporter {
    private static final Logger logger = LogManager.getLogger(DataImporter.class);

    private final UserService userService;
    private final TweetService tweetService;
    private final UserTweetService userTweetService;

    public DataImporter() {
        this.userService = new UserService();
        this.tweetService = new TweetService();
        this.userTweetService = new UserTweetService();
    }

    public void importUsers(Map<String, List<String>> scrapedData) {
        List<User> users = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : scrapedData.entrySet()) {
            String keyword = entry.getKey();
            List<String> usernames = entry.getValue();

            for (String username : usernames) {
                users.add(new User(username, null, 0, 0, null,
                        false, null, null, keyword, null));
            }
        }

        userService.batchInsertUsers(users);
    }
    public void processTweets(JsonNode rootNode) {
        List<Tweet> tweets = new ArrayList<>();
        List<User_Tweet> userTweets = new ArrayList<>();
        List<User> users = new ArrayList<>();

        for (Iterator<Map.Entry<String, JsonNode>> it = rootNode.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> entry = it.next();
            String username = entry.getKey();
            JsonNode tweetsList = entry.getValue();

            // Register user
            users.add(new User(username, null, 0, 0, null,
                    false, null, null, null, null));

            for (JsonNode tweetNode : tweetsList) {
                long tweetID = tweetNode.get("tweetID").asLong();
                String type = tweetNode.get("type").asText();

                String author;
                if (type.equals("POST")) {
                    author = username;
                } else {
                    author = tweetNode.get("originalAuthor").asText();
                    userTweets.add(new User_Tweet(username, tweetID, null,
                            author, "RETWEET", LocalDateTime.now()));
                }
                // Create Tweet record
                Tweet tweet = new Tweet(tweetID, author, "", null, 0, 0,
                        0, 0, null, null, null);
                tweets.add(tweet);

                // Handle Mentions
                if (tweetNode.has("mentions")) {
                    for (JsonNode mention : tweetNode.get("mentions")) {
                        String mentionedUser = mention.asText();
                        users.add(new User(mentionedUser, null, 0, 0, null,
                                false, null, null, null, null));
                        userTweets.add(new User_Tweet(username, tweetID, null,
                                mentionedUser, "MENTION", LocalDateTime.now()));
                    }
                }

                // Handle Quotes
                if (tweetNode.has("quote")) {
                    JsonNode quoteNode = tweetNode.get("quote");

                    // Check fields
                    String quotedAuthor = quoteNode.has("quoteAuthor")
                            ? quoteNode.get("quoteAuthor").asText() : "unknown_author";
                    long quotedTweetID = quoteNode.has("quoteTweetID")
                            ? quoteNode.get("quoteTweetID").asLong() : -1;

                    // Add to list if valid
                    if (!quotedAuthor.equals("unknown_author") && quotedTweetID != -1) {
                        users.add(new User(quotedAuthor, null, 0, 0, null,
                                false, null, null, null, null));
                        tweets.add(new Tweet(quotedTweetID, quotedAuthor, "", null, 0,
                                0, 0, 0, null, null, null));
                        userTweets.add(new User_Tweet(username, quotedTweetID, tweetID,
                                quotedAuthor, "QUOTE", LocalDateTime.now()));
                    }
                }
            }
        }

        userService.batchInsertUsers(users);
        tweetService.batchInsertTweets(tweets);
        userTweetService.batchInsertUserTweets(userTweets);
    }

    /**
     * Insert data into the Hashtags table.
     */
    public static void insertHashtags(String text, int tweetCount) {
        String insertOrUpdateQuery = QueryLoader.getQuery("INSERT_HASHTAGS");
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement ps = connection.prepareStatement(insertOrUpdateQuery)) {

            ps.setString(1, text);
            ps.setInt(2, tweetCount);
            ps.setInt(3, tweetCount);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Inserted/Updated hashtag: {}", text);
            }
        } catch (SQLException e) {
            logger.error("Failed to insert hashtag: {}", text, e);
        }
    }
    /**
     * Insert data into the User_Follows table.
     */
    public static void insertUserFollows(int followerID, int followedID, Object followTime) {
        String insertQuery = QueryLoader.getQuery("INSERT_USER_FOLLOWS");
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement ps = connection.prepareStatement(insertQuery)) {
            ps.setInt(1, followerID);
            ps.setInt(2, followedID);
            ps.setObject(3, followTime);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Inserted User_Follows with followerID: {} and followedID: {}",
                        followerID, followedID);
            }
        } catch (SQLException e) {
            logger.error("Failed to insert User_Follows with followerID: {} and followedID: {}",
                    followerID, followedID, e);
        }
    }
    /**
     * Insert data into the Hashtag_Tweets table.
     */
    public static void insertHashtagTweets(int hashtagID, long tweetID) {
        String insertQuery = QueryLoader.getQuery("INSERT_HASHTAG_TWEETS");
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement ps = connection.prepareStatement(insertQuery)) {
            ps.setInt(1, hashtagID);
            ps.setLong(2, tweetID);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Inserted Hashtag_Tweet with hashtagID: {} and tweetID: {}", hashtagID, tweetID);
            }
        } catch (SQLException e) {
            logger.error("Failed to insert Hashtag_Tweet with hashtagID: {} and tweetID: {}", hashtagID, tweetID, e);
        }
    }
    public static void main(String[] args) {
        AppConfig.loadProperties();
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            DataImporter dataImporter = new DataImporter();
            JsonNode outputNode = objectMapper.readTree(new File("output/data/kol_tweet_ids/kol_tweet_ids_2.json"));
            dataImporter.processTweets(outputNode);
        } catch (IOException e) {
            logger.error("Error reading Json file:{}", e.getMessage());
        }
    }
}