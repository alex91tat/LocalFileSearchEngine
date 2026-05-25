# Local File Search Engine

A fast, local file search system built in Java that indexes files on your device and enables intelligent full-text search across their content, metadata, and visual properties. Built with enterprise design patterns and extensible architecture.

---

## Overview

Instead of scanning your disk every time you search, this engine indexes your files once into a SQLite database and queries it instantly. Supports advanced search qualifiers, multiple ranking strategies, query aliases, and visual widgets.

---

## Features

- **Full-text search** — powered by SQLite FTS5 with BM25 ranking and Porter stemming
- **Query qualifiers** — filter by `path:`, `content:`, or `color:` 
- **Incremental indexing** — only re-indexes changed files
- **Search history & suggestions** — auto-saves queries with frequency-based suggestions
- **Search aliases** — save complex queries as reusable shortcuts
- **Multiple ranking strategies** — Relevance, Alphabetical, or Date Accessed
- **Image indexing** — extracts and indexes dominant colors for visual search
- **Intelligent widgets** — auto-displays Gallery View for images or Code View for source files
- **File previews** — shows first 3 lines of matching files
- **Runtime configuration** — all settings in `config.json`, no recompilation needed

---

## Architecture

The project follows the **C4 model** architecture (documented in `ARCHITECTURE.md`) and is organized into the following packages:

```
com.searchengine/
├── Main.java                       — entry point, wires all components
├── cli/
│   ├── CLI.java                    — interactive user interface
│   ├── Widget.java                 — interface for result visualization (Observer pattern)
│   ├── GalleryWidget.java          — displays image results and dominant colors
│   ├── SourceCodeWidget.java       — displays code-focused results
│   └── WidgetFactory.java          — creates appropriate widgets for results
├── config/
│   └── Config.java                 — reads and holds runtime configuration
├── model/
│   ├── FileRecord.java             — represents an indexed file with all metadata
│   ├── SearchResult.java           — represents a search result shown to user
│   └── IndexReport.java            — tracks indexing statistics
├── indexing/
│   ├── IndexingService.java        — coordinates the indexing pipeline
│   ├── FileCrawler.java            — recursive file system traversal
│   ├── FileFilter.java             — filters ignored directories and extensions
│   ├── Extractor.java              — interface for format-specific extraction (Strategy pattern)
│   ├── PlainTextExtractor.java     — extracts content from text/code files
│   ├── ImageExtractor.java         — extracts dominant color from images
│   ├── ChangeDetector.java         — detects new, modified, and unchanged files
│   └── PathScorer.java             — scores files based on path relevance
├── search/
│   ├── SearchService.java          — processes queries and returns ranked results
│   ├── QueryParser.java            — parses queries with qualifiers (path:, content:, color:)
│   ├── ParsedQuery.java            — structured representation of a parsed query
│   ├── QueryProcessor.java         — interface for query transformation (Decorator pattern)
│   ├── BaseQueryProcessor.java     — base implementation of QueryProcessor
│   ├── SanitizationDecorator.java  — removes FTS5 special characters
│   ├── SynonymDecorator.java       — expands queries with synonyms
│   ├── LogicDecorator.java         — adds wildcard support for prefix matching
│   ├── RankingStrategy.java        — interface for result ranking (Strategy pattern)
│   ├── RelevanceRanking.java       — ranks by FTS5 BM25 relevance score
│   ├── AlphabeticalRanking.java    — ranks alphabetically by file name
│   ├── DateAccessedRanking.java    — ranks by most recently accessed
│   ├── SearchObserver.java         — interface for search event tracking (Observer pattern)
│   ├── SearchHistoryTracker.java   — tracks search queries and provides suggestions
│   └── AliasManager.java           — manages saved query aliases
└── repository/
    ├── DatabaseManager.java        — manages SQLite connection and schema
    └── FileRepository.java         — all SQL operations and queries
```

---

## Database Schema

Two tables power the search engine:

```sql
-- Stores file metadata and previews
files (
    id               INTEGER PRIMARY KEY,
    path             TEXT UNIQUE,
    name             TEXT,
    extension        TEXT,
    size             INTEGER,
    last_modified    INTEGER,
    preview          TEXT,
    path_score       REAL,
    dominant_color   TEXT        -- for images
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
  "imageExtensions": ["jpg", "jpeg", "png", "gif", "bmp", "webp"],
  "reportFormat": "txt",
  "synonyms": {
    "img": ["image", "photo", "picture"],
    "doc": ["document", "documentation"],
    "js": ["javascript"],
    "py": ["python"],
    "algo": ["algorithm"],
    "db": ["database"]
  }
}
```

| Field | Description |
|-------|-------------|
| `rootDirectory` | Where the crawler starts — change this to your path |
| `databasePath` | Where the SQLite `.db` file is saved |
| `ignoredDirectories` | Directories skipped entirely during crawling |
| `ignoredExtensions` | File types skipped during indexing |
| `textExtensions` | File types that get full content extraction |
| `imageExtensions` | File types processed for image analysis and dominant color extraction |
| `reportFormat` | Format of the index report |
| `synonyms` | Keyword relationships for query expansion (e.g., searching "img" also finds "image", "photo") |

---

## Quick Start

```bash
mvn compile
mvn exec:java
```

**Commands:**
- `index` — crawl and index files from `rootDirectory`
- `search` — find files with optional qualifiers
- `alias` — create shortcuts for complex queries
- `exit` — quit the application

**Search examples:**
```
content:error                    # files containing "error"
path:src content:main            # "main" in paths containing "src"
color:red                        # images with red dominant color
@myalias                         # expand saved alias
```

---

## Design Patterns & Architecture

| Pattern | Used For | Implementations |
|---------|----------|-----------------|
| **Strategy** | Result ranking | `RelevanceRanking`, `AlphabeticalRanking`, `DateAccessedRanking` |
| **Strategy** | File extraction | `PlainTextExtractor`, `ImageExtractor` |
| **Decorator** | Query processing | `SanitizationDecorator`, `SynonymDecorator`, `LogicDecorator` |
| **Observer** | Search tracking | `SearchHistoryTracker` observes `SearchService` |
| **Factory** | Widget creation | `WidgetFactory` creates `GalleryWidget`, `SourceCodeWidget` |

**Why these patterns?**
- Ranking strategies can be swapped without changing search logic
- Query transformations are modular and composable
- Search history tracking is independent from search execution
- Widgets are created/displayed based on result types

---

## Design Decisions

- **SQLite** — No server setup, single-file database created automatically
- **FTS5 + Porter stemming** — Full-text search that matches word variations (e.g., "run", "running", "runs")
- **Incremental indexing** — Compares file timestamps to avoid re-indexing unchanged files
- **Decorator chain** — Query transformations are modular (sanitize -> expand synonyms -> add wildcards)
- **Observer pattern** — Search history tracking is independent from search logic
- **Multiple ranking strategies** — Users can choose Relevance, Alphabetical, or Date Accessed ordering
- **Qualifier syntax** — `path:`, `content:`, `color:` for precise filtering without complex query language
- **Widget system** — Contextual visualization (Gallery for images, Code for source files)
- **Search aliases** — Complex queries saved and persisted as reusable shortcuts
- **Lazy widget activation** — Widgets check if they should render to avoid clutter


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