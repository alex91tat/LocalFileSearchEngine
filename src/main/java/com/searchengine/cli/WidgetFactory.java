package com.searchengine.cli;

import com.searchengine.model.SearchResult;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory that manages all available widgets and decides
 * which ones to activate based on search results.
 * This is the Factory Pattern
 */
public class WidgetFactory {
    private final List<Widget> widgets;

    public WidgetFactory() {
        // register all available widgets
        this.widgets = new ArrayList<>();
        widgets.add(new GalleryWidget());
        widgets.add(new SourceCodeWidget());
    }

    /**
     * Analyzes the search results and returns all widgets
     * that should be activated for this result set.
     * Each widget independently checks shouldActivate()
     */
    public List<Widget> getActiveWidgets(List<SearchResult> results) {
        List<Widget> activeWidgets = new ArrayList<>();

        for (Widget widget : this.widgets) {
            if (widget.shouldActivate(results)) {
                activeWidgets.add(widget);
            }
        }

        return activeWidgets;
    }
}