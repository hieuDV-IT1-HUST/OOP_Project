package processor.dataprocessing;

import others.config.AppConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final Logger logger = LogManager.getLogger(DatabaseConnector.class);

    // Connect to database using AppConfig
    public static Connection connect() {
        AppConfig.loadProperties();

        try {
            Connection connection = DriverManager.getConnection(
                    AppConfig.getJdbcUrl(),
                    AppConfig.getDbUser(),
                    AppConfig.getDbPassword()
            );
//            logger.info("Connect to the database successfully.");
            return connection;
        } catch (SQLException e) {
            logger.error("Failed to connect to the database!", e);
            throw new RuntimeException("Failed to connect to the database!", e);
        }
    }
}