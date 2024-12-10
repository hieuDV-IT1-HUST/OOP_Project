package scraper;

import others.config.AppConfig;
import scraper.tools.Navigator;
import scraper.tools.TwitterLogin;
import scraper.tools.UserProcessor;
import org.openqa.selenium.WebDriver;

public class Main {
    public static void main(String[] args) {
        // Đăng nhập vào Twitter
        TwitterLogin login = new TwitterLogin();
        AppConfig.loadProperties();
        String username = AppConfig.getTwitterUsername();
        String password = AppConfig.getTwitterPassword();
        WebDriver driver = login.login(username, password);

        try {
            // Điều hướng đến tab "People"
            Navigator navigator = new Navigator(driver);
            navigator.navigateToPeopleTab("blockchain");

            // Xử lý tất cả người dùng trong danh sách
            UserProcessor userProcessor = new UserProcessor(driver);
            userProcessor.processAllUsers();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Đóng trình duyệt
            driver.quit();
        }
    }
}