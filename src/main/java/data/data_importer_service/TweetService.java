package data.data_importer_service;

import data.DatabaseConnector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import data.sql.QueryLoader;

import java.sql.*;

public class TweetService {
    private static final Logger logger = LogManager.getLogger(TweetService.class);

    public void insertTweet(String tweetID, int userID, String content) {
        if (content == null) content = "";
        String query = QueryLoader.getQuery("INSERT_TWEETS_TABLE");
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, tweetID);
            ps.setInt(2, userID);
            ps.setString(3, content);
            ps.setObject(4, null); // createdAt
            ps.setInt(5, 0); // retweetCount
            ps.setInt(6, 0); // likeCount
            ps.setInt(7, 0); // replyCount
            ps.setInt(8, 0); // quoteCount
            ps.setString(9, null); // mediaURL
            ps.setString(10, null); // hashtags
            ps.setString(11, null); // language
            ps.executeUpdate();
            logger.info("Inserted or updated tweet: {}", tweetID);
        } catch (SQLException e) {
            logger.error("Failed to insert tweet: {}", tweetID, e);
        }
    }

    public void insertUserTweetInteraction(int userID, String tweetID, String tweetQuoteReplyID,
                                           Integer authorOrMentionedID, String interactionType) {
        String query = QueryLoader.getQuery("INSERT_USER_TWEETS_TABLE");
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, userID);
            ps.setString(2, tweetID);

            if (tweetQuoteReplyID != null) {
                ps.setString(3, tweetQuoteReplyID);
            } else {
                ps.setNull(3, Types.VARCHAR);
            }

            if (authorOrMentionedID != null) {
                ps.setInt(4, authorOrMentionedID);
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            ps.setString(5, interactionType);
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));

            ps.executeUpdate();
            logger.info("Inserted tweet interaction: userID={} interactionType={} tweetID={}", userID, interactionType, tweetID);
        } catch (SQLException e) {
            logger.error("Failed to insert tweet interaction for userID={} tweetID={}", userID, tweetID, e);
        }
    }

    public void updateTweetRetweetCount(String tweetID, int newCount) {
        String query = "UPDATE Tweets SET retweetCount = ? WHERE tweetID = ?";
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, newCount);
            ps.setString(2, tweetID);
            ps.executeUpdate();
            logger.info("Updated retweetCount for tweetID={} to {}", tweetID, newCount);
        } catch (SQLException e) {
            logger.error("Failed to update retweetCount for tweetID={}", tweetID, e);
        }
    }
}