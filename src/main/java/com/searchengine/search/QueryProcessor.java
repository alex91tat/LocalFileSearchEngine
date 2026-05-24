package com.searchengine.search;

/**
 * Interface for the Query Pre-Processor Pipeline.
 * Each implementation either processes the query directly
 * or wraps another QueryProcessor adding behavior on top.
 * This is the Decorator Pattern - decorators implement this
 * interface AND hold a reference to another QueryProcessor.
 */
public interface QueryProcessor {
    /**
     * Processes a single query term and returns the transformed result.
     */
    String process(String term);
}