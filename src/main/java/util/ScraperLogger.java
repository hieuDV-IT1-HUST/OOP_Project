package util;

import java.util.logging.Logger;

public class ScraperLogger {
    private static final Logger logger = Logger.getLogger(ScraperLogger.class.getName());

    public void logInfo(String message) {
        logger.info(message);
    }

    public void logError(String message) {
        logger.severe(message);
    }
}
