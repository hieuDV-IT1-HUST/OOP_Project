package scraper.selenium;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.NoSuchElementException;
import utils.FileUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

public class TweetScraper {
    private static final Logger logger = LogManager.getLogger(TweetScraper.class);

    private final WebDriver driver;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, List<Map<String, Object>>> scrapedTweets = new HashMap<>();

    public TweetScraper() {
        WebDriverManager.chromedriver().setup();
        this.driver = new ChromeDriver();
    }

    public void scrapeTweets(String inputFilePath, String outputFilePath) throws IOException {
//        JsonNode scrapedData = objectMapper.readTree(new File(inputFilePath));
//
//        for (Iterator<String> usernames = scrapedData.fieldNames(); usernames.hasNext(); ) {
//            String username = usernames.next();
//            logger.info("Scraping tweets for username: {}", username);
//            List<Map<String, Object>> tweets = scrapeUserTweets(username);
//            scrapedTweets.put(username, tweets);
//        }

        String username = "WEB_3_web";
        List<Map<String, Object>> tweets = scrapeUserTweets(username);
        scrapedTweets.put(username, tweets);

        driver.quit();
        FileUtils.writeJsonToFile(outputFilePath, scrapedTweets);
        logger.info("Scraping completed. Data saved to: {}", outputFilePath);
    }

    private List<Map<String, Object>> scrapeUserTweets(String username) {
        List<Map<String, Object>> tweetData = new ArrayList<>();
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            driver.get("https://x.com/" + username);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("article")));

            int tweetCount = 0;
            List<WebElement> tweets = driver.findElements(By.cssSelector("article"));
            for (WebElement tweet : tweets) {
                if (tweetCount >= 2) break;
                try {
                    js.executeScript("arguments[0].scrollIntoView(true);", tweet);
                    js.executeScript("arguments[0].click();", tweet);
                    Thread.sleep(2000);

                    String currentUrl = driver.getCurrentUrl();
                    if (!currentUrl.contains("/status/")) continue;

                    String tweetID = currentUrl.split("/status/")[1];
                    Map<String, Object> tweetDetails = new HashMap<>();
                    tweetDetails.put("tweetID", tweetID);

                    String urlUsername = currentUrl.split("/")[3];
                    boolean isRetweet = !urlUsername.equalsIgnoreCase(username);

                    if (isRetweet) {
                        tweetDetails.put("type", "RETWEET");
                        tweetDetails.put("originalAuthor", urlUsername);
                    } else {
                        try {
                            WebElement refreshedArticleElement = driver.findElement(By.cssSelector("article[tabindex='-1']"));
                            WebElement quoteElement = refreshedArticleElement.findElement(By.cssSelector("div[tabindex='0'][role='link']"));
                            quoteElement.click();

                            String quoteUrl = driver.getCurrentUrl();
                            String[] urlParts = quoteUrl.split("/");
                            String quotedUsername = urlParts[3];
                            String quotedTweetID = urlParts[5];

                            tweetDetails.put("type", "QUOTE");
                            tweetDetails.put("quotedAuthor", quotedUsername);
                            tweetDetails.put("quotedTweetID", quotedTweetID);

                            driver.navigate().back();
                        } catch (NoSuchElementException e) {
                            tweetDetails.put("type", "POST");
                            logger.error("Not found quote tweet: {}", e.getMessage());
                        } catch (Exception e) {
                            logger.error("Error processing quote tweet: {}", e.getMessage());
                        }
                    }

                    // Check for mentions
                    List<WebElement> mentionElements = driver.findElements(By.cssSelector("a[href^='/'][dir='ltr']"));
                    List<String> mentionedUsers = new ArrayList<>();
                    for (WebElement mention : mentionElements) {
                        String mentionedUser = mention.getText();
                        if (mentionedUser.startsWith("@")) {
                            if (!mentionedUser.equalsIgnoreCase(username)) {
                                mentionedUsers.add(mentionedUser.substring(1));
                            }
                        }
                    }
                    tweetDetails.put("mentionedUsers", mentionedUsers);

                    // Get language
                    WebElement articleElement = driver.findElement(By.cssSelector("article[tabindex='-1']"));
                    WebElement tweetTextElement = articleElement.findElement(By.cssSelector("div[dir='auto'][data-testid='tweetText']"));
                    String lang = tweetTextElement.getAttribute("lang");
                    tweetDetails.put("language", lang);

                    // Lấy thông tin tương tác
                    WebElement interactionElement = articleElement.findElement(By.cssSelector("div[aria-label]"));
                    String interactionText = interactionElement.getAttribute("aria-label");
                    String[] interactionParts = interactionText.split(", ");
                    if (interactionParts.length == 5) {
                        tweetDetails.put("replies", Integer.parseInt(interactionParts[0].split(" ")[0]));
                        tweetDetails.put("reposts", Integer.parseInt(interactionParts[1].split(" ")[0]));
                        tweetDetails.put("likes", Integer.parseInt(interactionParts[2].split(" ")[0]));
                        tweetDetails.put("bookmarks", Integer.parseInt(interactionParts[3].split(" ")[0]));
                        tweetDetails.put("views", Integer.parseInt(interactionParts[4].split(" ")[0]));
                    }

                    List<Map<String, String>> replies = new ArrayList<>();
                    List<WebElement> replyElements = driver.findElements(By.cssSelector("article[tabindex='0'][data-testid='tweet']"));

                    int replyCount = 0;
                    for (WebElement replyElement : replyElements) {
                        if (replyCount >= 3) break;
                        try {
                            replyElement.click();

                            String replyUrl = driver.getCurrentUrl();
                            String replyID = replyUrl.split("/status/")[1];
                            String replyUsername = replyUrl.split("/")[3];

                            if (!replyUsername.equalsIgnoreCase(username)) {
                                Map<String, String> replyDetails = new HashMap<>();
                                replyDetails.put("username", replyUsername);
                                replyDetails.put("tweetID", replyID);
                                replies.add(replyDetails);
                                replyCount++;
                            }

                            driver.navigate().back();
                        } catch (Exception e) {
                            logger.error("Error processing reply tweet: {}", e.getMessage());
                        }
                    }
                    tweetDetails.put("repliesData", replies);

                    tweetData.add(tweetDetails);
                    tweetCount++;
                    driver.navigate().back();
                    tweets = driver.findElements(By.cssSelector("article"));
                    Thread.sleep(2000);
                } catch (Exception e) {
                    logger.error("Failed to process tweet for username: {}. Error: {}", username, e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Failed to scrape tweets for username: {}. Error: {}", username, e.getMessage());
        }

        return tweetData;
    }

    public static void main(String[] args) {
        TweetScraper scraper = new TweetScraper();
        String inputFilePath = "output/data/scraped_username_data.json";
        String outputFilePath = "output/data/tweet_data.json";

        try {
            scraper.scrapeTweets(inputFilePath, outputFilePath);
        } catch (IOException e) {
            logger.error("Error during scraping: {}", e.getMessage());
        }
    }
}