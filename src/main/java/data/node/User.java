package data.node;

public class User {
    private final String username;
    private final String displayName;
    private final int followerCount;
    private final int followingCount;
    private final String bio;
    private final boolean verified;
    private final String profileImageURL;
    private final Object createdAt;
    private final String fromHashtag;
    private final String location;

    // Constructor
    public User(String username, String displayName, int followerCount, int followingCount, String bio,
                boolean verified, String profileImageURL, Object createdAt, String fromHashtag, String location) {
        this.username = username;
        this.displayName = displayName;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
        this.bio = bio;
        this.verified = verified;
        this.profileImageURL = profileImageURL;
        this.createdAt = createdAt;
        this.fromHashtag = fromHashtag;
        this.location = location;
    }

    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
    public int getFollowerCount() { return followerCount; }
    public int getFollowingCount() { return followingCount; }
    public String getBio() { return bio; }
    public boolean isVerified() { return verified; }
    public String getProfileImageURL() { return profileImageURL; }
    public Object getCreatedAt() { return createdAt; }
    public String getFromHashtag() { return fromHashtag; }
    public String getLocation() { return location; }
}