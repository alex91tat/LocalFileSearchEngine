package com.searchengine.indexing;

import com.searchengine.model.FileRecord;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageExtractor implements Extractor {

    private final List<String> imageExtensions;

    public ImageExtractor(List<String> imageExtensions) {
        this.imageExtensions = imageExtensions;
    }

    @Override
    public boolean supports(String extension) {
        return imageExtensions.contains(extension.toLowerCase());
    }

    @Override
    public FileRecord extract(Path filePath) {
        String fileName = filePath.getFileName().toString();
        String extension = getExtension(fileName);
        long size = 0;
        long lastModified = 0;
        String dominantColor = null;

        try {
            size = java.nio.file.Files.size(filePath);
            lastModified = java.nio.file.Files.getLastModifiedTime(filePath).toMillis();

            // read image and extract dominant color
            BufferedImage image = ImageIO.read(filePath.toFile());
            if (image != null) {
                dominantColor = extractDominantColor(image);
            }

        } catch (IOException e) {
            System.err.println("[ImageExtractor] Could not read image: "
                    + filePath + " — " + e.getMessage());
        }

        return new FileRecord(
                filePath.toAbsolutePath().toString(),
                fileName,
                extension,
                size,
                lastModified,
                "",           // no text content
                "",           // no preview
                0.0,          // pathScore set later by IndexingService
                dominantColor
        );
    }

    private String extractDominantColor(BufferedImage image) {
        Map<String, Integer> colorCounts = new HashMap<>();

        int width = image.getWidth();
        int height = image.getHeight();

        // sample every 5th pixel for performance
        for (int y = 0; y < height; y += 5) {
            for (int x = 0; x < width; x += 5) {
                int rgb = image.getRGB(x, y);
                String color = classifyColor(rgb);
                colorCounts.put(color, colorCounts.getOrDefault(color, 0) + 1);
            }
        }

        // find the color with highest count
        return colorCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("unknown");
    }

    private String classifyColor(int rgb) {
        int r = (rgb >> 16) & 0xFF;  // extract red component
        int g = (rgb >> 8) & 0xFF;   // extract green component
        int b = rgb & 0xFF;           // extract blue component

        // white
        if (r > 200 && g > 200 && b > 200) {
            return "white";
        }

        // black
        if (r < 50 && g < 50 && b < 50) {
            return "black";
        }

        // gray — all components similar and medium
        if (Math.abs(r - g) < 30 && Math.abs(g - b) < 30 && Math.abs(r - b) < 30) {
            return "gray";
        }

        // find dominant component
        if (r > g && r > b) {
            // red is dominant
            if (g > 150) {
                return "yellow";      // red + high green = yellow
            }

            if (g > 80)  {
                return "orange";      // red + medium green = orange
            }

            if (b > 100) {
                return "pink";        // red + some blue = pink
            }

            return "red";
        }

        if (g > r && g > b) {
            return "green";  // green is dominant
        }

        if (b > r && b > g) {
            if (r > 100) {
                return "purple";  // blue + some red = purple
            }

            return "blue";
        }

        return "gray";  // fallback
    }

    private String getExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot <= 0) return "";
        return fileName.substring(lastDot + 1).toLowerCase();
    }
}