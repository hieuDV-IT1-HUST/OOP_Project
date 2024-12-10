package scraper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterV2;
import twitter4j.TwitterV2ExKt;
import twitter4j.UsersResponse;
import twitter4j.auth.AccessToken;
import others.config.AppConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public final class RetweetUserInfo {

    public static void main(String[] args) {
        // OAuthentication
        Twitter twitter = new TwitterFactory().getInstance();
        AppConfig.loadProperties();
        String consumerKey = AppConfig.getConsumerKey();
        String consumerSecret = AppConfig.getConsumer_Key_Secret();
        String accessToken = AppConfig.getAccess_Token();
        String accessTokenSecret = AppConfig.getAccess_Token_Secret();

        twitter.setOAuthConsumer(consumerKey, consumerSecret);
        twitter.setOAuthAccessToken(new AccessToken(accessToken, accessTokenSecret));

        String rootDirectory = "output/Data";

        try {
            // Sparse through tweetIDs
            Files.list(Paths.get(rootDirectory)).forEach(userDir -> {
                if (Files.isDirectory(userDir)) {
                    File tweetDataFile = new File(userDir.toFile(), "tweetData.json");
                    if (tweetDataFile.exists()) {
                        System.out.println("Processing file: " + tweetDataFile.getAbsolutePath());
                        boolean processed = processTweetDataFile(tweetDataFile, twitter, userDir.toString());

                        // Wait 920s due to API limits
                        if (processed) {
                            try {
                                System.out.println("Wait 920 seconds...");
                                Thread.sleep(920000); // Chờ 920 giây
                            } catch (InterruptedException e) {
                                System.err.println("Interrupted: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean processTweetDataFile(File tweetDataFile, Twitter twitter, String userDirectoryPath) {
        File retweetDataFile = new File(userDirectoryPath, "retweetData.json");

        // Check if retweetData.json has existed
        if (retweetDataFile.exists()) {
            System.out.println("File retweetData.json has already existed: " + userDirectoryPath + ". Skipping...");
            return false;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> inputData = objectMapper.readValue(tweetDataFile, new TypeReference<Map<String, Object>>() {});

            // Get username
            String username = (String) inputData.get("username");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tweetsData = (List<Map<String, Object>>) inputData.get("tweets");

            List<Map<String, Object>> outputData = new ArrayList<>();
            final TwitterV2 v2 = TwitterV2ExKt.getV2(twitter);

            // Limit 10 post only (Due to API Limits)
            int tweetLimit = 10;
            int processedCount = 0;

            for (Map<String, Object> tweetData : tweetsData) {
                if (processedCount >= tweetLimit) break;

                String tweetID = (String) tweetData.get("tweetID");
                List<String> mentions = new ArrayList<>();
                Object mentionsObj = tweetData.get("mentions");
                if (mentionsObj instanceof List<?>) {
                    for (Object obj : (List<?>) mentionsObj) {
                        if (obj instanceof String) {
                            mentions.add((String) obj);
                        }
                    }
                }

                try {
                    final UsersResponse usersResponse = v2.getRetweetUsers(
                            Long.parseLong(tweetID),  // TweetID
                            null,
                            "id,text,created_at",
                            "id,name,username"
                    );

                    List<String> retweeters = new ArrayList<>();
                    usersResponse.getUsers().forEach(user -> retweeters.add("@" + user.getScreenName()));

                    // Save result
                    Map<String, Object> result = new HashMap<>();
                    result.put("tweetID", tweetID);
                    result.put("mentions", mentions);
                    result.put("username", "@" + username);
                    result.put("retweeters", retweeters);

                    outputData.add(result);

                    System.out.println("Done " + tweetID);
                    processedCount++;

                    Thread.sleep(920000); // Sleep 920s due to API limits
                } catch (TwitterException e) {
                    System.err.println("Error when get retweeter for tweetID: " + tweetID);
                    e.printStackTrace();
                }
            }

            // Save to JSON file
            saveToJsonFile(outputData, retweetDataFile.getAbsolutePath());
            return true;
        } catch (Exception e) {
            System.err.println("Error when handling json file:" + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private static void saveToJsonFile(List<Map<String, Object>> data, String filePath) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), data);
            System.out.println("Data saved in: " + filePath);
        } catch (IOException e) {
            System.err.println("Error when handling JSON file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}