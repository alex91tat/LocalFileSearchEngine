package com.searchengine;

import com.searchengine.cli.CLI;
import com.searchengine.config.Config;
import com.searchengine.indexing.*;
import com.searchengine.repository.DatabaseManager;
import com.searchengine.repository.FileRepository;
import com.searchengine.search.SearchService;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        Config config = Config.load("config.json");
        config.validate();

        DatabaseManager db = new DatabaseManager(config.getDatabasePath());
        db.initialize();

        FileRepository repository = new FileRepository(db.getConnection());

        FileFilter filter           = new FileFilter(config);
        FileCrawler crawler         = new FileCrawler(filter);
        ChangeDetector detector     = new ChangeDetector(repository);
        PlainTextExtractor extractor = new PlainTextExtractor(config.getTextExtensions());

        IndexingService indexingService = new IndexingService(config, crawler, filter, List.of(extractor),
                detector, repository);

        SearchService searchService = new SearchService(repository);
        CLI cli = new CLI(indexingService, searchService);
        cli.start();

        db.close();
    }
}