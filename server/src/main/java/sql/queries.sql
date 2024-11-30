-- GET_USER_TWEETS
SELECT u.username, t.tweetID
FROM Tweets t
        JOIN Users u ON t.username = u.username;

-- GET_USER_FOLLOWS
SELECT u1.username AS follower, u2.username AS following
FROM User_Follows uf
         JOIN Users u1 ON uf.follower = u1.username
         JOIN Users u2 ON uf.following = u2.username;

-- GET_TWEET_USER_INTERACTIONS
SELECT u.username, t.tweetID, ut.interactionType, ut.tweetQRID, ut.authorOrMed
FROM User_Tweets ut
         JOIN Users u ON ut.username = u.username
         JOIN Tweets t ON ut.tweetID = t.tweetID;

-- GET_ALL_USERS
SELECT username FROM Users;

-- GET_ALL_TWEETS
SELECT tweetID FROM Tweets;