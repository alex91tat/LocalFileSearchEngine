package com.searchengine.search;

/**
 * Decorator that strips special characters that could
 * break FTS5 syntax before passing to the next processor.
 * This is the Decorator Pattern - wraps another QueryProcessor
 * and adds sanitization behavior on top.
 */
public class SanitizationDecorator implements QueryProcessor {
    private final QueryProcessor wrapped;

    public SanitizationDecorator(QueryProcessor wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public String process(String term) {
        // first let the wrapped processor handle it
        String processed = wrapped.process(term);

        // then sanitize - remove FTS5 special characters
        return processed
                .replaceAll("[\"'\\-:^.]", " ")  // remove FTS5 operators
                .replaceAll("\\s+", " ")          // collapse multiple spaces
                .trim();
    }
}