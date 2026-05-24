package com.searchengine.cli;

import com.searchengine.model.SearchResult;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GalleryWidget implements Widget {
    private static final List<String> IMAGE_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "bmp", "webp");
    private static final double ACTIVATION_THRESHOLD = 0.3; // 30% of results

    @Override
    public boolean shouldActivate(List<SearchResult> results) {
        if (results == null || results.isEmpty()) return false;

        long imageCount = results.stream()
                .filter(r -> IMAGE_EXTENSIONS.contains(
                        r.getFileRecord().getExtension().toLowerCase())).count();

        return (double) imageCount / results.size() >= ACTIVATION_THRESHOLD;
    }

    @Override
    public void render(List<SearchResult> results) {
        // count images
        List<SearchResult> imageResults = results.stream()
                .filter(r -> IMAGE_EXTENSIONS.contains(
                        r.getFileRecord().getExtension().toLowerCase()))
                .collect(Collectors.toList());

        // count colors from dominant_color field
        Map<String, Long> colorCounts = imageResults.stream()
                .filter(r -> r.getFileRecord().getDominantColor() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getFileRecord().getDominantColor(),
                        Collectors.counting()
                ));

        System.out.println("\nGallery View:");
        System.out.println("Found " + imageResults.size()
                + " image file(s) in results");

        if (!colorCounts.isEmpty()) {
            String colorSummary = colorCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .map(e -> e.getKey() + "(" + e.getValue() + ")")
                    .collect(Collectors.joining(", "));
            System.out.println("Colors: " + colorSummary);
        }

        System.out.println("Tip: Try color:red to find red images");
    }

    @Override
    public String getName() {
        return "Gallery View";
    }
}