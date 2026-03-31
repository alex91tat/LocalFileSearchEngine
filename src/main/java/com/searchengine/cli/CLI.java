package com.searchengine.cli;

import com.searchengine.indexing.IndexingService;
import com.searchengine.model.IndexReport;
import com.searchengine.model.SearchResult;
import com.searchengine.search.SearchService;
import java.util.List;
import java.util.Scanner;

public class CLI {
    private final IndexingService indexingService;
    private final SearchService searchService;
    private final Scanner scanner;


    public CLI(IndexingService indexingService, SearchService searchService) {
        this.indexingService = indexingService;
        this.searchService = searchService;
        this.scanner = new Scanner(System.in);
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
        System.out.println("Type your query ('back' to return to main menu):");

        while (true) {
            System.out.print("Search: ");
            String query = scanner.nextLine().trim();

            if (query.equalsIgnoreCase("back")) {
                return;
            }

            if (query.isBlank()) {
                continue;
            }

            List<SearchResult> results = searchService.search(query);
            displayResults(results, query);
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