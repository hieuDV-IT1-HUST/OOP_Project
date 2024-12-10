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
            // Find all profile's links
            List<WebElement> users = driver.findElements(By.xpath(
                "//a[contains(@href, '/') and contains(@class, 'r-1wbh5a2')]"
            ));

            if (users.isEmpty()) {
                System.out.println("No user found.");
                return;
            }

            System.out.println("Found " + users.size() + " users in the list.");

            // Get URL
            Set<String> uniqueUrls = new HashSet<>();
            for (WebElement user : users) {
                String href = user.getAttribute("href");
                if (href != null && !href.isEmpty()) {
                    uniqueUrls.add(href.startsWith("http") ? href : "https://twitter.com" + href);
                }
            }

            System.out.println("Found " + uniqueUrls.size() + " URL in the list.");

            // Sparse URL
            int index = 1;
            for (String userUrl : uniqueUrls) {
                try {
                    // Get username
                    String username = userUrl.substring(userUrl.lastIndexOf("/") + 1);

                    String userDirectoryPath = "output" + File.separator + username;
                    File userDirectory = new File(userDirectoryPath);
                    if (userDirectory.exists()) {
                        System.out.println("User " + username + " has existed. Skipping...");
                        continue;
                    }

                    // Open new tab
                    openNewTab(userUrl);

                    // Handling new tab
                    handleUserTab(userUrl);

                    // Back to Main tab
                    switchToMainTab();

                    System.out.println("Processed user " + index + " and back to user list.");
                    index++;
                } catch (Exception e) {
                    System.out.println("Error processing user " + index);
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println("Cannot get user list.");
            e.printStackTrace();
        }
    }

    private void openNewTab(String url) {
        try {
            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
            jsExecutor.executeScript("window.open(arguments[0]);", url);
            System.out.println("Open URL: " + url);
        } catch (Exception e) {
            System.out.println("Error open URL: " + url);
            e.printStackTrace();
        }
    }

    private void handleUserTab(String url) {
        try {
            for (String tabHandle : driver.getWindowHandles()) {
                driver.switchTo().window(tabHandle);
            }
            System.out.println("Handling URL: " + url);

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
            System.out.println("Close URL: " + url);
        } catch (Exception e) {
            System.out.println("Error when processing URL: " + url);
            e.printStackTrace();
        }
    }

    private void switchToMainTab() {
        try {
            // Back to main tab
            for (String tabHandle : driver.getWindowHandles()) {
                driver.switchTo().window(tabHandle);
                break;
            }
            System.out.println("Back to main tab.");
        } catch (Exception e) {
            System.out.println("Error when back to main tab.");
            e.printStackTrace();
        }
    }
}