CREATE TABLE IF NOT EXISTS Users (
    username VARCHAR(50) PRIMARY KEY NOT NULL,
    displayName VARCHAR(100),
    followerCount INT DEFAULT 0,
    followingCount INT DEFAULT 0,
    bio TEXT,
    verified BOOLEAN DEFAULT FALSE,
    profileImageURL VARCHAR(255),
    createdAt DATETIME,
    fromHashtag VARCHAR(255),
    location VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS Tweets (
    tweetID BIGINT PRIMARY KEY NOT NULL,
    username VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    createdAt DATETIME,
    retweetCount INT DEFAULT 0,
    likeCount INT DEFAULT 0,
    replyCount INT DEFAULT 0,
    viewCount INT DEFAULT 0,
    mediaURL VARCHAR(255),
    hashtags VARCHAR(255),
    language VARCHAR(10),
    FOREIGN KEY (username) REFERENCES Users(username) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS User_Tweets (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50),
    tweetID BIGINT,
    tweetQuoteReplyID BIGINT DEFAULT NULL,
    authorOrMentioned VARCHAR(50) DEFAULT NULL,
    interactionType ENUM('QUOTE', 'MENTION', 'RETWEET', 'REPLY'),
    interactionTime DATETIME,
    FOREIGN KEY (username) REFERENCES Users(username) ON DELETE CASCADE,
    FOREIGN KEY (authorOrMentioned) REFERENCES Users(username) ON DELETE CASCADE,
    FOREIGN KEY (tweetID) REFERENCES Tweets(tweetID) ON DELETE CASCADE,
    FOREIGN KEY (tweetQuoteReplyID) REFERENCES Tweets(tweetID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Hashtags (
    hashtagID INT PRIMARY KEY AUTO_INCREMENT,
    text VARCHAR(255) UNIQUE NOT NULL,
    tweetCount INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS User_Follows (
    follower VARCHAR(50),
    followed VARCHAR(50),
    followTime DATETIME,
    PRIMARY KEY (follower, followed),
    FOREIGN KEY (follower) REFERENCES Users(username) ON DELETE CASCADE,
    FOREIGN KEY (followed) REFERENCES Users(username) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Hashtag_Tweets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    hashtagID INT NOT NULL,
    tweetID BIGINT NOT NULL,
    FOREIGN KEY (hashtagID) REFERENCES Hashtags(hashtagID) ON DELETE CASCADE,
    FOREIGN KEY (tweetID) REFERENCES Tweets(tweetID) ON DELETE CASCADE
);