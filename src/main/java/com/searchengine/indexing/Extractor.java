package com.searchengine.indexing;

import com.searchengine.model.FileRecord;
import java.nio.file.Path;

public interface Extractor {
    FileRecord extract(Path filePath);
    boolean supports(String extension);
}