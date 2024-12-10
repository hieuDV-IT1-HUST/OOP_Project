package scraper;

import others.config.AppConfig;
import scraper.tools.Navigator;
import scraper.tools.TwitterLogin;
import scraper.tools.UserProcessor;
import org.openqa.selenium.WebDriver;

public class Main {
    public static void main(String[] args) {
        // Login
        TwitterLogin login = new TwitterLogin();
        AppConfig.loadProperties();
        String username = AppConfig.getTwitterUsername();
        String password = AppConfig.getTwitterPassword();
        WebDriver driver = login.login(username, password);

        try {
            // Navigate to tab "People"
            Navigator navigator = new Navigator(driver);
            navigator.navigateToPeopleTab("blockchain");

            // Handling users
            UserProcessor userProcessor = new UserProcessor(driver);
            userProcessor.processAllUsers();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close driver
            driver.quit();
        }
    }
}