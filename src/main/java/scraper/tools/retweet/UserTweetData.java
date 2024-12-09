package scraper.tools.retweet;

import java.util.List;

public class UserTweetData {
    private String username;
    private List<String> tweetIDs;

    public UserTweetData(String username, List<String> tweetIDs) {
        this.username = username;
        this.tweetIDs = tweetIDs;
    }
}