package com.searchengine.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private final String databasePath;
    private Connection connection;

    public DatabaseManager(String databasePath) {
        this.databasePath = databasePath;
    }

    public void initialize() throws SQLException {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            try (Statement pragma = connection.createStatement()) {
                pragma.execute("PRAGMA journal_mode=WAL");
            }
            System.out.println("[DB] Database initialized at: " + databasePath);
            createTables();
            System.out.println("[DB] Schema ready.");
        } catch (SQLException e) {
            throw new SQLException("Failed to initialize database at '"
                    + databasePath + "': " + e.getMessage(), e);
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[DB] Database connection closed.");
            } catch (SQLException e) {
                System.err.println("[DB] Warning: could not close connection: " + e.getMessage());
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS files (
                        id            INTEGER PRIMARY KEY AUTOINCREMENT,
                        path          TEXT    NOT NULL UNIQUE,
                        name          TEXT    NOT NULL,
                        extension     TEXT,
                        size          INTEGER,
                        last_modified INTEGER,
                        preview       TEXT,
                        path_score    REAL    DEFAULT 0.0
                    )
                    """);

            stmt.execute("""
                    CREATE INDEX IF NOT EXISTS idx_files_path
                    ON files(path)
                    """);

            stmt.execute("""
                    CREATE VIRTUAL TABLE IF NOT EXISTS files_fts
                    USING fts5(
                        path,
                        content,
                        tokenize='porter ascii'
                    )
                    """);
        }
    }
}