package com.searchengine.search;

/**
 * Decorator that adds wildcard suffix to each term
 * to support prefix matching in FTS5.
 * "search" becomes "search*".
 * If the term contains OR (from SynonymDecorator),
 * each part gets its own wildcard:
 * "img OR image OR photo" becomes "img* OR image* OR photo*"
 * This is the Decorator Pattern - wraps another QueryProcessor
 * and adds wildcard logic on top.
 */
public class LogicDecorator implements QueryProcessor {
    private final QueryProcessor wrapped;

    public LogicDecorator(QueryProcessor wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public String process(String term) {
        // first let the wrapped processor handle it
        String processed = wrapped.process(term);

        // if term contains OR parts (from SynonymDecorator)
        // add wildcard to each part separately
        if (processed.contains(" OR ")) {
            String[] parts = processed.split(" OR ");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) result.append(" OR ");
                result.append(parts[i].trim()).append("*");
            }
            return result.toString();
        }

        // simple term, just add wildcard
        return processed + "*";
    }
}