package com.searchengine.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class IndexReport {
    private int totalFilesFound;
    private int filesIndexed;
    private int filesSkipped;
    private int filesFiltered;
    private int errors;

    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private final List<String> errorLog;

    public IndexReport() {
        this.startTime = LocalDateTime.now();
        this.errorLog = new ArrayList<>();
    }

    public void incrementFound() {
        totalFilesFound++;
    }

    public void incrementIndexed() {
        filesIndexed++;
    }

    public void incrementSkipped() {
        filesSkipped++;
    }

    public void incrementFiltered() {
        filesFiltered++;
    }

    public void incrementErrors() {
        errors++;
    }

    public void logError(String filePath, String reason) {
        errors++;
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
        sb.append(String.format("Files indexed:  %4d%n", filesIndexed));
        sb.append(String.format("Files skipped:  %4d  (unchanged since last index)%n", filesSkipped));
        sb.append(String.format("Files filtered: %4d  (ignored by rules)%n", filesFiltered));
        sb.append(String.format("Errors:         %4d%n", errors));

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
        return filesIndexed;
    }

    public int getFilesSkipped() {
        return filesSkipped;
    }

    public int getFilesFiltered() {
        return filesFiltered;
    }

    public int getErrors() {
        return errors;
    }

    public List<String> getErrorLog() {
        return errorLog;
    }
}