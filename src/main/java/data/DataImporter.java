package data;

// DataImporter: Import raw data from file (JSON, CSV, etc.) into database.
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sql.QueryLoader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataImporter {
    private static final Logger logger = LogManager.getLogger(DataImporter.class);

    /**
     * Insert a username or update user information in the Users table.
     *
     * @param username        the username of the user
     * @param displayName     the display name of the user
     * @param followerCount   the number of followers
     * @param followingCount  the number of followings
     * @param bio             the user's bio
     * @param verified        whether the user is verified
     * @param profileImageURL the URL of the user's profile image
     * @param createdAt       the account creation date
     * @param location        the location of the user
     */
    public static void insertUserTable(String username, String displayName, int followerCount, int followingCount,
                                       String bio, boolean verified, String profileImageURL, Object createdAt,
                                       String location) {
        String insertOrUpdateQuery = QueryLoader.getQuery("INSERT_USERS_TABLE");
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement ps = connection.prepareStatement(insertOrUpdateQuery)) {

            ps.setString(1, username);
            ps.setString(2, displayName);
            ps.setInt(3, followerCount);
            ps.setInt(4, followingCount);
            ps.setString(5, bio);
            ps.setBoolean(6, verified);
            ps.setString(7, profileImageURL);
            ps.setObject(8, createdAt);
            ps.setString(9, location);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Inserted or updated user: {}", username);
            }
        } catch (SQLException e) {
            logger.error("Failed to insert user: {}", username, e);
        }
    }
}