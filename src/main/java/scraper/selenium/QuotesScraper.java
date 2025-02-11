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

public class QuotesScraper extends BaseScraper {
    private static final Logger logger = LogManager.getLogger(QuotesScraper.class);
    private final Map<String, List<Map<String, Object>>> quoteData = new HashMap<>();

    public void scrape(String inputFilePath, String outputFilePath) throws IOException {
        login();
        JsonNode rootNode = objectMapper.readTree(new File(inputFilePath));

        // Iterate all username in JSON file
        rootNode.fields().forEachRemaining(entry -> {
            String username = entry.getKey();
            JsonNode tweets = entry.getValue();

            List<Map<String, Object>> userQuotes = new ArrayList<>();

            // Iterate tweet list of each username
            for (JsonNode tweet : tweets) {
                String tweetID = tweet.get("tweetID").asText();
                String type = tweet.get("type").asText();
                String author = type.equals("RETWEET") && tweet.has("originalAuthor")
                        ? tweet.get("originalAuthor").asText()
                        : username;

                String tweetUrl = "https://x.com/" + author + "/status/" + tweetID;
                logger.info("Scraping quotes for tweet: {}", tweetUrl);

                List<Map<String, String>> quotes = scrapeQuoteDetails(tweetUrl, tweetID);

                // Create data structure for each tweet
                Map<String, Object> tweetData = new HashMap<>();
                tweetData.put("tweetID", tweetID);
                tweetData.put("quotes", quotes);

                // Save to userQuotes List
                userQuotes.add(tweetData);
            }

            // Save result for current username
            quoteData.put(username, userQuotes);
        });

        close();
        saveData(outputFilePath, quoteData);
    }

    private List<Map<String, String>> scrapeQuoteDetails(String tweetUrl, String tweetID) {
        List<Map<String, String>> quotes = new ArrayList<>();
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            driver.get(tweetUrl + "/quotes");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            while (true) {
                List<WebElement> quotesList;

                try {
                    quotesList = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                            By.cssSelector("article[data-testid='tweet']")
                    ));
                } catch (TimeoutException e) {
                    logger.warn("No quotes found for tweet {} within timeout.", tweetID);
                    break;
                }

                for (WebElement quote : quotesList) {
                    try {
                        WebElement userLink = quote.findElement(By.cssSelector("a[href^='/']"));
                        WebElement quoteLink = quote.findElement(By.cssSelector("a[href*='/status/']"));

                        String quoteUsername = "@" + userLink.getAttribute("href").split("/")[3];
                        String quoteTweetID = quoteLink.getAttribute("href").split("/status/")[1].split("\\?")[0];

                        Map<String, String> quoteInfo = new HashMap<>();
                        quoteInfo.put("username", quoteUsername);
                        quoteInfo.put("quoteTweetID", quoteTweetID);

                        if (!quotes.contains(quoteInfo)) {
                            quotes.add(quoteInfo);
                        }
                    } catch (NoSuchElementException e) {
                        logger.error("Failed to extract quote for tweet {}: {}", tweetID, e.getMessage());
                    }
                }

                js.executeScript("window.scrollBy(0, 800);");
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            logger.error("Error scraping quotes for tweet {}: {}", tweetID, e.getMessage());
        }

        return quotes;
    }

    public static void main(String[] args) {
        QuotesScraper scraper = new QuotesScraper();
        String inputFilePath = "output/data/kol_tweet_ids/kol_tweet_ids_2.json";
        String outputFilePath = "output/data/quote/kol_quote_data.json";

        try {
            scraper.scrape(inputFilePath, outputFilePath);
        } catch (IOException e) {
            logger.error("Error during scraping: {}", e.getMessage());
        }
    }
}