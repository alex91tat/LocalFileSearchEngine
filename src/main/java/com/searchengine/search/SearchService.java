package com.searchengine.search;

import com.searchengine.model.SearchResult;
import com.searchengine.repository.FileRepository;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class SearchService {
    private final FileRepository repository;

    public SearchService(FileRepository repository) {
        this.repository = repository;
    }

    public List<SearchResult> search(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return Collections.emptyList();
        }

        String sanitized = sanitize(rawQuery);

        if (sanitized.isBlank()) {
            return Collections.emptyList();
        }

        try {
            List<SearchResult> results = repository.search(sanitized);

            if (results.isEmpty()) {
                System.out.println("No results found for: \"" + rawQuery + "\"");
            }

            return results;

        } catch (SQLException e) {
            System.err.println("[Search] Database error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private String sanitize(String query) {
        // remove FTS5 special characters that could cause syntax errors
        return query
                .replaceAll("[\"'\\-:^*]", " ")  // remove FTS5 operators
                .replaceAll("\\s+", " ")           // collapse multiple spaces
                .trim();
    }
}