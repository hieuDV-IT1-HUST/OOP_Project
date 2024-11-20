package util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class DataSaver {
    public static void saveToJSON(List<String> data, String fileName) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new java.io.File(fileName), data);
            System.out.println("Data saved to " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}