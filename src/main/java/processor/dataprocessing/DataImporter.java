package processor.dataprocessing;

import processor.dataprocessing.dataimporterservices.FollowerImporter;
import processor.dataprocessing.dataimporterservices.FollowingImporter;
import processor.dataprocessing.dataimporterservices.RetweetImporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class DataImporter {
    private static final Logger logger = LogManager.getLogger(DataImporter.class);

    private final FollowerImporter followerImporter = new FollowerImporter();
    private final FollowingImporter followingImporter = new FollowingImporter();
    private final RetweetImporter retweetImporter = new RetweetImporter();

    public void run(String rootDirectory) {
        try {
            logger.info("Importing Data from: {}", rootDirectory);
            importData(rootDirectory);
            logger.info("Complete import Data from: {}", rootDirectory);
        } catch (Exception e) {
            logger.error("Error when import Data from: {}", e.getMessage(), e);
        }
    }

    public void importData(String rootDirectory) {
        File rootDir = new File(rootDirectory);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            logger.error("No root directory found in: {}", rootDirectory);
            return;
        }

        for (File userDir : rootDir.listFiles(File::isDirectory)) {
            logger.info("Handling user: {}", userDir.getName());

            File followersFile = new File(userDir, "followers.json");
            File followingFile = new File(userDir, "following.json");
            File retweetDataFile = new File(userDir, "retweetData.json");

            if (followersFile.exists()) {
                logger.info("Importing Followers from: {}", followersFile.getAbsolutePath());
                followerImporter.importFollowers(followersFile);
            } else {
                logger.warn("No followers.json found in: {}", userDir.getAbsolutePath());
            }

            if (followingFile.exists()) {
                logger.info("Importing Following from: {}", followingFile.getAbsolutePath());
                followingImporter.importFollowing(followingFile);
            } else {
                logger.warn("No following.json found in: {}", userDir.getAbsolutePath());
            }

            if (retweetDataFile.exists()) {
                logger.info("Importing Retweet from: {}", retweetDataFile.getAbsolutePath());
                retweetImporter.importRetweetData(retweetDataFile);
            } else {
                logger.warn("No retweetData.json found in: {}", userDir.getAbsolutePath());
            }
        }
    }
}