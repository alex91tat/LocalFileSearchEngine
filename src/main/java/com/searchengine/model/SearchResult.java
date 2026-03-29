package com.searchengine.model;

public class SearchResult {

    private FileRecord fileRecord;
    private String preview;
    private int rank;

    public SearchResult(FileRecord fileRecord, String preview, int rank) {
        this.fileRecord = fileRecord;
        this.preview = preview;
        this.rank = rank;
    }

    public FileRecord getFileRecord() {
        return fileRecord;
    }

    public String getPreview() {
        return preview;
    }

    public int getRank() {
        return rank;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s — %s (%s)%n     Preview: %s", rank, fileRecord.getName(), fileRecord.getPath(),
                formatSize(fileRecord.getSize()), preview.replace("\n", "\n     "));
    }


    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}