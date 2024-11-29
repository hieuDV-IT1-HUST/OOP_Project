package config;

// AppConfig: Stores other configurations such as database connection information, API query limits.
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public class AppConfig {
    private static String jdbcUrl;
    private static String dbUser;
    private static String dbPassword;
    private static String twitterUsername;
    private static String twitterPassword;
    private static String dbPath;
    private static String DSGAdjListPath;
    private static String owDSGAdjListPath;
    private static int apiQueryLimit;
    private static String queriesPath;
    private static String prPointsPath;

    private static final Logger logger = LogManager.getLogger(AppConfig.class);

    // Load application.properties file to set configurations
    public static void loadProperties() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/resources/application.properties")) {
            properties.load(fis);

            jdbcUrl = properties.getProperty("spring.datasource.url");
            dbUser = properties.getProperty("spring.datasource.username");
            dbPassword = properties.getProperty("spring.datasource.password");

            apiQueryLimit = Integer.parseInt(properties.getProperty("api.query.limit", "1000"));
            dbPath = properties.getProperty("initialize_databasePath");
            DSGAdjListPath = properties.getProperty("directedSimpleGraphAdjListPath");
            owDSGAdjListPath = properties.getProperty("1-wayDirectedSimpleGraphAdjListPath");
            queriesPath = properties.getProperty("queriesPath");
            prPointsPath = properties.getProperty("PageRankOutputPath");

            if (jdbcUrl == null || dbUser == null || dbPassword == null) {
                throw new IllegalArgumentException("Missing required database configurations in application.properties");
            }
        } catch (IOException e) {
            logger.error("Failed to load properties from application.properties", e);
            throw new RuntimeException("Failed to load properties from application.properties", e);
        }
    }

    // get information connecting to database
    public static String getJdbcUrl() {
        return jdbcUrl;
    }

    public static String getDbUser() {
        return dbUser;
    }

    public static String getDbPassword() {
        return dbPassword;
    }

    // get username and password
    public static String getTwitterUsername() {
        return twitterUsername;
    }

    public static String getTwitterPassword() {
        return twitterPassword;
    }

    // Set Twitter account
    public static void setTwitterUsername(String username) {
        twitterUsername = username;
    }

    public static void setTwitterPassword(String password) {
        twitterPassword = password;
    }

    // Get API limit
    public static int getApiQueryLimit() {
        return apiQueryLimit;
    }

    // Get dbPath
    public static String getInitialize_databasePath() { return dbPath; }

    // Get AdjListPath
    public static String getDSGAdjListPath() { return DSGAdjListPath; }
    public static String getOwDSGAdjListPath() { return owDSGAdjListPath; }

    // Get queriesPath
    public static String getQueriesPath() { return queriesPath; }

    public static String getPageRankOutputPath() { return prPointsPath; }
}