package com.searchengine.search;

import java.util.List;

public class ParsedQuery {
    private final List<String> contentTerms;
    private final List<String> pathTerms;
    private final String rawQuery;
    private final boolean hasQualifiers;

    public ParsedQuery(List<String> contentTerms, List<String> pathTerms, String rawQuery, boolean hasQualifiers) {
        this.contentTerms = contentTerms;
        this.pathTerms = pathTerms;
        this.rawQuery = rawQuery;
        this.hasQualifiers = hasQualifiers;
    }

    public boolean isPlainQuery() {
        return !hasQualifiers;
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
