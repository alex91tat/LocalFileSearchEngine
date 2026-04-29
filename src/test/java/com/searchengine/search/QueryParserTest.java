package com.searchengine.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QueryParserTest {
    private QueryParser parser;

    @BeforeEach
    void setUp() {
        parser = new QueryParser();
    }

    @Test
    void testPlainQuery() {
        ParsedQuery result = parser.parse("hello");
        assertTrue(result.isPlainQuery());
        assertTrue(result.getContentTerms().contains("hello"));
        assertTrue(result.getPathTerms().isEmpty());
    }

    @Test
    void testContentQualifier() {
        ParsedQuery result = parser.parse("content:hello");
        assertFalse(result.isPlainQuery());
        assertTrue(result.getContentTerms().contains("hello"));
        assertTrue(result.getPathTerms().isEmpty());
    }

    @Test
    void testPathQualifier() {
        ParsedQuery result = parser.parse("path:Documents");
        assertFalse(result.isPlainQuery());
        assertTrue(result.getPathTerms().contains("Documents"));
        assertTrue(result.getContentTerms().isEmpty());
    }

    @Test
    void testBothQualifiers() {
        ParsedQuery result = parser.parse("path:Documents content:hello");
        assertFalse(result.isPlainQuery());
        assertTrue(result.getPathTerms().contains("Documents"));
        assertTrue(result.getContentTerms().contains("hello"));
    }

    @Test
    void testDuplicateContentQualifiers() {
        ParsedQuery result = parser.parse("content:hello content:world");
        assertFalse(result.isPlainQuery());
        assertEquals(2, result.getContentTerms().size());
        assertTrue(result.getContentTerms().contains("hello"));
        assertTrue(result.getContentTerms().contains("world"));
    }

    @Test
    void testDuplicatePathQualifiers() {
        ParsedQuery result = parser.parse("path:facultate path:sd");
        assertEquals(2, result.getPathTerms().size());
        assertTrue(result.getPathTerms().contains("facultate"));
        assertTrue(result.getPathTerms().contains("sd"));
    }

    @Test
    void testPathWithDot() {
        // dots must be preserved in path terms
        ParsedQuery result = parser.parse("path:FileRecord.java");
        assertTrue(result.getPathTerms().contains("FileRecord.java"));
    }

    @Test
    void testAnyPermutation() {
        ParsedQuery result1 = parser.parse("path:Documents content:hello");
        ParsedQuery result2 = parser.parse("content:hello path:Documents");
        assertEquals(result1.getPathTerms(), result2.getPathTerms());
        assertEquals(result1.getContentTerms(), result2.getContentTerms());
    }
}