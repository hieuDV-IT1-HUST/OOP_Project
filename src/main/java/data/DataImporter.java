package data;

import data.dataimporterservices.FollowerImporter;
import data.dataimporterservices.FollowingImporter;
import data.dataimporterservices.RetweetImporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class DataImporter {
    private static final Logger logger = LogManager.getLogger(DataImporter.class);

    // Các importer chuyên trách
    private final FollowerImporter followerImporter = new FollowerImporter();
    private final FollowingImporter followingImporter = new FollowingImporter();
    private final RetweetImporter retweetImporter = new RetweetImporter();

    public static void main(String[] args) {
        String rootDirectory = "output/Data"; // Thư mục chứa các tệp JSON
        DataImporter importer = new DataImporter();
        importer.run(rootDirectory);
    }

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