package scraper.tools;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class Navigator {
    private WebDriver driver;

    public Navigator(WebDriver driver) {
        this.driver = driver;
    }

    public void navigateToPeopleTab(String hashtag) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        try {
            driver.get("https://twitter.com/search?q=%23" + hashtag);

            // Wait until tab People to appears
            WebElement peopleTab = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//span[text()='People' and contains(@class, 'css-1jxf684')]")
            ));

            // Press in tab People
            peopleTab.click();

            // Wait for loading
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("processor/main")));

            System.out.println("Navigated to tab People successfully.");
            Thread.sleep(5000);
        } catch (Exception e) {
            System.out.println("Cannot navigate to tab People.");
            e.printStackTrace();
        }
    }
}