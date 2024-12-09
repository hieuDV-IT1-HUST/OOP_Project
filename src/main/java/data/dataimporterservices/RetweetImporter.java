package data.dataimporterservices;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RetweetImporter {
    private static final Logger logger = LogManager.getLogger(RetweetImporter.class);
    private final UserService userService = new UserService();
    private final TweetService tweetService = new TweetService();

    public void importRetweetData(File file) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> data = objectMapper.readValue(file, new TypeReference<>() {});
            String username = (String) data.get("username");

            List<Map<String, Object>> tweetsData = (List<Map<String, Object>>) data.get("tweets");

            int authorUserID = userService.getUserID(username);
            if (authorUserID == -1) {
                logger.error("User not found for username: {}", username);
                return;
            }

            // Bước 1: Chèn tất cả Users & Tweets
            for (Map<String, Object> tweet : tweetsData) {
                String tweetID = (String) tweet.get("tweetID");

                List<String> mentions = (List<String>) tweet.get("mentions");
                if (mentions == null) mentions = new ArrayList<>();

                List<String> retweeters = (List<String>) tweet.get("retweeters");
                if (retweeters == null) retweeters = new ArrayList<>();

                tweetService.insertTweet(tweetID, authorUserID, "");

                // Đảm bảo user mention tồn tại
                for (String mentionUsername : mentions) {
                    int mentionedUserID = userService.getUserID(mentionUsername);
                    if (mentionedUserID == -1) {
                        logger.error("Failed to find or insert user for mention username: {}", mentionUsername);
                    }
                }

                // Đảm bảo user retweeter tồn tại
                for (String retweeterUsername : retweeters) {
                    int retweeterUserID = userService.getUserID(retweeterUsername);
                    if (retweeterUserID == -1) {
                        logger.error("Failed to find or insert user for retweeter username: {}", retweeterUsername);
                    }
                }
            }

            // Bước 2: Chèn dữ liệu vào User_Tweets và cập nhật retweetCount
            for (Map<String, Object> tweet : tweetsData) {
                String tweetID = (String) tweet.get("tweetID");

                List<String> mentions = (List<String>) tweet.get("mentions");
                if (mentions == null) mentions = new ArrayList<>();

                List<String> retweeters = (List<String>) tweet.get("retweeters");
                if (retweeters == null) retweeters = new ArrayList<>();

                // MENTION
                for (String mentionUsername : mentions) {
                    int mentionedUserID = userService.getUserID(mentionUsername);
                    if (mentionedUserID > 0) {
                        tweetService.insertUserTweetInteraction(authorUserID, tweetID, null, mentionedUserID, "MENTION");
                    }
                }

                // RETWEET
                for (String retweeterUsername : retweeters) {
                    int retweeterUserID = userService.getUserID(retweeterUsername);
                    if (retweeterUserID > 0) {
                        tweetService.insertUserTweetInteraction(retweeterUserID, tweetID, null, authorUserID, "RETWEET");
                    }
                }

                // Cập nhật retweetCount cho tweet
                tweetService.updateTweetRetweetCount(tweetID, retweeters.size());
            }

        } catch (IOException e) {
            logger.error("Failed to import retweet data from file: {}", file.getAbsolutePath(), e);
        }
    }
}