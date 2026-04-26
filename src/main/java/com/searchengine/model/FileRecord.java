package com.searchengine.model;

public class FileRecord {

    private long id;
    private String path;
    private String name;
    private String extension;
    private long size;
    private long lastModified;
    private String content;
    private String preview;

    private double pathScore;


    public FileRecord(String path, String name, String extension, long size, long lastModified, String content,
                      String preview, double pathScore) {
        this.path = path;
        this.name = name;
        this.extension = extension;
        this.size = size;
        this.lastModified = lastModified;
        this.content = content;
        this.preview = preview;
        this.pathScore = pathScore;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public double getPathScore() {
        return pathScore;
    }

    public void setPathScore(double pathScore) {
        this.pathScore = pathScore;
    }

    @Override
    public String toString() {
        return "FileRecord{" +
                "path='" + path + '\'' +
                ", name='" + name + '\'' +
                ", extension='" + extension + '\'' +
                ", size=" + size +
                ", lastModified=" + lastModified +
                '}';
    }
}