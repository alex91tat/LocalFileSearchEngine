package com.searchengine.repository;

import com.searchengine.model.FileRecord;
import com.searchengine.model.SearchResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FileRepository {
    private final Connection connection;

    public FileRepository(Connection connection) {
        this.connection = connection;
    }

    public void save(FileRecord record) throws SQLException {
        String insertFile = """
                INSERT INTO files (path, name, extension, size, last_modified, preview)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        String insertFts = """
                INSERT INTO files_fts (path, content)
                VALUES (?, ?)
                """;

        try (PreparedStatement stmtFile = connection.prepareStatement(insertFile);
             PreparedStatement stmtFts = connection.prepareStatement(insertFts)) {

            stmtFile.setString(1, record.getPath());
            stmtFile.setString(2, record.getName());
            stmtFile.setString(3, record.getExtension());
            stmtFile.setLong(4, record.getSize());
            stmtFile.setLong(5, record.getLastModified());
            stmtFile.setString(6, record.getPreview());
            stmtFile.executeUpdate();

            stmtFts.setString(1, record.getPath());
            stmtFts.setString(2, record.getContent());
            stmtFts.executeUpdate();
        }
    }

    public void update(FileRecord record) throws SQLException {
        String updateFile = """
                UPDATE files
                SET name = ?, extension = ?, size = ?, last_modified = ?, preview = ?
                WHERE path = ?
                """;

        String deleteFts = "DELETE FROM files_fts WHERE path = ?";
        String insertFts = "INSERT INTO files_fts (path, content) VALUES (?, ?)";

        try (PreparedStatement stmtFile = connection.prepareStatement(updateFile);
             PreparedStatement stmtDeleteFts = connection.prepareStatement(deleteFts);
             PreparedStatement stmtInsertFts = connection.prepareStatement(insertFts)) {

            stmtFile.setString(1, record.getName());
            stmtFile.setString(2, record.getExtension());
            stmtFile.setLong(3, record.getSize());
            stmtFile.setLong(4, record.getLastModified());
            stmtFile.setString(5, record.getPreview());
            stmtFile.setString(6, record.getPath());
            stmtFile.executeUpdate();

            stmtDeleteFts.setString(1, record.getPath());
            stmtDeleteFts.executeUpdate();

            stmtInsertFts.setString(1, record.getPath());
            stmtInsertFts.setString(2, record.getContent());
            stmtInsertFts.executeUpdate();
        }
    }

    public void delete(String path) throws SQLException {
        try (PreparedStatement stmtFile = connection.prepareStatement(
                "DELETE FROM files WHERE path = ?");
             PreparedStatement stmtFts = connection.prepareStatement(
                     "DELETE FROM files_fts WHERE path = ?")){

            stmtFile.setString(1, path);
            stmtFile.executeUpdate();

            stmtFts.setString(1, path);
            stmtFts.executeUpdate();
        }
    }

    public FileRecord findByPath(String path) throws SQLException {
        String sql = "SELECT * FROM files WHERE path = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, path);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSet(rs);
            }
            return null;
        }
    }

    public List<SearchResult> search(String query) throws SQLException {
        List<SearchResult> results = new ArrayList<>();

        String ftsSQL = """
                SELECT f.path, f.name, f.extension, f.size, f.last_modified, f.preview
                FROM files_fts fts
                JOIN files f ON fts.path = f.path
                WHERE files_fts MATCH ?
                ORDER BY rank
                LIMIT 20
                """;

        String nameSQL = """
                SELECT path, name, extension, size, last_modified, preview
                FROM files
                WHERE name LIKE ?
                LIMIT 10
                """;

        try (PreparedStatement stmtFts = connection.prepareStatement(ftsSQL);
             PreparedStatement stmtName = connection.prepareStatement(nameSQL)) {

            stmtFts.setString(1, query);
            ResultSet rsFts = stmtFts.executeQuery();
            int rank = 1;
            while (rsFts.next()) {
                FileRecord record = mapResultSet(rsFts);
                results.add(new SearchResult(record, record.getPreview(), rank++));
            }

            stmtName.setString(1, "%" + query + "%");
            ResultSet rsName = stmtName.executeQuery();
            while (rsName.next()) {
                String path = rsName.getString("path");
                boolean alreadyFound = results.stream()
                        .anyMatch(r -> r.getFileRecord().getPath().equals(path));
                if (!alreadyFound) {
                    FileRecord record = mapResultSet(rsName);
                    results.add(new SearchResult(record, record.getPreview(), rank++));
                }
            }
        }

        return results;
    }

    private FileRecord mapResultSet(ResultSet rs) throws SQLException {
        return new FileRecord(
                rs.getString("path"),
                rs.getString("name"),
                rs.getString("extension"),
                rs.getLong("size"),
                rs.getLong("last_modified"),
                "",
                rs.getString("preview")
        );
    }
}