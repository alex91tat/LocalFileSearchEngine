package com.searchengine;

import com.searchengine.cli.CLI;
import com.searchengine.config.Config;
import com.searchengine.indexing.*;
import com.searchengine.repository.DatabaseManager;
import com.searchengine.repository.FileRepository;
import com.searchengine.search.*;

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
        ImageExtractor imageExtractor = new ImageExtractor(config.getImageExtensions());

        IndexingService indexingService = new IndexingService(config, crawler, filter, List.of(extractor, imageExtractor),
                detector, repository);

        AliasManager aliasManager = new AliasManager();

        QueryProcessor pipeline =
                new LogicDecorator(
                        new SynonymDecorator(
                                new SanitizationDecorator(
                                        new BaseQueryProcessor()
                                ),
                                config.getSynonyms()
                        )
                );

        QueryParser queryParser = new QueryParser(pipeline);

        SearchService searchService = new SearchService(repository, aliasManager, queryParser);

        SearchHistoryTracker historyTracker = new SearchHistoryTracker();
        searchService.addObserver(historyTracker);
        searchService.setRankingStrategy(new RelevanceRanking(historyTracker));

        CLI cli = new CLI(indexingService, searchService, historyTracker, aliasManager);
        cli.start();

        db.close();
    }
}