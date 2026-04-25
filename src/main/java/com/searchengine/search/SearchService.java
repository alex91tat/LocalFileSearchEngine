package com.searchengine.search;

import com.searchengine.model.SearchResult;
import com.searchengine.repository.FileRepository;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class SearchService {
    private final FileRepository repository;
    private final QueryParser parser;

    public SearchService(FileRepository repository) {
        this.repository = repository;
        this.parser = new QueryParser();
    }

    public List<SearchResult> search(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return Collections.emptyList();
        }

        ParsedQuery parsedQuery = parser.parse(rawQuery);

        try {
            List<SearchResult> results = repository.search(parsedQuery);
            if (results.isEmpty()) {
                System.out.println("No results found for: \"" + rawQuery + "\"");
            }
            return results;
        } catch (SQLException e) {
            System.err.println("[Search] Database error: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}