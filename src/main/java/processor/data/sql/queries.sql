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
         JOIN Users uam ON ut.authorOrMentionedID = uam.userID;

-- GET_ALL_USERS
SELECT userID, username, fromHashtag FROM Users;

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
    tweetID, userID, content, createdAt, retweetCount, likeCount,
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
    userID,
    tweetID,
    tweetQuoteReplyID,
    authorOrMentionedID,
    interactionType,
    interactionTime
) VALUES (?, ?, ?, ?, ?, ?)
ON DUPLICATE KEY UPDATE
    tweetQuoteReplyID = VALUES(tweetQuoteReplyID),
    authorOrMentionedID = VALUES(authorOrMentionedID),
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
    followerID,
    followedID,
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