package com.searchengine.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Config {
    private String rootDirectory;
    private String databasePath;
    private List<String> ignoredDirectories;
    private List<String> ignoredExtensions;
    private List<String> textExtensions;
    private String reportFormat;

    public Config() {}

    public static Config load(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(path), Config.class);
    }

    public void validate() {
        if (rootDirectory == null || rootDirectory.isBlank()) {
            throw new IllegalStateException("config.json: 'rootDirectory' must not be empty");
        }
        File root = new File(rootDirectory);
        if (!root.exists()) {
            throw new IllegalStateException("config.json: rootDirectory does not exist: " + rootDirectory);
        }
        if (!root.isDirectory()) {
            throw new IllegalStateException("config.json: rootDirectory is not a directory: " + rootDirectory);
        }
        if (databasePath == null || databasePath.isBlank()) {
            throw new IllegalStateException("config.json: 'databasePath' must not be empty");
        }
    }


    public String getRootDirectory() {
        return rootDirectory;
    }

    public void setRootDirectory(String rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public String getDatabasePath() {
        return databasePath;
    }

    public void setDatabasePath(String databasePath) {
        this.databasePath = databasePath;
    }

    public List<String> getIgnoredDirectories() {
        return ignoredDirectories;
    }

    public void setIgnoredDirectories(List<String> ignoredDirectories) {
        this.ignoredDirectories = ignoredDirectories;
    }

    public List<String> getIgnoredExtensions() {
        return ignoredExtensions;
    }

    public void setIgnoredExtensions(List<String> ignoredExtensions) {
        this.ignoredExtensions = ignoredExtensions;
    }

    public List<String> getTextExtensions() {
        return textExtensions;
    }

    public void setTextExtensions(List<String> textExtensions) {
        this.textExtensions = textExtensions;
    }

    public String getReportFormat() {
        return reportFormat;
    }

    public void setReportFormat(String reportFormat) {
        this.reportFormat = reportFormat;
    }

    @Override
    public String toString() {
        return "Config{" +
                "rootDirectory='" + rootDirectory + '\'' +
                ", databasePath='" + databasePath + '\'' +
                ", ignoredDirectories=" + ignoredDirectories +
                ", ignoredExtensions=" + ignoredExtensions +
                ", textExtensions=" + textExtensions +
                ", reportFormat='" + reportFormat + '\'' +
                '}';
    }
}