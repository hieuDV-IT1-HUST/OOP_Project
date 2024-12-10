package scraper.tools;

public interface IScraper {
    void scrapeData(String username, String outputPath); // Triển khai chung cho các loại scraper
}
