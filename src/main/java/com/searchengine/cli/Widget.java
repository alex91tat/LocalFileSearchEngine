package com.searchengine.cli;

import com.searchengine.model.SearchResult;
import java.util.List;

/**
 * Each widget independently decides whether to activate
 * based on the search results it receives.
 * This is the Observer Pattern
 */
public interface Widget {
    /**
     * Checks if this widget should activate for the given results.
     * Each widget has its own activation rule.
     */
    boolean shouldActivate(List<SearchResult> results);

    /**
     * Renders the widget output to the user.
     * Only called if shouldActivate() returns true.
     */
    void render(List<SearchResult> results);

    String getName();
}