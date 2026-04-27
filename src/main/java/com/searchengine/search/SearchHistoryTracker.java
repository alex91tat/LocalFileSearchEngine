package com.searchengine.search;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class SearchHistoryTracker implements SearchObserver {

    private static final String HISTORY_FILE = "search_history.json";
    private static final int MAX_HISTORY = 100; // keep last 100 searches

    private final List<String> history;
    private final ObjectMapper mapper;

    public SearchHistoryTracker() {
        this.mapper = new ObjectMapper();
        this.history = loadHistory();
    }

    /**
     * Called by SearchService every time a search happens.
     * Records the query and persists to disk.
     */
    @Override
    public void onSearch(String query) {
        if (query == null || query.isBlank()) return;

        history.add(query);

        // keep only last MAX_HISTORY searches
        if (history.size() > MAX_HISTORY) {
            history.remove(0);
        }

        saveHistory();
    }

    public List<String> getSuggestions(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return Collections.emptyList();
        }

        String lowerPrefix = prefix.toLowerCase();

        // count frequency of each matching query
        return history.stream()
                .filter(q -> q.toLowerCase().startsWith(lowerPrefix))
                .collect(Collectors.groupingBy(q -> q, Collectors.counting()))
                .entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(e -> e.getKey() + " (" + e.getValue() + "x)")
                .limit(5)
                .collect(Collectors.toList());
    }

    public List<String> getHistory() {
        return Collections.unmodifiableList(history);
    }

    private List<String> loadHistory() {
        File file = new File(HISTORY_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (IOException e) {
            System.err.println("[History] Could not load history: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void saveHistory() {
        try {
            mapper.writeValue(new File(HISTORY_FILE), history);
        } catch (IOException e) {
            System.err.println("[History] Could not save history: " + e.getMessage());
        }
    }
}