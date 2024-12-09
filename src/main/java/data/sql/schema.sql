CREATE TABLE IF NOT EXISTS Users (
                                     userID SERIAL PRIMARY KEY,
                                     username VARCHAR(50) UNIQUE NOT NULL,
                                     displayName VARCHAR(100),
                                     followerCount INT DEFAULT 0,
                                     followingCount INT DEFAULT 0,
                                     bio TEXT,
                                     verified BOOLEAN DEFAULT FALSE,
                                     profileImageURL VARCHAR(255),
                                     createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     location VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS Tweets (
                                      tweetID VARCHAR(50) PRIMARY KEY,
                                      userID INT NOT NULL,
                                      content TEXT NOT NULL,
                                      createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      retweetCount INT DEFAULT 0,
                                      likeCount INT DEFAULT 0,
                                      replyCount INT DEFAULT 0,
                                      quoteCount INT DEFAULT 0,
                                      mediaURL VARCHAR(255),
                                      hashtags VARCHAR(255),
                                      language VARCHAR(10),
                                      FOREIGN KEY (userID) REFERENCES Users(userID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS User_Tweets (
                                           id SERIAL PRIMARY KEY,
                                           userID INT NOT NULL,
                                           tweetID VARCHAR(50) NOT NULL,
                                           tweetQuoteReplyID VARCHAR(50) DEFAULT NULL,
                                           authorOrMentionedID INT DEFAULT NULL,
                                           interactionType VARCHAR(10) CHECK (interactionType IN ('QUOTE', 'MENTION', 'RETWEET', 'REPLY')),
                                           interactionTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           FOREIGN KEY (userID) REFERENCES Users(userID) ON DELETE CASCADE,
                                           FOREIGN KEY (authorOrMentionedID) REFERENCES Users(userID) ON DELETE CASCADE,
                                           FOREIGN KEY (tweetID) REFERENCES Tweets(tweetID) ON DELETE CASCADE,
                                           FOREIGN KEY (tweetQuoteReplyID) REFERENCES Tweets(tweetID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Hashtags (
                                        hashtagID SERIAL PRIMARY KEY,
                                        text VARCHAR(255) UNIQUE NOT NULL,
                                        tweetCount INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS User_Follows (
                                            followerID INT NOT NULL,
                                            followedID INT NOT NULL,
                                            followTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                            PRIMARY KEY (followerID, followedID),
                                            FOREIGN KEY (followerID) REFERENCES Users(userID) ON DELETE CASCADE,
                                            FOREIGN KEY (followedID) REFERENCES Users(userID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Hashtag_Tweets (
                                              id SERIAL PRIMARY KEY,
                                              hashtagID INT NOT NULL,
                                              tweetID VARCHAR(50) NOT NULL,
                                              FOREIGN KEY (hashtagID) REFERENCES Hashtags(hashtagID) ON DELETE CASCADE,
                                              FOREIGN KEY (tweetID) REFERENCES Tweets(tweetID) ON DELETE CASCADE
);