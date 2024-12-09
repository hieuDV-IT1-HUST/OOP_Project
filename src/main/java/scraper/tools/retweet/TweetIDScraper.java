package scraper.tools.retweet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TweetIDScraper {
    private WebDriver driver;

    public TweetIDScraper(WebDriver driver) {
        this.driver = driver;
    }

    public void scrapeTweetIDsForUser(String username, String userDirectoryPath) {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        List<Map<String, Object>> tweetDataList = new ArrayList<>();
        int maxScrolls = 5; // Giới hạn số lần cuộn
        int scrollCount = 0;

        try {
            while (scrollCount < maxScrolls) { // Chỉ cuộn xuống tối đa 15 lần
                // Lấy danh sách tất cả bài viết (tweet)
                List<WebElement> tweets = driver.findElements(By.xpath("//article[@data-testid='tweet']"));

                for (WebElement tweet : tweets) {
                    try {
                        String tweetURL = tweet.findElement(By.xpath(".//a[contains(@href, '/status/')]")).getAttribute("href");
                        if (tweetURL != null && tweetURL.contains("/status/")) {
                            // Lấy tweet ID
                            String tweetID = tweetURL.substring(tweetURL.lastIndexOf("/") + 1);

                            // Kiểm tra bài repost
                            WebElement repostElement = null;
                            try {
                                repostElement = tweet.findElement(By.xpath(".//span[contains(text(),'reposted')]"));
                            } catch (Exception ignored) {}

                            if (repostElement == null && tweetID.matches("\\d{10,}")) {
                                // Lấy mentions từ bài viết
                                List<String> mentions = getMentions(tweet);

                                // Tạo đối tượng tweet data
                                Map<String, Object> tweetData = new HashMap<>();
                                tweetData.put("tweetID", tweetID);
                                tweetData.put("mentions", mentions.isEmpty() ? null : mentions); // Nếu không có mention thì để null

                                // Thêm vào danh sách nếu không trùng lặp
                                if (!tweetDataList.contains(tweetData)) {
                                    tweetDataList.add(tweetData);
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Lỗi khi xử lý tweet: " + e.getMessage());
                    }
                }

                // Cuộn xuống cuối trang
                jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                scrollCount++; // Tăng số lần cuộn
                Thread.sleep(2000); // Đợi trang tải nội dung
            }

            // Lưu danh sách tweet vào file JSON
            saveToJsonFile(username, tweetDataList, userDirectoryPath);

        } catch (Exception e) {
            System.out.println("Không thể thu thập tất cả TweetID.");
            e.printStackTrace();
        }
    }

    private List<String> getMentions(WebElement tweet) {
        List<String> mentions = new ArrayList<>();
        try {
            // Lấy tất cả các thẻ <a> có mentions trong bài viết
            List<WebElement> mentionElements = tweet.findElements(By.cssSelector("div[data-testid='tweetText'] a"));

            for (WebElement mentionElement : mentionElements) {
                String mentionText = mentionElement.getText().trim();
                if (mentionText.startsWith("@")) {
                    mentions.add(mentionText);
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi lấy mentions: " + e.getMessage());
        }
        return mentions;
    }

    private void saveToJsonFile(String username, List<Map<String, Object>> tweetDataList, String userDirectoryPath) {
        try {
            File directory = new File(userDirectoryPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String filename = userDirectoryPath + File.separator + "tweetData.json";

            Map<String, Object> data = new HashMap<>();
            data.put("username", username);
            data.put("tweets", tweetDataList);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), data);

            System.out.println("Dữ liệu tweet đã được lưu vào file: " + filename);
        } catch (IOException e) {
            System.out.println("Không thể lưu dữ liệu vào file JSON.");
            e.printStackTrace();
        }
    }
}