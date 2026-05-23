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
        List<String> colorTerms = new ArrayList<>();
        boolean hasQualifiers = false;

        String[] tokens = rawQuery.trim().split("\\s+");

        for (String token : tokens) {
            if (token.startsWith("path:")) {
                hasQualifiers = true;
                String value = token.substring("path:".length());
                if (!value.isBlank()) {
                    pathTerms.add(value.trim());
                }
            } else if (token.startsWith("content:")) {
                hasQualifiers = true;
                String value = token.substring("content:".length());
                contentTerms.add(sanitize(value));
            } else if (token.startsWith("color:")) {
                hasQualifiers = true;
                String value = token.substring("color:".length());
                if (!value.isBlank()) {
                    colorTerms.add(value.trim().toLowerCase());
                }
            } else {
                contentTerms.add(sanitize(token));
            }
        }

        return new ParsedQuery(contentTerms, pathTerms, rawQuery, hasQualifiers, colorTerms);
    }
}