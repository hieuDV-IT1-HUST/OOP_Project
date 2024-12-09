package scraper.login;

import config.AppConfig;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OAuthAuthenticator {
    private static final Logger logger = LogManager.getLogger(OAuthAuthenticator.class);

    public Twitter authenticate() {
        // Load OAuth credentials from AppConfig
        AppConfig.loadProperties();
        String consumerKey = AppConfig.getConsumerKey();
        String consumerSecret = AppConfig.getConsumer_Key_Secret();
        String accessToken = AppConfig.getAccess_Token();
        String accessTokenSecret = AppConfig.getAccess_Token_Secret();

        // Validate presence of OAuth credentials
        if (consumerKey == null || consumerSecret == null || accessToken == null || accessTokenSecret == null) {
            logger.error("OAuth credentials are missing in the configuration!");
            throw new IllegalStateException("Missing Twitter OAuth credentials!");
        }

        try {
            // Configure Twitter instance with OAuth credentials
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setOAuthConsumerKey(consumerKey)
                    .setOAuthConsumerSecret(consumerSecret)
                    .setOAuthAccessToken(accessToken)
                    .setOAuthAccessTokenSecret(accessTokenSecret);

            logger.info("Successfully configured Twitter OAuth credentials.");
            return new TwitterFactory(cb.build()).getInstance();
        } catch (Exception e) {
            logger.error("Failed to authenticate with Twitter API: {}", e.getMessage());
            throw new RuntimeException("Authentication failed", e);
        }
    }
}