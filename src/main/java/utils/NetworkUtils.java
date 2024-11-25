package utils;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;

public class NetworkUtils {

    // Gửi yêu cầu GET tới một URL
    public static String sendGetRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000); // Thời gian chờ kết nối
        connection.setReadTimeout(5000);    // Thời gian chờ dữ liệu

        int status = connection.getResponseCode();
        if (status != 200) {
            throw new IOException("Error: Failed to fetch data from URL. Status code: " + status);
        }

        return readInputStream(connection);
    }

    // Gửi yêu cầu POST tới một URL
    public static String sendPostRequest(String urlString, String jsonBody) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true); // Cho phép gửi dữ liệu trong body của yêu cầu
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestProperty("Content-Type", "application/json");

        // Gửi dữ liệu
        connection.getOutputStream().write(jsonBody.getBytes("UTF-8"));

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
        return content.toString(); // Trả về dữ liệu nhận được dưới dạng chuỗi
    }

    // Xử lý lỗi mạng (chẳng hạn như timeout, không kết nối được)
    public static String handleNetworkError(IOException e) {
        // Có thể ghi log hoặc trả về thông báo lỗi tùy vào yêu cầu dự án
        return "Network error occurred: " + e.getMessage();
    }
}