package com.searchengine.search;

import com.searchengine.model.SearchResult;
import com.searchengine.repository.FileRepository;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class SearchService {
    private final FileRepository repository;
    private final QueryParser parser;

    private RankingStrategy rankingStrategy;

    public SearchService(FileRepository repository) {
        this.repository = repository;
        this.parser = new QueryParser();
        this.rankingStrategy = new RelevanceRanking();
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
}