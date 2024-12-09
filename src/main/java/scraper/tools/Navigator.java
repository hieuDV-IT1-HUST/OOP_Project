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
            // Điều hướng đến trang tìm kiếm với hashtag
            driver.get("https://twitter.com/search?q=%23" + hashtag);

            // Chờ tab "People" xuất hiện
            WebElement peopleTab = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//span[text()='People' and contains(@class, 'css-1jxf684')]")
            ));

            // Nhấp vào tab "People"
            peopleTab.click();

            // Chờ nội dung của tab "People" tải xong
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("processor/main")));

            System.out.println("Đã điều hướng đến tab 'People' thành công.");
            Thread.sleep(5000);
        } catch (Exception e) {
            System.out.println("Không thể điều hướng đến tab 'People'.");
            e.printStackTrace();
        }
    }
}