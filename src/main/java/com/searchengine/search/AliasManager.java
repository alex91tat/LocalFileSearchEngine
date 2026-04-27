package com.searchengine.search;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages search aliases — shortcuts for complex queries.
 *
 * Aliases are persisted to aliases.json so they survive app restarts.
 */
public class AliasManager {
    private static final String ALIASES_FILE = "aliases.json";
    private final Map<String, String> aliases;
    private final ObjectMapper mapper;

    public AliasManager() {
        this.mapper = new ObjectMapper();
        this.aliases = loadAliases();
    }

    public boolean add(String name, String query) {
        if (name == null || name.isBlank()) {
            System.out.println("[Alias] Name cannot be empty.");
            return false;
        }

        if (query == null || query.isBlank()) {
            System.out.println("[Alias] Query cannot be empty.");
            return false;
        }

        if (aliases.containsKey(name.toLowerCase())) {
            System.out.println("[Alias] Alias already exists: @" + name);
            return false;
        }

        aliases.put(name.toLowerCase(), query);
        saveAliases();
        return true;
    }

    public boolean remove(String name) {
        if (!aliases.containsKey(name.toLowerCase())) {
            System.out.println("[Alias] Alias not found: @" + name);
            return false;
        }

        aliases.remove(name.toLowerCase());
        saveAliases();
        return true;
    }

    public Map<String, String> list() {
        return Collections.unmodifiableMap(aliases);
    }

    // Expands an alias if the input starts with @.
    // Returns the original input if not an alias.
    public String expand(String input) {
        if (input == null || !input.startsWith("@")) {
            return input;
        }
        String name = input.substring(1).toLowerCase(); // remove @
        String expanded = aliases.get(name);
        if (expanded == null) {
            System.out.println("[Alias] Unknown alias: " + input);
            return input; // return as-is if not found
        }
        System.out.println("[Alias] Expanding: " + input + " → " + expanded);
        return expanded;
    }

    private Map<String, String> loadAliases() {
        File file = new File(ALIASES_FILE);
        if (!file.exists()) {
            return new HashMap<>();
        }
        try {
            return mapper.readValue(file, mapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class));
        } catch (IOException e) {
            System.err.println("[Alias] Could not load aliases: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private void saveAliases() {
        try {
            mapper.writeValue(new File(ALIASES_FILE), aliases);
        } catch (IOException e) {
            System.err.println("[Alias] Could not save aliases: " + e.getMessage());
        }
    }
}
