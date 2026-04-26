package com.searchengine.search;

import com.searchengine.model.SearchResult;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Ranking strategy that sorts results alphabetically by filename.
 */
public class AlphabeticalRanking implements RankingStrategy {
    @Override
    public List<SearchResult> rank(List<SearchResult> results) {
        List<SearchResult> sorted = new ArrayList<>(results);
        sorted.sort(Comparator.comparing(
                r -> r.getFileRecord().getName().toLowerCase()
        ));
        return sorted;
    }

    @Override
    public String getName() {
        return "Alphabetical";
    }
}