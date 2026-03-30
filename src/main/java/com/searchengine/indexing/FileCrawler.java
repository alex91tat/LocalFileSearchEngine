package com.searchengine.indexing;

import com.searchengine.model.IndexReport;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileCrawler {
    private final FileFilter fileFilter;

    public FileCrawler(FileFilter fileFilter) {
        this.fileFilter = fileFilter;
    }

    public List<Path> crawl(String rootDirectory, IndexReport report) {
        List<Path> discoveredFiles = new ArrayList<>();
        Set<Path> visitedRealPaths = new HashSet<>();

        Path root = Paths.get(rootDirectory);

        try {
            Files.walkFileTree(root, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (!dir.equals(root) && fileFilter.isIgnoredDirectory(dir)) {
                        report.incrementFiltered();
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    try {
                        Path realPath = dir.toRealPath();
                        if (!visitedRealPaths.add(realPath)) {
                            System.err.println("[Crawler] Symlink loop detected, skipping: " + dir);
                            report.logError(dir.toString(), "Symlink loop detected");
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                    } catch (IOException e) {
                        System.err.println("[Crawler] Cannot resolve real path: " + dir);
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    report.incrementFound();

                    if (fileFilter.shouldIndex(file)) {
                        discoveredFiles.add(file);
                    } else {
                        report.incrementFiltered();
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    System.err.println("[Crawler] Cannot access: " + file + " — " + exc.getMessage());
                    report.logError(file.toString(), exc.getMessage());
                    return FileVisitResult.CONTINUE;
                }
            });

        } catch (IOException e) {
            System.err.println("[Crawler] Fatal error during crawl: " + e.getMessage());
            report.logError(rootDirectory, "Fatal crawl error: " + e.getMessage());
        }

        System.out.println("[Crawler] Discovered " + discoveredFiles.size()
                + " files to process.");
        return discoveredFiles;
    }
}