package processor.data.node;

public record User(
        String username,
        String displayName,
        int followerCount,
        int followingCount,
        String bio,
        boolean verified,
        String profileImageURL,
        Object createdAt,
        String fromHashtag,
        String location
) {}