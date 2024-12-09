package scraper.selenium;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

public class KOLTweets extends BaseScraper {
    private static final Logger logger = LogManager.getLogger(KOLTweets.class);
    private final Map<String, List<Map<String, Object>>> scrapedTweetIDs = new HashMap<>();

    public void scrape(String inputFilePath, String outputFilePath) throws IOException {
        JsonNode rootNode = objectMapper.readTree(new File(inputFilePath));

        for (JsonNode usernames : rootNode) {
            for (JsonNode usernameNode : usernames) {
                String username = usernameNode.asText();
                logger.info("Scraping tweetIDs for username: {}", username);

                List<Map<String, Object>> tweetIDs = scrapeUserTweetIDs(username, 5);
                scrapedTweetIDs.put(username, tweetIDs);
            }
        }

        close();
        saveData(outputFilePath, scrapedTweetIDs);
    }

    private List<Map<String, Object>> scrapeUserTweetIDs(String username, int maxTweets) {

        List<Map<String, Object>> tweetData = new ArrayList<>();
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            driver.get("https://x.com/" + username);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            while (tweetData.size() < maxTweets) {
                List<WebElement> tweets = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                        By.cssSelector("article[data-testid='tweet']")
                ));

                for (WebElement tweet : tweets) {
                    try {
                        WebElement link = tweet.findElement(By.cssSelector("a[href*='/status/']"));
                        String tweetUrl = link.getAttribute("href");

                        if (tweetUrl.contains("/status/")) {
                            String tweetID = tweetUrl.split("/status/")[1].split("\\?")[0];

                            boolean isRetweet = false;
                            String originalAuthor = null;
                            try {
                                WebElement repostBanner = tweet.findElement(By
                                        .cssSelector("span[data-testid='socialContext'] span"));
                                originalAuthor = repostBanner.getText().replace("reposted", "").trim();
                                isRetweet = true;
                            } catch (NoSuchElementException _) {
                            }

                            if (isRetweet) {
                                try {
                                    List<WebElement> originalAuthorElements = tweet.findElements(By
                                            .cssSelector("a[href^='/'] div[dir='ltr'] span"));
                                    for (WebElement originalAuthorElement : originalAuthorElements) {
                                        if (originalAuthorElement.getText().startsWith("@")) {
                                            originalAuthor = originalAuthorElement.getText().substring(1);
                                        }

                                    }
                                } catch (NoSuchElementException e) {
                                    originalAuthor = null;
                                }
                            }

                            Map<String, Object> tweetInfo = new HashMap<>();
                            tweetInfo.put("tweetID", tweetID);
                            tweetInfo.put("type", isRetweet ? "RETWEET" : "POST");
                            if (isRetweet) {
                                tweetInfo.put("originalAuthor", originalAuthor);
                            }

                            if (!tweetData.contains(tweetInfo)) {
                                tweetData.add(tweetInfo);
                            }

                            if (tweetData.size() >= maxTweets) break;
                        }
                    } catch (NoSuchElementException e) {
                        logger.error("Tweet link not found for username {}: {}", username, e.getMessage());
                    }
                }

                js.executeScript("window.scrollBy(0, 800);");
                Thread.sleep(3000);
            }
        } catch (Exception e) {
            logger.error("Failed to scrape tweetIDs for username {}: {}", username, e.getMessage());
        }
        return tweetData;
    }

    public static void main(String[] args) {
        KOLTweets scraper = new KOLTweets();
        String inputFilePath = "output/data/scraped_username_data_4.json";
        String outputFilePath = "output/data/kol_tweet_ids_2.json";

        try {
            scraper.scrape(inputFilePath, outputFilePath);
        } catch (IOException e) {
            logger.error("Error during scraping: {}", e.getMessage());
        }
    }
}