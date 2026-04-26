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

    public void setRank(int rank) {
        this.rank = rank;
    }

    @Override
    public String toString() {
        String previewText = (preview == null || preview.isBlank())
                ? "Preview not available"
                : preview.replace("\n", "\n     ");

        return String.format("[%d] %s — %s (%s)%n     Preview: %s", rank, fileRecord.getName(), fileRecord.getPath(),
                formatSize(fileRecord.getSize()), previewText);
    }


    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}