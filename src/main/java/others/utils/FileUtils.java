package others.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;
import java.nio.file.*;
import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileUtils {
    private static final Logger logger = LogManager.getLogger(FileUtils.class);

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

    // Check if file exists
    public static boolean fileExists(String filePath) {
        boolean exists = Files.exists(Paths.get(filePath));
        logger.info("Checking if file exists: {} -> {}", filePath, exists);
        return exists;
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

    /**
     * Generalized method to read a JSON file and map it to a desired Java type.
     *
     * @param <T>      The type of the desired object
     * @param filePath The path to the JSON file
     * @param typeRef  The type reference for mapping JSON data
     * @return The mapped object of type T
     * @throws IOException If there is an error reading the file
     */
    public static <T> T readJsonFile(String filePath, TypeReference<T> typeRef) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new FileReader(filePath), typeRef);
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