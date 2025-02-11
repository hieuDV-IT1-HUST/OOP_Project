package processor.pagerank.adjacency_list_builder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ComputeWeight {
    /**
     * Compute weight for an edge
     * @param type a type of edge considering
     * @param interactionType an interaction between 2 nodes
     * @param interactionTime the timestamp of the interaction (DATETIME format)
     * @return weight
     */
    public static double computeWeight(String type, String interactionType, LocalDateTime interactionTime) {
        double baseWeight = 1.0;

        switch (type) {
            case "User -> User" -> {
                switch (interactionType) {
                    case "FOLLOW+" -> baseWeight = 7.0;
                    case "FOLLOW-" -> baseWeight = 2.0;
                    case "REPLY", "QUOTE" -> baseWeight = 5.0;
                    case "RETWEET" -> baseWeight = 5.1;
                }
            }
            case "User -> Tweet" -> {
                switch (interactionType) {
                    case "POST+" -> baseWeight = 7.0;
                    case "POST-" -> baseWeight = 2.0;
                    case "REPLY+", "QUOTE+" -> baseWeight = 6.0;
                    case "RETWEET+" -> baseWeight = 4.0;
                }
            }
            case "Tweet -> Tweet" -> {
                switch (interactionType) {
                    case "REPLY+", "QUOTE+" -> baseWeight = 8.0;
                    case "REPLY-", "QUOTE-" -> baseWeight = 4.0;
                    case "RETWEET-" -> baseWeight = 1.1;
                }
            }
            case "Tweet -> User" -> {
                switch (interactionType) {
                    case "POST-" -> baseWeight = 5.0;
                    case "MENTION" -> baseWeight = 5.1;
                    case "REPLY-", "QUOTE-" -> baseWeight = 2.0;
                    case "REPLY", "QUOTE" -> baseWeight = 6.0;
                }
            }
        }

        // Compute minutes difference
        long minutesDifference = ChronoUnit.MINUTES.between(interactionTime, LocalDateTime.now());

        // Time decay constant (lambda)
        double lambda = 0.00001;  // Adjust decay constant as needed
        double decayFactor = Math.exp(-lambda * minutesDifference);

        // Return adjusted weight
        return baseWeight * decayFactor;
    }
}