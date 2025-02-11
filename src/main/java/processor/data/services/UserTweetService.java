package processor.data.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import others.config.AppConfig;
import processor.data.node.User_Tweet;
import processor.data.DatabaseConnector;
import processor.data.sql.QueryLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class UserTweetService {
    private static final Logger logger = LogManager.getLogger(UserTweetService.class);
    /**
     * Insert multiple Interactions into the User_Tweets table using batch processing.
     *
     * @param userTweets List of User-Tweet interaction objects containing user data to insert.
     */
    public void batchInsertUserTweets(List<User_Tweet> userTweets) {
        AppConfig.loadProperties();
        String query = QueryLoader.getQuery("INSERT_USER_TWEETS");

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement ps = connection.prepareStatement(query)) {

            connection.setAutoCommit(false);

            for (User_Tweet userTweet : userTweets) {
                ps.setString(1, userTweet.username());
                ps.setLong(2, userTweet.tweetID());
                ps.setObject(3, userTweet.tweetQuoteReplyID());
                ps.setString(4, userTweet.authorOrMentioned());
                ps.setString(5, userTweet.interactionType());
                ps.setObject(6, userTweet.interactionTime());
                ps.addBatch();
            }

            ps.executeBatch();
            connection.commit();
            logger.info("Batch inserted {} user-tweet relationships.", userTweets.size());
        } catch (SQLException e) {
            throw new RuntimeException("Batch insert of user-tweet relationships failed.", e);
        }
    }
}