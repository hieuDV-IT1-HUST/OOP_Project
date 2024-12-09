package processor.dataprocessing.dataimporterservices;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FollowerImporter {
    private static final Logger logger = LogManager.getLogger(FollowerImporter.class);
    private final UserService userService = new UserService();

    public void importFollowers(File file) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> data = objectMapper.readValue(file, new TypeReference<>() {});
            String username = (String) data.get("username");
            List<String> followersList = (List<String>) data.get("userIDs");

            int userID = userService.getUserID(username);
            if (userID == -1) {
                logger.error("User not found for username: {}", username);
                return;
            }

            for (String followerUsername : followersList) {
                int followerID = userService.getUserID(followerUsername);
                if (followerID > 0) {
                    userService.insertUserFollows(followerID, userID, null);
                } else {
                    logger.error("Failed to find userID for follower username: {}", followerUsername);
                }
            }

            userService.updateUserFollowerCount(userID, followersList.size());

        } catch (IOException e) {
            logger.error("Failed to import followers from file: {}", file.getAbsolutePath(), e);
        }
    }
}