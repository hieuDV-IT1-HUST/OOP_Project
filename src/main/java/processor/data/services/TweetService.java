package processor.data.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import others.config.AppConfig;
import processor.data.node.Tweet;
import processor.data.DatabaseConnector;
import processor.data.sql.QueryLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class TweetService {
    private static final Logger logger = LogManager.getLogger(TweetService.class);
    /**
     * Insert multiple tweets into the Tweets table using batch processing.
     *
     * @param tweets List of Tweet objects containing user data to insert.
     */
    public void batchInsertTweets(List<Tweet> tweets) {
        AppConfig.loadProperties();
        String query = QueryLoader.getQuery("INSERT_TWEETS");

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement ps = connection.prepareStatement(query)) {

            connection.setAutoCommit(false);

            for (Tweet tweet : tweets) {
                ps.setLong(1, tweet.tweetID());
                ps.setString(2, tweet.username());
                ps.setString(3, tweet.content());
                ps.setObject(4, tweet.createdAt());
                ps.setInt(5, tweet.retweetCount());
                ps.setInt(6, tweet.likeCount());
                ps.setInt(7, tweet.replyCount());
                ps.setInt(8, tweet.viewCount());
                ps.setString(9, tweet.mediaURL());
                ps.setString(10, tweet.hashtags());
                ps.setString(11, tweet.language());
                ps.addBatch();
            }

            ps.executeBatch();
            connection.commit();
            logger.info("Batch inserted {} tweets.", tweets.size());
        } catch (SQLException e) {
            throw new RuntimeException("Batch insert of tweets failed.", e);
        }
    }
}