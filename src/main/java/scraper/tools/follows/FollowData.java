package scraper.tools.follows;

import java.util.List;

public class FollowData {
    public String username;
    public String type;
    public List<String> userIDs;

    public FollowData(String username, String type, List<String> userIDs) {
        this.username = username;
        this.type = type;
        this.userIDs = userIDs;
    }
}
