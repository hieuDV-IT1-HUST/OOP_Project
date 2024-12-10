package scraper.selenium;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import scraper.login.TwitterLogin;
import others.utils.FileUtils;

import java.io.IOException;
import java.util.Map;

public abstract class BaseScraper {
    protected static final Logger logger = LogManager.getLogger(BaseScraper.class);
    protected WebDriver driver;
    protected final ObjectMapper objectMapper = new ObjectMapper();

    public BaseScraper() {
        WebDriverManager.chromedriver().setup();
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