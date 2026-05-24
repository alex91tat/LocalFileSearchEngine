package com.searchengine.cli;

import com.searchengine.model.SearchResult;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SourceCodeWidget implements Widget {
    private static final List<String> CODE_EXTENSIONS = List.of("java", "py", "js","cpp", "c");
    private static final double ACTIVATION_THRESHOLD = 0.5; // 50% of results

    @Override
    public boolean shouldActivate(List<SearchResult> results) {
        if (results == null || results.isEmpty()) return false;

        long codeCount = results.stream()
                .filter(r -> CODE_EXTENSIONS.contains(
                        r.getFileRecord().getExtension().toLowerCase())).count();

        return (double) codeCount / results.size() >= ACTIVATION_THRESHOLD;
    }

    @Override
    public void render(List<SearchResult> results) {
        // group by extension to show language breakdown
        Map<String, Long> languageCounts = results.stream()
                .filter(r -> CODE_EXTENSIONS.contains(
                        r.getFileRecord().getExtension().toLowerCase()))
                .collect(Collectors.groupingBy(
                        r -> r.getFileRecord().getExtension().toLowerCase(),
                        Collectors.counting()
                ));

        long totalCode = languageCounts.values().stream()
                .mapToLong(Long::longValue).sum();

        String languageSummary = languageCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> e.getKey() + "(" + e.getValue() + ")")
                .collect(Collectors.joining(", "));

        System.out.println("\nCode Browser:");
        System.out.println("Found " + totalCode
                + " source file(s) in results");
        System.out.println("Languages: " + languageSummary);
        System.out.println("Tip: Try path:SearchEngine content:class");
    }

    @Override
    public String getName() {
        return "Code Browser";
    }
}