package com.searchengine.search;

import java.util.List;
import java.util.Map;

/**
 * Decorator that expands terms to include synonyms.
 * "img" becomes "img OR image OR photo" etc.
 * Synonyms are loaded from config.json so the user
 * can define their own without changing code.
 * This is the Decorator Pattern - wraps another QueryProcessor
 * and adds synonym expansion on top.
 */
public class SynonymDecorator implements QueryProcessor {
    private final QueryProcessor wrapped;
    private final Map<String, List<String>> synonyms;

    public SynonymDecorator(QueryProcessor wrapped, Map<String, List<String>> synonyms) {
        this.wrapped = wrapped;
        this.synonyms = synonyms;
    }

    @Override
    public String process(String term) {
        // first let the wrapped processor handle it
        String processed = wrapped.process(term);

        // check if this term has synonyms defined
        List<String> termSynonyms = synonyms.get(processed.toLowerCase().trim());

        if (termSynonyms == null || termSynonyms.isEmpty()) {
            return processed; // no synonyms, return unchanged
        }

        // build: "term OR synonym1 OR synonym2"
        StringBuilder expanded = new StringBuilder(processed);
        for (String synonym : termSynonyms) {
            expanded.append(" OR ").append(synonym);
        }

        return expanded.toString();
    }
}