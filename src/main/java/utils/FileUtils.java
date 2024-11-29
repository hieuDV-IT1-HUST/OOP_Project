package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileUtils {
    private static final Logger logger = LogManager.getLogger(FileUtils.class);

    // Read data from file
    public static String readFile(String filePath) throws IOException {
        logger.info("Reading data from file: {}", filePath);
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    // Write data to file
    public static void writeFile(String filePath, String data) {
        try {
            ensureParentDirectoryExists(filePath);
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
                writer.write(data);
                logger.info("Data is written to file: {}", filePath);
            }
        } catch (IOException e) {
            logger.error("Failed to write to file: {}", filePath, e);
            throw new RuntimeException("Failed to write to file: " + filePath, e);
        }
    }

    // Read text file and return list of lines
    public static List<String> readLines(String filePath) throws IOException {
        logger.info("Reading list from file: {}", filePath);
        return Files.readAllLines(Paths.get(filePath));
    }

    // Write a list of lines to a file
    public static void writeLines(String filePath, List<String> lines) {
        try {
            ensureParentDirectoryExists(filePath);
            Files.write(Paths.get(filePath), lines);
            logger.info("The list of lines is written to file: {}", filePath);
        } catch (IOException e) {
            logger.error("Failed to write lines to file: {}", filePath, e);
            throw new RuntimeException("Failed to write lines to file: " + filePath, e);
        }
    }

    // Check if file exists
    public static boolean fileExists(String filePath) {
        boolean exists = Files.exists(Paths.get(filePath));
        logger.info("Checking if file exists: {} -> {}", filePath, exists);
        return exists;
    }

    // Delete file
    public static void deleteFile(String filePath) {
        try {
            Files.delete(Paths.get(filePath));
            logger.info("Deleted file: {}", filePath);
        } catch (IOException e) {
            logger.error("Failed to delete file: {}", filePath, e);
            throw new RuntimeException("Failed to delete file: " + filePath, e);
        }
    }

    // Write object as JSON to file
    public static void writeJsonToFile(String filePath, Object object) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            ensureParentDirectoryExists(filePath);

            if (!FileUtils.fileExists(filePath)) {
                FileUtils.writeFile(filePath, ""); // Write empty file
                logger.info("File did not exist and was created: {}", filePath);
            }

            mapper.writeValue(new File(filePath), object);
            logger.info("The Object was written in JSON to file: {}", filePath);
        } catch (IOException e) {
            logger.error("Failed to write JSON to file: {}", filePath, e);
            throw new RuntimeException("Failed to write JSON to file: " + filePath, e);
        }
    }

    // Check and create parent directory from file path
    public static void ensureParentDirectoryExists(String filePath) throws IOException {
        Path parentDir = Paths.get(filePath).getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
            logger.info("Parent directory was created for file: {}", filePath);
        }
    }
}