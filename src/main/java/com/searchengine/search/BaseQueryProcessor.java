package com.searchengine.search;

/**
 * Base implementation of QueryProcessor.
 * Returns the term unchanged - acts as the innermost
 * layer of the decorator chain.
 * Every decorator wraps this at its core:
 * LogicDecorator(SynonymDecorator(SanitizationDecorator(BaseQueryProcessor)))
 */
public class BaseQueryProcessor implements QueryProcessor {
    @Override
    public String process(String term) {
        return term; // no transformation
    }
}