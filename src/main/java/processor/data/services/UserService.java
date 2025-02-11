package processor.data.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import others.config.AppConfig;
import processor.data.node.User;
import processor.data.DatabaseConnector;
import processor.data.sql.QueryLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);
    /**
     * Insert multiple users into the Users table using batch processing.
     *
     * @param users List of User objects containing user data to insert.
     */
    public void batchInsertUsers(List<User> users) {
        AppConfig.loadProperties();
        String query = QueryLoader.getQuery("INSERT_USERS");

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement ps = connection.prepareStatement(query)) {

            connection.setAutoCommit(false);

            for (User user : users) {
                ps.setString(1, user.username());
                ps.setString(2, user.displayName());
                ps.setInt(3, user.followerCount());
                ps.setInt(4, user.followingCount());
                ps.setString(5, user.bio());
                ps.setBoolean(6, user.verified());
                ps.setString(7, user.profileImageURL());
                ps.setObject(8, user.createdAt());
                ps.setString(9, user.fromHashtag());
                ps.setString(10, user.location());
                ps.addBatch();
            }

            int[] rowsAffected = ps.executeBatch();
            connection.commit();

            logger.info("Batch inserted {} users.", rowsAffected.length);
        } catch (SQLException e) {
            throw new RuntimeException("Batch insert of users failed.", e);
        }
    }
}