package com.searchengine.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class IndexReport {
    private int totalFilesFound;
    private final AtomicInteger filesIndexed = new AtomicInteger(0);
    private final AtomicInteger filesSkipped = new AtomicInteger(0);
    private final AtomicInteger filesFiltered = new AtomicInteger(0);
    private final AtomicInteger errors = new AtomicInteger(0);

    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private final List<String> errorLog;

    public IndexReport() {
        this.startTime = LocalDateTime.now();
        this.errorLog = new CopyOnWriteArrayList<>();
    }

    public void incrementFound() {
        totalFilesFound++;
    }

    public void incrementIndexed() {
        filesIndexed.incrementAndGet();
    }

    public void incrementSkipped() {
        filesSkipped.incrementAndGet();
    }

    public void incrementFiltered() {
        filesFiltered.incrementAndGet();
    }

    public void incrementErrors() {
        errors.incrementAndGet();
    }

    public void logError(String filePath, String reason) {
        errors.incrementAndGet();
        errorLog.add(String.format("  [ERROR] %s — %s", filePath, reason));
    }

    public void finish() {
        this.endTime = LocalDateTime.now();
    }

    public String generateReport() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();

        sb.append("\nINDEX REPORT:\n");
        sb.append(String.format("Started:   %s%n", startTime.format(fmt)));
        sb.append(String.format("Finished:  %s%n", endTime != null ? endTime.format(fmt) : "N/A"));
        sb.append(String.format("Files found:    %4d%n", totalFilesFound));
        sb.append(String.format("Files indexed:  %4d%n", filesIndexed.get()));
        sb.append(String.format("Files skipped:  %4d  (unchanged since last index)%n", filesSkipped.get()));
        sb.append(String.format("Files filtered: %4d  (ignored by rules)%n", filesFiltered.get()));
        sb.append(String.format("Errors:         %4d%n", errors.get()));

        if (!errorLog.isEmpty()) {
            sb.append("Error details:\n");
            errorLog.forEach(e -> sb.append(e).append("\n"));
        }

        return sb.toString();
    }

    public int getTotalFilesFound() {
        return totalFilesFound;
    }

    public int getFilesIndexed() {
        return filesIndexed.get();
    }

    public int getFilesSkipped() {
        return filesSkipped.get();
    }

    public int getFilesFiltered() {
        return filesFiltered.get();
    }

    public int getErrors() {
        return errors.get();
    }

    public List<String> getErrorLog() {
        return errorLog;
    }
}