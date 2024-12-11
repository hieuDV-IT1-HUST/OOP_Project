package com.example.src.java.com.example.visulizer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class VisualizerApplication {
    public static void main(String[] args) {
        SpringApplication.run(VisualizerApplication.class, args);
    }
}

@RestController
@RequestMapping("/api")
class GraphController {

    private final Map<String, Double> userData;
    private final double averagePageRank;

    public GraphController() throws IOException {
        // Load data from JSON file
        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, String> data = objectMapper.readValue(
                new File("output/PageRankPoints/pageRankPoints.json"),
                new TypeReference<Map<String, String>>() {}
        );

        // Convert and filter data
        this.userData = data.entrySet().stream()
                .filter(entry -> !entry.getKey().startsWith("T"))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Double.parseDouble(entry.getValue())
                ));

        // Calculate average PageRank
        this.averagePageRank = userData.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    @GetMapping("/nodes")
    public List<Map<String, Object>> getNodes() {
        return userData.entrySet().stream()
                .filter(entry -> entry.getValue() > averagePageRank)
                .map(entry -> Map.of(
                        "data", Map.of(
                                "id", entry.getKey(),
                                "label", entry.getKey(),
                                "pagerank", entry.getValue()
                        ),
                        "classes", "node"
                ))
                .collect(Collectors.toList());
    }

    @GetMapping("/edges")
    public List<Map<String, Map<String, String>>> getEdges() {
        List<String> keys = new ArrayList<>(userData.keySet());

        return keys.stream()
                .limit(keys.size() - 1)
                .map(source -> Map.of(
                        "data", Map.of(
                                "source", source,
                                "target", keys.get(keys.indexOf(source) + 1)
                        )
                ))
                .filter(edge -> userData.containsKey(edge.get("data").get("source"))
                        && userData.containsKey(edge.get("data").get("target")))
                .collect(Collectors.toList());
    }

    @GetMapping("/average-pagerank")
    public double getAveragePageRank() {
        return averagePageRank;
    }
}

