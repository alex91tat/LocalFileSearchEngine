package com.searchengine.indexing;

import com.searchengine.config.Config;
import com.searchengine.indexing.ChangeDetector.FileStatus;
import com.searchengine.model.FileRecord;
import com.searchengine.model.IndexReport;
import com.searchengine.repository.FileRepository;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class IndexingService {

    private final Config config;
    private final FileCrawler crawler;
    private final FileFilter filter;
    private final List<Extractor> extractors;
    private final ChangeDetector changeDetector;
    private final FileRepository repository;

    private final PathScorer pathScorer;

    private static final int THREAD_COUNT = 3;
    private static final int QUEUE_CAPACITY = 100;

    // special sentinel object, signals readers are done
    private static final FileRecord POISON_PILL = new FileRecord(null, null, null, 0,
            0, null, null, 0.0, null);

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

        // crawl filesystem
        List<Path> files = crawler.crawl(config.getRootDirectory(), report);
        System.out.println("[Indexer] Found " + files.size() + " files to process.");
        System.out.println("[Indexer] Using " + THREAD_COUNT + " reader threads + 1 writer thread");

        // create shared queue between readers and writer
        BlockingQueue<FileRecord> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

        // thread-safe progress counter
        AtomicInteger processed = new AtomicInteger(0);
        int totalFiles = files.size();

        // start writer thread (single consumer)
        Thread writerThread = new Thread(() -> processFileConsumer(queue, report));
        writerThread.start();

        // start reader threads (multiple producers)
        int chunkSize = (int) Math.ceil((double) files.size() / THREAD_COUNT);

        try (ExecutorService readerPool = Executors.newFixedThreadPool(THREAD_COUNT)) {
            for (int i = 0; i < THREAD_COUNT; i++) {
                int start = i * chunkSize;
                int end = Math.min(start + chunkSize, files.size());
                if (start >= files.size()) break;

                List<Path> chunk = files.subList(start, end);
                readerPool.submit(() -> {
                    for (Path filePath : chunk) {
                        processFileProducer(filePath, queue, report, processed, totalFiles);
                    }
                });
            }

            // wait for all readers to finish
            readerPool.shutdown();
            try {
                boolean finished = readerPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                if (!finished) {
                    System.err.println("[Indexer] Reader threads did not finish in time.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[Indexer] Reader threads interrupted: " + e.getMessage());
            }
        }

        // send poison pill to signal writer to stop
        try {
            queue.put(POISON_PILL);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[Indexer] Interrupted while sending poison pill: " + e.getMessage());
        }

        // wait for writer to finish
        try {
            writerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[Indexer] Writer thread interrupted: " + e.getMessage());
        }

        report.finish();
        System.out.println("[Indexer] Indexing complete.");
        return report;
    }

    /**
     * Producer - runs in reader threads.
     * Reads file content and puts FileRecord into queue.
     */
    private void processFileProducer(Path filePath, BlockingQueue<FileRecord> queue, IndexReport report,
                                     AtomicInteger processed, int total) {
        try {
            FileStatus status = changeDetector.getStatus(filePath);
            if (status == FileStatus.UNCHANGED) {
                report.incrementSkipped();
                return;
            }

            String extension = filter.getExtension(filePath.getFileName().toString());
            Extractor extractor = findExtractor(extension);

            FileRecord record;
            if (extractor == null) {
                // no extractor, build metadata only record
                long size = java.nio.file.Files.size(filePath);
                long lastModified = java.nio.file.Files.getLastModifiedTime(filePath).toMillis();
                String name = filePath.getFileName().toString();
                record = new FileRecord(
                        filePath.toAbsolutePath().toString(),
                        name, extension, size, lastModified,
                        "", "", 0.0, null
                );
            } else {
                record = extractor.extract(filePath);
            }

            // compute path score
            double score = pathScorer.score(
                    record.getPath(),
                    record.getExtension(),
                    record.getLastModified(),
                    record.getSize(),
                    config.getRootDirectory()
            );
            record.setPathScore(score);

            // store status so writer knows save vs update
            record.setStatus(status);

            // put into queue, blocks if queue is full
            queue.put(record);

            // progress update, thread safe via AtomicInteger
            int count = processed.incrementAndGet();
            if (count % 100 == 0) {
                System.out.println("[Indexer] Progress: " + count + "/" + total
                        + " files processed... (thread: " + Thread.currentThread().getName() + ")");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("[Indexer] Reader error: " + filePath + " — " + e.getMessage());
            report.logError(filePath.toString(), e.getMessage());
        }
    }

    /**
     * Consumer - runs in single writer thread.
     * Takes FileRecords from queue and saves to database.
     */
    private void processFileConsumer(BlockingQueue<FileRecord> queue, IndexReport report) {
        try {
            while (true) {
                // take from queue, blocks if queue is empty
                FileRecord record = queue.take();

                // if all readers are done, stop
                if (record == POISON_PILL) {
                    break;
                }

                try {
                    if (record.getStatus() == FileStatus.NEW) {
                        repository.save(record);
                    } else {
                        repository.update(record);
                    }
                    report.incrementIndexed();
                } catch (SQLException e) {
                    System.err.println("[Indexer] Writer DB error: " + e.getMessage());
                    report.logError(record.getPath(), "DB error: " + e.getMessage());
                }

            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[Indexer] Writer thread interrupted: " + e.getMessage());
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
}