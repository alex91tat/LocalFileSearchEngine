package com.searchengine.search;

import com.searchengine.model.FileRecord;
import com.searchengine.model.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RankingTest {
    private SearchResult resultA;
    private SearchResult resultB;
    private SearchResult resultC;

    @BeforeEach
    void setUp() {
        long old    = 1000L;   // oldest
        long medium = 5000L;   // medium
        long recent = 9000L;   // most recent

        FileRecord recordA = new FileRecord(
                "/home/user/Charlie.java", "Charlie.java", "java",
                100, old, "", "", 0.9
        );
        FileRecord recordB = new FileRecord(
                "/home/user/Alice.java", "Alice.java", "java",
                100, recent, "", "", 0.5
        );
        FileRecord recordC = new FileRecord(
                "/home/user/Bob.java", "Bob.java", "java",
                100, medium, "", "", 0.7
        );

        resultA = new SearchResult(recordA, "", 1);
        resultB = new SearchResult(recordB, "", 2);
        resultC = new SearchResult(recordC, "", 3);
    }

    @Test
    void testAlphabeticalRanking() {
        AlphabeticalRanking strategy = new AlphabeticalRanking();
        List<SearchResult> ranked = strategy.rank(List.of(resultA, resultB, resultC));

        // expected order: Alice, Bob, Charlie
        assertEquals("Alice.java", ranked.get(0).getFileRecord().getName());
        assertEquals("Bob.java",   ranked.get(1).getFileRecord().getName());
        assertEquals("Charlie.java", ranked.get(2).getFileRecord().getName());
    }

    @Test
    void testDateAccessedRanking() {
        DateAccessedRanking strategy = new DateAccessedRanking();
        List<SearchResult> ranked = strategy.rank(List.of(resultA, resultB, resultC));

        // expected order: most recent first
        // resultB (recent=9000) → resultC (medium=5000) → resultA (old=1000)
        assertEquals("Alice.java",   ranked.get(0).getFileRecord().getName());
        assertEquals("Bob.java",     ranked.get(1).getFileRecord().getName());
        assertEquals("Charlie.java", ranked.get(2).getFileRecord().getName());
    }

    @Test
    void testRelevanceRanking() {
        // create a tracker with empty history for clean test
        SearchHistoryTracker tracker = new SearchHistoryTracker();
        RelevanceRanking strategy = new RelevanceRanking(tracker);
        List<SearchResult> ranked = strategy.rank(List.of(resultA, resultB, resultC));

        // expected order: highest pathScore first
        // resultA(0.9) → resultC(0.7) → resultB(0.5)
        assertEquals("Charlie.java", ranked.get(0).getFileRecord().getName());
        assertEquals("Bob.java",     ranked.get(1).getFileRecord().getName());
        assertEquals("Alice.java",   ranked.get(2).getFileRecord().getName());
    }

    @Test
    void testRankingDoesNotModifyOriginalList() {
        AlphabeticalRanking strategy = new AlphabeticalRanking();
        List<SearchResult> original = List.of(resultA, resultB, resultC);
        strategy.rank(original);

        // original list should be unchanged
        assertEquals("Charlie.java", original.get(0).getFileRecord().getName());
        assertEquals("Alice.java",   original.get(1).getFileRecord().getName());
        assertEquals("Bob.java",     original.get(2).getFileRecord().getName());
    }
}