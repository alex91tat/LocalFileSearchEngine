package com.searchengine.cli;

import com.searchengine.indexing.IndexingService;
import com.searchengine.model.IndexReport;
import com.searchengine.model.SearchResult;
import com.searchengine.search.*;

import java.util.List;
import java.util.Scanner;

public class CLI {
    private final IndexingService indexingService;
    private final SearchService searchService;
    private final Scanner scanner;

    private final SearchHistoryTracker historyTracker;


    public CLI(IndexingService indexingService, SearchService searchService, SearchHistoryTracker historyTracker) {
        this.indexingService = indexingService;
        this.searchService = searchService;
        this.scanner = new Scanner(System.in);
        this.historyTracker = historyTracker;
    }

    public void start() {
        printWelcome();

        while (true) {
            System.out.print("\n> ");
            String command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "index"  -> handleIndex();
                case "search" -> handleSearch();
                case "exit"   -> {
                    System.out.println("Bye!");
                    return;
                }
                default -> System.out.println(
                        "Unknown command. Use: 'index' | 'search' | 'exit'"
                );
            }
        }
    }

    private void handleIndex() {
        System.out.println("Starting indexing...");
        IndexReport report = indexingService.index();
        System.out.println(report.generateReport());
    }

    private void handleSearch() {
        selectRankingStrategy();
        System.out.println("Type your query ('back' to return to main menu):");

        while (true) {
            System.out.print("Search: ");
            String query = scanner.nextLine().trim();

            if (query.equalsIgnoreCase("back")) return;
            if (query.isBlank()) continue;

            // show suggestions
            List<String> suggestions = historyTracker.getSuggestions(query);
            if (!suggestions.isEmpty()) {
                System.out.println("Similar past searches: " + suggestions);
                System.out.print("Refine query (or Enter to search \"" + query + "\"): ");
                String refined = scanner.nextLine().trim();
                if (!refined.isBlank()) {
                    query = refined;
                }
            }

            List<SearchResult> results = searchService.search(query);
            displayResults(results, query);
        }
    }

    private void selectRankingStrategy() {
        System.out.println("\nSelect ranking strategy:");
        System.out.println("(1) Relevance (default)");
        System.out.println("(2) Alphabetical");
        System.out.println("(3) Date (most recent first)");
        System.out.print("Choice: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> {
                searchService.setRankingStrategy(new RelevanceRanking(historyTracker));
                System.out.println("Strategy set to: Relevance");
            }
            case "2" -> {
                searchService.setRankingStrategy(new AlphabeticalRanking());
                System.out.println("Strategy set to: Alphabetical");
            }
            case "3" -> {
                searchService.setRankingStrategy(new DateAccessedRanking());
                System.out.println("Strategy set to: Date (most recent first)");
            }
            default -> System.out.println("Invalid choice, keeping current strategy: "
                    + searchService.getRankingStrategy().getName());
        }
    }

    private void displayResults(List<SearchResult> results, String query) {
        if (results.isEmpty()) {
            System.out.println("No results found for: \"" + query + "\"");
            return;
        }

        System.out.println("\nFound " + results.size()
                + " result(s) for \"" + query + "\":");
        System.out.println("----------------------------------------");

        for (SearchResult result : results) {
            System.out.println(result.toString());
            System.out.println();
        }
    }


    private void printWelcome() {
        System.out.println("------------------------------------");
        System.out.println("|       Local File Search Engine   |");
        System.out.println("------------------------------------");
        System.out.println("Commands: 'index' | 'search' | 'exit'");
    }
}