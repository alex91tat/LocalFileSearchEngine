package com.searchengine.repository;

import com.searchengine.model.FileRecord;
import com.searchengine.model.SearchResult;
import com.searchengine.search.ParsedQuery;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FileRepository {
    private final Connection connection;

    public FileRepository(Connection connection) {
        this.connection = connection;
    }

    private void checkConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Database connection is not available");
        }
    }

    public void save(FileRecord record) throws SQLException {
        checkConnection();
        String insertFile = """
                INSERT INTO files (path, name, extension, size, last_modified, preview, path_score)
                VALUES (?, ?, ?, ?, ?, ?, ?)
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
            stmtFile.setDouble(7, record.getPathScore());
            stmtFile.executeUpdate();

            stmtFts.setString(1, record.getPath());
            stmtFts.setString(2, record.getContent());
            stmtFts.executeUpdate();
        }
    }

    public void update(FileRecord record) throws SQLException {
        checkConnection();
        String updateFile = """
                UPDATE files
                SET name = ?, extension = ?, size = ?, last_modified = ?, preview = ?, path_score = ?
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
            stmtFile.setDouble(6, record.getPathScore());
            stmtFile.setString(7, record.getPath());
            stmtFile.executeUpdate();

            stmtDeleteFts.setString(1, record.getPath());
            stmtDeleteFts.executeUpdate();

            stmtInsertFts.setString(1, record.getPath());
            stmtInsertFts.setString(2, record.getContent());
            stmtInsertFts.executeUpdate();
        }
    }

    public void delete(String path) throws SQLException {
        checkConnection();
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
        checkConnection();
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

    public List<SearchResult> search(ParsedQuery query) throws SQLException {
        checkConnection();

        if (query.isPlainQuery()) {
            return searchPlain(query.getRawQuery());
        }

        if (!query.getContentTerms().isEmpty() && query.getPathTerms().isEmpty()) {
            return searchByContent(query.getContentTerms());
        }

        if (query.getContentTerms().isEmpty() && !query.getPathTerms().isEmpty()) {
            return searchByPath(query.getPathTerms());
        }

        return searchByContentAndPath(query.getContentTerms(), query.getPathTerms());
    }

    private List<SearchResult> searchPlain(String rawQuery) throws SQLException {
        List<SearchResult> results = new ArrayList<>();
        int rank = 1;

        String ftsSQL = """
            SELECT f.path, f.name, f.extension, f.size, f.last_modified, f.preview, f.path_score
            FROM files_fts fts
            JOIN files f ON fts.path = f.path
            WHERE files_fts MATCH ?
            ORDER BY path_score DESC, rank ASC
            LIMIT 20
            """;

        String nameSQL = """
            SELECT path, name, extension, size, last_modified, preview, path_score
            FROM files
            WHERE name LIKE ?
            ORDER BY path_score DESC
            LIMIT 10
            """;

        try (PreparedStatement stmtFts = connection.prepareStatement(ftsSQL);
             PreparedStatement stmtName = connection.prepareStatement(nameSQL)) {

            stmtFts.setString(1, rawQuery);
            ResultSet rsFts = stmtFts.executeQuery();
            while (rsFts.next()) {
                FileRecord record = mapResultSet(rsFts);
                results.add(new SearchResult(record, record.getPreview(), rank++));
            }

            stmtName.setString(1, "%" + rawQuery + "%");
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

    private List<SearchResult> searchByContent(List<String> contentTerms) throws SQLException {
        List<SearchResult> results = new ArrayList<>();
        // join terms with space — FTS5 treats spaces as AND
        String ftsQuery = String.join(" ", contentTerms);

        String sql = """
            SELECT f.path, f.name, f.extension, f.size, f.last_modified, f.preview, f.path_score
            FROM files_fts fts
            JOIN files f ON fts.path = f.path
            WHERE files_fts MATCH ?
            ORDER BY path_score DESC, rank ASC
            LIMIT 20
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ftsQuery);
            ResultSet rs = stmt.executeQuery();
            int rank = 1;
            while (rs.next()) {
                FileRecord record = mapResultSet(rs);
                results.add(new SearchResult(record, record.getPreview(), rank++));
            }
        }
        return results;
    }

    private List<SearchResult> searchByPath(List<String> pathTerms) throws SQLException {
        List<SearchResult> results = new ArrayList<>();

        // build: WHERE path LIKE ? AND path LIKE ? ...
        StringBuilder sql = new StringBuilder("""
            SELECT path, name, extension, size, last_modified, preview, path_score
            FROM files WHERE 1=1
            """);

        for (int i = 0; i < pathTerms.size(); i++) {
            sql.append(" AND path LIKE ?");
        }
        sql.append(" ORDER BY path_score DESC LIMIT 20");

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            // set each path term as a parameter
            for (int i = 0; i < pathTerms.size(); i++) {
                stmt.setString(i + 1, "%" + pathTerms.get(i) + "%");
            }
            ResultSet rs = stmt.executeQuery();
            int rank = 1;
            while (rs.next()) {
                FileRecord record = mapResultSet(rs);
                results.add(new SearchResult(record, record.getPreview(), rank++));
            }
        }
        return results;
    }

    private List<SearchResult> searchByContentAndPath(List<String> contentTerms,
                                                      List<String> pathTerms) throws SQLException {
        List<SearchResult> results = new ArrayList<>();
        String ftsQuery = String.join(" ", contentTerms);

        StringBuilder sql = new StringBuilder("""
            SELECT f.path, f.name, f.extension, f.size, f.last_modified, f.preview, f.path_score
            FROM files_fts fts
            JOIN files f ON fts.path = f.path
            WHERE files_fts MATCH ?
            """);

        for (int i = 0; i < pathTerms.size(); i++) {
            sql.append(" AND f.path LIKE ?");
        }
        sql.append(" ORDER BY path_score DESC, rank ASC LIMIT 20");

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            // first parameter is the FTS query
            stmt.setString(1, ftsQuery);
            // remaining parameters are path terms
            for (int i = 0; i < pathTerms.size(); i++) {
                stmt.setString(i + 2, "%" + pathTerms.get(i) + "%");
            }
            ResultSet rs = stmt.executeQuery();
            int rank = 1;
            while (rs.next()) {
                FileRecord record = mapResultSet(rs);
                results.add(new SearchResult(record, record.getPreview(), rank++));
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
                rs.getString("preview"),
                rs.getDouble("path_score")
        );
    }
}