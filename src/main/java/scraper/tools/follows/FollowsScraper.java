package scraper.tools.follows;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class FollowsScraper {
    private WebDriver driver;
    private WebDriverWait wait;

    public FollowsScraper(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    public void scrapeAndSave(String type, String username, String userDirectoryPath) {
        List<String> userIDs;
        if ("Followers".equalsIgnoreCase(type)) {
            userIDs = getVerifiedFollowers();
        } else if ("Following".equalsIgnoreCase(type)) {
            userIDs = getFollowing();
        } else {
            throw new IllegalArgumentException("Invalid type specified. Use 'Followers' or 'Following'.");
        }

        // Lưu thông tin vào file JSON
        saveToJsonFile(username, type, userIDs, userDirectoryPath);
    }

    private List<String> getVerifiedFollowers() {
        List<String> followersIDs = new ArrayList<>();
        try {
            // Điều hướng đến tab "Người theo dõi"
            WebElement followersTab = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//span[text()='Followers']/ancestor::a")
            ));
            followersTab.click();
            Thread.sleep(5000); // Đợi trang tải

            scrollToBottom(5); // Cuộn trang để tải đầy đủ nội dung

            // Lấy danh sách ID từ div chứa người theo dõi
            WebElement followersDiv = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div[class*='css-175oi2r r-kemksi r-1kqtdi0 r-1ua6aaf r-th6na r-1phboty r-16y2uox r-184en5c r-1abdc3e r-1lg4w6u r-f8sm7e r-13qz1uu r-1ye8kvj']")
            ));

            List<WebElement> followersIDList = followersDiv.findElements(By.xpath(
                    "//div[contains(@class, 'css-146c3p1 r-dnmrzs r-1udh08x r-3s2u2q r-bcqeeo r-1ttztb7 r-qvutc0 r-37j5jr r-a023e6 r-rjixqe r-16dba41 r-18u37iz r-1wvb978')]"
            ));

            for (WebElement idElement : followersIDList) {
                String id = idElement.getText();
                if (!id.isEmpty()) {
                    followersIDs.add(id);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách người theo dõi: " + e.getMessage());
            e.printStackTrace();
        }
        return followersIDs;
    }

    private List<String> getFollowing() {
        List<String> followingIDs = new ArrayList<>();
        try {
            // Điều hướng đến tab "Người đang theo dõi"
            WebElement followingTab = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//span[text()='Following']/ancestor::a")
            ));
            followingTab.click();
            Thread.sleep(5000); // Đợi trang tải

            scrollToBottom(5); // Cuộn trang để tải đầy đủ nội dung

            // Lấy danh sách ID từ div chứa người đang theo dõi
            WebElement followingsDiv = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div[class*='css-175oi2r r-kemksi r-1kqtdi0 r-1ua6aaf r-th6na r-1phboty r-16y2uox r-184en5c r-1abdc3e r-1lg4w6u r-f8sm7e r-13qz1uu r-1ye8kvj']")
            ));

            List<WebElement> followingsIDList = followingsDiv.findElements(By.xpath(
                    "//div[contains(@class, 'css-146c3p1 r-dnmrzs r-1udh08x r-3s2u2q r-bcqeeo r-1ttztb7 r-qvutc0 r-37j5jr r-a023e6 r-rjixqe r-16dba41 r-18u37iz r-1wvb978')]"
            ));

            for (WebElement idElement : followingsIDList) {
                String id = idElement.getText();
                if (!id.isEmpty()) {
                    followingIDs.add(id);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách người đang theo dõi: " + e.getMessage());
            e.printStackTrace();
        }
        return followingIDs;
    }

    private void scrollToBottom(int maxScrollRetries) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        int retries = 0;
        long lastHeight = (long) js.executeScript("return document.body.scrollHeight");

        while (retries < maxScrollRetries) {
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            try {
                Thread.sleep(1000); // Đợi trang tải nội dung
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long newHeight = (long) js.executeScript("return document.body.scrollHeight");
            if (newHeight == lastHeight) {
                retries++;
            } else {
                retries = 0;
            }
            lastHeight = newHeight;
        }
    }


    private void saveToJsonFile(String username, String type, List<String> userIDs, String userDirectoryPath) {
        try {
            File directory = new File(userDirectoryPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String filename = userDirectoryPath + File.separator + type.toLowerCase() + ".json";

            FollowData data = new FollowData("@" + username, type, userIDs);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), data);

            System.out.println("Thông tin " + type + " đã được lưu vào file: " + filename);
        } catch (IOException e) {
            System.err.println("Lỗi khi lưu file JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static class FollowData {
        public String username;
        public String type;
        public List<String> userIDs;

        public FollowData(String username, String type, List<String> userIDs) {
            this.username = username;
            this.type = type;
            this.userIDs = userIDs;
        }
    }
}