package data.data_importer_service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FollowingImporter {
    private static final Logger logger = LogManager.getLogger(FollowingImporter.class);
    private final UserService userService = new UserService();

    public void importFollowing(File file) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> data = objectMapper.readValue(file, new TypeReference<>() {});

            String username = (String) data.get("username");
            List<String> followingList = (List<String>) data.get("userIDs");

            int userID = userService.getUserID(username);
            if (userID == -1) {
                logger.error("User not found for username: {}", username);
                return;
            }

            for (String followedUsername : followingList) {
                int followedID = userService.getUserID(followedUsername);
                if (followedID > 0) {
                    userService.insertUserFollows(userID, followedID, null);
                } else {
                    logger.error("Failed to find userID for followed username: {}", followedUsername);
                }
            }

            userService.updateUserFollowingCount(userID, followingList.size());

        } catch (IOException e) {
            logger.error("Failed to import following from file: {}", file.getAbsolutePath(), e);
        }
    }
}