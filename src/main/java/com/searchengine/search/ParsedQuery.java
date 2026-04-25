package com.searchengine.search;

import java.util.List;

public class ParsedQuery {
    private final List<String> contentTerms;
    private final List<String> pathTerms;
    private final String rawQuery;

    public ParsedQuery(List<String> contentTerms, List<String> pathTerms, String rawQuery) {
        this.contentTerms = contentTerms;
        this.pathTerms = pathTerms;
        this.rawQuery = rawQuery;
    }

    public boolean isPlainQuery() {
        return contentTerms.isEmpty() && pathTerms.isEmpty();
    }

    public List<String> getContentTerms() {
        return contentTerms;
    }

    public List<String> getPathTerms() {
        return pathTerms;
    }

    public String getRawQuery() {
        return rawQuery;
    }
}
