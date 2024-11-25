package utils;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class NetworkUtils {

    // Send a GET request to a URL
    public static String sendGetRequest(String urlString) throws Exception {
        // Create URI from URL string
        URI uri = new URI(urlString);

        // Convert URI to URL
        URL url = uri.toURL();

        // Open HTTP connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000); // Time to wait for connection
        connection.setReadTimeout(5000);    // Time to wait for data

        // Check HTTP status code
        int status = connection.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            throw new IOException("Error: Failed to fetch data from URL. Status code: " + status);
        }

        // Read data from InputStream
        return readInputStream(connection);
    }

    // Gửi yêu cầu POST tới một URL
    public static String sendPostRequest(String urlString, String jsonBody) throws Exception {
        URI uri = new URI(urlString);
        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true); // Cho phép gửi dữ liệu trong body của yêu cầu
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestProperty("Content-Type", "application/json");

        // Gửi dữ liệu
        connection.getOutputStream().write(jsonBody.getBytes(StandardCharsets.UTF_8));

        int status = connection.getResponseCode();
        if (status != 200) {
            throw new IOException("Error: Failed to send data. Status code: " + status);
        }

        return readInputStream(connection);
    }

    @NotNull
    private static String readInputStream(HttpURLConnection connection) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        return content.toString(); // Returns the received data as a string
    }

    // Handling network errors (such as timeouts, no connection)
    public static String handleNetworkError(IOException e) {
        // Log or return error messages depending on project requirements
        return "Network error occurred: " + e.getMessage();
    }
}