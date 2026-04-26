package com.searchengine.indexing;

import java.time.Instant;
import java.util.List;

public class PathScorer {
    private static final List<String> IMPORTANT_EXTENSIONS = List.of("java", "c", "md", "cpp", "py", "js");

    // scores file path between 1.0 and 0.0
    public double score(String path, String extension, long lastModified,
                        long size, String rootDirectory) {
        double score = 1.0;

        // relative depth from root
        String relativePath = path.replace(rootDirectory, "");
        int relativeDepth = relativePath.split("/").length;
        score -= relativeDepth * 0.1;

        // penalty for large files (likely libraries)
        if (size > 1_000_000) {
            score -= 0.3;
        } else if (size > 100_000) {
            score -= 0.1;
        }

        // bonus for important extensions
        if (IMPORTANT_EXTENSIONS.contains(extension.toLowerCase())) {
            score += 0.2;
        }

        // bonus if recently modified
        long now = Instant.now().toEpochMilli();
        long daysSinceModified = (now - lastModified) / (1000L * 60 * 60 * 24);
        if (daysSinceModified < 7) {
            score += 0.3;
        } else if (daysSinceModified < 30) {
            score += 0.1;
        }

        return Math.max(0.0, Math.min(1.0, score));
    }
}
