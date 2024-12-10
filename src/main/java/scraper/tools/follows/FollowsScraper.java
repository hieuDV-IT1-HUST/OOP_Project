package scraper.tools.follows;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import scraper.tools.BaseScraper;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class FollowsScraper extends BaseScraper {
    private final WebDriverWait wait;

    public FollowsScraper(WebDriver driver) {
        super(driver);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    @Override
    public void scrapeData(String username, String outputPath) {
        scrapeAndSave("Followers", username, outputPath);
        scrapeAndSave("Following", username, outputPath);
    }

    private void scrapeAndSave(String type, String username, String userDirectoryPath) {
        List<String> userIDs;
        if ("Followers".equalsIgnoreCase(type)) {
            userIDs = getVerifiedFollowers();
        } else if ("Following".equalsIgnoreCase(type)) {
            userIDs = getVerifiedFollowing();
        } else {
            throw new IllegalArgumentException("Invalid type specified. Use 'Followers' or 'Following'.");
        }

        saveToJsonFile(username, type, userIDs, userDirectoryPath);
    }

    private List<String> getVerifiedFollowers() {
        return extractUsers("//span[text()='Followers']/ancestor::a",
                "div[class*='css-175oi2r r-kemksi r-1kqtdi0 r-1ua6aaf r-th6na r-1phboty r-16y2uox r-184en5c r-1abdc3e r-1lg4w6u r-f8sm7e r-13qz1uu r-1ye8kvj']",
                "//div[contains(@class, 'css-146c3p1 r-dnmrzs r-1udh08x r-3s2u2q r-bcqeeo r-1ttztb7 r-qvutc0 r-37j5jr r-a023e6 r-rjixqe r-16dba41 r-18u37iz r-1wvb978')]");
    }

    private List<String> getVerifiedFollowing() {
        return extractUsers("//span[text()='Following']/ancestor::a",
                "div[class*='css-175oi2r r-kemksi r-1kqtdi0 r-1ua6aaf r-th6na r-1phboty r-16y2uox r-184en5c r-1abdc3e r-1lg4w6u r-f8sm7e r-13qz1uu r-1ye8kvj']",
                "//div[contains(@class, 'css-146c3p1 r-dnmrzs r-1udh08x r-3s2u2q r-bcqeeo r-1ttztb7 r-qvutc0 r-37j5jr r-a023e6 r-rjixqe r-16dba41 r-18u37iz r-1wvb978')]");
    }

    private List<String> extractUsers(String tabXpath, String divCssSelector, String userXpath) {
        List<String> userIDs = new ArrayList<>();
        try {
            WebElement tab = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(tabXpath)));
            tab.click();
            Thread.sleep(5000); // Đợi nội dung tải

            int scrollCount = 0;
            int maxScroll = 5;
            while (scrollCount < maxScroll) {
                WebElement userDiv = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(divCssSelector)));
                List<WebElement> userElements = userDiv.findElements(By.xpath(userXpath));

                for (WebElement element : userElements) {
                    String userId = element.getText();
                    if (!userId.isEmpty() && !userIDs.contains(userId)) {
                        userIDs.add(userId);
                    }
                }
                scroll(1);
                scrollCount++;
            }
        } catch (Exception e) {
            System.err.println("Error extracting users: " + e.getMessage());
        }
        return userIDs;
    }

    private void saveToJsonFile(String username, String type, List<String> userIDs, String outputPath) {
        try {
            File directory = new File(outputPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String filename = outputPath + File.separator + type.toLowerCase() + ".json";

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), new FollowData(username, type, userIDs));

            System.out.println(type + " data saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error saving JSON: " + e.getMessage());
        }
    }
}