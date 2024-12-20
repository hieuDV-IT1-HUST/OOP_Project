package scraper.tools;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;

public class TwitterLogin {
    private WebDriver driver;

    public TwitterLogin() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-features=NetworkService,NetworkServiceInProcess");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);
        this.driver = new ChromeDriver(options);
    }

    public WebDriver login(String username, String password) {
        driver.get("https://twitter.com/login");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        try {
            // Username
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@name='text']")));
            usernameField.sendKeys(username);

            // Press next
            WebElement nextButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Next']/..")));
            nextButton.click();

            // Password
            WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@name='password']")));
            passwordField.sendKeys(password);

            // Login
            WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Log in']/..")));
            loginButton.click();

            Thread.sleep(5000);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return driver;
    }
}