package processor.data.node;

import java.time.LocalDateTime;

public record User_Tweet(
        String username,
        long tweetID,
        Long tweetQuoteReplyID,
        String authorOrMentioned,
        String interactionType,
        LocalDateTime interactionTime
) {}