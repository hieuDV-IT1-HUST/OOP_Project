package processor.data.node;

import java.time.LocalDateTime;

public record Tweet(
        long tweetID,
        String username,
        String content,
        LocalDateTime createdAt,
        int retweetCount,
        int likeCount,
        int replyCount,
        int viewCount,
        String mediaURL,
        String hashtags,
        String language
) {}