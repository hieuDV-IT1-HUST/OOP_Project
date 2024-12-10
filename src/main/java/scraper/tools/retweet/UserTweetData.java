package scraper.tools.retweet;

import java.util.List;

public class UserTweetData {
    private String username;
    private List<String> tweetIDs;

    public UserTweetData(String username, List<String> tweetIDs) {
        this.username = username;
        this.tweetIDs = tweetIDs;
    }

    // Getter và Setter
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getTweetIDs() {
        return tweetIDs;
    }

    public void setTweetIDs(List<String> tweetIDs) {
        this.tweetIDs = tweetIDs;
    }
}