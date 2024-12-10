package scraper.tools;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import scraper.tools.follows.FollowsScraper;
import scraper.tools.retweet.TweetIDScraper;

import org.openqa.selenium.JavascriptExecutor;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserProcessor {
    private WebDriver driver;

    public UserProcessor(WebDriver driver) {
        this.driver = driver;
    }

    public void processAllUsers() {
        try {
            // Tìm tất cả các phần tử liên kết profile
            List<WebElement> users = driver.findElements(By.xpath(
                "//a[contains(@href, '/') and contains(@class, 'r-1wbh5a2')]"
            ));

            if (users.isEmpty()) {
                System.out.println("Không tìm thấy người dùng nào.");
                return;
            }

            System.out.println("Tìm thấy " + users.size() + " người dùng trong danh sách.");

            // Sử dụng Set để lưu trữ các URL duy nhất
            Set<String> uniqueUrls = new HashSet<>();

            // Duyệt qua từng người dùng và thêm URL vào Set
            for (WebElement user : users) {
                String href = user.getAttribute("href");
                if (href != null && !href.isEmpty()) {
                    uniqueUrls.add(href.startsWith("http") ? href : "https://twitter.com" + href);
                }
            }

            System.out.println("Tìm thấy " + uniqueUrls.size() + " URL duy nhất trong danh sách.");

            // Duyệt qua các URL duy nhất để xử lý
            int index = 1;
            for (String userUrl : uniqueUrls) {
                try {
                    // Lấy username từ URL
                    String username = userUrl.substring(userUrl.lastIndexOf("/") + 1);

                    // Kiểm tra xem thư mục của người dùng đã tồn tại chưa
                    String userDirectoryPath = "output" + File.separator + username;
                    File userDirectory = new File(userDirectoryPath);
                    if (userDirectory.exists()) {
                        System.out.println("Người dùng " + username + " đã tồn tại trong output. Bỏ qua.");
                        continue;
                    }

                    // Mở tab mới
                    openNewTab(userUrl);

                    // Xử lý tab mới (sử dụng TweetIDScraper và FollowsScraper)
                    handleUserTab(userUrl);

                    // Quay lại tab chính
                    switchToMainTab();

                    System.out.println("Đã xử lý xong người dùng thứ " + index + " và quay lại danh sách.");
                    index++;
                } catch (Exception e) {
                    System.out.println("Có lỗi khi xử lý người dùng thứ " + index);
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println("Không thể lấy danh sách người dùng.");
            e.printStackTrace();
        }
    }

    private void openNewTab(String url) {
        try {
            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
            jsExecutor.executeScript("window.open(arguments[0]);", url);
            System.out.println("Đã mở tab mới với URL: " + url);
        } catch (Exception e) {
            System.out.println("Lỗi khi mở tab mới với URL: " + url);
            e.printStackTrace();
        }
    }

    private void handleUserTab(String url) {
        try {
            for (String tabHandle : driver.getWindowHandles()) {
                driver.switchTo().window(tabHandle);
            }
            System.out.println("Đang xử lý tab mới với URL: " + url);

            String username = url.substring(url.lastIndexOf("/") + 1);
            String userDirectoryPath = "output" + File.separator + "Data" + File.separator + username;
            File userDirectory = new File(userDirectoryPath);
            if (!userDirectory.exists()) {
                userDirectory.mkdirs();
            }

            FollowsScraper followsScraper = new FollowsScraper(driver);

            followsScraper.scrapeData(username, userDirectoryPath);

            driver.navigate().back();
            driver.navigate().back();

            TweetIDScraper tweetIDScraper = new TweetIDScraper(driver);
            tweetIDScraper.scrapeData(username, userDirectoryPath);

            driver.close();
            System.out.println("Đã đóng tab với URL: " + url);
        } catch (Exception e) {
            System.out.println("Lỗi khi xử lý tab với URL: " + url);
            e.printStackTrace();
        }
    }

    private void switchToMainTab() {
        try {
            // Chuyển về tab chính
            for (String tabHandle : driver.getWindowHandles()) {
                driver.switchTo().window(tabHandle);
                break;
            }
            System.out.println("Đã chuyển về tab chính.");
        } catch (Exception e) {
            System.out.println("Lỗi khi chuyển về tab chính.");
            e.printStackTrace();
        }
    }
}