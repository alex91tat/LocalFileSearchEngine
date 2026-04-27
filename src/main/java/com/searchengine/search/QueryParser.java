package com.searchengine.search;

import java.util.ArrayList;
import java.util.List;

public class QueryParser {
    private String sanitize(String query) {
        // remove FTS5 special characters that could cause syntax errors
        return query
                .replaceAll("[\"'\\-:^*.]", " ")  // remove FTS5 operators
                .replaceAll("\\s+", " ")           // collapse multiple spaces
                .trim();
    }

    public ParsedQuery parse(String rawQuery) {
        List<String> contentTerms = new ArrayList<>();
        List<String> pathTerms = new ArrayList<>();
        String[] tokens = rawQuery.trim().split("\\s+");

        for (String token : tokens) {
            if (token.startsWith("path:")) {
                String value = token.substring("path:".length());
                // don't sanitize path terms — dots are valid in paths
                if (!value.isBlank()) {
                    pathTerms.add(value.trim());
                }
            } else if (token.startsWith("content:")) {
                String value = token.substring("content:".length());
                contentTerms.add(sanitize(value));
            } else {
                contentTerms.add(sanitize(token));
            }
        }

        return new ParsedQuery(contentTerms, pathTerms, rawQuery);
    }
}
