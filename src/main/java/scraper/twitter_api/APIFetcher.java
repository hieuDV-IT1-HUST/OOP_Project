package scraper.twitter_api;

import scraper.login.OAuthAuthenticator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitter4j.*;
import twitter4j.TwitterException;
import utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

public class APIFetcher {
    private static final Logger logger = LogManager.getLogger(APIFetcher.class);
    private final Twitter twitter;

    public APIFetcher() {
        OAuthAuthenticator oAuthAuthenticator = new OAuthAuthenticator();
        this.twitter = oAuthAuthenticator.authenticate();
    }

    /**
     * Fetch KOLs based on hashtag search.
     */
    public List<User> fetchKOLs(String hashtag, int maxResults) {
        List<User> kolList = new ArrayList<>();
        try {
            Query query = new Query("#" + hashtag);
            query.setCount(maxResults);
            QueryResult result = twitter.search(query);

            for (Status status : result.getTweets()) {
                User user = status.getUser();
                if (!kolList.contains(user)) {
                    kolList.add(user);
                }
            }
            logger.info("Fetched {} KOLs for hashtag #{}", kolList.size(), hashtag);
        } catch (TwitterException e) {
            logger.error("Failed to fetch KOLs for hashtag #{}: {}", hashtag, e.getMessage());
        }
        return kolList;
    }

    /**
     * Fetch followers for a given user.
     */
    public List<User> fetchFollowers(String username, int maxFollowers) {
        List<User> followers = new ArrayList<>();
        try {
            long cursor = -1;
            int count = 0;
            while (count < maxFollowers) {
                PagableResponseList<User> followersList = twitter.getFollowersList(username, cursor);
                followers.addAll(followersList);
                count += followersList.size();
                cursor = followersList.getNextCursor();
                if (cursor == 0) break;
            }
            logger.info("Fetched {} followers for user {}", followers.size(), username);
        } catch (TwitterException e) {
            logger.error("Failed to fetch followers for user {}: {}", username, e.getMessage());
        }
        return followers;
    }

    /**
     * Fetch tweets for a given user.
     */
    public List<Status> fetchTweets(String username, int maxTweets) {
        List<Status> tweets = new ArrayList<>();
        try {
            int page = 1;
            while (tweets.size() < maxTweets) {
                Paging paging = new Paging(page++, 100);
                List<Status> tweetBatch = twitter.getUserTimeline(username, paging);
                if (tweetBatch.isEmpty()) break;
                tweets.addAll(tweetBatch);
            }
            logger.info("Fetched {} tweets for user {}", tweets.size(), username);
        } catch (TwitterException e) {
            logger.error("Failed to fetch tweets for user {}: {}", username, e.getMessage());
        }
        return tweets;
    }

    /**
     * Fetch detailed user data for a given username.
     */
    public User fetchUserDetails(String username) {
        try {
            User user = twitter.showUser(username);
            logger.info("Fetched user details for {}", username);
            return user;
        } catch (TwitterException e) {
            logger.error("Failed to fetch user details for {}: {}", username, e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        APIFetcher fetcher = new APIFetcher();

        // Tìm KOLs từ hashtag
        List<User> kolList = fetcher.fetchKOLs("blockchain", 10);

        // Get followers and tweets data from first KOL
        if (!kolList.isEmpty()) {
            User kol = kolList.getFirst();
            User user = fetcher.fetchUserDetails(kol.getScreenName());
            List<User> followers = fetcher.fetchFollowers(kol.getScreenName(), 10);
            List<Status> tweets = fetcher.fetchTweets(kol.getScreenName(), 10);

            // Save data to JSON file
            FileUtils.writeJsonToFile("kol.json", kol);
            FileUtils.writeJsonToFile("user.json", user);
            FileUtils.writeJsonToFile("followers.json", followers);
            FileUtils.writeJsonToFile("tweets.json", tweets);
        }
    }
}