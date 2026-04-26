package com.searchengine.indexing;

import com.searchengine.config.Config;
import com.searchengine.indexing.ChangeDetector.FileStatus;
import com.searchengine.model.FileRecord;
import com.searchengine.model.IndexReport;
import com.searchengine.repository.FileRepository;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class IndexingService {

    private final Config config;
    private final FileCrawler crawler;
    private final FileFilter filter;
    private final List<Extractor> extractors;
    private final ChangeDetector changeDetector;
    private final FileRepository repository;

    private final PathScorer pathScorer;

    public IndexingService(Config config, FileCrawler crawler, FileFilter filter, List<Extractor> extractors,
                           ChangeDetector changeDetector, FileRepository repository) {
        this.config = config;
        this.crawler = crawler;
        this.filter = filter;
        this.extractors = extractors;
        this.changeDetector = changeDetector;
        this.repository = repository;
        this.pathScorer = new PathScorer();
    }

    // Run the full pipeline: crawl → detect changes → extract → save
    public IndexReport index() {
        IndexReport report = new IndexReport();
        System.out.println("[Indexer] Starting indexing from: " + config.getRootDirectory());

        List<Path> files = crawler.crawl(config.getRootDirectory(), report);
        System.out.println("[Indexer] Found " + files.size() + " files to process.");

        int processed = 0;
        for (Path filePath : files) {
            processFile(filePath, report);
            processed++;

            // Print progress every 100 files so user knows is still running
            if (processed % 100 == 0) {
                System.out.println("[Indexer] Progress: " + processed + "/" + files.size() + " files processed...");
            }
        }

        report.finish();
        System.out.println("[Indexer] Indexing complete.");
        return report;
    }

    private void processFile(Path filePath, IndexReport report) {
        try {
            FileStatus status = changeDetector.getStatus(filePath);
            if (status == FileStatus.UNCHANGED) {
                report.incrementSkipped();
                return;
            }

            String extension = filter.getExtension(filePath.getFileName().toString());
            Extractor extractor = findExtractor(extension);

            if (extractor == null) {
                indexMetadataOnly(filePath, extension, status, report);
                return;
            }

            FileRecord record = extractor.extract(filePath);
            // Compute path score at index time
            double score = pathScorer.score(
                    record.getPath(),
                    record.getExtension(),
                    record.getLastModified(),
                    record.getSize(),
                    config.getRootDirectory()
            );
            record.setPathScore(score);

            // Save or update in database
            if (status == FileStatus.NEW) {
                repository.save(record);
                report.incrementIndexed();
            } else {
                repository.update(record);
                report.incrementIndexed();
            }

        } catch (SQLException e) {
            System.err.println("[Indexer] DB error processing: " + filePath + " — " + e.getMessage());
            report.logError(filePath.toString(), "DB error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[Indexer] Unexpected error processing: " + filePath + " — " + e.getMessage());
            report.logError(filePath.toString(), e.getMessage());
        }
    }

    // Loop through extractors and return the first one that handles this extension
    private Extractor findExtractor(String extension) {
        for (Extractor extractor : extractors) {
            if (extractor.supports(extension)) {
                return extractor;
            }
        }
        return null;
    }

    // No extractor found — store metadata only so file still appears in name searches
    private void indexMetadataOnly(Path filePath, String extension, FileStatus status, IndexReport report) {
        try {
            long size = java.nio.file.Files.size(filePath);
            long lastModified = java.nio.file.Files.getLastModifiedTime(filePath).toMillis();
            String name = filePath.getFileName().toString();

            FileRecord record = new FileRecord(
                    filePath.toAbsolutePath().toString(),
                    name,
                    extension,
                    size,
                    lastModified,
                    "",
                    "",
                    0.0
            );

            // compute score for metadata-only files
            double score = pathScorer.score(
                    filePath.toAbsolutePath().toString(),
                    extension,
                    lastModified,
                    size,
                    config.getRootDirectory()
            );
            record.setPathScore(score);

            if (status == FileStatus.NEW) {
                repository.save(record);
            } else {
                repository.update(record);
            }
            report.incrementIndexed();

        } catch (Exception e) {
            report.logError(filePath.toString(), "Metadata only error: " + e.getMessage());
        }
    }
}