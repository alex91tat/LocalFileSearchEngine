package com.searchengine.search;

import com.searchengine.model.SearchResult;
import com.searchengine.repository.FileRepository;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchService {
    private final FileRepository repository;
    private final QueryParser parser;

    private RankingStrategy rankingStrategy;
    private final List<SearchObserver> observers = new ArrayList<>();
    private final AliasManager aliasManager;

    public SearchService(FileRepository repository, AliasManager aliasManager) {
        this.repository = repository;
        this.parser = new QueryParser();
        this.rankingStrategy = null; // set from Main
        this.aliasManager = aliasManager;
    }

    public List<SearchResult> search(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return Collections.emptyList();
        }
        notifyObservers(rawQuery);

        String expandedQuery = aliasManager.expand(rawQuery);

        ParsedQuery parsedQuery = parser.parse(expandedQuery);

        try {
            List<SearchResult> results = repository.search(parsedQuery);
            if (results.isEmpty()) {
                System.out.println("No results found for: \"" + rawQuery + "\"");
                return results;
            }

            List<SearchResult> ranked = rankingStrategy.rank(results);
            List<SearchResult> top20 = ranked.subList(0, Math.min(20, ranked.size()));
            for (int i = 0; i < top20.size(); i++) {
                top20.get(i).setRank(i + 1);
            }
            return top20;
        } catch (SQLException e) {
            System.err.println("[Search] Database error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public void setRankingStrategy(RankingStrategy strategy) {
        this.rankingStrategy = strategy;
    }

    public RankingStrategy getRankingStrategy() {
        return rankingStrategy;
    }

    public void addObserver(SearchObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers(String query) {
        for (SearchObserver observer : observers) {
            observer.onSearch(query);
        }
    }
}