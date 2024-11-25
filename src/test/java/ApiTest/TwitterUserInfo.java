package ApiTest;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterV2;
import twitter4j.TwitterV2ExKt;
import twitter4j.UsersResponse;
import twitter4j.auth.AccessToken;

public final class TwitterUserInfo {

    public static void main(String[] args) throws TwitterException {
        // Tạo Twitter instance với thông tin OAuth
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer("X00aSiSM0bxJTYorsKVLT8g41", "cbVwaSmYv4IB3FxqoHOSK3OMslxCEQwvmu9Fj9CxLOr4gTvNEU");
        twitter.setOAuthAccessToken(new AccessToken("1843912793499807744-2i0oJw6fjqGFOCvaROIJ7GJ4apJwDz", "ot1szIGMR3eR7x1w4XRRbz7rRaPCnCTlOmrSxGDTf1ndL"));

        try {
            // Lấy thông tin về người đã retweet tweet
            final TwitterV2 v2 = TwitterV2ExKt.getV2(twitter);

            // Gọi getRetweetUsers
            final UsersResponse usersResponse = v2.getRetweetUsers(
                    1860566270942544333L,  // ID của Tweet
                    null,                 // Không yêu cầu mở rộng trường bổ sung
                    "id,text,created_at", // Các trường chi tiết tweet
                    "id,name,username"    // Các trường chi tiết người dùng
            );

            // In kết quả ra console
            System.out.println("Users who retweeted:");
            usersResponse.getUsers().forEach(user -> {
                System.out.println("Name: " + user.getName());
                System.out.println("Username: " + user.getScreenName());
            });

        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }
}