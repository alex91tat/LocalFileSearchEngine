package com.searchengine.search;

import com.searchengine.model.SearchResult;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Ranking strategy that sorts results by last modified date.
 * Most recently modified files appear first.
 */
public class DateAccessedRanking implements RankingStrategy {
    @Override
    public List<SearchResult> rank(List<SearchResult> results) {
        List<SearchResult> sorted = new ArrayList<>(results);
        // reversed → most recent first
        sorted.sort(Comparator.comparing(
                r -> r.getFileRecord().getLastModified(),
                Comparator.reverseOrder()
        ));
        return sorted;
    }

    @Override
    public String getName() {
        return "Date (most recent first)";
    }
}