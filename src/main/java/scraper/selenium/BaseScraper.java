package scraper.selenium;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.chrome.ChromeOptions;
import scraper.login.TwitterLogin;
import others.utils.FileUtils;

import java.io.IOException;
import java.util.Map;

public abstract class BaseScraper {
    protected static final Logger logger = LogManager.getLogger(BaseScraper.class);
    protected WebDriver driver;
    protected final ObjectMapper objectMapper = new ObjectMapper();

    public BaseScraper() {
//        ChromeOptions options = new ChromeOptions();
//        String userProfilePath = "C:\\Users\\Thinkpad\\AppData\\Local\\Google\\Chrome\\User Data";
//        options.addArguments("user-data-dir=" + userProfilePath);
//        options.addArguments("profile-directory=Profile 8");
//        options.addArguments("--no-first-run");
//        options.addArguments("--disable-extensions");
//        options.addArguments("--disable-blink-features=AutomationControlled");
//        options.addArguments("--start-maximized");
//        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
//        driver = new ChromeDriver(options);
        driver = new ChromeDriver();
    }

    public abstract void scrape(String inputFilePath, String outputFilePath)
            throws IOException, InterruptedException;

    protected void login() {
        try {
            TwitterLogin twitterLogin = new TwitterLogin(driver);
            twitterLogin.login();
            logger.info("Login successful.");
        } catch (Exception e) {
            logger.error("Login failed: {}", e.getMessage());
            throw new RuntimeException("Login process failed.");
        }
    }

    protected void saveData(String outputFilePath, Map<String, ?> data) {
        FileUtils.writeJsonToFile(outputFilePath, data);
        logger.info("Data saved to: {}", outputFilePath);
    }

    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }
}