package data;

// DataImporter: Import raw data from file (JSON, CSV, etc.) into database.
import data.node.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import data.sql.QueryLoader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DataImporter {
    private static final Logger logger = LogManager.getLogger(DataImporter.class);

    /**
     * Insert multiple users into the Users table using batch processing.
     *
     * @param users List of User objects containing user data to insert.
     */
    public static void batchInsertUsers(List<User> users) {
        String insertQuery = QueryLoader.getQuery("INSERT_USERS");

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement ps = connection.prepareStatement(insertQuery)) {

            connection.setAutoCommit(false);

            for (User user : users) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getDisplayName());
                ps.setInt(3, user.getFollowerCount());
                ps.setInt(4, user.getFollowingCount());
                ps.setString(5, user.getBio());
                ps.setBoolean(6, user.isVerified());
                ps.setString(7, user.getProfileImageURL());
                ps.setObject(8, user.getCreatedAt());
                ps.setString(9, user.getFromHashtag());
                ps.setString(10, user.getLocation());

                ps.addBatch();
            }

            int[] rowsAffected = ps.executeBatch();
            connection.commit();

            logger.info("Batch inserted {} users.", rowsAffected.length);
        } catch (SQLException e) {
            logger.error("Batch insert failed.", e);
        }
    }

    /**
     * Insert data into the Tweets table.
     */
    public static void insertTweets(long tweetID, String text, int authorID, Object creationTime, int likeCount,
                                    int retweetCount, int replyCount) {
        String insertQuery = QueryLoader.getQuery("INSERT_TWEETS");
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement ps = connection.prepareStatement(insertQuery)) {

            ps.setLong(1, tweetID);
            ps.setString(2, text);
            ps.setInt(3, authorID);
            ps.setObject(4, creationTime);
            ps.setInt(5, likeCount);
            ps.setInt(6, retweetCount);
            ps.setInt(7, replyCount);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Inserted Tweet with tweetID: {}", tweetID);
            }
        } catch (SQLException e) {
            logger.error("Failed to insert Tweet with tweetID: {}", tweetID, e);
        }
    }
    /**
     * Insert data into the User_Tweets table.
     */
    public static void insertUserTweets(int userID, long tweetID, Long tweetQuoteReplyID,
                                        Integer authorOrMentionedID, String interactionType, Object interactionTime) {
        String insertQuery = QueryLoader.getQuery("INSERT_USER_TWEETS");
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement ps = connection.prepareStatement(insertQuery)) {

            ps.setInt(1, userID);
            ps.setLong(2, tweetID);
            ps.setObject(3, tweetQuoteReplyID);
            ps.setObject(4, authorOrMentionedID);
            ps.setString(5, interactionType);
            ps.setObject(6, interactionTime);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Inserted User_Tweet with tweetID: {}", tweetID);
            }
        } catch (SQLException e) {
            logger.error("Failed to insert User_Tweet with tweetID: {}", tweetID, e);
        }
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
                logger.info("Inserted User_Follows with followerID: {} and followedID: {}", followerID, followedID);
            }
        } catch (SQLException e) {
            logger.error("Failed to insert User_Follows with followerID: {} and followedID: {}", followerID, followedID, e);
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
}