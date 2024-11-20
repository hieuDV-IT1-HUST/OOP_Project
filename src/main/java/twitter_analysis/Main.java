package twitter_analysis;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import scraper.TwitterScraper;
import scraper.TweetScraper;
import util.DataSaver;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();
        TwitterScraper twitterScraper = new TwitterScraper();

        String username = System.getenv("TWITTER_USERNAME");
        String password = System.getenv("TWITTER_PASSWORD");

        twitterScraper.login(username, password);

        List<String> kolHandles = twitterScraper.searchHashtag("#blockchain", 50);
        System.out.println("KOLs found: " + kolHandles);

        DataSaver.saveToJSON(kolHandles, "kol_blockchain.json");

        TweetScraper tweetScraper = new TweetScraper(driver);
        for (String handle : kolHandles) {
            List<String> tweets = tweetScraper.fetchTweets(handle, 20);
            DataSaver.saveToJSON(tweets, handle + "_tweets.json");
        }

        twitterScraper.close();
    }
}
