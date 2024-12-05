package scraper;

import config.AppConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.OCRUtils;

import java.io.File;
import java.time.Duration;

public class TwitterLogin {
    private static final Logger logger = LogManager.getLogger(TwitterLogin.class);
    private final WebDriver driver;

    public TwitterLogin(WebDriver driver) {
        this.driver = driver;
    }

    public void login() {
        AppConfig.loadProperties();
        String username = AppConfig.getTwitterUsername();
        String usernameOrPhone = AppConfig.getUsernameOrPhone();
        String password = AppConfig.getTwitterPassword();

        try {
            driver.get("https://twitter.com/login");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // Enter username
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("text")));
            usernameField.sendKeys(username);
            usernameField.sendKeys(Keys.RETURN);

            Thread.sleep(3000);

            if (isElementPresent(By.name("text"))) {
                WebElement phoneOrUsernameField = driver.findElement(By.name("text"));
                phoneOrUsernameField.clear();
                phoneOrUsernameField.sendKeys(usernameOrPhone);
                phoneOrUsernameField.sendKeys(Keys.ENTER);
                Thread.sleep(3000);
            }

            // Enter password
            WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password")));
            passwordField.sendKeys(password);
            passwordField.sendKeys(Keys.RETURN);

            // Wait for either captcha or homepage
            try {
                wait.until(ExpectedConditions.urlContains("home"));
                logger.info("Successfully logged into Twitter.");
            } catch (TimeoutException e) {
                logger.warn("Captcha detected during login process.");
                handleCaptcha();
            }
        } catch (Exception e) {
            logger.error("Login failed. Please check credentials or page structure.", e);
            throw new RuntimeException("Login failed", e);
        }
    }

    private boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private void handleCaptcha() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

            // Locate captcha image
            WebElement captchaImage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("img[alt='captcha']")));
            File screenshot = captchaImage.getScreenshotAs(OutputType.FILE);

            // Process image using OCR
            String captchaText = OCRUtils.recognizeTextFromImage(screenshot);
            if (captchaText != null && !captchaText.isEmpty()) {
                logger.info("Recognized captcha text: {}", captchaText);

                // Enter recognized captcha text
                WebElement captchaInput = driver.findElement(By.name("captcha"));
                captchaInput.sendKeys(captchaText);
                captchaInput.sendKeys(Keys.RETURN);

                // Verify if login was successful
                new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.urlContains("home"));
                logger.info("Login successful after solving captcha.");
            } else {
                logger.warn("Captcha recognition failed. Waiting for manual input.");
                requestManualCaptchaInput();
            }
        } catch (Exception e) {
            logger.error("Failed to handle captcha", e);
            throw new RuntimeException("Captcha handling failed", e);
        }
    }

    private void requestManualCaptchaInput() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(120));

            logger.info("Please solve the captcha manually in the browser.");
            wait.until(ExpectedConditions.urlContains("home"));
            logger.info("Login successful after manual captcha resolution.");
        } catch (TimeoutException e) {
            logger.error("Manual captcha resolution timed out.");
            throw new RuntimeException("Manual captcha resolution timed out.", e);
        }
    }
}