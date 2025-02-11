-- GET_USER_TWEETS
SELECT t.tweetID, u.username, t.createdAt
FROM Tweets t
         JOIN Users u ON t.username = u.username;
-- GET_USER_FOLLOWS
SELECT u1.username AS follower, u2.username AS followed
FROM User_Follows uf
         JOIN Users u1 ON uf.follower = u1.username
         JOIN Users u2 ON uf.followed = u2.username;

-- GET_TWEET_USER_INTERACTIONS
SELECT t.tweetID, ut.username, ut.interactionType, ut.interactionTime, ut.tweetQuoteReplyID, ut.authorOrMentioned
FROM User_Tweets ut
         JOIN Users u ON ut.username = u.username
         JOIN Tweets t ON ut.tweetID = t.tweetID
         JOIN Users uam ON ut.authorOrMentioned = uam.username;

-- GET_ALL_USERS
SELECT username, fromHashtag FROM Users;

-- GET_ALL_TWEETS
SELECT tweetID FROM Tweets;

-- INSERT_USERS
INSERT INTO Users (
    username, displayName, followerCount, followingCount, bio, verified,
                   profileImageURL, createdAt, fromHashtag, location)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
ON DUPLICATE KEY UPDATE
    displayName = VALUES(displayName),
    followerCount = VALUES(followerCount),
    followingCount = VALUES(followingCount),
    bio = VALUES(bio),
    verified = VALUES(verified),
    profileImageURL = VALUES(profileImageURL),
    createdAt = VALUES(createdAt),
    fromHashtag = IF(fromHashtag IS NULL, VALUES(fromHashtag), fromHashtag),
    location = VALUES(location);
-- INSERT_TWEETS
INSERT INTO Tweets (
    tweetID, username, content, createdAt, retweetCount, likeCount,
                    replyCount, viewCount, mediaURL, hashtags, language)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
ON DUPLICATE KEY UPDATE
    content = VALUES(content),
    createdAt = VALUES(createdAt),
    retweetCount = VALUES(retweetCount),
    likeCount = VALUES(likeCount),
    replyCount = VALUES(replyCount),
    viewCount = VALUES(viewCount),
    mediaURL = VALUES(mediaURL),
    hashtags = VALUES(hashtags),
    language = VALUES(language);
-- INSERT_USER_TWEETS
INSERT INTO User_Tweets (
    username,
    tweetID,
    tweetQuoteReplyID,
    authorOrMentioned,
    interactionType,
    interactionTime
) VALUES (?, ?, ?, ?, ?, ?)
ON DUPLICATE KEY UPDATE
    tweetQuoteReplyID = VALUES(tweetQuoteReplyID),
    authorOrMentioned = VALUES(authorOrMentioned),
    interactionTime = VALUES(interactionTime);
-- INSERT_HASHTAGS
INSERT INTO Hashtags (
    text,
    tweetCount
) VALUES (?, ?)
ON DUPLICATE KEY UPDATE
    tweetCount = tweetCount + VALUES(tweetCount);
-- INSERT_USER_FOLLOWS
INSERT INTO User_Follows (
    follower,
    followed,
    followTime
) VALUES (?, ?, ?)
ON DUPLICATE KEY UPDATE
    followTime = VALUES(followTime);
-- INSERT_HASHTAG_TWEETS
INSERT INTO Hashtag_Tweets (
    hashtagID,
    tweetID
) VALUES (?, ?)
-- QUERY_USERID_BY_USERNAME