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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public final class RetweetUserInfo {

    public static void main(String[] args) {
        // Tạo Twitter instance với thông tin OAuth
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer("ZpeWcEcY8RVGIBKv96BJOqNXG", "nlmKWY9xbhdm2xKOEja8X0QMaqpD1OxJssArgD6fZvwuN3pP8q");
        twitter.setOAuthAccessToken(new AccessToken("1865056103102664704-gu6WmjOIUopPaTV6QBjzOeHinGfaek", "7xUWxapgEeqld3zgmIEw2SJzRj2xoZzuHvaJvq2ocxh9T"));

        String rootDirectory = "output/Data";

        try {
            // Duyệt qua các thư mục con
            Files.list(Paths.get(rootDirectory)).forEach(userDir -> {
                if (Files.isDirectory(userDir)) {
                    File tweetDataFile = new File(userDir.toFile(), "tweetData.json");
                    if (tweetDataFile.exists()) {
                        System.out.println("Đang xử lý file: " + tweetDataFile.getAbsolutePath());
                        boolean processed = processTweetDataFile(tweetDataFile, twitter, userDir.toString());

                        // Chỉ chờ 920 giây nếu thực sự đã xử lý thư mục
                        if (processed) {
                            try {
                                System.out.println("Chờ 920 giây trước khi xử lý thư mục tiếp theo...");
                                Thread.sleep(920000); // Chờ 920 giây
                            } catch (InterruptedException e) {
                                System.err.println("Quá trình chờ bị gián đoạn: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        } catch (IOException e) {
            System.err.println("Lỗi khi duyệt thư mục: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean processTweetDataFile(File tweetDataFile, Twitter twitter, String userDirectoryPath) {
        File retweetDataFile = new File(userDirectoryPath, "retweetData.json");

        // Kiểm tra nếu file retweetData.json đã tồn tại
        if (retweetDataFile.exists()) {
            System.out.println("File retweetData.json đã tồn tại trong thư mục: " + userDirectoryPath + ". Bỏ qua.");
            return false;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> inputData = objectMapper.readValue(tweetDataFile, new TypeReference<Map<String, Object>>() {});

            // Lấy tên người dùng từ `twitterData.json`
            String username = (String) inputData.get("username");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tweetsData = (List<Map<String, Object>>) inputData.get("tweets");

            List<Map<String, Object>> outputData = new ArrayList<>();
            final TwitterV2 v2 = TwitterV2ExKt.getV2(twitter);

            // Giới hạn chỉ xử lý tối đa 10 tweet
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
                            Long.parseLong(tweetID),  // ID của Tweet
                            null,                    // Không yêu cầu mở rộng trường bổ sung
                            "id,text,created_at",    // Các trường chi tiết tweet
                            "id,name,username"       // Các trường chi tiết người dùng
                    );

                    List<String> retweeters = new ArrayList<>();
                    usersResponse.getUsers().forEach(user -> retweeters.add("@" + user.getScreenName()));

                    // Lưu kết quả cho tweetID hiện tại
                    Map<String, Object> result = new HashMap<>();
                    result.put("tweetID", tweetID);
                    result.put("mentions", mentions);
                    result.put("username", "@" + username); // Thêm username từ `tweetData.json`
                    result.put("retweeters", retweeters);

                    outputData.add(result);

                    System.out.println("Đã xử lý xong tweetID: " + tweetID);
                    processedCount++;

                    // Tạm dừng để tránh vượt quá giới hạn API
                    Thread.sleep(920000); // Nghỉ 1 giây giữa các tweet (tùy chỉnh theo giới hạn API)
                } catch (TwitterException e) {
                    System.err.println("Lỗi khi lấy thông tin retweeters cho tweetID: " + tweetID);
                    e.printStackTrace();
                }
            }

            // Ghi kết quả vào file retweetData.json
            saveToJsonFile(outputData, retweetDataFile.getAbsolutePath());
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khi xử lý file tweetData.json: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private static void saveToJsonFile(List<Map<String, Object>> data, String filePath) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), data);
            System.out.println("Dữ liệu đã được lưu vào file: " + filePath);
        } catch (IOException e) {
            System.err.println("Lỗi khi lưu dữ liệu vào file JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }
}