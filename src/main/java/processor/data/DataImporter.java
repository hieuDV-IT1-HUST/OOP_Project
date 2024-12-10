package processor.data;

// DataImporter: Import raw data from file (JSON, CSV, etc.) into database.
import processor.data.data_importer_services.FollowerImporter;
import processor.data.data_importer_services.FollowingImporter;
import processor.data.data_importer_services.RetweetImporter;
import processor.data.node.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import processor.data.sql.QueryLoader;

import java.io.File;
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

    // Các importer chuyên trách
    private final FollowerImporter followerImporter = new FollowerImporter();
    private final FollowingImporter followingImporter = new FollowingImporter();
    private final RetweetImporter retweetImporter = new RetweetImporter();

    public void run(String rootDirectory) {
        try {
            logger.info("Bắt đầu import dữ liệu từ thư mục: {}", rootDirectory);
            importData(rootDirectory);
            logger.info("Hoàn thành import dữ liệu từ thư mục: {}", rootDirectory);
        } catch (Exception e) {
            logger.error("Đã xảy ra lỗi khi import dữ liệu: {}", e.getMessage(), e);
        }
    }

    public void importData(String rootDirectory) {
        File rootDir = new File(rootDirectory);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            logger.error("Thư mục gốc không tồn tại hoặc không phải là thư mục: {}", rootDirectory);
            return;
        }

        // Duyệt qua từng thư mục con
        for (File userDir : rootDir.listFiles(File::isDirectory)) {
            logger.info("Đang xử lý thư mục người dùng: {}", userDir.getName());

            File followersFile = new File(userDir, "followers.json");
            File followingFile = new File(userDir, "following.json");
            File retweetDataFile = new File(userDir, "retweetData.json");

            if (followersFile.exists()) {
                logger.info("Đang import dữ liệu followers từ: {}", followersFile.getAbsolutePath());
                followerImporter.importFollowers(followersFile);
            } else {
                logger.warn("Không tìm thấy file followers.json trong thư mục: {}", userDir.getAbsolutePath());
            }

            if (followingFile.exists()) {
                logger.info("Đang import dữ liệu following từ: {}", followingFile.getAbsolutePath());
                followingImporter.importFollowing(followingFile);
            } else {
                logger.warn("Không tìm thấy file following.json trong thư mục: {}", userDir.getAbsolutePath());
            }

            if (retweetDataFile.exists()) {
                logger.info("Đang import dữ liệu retweet từ: {}", retweetDataFile.getAbsolutePath());
                retweetImporter.importRetweetData(retweetDataFile);
            } else {
                logger.warn("Không tìm thấy file retweetData.json trong thư mục: {}", userDir.getAbsolutePath());
            }
        }
    }
}