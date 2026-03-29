package com.searchengine;

import com.searchengine.config.Config;

public class Main {
    public static void main(String[] args) throws Exception {
        Config config = Config.load("config.json");
        config.validate();
        System.out.println(config);
    }
}