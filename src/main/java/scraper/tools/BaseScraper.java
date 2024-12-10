package scraper.tools;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.JavascriptExecutor;

public abstract class BaseScraper implements IScraper {
    protected WebDriver driver;

    public BaseScraper(WebDriver driver) {
        this.driver = driver;
    }

    protected void scroll(int maxScrollRetries) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        int retries = 0;
        while (retries < maxScrollRetries) {
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            try {
                Thread.sleep(1000); // Đợi nội dung tải thêm
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retries++;
        }
    }
}