# Changelog

All notable changes to this project are documented in this file.
Versioning follows [Semantic Versioning](https://semver.org/).

---

## [3.0.0] - 2026-05-25

### Added
- Multimodal search — `ImageExtractor` extracts dominant color from images using Strategy Pattern
- `color:` search qualifier — find images by dominant color (e.g., `color:red`)
- Context-aware widgets using Factory + Observer Pattern:
    - `GalleryWidget` — activates for image-heavy results, shows color breakdown
    - `SourceCodeWidget` — activates for code-heavy results, shows language breakdown
    - `WidgetFactory` — manages widget activation based on result set analysis
- Query Pre-Processor Pipeline using Decorator Pattern:
    - `SanitizationDecorator` — strips FTS5 special characters
    - `SynonymDecorator` — expands terms with user-defined synonyms
    - `LogicDecorator` — adds wildcard prefix matching
- Producer-Consumer indexing — 3 parallel reader threads + 1 dedicated writer thread using `BlockingQueue`
- Pre-commit hook for compilation check before every commit

### Changed
- `IndexingService` refactored from single-threaded to producer-consumer architecture
- `IndexReport` counters made thread-safe with `AtomicInteger`
- `QueryParser` and `SearchService` now use injected dependencies

---

## [2.0.0] - 2026-04-29

### Added
- Query parser with `path:`, `content:` qualifiers — any permutation supported
- Path scoring at index time via `PathScorer` — depth, size, extension, recency
- Swappable ranking strategies — Relevance, Alphabetical, Date Accessed
- Search history tracking with query suggestions using Observer Pattern
- Alias system similar to DuckDuckGo bang patterns — `@alias` expands to full query
- Unit tests for query parsing and ranking strategies

### Changed
- Search results ordered by `path_score` + FTS5 BM25 rank
- `FileRepository.search()` accepts structured `ParsedQuery` instead of raw string

### Fixed
- FTS5 syntax error when searching filenames with special characters

---

## [1.0.0] - 2026-04-01

### Added
- Recursive file system crawling with symlink loop detection and permission error handling
- SQLite database with FTS5 full-text search and BM25 ranking
- `Extractor` interface using Strategy Pattern for format-specific file extraction
- Incremental indexing — only re-indexes new or modified files via timestamp comparison
- Runtime configuration via `config.json` — no recompilation needed
- Index report with detailed statistics after every indexing run
- CLI with `index` and `search` commands