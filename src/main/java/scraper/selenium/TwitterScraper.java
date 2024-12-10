package scraper.selenium;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import others.config.AppConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import others.utils.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.List;

public class TwitterScraper {
    private static final Logger logger = LogManager.getLogger(TwitterScraper.class);
    private final WebDriver driver;
    private final ObjectMapper objectMapper;

    public TwitterScraper() {
        this.objectMapper = new ObjectMapper();

        // Selenium WebDriver Configuration
//        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--headless"); // Run Selenium in non-interface mode
//        options.addArguments("--disable-gpu");
//        options.addArguments("--no-sandbox");
//        this.driver = new ChromeDriver(options);
        this.driver = new ChromeDriver();
    }

    public void scrapeData() {
        try {
            String usernameDataPath = AppConfig.getUsernameDataPath();
            JsonNode scrapedData = FileUtils.readJsonFile(usernameDataPath, new TypeReference<>() {});
            ArrayNode kolData = objectMapper.createArrayNode();

            Iterator<String> keywords = scrapedData.fieldNames();
            while (keywords.hasNext()) {
                String keyword = keywords.next();
                JsonNode usernames = scrapedData.get(keyword);

                for (JsonNode usernameNode : usernames) {
                    String username = usernameNode.asText();
                    logger.info("Scraping data for username: {}", username);

                    // Scrape KOL profile data
                    ObjectNode kolInfo = scrapeKOLProfile(username);
                    kolData.add(kolInfo);

                    // Collect KOL tweets
                    ArrayNode tweets = scrapeKOLTweets(username);
                    kolInfo.set("tweets", tweets);

                    // Collect followers
                    ArrayNode followers = scrapeKOLFollowers(username);
                    kolInfo.set("followers", followers);
                }
            }

            // Write data to JSON file
            String userDataPath = AppConfig.getUserDataPath();
            FileUtils.writeJsonToFile(userDataPath, kolData);

        } catch (Exception e) {
            logger.error("Failed scraping data", e);
        } finally {
            driver.quit();
        }
    }

    // Collect basic information of KOL
    private ObjectNode scrapeKOLProfile(String username) throws InterruptedException {
        ObjectNode kolInfo = objectMapper.createObjectNode();
        driver.get("https://twitter.com/@" + username);
        Thread.sleep(3000);

        // Collect information from the interface
        kolInfo.put("username", driver.findElement(By.xpath("//div[@data-testid='UserName']")).getText());
        kolInfo.put("displayName", driver.findElement(By.xpath("//div[@data-testid='DisplayName']")).getText());
        kolInfo.put("bio", driver.findElement(By.xpath("//div[@data-testid='UserDescription']")).getText());
        kolInfo.put("followerCount", extractNumber(driver.findElement(By.xpath("//a[@href='/followers']")).getText()));
        kolInfo.put("followingCount", extractNumber(driver.findElement(By.xpath("//a[@href='/following']")).getText()));

        return kolInfo;
    }

    // Collect KOL tweets
    private ArrayNode scrapeKOLTweets(String kolUrl) throws InterruptedException {
        ArrayNode tweets = objectMapper.createArrayNode();
        driver.get(kolUrl);
        Thread.sleep(3000);

        List<WebElement> tweetElements = driver.findElements(By.xpath("//div[@data-testid='tweet']"));
        for (WebElement tweetElement : tweetElements) {
            ObjectNode tweet = objectMapper.createObjectNode();
            tweet.put("content", tweetElement.getText());
            tweets.add(tweet);
        }
        return tweets;
    }

    // Collect KOL followers
    private ArrayNode scrapeKOLFollowers(String kolUrl) throws InterruptedException {
        ArrayNode followers = objectMapper.createArrayNode();
        driver.get(kolUrl + "/followers");
        Thread.sleep(3000);

        List<WebElement> followerElements = driver.findElements(By.xpath("//div[@data-testid='UserCell']//a[@role='link']"));
        for (WebElement follower : followerElements) {
            followers.add(follower.getAttribute("href"));
        }
        return followers;
    }

    // Extract quantity from string
    private int extractNumber(String text) {
        String number = text.replaceAll("\\D", "");
        return number.isEmpty() ? 0 : Integer.parseInt(number);
    }

    public static void main(String[] args) {
        TwitterScraper scraper = new TwitterScraper();
        scraper.scrapeData();
    }
}