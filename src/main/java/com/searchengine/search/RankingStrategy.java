package com.searchengine.search;

import com.searchengine.model.SearchResult;
import java.util.List;

/**
 * Strategy interface for ranking search results.
 * Each implementation defines a different ordering of results.
 * SearchService holds the current strategy and applies it after
 * the database returns results — without changing the core engine.
 */
public interface RankingStrategy {

    /**
     * Sorts the given list of search results according to this strategy.
     * Returns a new sorted list — does not modify the original.
     *
     * @param results list of results from the database
     * @return sorted list according to this strategy
     */
    List<SearchResult> rank(List<SearchResult> results);

    /**
     * Returns the display name of this strategy.
     * Shown to the user in the CLI when selecting a strategy.
     */
    String getName();
}