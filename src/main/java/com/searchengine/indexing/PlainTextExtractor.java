package com.searchengine.indexing;

import com.searchengine.model.FileRecord;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


public class PlainTextExtractor implements Extractor {
    private final List<String> textExtensions;

    public PlainTextExtractor(List<String> textExtensions) {
        this.textExtensions = textExtensions;
    }


    @Override
    public boolean supports(String extension) {
        return textExtensions.contains(extension.toLowerCase());
    }

    @Override
    public FileRecord extract(Path filePath) {
        String fileName = filePath.getFileName().toString();
        String extension = getExtension(fileName);
        long size = 0;
        long lastModified = 0;
        String content = "";
        String preview = "";

        try {
            size = Files.size(filePath);
            lastModified = Files.getLastModifiedTime(filePath).toMillis();

            content = Files.readString(filePath, StandardCharsets.UTF_8);

            preview = buildPreview(content);
        } catch (MalformedInputException e) {
            System.err.println("[Extractor] Binary content in text file, skipping content: " + filePath);
        } catch (IOException e) {
            System.err.println("[Extractor] Could not read file: " + filePath + " — " + e.getMessage());
        }

        return new FileRecord(filePath.toAbsolutePath().toString(), fileName, extension, size, lastModified,
                content, preview);
    }

    private String buildPreview(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }

        StringBuilder preview = new StringBuilder();
        int linesAdded = 0;

        for (String line : content.split("\n")) {
            if (!line.isBlank()) {
                if (linesAdded > 0) preview.append("\n");
                preview.append(line.trim());
                linesAdded++;
                if (linesAdded == 3) break;
            }
        }

        return preview.toString();
    }

    private String getExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot <= 0) return "";
        return fileName.substring(lastDot + 1).toLowerCase();
    }
}