//package data;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import sql.QueryLoader;
//
//import java.io.File;
//import java.io.IOException;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import java.util.List;
//import java.util.Map;
//
//public class DataImporter {
//    private static final Logger logger = LogManager.getLogger(DataImporter.class);
//
//    public static void importData(String rootDirectory) {
//        File rootDir = new File(rootDirectory);
//        if (!rootDir.exists() || !rootDir.isDirectory()) {
//            logger.error("Root directory does not exist or is not a directory: {}", rootDirectory);
//            return;
//        }
//
//        // Loop through user directories in the root directory
//        for (File userDir : rootDir.listFiles(File::isDirectory)) {
//            logger.info("Processing user directory: {}", userDir.getName());
//
//            File followersFile = new File(userDir, "followers.json");
//            File followingFile = new File(userDir, "following.json");
//            File retweetDataFile = new File(userDir, "retweetData.json");
//
//            if (followersFile.exists()) {
//                importFollowers(followersFile);
//            }
//            if (followingFile.exists()) {
//                importFollowing(followingFile);
//            }
//            if (retweetDataFile.exists()) {
//                importRetweetData(retweetDataFile);
//            }
//        }
//    }
//
//    private static void importFollowers(File file) {
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            Map<String, Object> data = objectMapper.readValue(file, new TypeReference<>() {});
//
//            String username = (String) data.get("username");
//            @SuppressWarnings("unchecked")
//            List<String> userIDs = (List<String>) data.get("userIDs");
//
//            int userID = getUserID(username);
//            if (userID == -1) {
//                logger.error("User not found for username: {}", username);
//                return;
//            }
//
//            for (String followerUsername : userIDs) {
//                insertUserFollows(followerUsername, userID);
//            }
//        } catch (IOException e) {
//            logger.error("Failed to import followers from file: {}", file.getAbsolutePath(), e);
//        }
//    }
//
//    private static void importFollowing(File file) {
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            Map<String, Object> data = objectMapper.readValue(file, new TypeReference<>() {});
//
//            String username = (String) data.get("username");
//            @SuppressWarnings("unchecked")
//            List<String> userIDs = (List<String>) data.get("userIDs");
//
//            int userID = getUserID(username);
//            if (userID == -1) {
//                logger.error("User not found for username: {}", username);
//                return;
//            }
//
//            for (String followedUsername : userIDs) {
//                insertUserFollows(userID, followedUsername);
//            }
//        } catch (IOException e) {
//            logger.error("Failed to import following from file: {}", file.getAbsolutePath(), e);
//        }
//    }
//
//    private static void importRetweetData(File file) {
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            Map<String, Object> data = objectMapper.readValue(file, new TypeReference<>() {});
//
//            String username = (String) data.get("username");
//            @SuppressWarnings("unchecked")
//            List<Map<String, Object>> tweets = (List<Map<String, Object>>) data.get("tweets");
//
//            int userID = getUserID(username);
//            if (userID == -1) {
//                logger.error("User not found for username: {}", username);
//                return;
//            }
//
//            for (Map<String, Object> tweet : tweets) {
//                String tweetID = (String) tweet.get("tweetID");
//                @SuppressWarnings("unchecked")
//                List<String> retweeters = (List<String>) tweet.get("retweeters");
//
//                // Insert tweet into database
//                insertTweet(tweetID, userID, null);
//
//                for (String retweeterUsername : retweeters) {
//                    insertUserTweetInteraction(retweeterUsername, tweetID, "RETWEET");
//                }
//            }
//        } catch (IOException e) {
//            logger.error("Failed to import retweet data from file: {}", file.getAbsolutePath(), e);
//        }
//    }
//
//    private static void insertUserFollows(Object follower, Object followed) {
//        String query = QueryLoader.getQuery("INSERT_USER_FOLLOWS_TABLE");
//        try (Connection connection = DatabaseConnector.connect();
//             PreparedStatement ps = connection.prepareStatement(query)) {
//
//            ps.setObject(1, follower);
//            ps.setObject(2, followed);
//            ps.executeUpdate();
//            logger.info("Inserted user follow relationship: {} -> {}", follower, followed);
//        } catch (SQLException e) {
//            logger.error("Failed to insert user follow relationship: {} -> {}", follower, followed, e);
//        }
//    }
//
//    private static void insertTweet(String tweetID, int userID, String content) {
//        String query = QueryLoader.getQuery("INSERT_TWEETS_TABLE");
//        try (Connection connection = DatabaseConnector.connect();
//             PreparedStatement ps = connection.prepareStatement(query)) {
//
//            ps.setString(1, tweetID);
//            ps.setInt(2, userID);
//            ps.setString(3, content);
//            ps.setObject(4, null); // createdAt
//            ps.setInt(5, 0); // retweetCount
//            ps.setInt(6, 0); // likeCount
//            ps.setInt(7, 0); // replyCount
//            ps.setInt(8, 0); // quoteCount
//            ps.setString(9, null); // mediaURL
//            ps.setString(10, null); // hashtags
//            ps.setString(11, null); // language
//            ps.executeUpdate();
//            logger.info("Inserted or updated tweet: {}", tweetID);
//        } catch (SQLException e) {
//            logger.error("Failed to insert tweet: {}", tweetID, e);
//        }
//    }
//
//    private static void insertUserTweetInteraction(String username, String tweetID, String interactionType) {
//        String query = QueryLoader.getQuery("INSERT_USER_TWEETS_TABLE");
//        try (Connection connection = DatabaseConnector.connect();
//             PreparedStatement ps = connection.prepareStatement(query)) {
//
//            ps.setObject(1, username);
//            ps.setObject(2, tweetID);
//            ps.setObject(3, interactionType);
//            ps.executeUpdate();
//            logger.info("Inserted tweet interaction for user: {}", username);
//        } catch (SQLException e) {
//            logger.error("Failed to insert tweet interaction for user: {}", username, e);
//        }
//    }
//
//    private static int getUserID(String username) {
//        String query = QueryLoader.getQuery("QUERY_USERID_BY_USERNAME");
//        try (Connection connection = DatabaseConnector.connect();
//             PreparedStatement ps = connection.prepareStatement(query)) {
//
//            ps.setString(1, username);
//            var rs = ps.executeQuery();
//            if (rs.next()) {
//                return rs.getInt("userID");
//            }
//        } catch (SQLException e) {
//            logger.error("Failed to get userID for username: {}", username, e);
//        }
//        return -1; // User not found
//    }
//}