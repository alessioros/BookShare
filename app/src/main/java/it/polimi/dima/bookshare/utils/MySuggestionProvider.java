package it.polimi.dima.bookshare.utils;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by matteo on 11/04/16.
 */
public class MySuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "it.polimi.dima.bookshare.utils.MySuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public MySuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}