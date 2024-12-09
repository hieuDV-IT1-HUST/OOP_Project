-- QUERY_USERID_BY_USERNAME
SELECT userID
FROM Users
WHERE username = ?;

-- GET_USER_TWEETS
SELECT u.userID, t.tweetID, u.username
FROM Tweets t
         JOIN Users u ON t.userID = u.userID;

-- GET_USER_FOLLOWS
SELECT u1.userID AS follower, u2.userID AS followed,
       u1.username AS followUser, u2.username AS followedUser
FROM User_Follows uf
         JOIN Users u1 ON uf.followerID = u1.userID
         JOIN Users u2 ON uf.followedID = u2.userID;

-- GET_TWEET_USER_INTERACTIONS
SELECT u.userID, t.tweetID, ut.interactionType, ut.tweetQuoteReplyID, ut.authorOrMentionedID,
       u.username AS interactUser, uam.username AS authorOrMentionedUser
FROM User_Tweets ut
         JOIN Users u ON ut.userID = u.userID
         JOIN Tweets t ON ut.tweetID = t.tweetID
         LEFT JOIN Users uam ON ut.authorOrMentionedID = uam.userID;

-- GET_ALL_USERS
SELECT userID, username FROM Users;

-- GET_ALL_TWEETS
SELECT tweetID FROM Tweets;

-- INSERT_USERS_TABLE
INSERT INTO Users (username, displayName, followerCount, followingCount, bio, verified, profileImageURL, createdAt, location)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
ON CONFLICT (username) DO UPDATE
    SET displayName = EXCLUDED.displayName,
        followerCount = EXCLUDED.followerCount,
        followingCount = EXCLUDED.followingCount,
        bio = EXCLUDED.bio,
        verified = EXCLUDED.verified,
        profileImageURL = EXCLUDED.profileImageURL,
        createdAt = EXCLUDED.createdAt,
        location = EXCLUDED.location;

-- INSERT_TWEETS_TABLE
INSERT INTO Tweets (tweetID, userID, content, createdAt, retweetCount, likeCount, replyCount, quoteCount, mediaURL, hashtags, language)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
ON CONFLICT (tweetID) DO UPDATE
    SET content = EXCLUDED.content,
        createdAt = EXCLUDED.createdAt,
        retweetCount = EXCLUDED.retweetCount,
        likeCount = EXCLUDED.likeCount,
        replyCount = EXCLUDED.replyCount,
        quoteCount = EXCLUDED.quoteCount,
        mediaURL = EXCLUDED.mediaURL,
        hashtags = EXCLUDED.hashtags,
        language = EXCLUDED.language;

-- INSERT_USER_FOLLOWS_TABLE
INSERT INTO User_Follows (followerID, followedID, followTime)
VALUES (?, ?, ?)
ON CONFLICT (followerID, followedID) DO NOTHING;

-- INSERT_USER_TWEETS_TABLE
INSERT INTO User_Tweets (userID, tweetID, tweetQuoteReplyID, authorOrMentionedID, interactionType, interactionTime)
VALUES (?, ?, ?, ?, ?, ?)
ON CONFLICT (id) DO UPDATE
    SET tweetQuoteReplyID = EXCLUDED.tweetQuoteReplyID,
        authorOrMentionedID = EXCLUDED.authorOrMentionedID,
        interactionType = EXCLUDED.interactionType,
        interactionTime = EXCLUDED.interactionTime;

-- INSERT_HASHTAGS_TABLE
INSERT INTO Hashtags (text, tweetCount)
VALUES (?, ?)
ON CONFLICT (text) DO UPDATE
    SET tweetCount = EXCLUDED.tweetCount;

-- INSERT_HASHTAGS_TWEETS_TABLE
INSERT INTO Hashtag_Tweets (hashtagID, tweetID)
VALUES (?, ?)
ON CONFLICT (id) DO NOTHING;

