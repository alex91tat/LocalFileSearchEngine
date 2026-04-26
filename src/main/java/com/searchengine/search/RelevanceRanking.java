package com.searchengine.search;

import com.searchengine.model.SearchResult;
import java.util.ArrayList;
import java.util.List;

/**
 * Ranking strategy that preserves the database order.
 * Results are already ordered by path_score DESC and FTS5 BM25 rank
 * so this strategy simply returns them as-is.
 * This is the default strategy.
 */
public class RelevanceRanking implements RankingStrategy {
    @Override
    public List<SearchResult> rank(List<SearchResult> results) {
        // DB already sorted by relevance
        return new ArrayList<>(results);
    }

    @Override
    public String getName() {
        return "Relevance";
    }
}