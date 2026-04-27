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
    private final SearchHistoryTracker historyTracker;

    public RelevanceRanking(SearchHistoryTracker historyTracker) {
        this.historyTracker = historyTracker;
    }

    private double historyBoost(SearchResult result) {
        String name = result.getFileRecord().getName().toLowerCase();
        long count = historyTracker.getHistory().stream()
                .filter(q -> name.contains(q.toLowerCase()))
                .count();
        return count * 0.2;
    }

    @Override
    public List<SearchResult> rank(List<SearchResult> results) {
        List<SearchResult> sorted = new ArrayList<>(results);
        sorted.sort((a, b) -> {
            double scoreA = a.getFileRecord().getPathScore() + historyBoost(a);
            double scoreB = b.getFileRecord().getPathScore() + historyBoost(b);
            return Double.compare(scoreB, scoreA);
        });
        return sorted;
    }

    @Override
    public String getName() {
        return "Relevance";
    }
}