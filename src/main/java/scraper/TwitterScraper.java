package scraper;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.ArrayList;
import java.util.List;
import java.io.FileInputStream;
import java.util.Properties;

public class TwitterScraper {
    private WebDriver driver;

    // Constructor
    public TwitterScraper() {
        WebDriverManager.chromedriver().setup();
        this.driver = new ChromeDriver();
    }

    public void login(String username, String password) {
        driver.get("https://twitter.com/login");
        try {
            Thread.sleep(2000);

            WebElement usernameField = driver.findElement(By.name("session[username_or_email]"));
            WebElement passwordField = driver.findElement(By.name("session[password]"));
            WebElement loginButton = driver.findElement(By.cssSelector("div[data-testid='LoginForm_Login_Button']"));

            usernameField.sendKeys(username);
            passwordField.sendKeys(password);
            loginButton.click();

            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> searchHashtag(String hashtag) {
        List<String> userHandles = new ArrayList<>();
        try {
            driver.get("https://twitter.com/search?q=" + hashtag + "&src=typed_query");
            Thread.sleep(3000);

            for (int i = 0; i < 5; i++) {
                List<WebElement> userElements = driver.findElements(By.xpath("//div[@data-testid='UserCell']//span"));
                for (WebElement userElement : userElements) {
                    String handle = userElement.getText();
                    if (handle.startsWith("@") && !userHandles.contains(handle)) {
                        userHandles.add(handle);
                    }
                }
                scrollDown();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userHandles;
    }

    private void scrollDown() {
        try {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }
}