package others.config;

// AppConfig: Stores other configurations such as database connection information, API query limits.
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class AppConfig {
    private static String jdbcUrl;
    private static String dbUser;
    private static String dbPassword;
    private static String twitterUsername;
    private static String usernameOrPhone;
    private static String twitterPassword;
    private static String dbPath;
    private static String DSGAdjListPath;
    private static String owDSGAdjListPath;
    private static String queriesPath;
    private static String prPointsPath;
    private static String incPrPointsPath;
    private static String usernameDataPath;
    private static String userDataPath;
    private static List<String> blockchainKeywords;
    private static String ConsumerKey;
    private static String Consumer_Key_Secret;
    private static String Access_Token;
    private static String Access_Token_Secret;

    private static final Logger logger = LogManager.getLogger(AppConfig.class);

    // Load application.properties file to set configurations
    public static void loadProperties() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/resources/application.properties")) {
            properties.load(fis);

            jdbcUrl = properties.getProperty("spring.datasource.url");
            dbUser = properties.getProperty("spring.datasource.username");
            dbPassword = properties.getProperty("spring.datasource.password");

            dbPath = properties.getProperty("initialize_databasePath");
            DSGAdjListPath = properties.getProperty("directedSimpleGraphAdjListPath");
            owDSGAdjListPath = properties.getProperty("1-wayDirectedSimpleGraphAdjListPath");

            twitterUsername = properties.getProperty("twitter.username");
            usernameOrPhone = properties.getProperty("twitter.usernameOrPhone");
            twitterPassword = properties.getProperty("twitter.password");

            queriesPath = properties.getProperty("queriesPath");
            prPointsPath = properties.getProperty("PageRankOutputPath");
            incPrPointsPath = properties.getProperty("IncrementalPageRankOutputPath");
            usernameDataPath = properties.getProperty("usernameDataPath");
            userDataPath = properties.getProperty("userDataPath");
            blockchainKeywords = Arrays.asList("#blockchain", "#cryptocurrency", "#web3", "#NFT");

            ConsumerKey = properties.getProperty("oauth.ConsumerKey");
            Consumer_Key_Secret = properties.getProperty("oauth.Consumer_Key_Secret");
            Access_Token = properties.getProperty("oauth.Access_Token");
            Access_Token_Secret = properties.getProperty("oauth.Access_Token_Secret");

            if (jdbcUrl == null || dbUser == null || dbPassword == null) {
                throw new IllegalArgumentException("Missing required database configurations in application.properties");
            }
        } catch (IOException e) {
            logger.error("Failed to load properties from application.properties", e);
            throw new RuntimeException("Failed to load properties from application.properties", e);
        }
    }

    // get information connecting to database
    public static String getJdbcUrl() { return jdbcUrl; }
    public static String getDbUser() { return dbUser; }
    public static String getDbPassword() { return dbPassword; }

    // get userID and password
    public static String getTwitterUsername() { return twitterUsername; }
    public static String getUsernameOrPhone() { return usernameOrPhone; }
    public static String getTwitterPassword() { return twitterPassword; }

    // Get dbPath
    public static String getInitialize_databasePath() { return dbPath; }

    // Get AdjListPath
    public static String getDSGAdjListPath() { return DSGAdjListPath; }
    public static String getOwDSGAdjListPath() { return owDSGAdjListPath; }

    // Get queriesPath
    public static String getQueriesPath() { return queriesPath; }

    public static String getPageRankOutputPath() { return prPointsPath; }
    public static String getIncrementalPageRankOutputPath() { return incPrPointsPath; }
    public static String getUsernameDataPath() { return usernameDataPath; }
    public static String getUserDataPath() { return userDataPath; }
    public static List<String> getBlockchainKeywords() { return blockchainKeywords; }

    public static String getConsumerKey() { return ConsumerKey; }
    public static String getConsumer_Key_Secret() { return Consumer_Key_Secret; }

    public static String getAccess_Token() { return Access_Token; }
    public static String getAccess_Token_Secret() { return Access_Token_Secret; }
}