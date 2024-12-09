package scraper.old.selenium;

import config.AppConfig;
import data.node.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.NoSuchElementException;

import static data.DataImporter.*;

public class UsernameScraper extends BaseScraper {
    private static final Logger logger = LogManager.getLogger(UsernameScraper.class);

    private final Map<String, List<String>> scrapedData = new HashMap<>();
    @Override
    public void scrape(String inputFilePath, String outputFilePath) throws InterruptedException {
        login();

        AppConfig.loadProperties();
        List<String> keywords = AppConfig.getBlockchainKeywords();

        for (String keyword : keywords) {
            scrapeForKeyword(keyword);
        }

        saveData(outputFilePath, scrapedData);
        List<User> users = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : scrapedData.entrySet()) {
            String keyword = entry.getKey();
            List<String> usernames = entry.getValue();

            for (String username : usernames) {
                users.add(new User(username, null, 0, 0, null,
                        false, null, null, keyword, null));
            }
        }
        batchInsertUsers(users);
        close();
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
                WebElement tabName = wait.until(ExpectedConditions.visibilityOf(tab.findElement(By.cssSelector("div[dir='ltr']"))));
                if (tabName.getText().equalsIgnoreCase("People")) {
                    tab.click();
                    Thread.sleep(3000 + new Random().nextInt(2000));
                    break;
                }
            }

            int resultsLimit = 2;
            Set<String> usernames = new HashSet<>();

            List<WebElement> userElements = driver.findElements(By
                    .cssSelector("button[data-testid='UserCell'] div[dir='ltr']"));
            for (WebElement element : userElements) {
                String username = element.getText();
                if (username.startsWith("@") && usernames.size() < resultsLimit) {
                    usernames.add(username.substring(1));
                }
            }

            Actions actions = new Actions(driver);
            actions.sendKeys(Keys.PAGE_DOWN).perform();
            Thread.sleep(2000 + new Random().nextInt(3000));

            scrapedData.put(keyword, new ArrayList<>(usernames));
//            logger.info("Scraped {} usernames for keyword '{}': {}", usernames.size(), keyword, usernames);

        } catch (NoSuchElementException e) {
            logger.error("Search box or user elements not found for keyword '{}'", keyword, e);
        }
    }

    public static void main(String[] args) {
        UsernameScraper scraper = new UsernameScraper();
        String inputFilePath = "";
        String outputFilePath = "output/data/scraped_username_data_5.json";

        try {
            scraper.scrape(inputFilePath, outputFilePath);
        } catch (InterruptedException e) {
            logger.error("Error during scraping: {}", e.getMessage());
        }
    }
}