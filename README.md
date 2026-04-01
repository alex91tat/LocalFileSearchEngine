# Local File Search Engine

A fast, local file search system built in Java that indexes files on your device and enables full-text search across their content and metadata.

---

## Overview

Instead of scanning your disk every time you search, this engine indexes your files once into a SQLite database and queries it instantly. The result is a search experience that feels fast and responsive even across tens of thousands of files.

---

## Features

- **Recursive file crawling** — traverses your entire file system from a configurable root directory
- **Full-text search** — powered by SQLite FTS5 with BM25 ranking and Porter stemming
- **Filename search** — find files by name even if their content can't be read
- **File previews** — shows the first 3 lines of matching files in search results
- **Incremental indexing** — on subsequent runs, only re-indexes files that have changed
- **Runtime configuration** — all settings managed via `config.json`, no recompilation needed
- **Index report** — detailed report after every indexing run with counts and error details
- **Edge case handling** — gracefully handles permission errors, symlink loops, binary files, and database connection issues

---

## Architecture

The project follows the **C4 model** architecture (documented in `ARCHITECTURE.md`) and is organized into the following packages:

```
com.searchengine/
├── Main.java                       — entry point, wires all components
├── cli/
│   └── CLI.java                    — user interface
├── config/
│   └── Config.java                 — reads and holds runtime configuration
├── model/
│   ├── FileRecord.java             — represents an indexed file
│   ├── SearchResult.java           — represents a search result shown to user
│   └── IndexReport.java            — tracks indexing statistics
├── indexing/
│   ├── IndexingService.java        — coordinates the indexing pipeline
│   ├── FileCrawler.java            — recursive file system traversal
│   ├── FileFilter.java             — filters ignored directories and extensions
│   ├── Extractor.java              — interface (Strategy Pattern)
│   ├── PlainTextExtractor.java     — extracts content from text files
│   └── ChangeDetector.java        — detects new, modified, and unchanged files
├── search/
│   └── SearchService.java          — processes queries and returns results
└── repository/
    ├── DatabaseManager.java        — manages SQLite connection and schema
    └── FileRepository.java         — all SQL operations
```

---

## Database Schema

Two tables power the search engine:

```sql
-- Stores file metadata and previews
files (
    id            INTEGER PRIMARY KEY,
    path          TEXT UNIQUE,
    name          TEXT,
    extension     TEXT,
    size          INTEGER,
    last_modified INTEGER,
    preview       TEXT
)

-- FTS5 virtual table for full-text search
files_fts (
    path    TEXT,
    content TEXT,
    tokenize='porter ascii'
)
```

---

## Configuration

Edit `config.json` before running the application:

```json
{
  "rootDirectory": "/home/user",
  "databasePath": "index.db",
  "ignoredDirectories": [".git", "node_modules", "target", ".idea", ".mvn"],
  "ignoredExtensions": ["class", "jar", "exe", "bin", "zip", "tar", "gz"],
  "textExtensions": ["txt", "md", "java", "py", "js", "ts", "html", "css", "xml", "json", "yaml", "yml", "csv", "log"],
  "reportFormat": "txt"
}
```

| Field | Description |
|-------|-------------|
| `rootDirectory` | Where the crawler starts — change this to your path |
| `databasePath` | Where the SQLite `.db` file is saved |
| `ignoredDirectories` | Directories skipped entirely during crawling |
| `ignoredExtensions` | File types skipped during indexing |
| `textExtensions` | File types that get full content extraction |
| `reportFormat` | Format of the index report |

---

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+

### Build

```bash
mvn compile
```

### Run

```bash
mvn exec:java
```

---
## Design Decisions

- **SQLite** — no server setup needed, database is a single file created automatically on first run
- **FTS5** — built-in full-text search with BM25 ranking, searching "running" also matches "run"
- **Extractor interface** — new file types (PDF, DOCX) can be added in future iterations without touching existing code
- **Incremental indexing** — compares `lastModified` timestamps so subsequent runs take seconds instead of minutes

---

## Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| `sqlite-jdbc` | 3.45.1.0 | SQLite database driver |
| `jackson-databind` | 2.18.3 | JSON config file parsing |
| `slf4j-nop` | 1.7.36 | Suppress JDBC logging output |

---

## Project Structure

```
SearchEngine/
├── pom.xml
├── config.json
├── ARCHITECTURE.md
├── README.md
└── src/
    └── main/
        └── java/
            └── com/searchengine/
```