package scraper;

import config.AppConfig;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.FileUtils;

import java.time.Duration;
import java.util.*;
import java.util.NoSuchElementException;

import static data.DataImporter.*;

public class UsernameScraper {
    private static final Logger logger = LogManager.getLogger(UsernameScraper.class);

    private final WebDriver driver;
    private final Map<String, List<String>> scrapedData = new HashMap<>();

    public UsernameScraper() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
    }

    public void scrapeKeywords() throws Exception {
        TwitterLogin twitterLogin = new TwitterLogin(driver);
        twitterLogin.login();

        AppConfig.loadProperties();
        List<String> keywords = AppConfig.getBlockchainKeywords();

        for (String keyword : keywords) {
            scrapeForKeyword(keyword);
        }

        FileUtils.writeJsonToFile("output/data/scraped_username_data_2.json", scrapedData);

        for (Map.Entry<String, List<String>> entry : scrapedData.entrySet()) {
            List<String> usernames = entry.getValue();

            for (String username : usernames) {
                insertUserTable(username, null, 0, 0, null,
                        false, null, null, null);
            }
        }

        driver.quit();
    }

    private void scrapeForKeyword(String keyword) throws InterruptedException {
        driver.get("https://twitter.com");

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("input[data-testid='SearchBox_Search_Input']")
            ));

            searchBox.sendKeys(keyword);
            Thread.sleep(2000 + new Random().nextInt(1000));
            searchBox.sendKeys(Keys.ENTER);

            Thread.sleep(5000);
            WebElement tabList = driver.findElement(By.cssSelector("div[role='tablist']"));
            List<WebElement> tabs = tabList.findElements(By.cssSelector("div[role='presentation']"));
            for (WebElement tab : tabs) {
                WebElement tabName = tab.findElement(By.cssSelector("span[dir='ltr']"));
                if (tabName.getText().equalsIgnoreCase("People")) {
                    tab.click();
                    Thread.sleep(3000 + new Random().nextInt(2000));
                    break;
                }
            }

            int resultsLimit = 1;
            Set<String> usernames = new HashSet<>();

            List<WebElement> userElements = driver.findElements(By
                    .cssSelector("button[data-testid='UserCell'] span[dir='ltr']"));
            for (WebElement element : userElements) {
                String username = element.getText();
                if (username.startsWith("@") && usernames.size() < resultsLimit) {
                    usernames.add(username);
                }
                Actions actions = new Actions(driver);
                actions.sendKeys(Keys.PAGE_DOWN).perform();
                Thread.sleep(2000 + new Random().nextInt(3000));
            }

            scrapedData.put(keyword, new ArrayList<>(usernames));
            logger.info("Scraped {} usernames for keyword '{}': {}", usernames.size(), keyword, usernames);

        } catch (NoSuchElementException e) {
            logger.error("Search box or user elements not found for keyword '{}'", keyword, e);
        }
    }

    public static void main(String[] args) throws Exception {
        new UsernameScraper().scrapeKeywords();
    }
}