package com.searchengine.search;

/**
 * Observer interface for search events.
 * Any class that wants to be notified when a search happens
 * must implement this interface.
 *
 * This is the Observer Pattern — SearchService is the Subject,
 * SearchHistoryTracker is the Observer.
 */
public interface SearchObserver {

    /**
     * Called by SearchService every time a search is executed.
     * @param query the query that was searched
     */
    void onSearch(String query);
}