package com.beerdroid.beta;

import android.content.SearchRecentSuggestionsProvider;

public class BeerDroidSearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.beerdroid.beta.BeerDroidSearchSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public BeerDroidSearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}