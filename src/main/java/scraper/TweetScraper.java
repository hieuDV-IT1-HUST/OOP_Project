package scraper;

import org.openqa.selenium.WebDriver;

import java.util.List;

public class TweetScraper {
    private WebDriver driver;

    public TweetScraper(WebDriver driver) {
        this.driver = driver;
    }

    public List<String> fetchTweets(String userHandle, int maxTweets) {
        // Lấy tweet từ tài khoản
    }
}
