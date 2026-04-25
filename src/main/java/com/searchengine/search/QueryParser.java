package com.searchengine.search;

import java.util.ArrayList;
import java.util.List;

public class QueryParser {
    private String sanitize(String query) {
        // remove FTS5 special characters that could cause syntax errors
        return query
                .replaceAll("[\"'\\-:^*]", " ")  // remove FTS5 operators
                .replaceAll("\\s+", " ")           // collapse multiple spaces
                .trim();
    }

    public ParsedQuery parse(String rawQuery) {
        List<String> contentTerms = new ArrayList<>();
        List<String> pathTerms = new ArrayList<>();
        String[] tokens = rawQuery.trim().split("\\s+");

        for (String token : tokens) {
           if (token.startsWith("path:")) {
               pathTerms.add(sanitize(token.substring("path:".length())));
           }
           else if (token.startsWith("content:")) {
               contentTerms.add(sanitize(token.substring("content:".length())));
           }
           else {
               // plain word, treat as content term
               contentTerms.add(sanitize(token));
           }
        }

        return new ParsedQuery(contentTerms, pathTerms, rawQuery);
    }
}
