package data.data_importer_service;

import data.DatabaseConnector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import data.sql.QueryLoader;

import java.sql.*;
import java.sql.Timestamp;

public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);

    public int getUserID(String username) {
        String queryUserID = QueryLoader.getQuery("QUERY_USERID_BY_USERNAME");
        String insertUserQuery = QueryLoader.getQuery("INSERT_USERS_TABLE");

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement psQuery = connection.prepareStatement(queryUserID)) {

            psQuery.setString(1, username);
            ResultSet rs = psQuery.executeQuery();

            if (rs.next()) {
                return rs.getInt("userID");
            } else {
                try (PreparedStatement psInsert = connection.prepareStatement(insertUserQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    psInsert.setString(1, username);
                    psInsert.setString(2, username); // DisplayName
                    psInsert.setInt(3, 0); // followerCount
                    psInsert.setInt(4, 0); // followingCount
                    psInsert.setString(5, ""); // Bio
                    psInsert.setBoolean(6, false); // Verified
                    psInsert.setString(7, ""); // ProfileImageURL
                    psInsert.setObject(8, null); // CreatedAt
                    psInsert.setString(9, ""); // Location

                    psInsert.executeUpdate();
                    ResultSet generatedKeys = psInsert.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get or insert userID for username: {}", username, e);
        }
        return -1;
    }

    public void insertUserFollows(int followerID, int followedID, Timestamp followTime) {
        String query = QueryLoader.getQuery("INSERT_USER_FOLLOWS_TABLE");
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, followerID);
            ps.setInt(2, followedID);
            ps.setTimestamp(3, followTime != null ? followTime : new Timestamp(System.currentTimeMillis()));

            ps.executeUpdate();
            logger.info("Inserted user follow relationship: {} -> {}", followerID, followedID);
        } catch (SQLException e) {
            logger.error("Failed to insert user follow relationship: {} -> {}", followerID, followedID, e);
        }
    }

    public void updateUserFollowerCount(int userID, int newCount) {
        String query = "UPDATE Users SET followerCount = ? WHERE userID = ?";
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, newCount);
            ps.setInt(2, userID);
            ps.executeUpdate();
            logger.info("Updated followerCount for userID={} to {}", userID, newCount);
        } catch (SQLException e) {
            logger.error("Failed to update followerCount for userID={}", userID, e);
        }
    }

    public void updateUserFollowingCount(int userID, int newCount) {
        String query = "UPDATE Users SET followingCount = ? WHERE userID = ?";
        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, newCount);
            ps.setInt(2, userID);
            ps.executeUpdate();
            logger.info("Updated followingCount for userID={} to {}", userID, newCount);
        } catch (SQLException e) {
            logger.error("Failed to update followingCount for userID={}", userID, e);
        }
    }
}