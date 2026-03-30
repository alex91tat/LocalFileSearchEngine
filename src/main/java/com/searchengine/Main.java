package com.searchengine;

import com.searchengine.config.Config;
import com.searchengine.repository.DatabaseManager;

public class Main {
    public static void main(String[] args) throws Exception {
        Config config = Config.load("config.json");
        config.validate();

        DatabaseManager db = new DatabaseManager(config.getDatabasePath());
        db.initialize();

        db.close();
    }
}