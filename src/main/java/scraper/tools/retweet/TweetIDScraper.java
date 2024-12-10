package scraper.tools.retweet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import scraper.tools.BaseScraper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TweetIDScraper extends BaseScraper {

    public TweetIDScraper(WebDriver driver) {
        super(driver);
    }

    @Override
    public void scrapeData(String username, String outputPath) {
        scrapeTweets(username, outputPath);
    }

    private void scrapeTweets(String username, String outputPath) {
        List<Map<String, Object>> tweetDataList = new ArrayList<>();
        int maxScrollCount = 5; // Giới hạn số lần cuộn
        int scrollCount = 0;

        try {
            while(scrollCount < maxScrollCount){
                // Lấy danh sách tất cả bài viết (tweet)
                List<WebElement> tweets = driver.findElements(By.xpath("//article[@data-testid='tweet']"));

                // Thêm dữ liệu tweet vào danh sách
                for (WebElement tweet : tweets) {
                    String tweetID = extractTweetID(tweet);
                    List<String> mentions = extractMentions(tweet);

                    if (tweetID != null) {
                        Map<String, Object> tweetData = new HashMap<>();
                        tweetData.put("tweetID", tweetID);
                        tweetData.put("mentions", mentions.isEmpty() ? null : mentions);

                        // Tránh trùng lặp dữ liệu
                        if (!tweetDataList.contains(tweetData)) {
                            tweetDataList.add(tweetData);
                        }
                    }
                }
                scroll(1); // Cuộn xuống cuối trang
                scrollCount++;
            }

            // Lưu dữ liệu tweet vào file JSON
            saveToJsonFile(username, tweetDataList, outputPath);
        } catch (Exception e) {
            System.err.println("Không thể thu thập tất cả Tweet ID: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String extractTweetID(WebElement tweet) {
        try {
            WebElement link = tweet.findElement(By.xpath(".//a[contains(@href, '/status/')]"));
            String tweetURL = link.getAttribute("href");
            if (tweetURL != null && tweetURL.contains("/status/")) {
                return tweetURL.substring(tweetURL.lastIndexOf("/") + 1);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tweet ID: " + e.getMessage());
        }
        return null;
    }

    private List<String> extractMentions(WebElement tweet) {
        List<String> mentions = new ArrayList<>();
        try {
            List<WebElement> mentionElements = tweet.findElements(By.cssSelector("div[data-testid='tweetText'] a"));
            for (WebElement element : mentionElements) {
                String text = element.getText().trim();
                if (text.startsWith("@")) {
                    mentions.add(text);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy mentions: " + e.getMessage());
        }
        return mentions;
    }

    private void saveToJsonFile(String username, List<Map<String, Object>> tweetDataList, String outputPath) {
        try {
            File directory = new File(outputPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String filename = outputPath + File.separator + "tweetData.json";

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), tweetDataList);

            System.out.println("Tweet data saved to " + filename);
        } catch (IOException e) {
            System.err.println("Không thể lưu dữ liệu vào file JSON: " + e.getMessage());
        }
    }
}